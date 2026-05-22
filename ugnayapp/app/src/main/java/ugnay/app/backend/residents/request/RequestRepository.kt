package ugnay.app.backend.residents.request

import io.github.jan.supabase.postgrest.from
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.Request
import ugnay.app.backend.residents.data.RequestStatus
import ugnay.app.backend.residents.login.LoginRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object RequestRepository {
    suspend fun submitRequest(request: Request) {
        val currentUser = LoginRepository.getCurrentUser()
        val userId = currentUser?.userId ?: throw Exception("User not logged in")

        val startDate = LocalDate.now()
        val endDate = startDate.plusMonths(1)
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE

        val finalRequest = request.copy(
            userId = userId,
            status = RequestStatus.PENDING,
            startDate = startDate.format(formatter),
            endDate = endDate.format(formatter)
        )

        SupabaseConfig.client.from("requests").insert(finalRequest)
    }

    suspend fun getAllRequests(): List<Request> {
        return SupabaseConfig.client.from("requests")
            .select()
            .decodeList<Request>()
    }

    suspend fun getResidentRequests(): List<Request> {
        val currentUser = LoginRepository.getCurrentUser()
        val userId = currentUser?.userId ?: return emptyList()

        return SupabaseConfig.client.from("requests")
            .select {
                filter {
                    eq("user_id", userId)
                }
            }
            .decodeList<Request>()
    }

    suspend fun updateRequestStatus(requestId: String, status: RequestStatus) {
        SupabaseConfig.client.from("requests").update(
            {
                Request::status setTo status.displayName
            }
        ) {
            filter {
                eq("request_id", requestId)
            }
        }
    }

    fun validateRequest(request: Request): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (request.fullName.isNullOrEmpty()) errors["fullName"] = "Name is required"
        if (request.purpose.isNullOrEmpty()) errors["purpose"] = "Purpose is required"
        if (request.type.isNullOrEmpty()) errors["type"] = "Document type is required"
        
        val allowedTypes = listOf(
            "Certificate of Indigency",
            "Certificate of Residency",
            "Barangay ID",
            "Barangay Clearance"
        )
        if (request.type != null && request.type !in allowedTypes) {
            errors["type"] = "Invalid document type"
        }

        return errors
    }
}
