package com.example.poseexercise.views.fragment

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.example.poseexercise.R
import com.example.poseexercise.data.plan.Plan
import com.example.poseexercise.util.MemoryManagement
import com.example.poseexercise.viewmodels.AddPlanViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * PlanStepTwoFragment: Fragment for capturing additional details of an exercise plan.
 *
 * This fragment allows the user to input details such as the exercise name, repetition count,
 * and select the days of the week for the exercise plan.
 */
class PlanStepTwoFragment : Fragment(), MemoryManagement {
    private lateinit var addPlanViewModel: AddPlanViewModel
    private var mExerciseName: String = ""
    private var mKcal: Double = 0.0
    private val selectedDays = mutableListOf<String>()
    private val days = arrayOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan_step_two, container, false)
        // Set the values
        mExerciseName = arguments?.getString("exerciseName") ?: ""
        mKcal = arguments?.getDouble("caloriesPerRep") ?: 0.0
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get all the layout info
        val exerciseEditText = view.findViewById<EditText>(R.id.exercise_name)
        val repeatEditText = view.findViewById<EditText>(R.id.repeat_count)
        val addPlanButton = view.findViewById<Button>(R.id.button)
        val listOfDays = view.findViewById<ListView>(R.id.days_list)

        // Set the default value for the exercise
        exerciseEditText.setText(mExerciseName)
        // Set the min and max value for Repeat count
        setEditTextLimit(repeatEditText, 1, 100)
        // Option list for days in week
        val listAdapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_list_item_multiple_choice,
            days
        )
        listOfDays.adapter = listAdapter
        // Get the selected days
        listOfDays.setOnItemClickListener { _, _, position, _ ->
            val element = listAdapter.getItem(position)// The item that was clicked
            if (element != null) {
                if (element in selectedDays) {
                    selectedDays.remove(element)
                } else {
                    selectedDays.add(element)
                }
            }
        }
        // Saving plan
        addPlanButton.setOnClickListener {
            if (repeatEditText.text.isEmpty() || selectedDays.isEmpty()) {
                showErrorMessage()
            } else {
                addPlanViewModel = ViewModelProvider(this)[AddPlanViewModel::class.java]
                val days = selectedDays.joinToString(" ") { it.uppercase() }
                val repeatCount = repeatEditText.text.toString()
                
                Log.d("PlanStepTwo", "Creating plan with days: $days")
                
                val newPlan = Plan(
                    id = 0,
                    exercise = mExerciseName,
                    kcal = mKcal,
                    repeatCount = repeatCount.toInt(),
                    selectedDays = days,
                    completed = false
                )
                
                lifecycleScope.launch {
                    try {
                        addPlanViewModel.insert(newPlan)
                        Log.d("PlanStepTwo", "Plan saved successfully: $newPlan")
                        withContext(Dispatchers.Main) {
                            view.findNavController()
                                .navigate(R.id.action_planStepTwoFragment_to_homeFragment)
                        }
                    } catch (e: Exception) {
                        Log.e("PlanStepTwo", "Error saving plan", e)
                    }
                }
            }
        }
    }

    private fun showErrorMessage() {
        Toast.makeText(
            activity,
            "Please fill the form",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setEditTextLimit(editText: EditText, minValue: Int, maxValue: Int) {
        val inputFilter = InputFilter { source, _, _, dest, _, _ ->
            try {
                val input = (dest.toString() + source.toString()).toIntOrNull()
                if (input != null && input in minValue..maxValue) {
                    null // Input is within the range, allow the input
                } else {
                    "" // Input is outside the range, disallow the input by returning an empty string
                }
            } catch (nfe: NumberFormatException) {
                nfe.printStackTrace()
                "" // Return empty string for non-integer input
            }
        }
        val filters = arrayOf(inputFilter)
        editText.filters = filters
    }

    override fun clearMemory() {
        mExerciseName = ""
        mKcal = 0.0
        selectedDays.clear()
    }

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }
}