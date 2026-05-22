package ugnay.app.backend.residents.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant

@Serializable
data class News(
    @SerialName("id")
    val id: String? = null,
    @SerialName("announcement_id")
    val announcementId: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("title")
    val title: String,
    @SerialName("content")
    val content: String,
    @SerialName("date_posted")
    val datePosted: String? = null,
    @SerialName("duration")
    val duration: String? = null,
    @SerialName("status")
    val status: String? = null,
    @SerialName("priority")
    val priority: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null
) {
    fun relativeDuration(): String? = computeRelativeDuration(datePosted)

    fun priorityRank(): Int = when (priority) {
        "Critical" -> 2
        "High" -> 1
        else -> 0
    }
}

private fun computeRelativeDuration(dateString: String?): String? {
    if (dateString.isNullOrBlank()) return null

    return try {
        val postedAt = Instant.parse(dateString)
        val diff = Duration.between(postedAt, Instant.now())
        when {
            diff.toMinutes() < 1 -> "Just now"
            diff.toMinutes() < 60 -> "${diff.toMinutes()} min ago"
            diff.toHours() < 24 -> "${diff.toHours()} hr ago"
            diff.toDays() < 7 -> "${diff.toDays()} d ago"
            else -> "${diff.toDays()} d ago"
        }
    } catch (e: Exception) {
        null
    }
}
