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
        val closeButton = view.findViewById<MaterialButton>(R.id.quizCloseButton)

        closeButton.setOnClickListener { dismissAllowingStateLoss() }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val question = state.question ?: return@observe
            questionText.text = question.question
            resultText.text = state.result?.message.orEmpty()
            optionsContainer.removeAllViews()

            question.options.forEachIndexed { index, option ->
                val button = MaterialButton(requireContext()).apply {
                    text = option
                    isAllCaps = false
                    setOnClickListener {
                        if (state.result == null) {
                            viewModel.submitAnswer(index)
                        }
                    }
                }
                optionsContainer.addView(button)
            }

            if (state.result != null) {
                questionText.text = question.question
                resultText.text = state.result.message
                optionsContainer.forEachChild { child ->
                    (child as? MaterialButton)?.isEnabled = false
                }
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


