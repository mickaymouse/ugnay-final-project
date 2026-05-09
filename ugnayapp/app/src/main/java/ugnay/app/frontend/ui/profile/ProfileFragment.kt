package ugnay.app.frontend.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ugnay.app.R
import ugnay.app.backend.login.LoginRepository
import ugnay.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        displayProfileInfo()

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_settings)
        }

        binding.btnLogout.setOnClickListener {
             LoginRepository.logout()
             findNavController().navigate(R.id.action_profile_to_login)
        }
    }

    private fun displayProfileInfo() {
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
