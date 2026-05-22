package ugnay.app.backend.brgy_officials.login

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.data.UserType
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.backend.residents.utils.HashUtils

object OfficialLoginRepository {
    private const val TAG = "OfficialLoginRepo"

    suspend fun loginAsOfficial(email: String, password: String): User? {
        val trimmedEmail = email.trim()
        Log.d(TAG, "Attempting login for official: $trimmedEmail")

        // 1. FIRST: Check if user exists in the database and is a Barangay Official
        // We use ilike for case-insensitive matching and trim the input.
        val dbUser = try {
            val response = SupabaseConfig.client.from("users")
                .select {
                    filter {
                        ilike("email_address", trimmedEmail)
                    }
                }
            val user = response.decodeSingleOrNull<User>()
            Log.d(TAG, "DB search result: found=${user != null}, type=${user?.userType}")
            user
        } catch (e: Exception) {
            Log.e(TAG, "Error querying users table: ${e.message}", e)
            null
        }

        // If a record is found, it MUST be a Barangay Official.
        // Note: If dbUser is null, it might be due to RLS if the user hasn't logged in yet.
        // However, if the user was pre-registered in the 'users' table, we expect to find them.
        if (dbUser != null && dbUser.userType != UserType.BARANGAY_OFFICIAL) {
            Log.w(TAG, "User $trimmedEmail found but is NOT a Barangay Official (Type: ${dbUser.userType})")
            return null
        }

        // 2. THEN: Attempt to login via Supabase Auth
        Log.d(TAG, "Proceeding to authentication...")
        var user = LoginRepository.login(trimmedEmail, password)
        Log.d(TAG, "Initial login attempt result: ${if (user != null) "Success" else "Failure"}")

        // 3. SPECIAL CASE: First-time login with the default password "Guintas@2023"
        // This handles officials who are in the 'users' table but not yet in Supabase Auth.
        if (user == null && password == "Guintas@2023") {
            Log.i(TAG, "Normal login failed with default password. Attempting first-time setup flow.")
            try {
                // We re-fetch or use the dbUser if we have it
                val officialRecord = dbUser ?: try {
                    SupabaseConfig.client.from("users")
                        .select {
                            filter {
                                ilike("email_address", trimmedEmail)
                            }
                        }.decodeSingleOrNull<User>()
                } catch (e: Exception) { null }

                if (officialRecord != null && officialRecord.userType == UserType.BARANGAY_OFFICIAL) {
                    var authUserId: String? = null
                    
                    // Try to sign in first (might already exist in Auth but not linked in DB)
                    try {
                        SupabaseConfig.client.auth.signInWith(Email) {
                            this.email = trimmedEmail
                            this.password = "Guintas@2023"
                        }
                        authUserId = SupabaseConfig.client.auth.currentUserOrNull()?.id
                        Log.d(TAG, "Auth sign-in successful. UID: $authUserId")
                    } catch (e: Exception) {
                        Log.d(TAG, "Auth sign-in failed, trying sign-up: ${e.message}")
                        try {
                            val authUser = SupabaseConfig.client.auth.signUpWith(Email) {
                                this.email = trimmedEmail
                                this.password = "Guintas@2023"
                            }
                            authUserId = authUser?.id
                            Log.d(TAG, "Auth sign-up successful. UID: $authUserId")
                        } catch (signUpEx: Exception) {
                            Log.e(TAG, "Auth sign-up failed: ${signUpEx.message}")
                            return null
                        }
                    }

                    if (authUserId != null) {
                        // Update the database to link the Auth UID and set the hashed password
                        try {
                            Log.d(TAG, "Linking Auth UID $authUserId to email $trimmedEmail")
                            val updateResponse = try {
                                SupabaseConfig.client.from("users").update(
                                    {
                                        User::userId setTo authUserId
                                        User::password setTo HashUtils.hashPassword("Guintas@2023")
                                    }
                                ) {
                                    filter {
                                        ilike("email_address", trimmedEmail)
                                    }
                                    select()
                                }
                            } catch (rlsException: Exception) {
                                Log.e(TAG, "RLS might be blocking the update of 'users' table: ${rlsException.message}")
                                null
                            }
                            Log.d(TAG, "Database update response: ${updateResponse?.data}")
                            Log.d(TAG, "Database record linked attempt finished.")
                        } catch (updateEx: Exception) {
                            Log.e(TAG, "Failed to update database record (RLS?): ${updateEx.message}", updateEx)
                        }
                        
                        // Re-attempt login to populate LoginRepository session
                        user = LoginRepository.login(trimmedEmail, "Guintas@2023")
                        
                        // Fallback: If login failed to populate (e.g. RLS on select), manually set current user
                        if (user == null) {
                            Log.i(TAG, "Manually setting session user.")
                            val sessionUser = officialRecord.copy(userId = authUserId)
                            LoginRepository.updateCurrentUser(sessionUser)
                            user = sessionUser
                        }
                    }
                } else {
                    Log.w(TAG, "Setup aborted: User not found in database or not an official.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during first-time setup: ${e.message}")
            }
        }

        // FINAL VERIFICATION: Check user session and type
        val finalUser = if (user != null && user.userType == UserType.BARANGAY_OFFICIAL) {
            Log.i(TAG, "Official login successful: ${user.firstName}")
            user
        } else {
            Log.w(TAG, "Login failed or unauthorized. Final result: $user")
            if (user != null) LoginRepository.logout()
            null
        }

        return finalUser
    }
}
