import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

data class AssignedExercise(
    val id: String = "",
    val classRosterId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val dueDate: String = "",
    val exerciseName: String = "",
    val instructorId: String = "",
    val instructorName: String = "",
    val repetitions: Int = 0
) 