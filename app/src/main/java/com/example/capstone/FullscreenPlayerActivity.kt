package com.example.capstone

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.capstone.presentation.ProgressViewModel

class FullscreenPlayerActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private lateinit var progressViewModel: ProgressViewModel
    private var quizShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        progressViewModel = ViewModelProvider(this, defaultViewModelProviderFactory)[ProgressViewModel::class.java]

        setContentView(R.layout.activity_fullscreen_player)

        val uriStr = intent.getStringExtra(EXTRA_URI) ?: run {
            finish()
            return
        }

        val disasterKey = intent.getStringExtra(EXTRA_DISASTER_KEY)
        val chapterIndex = intent.getIntExtra(EXTRA_CHAPTER_INDEX, -1)

        val playerView = findViewById<PlayerView>(R.id.fullscreenPlayerView)
        val exo = ExoPlayer.Builder(this).build().also { player = it }
        playerView.player = exo

        exo.removeListener(playbackListener)
        exo.addListener(playbackListener)
        exo.setMediaItem(MediaItem.fromUri(uriStr))
        exo.prepare()
        exo.playWhenReady = true

        findViewById<android.view.View>(R.id.closeButton)?.setOnClickListener { finish() }

        if (disasterKey != null && chapterIndex >= 0) {
            fullscreenCompletionKey = disasterKey to chapterIndex
        }
    }

    private var fullscreenCompletionKey: Pair<String, Int>? = null

    private fun handleCompleted() {
        if (quizShown) return
        val (disasterKey, chapterIndex) = fullscreenCompletionKey ?: return
        quizShown = true
        progressViewModel.markChapterCompleted(disasterKey, chapterIndex)
    }

    private val playbackListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_ENDED) {
                handleCompleted()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        player?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.removeListener(playbackListener)
        player?.release()
        player = null
    }

    companion object {
        const val EXTRA_URI = "extra_uri"
        const val EXTRA_DISASTER_KEY = "extra_disaster_key"
        const val EXTRA_CHAPTER_INDEX = "extra_chapter_index"
    }
}
