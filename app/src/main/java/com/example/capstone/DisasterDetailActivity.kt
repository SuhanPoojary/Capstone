package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.capstone.model.LanguageOption

class DisasterDetailActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var currentVideoUri: String? = null

    private var selectedChapterIndex: Int? = null
    private var selectedLanguageCode: String = "en"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_disaster_detail)

        val disasterKey = intent.getStringExtra(EXTRA_DISASTER_KEY) ?: "earthquake"

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
            // TODO: hook quiz activity later
        }

        findViewById<android.view.View>(R.id.ccBadge)?.setOnClickListener {
            showSubtitleInfo()
        }

        findViewById<android.view.View>(R.id.fullscreenBtn)?.setOnClickListener {
            val uri = currentVideoUri ?: return@setOnClickListener
            startActivity(Intent(this, FullscreenPlayerActivity::class.java).apply {
                putExtra(FullscreenPlayerActivity.EXTRA_URI, uri)
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

        val mediaItem = MediaItem.fromUri(uri)
        exo.setMediaItem(mediaItem)
        exo.prepare()
        exo.playWhenReady = true

        currentVideoUri = uri.toString()

        // highlight chapters
        findViewById<TextView>(R.id.chapter1).isSelected = idx == 0
        findViewById<TextView>(R.id.chapter2).isSelected = idx == 1
        findViewById<TextView>(R.id.chapter3).isSelected = idx == 2
    }

    private fun bindDescription(disasterKey: String) {
        val title = findViewById<TextView>(R.id.descTitle)
        val body = findViewById<TextView>(R.id.descBody)

        when (disasterKey.lowercase()) {
            "earthquake" -> {
                title.text = "Earthquake basics"
                body.text = "Earthquakes are sudden ground shaking caused by movement in the Earth's crust.\n\nStay safer by securing heavy items, knowing safe spots indoors, and practicing Drop–Cover–Hold On."
            }
            "flood", "floods" -> {
                title.text = "Flood basics"
                body.text = "Floods happen when water overflows onto land due to heavy rainfall, storm surges, or dam/river overflow.\n\nMove to higher ground, avoid walking/driving through water, and keep emergency supplies ready."
            }
            "cyclone" -> {
                title.text = "Cyclone basics"
                body.text = "Cyclones bring strong winds, heavy rain, and storm surges.\n\nFollow official alerts, secure windows/roof items, and evacuate early if advised."
            }
            "landslide", "landslides" -> {
                title.text = "Landslide basics"
                body.text = "Landslides occur when soil/rock moves downhill, often after heavy rain or earthquakes.\n\nAvoid steep slopes during intense rain, watch for cracks, and move away from slide paths."
            }
            else -> {
                title.text = "Disaster basics"
                body.text = "Select a chapter below to start the lesson."
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
            .setTitle("Video not found")
            .setMessage(
                "This chapter video isn't available on the device yet.\n\n" +
                    "Expected naming: {disaster}_ch{n}_{phase}_{lang}.mp4 in res/raw/\n" +
                    "Example: flood_ch2_during_en.mp4"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSubtitleInfo() {
        AlertDialog.Builder(this)
            .setTitle("Subtitles")
            .setMessage(
                "Since you have separate videos per language (different audio), we don't need subtitles to switch languages.\n\n" +
                    "If you still want captions, we can add VTT/SRT later."
            )
            .setPositiveButton("OK", null)
            .show()
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
        const val EXTRA_DISASTER_KEY = "extra_disaster_key"

        fun newIntent(from: AppCompatActivity, disasterKey: String): Intent {
            return Intent(from, DisasterDetailActivity::class.java).apply {
                putExtra(EXTRA_DISASTER_KEY, disasterKey)
            }
        }
    }
}
