package ugnay.app.frontend.brgy_officials.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.news.NewsRepository
import ugnay.app.backend.residents.data.News
import ugnay.app.databinding.FragmentOfficialNewsBinding
import ugnay.app.R

class OfficialNewsFragment : Fragment() {

    private var _binding: FragmentOfficialNewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var newsAdapter: OfficialNewsAdapter

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

        binding.tvNewsTitle.text = "Official News"

        setupRecyclerView()

        binding.btnCreatePost.setOnClickListener {
            findNavController().navigate(
                R.id.action_official_news_to_create_post
            )
        }

        loadNews()
        setupRealtimeListener()
//        loadOfficialHeader()
    }

//    private fun loadOfficialHeader() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            val currentUser = ugnay.app.backend.residents.login.LoginRepository.getCurrentUser()
//            if (currentUser != null && !currentUser.profilePictureUrl.isNullOrBlank()) {
//                binding.ivOfficialProfilePicNews.load(currentUser.profilePictureUrl) {
//                    crossfade(true)
//                    placeholder(R.drawable.ic_person)
//                    error(R.drawable.ic_person)
//                    listener(onSuccess = { _, _ ->
//                        binding.ivOfficialProfilePicNews.imageTintList = null
//                    })
//                }
//            }
//        }
//    }

    private fun setupRecyclerView() {
        newsAdapter = OfficialNewsAdapter(
            emptyList(),
            onEditClick = { news ->
                navigateToEditPost(news)
            },
            onDeleteClick = { news ->
                showDeleteConfirmation(news)
            }
        )
        binding.rvNewsFeed.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = newsAdapter
        }
    }

    private fun loadNews() {
        lifecycleScope.launch {
            try {
                val news = NewsRepository.fetchNews()
                newsAdapter.updateData(news)
            } catch (exception: Exception) {
                Toast.makeText(requireContext(), "Error loading announcements: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupRealtimeListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            val channel = SupabaseConfig.client.channel("announcements_channel_officials")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "announcements"
            }

            channel.subscribe()

            changeFlow.collect {
                // When any change happens in the announcements table, reload the news
                loadNews()
            }
        }
    }

    private fun navigateToEditPost(news: News) {
        val bundle = Bundle().apply {
            putSerializable("news", news)
        }
        findNavController().navigate(R.id.action_official_news_to_edit_post, bundle)
    }

    private fun showDeleteConfirmation(news: News) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Announcement")
            .setMessage("Are you sure you want to delete this announcement? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deletePost(news)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deletePost(news: News) {
        lifecycleScope.launch {
            try {
                NewsRepository.deleteAnnouncement(news.announcementId ?: return@launch)
                Toast.makeText(requireContext(), "Announcement deleted successfully!", Toast.LENGTH_SHORT).show()
                loadNews()
            } catch (exception: Exception) {
                Toast.makeText(requireContext(), "Unable to delete announcement: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}