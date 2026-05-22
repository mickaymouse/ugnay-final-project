package ugnay.app.backend.residents.data

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
    @SerialName("name_suffix")
    val nameSuffix: String? = null,
    @SerialName("nationality")
    val nationality: String? = null,
    @SerialName("committee_assignment")
    val committeeAssignment: String? = null,
    @SerialName("date_elected")
    val dateElected: String? = null,
    @SerialName("term_start_date")
    val termStartDate: String? = null,
    @SerialName("term_end_date")
    val termEndDate: String? = null,
    @SerialName("official_status")
    val officialStatus: OfficialStatus? = null,
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
    @SerialName("Separated") SEPARATED,
    @SerialName("Divorced") DIVORCED,
    @SerialName("Annulled") ANNULLED
}

@Serializable
enum class UserType {
    @SerialName("Resident") RESIDENT,
    @SerialName("Barangay Official") BARANGAY_OFFICIAL, // Matches our RLS Policy strings
    @SerialName("Admin") ADMIN
}

@Serializable
enum class UserPosition {
    @SerialName("Barangay Captain") BRGY_CAPTAIN,
    @SerialName("Captain") CAPTAIN_LEGACY,
    @SerialName("Kagawad") KAGAWAD,
    @SerialName("Secretary") SECRETARY,
    @SerialName("Treasurer") TREASURER,
    @SerialName("SK Chairperson") SK_CHAIRPERSON,
    @SerialName("SK Chairman") SK_CHAIRMAN_LEGACY,
    @SerialName("Councilor") COUNCILOR,
    @SerialName("Resident") RESIDENT,
    @SerialName("Admin") ADMIN
}

@Serializable
enum class OfficialStatus {
    @SerialName("Active") ACTIVE,
    @SerialName("Inactive") INACTIVE,
    @SerialName("Suspended") SUSPENDED
}