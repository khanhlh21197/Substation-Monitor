package com.khanhlh.substationmonitor.warning

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.os.IBinder
import android.util.Log
import com.khanhlh.substationmonitor.R

class WarningService : Service(), OnCompletionListener {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    var mediaPlayer: MediaPlayer? = null
    private val fname = "warning.mp3"
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
//        int resID = getResources().getIdentifier(fname, "raw", getPackageName());
        mediaPlayer = MediaPlayer.create(this, R.raw.warning)
        mediaPlayer!!.setOnCompletionListener(this)
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        isRunning = true
        if (!mediaPlayer!!.isPlaying) {
            mediaPlayer!!.start()
            Log.d("mediaPlayer", "started")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        isRunning = false
        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.stop()
            Log.d("mediaPlayer", "stopped")
        }
        mediaPlayer!!.release()
    }

    override fun onCompletion(mp: MediaPlayer) {
        stopSelf()
    }

    companion object {
        var isRunning = false
    }
}