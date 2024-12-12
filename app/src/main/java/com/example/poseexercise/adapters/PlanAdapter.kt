package com.example.poseexercise.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.R
import com.example.poseexercise.data.plan.Plan
import com.example.poseexercise.posedetector.classification.PoseClassifierProcessor.CHEST_PRESS_CLASS
import com.example.poseexercise.posedetector.classification.PoseClassifierProcessor.DEAD_LIFT_CLASS
import com.example.poseexercise.posedetector.classification.PoseClassifierProcessor.LUNGES_CLASS
import com.example.poseexercise.posedetector.classification.PoseClassifierProcessor.PUSHUPS_CLASS
import com.example.poseexercise.posedetector.classification.PoseClassifierProcessor.SHOULDER_PRESS_CLASS
import com.example.poseexercise.posedetector.classification.PoseClassifierProcessor.SITUP_UP_CLASS
import com.example.poseexercise.posedetector.classification.PoseClassifierProcessor.SQUATS_CLASS
import com.example.poseexercise.util.MyUtils.Companion.databaseNameToClassification
import java.util.Collections

class PlanAdapter internal constructor(
    private val context: Context,
    private val navController: NavController
) : RecyclerView.Adapter<PlanAdapter.MyViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var planList: MutableList<Plan> = mutableListOf()
    private lateinit var listener: ItemListener


    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val workoutImage: ImageView = itemView.findViewById(R.id.imageView)
        val workoutName: TextView = itemView.findViewById(R.id.exercise_title)
        val repeat: TextView = itemView.findViewById(R.id.exercise_rep)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Inflate the item view from the layout
        val itemView = inflater.inflate(R.layout.plan_list_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        // Return the number of items in the plan list
        return planList.size
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Bind data to the item views
        val currentPlan = planList[position]
        holder.workoutImage.setImageResource(
            getDrawableResourceIdExercise(currentPlan.exercise)
        )
        holder.workoutName.text = currentPlan.exercise
        holder.repeat.text = "${currentPlan.repeatCount} ${currentPlan.exercise} a day"
        holder.deleteButton.setOnClickListener {
            // Handle delete button click and notify the listener
            listener.onItemClicked(currentPlan.id, position)
            notifyDataSetChanged()
        }
    }

    // Interface for item click events
    interface ItemListener {
        fun onItemClicked(planId: Int, position: Int)
    }

    // Set the listener for item click events
    fun setListener(listener: ItemListener) {
        this.listener = listener
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setPlans(newPlans: List<Plan>) {
        planList.clear()
        planList.addAll(newPlans)
        notifyDataSetChanged()
        Log.d("PlanAdapter", "Setting plans: ${planList.size}")
    }

    /**
     * List of exercise images
     */
    private val exerciseImages = mapOf(
        "Push-up" to R.drawable.push_up,
        "Squat" to R.drawable.squat,
        "Sit-up" to R.drawable.sit_ups,
        "Deadlift" to R.drawable.dead_lift,
        "Chest Press" to R.drawable.chest_press,
        "Shoulder Press" to R.drawable.shoulder_press,
        "Lunges" to R.drawable.reverse_lunges,
        "Warrior Yoga" to R.drawable.warrior_yoga_pose,
        "Tree Yoga" to R.drawable.tree_yoga_pose
    )

    private fun getDrawableResourceIdExercise(exerciseKey: String): Int {
        // Get the image resource ID for the given exercise key
        return exerciseImages[exerciseKey] ?: R.drawable.exercise_placeholder
    }

}