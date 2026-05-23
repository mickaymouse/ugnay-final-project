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
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.news.NewsRepository
import ugnay.app.databinding.FragmentOfficialNewsBinding
import ugnay.app.frontend.residents.ui.news.NewsAdapter
import ugnay.app.R

class OfficialNewsFragment : Fragment() {

    private var _binding: FragmentOfficialNewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var newsAdapter: NewsAdapter

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

        setupRecyclerView()

        binding.btnCreatePost.setOnClickListener {
            findNavController().navigate(
                R.id.action_official_news_to_create_post
            )
        }

        loadNews()
        setupRealtimeListener()
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(emptyList())
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}