package com.example.capstone

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.capstone.data.GlobalLeaderboardEntry
import com.example.capstone.data.repository.FirebaseGlobalLeaderboardRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class Phase4LeaderboardActivity : AppCompatActivity() {
    private val leaderboardRepo by lazy {
        FirebaseGlobalLeaderboardRepository(FirebaseFirestore.getInstance())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phase4_leaderboard)

        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        val loading = findViewById<ProgressBar>(R.id.phase4LeaderboardLoading)
        val errorText = findViewById<TextView>(R.id.phase4LeaderboardError)
        val rankText = findViewById<TextView>(R.id.phase4MyGlobalRank)
        val listContainer = findViewById<LinearLayout>(R.id.phase4LeaderboardList)

        loading.visibility = View.VISIBLE
        errorText.visibility = View.GONE
        listContainer.removeAllViews()

        lifecycleScope.launch {
            val leaderboardResult = leaderboardRepo.getGlobalLeaderboard(limit = 20)
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val rankResult = if (userId.isNullOrBlank()) null else leaderboardRepo.getUserGlobalRank(userId)

            loading.visibility = View.GONE

            rankText.text = when {
                rankResult == null -> "My rank: sign in to view"
                rankResult.isSuccess && rankResult.getOrNull() != null && rankResult.getOrNull()!! > 0 -> {
                    "My rank: #${rankResult.getOrNull()}"
                }
                else -> "My rank: not ranked yet"
            }

            leaderboardResult.onSuccess { entries ->
                if (entries.isEmpty()) {
                    errorText.visibility = View.VISIBLE
                    errorText.text = "No leaderboard entries found yet."
                    return@onSuccess
                }
                renderRows(entries, listContainer)
            }.onFailure {
                errorText.visibility = View.VISIBLE
                errorText.text = "Failed to load leaderboard. Check Firebase data and connection."
            }
        }
    }

    private fun renderRows(entries: List<GlobalLeaderboardEntry>, container: LinearLayout) {
        entries.forEach { entry ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_white_card)
                setPadding(20, 16, 20, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { bottomMargin = 12 }
            }

            val title = TextView(this).apply {
                text = "#${entry.rank}  ${entry.userName}"
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@Phase4LeaderboardActivity, R.color.dashboard_name))
            }

            val body = TextView(this).apply {
                val regionLabel = entry.region ?: "Unknown region"
                text = "Points: ${entry.totalPoints} | Level: ${entry.level} | Lessons: ${entry.lessonsCompleted} | $regionLabel"
                textSize = 13f
                setTextColor(ContextCompat.getColor(this@Phase4LeaderboardActivity, R.color.dashboard_greeting))
                setPadding(0, 6, 0, 0)
            }

            row.addView(title)
            row.addView(body)
            container.addView(row)
        }
    }
}

