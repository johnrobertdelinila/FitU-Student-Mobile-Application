import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.R
import com.example.poseexercise.data.exercise.AssignedExercise
import java.text.SimpleDateFormat
import java.util.*

class AssignedExerciseAdapter(
    private val onItemClick: (AssignedExercise, View) -> Unit
) : RecyclerView.Adapter<AssignedExerciseAdapter.ViewHolder>() {
    private var exercises = listOf<AssignedExercise>()
    
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val exerciseImage: ImageView = view.findViewById(R.id.exerciseImage)
        val exerciseName: TextView = view.findViewById(R.id.exerciseName)
        val exerciseDetails: TextView = view.findViewById(R.id.exerciseDetails)
        val instructorName: TextView = view.findViewById(R.id.instructorName)
        val dueDate: TextView = view.findViewById(R.id.dueDate)

        fun bind(exercise: AssignedExercise) {
            // Set exercise name
            exerciseName.text = exercise.exerciseName
            
            // Set exercise details (repetitions)
            exerciseDetails.text = "${exercise.repetitions} ${exercise.exerciseName} a day"
            
            // Set instructor name
            instructorName.text = "Assigned by ${exercise.instructorName}"
            
            // Set due date with proper formatting
            dueDate.text = "Due: ${formatDate(exercise.dueDate)}"
            
            // Set exercise image based on exercise name
            exerciseImage.setImageResource(getExerciseImage(exercise.exerciseName))
            
            // Add click listener to the entire item view
            itemView.setOnClickListener {
                this@AssignedExerciseAdapter.onItemClick(exercise, itemView)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_assigned_exercise, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.bind(exercise)
    }

    override fun getItemCount() = exercises.size

    fun updateExercises(newExercises: List<AssignedExercise>) {
        exercises = newExercises
        notifyDataSetChanged()
    }

    private fun getExerciseImage(exerciseName: String): Int {
        return when (exerciseName.lowercase()) {
            "pushup" -> R.drawable.push_up
            "squat" -> R.drawable.squat
            "sit-up" -> R.drawable.sit_ups
            "deadlift" -> R.drawable.dead_lift
            "chest press" -> R.drawable.chest_press
            "shoulder press" -> R.drawable.shoulder_press
            "lunges" -> R.drawable.reverse_lunges
            "warrior yoga" -> R.drawable.warrior_yoga_pose
            "tree yoga" -> R.drawable.tree_yoga_pose
            else -> R.drawable.exercise_placeholder
        }
    }

    private fun formatDate(dateString: String): String {
        try {
            // Parse the ISO 8601 date string
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            
            // Format the date in a more readable way
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            return date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            return dateString // Return original string if parsing fails
        }
    }

    // Add getter for exercises list
    fun getExercises(): List<AssignedExercise>? = exercises
} 