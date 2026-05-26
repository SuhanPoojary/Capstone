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

class Phase4FriendsActivity : AppCompatActivity() {
    private val firestore = runCatching { FirebaseFirestore.getInstance() }.getOrNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phase4_friends)

        val rv = findViewById<RecyclerView>(R.id.rvFriends)
        val empty = findViewById<TextView>(R.id.tvFriendsEmpty)
        val progress = findViewById<ProgressBar>(R.id.pbFriendsLoading)
        val btnRefresh = findViewById<MaterialButton>(R.id.btnFriendsRefresh)

        rv.layoutManager = LinearLayoutManager(this)
        val adapter = FriendsAdapter()
        rv.adapter = adapter

        fun showLoading(loading: Boolean) {
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        }

        fun loadFriends() {
            showLoading(true)
            val user = FirebaseAuth.getInstance().currentUser
            if (firestore == null || user == null) {
                // Show mock data when Firebase not available or user not signed in
                adapter.submitList(listOf(
                    Friend("1", "Alice", "Friend"),
                    Friend("2", "Bob", "Requested"),
                    Friend("3", "Charlie", "Friend")
                ))
                empty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
                showLoading(false)
                return
            }

            val friendsRef = firestore.collection("users").document(user.uid).collection("friends")
            friendsRef.get()
                .addOnSuccessListener { snap ->
                    val items = snap.documents.map { doc ->
                        Friend(doc.id, doc.getString("name") ?: "Unnamed", doc.getString("status") ?: "Friend")
                    }
                    adapter.submitList(items)
                    empty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
                }
                .addOnFailureListener {
                    // fallback to empty state
                    adapter.submitList(emptyList())
                    empty.visibility = View.VISIBLE
                }
                .addOnCompleteListener { showLoading(false) }
        }

        btnRefresh.setOnClickListener { loadFriends() }

        // initial load
        loadFriends()
    }
}

data class Friend(val id: String, val name: String, val status: String)

