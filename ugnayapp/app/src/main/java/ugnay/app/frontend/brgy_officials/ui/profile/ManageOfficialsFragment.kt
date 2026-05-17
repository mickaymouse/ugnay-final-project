package ugnay.app.frontend.brgy_officials.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ugnay.app.databinding.FragmentManageOfficialsBinding

class ManageOfficialsFragment : Fragment() {

    private var _binding: FragmentManageOfficialsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageOfficialsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvManageOfficialsTitle.text = "Manage Barangay Officials"

        binding.tvManageOfficialsSubtitle.text =
            "View, add, edit, or remove barangay officials."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}