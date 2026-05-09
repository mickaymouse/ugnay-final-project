package ugnay.app.backend.login

import io.github.jan.supabase.postgrest.from
import ugnay.app.backend.SupabaseConfig
import ugnay.app.backend.data.Address
import ugnay.app.backend.data.User
import ugnay.app.backend.utils.HashUtils

object LoginRepository {
    private var currentUser: User? = null
    private var currentAddress: Address? = null

    fun validateLogin(email: String, password: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (email.isEmpty()) errors["email"] = "Email address is required"
        if (password.isEmpty()) errors["password"] = "Password is required"
        return errors
    }

    suspend fun login(email: String, password: String): User? {
        val hashedPassword = HashUtils.hashPassword(password)
        val userResponse = SupabaseConfig.client.from("users")
            .select {
                filter {
                    eq("email_address", email)
                    eq("password", hashedPassword)
                }
            }
        currentUser = userResponse.decodeSingleOrNull<User>()
        
        if (currentUser != null) {
            val addressResponse = SupabaseConfig.client.from("address")
                .select {
                    filter {
                        eq("user_id", currentUser!!.userId!!)
                    }
                }
            currentAddress = addressResponse.decodeSingleOrNull<Address>()
        }
        
        return currentUser
    }

    fun getCurrentUser(): User? = currentUser
    
    fun getCurrentAddress(): Address? = currentAddress

    fun updateCurrentUser(user: User) {
        currentUser = user
    }
    
    fun updateCurrentAddress(address: Address) {
        currentAddress = address
    }

    fun logout() {
        currentUser = null
        currentAddress = null
    }
}
