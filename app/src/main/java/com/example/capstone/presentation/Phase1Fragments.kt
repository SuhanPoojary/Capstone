package com.example.capstone.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.capstone.CrashViewerActivity
import com.example.capstone.DisasterDetailActivity
import com.example.capstone.MainActivity
import com.example.capstone.R
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, defaultViewModelProviderFactory)[HomeViewModel::class.java]

        val greeting = view.findViewById<TextView>(R.id.homeGreeting)
        val name = view.findViewById<TextView>(R.id.homeUserName)
        val region = view.findViewById<TextView>(R.id.homeRegion)
        val progressPercent = view.findViewById<TextView>(R.id.homeProgressPercent)
        val progressBar = view.findViewById<ProgressBar>(R.id.homeProgressBar)
        val recommendationBody = view.findViewById<TextView>(R.id.homeRecommendationBody)
        val gamificationText = view.findViewById<TextView>(R.id.homeGamificationText)

        view.findViewById<MaterialButton>(R.id.homeStartTrainingButton).setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_training)
        }
        view.findViewById<MaterialButton>(R.id.homeViewProgressButton).setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_progress)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            greeting.text = greetingText()
            name.text = state.user.name.ifBlank { "User" }
            region.text = state.regionLabel
            progressPercent.text = "${state.overallProgress}%"
            progressBar.progress = state.overallProgress
            val recommendation = state.recommendation ?: state.recommendedModule?.let {
                com.example.capstone.data.RecommendationCard(
                    title = it.title,
                    reason = it.summary,
                    ctaLabel = "Continue",
                    disasterKey = it.key,
                    chapterIndex = 0,
                )
            }
            recommendationBody.text = if (recommendation == null) {
                "Start a lesson to see a recommendation."
            } else {
                "${recommendation.title}: ${recommendation.reason}\n${recommendation.ctaLabel}"
            }
            gamificationText.text = state.gamification.progressText
        }
    }

    private fun greetingText(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning 👋"
            in 12..16 -> "Good afternoon 👋"
            in 17..20 -> "Good evening 👋"
            else -> "Good night 👋"
        }
    }
}

class TrainingFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.activity_start_learning, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = view.findViewById<TextView>(R.id.title)
        title.text = "Training"

        view.findViewById<View>(R.id.backButton)?.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_home)
        }

        view.findViewById<View>(R.id.cardEarthquake)?.setOnClickListener {
            startDisaster("earthquake")
        }
        view.findViewById<View>(R.id.cardFloods)?.setOnClickListener {
            startDisaster("floods")
        }
        view.findViewById<View>(R.id.cardCyclone)?.setOnClickListener {
            startDisaster("cyclone")
        }
        view.findViewById<View>(R.id.cardLandslides)?.setOnClickListener {
            startDisaster("landslides")
        }
    }

    private fun startDisaster(disasterKey: String) {
        val activity = requireActivity() as AppCompatActivity
        startActivity(DisasterDetailActivity.newIntent(activity, disasterKey))
    }
}

class ProgressFragment : Fragment() {
    private lateinit var viewModel: ProgressViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, defaultViewModelProviderFactory)[ProgressViewModel::class.java]

        val overallPercent = view.findViewById<TextView>(R.id.overallProgressPercent)
        val overallBar = view.findViewById<ProgressBar>(R.id.overallProgressBar)
        val summary = view.findViewById<TextView>(R.id.quizSummaryText)
        val container = view.findViewById<LinearLayout>(R.id.progressListContainer)

        view.findViewById<MaterialButton>(R.id.clearProgressButton).setOnClickListener {
            viewModel.clearProgress()
            Toast.makeText(requireContext(), "Local progress cleared", Toast.LENGTH_SHORT).show()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val snapshot = state.snapshot
            overallPercent.text = "${snapshot.overallPercent}%"
            overallBar.progress = snapshot.overallPercent
            summary.text = state.gamification.progressText
            container.removeAllViews()
            snapshot.disasterProgress.forEach { progress ->
                container.addView(progressRow(progress))
            }
        }
    }

    private fun progressRow(progress: com.example.capstone.data.DisasterProgress): View {
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_white_card)
            setPadding(16, 16, 16, 16)
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 16
            }
        }

        val title = TextView(requireContext()).apply {
            text = progress.title
            textSize = 16f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.dashboard_name))
        }

        val body = TextView(requireContext()).apply {
            text = "${progress.completedChapters}/${progress.totalChapters} chapters completed"
            textSize = 13f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.dashboard_greeting))
            setPadding(0, 6, 0, 8)
        }

        val percent = TextView(requireContext()).apply {
            text = "${progress.percent}%"
            textSize = 18f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.dashboard_name))
        }

        val bar = ProgressBar(requireContext(), null, android.R.attr.progressBarStyleHorizontal).apply {
            max = 100
            this.progress = progress.percent
        }

        row.addView(title)
        row.addView(body)
        row.addView(percent)
        row.addView(bar)
        return row
    }
}

