package com.example.capstone.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.capstone.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class QuizFragment : Fragment() {

    private lateinit var viewModel: QuizViewModel
    private var topic: String = "disaster preparedness"
    private var disasterKey: String? = null
    private var chapterIndex: Int = 0

    companion object {
        const val ARG_TOPIC = "topic"
        const val ARG_DISASTER_KEY = "extra_disaster_key"
        const val ARG_CHAPTER_INDEX = "extra_chapter_index"

        fun newInstance(topic: String?, disasterKey: String? = null, chapterIndex: Int = 0): QuizFragment {
            val fragment = QuizFragment()
            val args = Bundle()
            args.putString(ARG_TOPIC, topic)
            args.putString(ARG_DISASTER_KEY, disasterKey)
            args.putInt(ARG_CHAPTER_INDEX, chapterIndex)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        topic = arguments?.getString(ARG_TOPIC) ?: getString(R.string.quiz_default_topic)
        disasterKey = arguments?.getString(ARG_DISASTER_KEY)
        chapterIndex = arguments?.getInt(ARG_CHAPTER_INDEX) ?: 0

        viewModel = ViewModelProvider(this)[QuizViewModel::class.java]

        setupObservers(view)
        setupListeners(view)

        if (savedInstanceState == null) {
            if (disasterKey != null) {
                viewModel.loadQuestion(disasterKey!!, chapterIndex)
            } else {
                viewModel.generateDynamicQuiz(topic)
            }
        }
    }

    private fun setupObservers(view: View) {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            updateUI(view, state)
        }
    }

    private fun setupListeners(view: View) {
        view.findViewById<ImageButton>(R.id.quizBackButton).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<MaterialButton>(R.id.quizNextButton).setOnClickListener {
            viewModel.nextQuestion()
        }

        view.findViewById<MaterialButton>(R.id.quizContinueButton).setOnClickListener {
            viewModel.nextQuestion()
        }

        view.findViewById<MaterialButton>(R.id.quizRetryButton).setOnClickListener {
            if (disasterKey != null) {
                viewModel.loadQuestion(disasterKey!!, chapterIndex)
            } else {
                viewModel.generateDynamicQuiz(topic, viewModel.state.value?.currentLevel ?: 1)
            }
        }

        view.findViewById<MaterialButton>(R.id.quizFallbackRetryButton).setOnClickListener {
            val level = viewModel.state.value?.currentLevel ?: 1
            viewModel.generateDynamicQuiz(topic, level)
        }

        view.findViewById<MaterialButton>(R.id.quizNextLevelButton).setOnClickListener {
            val state = viewModel.state.value ?: return@setOnClickListener
            if (state.currentLevel < 3) {
                viewModel.startNextLevel()
            } else {
                Toast.makeText(requireContext(), R.string.quiz_levels_complete, Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
        }

        view.findViewById<MaterialButton>(R.id.quizExitButton).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun updateUI(view: View, state: QuizState) {
        val loadingContainer = view.findViewById<LinearLayout>(R.id.quizLoadingContainer)
        val emptyContainer = view.findViewById<LinearLayout>(R.id.quizEmptyContainer)
        val quizContainer = view.findViewById<androidx.core.widget.NestedScrollView>(R.id.quizContainer)
        val explanationContainer = view.findViewById<androidx.core.widget.NestedScrollView>(R.id.quizExplanationContainer)
        val resultContainer = view.findViewById<androidx.core.widget.NestedScrollView>(R.id.quizResultContainer)

        loadingContainer.visibility = if (state.isLoading) View.VISIBLE else View.GONE
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
                showResults(view, state)
            }
            state.result != null -> {
                quizContainer.visibility = LinearLayout.GONE
                explanationContainer.visibility = LinearLayout.VISIBLE
                resultContainer.visibility = LinearLayout.GONE
                showExplanation(view, state)
            }
            state.question != null -> {
                quizContainer.visibility = LinearLayout.VISIBLE
                explanationContainer.visibility = LinearLayout.GONE
                resultContainer.visibility = LinearLayout.GONE
                displayQuestion(view, state)
            }
            else -> {
                quizContainer.visibility = LinearLayout.GONE
                explanationContainer.visibility = LinearLayout.GONE
                resultContainer.visibility = LinearLayout.GONE
                emptyContainer.visibility = LinearLayout.VISIBLE
                view.findViewById<TextView>(R.id.quizEmptyMessage).text = getString(R.string.quiz_empty_error)
            }
        }
    }

    private fun displayQuestion(view: View, state: QuizState) {
        val question = state.question ?: return
        val progressBar = view.findViewById<ProgressBar>(R.id.quizProgressBar)
        val progressText = view.findViewById<TextView>(R.id.quizProgressText)
        val questionText = view.findViewById<TextView>(R.id.quizQuestionText)
        val nextButton = view.findViewById<MaterialButton>(R.id.quizNextButton)
        val optionsContainer = view.findViewById<LinearLayout>(R.id.quizOptionsContainer)

        progressBar.max = state.totalQuestions
        progressBar.progress = state.currentQuestionIndex + 1
        progressText.text = "${state.currentQuestionIndex + 1}/${state.totalQuestions}"
        questionText.text = question.question

        optionsContainer.removeAllViews()
        question.options.forEachIndexed { index, option ->
            val cardView = MaterialCardView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    val margin = resources.getDimensionPixelSize(R.dimen.spacing_md)
                    setMargins(0, 0, 0, margin)
                }
                radius = resources.getDimension(R.dimen.corner_lg)
                cardElevation = resources.getDimension(R.dimen.elevation_light)
                strokeWidth = if (state.selectedIndex == index) 4 else 0
                strokeColor = requireContext().getColor(R.color.color_navy_700)
                setCardBackgroundColor(if (state.selectedIndex == index) requireContext().getColor(R.color.color_navy_50) else requireContext().getColor(R.color.bg_card))
                isClickable = true
                isFocusable = true
                
                val textView = TextView(context).apply {
                    text = option
                    setTextColor(requireContext().getColor(R.color.color_navy_900))
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
        nextButton.text = if (state.currentQuestionIndex == state.totalQuestions - 1) getString(R.string.quiz_finish) else getString(R.string.quiz_next)
    }

    private fun showExplanation(view: View, state: QuizState) {
        val result = state.result ?: return
        val explanationText = view.findViewById<TextView>(R.id.quizExplanationText)
        val continueButton = view.findViewById<MaterialButton>(R.id.quizContinueButton)

        explanationText.text = result.message
        explanationText.setTextColor(if (result.passed) requireContext().getColor(android.R.color.holo_green_light) else requireContext().getColor(android.R.color.holo_red_light))

        continueButton.text = if (state.currentQuestionIndex == state.totalQuestions - 1) getString(R.string.quiz_view_results) else getString(R.string.quiz_next_question)
    }

    private fun showResults(view: View, state: QuizState) {
        val scoreText = view.findViewById<TextView>(R.id.quizFinalScore)
        val summaryText = view.findViewById<TextView>(R.id.quizSummaryText)
        val nextLevelButton = view.findViewById<MaterialButton>(R.id.quizNextLevelButton)
        val retryButton = view.findViewById<MaterialButton>(R.id.quizRetryButton)

        val percentage = if (state.totalQuestions > 0) (state.score * 100) / state.totalQuestions else 0
        val passed = percentage >= 60

        scoreText.text = getString(R.string.percentage_format, percentage)
        
        val summary = StringBuilder()
        summary.append(getString(R.string.quiz_level_complete_format, state.currentLevel))
        summary.append(getString(R.string.quiz_score_format, state.score, state.totalQuestions))
        
        if (passed) {
            summary.append(getString(R.string.quiz_mastered))
            if (state.currentLevel < 3) {
                nextLevelButton.visibility = MaterialButton.VISIBLE
                nextLevelButton.text = getString(R.string.quiz_start_next_level, state.currentLevel + 1)
            } else {
                nextLevelButton.visibility = MaterialButton.VISIBLE
                nextLevelButton.text = getString(R.string.quiz_finish_action)
            }
            retryButton.visibility = MaterialButton.GONE
        } else {
            summary.append(getString(R.string.quiz_failed))
            nextLevelButton.visibility = MaterialButton.GONE
            retryButton.visibility = MaterialButton.VISIBLE
        }
        
        summaryText.text = summary.toString()
    }
}
