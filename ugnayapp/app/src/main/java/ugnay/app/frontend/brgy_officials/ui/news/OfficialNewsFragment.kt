package ugnay.app.frontend.brgy_officials.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ugnay.app.R
import ugnay.app.databinding.FragmentOfficialNewsBinding

class OfficialNewsFragment : Fragment() {

    private var _binding: FragmentOfficialNewsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfficialNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNewsTitle.text = "Official News & Announcements"

        binding.btnCreatePost.setOnClickListener {
            findNavController().navigate(
                R.id.action_official_news_to_create_post
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}