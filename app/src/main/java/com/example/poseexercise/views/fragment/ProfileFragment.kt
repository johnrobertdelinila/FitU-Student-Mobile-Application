package com.example.poseexercise.views.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.poseexercise.R
import com.example.poseexercise.data.database.AppRepository
import com.example.poseexercise.data.results.WorkoutResult
import com.example.poseexercise.util.MemoryManagement
import com.example.poseexercise.util.MyApplication
import com.example.poseexercise.viewmodels.ResultViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.example.poseexercise.views.activity.LoginActivity
import android.app.AlertDialog
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.Toast

/**
 * Profile view with the information on workout for a week
 * This fragment is part of the FitU application
 */
class ProfileFragment : Fragment(), MemoryManagement {
    // Declare variables for ViewModel, Chart, UI components, and data
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var chart: BarChart
    private var workoutResults: List<WorkoutResult>? = null
    private lateinit var workOutTime: TextView
    private lateinit var appRepository: AppRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var logoutButton: Button
    private lateinit var userEmailText: TextView
    private lateinit var nameValue: TextView
    private lateinit var ageValue: TextView
    private lateinit var heightValue: TextView
    private lateinit var weightValue: TextView
    private lateinit var courseValue: TextView
    private lateinit var yearLevelValue: TextView
    private lateinit var fitnessLevelValue: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var workoutCountText: TextView
    private lateinit var minutesText: TextView
    private lateinit var caloriesText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModel, Chart, and UI components
        resultViewModel = ResultViewModel(MyApplication.getInstance())
        chart = view.findViewById(R.id.chart)
        workOutTime = view.findViewById(R.id.total_time)
        appRepository = AppRepository(requireActivity().application)
        
        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        // Initialize views
        logoutButton = view.findViewById(R.id.logoutButton)
        userEmailText = view.findViewById(R.id.userEmailText)
        
        // Initialize stats views
        workoutCountText = view.findViewById(R.id.workoutCountText)
        minutesText = view.findViewById(R.id.minutesText)
        caloriesText = view.findViewById(R.id.caloriesText)
        
        // Initialize account detail views
        nameValue = view.findViewById(R.id.nameValue)
        ageValue = view.findViewById(R.id.ageValue)
        heightValue = view.findViewById(R.id.heightValue)
        weightValue = view.findViewById(R.id.weightValue)
        courseValue = view.findViewById(R.id.courseValue)
        yearLevelValue = view.findViewById(R.id.yearLevelValue)
        fitnessLevelValue = view.findViewById(R.id.fitnessLevelValue)
        
        // Set user email
        userEmailText.text = auth.currentUser?.email ?: "Guest User"
        
        // Set up logout button
        logoutButton.setOnClickListener {
            handleLogout()
        }
        
