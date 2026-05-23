package ugnay.app.frontend.brgy_officials.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ugnay.app.R
import ugnay.app.backend.residents.SupabaseConfig
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
        binding.tvLatestNews.text = "Loading..."
        binding.tvOfficialName.text = "Loading..."

        loadDashboardData()

        binding.cardAnnouncements.setOnClickListener {
            findNavController().navigate(R.id.nav_official_news)
        }
    }

    private fun loadDashboardData() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Run all queries in parallel for faster loading
            val residentsDeferred = async { fetchResidentCount() }
            val requestsDeferred = async { fetchPendingServicesCount() }
            val newsDeferred = async { fetchLatestNews() }
            val nameDeferred = async { fetchOfficialName() }

            // Await results and update UI
            binding.tvStatResidents.text = residentsDeferred.await().toString()
            binding.tvStatServices.text = requestsDeferred.await().toString()
            binding.tvLatestNews.text = newsDeferred.await() ?: "No recent announcements."
            binding.tvOfficialName.text = nameDeferred.await()
        }
    }

    private suspend fun fetchOfficialName(): String = withContext(Dispatchers.IO) {
        try {
            val userId = SupabaseConfig.client.auth.currentUserOrNull()?.id ?: return@withContext "Official"
            val response = SupabaseConfig.client.postgrest.from("users")
                .select { filter { eq("id", userId) }; single() }
                .decodeSingle<Map<String, Any>>()

            response["full_name"]?.toString() ?: "Official"
        } catch (e: Exception) {
            "Official"
        }
    }

    private suspend fun fetchLatestNews(): String? = withContext(Dispatchers.IO) {
        try {
            val response = SupabaseConfig.client.postgrest.from("news")
                .select {
                    limit(1)
                    order("created_at", order = Order.DESCENDING)
                }
                .decodeSingleOrNull<Map<String, Any>>()

            response?.get("title")?.toString()
        } catch (e: Exception) {
            null
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