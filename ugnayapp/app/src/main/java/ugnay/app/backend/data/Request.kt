package ugnay.app.backend.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Request(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("purpose")
    val purpose: String,
    @SerialName("status")
    val status: String = "Pending",
    @SerialName("created_at")
    val createdAt: String? = null
)
