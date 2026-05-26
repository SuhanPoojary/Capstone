package com.example.capstone.presentation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.capstone.R
import com.example.capstone.StartLearningActivity

class LabFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lab_modern, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Interactive Learning cards open StartLearningActivity
        view.findViewById<View>(R.id.interactive_card_earthquake)?.setOnClickListener {
            startActivity(Intent(requireContext(), StartLearningActivity::class.java))
        }
        view.findViewById<View>(R.id.interactive_card_floods)?.setOnClickListener {
            startActivity(Intent(requireContext(), StartLearningActivity::class.java))
        }

        // Quick drill sample click handlers (placeholder)
        view.findViewById<View>(R.id.drill_cpr)?.setOnClickListener {
            Toast.makeText(requireContext(), "Start CPR Practice (not implemented)", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.drill_first_aid)?.setOnClickListener {
            Toast.makeText(requireContext(), "Start First Aid Quiz (not implemented)", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.drill_supply_check)?.setOnClickListener {
            Toast.makeText(requireContext(), "Start Supply Check (not implemented)", Toast.LENGTH_SHORT).show()
        }
        view.findViewById<View>(R.id.drill_route_planning)?.setOnClickListener {
            Toast.makeText(requireContext(), "Start Route Planning (not implemented)", Toast.LENGTH_SHORT).show()
        }
    }
}

