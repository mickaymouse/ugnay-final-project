package ugnay.app.backend.brgy_officials.login

import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.data.UserType
import ugnay.app.backend.residents.login.LoginRepository

object OfficialLoginRepository {

    suspend fun loginAsOfficial(email: String, password: String): User? {
        val user = LoginRepository.login(email, password) ?: return null
        return if (user.userType == UserType.OFFICIAL) {
            user
        } else {
            LoginRepository.logout()
            null
        }
    }
}
