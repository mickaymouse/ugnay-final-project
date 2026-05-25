package ugnay.app.frontend.brgy_officials.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import kotlinx.coroutines.launch
import ugnay.app.R
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.databinding.FragmentOfficialProfileBinding
import ugnay.app.frontend.MainActivity

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

        loadCurrentOfficialProfile()

//        binding.btnManageResidents.setOnClickListener {
//            findNavController().navigate(R.id.action_profile_to_manage_residents)
//        }
//
//        binding.btnManageOfficials.setOnClickListener {
//            findNavController().navigate(R.id.action_profile_to_manage_officials)
//        }

        binding.btnManageProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_manage_profile)
        }

        binding.btnLogout.setOnClickListener {
            LoginRepository.logout()
            // Start MainActivity to go to login page instead of trying to navigate within official_nav_graph
            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadCurrentOfficialProfile() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val currentOfficial = LoginRepository.getCurrentUser()

                if (currentOfficial != null) {
                    // Retaining logic while updating ID display format
                    val fullName = "${currentOfficial.firstName} ${currentOfficial.lastName}".trim()
                    binding.tvOfficialName.text = fullName.ifEmpty { "Unnamed Official" }
                    binding.tvOfficialRole.text = currentOfficial.position?.toString() ?: "Barangay Official"
                    binding.tvOfficialEmail.text = currentOfficial.emailAddress ?: "No Email Linked"
                    binding.tvOfficialPhone.text = currentOfficial.contactNumber ?: "No Contact Number"

                    // Use the GUIN- format for any official ID display if needed
                    // Example: val formattedId = "ID: GUIN-${currentOfficial.userId?.take(4)?.uppercase() ?: "0000"}"

                    if (!currentOfficial.profilePictureUrl.isNullOrBlank()) {
                        binding.ivOfficialAvatar.load(currentOfficial.profilePictureUrl) {
                            crossfade(true)
                            placeholder(R.drawable.ic_person)
                            error(R.drawable.ic_person)
                            listener(onSuccess = { _, _ ->
                                binding.ivOfficialAvatar.imageTintList = null
                            })
                        }
                    } else {
                        // Ensure clear state for placeholder
                        binding.ivOfficialAvatar.setImageResource(R.drawable.ic_person)
                    }

                } else {
                    showErrorState("No active session found. Please log in again.")
                }
            } catch (e: Exception) {
                showErrorState("Database error: ${e.localizedMessage}")
            }
        }
    }

    private fun showErrorState(message: String) {
        if (isAdded) { // Safety check
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
            binding.tvOfficialName.text = "Error Loading Profile"
            binding.tvOfficialRole.text = "Unavailable"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}