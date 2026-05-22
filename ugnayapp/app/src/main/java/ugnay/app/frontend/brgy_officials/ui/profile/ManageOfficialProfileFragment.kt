package ugnay.app.frontend.brgy_officials.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.databinding.FragmentManageOfficialProfileBinding

class ManageOfficialProfileFragment : Fragment() {

    private var _binding: FragmentManageOfficialProfileBinding? = null
    private val binding get() = _binding!!

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

        loadCurrentProfile()

        binding.btnSaveProfile.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadCurrentProfile() {
        val user = LoginRepository.getCurrentUser() ?: return
        binding.apply {
            etFirstName.setText(user.firstName)
            etLastName.setText(user.lastName)
            etContactNumber.setText(user.contactNumber)
        }
    }

    private fun saveProfile() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val contactNumber = binding.etContactNumber.text.toString().trim()
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

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
                binding.btnSaveProfile.isEnabled = false
                val currentUser = LoginRepository.getCurrentUser() ?: return@launch

                // 1. Update User Data in DB
                val updatedUser = currentUser.copy(
                    firstName = firstName,
                    lastName = lastName,
                    contactNumber = contactNumber
                )

                SupabaseConfig.client.from("users").update(
                    {
                        User::firstName setTo firstName
                        User::lastName setTo lastName
                        User::contactNumber setTo contactNumber
                    }
                ) {
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

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                binding.btnSaveProfile.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
