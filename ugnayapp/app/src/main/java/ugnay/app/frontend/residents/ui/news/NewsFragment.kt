package ugnay.app.frontend.residents.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
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
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading news: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
