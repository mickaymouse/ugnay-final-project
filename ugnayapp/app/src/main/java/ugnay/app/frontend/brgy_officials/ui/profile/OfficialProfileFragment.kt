package ugnay.app.frontend.brgy_officials.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ugnay.app.R
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

        binding.tvProfileTitle.text = "Official Profile"

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
            findNavController().navigate(
                R.id.action_official_profile_to_login
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}