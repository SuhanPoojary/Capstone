package com.example.capstone.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.capstone.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class QuizBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private lateinit var viewModel: QuizViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, defaultViewModelProviderFactory)[QuizViewModel::class.java]
        val disasterKey = requireArguments().getString(ARG_DISASTER_KEY).orEmpty()
        val chapterIndex = requireArguments().getInt(ARG_CHAPTER_INDEX)
        viewModel.loadQuestion(disasterKey, chapterIndex)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.dialog_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val questionText = view.findViewById<TextView>(R.id.quizQuestion)
        val optionsContainer = view.findViewById<LinearLayout>(R.id.quizOptionsContainer)
        val resultText = view.findViewById<TextView>(R.id.quizResultText)
        val nextButton = view.findViewById<MaterialButton>(R.id.quizCloseButton)

        nextButton.setOnClickListener {
            val state = viewModel.state.value
            if (state?.isFinished == true || state?.question == null) {
                dismissAllowingStateLoss()
            } else if (state.result != null) {
                viewModel.nextQuestion()
            } else {
                dismissAllowingStateLoss()
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.isLoading) {
                questionText.text = "Loading quiz..."
                optionsContainer.removeAllViews()
                nextButton.visibility = View.GONE
                return@observe
            }

            if (state.isFinished) {
                val passed = (state.score.toFloat() / state.totalQuestions) > 0.5f
                questionText.text = if (passed) "Congratulations!" else "Quiz Finished"
                optionsContainer.removeAllViews()
                val scoreText = TextView(requireContext()).apply {
                    text = "Your score: ${state.score} / ${state.totalQuestions}\n\n" +
                            if (passed) "You passed this chapter! You can now proceed to the next one."
                            else "You didn't pass this time. A score of more than 50% is required to unlock the next chapter. Please review the lesson and try again."
                    textSize = 16f
                    setPadding(0, 20, 0, 20)
                }
                optionsContainer.addView(scoreText)
                nextButton.text = "Finish"
                nextButton.visibility = View.VISIBLE
                resultText.visibility = View.GONE
                return@observe
            }

            val question = state.question ?: return@observe
            questionText.text = "Question ${state.currentQuestionIndex + 1}/${state.totalQuestions}\n\n${question.question}"
            optionsContainer.removeAllViews()

            question.options.forEachIndexed { index, option ->
                val button = MaterialButton(requireContext(), null, com.google.android.material.R.attr.materialButtonStyle).apply {
                    text = option
                    isAllCaps = false
                    setOnClickListener {
                        if (state.result == null) {
                            viewModel.submitAnswer(index)
                        }
                    }
                    // Color coding for results
                    if (state.result != null) {
                        if (index == question.correctIndex) {
                            setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")) // Green
                            setTextColor(android.graphics.Color.WHITE)
                        } else if (index == state.selectedIndex) {
                            setBackgroundColor(android.graphics.Color.parseColor("#F44336")) // Red
                            setTextColor(android.graphics.Color.WHITE)
                        }
                    }
                }
                optionsContainer.addView(button)
            }

            if (state.result != null) {
                resultText.visibility = View.VISIBLE
                resultText.text = state.result.message
                nextButton.text = if (state.currentQuestionIndex + 1 < state.totalQuestions) "Next Question" else "See Results"
                nextButton.visibility = View.VISIBLE
            } else {
                resultText.visibility = View.GONE
                nextButton.visibility = View.GONE
            }
        }
    }

    private inline fun LinearLayout.forEachChild(block: (View) -> Unit) {
        for (i in 0 until childCount) block(getChildAt(i))
    }

    companion object {
        private const val ARG_DISASTER_KEY = "arg_disaster_key"
        private const val ARG_CHAPTER_INDEX = "arg_chapter_index"

        fun newInstance(disasterKey: String, chapterIndex: Int): QuizBottomSheetDialogFragment {
            return QuizBottomSheetDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DISASTER_KEY, disasterKey)
                    putInt(ARG_CHAPTER_INDEX, chapterIndex)
                }
            }
        }
    }
}


