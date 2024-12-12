import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.R
import com.example.poseexercise.data.exercise.AssignedExercise
import java.text.SimpleDateFormat
import java.util.Locale

class AssignedExerciseAdapter(
    private val onItemClick: (AssignedExercise, View) -> Unit
) : RecyclerView.Adapter<AssignedExerciseAdapter.ViewHolder>() {
    private var exercises = listOf<AssignedExercise>()
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val exerciseImage: ImageView = itemView.findViewById(R.id.exerciseImage)
        private val exerciseNameText: TextView = itemView.findViewById(R.id.exerciseNameText)
        private val instructorNameText: TextView = itemView.findViewById(R.id.instructorNameText)
        private val repetitionsText: TextView = itemView.findViewById(R.id.repetitionsText)
        private val dueDateText: TextView = itemView.findViewById(R.id.dueDateText)
        private val statusText: TextView = itemView.findViewById(R.id.statusText)
        private val cardView: CardView = itemView.findViewById(R.id.exerciseCard)

        fun bind(exercise: AssignedExercise) {
            // Set exercise image based on exercise name
            val imageResource = when (exercise.exerciseName.lowercase()) {
                "push-up" -> R.drawable.push_up
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
            exerciseImage.setImageResource(imageResource)

            exerciseNameText.text = exercise.exerciseName
            instructorNameText.text = "Instructor: ${exercise.instructorName}"
            
            // Add attempt count display
            val attemptsText = "Attempts: ${exercise.attemptCount}/3"
            repetitionsText.text = "Repetitions: ${exercise.repetitions}\n$attemptsText"
            
            // Format due date
            val formattedDueDate = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val date = inputFormat.parse(exercise.dueDate)
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                "Due: ${date?.let { outputFormat.format(it) } ?: exercise.dueDate}"
            } catch (e: Exception) {
                "Due: ${exercise.dueDate}"
            }
            dueDateText.text = formattedDueDate

            // Disable click and show visual feedback if max attempts reached
            if (exercise.attemptCount >= 3) {
                itemView.alpha = 0.5f
                statusText.text = "Max Attempts Reached"
                statusText.setTextColor(ContextCompat.getColor(itemView.context, R.color.red))
            } else {
                itemView.alpha = 1.0f
                // Set status text and style
                statusText.text = if (exercise.isCompleted) "Completed" else "Assigned"
                
                // Get colors from resources
                val greenColor = ContextCompat.getColor(itemView.context, R.color.green)
                val orangeColor = ContextCompat.getColor(itemView.context, R.color.orange)
                val completedBgColor = ContextCompat.getColor(itemView.context, R.color.completed_exercise_background)
                val whiteColor = ContextCompat.getColor(itemView.context, R.color.white)
                
                // Apply colors
                statusText.setTextColor(if (exercise.isCompleted) greenColor else orangeColor)
                cardView.setCardBackgroundColor(if (exercise.isCompleted) completedBgColor else whiteColor)
            }

            // Only allow clicks if under max attempts
            itemView.setOnClickListener { 
                if (exercise.attemptCount < 3) {
                    onItemClick(exercise, itemView)
                }
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

    // Add getter for exercises list
    fun getExercises(): List<AssignedExercise>? = exercises
} 