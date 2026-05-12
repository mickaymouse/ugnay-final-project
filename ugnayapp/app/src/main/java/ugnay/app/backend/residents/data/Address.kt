package ugnay.app.backend.residents.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    @SerialName("address_id")
    val addressId: String? = null,
    @SerialName("user_id")
    val userId: String?,
    @SerialName("purok")
    val purok: String,
    @SerialName("barangay")
    val barangay: String,
    @SerialName("municipality")
    val municipality: String,
    @SerialName("province")
    val province: String,
    @SerialName("zip_code")
    val zipCode: String
)
