package com.example.capstone

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class CrashViewerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crash_viewer)

        val text = findViewById<TextView>(R.id.crashText)
        text.text = loadLatestCrash() ?: "No crash logs found yet.\n\n1) Reproduce the crash\n2) Re-open this screen"
    }

    private fun loadLatestCrash(): String? {
        val dir = File(filesDir, "crash")
        val latest = dir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".txt") }
            ?.maxByOrNull { it.lastModified() }
            ?: return null

        return runCatching { latest.readText() }.getOrNull()
    }
}

