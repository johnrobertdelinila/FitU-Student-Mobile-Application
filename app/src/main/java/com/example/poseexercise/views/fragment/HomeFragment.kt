package com.example.poseexercise.views.fragment

import Announcement
import AnnouncementAdapter
import AssignedExercise
import AssignedExerciseAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.R
import com.example.poseexercise.adapters.PlanAdapter
import com.example.poseexercise.adapters.RecentActivityAdapter
import com.example.poseexercise.data.database.AppRepository
import com.example.poseexercise.data.plan.Plan
import com.example.poseexercise.data.results.RecentActivityItem
import com.example.poseexercise.data.results.WorkoutResult
import com.example.poseexercise.util.MemoryManagement
import com.example.poseexercise.util.MyApplication
import com.example.poseexercise.util.MyUtils
import com.example.poseexercise.viewmodels.AddPlanViewModel
import com.example.poseexercise.viewmodels.HomeViewModel
import com.example.poseexercise.viewmodels.ResultViewModel
import com.example.poseexercise.views.activity.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import kotlin.math.min

class HomeFragment : Fragment(), PlanAdapter.ItemListener, MemoryManagement {
    @Suppress("PropertyName")
    val TAG = "RepDetect Home Fragment"
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var resultViewModel: ResultViewModel
    private lateinit var recentActivityRecyclerView: RecyclerView
    private lateinit var recentActivityAdapter: RecentActivityAdapter
    private var planList: List<Plan>? = emptyList()
    private var notCompletePlanList: MutableList<Plan>? = Collections.emptyList()
    private var today: String = DateFormat.format("EEEE", Date()) as String
    private lateinit var progressText: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var noPlanTV: TextView
    private var workoutResults: List<WorkoutResult>? = null
    private lateinit var appRepository: AppRepository
    private lateinit var addPlanViewModel: AddPlanViewModel
    private lateinit var adapter: PlanAdapter
    private lateinit var announcementAdapter: AnnouncementAdapter
    private lateinit var announcementRecyclerView: RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var noAnnouncementsText: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var assignedExercisesRecyclerView: RecyclerView
    private lateinit var assignedExerciseAdapter: AssignedExerciseAdapter
    private lateinit var noAssignedExercisesText: TextView
    private lateinit var assignedExercisesLoadingIndicator: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize ViewModels and repository
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        resultViewModel = ViewModelProvider(this)[ResultViewModel::class.java]
        addPlanViewModel = ViewModelProvider(this)[AddPlanViewModel::class.java]
        appRepository = AppRepository(requireActivity().application)
        
        // Initialize views
        progressText = view.findViewById(R.id.exercise_left)
        recyclerView = view.findViewById(R.id.today_plans)
        noPlanTV = view.findViewById(R.id.no_plan)
        
        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PlanAdapter(requireContext())
        recyclerView.adapter = adapter
        adapter.setListener(this)
        
        // Initially hide both views until we have data
        recyclerView.visibility = View.GONE
        noPlanTV.visibility = View.GONE
        
        // Load initial plans
        loadPlans()
        
