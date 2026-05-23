package ugnay.app.frontend.residents.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import ugnay.app.R
import ugnay.app.backend.residents.data.UserType
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.databinding.FragmentResidentsProfileBinding
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentResidentsProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentsProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadProfile()

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_settings)
        }

        binding.btnLogout.setOnClickListener {
            val userType = LoginRepository.getCurrentUser()?.userType
            LoginRepository.logout()

            if (userType == UserType.BARANGAY_OFFICIAL) {
                findNavController().navigate(R.id.action_profile_to_official_login)
            } else {
                findNavController().navigate(R.id.action_profile_to_login)
            }
        }
    }

    private fun loadProfile() {
        val user = LoginRepository.getCurrentUser()

        if (user != null) {
            // Profile Text Components
            binding.tvProfileName.text = "${user.firstName} ${user.lastName}".trim()

            binding.tvProfileEmail.text = user.emailAddress ?: "No email address linked"
            binding.tvProfileContact.text = user.contactNumber ?: "No contact number"

            // Async Image Fetching via Supabase Public URL String
            if (!user.profilePictureUrl.isNullOrBlank()) {
                binding.imgProfile.load(user.profilePictureUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_person)
                    error(R.drawable.ic_person)
                }
                // Clear the default tint so user photo colors aren't overridden by the yellow filter
                binding.imgProfile.imageTintList = null
                binding.imgProfile.setPadding(0, 0, 0, 0)
            } else {
                // Fallback state if no image URL exists
                binding.imgProfile.setImageResource(R.drawable.ic_person)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}