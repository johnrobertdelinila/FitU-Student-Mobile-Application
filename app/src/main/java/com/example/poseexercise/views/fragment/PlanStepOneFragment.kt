package com.example.poseexercise.views.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.R
import com.example.poseexercise.adapters.ExerciseAdapter
import com.example.poseexercise.data.plan.Constants
import com.example.poseexercise.util.MemoryManagement
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * Displays a [RecyclerView] of exercise types.
 */
class PlanStepOneFragment : Fragment(), MemoryManagement {
    private val exerciseList = Constants.getExerciseList()
    private var searchQuery: CharSequence? = null
    private lateinit var adapter: ExerciseAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var chipGroup: ChipGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan_step_one, container, false)
        val activity = activity as Context

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view)
        chipGroup = view.findViewById(R.id.chip_group)

        // Setup adapter
        adapter = ExerciseAdapter(activity, findNavController(this))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.setExercises(exerciseList)

        // Setup chip group
        setupChipGroup()

        return view
    }

    private fun setupChipGroup() {
        // Set initial state - "All" selected
        chipGroup.check(R.id.chip_all)

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                // If nothing is selected, select "All"
                group.check(R.id.chip_all)
                return@setOnCheckedStateChangeListener
            }

            val checkedChipId = checkedIds.first()
            val query = when (checkedChipId) {
                R.id.chip_all -> "Intermediate"
                R.id.chip_strength -> "Strength"
                R.id.chip_yoga -> "Yoga"
                else -> ""
            }

            filterExercises(query)
        }
    }

    private fun filterExercises(query: String) {
        if (query.isEmpty()) {
            adapter.setExercises(exerciseList)
        } else {
            val filteredList = exerciseList.filter { exercise ->
                exercise.level.equals(query, ignoreCase = true)
            }
            adapter.setExercises(filteredList)
        }
    }

    override fun clearMemory() {
        searchQuery = null
    }

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }
}