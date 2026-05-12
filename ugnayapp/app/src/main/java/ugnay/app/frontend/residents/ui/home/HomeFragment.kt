package ugnay.app.frontend.residents.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        displayUserInfo()
    }

    private fun displayUserInfo() {
        val user = LoginRepository.getCurrentUser()
        if (user != null) {
            binding.tvFirstName.text = "${user.firstName} ${user.lastName}"
            binding.tvGreeting.text = "Good day,"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
