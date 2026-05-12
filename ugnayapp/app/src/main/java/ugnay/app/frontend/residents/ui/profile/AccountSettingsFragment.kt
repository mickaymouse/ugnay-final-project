package ugnay.app.frontend.residents.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ugnay.app.backend.residents.data.Address
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.backend.residents.profile.ProfileRepository
import ugnay.app.databinding.FragmentAccountSettingsBinding

class AccountSettingsFragment : Fragment() {

    private var _binding: FragmentAccountSettingsBinding? = null
    private val binding get() = _binding!!

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
        
        if (currentUser != null && currentAddress != null) {
            populateFields(currentUser, currentAddress)
        }

        binding.btnSaveSettings.setOnClickListener {
            saveChanges()
        }
    }

    private fun populateFields(user: User, address: Address) {
        binding.etSettingsFirstname.setText(user.firstName)
        binding.etSettingsLastname.setText(user.lastName)
        binding.etSettingsEmail.setText(user.emailAddress)
        binding.etSettingsContact.setText(user.contactNumber)
        
        binding.etSettingsPurok.setText(address.purok)
        binding.etSettingsBarangay.setText(address.barangay)
        binding.etSettingsMunicipality.setText(address.municipality)
        binding.etSettingsProvince.setText(address.province)
        binding.etSettingsZip.setText(address.zipCode)
    }

    private fun saveChanges() {
        val currentUser = LoginRepository.getCurrentUser() ?: return
        val currentAddress = LoginRepository.getCurrentAddress() ?: return
        
        val updatedUser = currentUser.copy(
            firstName = binding.etSettingsFirstname.text.toString().trim(),
            lastName = binding.etSettingsLastname.text.toString().trim(),
            emailAddress = binding.etSettingsEmail.text.toString().trim(),
            contactNumber = binding.etSettingsContact.text.toString().trim()
        )

        val updatedAddress = currentAddress.copy(
            purok = binding.etSettingsPurok.text.toString().trim(),
            barangay = binding.etSettingsBarangay.text.toString().trim(),
            municipality = binding.etSettingsMunicipality.text.toString().trim(),
            province = binding.etSettingsProvince.text.toString().trim(),
            zipCode = binding.etSettingsZip.text.toString().trim()
        )

        val errors = ProfileRepository.validateProfileUpdate(updatedUser, updatedAddress)
        if (errors.isNotEmpty()) {
            errors["firstName"]?.let { binding.etSettingsFirstname.error = it }
            errors["lastName"]?.let { binding.etSettingsLastname.error = it }
            errors["email"]?.let { binding.etSettingsEmail.error = it }
            
            errors["purok"]?.let { binding.etSettingsPurok.error = it }
            errors["barangay"]?.let { binding.etSettingsBarangay.error = it }
            errors["municipality"]?.let { binding.etSettingsMunicipality.error = it }
            errors["province"]?.let { binding.etSettingsProvince.error = it }
            errors["zipCode"]?.let { binding.etSettingsZip.error = it }
            return
        }

        lifecycleScope.launch {
            try {
                binding.btnSaveSettings.isEnabled = false
                val result = ProfileRepository.updateProfile(updatedUser, updatedAddress)
                if (result.isSuccess) {
                    LoginRepository.updateCurrentUser(updatedUser)
                    LoginRepository.updateCurrentAddress(updatedAddress)
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
