package ugnay.app.frontend.brgy_officials.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ugnay.app.R
import ugnay.app.databinding.FragmentOfficialHomeBinding

class OfficialHomeFragment : Fragment(R.layout.fragment_official_home) {

    private var _binding: FragmentOfficialHomeBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentOfficialHomeBinding.bind(view)

        setupClicks()
    }

    private fun setupClicks() {

        binding.cardRequests.setOnClickListener {
            Toast.makeText(requireContext(), "Requests clicked", Toast.LENGTH_SHORT).show()
            // findNavController().navigate(R.id.action_home_to_requests)
        }

        binding.cardAnnouncements.setOnClickListener {
            Toast.makeText(requireContext(), "Announcements clicked", Toast.LENGTH_SHORT).show()
        }

        binding.cardOfficials.setOnClickListener {
            Toast.makeText(requireContext(), "Officials clicked", Toast.LENGTH_SHORT).show()
        }

        binding.cardContact.setOnClickListener {
            Toast.makeText(requireContext(), "Contact clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}