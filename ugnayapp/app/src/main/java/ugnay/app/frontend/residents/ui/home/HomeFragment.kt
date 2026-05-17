package ugnay.app.frontend.residents.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.databinding.FragmentResidentsHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentResidentsHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentsHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        val user = LoginRepository.getCurrentUser()

        if (user != null) {

            val fullName = "${user.firstName} ${user.lastName}"

            binding.tvFirstName.text = fullName
            binding.tvGreeting.text = getGreetingMessage()
        } else {

            // fallback UI (important for safety)
            binding.tvFirstName.text = "Guest"
            binding.tvGreeting.text = "Welcome,"
        }
    }

    private fun getGreetingMessage(): String {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 0..11 -> "Good morning,"
            in 12..17 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}