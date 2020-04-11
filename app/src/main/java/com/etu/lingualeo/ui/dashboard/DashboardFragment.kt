package com.etu.lingualeo.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.etu.lingualeo.R
import com.etu.lingualeo.wordSelector.WordSelectorActivity
import kotlinx.android.synthetic.main.fragment_dashboard.*

class DashboardFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_dashboard, container, false)

        root.findViewById<Button>(R.id.button).setOnClickListener {
            startActivity(Intent(activity, WordSelectorActivity::class.java))
        }

        return root
    }
}
