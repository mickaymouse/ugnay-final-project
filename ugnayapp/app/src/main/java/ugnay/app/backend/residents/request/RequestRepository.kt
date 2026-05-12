package ugnay.app.backend.residents.request

import io.github.jan.supabase.postgrest.from
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.Request

object RequestRepository {
    suspend fun submitRequest(request: Request) {
        SupabaseConfig.client.from("requests").insert(request)
    }

    fun validateRequest(request: Request): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (request.fullName.isEmpty()) errors["fullName"] = "Name is required"
        if (request.purpose.isEmpty()) errors["purpose"] = "Purpose is required"
        return errors
    }
}
