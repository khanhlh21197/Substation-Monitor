package com.khanhlh.substationmonitor.helper.recyclerview

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.khanhlh.substationmonitor.BR
import com.khanhlh.substationmonitor.R
import com.khanhlh.substationmonitor.ui.main.MainActivity
import java.util.*

class BaseBindingAdapter<T>(context: Context, @LayoutRes resId: Int) :
    RecyclerView.Adapter<BaseBindingAdapter.ViewHolder>() {
    private var data: List<T>? = null
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    @LayoutRes
    private val resId: Int = resId
    private var onItemClickListener: OnItemClickListener<T>? = null
    private val mContext: Context = context
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<T>?) {
        this.onItemClickListener = onItemClickListener
    }

    fun setData(data: List<T>?) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            DataBindingUtil.inflate(
                inflater,
                resId,
                parent,
                false
            )
        )
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = data!![position]
                holder.binding.setVariable(BR.item, item);
        holder.binding.executePendingBindings()
    }

    override fun getItemCount(): Int {
        return if (data == null) 0 else data!!.size
    }

    class ViewHolder(var binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root)

    private fun createNotification(ng: String, idDevice: String) {
        val mBuilder =
            NotificationCompat.Builder(
                Objects.requireNonNull(mContext)
                    .applicationContext, "notify_001"
            )
        val ii = Intent(mContext.applicationContext, MainActivity::class.java)
        ii.putExtra("menuFragment", "DetailDeviceFragment")
        ii.putExtra("idDevice", idDevice)
        val pendingIntent = PendingIntent.getActivity(mContext, 0, ii, 0)
        val bigText =
            NotificationCompat.BigTextStyle()
        bigText.bigText("Nhiệt độ đo được: $ng tại thiết bị: $idDevice")
        bigText.setBigContentTitle("Nhiệt độ vượt ngưỡng !")
        bigText.setSummaryText("Cảnh báo")
        mBuilder.setContentIntent(pendingIntent)
        mBuilder.setSmallIcon(R.drawable.ic_warning_red)
        mBuilder.setContentTitle(mContext.getString(R.string.app_name))
        mBuilder.setContentText("Nhiệt độ vượt ngưỡng !")
        mBuilder.priority = Notification.PRIORITY_MAX
        mBuilder.setStyle(bigText)
        val mNotificationManager =
            mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

// === Removed some obsoletes
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
        mNotificationManager?.notify(0, mBuilder.build())
    }

    interface OnItemClickListener<T> {
        fun onItemClick(item: T)
        fun onItemEmptyClick(item: T)
        fun onItemLongClick(item: T)
    }

    companion object {
        private const val EMPTY_VIEW = 10
    }

}