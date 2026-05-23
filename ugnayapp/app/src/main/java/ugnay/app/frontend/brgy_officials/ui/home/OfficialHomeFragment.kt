package ugnay.app.frontend.brgy_officials.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ugnay.app.R
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.News
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.backend.residents.news.NewsRepository
import ugnay.app.databinding.FragmentOfficialHomeBinding

class OfficialHomeFragment : Fragment() {
    private var _binding: FragmentOfficialHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfficialHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set UI to loading
        binding.tvStatResidents.text = "0"
        binding.tvStatServices.text = "0"
        binding.tvOfficialName.text = "Loading..."
        binding.tvOfficialEmail.text = "Loading..."
        binding.tvOfficialContact.text = "Loading..."

        loadDashboardData()

        binding.tvSeeMoreNews.setOnClickListener {
            findNavController().navigate(R.id.nav_official_news)
        }
    }

    private fun loadDashboardData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Run all queries in parallel for faster loading
            val residentsDeferred = async { fetchResidentCount() }
            val requestsDeferred = async { fetchPendingServicesCount() }
            val newsDeferred = async { fetchLatestNewsList() }
            val currentUser = LoginRepository.getCurrentUser()

            // Await results and update UI
            binding.tvStatResidents.text = residentsDeferred.await().toString()
            binding.tvStatServices.text = requestsDeferred.await().toString()
            
            val latestNews = newsDeferred.await()
            updateNewsUI(latestNews)

            if (currentUser != null) {
                binding.tvOfficialName.text = "${currentUser.firstName} ${currentUser.lastName}"
                binding.tvOfficialEmail.text = currentUser.emailAddress ?: "No email"
                binding.tvOfficialContact.text = currentUser.contactNumber ?: "No contact"

                if (!currentUser.profilePictureUrl.isNullOrBlank()) {
                    binding.ivOfficialProfilePic.load(currentUser.profilePictureUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_person)
                        error(R.drawable.ic_person)
                        listener(onSuccess = { _, _ ->
                            binding.ivOfficialProfilePic.imageTintList = null
                        })
                    }
                }
            }
        }
    }

    private fun updateNewsUI(newsList: List<News>) {
        binding.llNewsContainer.removeAllViews()
        if (newsList.isEmpty()) {
            binding.tvNewsPlaceholder.visibility = View.VISIBLE
            binding.tvNewsPlaceholder.text = "No recent announcements."
        } else {
            binding.tvNewsPlaceholder.visibility = View.GONE
            newsList.forEach { news ->
                val newsView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_news, binding.llNewsContainer, false)
                
                newsView.findViewById<TextView>(R.id.tv_news_item_title).text = news.title
                newsView.findViewById<TextView>(R.id.tv_news_item_content).text = news.content
                newsView.findViewById<TextView>(R.id.tv_news_item_duration).text = news.relativeDuration() ?: "Recent"
                
                val imageView = newsView.findViewById<ImageView>(R.id.iv_news_item_image)
                val imageContainer = newsView.findViewById<View>(R.id.cv_news_image_container)
                
                if (!news.imageUrl.isNullOrBlank()) {
                    imageContainer.visibility = View.VISIBLE
                    imageView.load(news.imageUrl) {
                        crossfade(true)
                    }
                } else {
                    imageContainer.visibility = View.GONE
                }

                newsView.setOnClickListener {
                    findNavController().navigate(R.id.nav_official_news)
                }
                
                binding.llNewsContainer.addView(newsView)
            }
        }
    }

    private suspend fun fetchLatestNewsList(): List<News> = withContext(Dispatchers.IO) {
        try {
            NewsRepository.fetchNews().take(3)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchResidentCount(): Long = withContext(Dispatchers.IO) {
        try {
            SupabaseConfig.client.postgrest.from("users")
                .select { filter { eq("user_type", "Resident") }; count(Count.EXACT); limit(0) }
                .countOrNull() ?: 0L
        } catch (e: Exception) { 0L }
    }

    private suspend fun fetchPendingServicesCount(): Long = withContext(Dispatchers.IO) {
        try {
            SupabaseConfig.client.postgrest.from("requests")
                .select { filter { eq("status", "Pending") }; count(Count.EXACT); limit(0) }
                .countOrNull() ?: 0L
        } catch (e: Exception) { 0L }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
