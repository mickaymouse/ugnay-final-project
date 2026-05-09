package ugnay.app.backend.profile

import io.github.jan.supabase.postgrest.from
import ugnay.app.backend.SupabaseConfig
import ugnay.app.backend.data.Address
import ugnay.app.backend.data.User

object ProfileRepository {
    
    suspend fun updateProfile(user: User, address: Address): Result<Unit> {
        val userId = user.userId ?: return Result.failure(Exception("User ID is required for profile update"))
        return try {
            SupabaseConfig.client.from("users").update(user) {
                filter {
                    eq("user_id", userId)
                }
            }
            SupabaseConfig.client.from("address").update(address) {
                filter {
                    eq("user_id", userId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun validateProfileUpdate(user: User, address: Address): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        // Handle potentially nullable fields or String? types safely
        if (user.firstName.isBlank()) errors["firstName"] = "First name cannot be empty"
        if (user.lastName.isBlank()) errors["lastName"] = "Last name cannot be empty"
        
        val email = user.emailAddress
        if (email.isNullOrBlank()) {
            errors["email"] = "Email cannot be empty"
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errors["email"] = "Invalid email format"
        }

        if (address.purok.isBlank()) errors["purok"] = "Purok cannot be empty"
        if (address.barangay.isBlank()) errors["barangay"] = "Barangay cannot be empty"
        if (address.municipality.isBlank()) errors["municipality"] = "Municipality cannot be empty"
        if (address.province.isBlank()) errors["province"] = "Province cannot be empty"
        if (address.zipCode.isBlank()) errors["zipCode"] = "Zip Code cannot be empty"

        return errors
    }
}
