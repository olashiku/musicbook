package com.olashiku.musicbook.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.olashiku.musicbook.R
import com.olashiku.musicbook.service.AudioService


interface MediaNotification {
    fun setupNotification()
    fun setupNotificationChannel(context: Context)
    fun updateRemoteViews(musicDetails:List<String>,isMusicPlaying: Boolean)
     fun setUpRemoteView(remoteView: RemoteViews)
    fun startNotificationProcedure()
    fun cancelNotficationProcedure()
}

class MediaNotificationInitiation(private val context: Context) : MediaNotification {
     val ACTION_PLAY_PAUSE = context.getString(R.string.action_play_pause)
     val ACTION_PLAY_PREVIOUS = context.getString(R.string.action_play_previous)
     val ACTION_PLAY_NEXT = context.getString(R.string.action_play_next)

    private val NOTIFICATION_CHANNEL_ID = context.getString(R.string.action_channel_id)

    var builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
    var notificationManager: NotificationManager? = null
    private var removeView: RemoteViews? = null

    override fun setupNotification() {
        setupNotificationChannel(context)
        builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        removeView = RemoteViews(NOTIFICATION_CHANNEL_ID, R.layout.small_notification_view)
        setUpRemoteView(removeView!!)
        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(removeView!!)
            .setSilent(true)
            .setOngoing(true)
            .build()
//        updateRemoteViews()
    }

    override fun setupNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val descriptionText = context.getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            channel.setSound(null, null)
            notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    override fun updateRemoteViews(musicDetails:List<String>,isMusicPlaying: Boolean) {
        removeView!!.setTextViewText(R.id.lblWidgetCurrentMusicName, musicDetails.get(0))
        removeView!!.setTextViewText(R.id.lblWidgetCurrentArtistName, musicDetails.get(1))
        removeView!!.setImageViewResource(
            R.id.btnWidgetPlayPauseMusic,
            if (isMusicPlaying) R.drawable.play_icon else R.drawable.pause_icon
        )
        notificationManager?.run {
            notify(0, builder.build())
        }
    }

    override  fun setUpRemoteView(remoteView: RemoteViews) {
        val playNextIntent = Intent(context, AudioService::class.java)
        playNextIntent.action = ACTION_PLAY_NEXT
        val pNextIntent = PendingIntent.getService(context, 0, playNextIntent, 0)
        val playPrevIntent = Intent(context, AudioService::class.java)
        playPrevIntent.action = ACTION_PLAY_PREVIOUS
        val pPrevIntent = PendingIntent.getService(context, 0, playPrevIntent, 0)
        val playPauseIntent = Intent(context, AudioService::class.java)
        playPauseIntent.action = ACTION_PLAY_PAUSE
        val pplayPauseIntent = PendingIntent.getService(context, 0, playPauseIntent, 0)
        remoteView.setImageViewResource(R.id.btnWidgetCloseService, R.drawable.ic_clear_icon)
        remoteView.setImageViewResource(R.id.btnWidgetPlayPrevious, R.drawable.play_icon)
        remoteView.setImageViewResource(R.id.btnWidgetPlayNext, R.drawable.next_icon)
        remoteView.setOnClickPendingIntent(R.id.btnWidgetPlayPrevious, pPrevIntent)
        remoteView.setOnClickPendingIntent(R.id.btnWidgetPlayNext, pNextIntent)
        remoteView.setOnClickPendingIntent(R.id.btnWidgetPlayPauseMusic, pplayPauseIntent)
    }

    override fun startNotificationProcedure() {
        with(NotificationManagerCompat.from(context)) {
            notify(0, builder.build())
        }
    }

    override fun cancelNotficationProcedure() {
        NotificationManagerCompat.from(context).cancel(0)
    }
}