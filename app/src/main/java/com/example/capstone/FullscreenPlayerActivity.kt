package com.example.capstone

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class FullscreenPlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val layoutId = resources.getIdentifier("activity_fullscreen_player", "layout", packageName)
        if (layoutId == 0) {
            finish()
            return
        }
        setContentView(layoutId)

        val uriStr = intent.getStringExtra(EXTRA_URI) ?: run {
            finish()
            return
        }

        val playerViewId = resources.getIdentifier("fullscreenPlayerView", "id", packageName)
        val closeId = resources.getIdentifier("closeButton", "id", packageName)
        if (playerViewId == 0) {
            finish()
            return
        }

        val playerView = findViewById<PlayerView>(playerViewId)
        val exo = ExoPlayer.Builder(this).build().also { player = it }
        playerView.player = exo

        exo.setMediaItem(MediaItem.fromUri(uriStr))
        exo.prepare()
        exo.playWhenReady = true

        if (closeId != 0) {
            findViewById<android.view.View>(closeId)?.setOnClickListener { finish() }
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
    }

    companion object {
        const val EXTRA_URI = "extra_uri"
    }
}
