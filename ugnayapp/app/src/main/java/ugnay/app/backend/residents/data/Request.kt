package ugnay.app.backend.residents.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    @SerialName("request_id")
    val requestId: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("full_name")
    val fullName: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("purpose")
    val purpose: String? = null,
    @SerialName("status")
    val status: RequestStatus = RequestStatus.PENDING,
    @SerialName("start_date")
    val startDate: String? = null,
    @SerialName("end_date")
    val endDate: String? = null
)

@Serializable
enum class RequestStatus(val displayName: String) {
    @SerialName("Pending") PENDING("Pending"),
    @SerialName("Approved") APPROVED("Approved"),
    @SerialName("Rejected") REJECTED("Rejected"),
    @SerialName("Done") DONE("Done"),
    @SerialName("Expired") EXPIRED("Expired");

    companion object {
        fun fromString(value: String?): RequestStatus {
            return entries.find { it.displayName.equals(value, ignoreCase = true) } ?: PENDING
        }
    }
}
