package ugnay.app.backend.news

import io.github.jan.supabase.postgrest.from
import ugnay.app.backend.SupabaseConfig
import ugnay.app.backend.data.News

object NewsRepository {
    suspend fun fetchNews(): List<News> {
        return SupabaseConfig.client.from("news").select().decodeList<News>()
    }
}
