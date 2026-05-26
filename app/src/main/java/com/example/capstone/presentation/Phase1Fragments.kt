package com.example.capstone.presentation

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.example.capstone.DisasterDetailActivity
import com.example.capstone.MainActivity
import com.example.capstone.R
import com.google.android.material.button.MaterialButton
import com.example.capstone.presentation.viewmodel.MeshViewModel
import com.example.capstone.data.MeshMessage
import com.example.capstone.data.MeshMessageType

class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var meshViewModel: MeshViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_home_modern, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, defaultViewModelProviderFactory)[HomeViewModel::class.java]
        meshViewModel = ViewModelProvider(this, defaultViewModelProviderFactory)[MeshViewModel::class.java]

        val greeting = view.findViewById<TextView>(R.id.homeGreeting)
        val name = view.findViewById<TextView>(R.id.homeUserName)
        val region = view.findViewById<TextView>(R.id.homeLocation)
        val progressPercent = view.findViewById<TextView>(R.id.homeProgressPercent)
        val statusBadge = view.findViewById<TextView>(R.id.homeStatusBadge)
        val progressBar = view.findViewById<ProgressBar>(R.id.homeProgressBar)
        val emergencyBadge = view.findViewById<TextView>(R.id.homeEmergencyBadge)
        val recommendationBody = view.findViewById<TextView>(R.id.homeRecommendationBody)
        val recommendationCard = view.findViewById<CardView>(R.id.preparednessCard)
        val gamificationText = view.findViewById<TextView>(R.id.homeGamificationText)

        view.findViewById<CardView>(R.id.homeStartTrainingButton)?.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_lab)
        }
        view.findViewById<CardView>(R.id.homeViewProgressButton)?.setOnClickListener {
            (activity as? MainActivity)?.selectTab(R.id.nav_profile)
        }
        view.findViewById<CardView>(R.id.homeSendSosButton)?.setOnClickListener {
            // reuse the same SOS confirmation flow as older UI
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Send SOS")
                .setMessage("Are you sure you want to broadcast an SOS to nearby devices?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Send") { _, _ ->
                    val pkg = requireContext().packageName
                    val msg = MeshMessage(
                        senderId = pkg,
                        type = MeshMessageType.SOS,
                        content = "SOS: I need help. Sent from ${pkg}",
                    )
                    meshViewModel.broadcast(msg)
                    Toast.makeText(requireContext(), "SOS queued", Toast.LENGTH_SHORT).show()
                }
                .show()
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            greeting.text = greetingText()
            name.text = state.user.name.ifBlank { "User" }
            region.text = state.regionLabel
            progressPercent.text = getString(R.string.percentage_format, state.overallProgress)
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
                getString(R.string.home_recommendation_default)
            } else {
                "${recommendation.title}: ${recommendation.reason}\n${recommendation.ctaLabel}"
            }
            gamificationText.text = state.gamification.progressText
            statusBadge.text = when {
                state.overallProgress >= 80 -> getString(R.string.home_status_ready_to_review)
                state.overallProgress >= 1 -> getString(R.string.home_status_keep_going)
                else -> getString(R.string.home_status_start_here)
            }
            emergencyBadge.text = if (state.isEmergencyModeEnabled) {
                getString(R.string.home_emergency_mode_on)
            } else {
                getString(R.string.home_emergency_mode_off)
            }

            recommendationCard.setOnClickListener {
                val disasterKey = recommendation?.disasterKey ?: state.recommendedModule?.key
                if (!disasterKey.isNullOrBlank()) {
                    startActivity(DisasterDetailActivity.newIntent(requireActivity() as AppCompatActivity, disasterKey))
                } else {
                    (activity as? MainActivity)?.selectTab(R.id.nav_lab)
                }
            }

            recommendationBody.setOnClickListener { recommendationCard.performClick() }
        }
    }

    private fun greetingText(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> getString(R.string.home_greeting_morning)
            in 12..16 -> getString(R.string.home_greeting_afternoon)
            in 17..20 -> getString(R.string.home_greeting_evening)
            else -> getString(R.string.home_greeting_night)
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
        title.text = getString(R.string.training_title)

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
            overallPercent.text = getString(R.string.progress_overall_percent_format, snapshot.overallPercent)
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
            text = getString(R.string.progress_chapters_completed_format, progress.completedChapters, progress.totalChapters)
            textSize = 13f
            setTextColor(ContextCompat.getColor(requireContext(), R.color.dashboard_greeting))
            setPadding(0, 6, 0, 8)
        }

        val percent = TextView(requireContext()).apply {
            text = getString(R.string.progress_single_percent_format, progress.percent)
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

        val scroll = view.findViewById<ScrollView>(R.id.assistantScroll)
        val chatContainer = view.findViewById<LinearLayout>(R.id.assistantChatContainer)
        val promptsContainer = view.findViewById<LinearLayout>(R.id.assistantPromptsContainer)
        val input = view.findViewById<EditText>(R.id.assistantInput)
        val send = view.findViewById<MaterialButton>(R.id.assistantSendButton)
        val suggestion = view.findViewById<TextView>(R.id.assistantSuggestion)

        fun submitMessage() {
            val text = input.text?.toString().orEmpty().trim()
            if (text.isBlank()) {
                Toast.makeText(requireContext(), "Type a question first", Toast.LENGTH_SHORT).show()
                return
            }
            viewModel.sendMessage(text)
            input.text?.clear()
        }

        send.setOnClickListener { submitMessage() }

        input.setOnEditorActionListener { _, actionId, event ->
            val enterPressed = event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (actionId == EditorInfo.IME_ACTION_SEND || actionId == EditorInfo.IME_ACTION_DONE || enterPressed) {
                submitMessage()
                true
            } else {
                false
            }
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

class MedReadyFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_medready, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = view.findViewById<TextView>(R.id.medreadyTitle)
        title?.text = getString(R.string.medready_title)
    }
}
