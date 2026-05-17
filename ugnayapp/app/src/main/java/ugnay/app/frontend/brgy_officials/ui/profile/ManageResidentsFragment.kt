package ugnay.app.frontend.brgy_officials.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ugnay.app.databinding.FragmentManageResidentsBinding

class ManageResidentsFragment : Fragment() {

    private var _binding: FragmentManageResidentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageResidentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvManageResidentsTitle.text = "Manage Residents"
        binding.tvManageResidentsSubtitle.text =
            "View, search, and manage registered residents."

        binding.btnAddResident.setOnClickListener {
            // placeholder for now
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}