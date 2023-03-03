package com.olashiku.musicbook.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.olashiku.musicbook.databinding.ActivityMainBinding
import com.olashiku.musicbook.service.AudioService
import com.olashiku.musicbook.utils.utilz


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var audioService: AudioService? = null
    private var isBound = false
    private var isPause = false


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioServiceBinder
            audioService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupClickListener()

        val intent = Intent(applicationContext, AudioService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        utilz.delayForALittleBit {
            playAudioFromService()
        }
    }




    private fun setupClickListener() {
        binding.playButton.setOnClickListener {
            isPause = when (isPause) {
                false -> {
                    audioService!!.pause()
                    true
                }
                true -> {
                    audioService!!.resume()
                    false
                }
            }
        }

    }

    private fun playAudioFromService() {
        if (isBound) {

            playAudio(
                "https://etc.usf.edu/lit2go/audio/mp3/the-adventures-of-huckleberry-finn-002-chapter-1.99.mp3",
                "https://musicuploaddirectory.s3.amazonaws.com/nature-calls-136344.mp3"
            )

        } else {
            Toast.makeText(this, "Audio service not bound", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playAudio(bookUrl: String, backgroundUrl: String) {
        audioService?.setPrimaryAudio(bookUrl)
        audioService?.setBackgroundAudio(backgroundUrl)
        audioService?.playMusic()

    }

    override fun onResume() {
        super.onResume()
        audioService?.let {
            it.cancelNotficationProcedure()
        }
    }

    override fun onPause() {
        super.onPause()
        audioService?.let {
            it.startNotificationProcedure()
        }
    }
}


