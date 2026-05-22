package ugnay.app.backend.residents.news

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.backend.residents.data.News
import java.util.UUID

object NewsRepository {
    private const val TAG = "NewsRepository"

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

        // Crucial: Use the ID from the current auth session if available to ensure RLS compliance
        val authUserId = try {
            SupabaseConfig.client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }

        val effectiveUserId = authUserId ?: currentUser.userId
        
        Log.d(TAG, "Creating announcement. Auth Session UID: $authUserId, Local User ID: ${currentUser.userId}, User Type: ${currentUser.userType}")

        val id = announcementId ?: UUID.randomUUID().toString()
        val timestamp = java.time.Instant.now().toString()
        val announcement = News(
            announcementId = id,
            userId = effectiveUserId,
            title = title,
            content = content,
            datePosted = timestamp,
            status = "Active",
            priority = priority,
            imageUrl = imageUrl
        )

        Log.d(TAG, "Inserting announcement: $announcement")

        return try {
            SupabaseConfig.client.from("announcements")
                .insert(announcement) {
                    select()
                }
                .decodeSingleOrNull<News>()
                ?: throw Exception("Failed to save announcement (null response)")
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting announcement: ${e.message}", e)
            throw e
        }
    }
}

