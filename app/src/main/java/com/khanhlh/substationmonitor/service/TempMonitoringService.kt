package com.khanhlh.substationmonitor.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.ui.main.fragments.home.HomeViewModel
import com.khanhlh.substationmonitor.warning.WarningService
import java.io.IOException
import java.io.Serializable
import java.util.*

class TempMonitoringService : LifecycleService(), Serializable {
    private var timer: Timer? = null
    var counter = 0
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    fun startTimer() {
        timer = Timer()
        val timerTask: TimerTask = object : TimerTask() {
            override fun run() {
                Log.i("Count", "=========  " + counter++)
            }
        }
        timer!!.schedule(timerTask, 1000, 1000) //
    }

    fun stoptimertask() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(
            1,
            Notification()
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        isRunning = true
        initMedia()
        if (intent != null
            && intent.extras != null
            && intent.extras!!.getString("idDevice") != null
        ) {
            val idDevice =
                Objects.requireNonNull(intent.extras).getString("idDevice")
            Log.v("TempMonitoringService", "onStartCommand")
            startTimer()
            val viewModel = HomeViewModel()
            if (idDevice != null) {
                viewModel.observerAllDevices()
            }
        }
        return Service.START_STICKY
    }

    @SuppressLint("WrongConstant")
    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(1)
            stopForeground(9)
        }
        if (v != null && v!!.hasVibrator()) {
            v!!.cancel()
        }
        isRunning = false
        stopService(warningService)
        Log.v("TempMonitoringService", "onDestroy")
        super.onDestroy()
        stoptimertask()
        //auto restart service
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction("restartservice");
//        broadcastIntent.setClass(this, Restarter.class);
//        this.sendBroadcast(broadcastIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getData(
        dataSnapshot: DataSnapshot,
        callBack: FireBaseCallBack<ArrayList<Device>>
    ) {
        val t: GenericTypeIndicator<ArrayList<Device>> =
            object : GenericTypeIndicator<ArrayList<Device?>?>() {}
        val devices: ArrayList<Device> = dataSnapshot.getValue(t)
        callBack.afterDataChanged(devices)
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun createNotification(device: Device, idNoti: Int) {
        val notificationLayout =
            RemoteViews(packageName, R.layout.notification_monitoring)
        notificationLayout.setTextViewText(
            R.id.message, device.getNO()
                .toString() + " độ trên thiết bị "
                    + device.getName()
        )
        val stopWarning = Intent(this, NotificationIntentService::class.java)
        stopWarning.action = "stopWarning"
        notificationLayout.setOnClickPendingIntent(
            R.id.removeWarning,
            PendingIntent.getService(this, 0, stopWarning, PendingIntent.FLAG_UPDATE_CURRENT)
        )
        //        notificationLayout.setString(R.id.message, null, "Nhiệt độ đo được: " + ng + " trên thiết bị: " + idDevice);
        val mBuilder =
            NotificationCompat.Builder(applicationContext, "notify_001")
        val ii = Intent(applicationContext, MainActivity::class.java)
        ii.putExtra("menuFragment", "DetailDeviceFragment")
        ii.putExtra("idDevice", device.getId())
        mBuilder.setSmallIcon(R.drawable.ic_warning_red)
        mBuilder.setCustomContentView(notificationLayout)
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "Your_channel_id"
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_HIGH
            )
            mNotificationManager?.createNotificationChannel(channel)
            mBuilder.setChannelId(channelId)
        }
        mNotificationManager?.notify(idNoti, mBuilder.build())
        startMedia()
        vibrate()
    }

    private fun initMedia() {
        mediaPlayer = MediaPlayer.create(this, R.raw.warning)
        mediaPlayer!!.setOnCompletionListener { mp: MediaPlayer? -> stopSelf() }
        warningService = Intent(this, WarningService::class.java)
    }

    private fun startMedia() {
        if (!WarningService.isRunning) {
            startService(warningService)
        }
        //        try {
//            if (mediaPlayer == null) {
//                initMedia();
//                if (!mediaPlayer.isPlaying()) {
//                    mediaPlayer.start();
//                    Log.d("mediaPlayer", "started");
//                }
//            } else {
//                if (!mediaPlayer.isPlaying()) {
//                    mediaPlayer.start();
//                    Log.d("mediaPlayer", "started");
//                }
//            }
//        } catch (IllegalStateException e) {
//            e.printStackTrace();
//        }
    }

    private fun vibrate() {
        v =
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (!CommonActivity.isNullOrEmpty(v)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v!!.vibrate(
                    VibrationEffect.createOneShot(
                        500,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                //deprecated in API 26
                v!!.vibrate(3000)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "example.smarthome"
        val channelName = "Background TempMonitoring Service"

        //get Bundle Data
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager =
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        manager.createNotificationChannel(chan)
        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.drawable.ic_warning_red)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Đang theo dõi nhiệt độ")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE) //                .setStyle(bigText)
            .build()
        startForeground(9, notification)
    }

    private fun onButtonNotificationClick(@IdRes id: Int): PendingIntent {
        val intent = Intent()
        intent.putExtra(EXTRA_BUTTON_CLICKED, id)
        return PendingIntent.getBroadcast(this, id, intent, 0)
    }

    companion object {
        const val EXTRA_BUTTON_CLICKED = "EXTRA_BUTTON_CLICKED"
        var isRunning = false
        var v: Vibrator? = null
        var mediaPlayer: MediaPlayer? = null
        var warningService: Intent? = null
        fun stopMedia() {
            try {
                if (mediaPlayer != null) {
                    if (mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.stop()
                        mediaPlayer!!.prepare()
                        Log.d("mediaPlayer", "stopped")
                    }
                    mediaPlayer!!.release()
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}