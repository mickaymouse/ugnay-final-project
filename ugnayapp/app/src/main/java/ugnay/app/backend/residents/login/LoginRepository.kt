package ugnay.app.backend.residents.login

import io.github.jan.supabase.auth.*
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.Address
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.utils.HashUtils

object LoginRepository {
    private var currentUser: User? = null
    private var currentAddress: Address? = null

    fun validateLogin(email: String, password: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (email.isEmpty()) errors["email"] = "Email address is required"
        if (password.isEmpty()) errors["password"] = "Password is required"
        return errors
    }

    suspend fun login(emailAddress: String, password: String): User? {
        try {
            SupabaseConfig.client.auth.signInWith(Email) {
                email = emailAddress
                this.password = password
            }
        } catch (e: Exception) {
            return null
        }

        val authUserId = SupabaseConfig.client.auth.currentUserOrNull()?.id
            ?: SupabaseConfig.client.auth.retrieveUserForCurrentSession().id

        val userResponse = SupabaseConfig.client.from("users")
            .select {
                filter {
                    eq("user_id", authUserId)
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
        } else {
            runCatching {
                SupabaseConfig.client.auth.signOut()
            }
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
