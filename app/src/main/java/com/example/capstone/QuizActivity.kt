package com.example.capstone

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.capstone.data.QuizQuestion
import com.example.capstone.data.QuizResult
import com.example.capstone.presentation.QuizState
import com.example.capstone.presentation.QuizViewModel
import com.google.android.material.button.MaterialButton

class QuizActivity : AppCompatActivity() {

    private lateinit var viewModel: QuizViewModel
    private var topic: String = "disaster preparedness"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        topic = intent.getStringExtra(EXTRA_TOPIC) ?: "disaster preparedness"

        viewModel = ViewModelProvider(this)[QuizViewModel::class.java]

        setupObservers()
        setupListeners()

        if (savedInstanceState == null) {
            viewModel.generateDynamicQuiz(topic)
        }
    }

    private fun setupObservers() {
        viewModel.state.observe(this) { state ->
            updateUI(state)
        }
    }

    private fun setupListeners() {
        findViewById<ImageButton>(R.id.quizBackButton).setOnClickListener {
            onBackPressed()
        }

        findViewById<MaterialButton>(R.id.quizNextButton).setOnClickListener {
            viewModel.nextQuestion()
        }

        findViewById<MaterialButton>(R.id.quizContinueButton).setOnClickListener {
            viewModel.nextQuestion()
        }

        findViewById<MaterialButton>(R.id.quizRetryButton).setOnClickListener {
            if (topic.isNotBlank()) {
                viewModel.generateDynamicQuiz(topic, viewModel.state.value?.currentLevel ?: 1)
            } else {
                val disasterKey = intent.getStringExtra(EXTRA_DISASTER_KEY) ?: "general"
                val chapterIndex = intent.getIntExtra(EXTRA_CHAPTER_INDEX, 0)
                viewModel.loadQuestion(disasterKey, chapterIndex)
            }
        }

        findViewById<MaterialButton>(R.id.quizFallbackRetryButton).setOnClickListener {
            val level = viewModel.state.value?.currentLevel ?: 1
            viewModel.generateDynamicQuiz(topic, level)
        }

        findViewById<MaterialButton>(R.id.quizNextLevelButton).setOnClickListener {
            val state = viewModel.state.value ?: return@setOnClickListener
            if (state.currentLevel < 3) {
                viewModel.startNextLevel()
            } else {
                Toast.makeText(this, "All levels complete!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        findViewById<MaterialButton>(R.id.quizExitButton).setOnClickListener {
            finish()
        }
    }

    private fun updateUI(state: QuizState) {
        val loadingContainer = findViewById<LinearLayout>(R.id.quizLoadingContainer)
        val emptyContainer = findViewById<LinearLayout>(R.id.quizEmptyContainer)
        val quizContainer = findViewById<androidx.core.widget.NestedScrollView>(R.id.quizContainer)
        val explanationContainer = findViewById<androidx.core.widget.NestedScrollView>(R.id.quizExplanationContainer)
        val resultContainer = findViewById<androidx.core.widget.NestedScrollView>(R.id.quizResultContainer)

        loadingContainer.visibility = if (state.isLoading) LinearLayout.VISIBLE else LinearLayout.GONE
        emptyContainer.visibility = LinearLayout.GONE

        when {
            state.isLoading -> {
                quizContainer.visibility = LinearLayout.GONE
                explanationContainer.visibility = LinearLayout.GONE
                resultContainer.visibility = LinearLayout.GONE
            }
            state.isFinished -> {
                quizContainer.visibility = LinearLayout.GONE
                explanationContainer.visibility = LinearLayout.GONE
                resultContainer.visibility = LinearLayout.VISIBLE
                showResults(state)
            }
            state.result != null -> {
                quizContainer.visibility = LinearLayout.GONE
                explanationContainer.visibility = LinearLayout.VISIBLE
                resultContainer.visibility = LinearLayout.GONE
                showExplanation(state)
            }
            state.question != null -> {
                quizContainer.visibility = LinearLayout.VISIBLE
                explanationContainer.visibility = LinearLayout.GONE
                resultContainer.visibility = LinearLayout.GONE
                displayQuestion(state)
            }
            else -> {
                quizContainer.visibility = LinearLayout.GONE
                explanationContainer.visibility = LinearLayout.GONE
                resultContainer.visibility = LinearLayout.GONE
                emptyContainer.visibility = LinearLayout.VISIBLE
                findViewById<TextView>(R.id.quizEmptyMessage).text =
                    "Quiz could not be loaded. Please try again."
            }
        }
    }

    private fun displayQuestion(state: QuizState) {
        val question = state.question ?: return
        val progressBar = findViewById<ProgressBar>(R.id.quizProgressBar)
        val progressText = findViewById<TextView>(R.id.quizProgressText)
        val questionText = findViewById<TextView>(R.id.quizQuestionText)
        val nextButton = findViewById<MaterialButton>(R.id.quizNextButton)
        val optionsContainer = findViewById<LinearLayout>(R.id.quizOptionsContainer)

        progressBar.max = state.totalQuestions
        progressBar.progress = state.currentQuestionIndex + 1
        progressText.text = "${state.currentQuestionIndex + 1}/${state.totalQuestions}"
        questionText.text = question.question

        optionsContainer.removeAllViews()
        question.options.forEachIndexed { index, option ->
            val cardView = com.google.android.material.card.MaterialCardView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.spacing_md))
                }
                radius = resources.getDimension(R.dimen.corner_lg)
                cardElevation = resources.getDimension(R.dimen.elevation_light)
                strokeWidth = if (state.selectedIndex == index) 4 else 0
                strokeColor = getColor(R.color.color_navy_700)
                setCardBackgroundColor(if (state.selectedIndex == index) getColor(R.color.color_navy_50) else getColor(R.color.bg_card))
                isClickable = true
                isFocusable = true
                
                val textView = TextView(context).apply {
                    text = option
                    setTextColor(getColor(R.color.color_navy_900))
                    setPadding(
                        resources.getDimensionPixelSize(R.dimen.spacing_lg),
                        resources.getDimensionPixelSize(R.dimen.spacing_lg),
                        resources.getDimensionPixelSize(R.dimen.spacing_lg),
                        resources.getDimensionPixelSize(R.dimen.spacing_lg)
                    )
                    setTextAppearance(R.style.TextStyle_Body_Medium)
                }
                addView(textView)
                
                setOnClickListener {
                    viewModel.submitAnswer(index)
                }
            }
            optionsContainer.addView(cardView)
        }

        nextButton.isEnabled = state.selectedIndex != null
        nextButton.text = if (state.currentQuestionIndex == state.totalQuestions - 1) "Finish" else "Next"
    }

    private fun showExplanation(state: QuizState) {
        val result = state.result ?: return
        val explanationText = findViewById<TextView>(R.id.quizExplanationText)
        val continueButton = findViewById<MaterialButton>(R.id.quizContinueButton)

        explanationText.text = result.message
        explanationText.setTextColor(if (result.passed) getColor(android.R.color.holo_green_light) else getColor(android.R.color.holo_red_light))

        continueButton.text = if (state.currentQuestionIndex == state.totalQuestions - 1) "View Results" else "Next Question"
    }

    private fun showResults(state: QuizState) {
        val scoreText = findViewById<TextView>(R.id.quizFinalScore)
        val summaryText = findViewById<TextView>(R.id.quizSummaryText)
        val nextLevelButton = findViewById<MaterialButton>(R.id.quizNextLevelButton)
        val retryButton = findViewById<MaterialButton>(R.id.quizRetryButton)

        val percentage = if (state.totalQuestions > 0) (state.score * 100) / state.totalQuestions else 0
        val passed = percentage >= 60

        scoreText.text = "$percentage%"
        
        val summary = StringBuilder()
        summary.append("Level ${state.currentLevel} Complete!\n\n")
        summary.append("You scored: ${state.score} / ${state.totalQuestions}\n\n")
        
        if (passed) {
            summary.append("Great job! You've mastered this level.")
            if (state.currentLevel < 3) {
                nextLevelButton.visibility = MaterialButton.VISIBLE
                nextLevelButton.text = "Start Level ${state.currentLevel + 1}"
            } else {
                nextLevelButton.visibility = MaterialButton.VISIBLE
                nextLevelButton.text = "Finish Quiz"
            }
            retryButton.visibility = MaterialButton.GONE
        } else {
            summary.append("You need 60% to reach the next level. Try again!")
            nextLevelButton.visibility = MaterialButton.GONE
            retryButton.visibility = MaterialButton.VISIBLE
        }
        
        summaryText.text = summary.toString()
    }

    override fun onBackPressed() {
        if (viewModel.state.value?.isFinished == true) {
            super.onBackPressed()
            return
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Exit Quiz?")
            .setMessage("Your progress will be lost. Are you sure?")
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No", null)
            .show()
    }

    companion object {
        const val EXTRA_TOPIC = "topic"
        const val EXTRA_DISASTER_KEY = "extra_disaster_key"
        const val EXTRA_CHAPTER_INDEX = "extra_chapter_index"
    }
}
