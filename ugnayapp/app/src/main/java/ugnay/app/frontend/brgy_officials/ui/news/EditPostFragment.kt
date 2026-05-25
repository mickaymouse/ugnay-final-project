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
import coil.load
import kotlinx.coroutines.launch
import ugnay.app.backend.residents.data.News
import ugnay.app.backend.residents.news.NewsRepository
import ugnay.app.databinding.FragmentEditPostBinding

class EditPostFragment : Fragment() {

    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private var currentNews: News? = null
    private var removeCurrentImage: Boolean = false

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivPostImagePreview.load(it)
            binding.ivPostImagePreview.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.VISIBLE
            removeCurrentImage = false // User selected new image, so don't remove it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get the news item from arguments
        currentNews = arguments?.getSerializable("news") as? News
        
        if (currentNews == null) {
            Toast.makeText(requireContext(), "Error: Could not load announcement", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        val priorityOptions = listOf("Normal", "High", "Critical")
        binding.spPriority.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            priorityOptions
        )

        // Pre-fill with current data
        binding.etPostTitle.setText(currentNews?.title ?: "")
        binding.etPostContent.setText(currentNews?.content ?: "")
        
        val priorityIndex = priorityOptions.indexOf(currentNews?.priority ?: "Normal")
        if (priorityIndex >= 0) {
            binding.spPriority.setSelection(priorityIndex)
        }

        // Load current image if it exists
        if (!currentNews?.imageUrl.isNullOrBlank()) {
            binding.ivPostImagePreview.load(currentNews?.imageUrl) {
                crossfade(true)
            }
            binding.ivPostImagePreview.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.VISIBLE
        }

        binding.btnSelectPostImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnRemoveImage.setOnClickListener {
            removeCurrentImage = true
            binding.ivPostImagePreview.visibility = View.GONE
            binding.btnRemoveImage.visibility = View.GONE
            selectedImageUri = null
        }

        binding.btnSubmitPost.setOnClickListener {
            updatePost()
        }

        binding.btnCancelPost.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun updatePost() {
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
                val announcementId = currentNews?.announcementId ?: return@launch
                
                // Handle image upload if a new image was selected
                var uploadedImageUrl = currentNews?.imageUrl
                
                if (selectedImageUri != null) {
                    uploadedImageUrl = requireContext().contentResolver.openInputStream(selectedImageUri!!)?.readBytes()?.let { bytes ->
                        NewsRepository.uploadAnnouncementImage(announcementId, bytes)
                    }
                } else if (removeCurrentImage) {
                    uploadedImageUrl = null
                }

                NewsRepository.updateAnnouncement(
                    announcementId = announcementId,
                    title = title,
                    content = content,
                    priority = priority,
                    imageUrl = uploadedImageUrl
                )

                Toast.makeText(requireContext(), "Announcement updated successfully!", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } catch (exception: Exception) {
                Toast.makeText(requireContext(), "Unable to update announcement: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
