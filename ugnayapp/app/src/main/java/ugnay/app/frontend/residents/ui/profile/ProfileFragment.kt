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
        val user = LoginRepository.getCurrentUser() ?: return

        binding.apply {
            // Change from tv_profile_name to tvProfileName, etc.
            tvProfileName.text = "${user.firstName} ${user.middleName} ${user.lastName}".trim()
            // Updated line inside binding.apply
            tvUserId.text = "GUIN - ${user.userId?.take(8)?.uppercase() ?: "0000"}"

            tvProfileEmail.text = user.emailAddress ?: "Not set"
            tvProfileContact.text = user.contactNumber ?: "Not set"
            tvGender.text = user.gender?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "N/A"
            tvCivilStatus.text = user.civilStatus?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "N/A"

            if (!user.profilePictureUrl.isNullOrBlank()) {
                imgProfile.load(user.profilePictureUrl) {
                    transformations(CircleCropTransformation())
                    crossfade(true)
                }
                imgProfile.imageTintList = null
            } else {
                imgProfile.setImageResource(R.drawable.ic_person)
                imgProfile.imageTintList = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}