        // Load data
        loadDataAndSetupChart()
        loadUserData()
    }

    private fun updateStats(workoutResults: List<WorkoutResult>?) {
        val totalWorkouts = workoutResults?.size ?: 0
        val totalMinutes = workoutResults?.sumOf { it.workoutTimeInMin.toInt() } ?: 0
        val totalCalories = workoutResults?.sumOf { it.calorie.toInt() } ?: 0

        workoutCountText.text = totalWorkouts.toString()
        minutesText.text = totalMinutes.toString()
        caloriesText.text = totalCalories.toString()
    }

    private fun loadDataAndSetupChart() {
        lifecycleScope.launch(Dispatchers.IO) {
            workoutResults = resultViewModel.getAllResult()
            val currentWeek = getCurrentCalendarWeek()
            workoutResults = workoutResults?.filter {
                getCalendarWeek(it.timestamp) == currentWeek
            }

            withContext(Dispatchers.Main) {
                // Update stats
                updateStats(workoutResults)
                
                // Update chart
                val totalCaloriesPerDay = workoutResults?.let { calculateTotalCaloriesPerDay(it) }
                if (totalCaloriesPerDay != null) {
                    updateChart(totalCaloriesPerDay)
                }
            }
        }
    }

    // Function to update progress views (ProgressBar and TextView)
    private fun updateProgressViews(progressPercentage: Double) {
        // Update progress views (ProgressBar and TextView)
        val cappedProgress = min(progressPercentage, 110.0)

        val progressBar = view?.findViewById<ProgressBar>(R.id.progress_bar)
        val progressTextView = view?.findViewById<TextView>(R.id.percentage)

        progressBar?.progress = cappedProgress.toInt()
        progressTextView?.text = String.format("%.2f%%", cappedProgress)
    }

    // Function to get the current calendar week
    private fun getCurrentCalendarWeek(): Int {
        val calendar = Calendar.getInstance(Locale.getDefault())
        // Set the first day of the week to Sunday
        calendar.firstDayOfWeek = Calendar.SUNDAY
        // Get the week of the year
        return calendar.get(Calendar.WEEK_OF_YEAR)
    }

    // Function to calculate total workout time for a specific week
    private fun calculateTotalWorkoutTimeForWeek(
        workoutResults: List<WorkoutResult>,
        targetWeek: Int
    ): Double {
        return workoutResults
            .filter { getCalendarWeek(it.timestamp) == targetWeek }
            .sumOf { it.workoutTimeInMin }
    }

    // Function to get the week of the year from a timestamp
    private fun getCalendarWeek(timestamp: Long): Int {
        val calendar = Calendar.getInstance(Locale.getDefault())
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.WEEK_OF_YEAR)
    }

    // Function to calculate total calories per day from workout results
    private fun calculateTotalCaloriesPerDay(workoutResults: List<WorkoutResult>): Map<String, Double> {
        val totalCaloriesPerDay = mutableMapOf<String, Double>()

        // Initialize entries for each day of the week
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        for (day in daysOfWeek) {
            totalCaloriesPerDay[day] = 0.0
        }

        for (result in workoutResults) {
            val startDate = getStartOfDay(result.timestamp)
            val key = formatDate(startDate)
            val totalCalories = totalCaloriesPerDay.getOrDefault(key, 0.0) + result.calorie
            totalCaloriesPerDay[key] = totalCalories
        }

        return totalCaloriesPerDay
    }

    // Function to get the start of the day
    private fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp

        // Set the calendar to the start of the day
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    // Function to format a timestamp to a string representing the day of the week
    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    // Function to update the chart with total calories per week
    private fun updateChart(totalCaloriesPerWeek: Map<String, Double>) {
        // Map entries for BarChart
        val entries = totalCaloriesPerWeek.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        val labels = totalCaloriesPerWeek.keys.toList()
        val dataSet = BarDataSet(entries, "Total Calories per Week")
        dataSet.colors = getBarColors(totalCaloriesPerWeek)
        val data = BarData(dataSet)
        chart.data = data

        // Set custom labels for each bar
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // Set X-axis position to the bottom
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM

        chart.legend.isEnabled = false
        chart.description = null
        chart.axisLeft.axisMinimum = 0f

        // Set Y-Axis (Right) values
        chart.axisRight.apply {
            setDrawGridLines(false)
            setDrawLabels(false)
            setDrawAxisLine(false)
        }

        // Set Y-Axis (Left) values
        chart.axisLeft.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
        }

        // Set X-Axis values
        chart.xAxis.apply {
            setDrawGridLines(false)
            setDrawAxisLine(false)
        }

        // Hide values if they're zero
        for (i in entries.indices) {
            val value = totalCaloriesPerWeek[labels[i]]
            if (value != null && value == 0.0) {
                dataSet.valueTextSize = 0f
                break
            }
        }

        // Update chart appearance
        chart.setBackgroundColor(Color.WHITE)
        dataSet.valueTextColor = Color.BLACK
        chart.xAxis.textColor = Color.BLACK
        chart.axisLeft.textColor = Color.BLACK
        
        chart.invalidate()
    }

    // Function to calculate the total exercise count for the week
    private fun calculateTotalExerciseCountForWeek(workoutResults: List<WorkoutResult>?): Int {
        val totalExerciseCount = workoutResults?.count { result ->
            val startDate = getStartOfDay(result.timestamp)
            val dayOfWeek = getDayOfWeek(startDate)
            // returns a value from 1 (Sunday) to 7 (Saturday)
            dayOfWeek in 1..7
        } ?: 0

        return totalExerciseCount
    }

    // Function to get the day of the week from a timestamp
    private fun getDayOfWeek(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    // Function to get bar colors based on total calories per week
    private fun getBarColors(totalCaloriesPerWeek: Map<String, Double>): List<Int> {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primaryColor)

        return totalCaloriesPerWeek
            .map { (_, value) -> if (value > 0) primaryColor else Color.TRANSPARENT }
    }

    override fun clearMemory() {
        // Clear fragment-specific memory
        workoutResults = null
        chart.clear()
        
        try {
            // Clear application-level memory if available
            (requireActivity().application as? MyApplication)?.clearMemory()
        } catch (e: Exception) {
            // Log error or handle gracefully
        }
    }

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }

    private fun handleLogout() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Logout Confirmation")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                // Perform logout actions
                performLogout()
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun performLogout() {
        // Sign out from Firebase
        auth.signOut()
        
        // Clear any saved user data
        PreferenceManager.getDefaultSharedPreferences(requireContext())
            .edit()
            .remove("is_logged_in")
            .remove("user_email")
            .apply()
        
        // Clear memory before logout
        clearMemory()
        
        // Navigate to login screen
        requireActivity().finish()
        startActivity(Intent(requireContext(), LoginActivity::class.java))
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Handle not logged in state
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Update UI with user data
                    nameValue.text = document.getString("name") ?: "Not set"
                    ageValue.text = "${document.getLong("age") ?: 0} years"
                    heightValue.text = "${document.getDouble("height") ?: 0.0} cm"
                    weightValue.text = "${document.getDouble("weight") ?: 0.0} kg"
                    courseValue.text = document.getString("course") ?: "Not set"
                    yearLevelValue.text = "${document.getLong("yearLevel") ?: 0}"
                    fitnessLevelValue.text = document.getString("fitnessLevel") ?: "Not set"

                    // Also update the header name if you want
                    view?.findViewById<TextView>(R.id.userNameText)?.text = 
                        document.getString("name") ?: "FitU User"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    context,
                    "Error loading user data: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