class AssistantFragment : Fragment() {
    private lateinit var viewModel: AssistantViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_assistant, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, defaultViewModelProviderFactory)[AssistantViewModel::class.java]

        val scroll = view.findViewById<NestedScrollView>(R.id.assistantScroll)
        val chatContainer = view.findViewById<LinearLayout>(R.id.assistantChatContainer)
        val promptsContainer = view.findViewById<LinearLayout>(R.id.assistantPromptsContainer)
        val input = view.findViewById<EditText>(R.id.assistantInput)
        val send = view.findViewById<MaterialButton>(R.id.assistantSendButton)
        val suggestion = view.findViewById<TextView>(R.id.assistantSuggestion)

        send.setOnClickListener {
            viewModel.sendMessage(input.text?.toString().orEmpty())
            input.text?.clear()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            chatContainer.removeAllViews()
            state.messages.forEach { message ->
                chatContainer.addView(chatBubble(message.text, message.isUser))
            }
            suggestion.text = state.suggestedTopic ?: "Ask about a disaster or emergency kit to get started."

            promptsContainer.removeAllViews()
            state.prompts.forEach { prompt ->
                val chip = MaterialButton(requireContext()).apply {
                    text = prompt
                    isAllCaps = false
                    setOnClickListener {
                        input.setText(prompt)
                        viewModel.sendMessage(prompt)
                        input.text?.clear()
                    }
                }
                promptsContainer.addView(chip)
            }

            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun chatBubble(text: String, isUser: Boolean): View {
        val bubble = TextView(requireContext()).apply {
            this.text = text
            setPadding(20, 16, 20, 16)
            textSize = 14f
            setTextColor(resources.getColor(R.color.dashboard_name, null))
            setBackgroundResource(if (isUser) R.drawable.bg_chip_active else R.drawable.bg_white_card)
        }
        val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = 12
            marginStart = if (isUser) 64 else 0
            marginEnd = if (isUser) 0 else 64
        }
        bubble.layoutParams = params
        return bubble
    }
}

class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, defaultViewModelProviderFactory)[ProfileViewModel::class.java]

        val name = view.findViewById<TextView>(R.id.profileName)
        val email = view.findViewById<TextView>(R.id.profileEmail)
        val institution = view.findViewById<TextView>(R.id.profileInstitution)
        val region = view.findViewById<TextView>(R.id.profileRegion)
        val summary = view.findViewById<TextView>(R.id.profileSummaryBody)
        val gamificationText = view.findViewById<TextView>(R.id.profileGamificationText)

        view.findViewById<MaterialButton>(R.id.resetDataButton).setOnClickListener {
            viewModel.resetAllData()
            Toast.makeText(requireContext(), "Local data reset", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<MaterialButton>(R.id.openCrashViewerButton).setOnClickListener {
            startActivity(Intent(requireContext(), CrashViewerActivity::class.java))
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val profile = state.profile
            name.text = profile.name.ifBlank { "User" }
            email.text = if (profile.email.isBlank()) "Email not saved yet" else profile.email
            institution.text = if (profile.institution.isBlank()) "Institution not saved yet" else profile.institution
            region.text = when {
                !profile.city.isNullOrBlank() && !profile.state.isNullOrBlank() -> "Region: ${profile.city}, ${profile.state}"
                !profile.state.isNullOrBlank() -> "Region: ${profile.state}"
                !profile.city.isNullOrBlank() -> "Region: ${profile.city}"
                else -> "Region: not available"
            }
            summary.text = buildString {
                append("Completed lessons are tracked locally.\n")
                append("Total tracked disasters: ${state.completedDisasters.size}")
            }
            gamificationText.text = state.gamification.progressText
        }
    }
}



