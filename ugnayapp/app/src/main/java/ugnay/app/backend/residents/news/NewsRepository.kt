package ugnay.app.backend.residents.news

import io.github.jan.supabase.postgrest.from
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.News

object NewsRepository {
    suspend fun fetchNews(): List<News> {
        return SupabaseConfig.client.from("news").select().decodeList<News>()
    }
}
