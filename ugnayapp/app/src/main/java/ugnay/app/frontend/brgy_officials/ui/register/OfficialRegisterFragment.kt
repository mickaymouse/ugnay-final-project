package ugnay.app.frontend.brgy_officials.ui.register

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ugnay.app.R
import ugnay.app.backend.brgy_officials.register.OfficialRegisterRepository
import ugnay.app.backend.residents.data.Address
import ugnay.app.backend.residents.data.CivilStatus
import ugnay.app.backend.residents.data.Gender
import ugnay.app.backend.residents.data.OfficialStatus
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.data.UserPosition
import ugnay.app.backend.residents.data.UserType
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.backend.residents.register.RegisterRepository
import ugnay.app.backend.residents.utils.HashUtils
import ugnay.app.databinding.FragmentOfficialRegisterBinding
import java.util.Calendar
import java.util.Locale

class OfficialRegisterFragment : Fragment() {

    private var _binding: FragmentOfficialRegisterBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivOfficialRegisterProfile.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfficialRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupDatePickers()
        setupRealtimeValidation()

        binding.fabOfficialUploadPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnOfficialRegister.setOnClickListener {
            registerOfficial()
        }

        binding.tvOfficialGoToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRealtimeValidation() {
        val fields = listOf(
            binding.etOfficialFirstname,
            binding.etOfficialLastname,
            binding.etOfficialEmail,
            binding.etOfficialPassword,
            binding.etOfficialConfirmPassword,
            binding.etOfficialPurok,
            binding.etOfficialBarangay,
            binding.etOfficialMunicipality,
            binding.etOfficialProvince,
            binding.etOfficialZip,
            binding.etOfficialNationality,
            binding.etOfficialCommittee
        )
        fields.forEach { it.doAfterTextChanged { validateForm(false) } }
    }

    private fun validateForm(showErrors: Boolean): Boolean {
        val user = createUserObject("")
        val address = createAddressObject(user.userId)
        val confirmPassword = binding.etOfficialConfirmPassword.text.toString().trim()

        val errors = OfficialRegisterRepository.validateOfficial(user, address, confirmPassword)

        if (showErrors) {
            binding.etOfficialFirstname.error = errors["firstName"]
            binding.etOfficialLastname.error = errors["lastName"]
            binding.etOfficialEmail.error = errors["email"]
            binding.etOfficialPassword.error = errors["password"]
            binding.etOfficialConfirmPassword.error = errors["confirmPassword"]
            binding.etOfficialPurok.error = errors["purok"]
            binding.etOfficialBarangay.error = errors["barangay"]
            binding.etOfficialMunicipality.error = errors["municipality"]
            binding.etOfficialProvince.error = errors["province"]
            binding.etOfficialZip.error = errors["zipCode"]
            binding.etOfficialDob.error = errors["dob"]
            binding.etOfficialNationality.error = errors["nationality"]
            binding.actvOfficialPosition.error = errors["position"]
            binding.etOfficialCommittee.error = errors["committee"]
            binding.etOfficialDateElected.error = errors["dateElected"]
            binding.etOfficialTeamStart.error = errors["teamStart"]
            binding.etOfficialTermEnd.error = errors["termEnd"]
            binding.actvOfficialStatus.error = errors["status"]
        }

        return errors.isEmpty()
    }

    private fun genderFromUi(): Gender? {
        val genderStr = binding.actvOfficialGender.text.toString().trim()
        return when (genderStr.lowercase(Locale.US)) {
            "male" -> Gender.MALE
            "female" -> Gender.FEMALE
            else -> null
        }
    }

    private fun civilFromUi(): CivilStatus {
        val statusStr = binding.actvOfficialCivilStatus.text.toString().trim()
        return when (statusStr) {
            "Single" -> CivilStatus.SINGLE
            "Married" -> CivilStatus.MARRIED
            "Widowed" -> CivilStatus.WIDOWED
            "Separated" -> CivilStatus.SEPARATED
            "Divorced" -> CivilStatus.DIVORCED
            "Annulled" -> CivilStatus.ANNULLED
            else -> CivilStatus.SINGLE
        }
    }

    private fun positionFromUi(): UserPosition? {
        val label = binding.actvOfficialPosition.text.toString().trim()
        return when (label) {
            "Barangay Captain" -> UserPosition.BRGY_CAPTAIN
            "Kagawad" -> UserPosition.KAGAWAD
            "Secretary" -> UserPosition.SECRETARY
            "Treasurer" -> UserPosition.TREASURER
            "SK Chairperson" -> UserPosition.SK_CHAIRPERSON
            "Councilor" -> UserPosition.COUNCILOR
            else -> null
        }
    }

