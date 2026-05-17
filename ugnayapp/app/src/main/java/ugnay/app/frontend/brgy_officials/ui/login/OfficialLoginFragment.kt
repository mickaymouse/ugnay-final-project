package ugnay.app.frontend.brgy_officials.ui.login

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
import ugnay.app.backend.brgy_officials.login.OfficialLoginRepository
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.databinding.FragmentOfficialLoginBinding
import android.content.Intent
import ugnay.app.frontend.brgy_officials.OfficialMainActivity

class OfficialLoginFragment : Fragment() {

    private var _binding: FragmentOfficialLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfficialLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOfficialLogin.setOnClickListener {
            loginOfficial()
        }

        binding.tvGoToOfficialRegister.setOnClickListener {
            findNavController().navigate(
                R.id.action_official_login_to_official_register
            )
        }

        binding.tvGoToResidentLogin.setOnClickListener {
            findNavController().navigate(
                R.id.action_official_login_to_resident_login
            )
        }
    }

    private fun loginOfficial() {

        Toast.makeText(
            requireContext(),
            "Logged in successfully!",
            Toast.LENGTH_SHORT
        ).show()

        startActivity(
            Intent(requireContext(), OfficialMainActivity::class.java)
        )

        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}