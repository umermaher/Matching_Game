package com.example.matchinggame
import android.app.Service;
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class SoundService : Service() {
    private var player:MediaPlayer?=null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        player=MediaPlayer.create(applicationContext,R.raw.iltijajokhay)

        player?.start()

        player?.setOnCompletionListener {
            player?.start()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        stopSelf()
        player?.stop()
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? = null

}