        // Observe plans for changes
        homeViewModel.plans.observe(viewLifecycleOwner) { plans ->
            Log.d(TAG, "Plans changed in LiveData: ${plans.size}")
            if (plans.isNotEmpty()) {
                loadPlans()
            }
        }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Setup announcements RecyclerView
        announcementRecyclerView = view.findViewById(R.id.announcementRecyclerView)
        announcementAdapter = AnnouncementAdapter()
        announcementRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = announcementAdapter
        }
        noAnnouncementsText = view.findViewById(R.id.noAnnouncementsText)
        loadingIndicator = view.findViewById(R.id.announcementsLoadingIndicator)

        // Load announcements
        loadAnnouncements()

        setupAssignedExercisesSection(view)
        loadAssignedExercises()
    }

    private fun loadPlans() {
        lifecycleScope.launch {
            try {
                val today = (DateFormat.format("EEEE", Date()) as String).uppercase()
                Log.d(TAG, "Loading plans for day: $today")
                
                val result1 = withContext(Dispatchers.IO) { 
                    homeViewModel.getPlanByDay(today).also {
                        Log.d(TAG, "Plans for today: ${it?.size}, Content: $it")
                    }
                }
                val result2 = withContext(Dispatchers.IO) { 
                    homeViewModel.getNotCompletePlans(today).also {
                        Log.d(TAG, "Not completed plans: ${it?.size}, Content: $it")
                    }
                }
                
                withContext(Dispatchers.Main) {
                    updateResultFromDatabase(result1, result2)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading plans", e)
            }
        }
    }

    private fun updateResultFromDatabase(plan: List<Plan>?, notCompleted: MutableList<Plan>?) {
        planList = plan
        notCompletePlanList = notCompleted

        // Update UI with plan counts
        val exerciseLeftString = resources.getString(R.string.exercise_left, notCompletePlanList?.size ?: 0)
        progressText.text = exerciseLeftString

        // Update adapter with not completed plans and handle visibility
        if (notCompletePlanList.isNullOrEmpty()) {
            recyclerView.visibility = View.GONE
            noPlanTV.visibility = View.VISIBLE
            noPlanTV.text = getString(R.string.there_is_no_plan_set_at_the_moment)
            Log.d(TAG, "No plans to display, showing empty state")
        } else {
            recyclerView.visibility = View.VISIBLE
            noPlanTV.visibility = View.GONE
            adapter.setPlans(notCompletePlanList!!)
            Log.d(TAG, "Updated adapter with ${notCompletePlanList!!.size} plans")
        }

        // Force layout update
        recyclerView.post {
            adapter.notifyDataSetChanged()
        }
    }

    // Return true if the timestamp is today's date
    private fun isToday(s: Long, locale: Locale = Locale.getDefault()): Boolean {
        return try {
            val sdf = SimpleDateFormat("MM/dd/yyyy", locale)
            val netDate = Date(s)
            val currentDate = sdf.format(Date())
            sdf.format(netDate) == currentDate
        } catch (e: Exception) {
            false
        }
    }

    // Get the day from which the plan was marked as complete
    private fun getDayFromTimestamp(time: Long, locale: Locale = Locale.getDefault()): String? {
        return try {
            val sdf = SimpleDateFormat("EEEE", locale)
            val netDate = Date(time)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    // Delete the plan when user click on delete icon
    override fun onItemClicked(planId: Int, position: Int) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        // Show a dialog for user to confirm the choice
        builder
            .setMessage("Are you sure you want to delete the plan?")
            .setTitle("Delete plan")
            .setPositiveButton("Delete") { dialog, _ ->
                // Delete the plan from database
                lifecycleScope.launch {
                    addPlanViewModel.deletePlan(planId)
                }
                notCompletePlanList?.removeAt(position)
                adapter.notifyItemRemoved(position)
                updateEmptyPlan(notCompletePlanList)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // Cancel the action
                dialog.dismiss()
            }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    // Hide the recycler view if there are no plan left for today
    private fun updateEmptyPlan(plans: MutableList<Plan>?) {
        if (plans.isNullOrEmpty()) {
            noPlanTV.text = getString(R.string.there_is_no_plan_set_at_the_moment)
            recyclerView.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
        }
    }

    override fun clearMemory() {
        planList = null
        notCompletePlanList = null
        workoutResults = null
    }

    override fun onDestroy() {
        clearMemory()
        super.onDestroy()
    }

    private fun loadAnnouncements() {
        val currentUser = auth.currentUser ?: return
        
        // Show loading state
        loadingIndicator.visibility = View.VISIBLE
        announcementRecyclerView.visibility = View.GONE
        noAnnouncementsText.visibility = View.GONE

        db.collection("classRosters")
            .whereArrayContains("students", currentUser.uid)
            .get()
            .addOnSuccessListener { rosters ->
                if (!rosters.isEmpty) {
                    val instructorId = rosters.documents[0].getString("instructorId")
                    if (instructorId != null) {
                        fetchAnnouncementsForInstructor(instructorId, currentUser.uid)
                    } else {
                        showError("Instructor ID not found")
                    }
                } else {
                    hideLoading()
                    showNoAnnouncements()
                }
            }
            .addOnFailureListener { e ->
                showError("Error loading class roster: ${e.message}")
            }
    }

    private fun fetchAnnouncementsForInstructor(instructorId: String, studentId: String) {
        db.collection("instructors")
            .document(instructorId)
            .get()
            .addOnSuccessListener { instructorDoc ->
                val instructorName = instructorDoc.getString("fullName") ?: "Unknown Instructor"
                
                db.collection("announcements")
                    .whereEqualTo("instructorId", instructorId)
                    .whereArrayContains("students", studentId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        val announcements = documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Announcement::class.java).copy(
                                    instructorName = instructorName
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing announcement: ${e.message}")
                                null
                            }
                        }
                        
                        hideLoading()
                        if (announcements.isEmpty()) {
                            showNoAnnouncements()
                        } else {
                            showAnnouncements(announcements)
                        }
                    }
                    .addOnFailureListener { e ->
                        showError("Error loading announcements: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showError("Error loading instructor details: ${e.message}")
            }
    }

    private fun hideLoading() {
        loadingIndicator.visibility = View.GONE
    }

    private fun showNoAnnouncements() {
        announcementRecyclerView.visibility = View.GONE
        noAnnouncementsText.visibility = View.VISIBLE
    }

    private fun showAnnouncements(announcements: List<Announcement>) {
        announcementRecyclerView.visibility = View.VISIBLE
        noAnnouncementsText.visibility = View.GONE
        announcementAdapter.updateAnnouncements(announcements)
    }

    private fun showError(message: String) {
        hideLoading()
        showNoAnnouncements()
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun setupAssignedExercisesSection(view: View) {
        assignedExercisesRecyclerView = view.findViewById(R.id.assignedExercisesRecyclerView)
        noAssignedExercisesText = view.findViewById(R.id.noAssignedExercisesText)
        assignedExercisesLoadingIndicator = view.findViewById(R.id.assignedExercisesLoadingIndicator)
        
        assignedExerciseAdapter = AssignedExerciseAdapter { exercise, itemView ->
            // Add logging to verify the data
            Log.d(TAG, "Selected exercise: ${exercise.exerciseName}, Repetitions: ${exercise.repetitions}")
            
            // Create and show confirmation dialog
            val dialogBuilder = AlertDialog.Builder(requireContext())
            dialogBuilder.setTitle("Start Exercise")
            
            // Format the due date
            val formattedDueDate = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val date = inputFormat.parse(exercise.dueDate)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                date?.let { outputFormat.format(it) } ?: exercise.dueDate
            } catch (e: Exception) {
                exercise.dueDate
            }
            
            // Create message with exercise details
            val message = """
                Exercise: ${exercise.exerciseName}
                Repetitions: ${exercise.repetitions}
                Instructor: ${exercise.instructorName}
                Due Date: $formattedDueDate
            """.trimIndent()
            
            dialogBuilder.setMessage(message)
            
            dialogBuilder.setPositiveButton("START") { dialog, _ ->
                try {
                    // Store exercise details in MainActivity
                    MainActivity.currentExerciseName = exercise.exerciseName
                    MainActivity.currentClassRosterId = exercise.classRosterId
                    MainActivity.currentRepetitions = exercise.repetitions
                    MainActivity.currentExerciseAssignmentId = exercise.id
                    
                    // Add logging to verify the data
                    Log.d(TAG, "Storing exercise details - " +
                        "exerciseName: ${exercise.exerciseName}, " +
                        "repetitions: ${exercise.repetitions}, " +
                        "assignmentId: ${exercise.id}, " +
                        "classRosterId: ${exercise.classRosterId}")
                    
                    // Navigate to workout fragment
                    Navigation.findNavController(view)
                        .navigate(R.id.action_homeFragment_to_workoutFragment)
                    
                    dialog.dismiss()
                } catch (e: Exception) {
                    Log.e(TAG, "Error navigating to workout: ${e.message}", e)
                    Toast.makeText(context, "Error starting workout", Toast.LENGTH_SHORT).show()
                }
            }
            
            dialogBuilder.setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            
            // Create and show the dialog
            val dialog = dialogBuilder.create()
            dialog.show()
        }
        
        assignedExercisesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = assignedExerciseAdapter
        }
    }

    private fun loadAssignedExercises() {
        val currentUser = auth.currentUser ?: return
        
        assignedExercisesLoadingIndicator.visibility = View.VISIBLE
        assignedExercisesRecyclerView.visibility = View.GONE
        noAssignedExercisesText.visibility = View.GONE

        // First get the class roster
        db.collection("classRosters")
            .whereArrayContains("students", currentUser.uid)
            .get()
            .addOnSuccessListener { rosters ->
                if (!rosters.isEmpty) {
                    val roster = rosters.documents[0]
                    val instructorId = roster.getString("instructorId")
                    val rosterId = roster.id
                    
                    if (instructorId != null) {
                        fetchAssignedExercises(rosterId, instructorId)
                    } else {
                        showAssignedExercisesError("Instructor not found")
                    }
                } else {
                    showNoAssignedExercises()
                }
            }
            .addOnFailureListener { e ->
                showAssignedExercisesError("Error loading roster: ${e.message}")
            }
    }

    private fun fetchAssignedExercises(rosterId: String, instructorId: String) {
        // First get the instructor details
        db.collection("instructors")
            .document(instructorId)
            .get()
            .addOnSuccessListener { instructorDoc ->
                val instructorName = instructorDoc.getString("fullName") ?: "Unknown Instructor"
                
                // Get current timestamp in ISO 8601 format
                val currentDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    .format(Date())
                
                // Then fetch exercises that are not past due
                db.collection("exerciseAssignments")
                    .whereEqualTo("classRosterId", rosterId)
                    .whereEqualTo("instructorId", instructorId)
                    .whereGreaterThanOrEqualTo("dueDate", currentDate)  // Only get assignments due now or in the future
                    .get()
                    .addOnSuccessListener { documents ->
                        val exercises = documents.mapNotNull { doc ->
                            try {
                                // Include the document ID in the AssignedExercise object
                                doc.toObject(AssignedExercise::class.java).copy(
                                    id = doc.id,  // Set the document ID here
                                    instructorName = instructorName
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error parsing exercise: ${e.message}")
                                null
                            }
                        }
                        
                        hideAssignedExercisesLoading()
                        if (exercises.isEmpty()) {
                            showNoAssignedExercises()
                        } else {
                            showAssignedExercises(exercises)
                        }
                    }
                    .addOnFailureListener { e ->
                        showAssignedExercisesError("Error loading exercises: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showAssignedExercisesError("Error loading instructor details: ${e.message}")
            }
    }

    private fun hideAssignedExercisesLoading() {
        assignedExercisesLoadingIndicator.visibility = View.GONE
    }

    private fun showNoAssignedExercises() {
        hideAssignedExercisesLoading()
        assignedExercisesRecyclerView.visibility = View.GONE
        noAssignedExercisesText.visibility = View.VISIBLE
    }

    private fun showAssignedExercises(exercises: List<AssignedExercise>) {
        assignedExercisesRecyclerView.visibility = View.VISIBLE
        noAssignedExercisesText.visibility = View.GONE
        assignedExerciseAdapter.updateExercises(exercises)
    }

    private fun showAssignedExercisesError(message: String) {
        hideAssignedExercisesLoading()
        showNoAssignedExercises()
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
