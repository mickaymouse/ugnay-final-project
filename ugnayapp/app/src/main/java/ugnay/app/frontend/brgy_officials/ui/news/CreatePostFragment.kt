package ugnay.app.frontend.brgy_officials.ui.news

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ugnay.app.backend.residents.news.NewsRepository
import ugnay.app.databinding.FragmentCreatePostBinding

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivPostImagePreview.setImageURI(it)
            binding.ivPostImagePreview.visibility = View.VISIBLE
        }
    }

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

        val priorityOptions = listOf("Normal", "High", "Critical")
        binding.spPriority.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            priorityOptions
        )

        binding.btnSelectPostImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

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
        val priority = binding.spPriority.selectedItem?.toString() ?: "Normal"

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

        lifecycleScope.launch {
            try {
                val announcementId = java.util.UUID.randomUUID().toString()
                val uploadedImageUrl = selectedImageUri?.let { uri ->
                    requireContext().contentResolver.openInputStream(uri)?.readBytes()?.let { bytes ->
                        NewsRepository.uploadAnnouncementImage(announcementId, bytes)
                    }
                }

                NewsRepository.createAnnouncement(
                    title = title,
                    content = content,
                    priority = priority,
                    imageUrl = uploadedImageUrl,
                    announcementId = announcementId
                )

                Toast.makeText(requireContext(), "Announcement published successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (exception: Exception) {
                Toast.makeText(requireContext(), "Unable to publish announcement: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}