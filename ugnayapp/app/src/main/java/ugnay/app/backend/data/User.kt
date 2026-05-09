package ugnay.app.backend.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("firstname")
    val firstName: String,
    @SerialName("middle_name")
    val middleName: String? = null,
    @SerialName("lastname")
    val lastName: String,
    @SerialName("gender")
    val gender: Gender? = null,
    @SerialName("user_type")
    val userType: UserType? = UserType.RESIDENT,
    @SerialName("civil_status")
    val civilStatus: CivilStatus? = null,
    @SerialName("birthdate")
    val birthdate: String? = null,
    @SerialName("birthplace")
    val birthplace: String? = null,
    @SerialName("email_address")
    val emailAddress: String? = null,
    @SerialName("contact_number")
    val contactNumber: String? = null,
    @SerialName("position")
    val position: UserPosition? = null,
    @SerialName("password")
    val password: String? = null,
    @SerialName("profile_picture_url")
    val profilePictureUrl: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

@Serializable
enum class Gender {
    @SerialName("male") MALE,
    @SerialName("female") FEMALE,
}

@Serializable
enum class CivilStatus {
    @SerialName("Single") SINGLE,
    @SerialName("Married") MARRIED,
    @SerialName("Widowed") WIDOWED,
    @SerialName("Separated") SEPARATED
}

@Serializable
enum class UserType {
    @SerialName("Resident") RESIDENT,
    @SerialName("Barangay Official") OFFICIAL, // Matches our RLS Policy strings
    @SerialName("Admin") ADMIN
}

@Serializable
enum class UserPosition {
    @SerialName("Captain") CAPTAIN,
    @SerialName("Secretary") SECRETARY,
    @SerialName("Treasurer") TREASURER,
    @SerialName("Councilor") COUNCILOR,
    @SerialName("SK Chairman") SK_CHAIRMAN,
    @SerialName("Resident") NONE
}