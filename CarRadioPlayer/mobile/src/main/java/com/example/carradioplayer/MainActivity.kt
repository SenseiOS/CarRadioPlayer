package com.example.carradioplayer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.carradioplayer.shared.MyMusicService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var play=true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(Intent(this, MyMusicService::class.java))
    }

    override fun onDestroy() {
        stopService(Intent(this, MyMusicService::class.java))
        super.onDestroy()
    }
    fun playBtnClick(v: View){
        if (play) pause() else play()
    }

    private fun play() {
        startService(Intent(this, MyMusicService::class.java))
        playBtn.setBackgroundResource(R.drawable.stop)
        play=true
    }

    private fun pause() {
        startService(Intent(this, MyMusicService::class.java))
        playBtn.setBackgroundResource(R.drawable.play)
        play=false
    }

}
