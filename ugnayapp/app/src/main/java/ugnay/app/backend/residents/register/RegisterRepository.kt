package ugnay.app.backend.residents.register

import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.storage.storage
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.Address
import ugnay.app.backend.residents.data.User
import java.util.UUID

object RegisterRepository {
    suspend fun registerUser(user: User, address: Address) {
        // Insert user first to satisfy foreign key constraints
        SupabaseConfig.client.from("users").insert(user)
        
        // Ensure address has the correct userId from the user object
        val addressToInsert = address.copy(userId = user.userId)
        SupabaseConfig.client.from("address").insert(addressToInsert)
    }

    private suspend fun getUserCount(): Long {
        val response = SupabaseConfig.client.from("users")
            .select {
                count(Count.EXACT)
            }
        return response.countOrNull() ?: 0
    }

    fun generateNextUserId(): String {
        return UUID.randomUUID().toString()
    }

    suspend fun uploadProfilePicture(userId: String, byteArray: ByteArray): String {
        val storage = SupabaseConfig.client.storage
        val bucket = storage.from("profile_pictures")
        val path = "avatars/$userId.jpg"

        bucket.upload(path, byteArray) {
            upsert = true
        }

        return bucket.publicUrl(path)
    }

    fun validateUser(user: User, address: Address, confirmPassword: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        if (user.firstName.isBlank()) errors["firstName"] = "First name is required"
        if (user.lastName.isBlank()) errors["lastName"] = "Last name is required"
        
        // Safely check nullable birthdate
        if (user.birthdate.isNullOrBlank()) {
            errors["dob"] = "Date of birth is required"
        }
        
        // Email validation: Enforce @guintas.ph domain and check for prefix
        val email = user.emailAddress ?: ""
        val prefix = email.substringBefore("@guintas.ph", "")
        
        if (email.isBlank() || prefix.isBlank()) {
            errors["email"] = "Initials/Words are required before @guintas.ph"
        } else if (!email.endsWith("@guintas.ph")) {
            errors["email"] = "Email must end with @guintas.ph"
        }
        
        // Password validation: at least 1 uppercase, 1 special char, 1 number, and 6+ chars
        val password = user.password ?: ""
        val passwordRegex = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[@#$%^&+=!]).{6,}$".toRegex()

        if (password.isBlank()) {
            errors["password"] = "Password is required"
        } else if (!passwordRegex.matches(password)) {
            errors["password"] = "Must have: 6+ chars, 1+ uppercase, 1+ number, 1+ special char"
        }
        
        if (confirmPassword != user.password) {
            errors["confirmPassword"] = "Passwords do not match"
        }

        if (address.purok.isBlank()) errors["purok"] = "Purok is required"
        if (address.barangay.isBlank()) errors["barangay"] = "Barangay is required"
        if (address.municipality.isBlank()) errors["municipality"] = "Municipality is required"
        if (address.province.isBlank()) errors["province"] = "Province is required"
        if (address.zipCode.isBlank()) errors["zipCode"] = "Zip Code is required"

        return errors
    }
}
