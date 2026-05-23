package ugnay.app.frontend.residents.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import ugnay.app.R
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.Address
import ugnay.app.backend.residents.data.CivilStatus
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.backend.residents.profile.ProfileRepository
import ugnay.app.backend.residents.register.RegisterRepository
import ugnay.app.databinding.FragmentAccountSettingsBinding

class AccountSettingsFragment : Fragment() {

    private var _binding: FragmentAccountSettingsBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.imgSettingsProfile.load(it) {
                transformations(CircleCropTransformation())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = LoginRepository.getCurrentUser()
        val currentAddress = LoginRepository.getCurrentAddress()
        
        setupCivilStatusSpinner()

        if (currentUser != null && currentAddress != null) {
            populateFields(currentUser, currentAddress)
        }

        binding.fabChangePhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSaveSettings.setOnClickListener {
            saveChanges()
        }
    }

    private fun setupCivilStatusSpinner() {
        val statuses = CivilStatus.values().map { it.name.lowercase().replaceFirstChar { it.uppercase() } }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses)
        binding.actSettingsCivilStatus.setAdapter(adapter)
    }

    private fun populateFields(user: User, address: Address) {
        binding.etSettingsFirstname.setText(user.firstName)
        binding.etSettingsMiddlename.setText(user.middleName)
        binding.etSettingsLastname.setText(user.lastName)
        
        user.civilStatus?.let {
            binding.actSettingsCivilStatus.setText(it.name.lowercase().replaceFirstChar { it.uppercase() }, false)
        }

        // Read-only fields
        binding.tvSettingsGender.text = user.gender?.name ?: "---"
        binding.tvSettingsBirthdate.text = user.birthdate ?: "---"
        binding.tvSettingsBirthplace.text = user.birthplace ?: "---"
        binding.tvSettingsEmail.text = user.emailAddress ?: "---"
        binding.tvSettingsContact.text = user.contactNumber ?: "---"

        if (!user.profilePictureUrl.isNullOrBlank()) {
            binding.imgSettingsProfile.load(user.profilePictureUrl) {
                transformations(CircleCropTransformation())
                placeholder(R.drawable.ic_person)
                error(R.drawable.ic_person)
            }
        }
    }

    private fun saveChanges() {
        val currentUser = LoginRepository.getCurrentUser() ?: return
        val currentAddress = LoginRepository.getCurrentAddress() ?: return
        
        lifecycleScope.launch {
            try {
                binding.btnSaveSettings.isEnabled = false
                
                var profileUrl = currentUser.profilePictureUrl
                selectedImageUri?.let { uri ->
                    val bytes = requireContext().contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        profileUrl = RegisterRepository.uploadProfilePicture(currentUser.userId!!, bytes)
                    }
                }

                val selectedCivilStatus = try {
                    CivilStatus.valueOf(binding.actSettingsCivilStatus.text.toString().uppercase())
                } catch (e: Exception) {
                    currentUser.civilStatus
                }

                // Password change if provided
                val newPassword = binding.etSettingsNewPassword.text.toString().trim()
                val confirmPassword = binding.etSettingsConfirmPassword.text.toString().trim()
                var hashedPassword = currentUser.password

                if (newPassword.isNotEmpty()) {
                    if (newPassword != confirmPassword) {
                        Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                        binding.btnSaveSettings.isEnabled = true
                        return@launch
                    }
                    if (newPassword.length < 6) {
                        Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                        binding.btnSaveSettings.isEnabled = true
                        return@launch
                    }
                    // Update password in Supabase Auth
                    SupabaseConfig.client.auth.updateUser {
                        password = newPassword
                    }
                    hashedPassword = ugnay.app.backend.residents.utils.HashUtils.hashPassword(newPassword)
                }

                val updatedUser = currentUser.copy(
                    firstName = binding.etSettingsFirstname.text.toString().trim(),
                    middleName = binding.etSettingsMiddlename.text.toString().trim(),
                    lastName = binding.etSettingsLastname.text.toString().trim(),
                    civilStatus = selectedCivilStatus,
                    profilePictureUrl = profileUrl,
                    password = hashedPassword
                )

                val errors = ProfileRepository.validateProfileUpdate(updatedUser, currentAddress)
                if (errors.isNotEmpty()) {
                    errors["firstName"]?.let { binding.etSettingsFirstname.error = it }
                    errors["lastName"]?.let { binding.etSettingsLastname.error = it }
                    binding.btnSaveSettings.isEnabled = true
                    return@launch
                }

                val result = ProfileRepository.updateProfile(updatedUser, currentAddress)
                if (result.isSuccess) {
                    LoginRepository.updateCurrentUser(updatedUser)
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.btnSaveSettings.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
