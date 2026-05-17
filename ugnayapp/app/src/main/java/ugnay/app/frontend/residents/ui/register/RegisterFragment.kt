package ugnay.app.frontend.residents.ui.register

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import ugnay.app.R
import ugnay.app.backend.residents.data.Address
import ugnay.app.backend.residents.data.CivilStatus
import ugnay.app.backend.residents.data.Gender
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.data.UserType
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.backend.residents.register.RegisterRepository
import ugnay.app.backend.residents.utils.HashUtils
import ugnay.app.databinding.FragmentResidentsRegisterBinding
import java.util.Calendar
import java.util.Locale

class RegisterFragment : Fragment() {

    private var _binding: FragmentResidentsRegisterBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivRegisterProfile.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentsRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupDropdowns()
        setupDatePicker()
        setupRealTimeValidation()

        binding.fabUploadPhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.tvGoToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRealTimeValidation() {
        binding.etRegisterFirstname.doAfterTextChanged { validateForm(false) }
        binding.etRegisterLastname.doAfterTextChanged { validateForm(false) }
        binding.etRegisterEmail.doAfterTextChanged { validateForm(false) }
        binding.etRegisterPassword.doAfterTextChanged { validateForm(false) }
        binding.etRegisterConfirmPassword.doAfterTextChanged { validateForm(false) }
        binding.etRegisterPurok.doAfterTextChanged { validateForm(false) }
        binding.etRegisterBarangay.doAfterTextChanged { validateForm(false) }
        binding.etRegisterMunicipality.doAfterTextChanged { validateForm(false) }
        binding.etRegisterProvince.doAfterTextChanged { validateForm(false) }
        binding.etRegisterZip.doAfterTextChanged { validateForm(false) }
    }

    private fun validateForm(showErrors: Boolean): Boolean {
        val user = createUserObject("")
        val address = createAddressObject(user.userId)
        val confirmPassword = binding.etRegisterConfirmPassword.text.toString().trim()

        val errors = RegisterRepository.validateUser(user, address, confirmPassword)

        if (showErrors) {
            binding.etRegisterFirstname.error = errors["firstName"]
            binding.etRegisterLastname.error = errors["lastName"]
            binding.etRegisterEmail.error = errors["email"]
            binding.etRegisterPassword.error = errors["password"]
            binding.etRegisterConfirmPassword.error = errors["confirmPassword"]
            binding.etRegisterPurok.error = errors["purok"]
            binding.etRegisterBarangay.error = errors["barangay"]
            binding.etRegisterMunicipality.error = errors["municipality"]
            binding.etRegisterProvince.error = errors["province"]
            binding.etRegisterZip.error = errors["zipCode"]
        }

        return errors.isEmpty()
    }

    private fun createUserObject(profilePictureUrl: String): User {
        val genderStr = binding.actvRegisterGender.text.toString().trim()
        val genderEnum = when (genderStr.lowercase()) {
            "male" -> Gender.MALE
            "female" -> Gender.FEMALE
            else -> null
        }

        val statusStr = binding.actvRegisterCivilStatus.text.toString().trim()
        val statusEnum = when (statusStr) {
            "Single" -> CivilStatus.SINGLE
            "Married" -> CivilStatus.MARRIED
            "Widowed" -> CivilStatus.WIDOWED
            "Separated" -> CivilStatus.SEPARATED
            "Divorced" -> CivilStatus.DIVORCED
            "Annulled" -> CivilStatus.ANNULLED
            else -> CivilStatus.SINGLE
        }

        val emailInput = binding.etRegisterEmail.text.toString().trim()
        val emailAddress = when {
            emailInput.isEmpty() -> ""
            emailInput.contains("@") -> emailInput
            else -> "$emailInput@guintas.ph"
        }

        return User(
            userId = RegisterRepository.generateNextUserId(),
            firstName = binding.etRegisterFirstname.text.toString().trim(),
            middleName = binding.etRegisterMiddlename.text.toString().trim(),
            lastName = binding.etRegisterLastname.text.toString().trim(),
            userType = UserType.RESIDENT,
            gender = genderEnum,
            civilStatus = statusEnum,
            birthdate = binding.etRegisterDob.text.toString().trim(),
            birthplace = binding.etRegisterPob.text.toString().trim(),
            emailAddress = emailAddress,
            contactNumber = binding.etRegisterContact.text.toString().trim(),
            password = binding.etRegisterPassword.text.toString().trim(),
            profilePictureUrl = profilePictureUrl
        )
    }

    private fun createAddressObject(userId: String?): Address {
        return Address(
            userId = userId,
            purok = binding.etRegisterPurok.text.toString().trim(),
            barangay = binding.etRegisterBarangay.text.toString().trim(),
            municipality = binding.etRegisterMunicipality.text.toString().trim(),
            province = binding.etRegisterProvince.text.toString().trim(),
            zipCode = binding.etRegisterZip.text.toString().trim()
        )
    }

    private fun registerUser() {
        if (!validateForm(true)) return

        lifecycleScope.launch {
            try {
                binding.btnRegister.isEnabled = false
                
                var profileUrl: String? = null
                val userTemp = createUserObject("") // Temporary object to get the generated UUID
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

                RegisterRepository.registerUser(user, address)
                LoginRepository.updateCurrentUser(user)
                LoginRepository.updateCurrentAddress(address)

                Toast.makeText(requireContext(), "Registration Successful!", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_register_to_home)
            } catch (e: Exception) {
                binding.btnRegister.isEnabled = true
                Toast.makeText(requireContext(), "Registration Failed: ${e.message}", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }
        }
    }

    private fun setupDropdowns() {
        val genders = arrayOf("Male", "Female")
        val genderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, genders)
        binding.actvRegisterGender.setAdapter(genderAdapter)

        val civilStatuses = arrayOf("Single", "Married", "Widowed", "Separated", "Divorced", "Annulled")
        val statusAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, civilStatuses)
        binding.actvRegisterCivilStatus.setAdapter(statusAdapter)
    }

    private fun setupDatePicker() {
        binding.etRegisterDob.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val date = String.format(Locale.US, "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.etRegisterDob.setText(date)
            }, year, month, day).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
