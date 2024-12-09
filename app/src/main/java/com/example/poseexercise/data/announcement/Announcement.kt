import com.google.firebase.Timestamp

data class Announcement(
    val id: String = "",
    val instructorId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val students: List<String> = listOf(),
    val instructorName: String = ""
) 