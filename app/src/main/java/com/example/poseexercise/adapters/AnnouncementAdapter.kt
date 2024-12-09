import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.poseexercise.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.util.Log
import com.google.firebase.Timestamp

class AnnouncementAdapter : RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {
    private var announcements: List<Announcement> = listOf()
    private val TAG = "AnnouncementAdapter"

    class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.announcementTitle)
        val instructorText: TextView = itemView.findViewById(R.id.instructorName)
        val messageText: TextView = itemView.findViewById(R.id.announcementMessage)
        val timestampText: TextView = itemView.findViewById(R.id.timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        Log.d(TAG, "Creating new ViewHolder")
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcements[position]
        Log.d(TAG, "Binding announcement at position $position: $announcement")
        
        holder.titleText.text = announcement.title
        holder.instructorText.text = "Posted by ${announcement.instructorName}"
        holder.messageText.text = announcement.message
        holder.timestampText.text = formatTimestamp(announcement.timestamp)
        
        // Make sure the item view is visible
        holder.itemView.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int {
        Log.d(TAG, "getItemCount: ${announcements.size}")
        return announcements.size
    }

    fun updateAnnouncements(newAnnouncements: List<Announcement>) {
        Log.d(TAG, "Updating announcements: ${newAnnouncements.size}")
        announcements = newAnnouncements
        notifyDataSetChanged()
    }

    private fun formatTimestamp(timestamp: Timestamp): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(timestamp.toDate())
    }
} 