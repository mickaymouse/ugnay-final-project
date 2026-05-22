package ugnay.app.frontend.residents.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ugnay.app.R
import ugnay.app.backend.residents.data.UserType
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.databinding.FragmentResidentsProfileBinding

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
            binding.tvProfileName.text = "${user.firstName} ${user.lastName}"
            binding.tvProfileEmail.text = user.emailAddress
            binding.tvProfileUserId.text = user.userId
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}