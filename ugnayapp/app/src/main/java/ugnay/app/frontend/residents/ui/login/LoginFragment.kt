package ugnay.app.frontend.residents.ui.login

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
import ugnay.app.backend.residents.data.UserType
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.databinding.FragmentResidentsLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentResidentsLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentsLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            loginUser()
        }

        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // SWITCH TO OFFICIAL LOGIN (top right switch)
        binding.tvGoToOfficialLogin.setOnClickListener {
            findNavController().navigate(
                R.id.action_login_to_official_login
            )
        }
    }

    private fun loginUser() {

        val emailInput = binding.etLoginEmail.text.toString().trim()
        val password = binding.etLoginPassword.text.toString().trim()

        val email = if (emailInput.isEmpty()) {
            ""
        } else if (emailInput.contains("@")) {
            emailInput
        } else {
            "$emailInput@guintas.ph"
        }

        val errors = LoginRepository.validateLogin(email, password)

        if (errors.isNotEmpty()) {
            errors["email"]?.let { binding.etLoginEmail.error = it }
            errors["password"]?.let { binding.etLoginPassword.error = it }
            return
        }

        lifecycleScope.launch {
            try {
                binding.btnLogin.isEnabled = false

                val resident = LoginRepository.login(email, password)

                if (resident != null) {

                    if (resident.userType == UserType.OFFICIAL) {

                        LoginRepository.logout()

                        Toast.makeText(
                            requireContext(),
                            "This account is for Barangay Officials. Use Official Login.",
                            Toast.LENGTH_LONG
                        ).show()

                    } else {

                        Toast.makeText(
                            requireContext(),
                            "Welcome, ${resident.firstName}!",
                            Toast.LENGTH_SHORT
                        ).show()

                        findNavController().navigate(R.id.action_login_to_home)
                    }

                } else {
                    Toast.makeText(
                        requireContext(),
                        "Invalid Email or Password",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.btnLogin.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}