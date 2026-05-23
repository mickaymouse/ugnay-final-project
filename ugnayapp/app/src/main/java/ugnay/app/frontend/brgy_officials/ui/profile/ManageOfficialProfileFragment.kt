package ugnay.app.frontend.brgy_officials.ui.profile

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
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import ugnay.app.R
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.CivilStatus
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.backend.residents.register.RegisterRepository
import ugnay.app.databinding.FragmentManageOfficialProfileBinding

class ManageOfficialProfileFragment : Fragment() {

    private var _binding: FragmentManageOfficialProfileBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.imgOfficialProfile.load(it) {
                transformations(CircleCropTransformation())
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageOfficialProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCivilStatusSpinner()
        loadCurrentProfile()

        binding.fabOfficialChangePhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSaveOfficial.setOnClickListener {
            saveProfile()
        }
    }

    private fun setupCivilStatusSpinner() {
        val statuses = CivilStatus.values().map { it.name.lowercase().replaceFirstChar { it.uppercase() } }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, statuses)
        binding.actOfficialCivilStatus.setAdapter(adapter)
    }

    private fun loadCurrentProfile() {
        val user = LoginRepository.getCurrentUser() ?: return
        binding.apply {
            etOfficialFirstname.setText(user.firstName)
            etOfficialMiddlename.setText(user.middleName)
            etOfficialLastname.setText(user.lastName)
            
            user.civilStatus?.let {
                actOfficialCivilStatus.setText(it.name.lowercase().replaceFirstChar { it.uppercase() }, false)
            }

            // Read-only info
            tvOfficialGender.text = user.gender?.name ?: "---"
            tvOfficialBirthdate.text = user.birthdate ?: "---"
            tvOfficialBirthplace.text = user.birthplace ?: "---"
            tvOfficialEmail.text = user.emailAddress ?: "---"
            tvOfficialContact.text = user.contactNumber ?: "---"
            
            if (!user.profilePictureUrl.isNullOrBlank()) {
                imgOfficialProfile.load(user.profilePictureUrl) {
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_person)
                    error(R.drawable.ic_person)
                }
            }
        }
    }

    private fun saveProfile() {
        val firstName = binding.etOfficialFirstname.text.toString().trim()
        val middleName = binding.etOfficialMiddlename.text.toString().trim()
        val lastName = binding.etOfficialLastname.text.toString().trim()
        val newPassword = binding.etOfficialNewPassword.text.toString().trim()
        val confirmPassword = binding.etOfficialConfirmPassword.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(requireContext(), "Name fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword.isNotEmpty()) {
            if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                return
            }
            if (newPassword.length < 6) {
                Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.btnSaveOfficial.isEnabled = false
                val currentUser = LoginRepository.getCurrentUser() ?: return@launch

                var profileUrl = currentUser.profilePictureUrl
                selectedImageUri?.let { uri ->
                    val bytes = requireContext().contentResolver.openInputStream(uri)?.readBytes()
                    if (bytes != null) {
                        profileUrl = RegisterRepository.uploadProfilePicture(currentUser.userId!!, bytes)
                    }
                }

                val selectedCivilStatus = try {
                    CivilStatus.valueOf(binding.actOfficialCivilStatus.text.toString().uppercase())
                } catch (e: Exception) {
                    currentUser.civilStatus
                }

                // 1. Update User Data in DB
                val updatedUser = currentUser.copy(
                    firstName = firstName,
                    middleName = middleName,
                    lastName = lastName,
                    civilStatus = selectedCivilStatus,
                    profilePictureUrl = profileUrl
                )

                SupabaseConfig.client.from("users").update(updatedUser) {
                    filter {
                        eq("user_id", currentUser.userId ?: "")
                    }
                }

                // 2. Update Password if provided
                if (newPassword.isNotEmpty()) {
                    SupabaseConfig.client.auth.updateUser {
                        password = newPassword
                    }
                }

                LoginRepository.updateCurrentUser(updatedUser)
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnSaveOfficial.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
