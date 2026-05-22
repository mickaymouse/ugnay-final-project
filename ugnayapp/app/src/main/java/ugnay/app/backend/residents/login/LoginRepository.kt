package ugnay.app.backend.residents.login

import android.util.Log
import io.github.jan.supabase.auth.*
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.Address
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.utils.HashUtils

object LoginRepository {
    private const val TAG = "LoginRepository"
    private var currentUser: User? = null
    private var currentAddress: Address? = null

    fun validateLogin(email: String, password: String): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        if (email.isEmpty()) errors["email"] = "Email address is required"
        if (password.isEmpty()) errors["password"] = "Password is required"
        return errors
    }

    suspend fun login(emailAddress: String, password: String): User? {
        Log.d(TAG, "Login attempt for: $emailAddress")
        try {
            SupabaseConfig.client.auth.signInWith(Email) {
                email = emailAddress
                this.password = password
            }
            Log.d(TAG, "Auth sign-in successful for $emailAddress")
        } catch (e: Exception) {
            Log.e(TAG, "Auth sign-in failed: ${e.message}")
            return null
        }

        val authUserId = try {
            SupabaseConfig.client.auth.currentUserOrNull()?.id
                ?: SupabaseConfig.client.auth.retrieveUserForCurrentSession().id
        } catch (e: Exception) {
            Log.e(TAG, "Failed to retrieve auth UID: ${e.message}")
            null
        }

        Log.d(TAG, "Auth UID retrieved: $authUserId")

        if (authUserId == null) return null

        val userResponse = try {
            SupabaseConfig.client.from("users")
                .select {
                    filter {
                        eq("user_id", authUserId)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query users table for UID $authUserId: ${e.message}")
            null
        }

        currentUser = userResponse?.decodeSingleOrNull<User>()
        Log.d(TAG, "Current user after DB lookup: $currentUser")
        
        if (currentUser != null) {
            val addressResponse = try {
                SupabaseConfig.client.from("address")
                    .select {
                        filter {
                            eq("user_id", currentUser!!.userId!!)
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to query address table: ${e.message}")
                null
            }
            currentAddress = addressResponse?.decodeSingleOrNull<Address>()
        } else {
            Log.w(TAG, "User record not found in DB for UID $authUserId. Signing out.")
            runCatching {
                SupabaseConfig.client.auth.signOut()
            }
        }
        
        return currentUser
    }

    fun getCurrentUser(): User? {
        Log.d(TAG, "getCurrentUser() called, returning: $currentUser")
        return currentUser
    }
    
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
