package ugnay.app.frontend.residents.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.news.NewsRepository
import ugnay.app.databinding.FragmentResidentsNewsBinding

class NewsFragment : Fragment() {

    private var _binding: FragmentResidentsNewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var newsAdapter: NewsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentsNewsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
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
                val allNews = NewsRepository.fetchNews().filter { it.status != "Deleted" }
                newsAdapter.updateData(allNews)

                // Check for high-priority alerts to show in the red card
                val urgentNews = allNews.firstOrNull { it.priority == "High" }
                if (urgentNews != null) {
                    binding.alertCard.visibility = View.VISIBLE
                    binding.tvAlertText.text = "ALERT: ${urgentNews.title}"
                } else {
                    binding.alertCard.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading news", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRealtimeListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            val channel = SupabaseConfig.client.channel("announcements_channel_residents")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "announcements"
            }
            channel.subscribe()
            changeFlow.collect { loadNews() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}