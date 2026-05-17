package ugnay.app.frontend.brgy_officials.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ugnay.app.databinding.FragmentCreatePostBinding

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSubmitPost.setOnClickListener {
            submitPost()
        }

        binding.btnCancelPost.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun submitPost() {
        val title = binding.etPostTitle.text.toString().trim()
        val content = binding.etPostContent.text.toString().trim()

        // Reset error states
        binding.tilPostTitle.error = null
        binding.tilPostContent.error = null

        var hasError = false

        if (title.isEmpty()) {
            binding.tilPostTitle.error = "Title is required"
            hasError = true
        }

        if (content.isEmpty()) {
            binding.tilPostContent.error = "Content is required"
            hasError = true
        }

        if (hasError) return

        // TODO: Save to DB / API later
        Toast.makeText(requireContext(), "Post created successfully!", Toast.LENGTH_SHORT).show()

        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}