package ugnay.app.backend.brgy_officials.register

import ugnay.app.backend.residents.data.Address
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.register.RegisterRepository

object OfficialRegisterRepository {

    fun validateOfficial(user: User, address: Address, confirmPassword: String): Map<String, String> {
        val errors = RegisterRepository.validateUser(user, address, confirmPassword).toMutableMap()

        if (user.nationality.isNullOrBlank()) errors["nationality"] = "Nationality / Citizenship is required"
        if (user.position == null) errors["position"] = "Position is required"
        if (user.committeeAssignment.isNullOrBlank()) {
            errors["committee"] = "Committee assignment is required"
        }
        if (user.dateElected.isNullOrBlank()) errors["dateElected"] = "Date elected / appointed is required"
        if (user.teamStartDate.isNullOrBlank()) errors["teamStart"] = "Team start date is required"
        if (user.termEndDate.isNullOrBlank()) errors["termEnd"] = "Term end date is required"
        if (user.officialStatus == null) errors["status"] = "Status is required"

        return errors
    }

    suspend fun registerOfficial(user: User, address: Address) {
        RegisterRepository.registerUser(user, address)
    }
}
