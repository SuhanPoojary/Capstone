package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.capstone.model.LanguageOption
import com.example.capstone.presentation.ProgressViewModel

class DisasterDetailActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var currentVideoUri: String? = null
    private lateinit var progressViewModel: ProgressViewModel

    private var selectedChapterIndex: Int? = null
    private var selectedLanguageCode: String = "en"
    private var quizShownForChapter: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_disaster_detail)
        progressViewModel = ViewModelProvider(this, defaultViewModelProviderFactory)[ProgressViewModel::class.java]

        val disasterKey = intent.getStringExtra(QuizActivity.EXTRA_DISASTER_KEY) ?: "earthquake"

        findViewById<android.view.View>(R.id.backButton)?.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<TextView>(R.id.screenTitle).text = disasterKey.replaceFirstChar { it.uppercaseChar() }

        // Description shown by default (no autoplay)
        bindDescription(disasterKey)
        showDescriptionMode()

        // Chapters -> language dropdown -> play
        findViewById<TextView>(R.id.chapter1).setOnClickListener { onChapterClicked(disasterKey, 0) }
        findViewById<TextView>(R.id.chapter2).setOnClickListener { onChapterClicked(disasterKey, 1) }
        findViewById<TextView>(R.id.chapter3).setOnClickListener { onChapterClicked(disasterKey, 2) }

        // CTA now means "start lesson" (plays chapter 1 with language picker)
        findViewById<android.view.View>(R.id.takeQuizBtn)?.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java).apply {
                putExtra(QuizActivity.EXTRA_DISASTER_KEY, disasterKey)
                putExtra(QuizActivity.EXTRA_TOPIC, disasterKey)
            }
            startActivity(intent)
        }

        findViewById<android.view.View>(R.id.ccBadge)?.setOnClickListener {
            showSubtitleInfo()
        }

        findViewById<android.view.View>(R.id.fullscreenBtn)?.setOnClickListener {
            val uri = currentVideoUri ?: return@setOnClickListener
            startActivity(Intent(this, FullscreenPlayerActivity::class.java).apply {
                putExtra(FullscreenPlayerActivity.EXTRA_URI, uri)
                putExtra(FullscreenPlayerActivity.EXTRA_DISASTER_KEY, disasterKey)
                putExtra(FullscreenPlayerActivity.EXTRA_CHAPTER_INDEX, selectedChapterIndex ?: 0)
            })
        }
    }

    private fun onChapterClicked(disasterKey: String, chapterIndex: Int) {
        selectedChapterIndex = chapterIndex

        val langs = DemoVideoRepository.getAvailableLanguages(this, disasterKey, chapterIndex)
        if (langs.isEmpty()) {
            showMissingVideoDialog(disasterKey, chapterIndex)
            return
        }

        // If only one option exists, play directly
        if (langs.size == 1) {
            val only = langs.first()
            selectedLanguageCode = only.code
            playChapter(disasterKey, chapterIndex, only.code)
            return
        }

        showLanguagePicker(langs) { picked ->
            selectedLanguageCode = picked.code
            playChapter(disasterKey, chapterIndex, picked.code)
        }
    }

    private fun showLanguagePicker(options: List<LanguageOption>, onPick: (LanguageOption) -> Unit) {
        val labels = options.map { it.label }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Select audio language")
            .setItems(labels) { _, which ->
                onPick(options[which])
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun playChapter(disasterKey: String, idx: Int, languageCode: String) {
        val playerView = findViewById<PlayerView>(R.id.playerView)

        val uri = DemoVideoRepository.getVideoUri(this, disasterKey, idx, languageCode)
        if (uri == null) {
            showMissingVideoDialog(disasterKey, idx)
            return
        }

        showPlayerMode()

        val exo = (player ?: ExoPlayer.Builder(this).build().also { player = it })
        playerView.player = exo
        exo.clearMediaItems()
        exo.removeListener(playbackListener)
        exo.addListener(playbackListener)

        val mediaItem = MediaItem.fromUri(uri)
        exo.setMediaItem(mediaItem)
        exo.prepare()
        exo.playWhenReady = true

        currentVideoUri = uri.toString()

        // highlight chapters
        findViewById<TextView>(R.id.chapter1).isSelected = idx == 0
        findViewById<TextView>(R.id.chapter2).isSelected = idx == 1
        findViewById<TextView>(R.id.chapter3).isSelected = idx == 2
        quizShownForChapter = null
    }

    private fun onPlaybackCompleted(disasterKey: String, chapterIndex: Int) {
        if (quizShownForChapter == chapterIndex) return
        quizShownForChapter = chapterIndex
        progressViewModel.markChapterCompleted(disasterKey, chapterIndex)
        // Removed automatic QuizBottomSheetDialogFragment popup as per user request
    }

    private val playbackListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_ENDED) {
                val chapterIndex = selectedChapterIndex ?: return
                onPlaybackCompleted(intent.getStringExtra(QuizActivity.EXTRA_DISASTER_KEY) ?: "earthquake", chapterIndex)
            }
        }
    }

    private fun bindDescription(disasterKey: String) {
        val title = findViewById<TextView>(R.id.descTitle)
        val body = findViewById<TextView>(R.id.descBody)

        when (disasterKey.lowercase()) {
            "earthquake" -> {
                title.setText(R.string.lesson_earthquake_title)
                body.setText(R.string.lesson_earthquake_body)
            }
            "flood", "floods" -> {
                title.setText(R.string.lesson_flood_title)
                body.setText(R.string.lesson_flood_body)
            }
            "cyclone" -> {
                title.setText(R.string.lesson_cyclone_title)
                body.setText(R.string.lesson_cyclone_body)
            }
            "landslide", "landslides" -> {
                title.setText(R.string.lesson_landslide_title)
                body.setText(R.string.lesson_landslide_body)
            }
            else -> {
                title.setText(R.string.lesson_default_title)
                body.setText(R.string.lesson_default_body)
            }
        }
    }

    private fun showDescriptionMode() {
        findViewById<android.view.View>(R.id.descriptionBlock).visibility = android.view.View.VISIBLE
        findViewById<android.view.View>(R.id.playerView).visibility = android.view.View.GONE
        currentVideoUri = null
    }

    private fun showPlayerMode() {
        findViewById<android.view.View>(R.id.descriptionBlock).visibility = android.view.View.GONE
        findViewById<android.view.View>(R.id.playerView).visibility = android.view.View.VISIBLE
    }

    private fun showMissingVideoDialog(disasterKey: String, chapterIndex: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.missing_video_title)
            .setMessage(getString(R.string.missing_video_message, disasterKey, chapterIndex + 1, chapterPhase(chapterIndex), selectedLanguageCode))
            .setPositiveButton(R.string.missing_video_ok, null)
            .show()
    }

    private fun showSubtitleInfo() {
        AlertDialog.Builder(this)
            .setTitle(R.string.subtitles_title)
            .setMessage(R.string.subtitles_message)
            .setPositiveButton(R.string.missing_video_ok, null)
            .show()
    }

    private fun chapterPhase(chapterIndex: Int): String {
        return when (chapterIndex) {
            0 -> "general"
            1 -> "during"
            2 -> "after"
            else -> "general"
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
        const val EXTRA_DISASTER_KEY = "extra_disaster_key"

        fun newIntent(from: AppCompatActivity, disasterKey: String): Intent {
            return Intent(from, DisasterDetailActivity::class.java).apply {
                putExtra(EXTRA_DISASTER_KEY, disasterKey)
            }
        }
    }
}
