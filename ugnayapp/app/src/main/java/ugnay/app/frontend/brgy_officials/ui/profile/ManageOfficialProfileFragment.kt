package ugnay.app.frontend.brgy_officials.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ugnay.app.databinding.FragmentManageOfficialProfileBinding

class ManageOfficialProfileFragment : Fragment() {

    private var _binding: FragmentManageOfficialProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageOfficialProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvManageProfileTitle.text = "My Official Profile"

        binding.tvManageProfileSubtitle.text =
            "View and update your personal and official information here."
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}