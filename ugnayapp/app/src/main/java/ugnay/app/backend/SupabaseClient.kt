package ugnay.app.backend

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseConfig {
    private const val SUPABASE_URL = "https://ayzydnwrsecujoubohuy.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImF5enlkbndyc2VjdWpvdWJvaHV5Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3Nzc1OTUwMjcsImV4cCI6MjA5MzE3MTAyN30.uUiAZ3muMG4L5iuwiLIEdxvDk75V2L-TLJolRO6AhW4"

    val client = createSupabaseClient(SUPABASE_URL, SUPABASE_KEY) {
        install(Postgrest)
        install(Storage)
    }
}
