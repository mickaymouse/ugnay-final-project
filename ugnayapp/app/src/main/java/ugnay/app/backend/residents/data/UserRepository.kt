package ugnay.app.backend.residents.data

import io.github.jan.supabase.postgrest.from
import ugnay.app.backend.residents.SupabaseConfig

object UserRepository {
    suspend fun getUserById(userId: String): User? {
        return try {
            SupabaseConfig.client.from("users")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<User>()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAddressByUserId(userId: String): Address? {
        return try {
            SupabaseConfig.client.from("address")
                .select {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeSingleOrNull<Address>()
        } catch (e: Exception) {
            null
        }
    }
}
