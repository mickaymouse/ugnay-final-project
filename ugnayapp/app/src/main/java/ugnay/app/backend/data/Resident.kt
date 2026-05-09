package ugnay.app.backend.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Resident(
    @SerialName("user_id")
    val userId: String,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("middle_name")
    val middleName: String,
    @SerialName("last_name")
    val lastName: String,
    @SerialName("gender")
    val gender: String,
    @SerialName("civil_status")
    val civilStatus: String,
    @SerialName("date_of_birth")
    val dob: String,
    @SerialName("place_of_birth")
    val pob: String,
    @SerialName("purok")
    val purok: String,
    @SerialName("barangay")
    val barangay: String,
    @SerialName("municipality")
    val municipality: String,
    @SerialName("province")
    val province: String,
    @SerialName("zip_code")
    val zipCode: String,
    @SerialName("email")
    val email: String,
    @SerialName("contact_number")
    val contactNumber: String,
    @SerialName("password")
    val password: String
)
