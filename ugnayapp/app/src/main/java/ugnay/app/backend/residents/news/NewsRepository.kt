package ugnay.app.backend.residents.news

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.backend.residents.data.News
import java.util.UUID

object NewsRepository {
    suspend fun fetchNews(): List<News> {
        return SupabaseConfig.client.from("announcements")
            .select()
            .decodeList<News>()
            .sortedWith(
                compareByDescending<News> { it.priorityRank() }
                    .thenByDescending { it.datePosted }
            )
    }

    suspend fun uploadAnnouncementImage(announcementId: String, byteArray: ByteArray): String {
        val storage = SupabaseConfig.client.storage
        val bucket = storage.from("announcement_images")
        val path = "announcements/$announcementId.jpg"

        bucket.upload(path, byteArray) {
            upsert = true
        }

        return bucket.publicUrl(path)
    }

    suspend fun createAnnouncement(
        title: String,
        content: String,
        priority: String = "Normal",
        imageUrl: String? = null,
        announcementId: String? = null
    ): News {
        val currentUser = LoginRepository.getCurrentUser()
            ?: throw Exception("You must be logged in to publish announcements")

        val id = announcementId ?: UUID.randomUUID().toString()
        val timestamp = java.time.Instant.now().toString()
        val announcement = News(
            announcementId = id,
            userId = currentUser.userId,
            title = title,
            content = content,
            datePosted = timestamp,
            status = "Active",
            priority = priority,
            imageUrl = imageUrl
        )

        return SupabaseConfig.client.from("announcements")
            .insert(announcement) {
                // Postgrest insert requests default to minimal return values.
                // Call select() to have the inserted row returned for decoding.
                select()
            }
            .decodeSingleOrNull<News>()
            ?: throw Exception("Failed to save announcement")
    }
}

