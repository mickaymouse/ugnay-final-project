package ugnay.app.frontend.brgy_officials.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ugnay.app.R
import ugnay.app.backend.residents.login.LoginRepository // Uses the central session repository
import ugnay.app.databinding.FragmentOfficialProfileBinding

class OfficialProfileFragment : Fragment() {

    private var _binding: FragmentOfficialProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfficialProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Automatically load data from the logged-in session
        loadCurrentOfficialProfile()

        // Navigation Actions
        binding.btnManageResidents.setOnClickListener {
            findNavController().navigate(R.id.nav_manage_residents)
        }

        binding.btnManageOfficials.setOnClickListener {
            findNavController().navigate(R.id.nav_manage_officials)
        }

        binding.btnManageProfile.setOnClickListener {
            findNavController().navigate(R.id.nav_manage_official_profile)
        }

        binding.btnLogout.setOnClickListener {
            LoginRepository.logout()
            findNavController().navigate(R.id.action_official_profile_to_login)
        }
    }

    private fun loadCurrentOfficialProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Fetching the user that was authenticated inside the LoginRepository
                // Swap with LoginRepository.getCurrentUser() if configured as a function
                val currentOfficial = LoginRepository.getCurrentUser()

                if (currentOfficial != null) {
                    val fullName = "${currentOfficial.firstName} ${currentOfficial.lastName}".trim()

                    binding.tvOfficialName.text = fullName.ifEmpty { "Unnamed Official" }
                    binding.tvOfficialRole.text = currentOfficial.position?.toString() ?: "Barangay Official"
                    binding.tvOfficialEmail.text = currentOfficial.emailAddress ?: "No Email Linked"
                    binding.tvOfficialPhone.text = currentOfficial.contactNumber ?: "No Contact Number"

                    binding.tvOfficialId.text = currentOfficial.userId?.uppercase() ?: "NO ID FOUND"

                } else {
                    showErrorState("No active session found. Please log in again.")
                }
            } catch (e: Exception) {
                showErrorState("Database error: ${e.localizedMessage}")
            }
        }
    }

    private fun showErrorState(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        binding.tvOfficialName.text = "Error Loading Profile"
        binding.tvOfficialRole.text = "Unavailable"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}