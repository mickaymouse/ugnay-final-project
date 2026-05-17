package ugnay.app.frontend.brgy_officials.ui.requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ugnay.app.databinding.FragmentOfficialRequestsBinding

class OfficialRequestsFragment : Fragment() {

    private var _binding: FragmentOfficialRequestsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOfficialRequestsBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvOfficialRequestsTitle.text =
            "Resident Requests"

        binding.tvOfficialRequestsSubtitle.text =
            "Manage resident concerns, documents, and approvals here."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}