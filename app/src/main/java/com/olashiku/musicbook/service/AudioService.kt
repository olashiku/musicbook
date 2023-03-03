package com.olashiku.musicbook.service

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.olashiku.musicbook.notification.MediaNotificationInitiation
import com.olashiku.musicbook.utils.utilz


interface mediaPlayerCallback {
    fun playMusic()
    fun pause()
    fun resume()
    fun stop()
    fun totalDuration(): Int
}

class AudioService : Service(), mediaPlayerCallback {

    private var mediaNotification: MediaNotificationInitiation? = null

    private var isMusicPlaying: Boolean = false
    private var primaryMediaPlayer: MediaPlayer? = null
    private var backgroundMediaPlayer: MediaPlayer? = null
    private var primaryMediaDuration = 0
    private var backgroundMediaDuration = 0
    private var backgroundSeekPosition = 0
    private var primarySeekPosition = 0

    override fun onBind(intent: Intent?): IBinder {
        return AudioServiceBinder()
    }

    inner class AudioServiceBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }

    fun setPrimaryAudio(musicUrl: String) {
        resetMediaPlayer()
        primaryMediaPlayer = MediaPlayer().apply {
            setAudioAttributes(getAudioAttributes())
            setDataSource(musicUrl)
            prepareAsync()
        }
        primaryMediaDuration = primaryMediaPlayer!!.duration
    }

    fun setBackgroundAudio(backgroundMusicUrl: String) {
        backgroundMediaPlayer = MediaPlayer().apply {
            setAudioAttributes(getAudioAttributes())
            setDataSource( backgroundMusicUrl)
            prepareAsync()
            setVolume(0.3f, 0.3f)
        }
        backgroundMediaDuration = primaryMediaPlayer!!.duration
    }

    fun MediaPlayer.restartPlaying(position: Int) {
        this.seekTo(position)
        this.start()
    }

    fun resetMediaPlayer() {
        primaryMediaPlayer?.let {
            if (primaryMediaPlayer!!.isPlaying && backgroundMediaPlayer!!.isPlaying) {
                primaryMediaPlayer!!.stop()
                backgroundMediaPlayer!!.stop()
            }
        }

    }

    override fun playMusic() {
        isMusicPlaying = true
        primaryMediaPlayer!!.setOnPreparedListener { it.start() }
        backgroundMediaPlayer!!.setOnPreparedListener { it.start() }

        backgroundMediaPlayer!!.setOnCompletionListener {
            utilz.delayForALittleBit(2000) {
                backgroundMediaPlayer!!.restartPlaying(0)
            }
        }


        primaryMediaPlayer!!.setOnCompletionListener {
            if(!it.isPlaying){
                backgroundMediaPlayer!!.stop()
            }

        }
    }

    private fun getAudioAttributes(): AudioAttributes? {
        return AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
    }

    override fun pause() {
        isMusicPlaying = false
        with(backgroundMediaPlayer) {
            this!!.pause()
            backgroundSeekPosition = this.currentPosition
        }

        with(primaryMediaPlayer) {
            this!!.pause()
            primarySeekPosition = this.currentPosition
        }
        mediaNotification?.updateRemoteViews(listOf("somthing ooo!","adventure"),isMusicPlaying)
    }

    override fun resume() {
        isMusicPlaying = true
        backgroundMediaPlayer!!.restartPlaying(backgroundSeekPosition)
        primaryMediaPlayer!!.restartPlaying(primarySeekPosition)
        mediaNotification?.updateRemoteViews(listOf("somthing ooo!","adventure"),isMusicPlaying)
    }

    override fun stop() {
        backgroundMediaPlayer!!.stop()
        primaryMediaPlayer!!.stop()
    }

    override fun totalDuration(): Int {
        return primaryMediaPlayer!!.duration
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mediaNotification = MediaNotificationInitiation(this)
        mediaNotification!!.setupNotification()

        val action = intent!!.action
        if (action != null) {
            when (action) {
                mediaNotification?.ACTION_PLAY_PAUSE -> {
                    isMusicPlaying = when (isMusicPlaying) {
                        true -> {
                            pause()
                            false
                        }
                        false -> {
                            resume()
                            true
                        }
                    }
                }
                mediaNotification?.ACTION_PLAY_NEXT -> {
                    println("intent_next")
                }
                mediaNotification?.ACTION_PLAY_PREVIOUS -> {
                    println("intent_last")
                }
            }
        }
        return START_STICKY;
    }

    fun startNotificationProcedure() {
        mediaNotification?.let { it.startNotificationProcedure() }
    }

    fun cancelNotficationProcedure() {
        mediaNotification?.let { it.cancelNotficationProcedure() }
    }
}