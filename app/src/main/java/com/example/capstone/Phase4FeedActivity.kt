package com.example.capstone

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Phase4FeedActivity : AppCompatActivity() {
    private val firestore = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phase4_feed)

        val rv = findViewById<RecyclerView>(R.id.rvFeed)
        val empty = findViewById<TextView>(R.id.tvFeedEmpty)
        val progress = findViewById<ProgressBar>(R.id.pbFeedLoading)
        val btnRefresh = findViewById<MaterialButton>(R.id.btnFeedRefresh)

        rv.layoutManager = LinearLayoutManager(this)
        val adapter = FeedAdapter()
        rv.adapter = adapter

        fun showLoading(loading: Boolean) {
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        }

        fun loadFeed() {
            showLoading(true)
            val user = FirebaseAuth.getInstance().currentUser
            if (firestore == null || user == null) {
                adapter.submitList(listOf(
                    FeedItem("Welcome to SafeReady", "This is a sample feed entry."),
                    FeedItem("Leaderboard Updated", "A new leaderboard snapshot is available."),
                ))
                empty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
                showLoading(false)
                return
            }

            firestore.collection("feed").orderBy("timestamp")
                .get()
                .addOnSuccessListener { snap ->
                    val items = snap.documents.map { doc ->
                        FeedItem(doc.getString("title") ?: "Update", doc.getString("body") ?: "")
                    }
                    adapter.submitList(items)
                    empty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
                .addOnFailureListener {
                    adapter.submitList(emptyList())
                    empty.visibility = View.VISIBLE
                }
                .addOnCompleteListener { showLoading(false) }
        }

        btnRefresh.setOnClickListener { loadFeed() }
        loadFeed()
    }
}

data class FeedItem(val title: String, val body: String)