    private fun officialStatusFromUi(): OfficialStatus? {
        return when (binding.actvOfficialStatus.text.toString().trim()) {
            "Active" -> OfficialStatus.ACTIVE
            "Inactive" -> OfficialStatus.INACTIVE
            "Suspended" -> OfficialStatus.SUSPENDED
            else -> null
        }
    }

    private fun createUserObject(profilePictureUrl: String): User {
        val emailInput = binding.etOfficialEmail.text.toString().trim()
        val emailAddress = when {
            emailInput.isEmpty() -> ""
            emailInput.contains("@") -> emailInput
            else -> "$emailInput@guintas.ph"
        }

        val suffix = binding.etOfficialSuffix.text.toString().trim().ifBlank { null }

        return User(
            userId = RegisterRepository.generateNextUserId(),
            firstName = binding.etOfficialFirstname.text.toString().trim(),
            middleName = binding.etOfficialMiddlename.text.toString().trim().ifBlank { null },
            lastName = binding.etOfficialLastname.text.toString().trim(),
            nameSuffix = suffix,
            userType = UserType.OFFICIAL,
            gender = genderFromUi(),
            civilStatus = civilFromUi(),
            birthdate = binding.etOfficialDob.text.toString().trim(),
            birthplace = binding.etOfficialPob.text.toString().trim(),
            nationality = binding.etOfficialNationality.text.toString().trim(),
            emailAddress = emailAddress,
            contactNumber = binding.etOfficialContact.text.toString().trim(),
            password = binding.etOfficialPassword.text.toString().trim(),
            profilePictureUrl = profilePictureUrl,
            position = positionFromUi(),
            committeeAssignment = binding.etOfficialCommittee.text.toString().trim(),
            dateElected = binding.etOfficialDateElected.text.toString().trim(),
            teamStartDate = binding.etOfficialTeamStart.text.toString().trim(),
            termEndDate = binding.etOfficialTermEnd.text.toString().trim(),
            officialStatus = officialStatusFromUi()
        )
    }

    private fun createAddressObject(userId: String?): Address {
        return Address(
            userId = userId,
            purok = binding.etOfficialPurok.text.toString().trim(),
            barangay = binding.etOfficialBarangay.text.toString().trim(),
            municipality = binding.etOfficialMunicipality.text.toString().trim(),
            province = binding.etOfficialProvince.text.toString().trim(),
            zipCode = binding.etOfficialZip.text.toString().trim()
        )
    }

    private fun registerOfficial() {
        if (!validateForm(true)) return

        lifecycleScope.launch {
            try {
                binding.btnOfficialRegister.isEnabled = false

                var profileUrl: String? = null
                val userTemp = createUserObject("")
                val userId = userTemp.userId!!

                selectedImageUri?.let { uri ->
                    val bytes = requireContext().contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        profileUrl = RegisterRepository.uploadProfilePicture(userId, bytes)
                    }
                }

                val hashed = HashUtils.hashPassword(userTemp.password ?: "")
                val user = userTemp.copy(profilePictureUrl = profileUrl ?: "", password = hashed)
                val address = createAddressObject(userId)

                OfficialRegisterRepository.registerOfficial(user, address)
                LoginRepository.updateCurrentUser(user)
                LoginRepository.updateCurrentAddress(address)

                Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_official_register_to_home)
            } catch (e: Exception) {
                binding.btnOfficialRegister.isEnabled = true
                Toast.makeText(requireContext(), "Registration Failed: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun setupDropdowns() {
        val genders = arrayOf("Male", "Female")
        binding.actvOfficialGender.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        )

        val civilStatuses = arrayOf(
            "Single", "Married", "Widowed", "Separated", "Divorced", "Annulled"
        )
        binding.actvOfficialCivilStatus.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, civilStatuses)
        )

        val positions = arrayOf(
            "Barangay Captain",
            "Kagawad",
            "Secretary",
            "Treasurer",
            "SK Chairperson",
            "Councilor"
        )
        binding.actvOfficialPosition.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, positions)
        )

        val statuses = arrayOf("Active", "Inactive", "Suspended")
        binding.actvOfficialStatus.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses)
        )
    }

    private fun setupDatePickers() {
        val listenerFactory: (android.widget.EditText) -> View.OnClickListener = { target ->
            View.OnClickListener {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    requireContext(),
                    { _, y, m, d ->
                        target.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d))
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        }

        binding.etOfficialDob.setOnClickListener(listenerFactory(binding.etOfficialDob))
        binding.etOfficialDateElected.setOnClickListener(listenerFactory(binding.etOfficialDateElected))
        binding.etOfficialTeamStart.setOnClickListener(listenerFactory(binding.etOfficialTeamStart))
        binding.etOfficialTermEnd.setOnClickListener(listenerFactory(binding.etOfficialTermEnd))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
