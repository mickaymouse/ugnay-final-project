package ugnay.app.frontend.residents.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ugnay.app.R
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.Request
import ugnay.app.backend.residents.login.LoginRepository
import ugnay.app.backend.residents.news.NewsRepository
import ugnay.app.backend.residents.request.RequestRepository
import ugnay.app.databinding.FragmentResidentsHomeBinding
import ugnay.app.databinding.ItemStatusCardBinding
import java.util.Calendar
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentResidentsHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentsHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        loadRequestStatus()
        loadLatestAnnouncement()
        setupRealtimeListener()
        setupAnnouncementRealtimeListener()
        
        binding.cvLatestAnnouncement.setOnClickListener {
            findNavController().navigate(R.id.nav_news)
        }
    }

    private fun setupRealtimeListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            val channel = SupabaseConfig.client.channel("resident_requests_channel")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "requests"
            }

            channel.subscribe()

            changeFlow.collect {
                // When any change happens in the requests table, reload the status
                loadRequestStatus()
            }
        }
    }

    private fun setupAnnouncementRealtimeListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            val channel = SupabaseConfig.client.channel("announcements_channel")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "announcements"
            }

            channel.subscribe()

            changeFlow.collect {
                // When any change happens in the announcements table, reload the latest announcement
                loadLatestAnnouncement()
            }
        }
    }

    private fun setupUI() {
        val user = LoginRepository.getCurrentUser()

        if (user != null) {
            val fullName = "${user.firstName} ${user.lastName}"

            binding.tvFirstName.text = fullName
            binding.tvGreeting.text = getGreetingMessage()
        } else {
            binding.tvFirstName.text = "Guest"
            binding.tvGreeting.text = "Welcome,"
        }
    }

    private fun loadRequestStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val requests = RequestRepository.getResidentRequests()

                // Get latest request
                val latestRequest = requests
                    .sortedByDescending { it.startDate }
                    .firstOrNull()

                if (latestRequest != null) {
                    showStatusCard(latestRequest)
                } else {
                    binding.llStatusContainer.visibility = View.GONE
                }

            } catch (e: Exception) {
                e.printStackTrace()
                binding.llStatusContainer.visibility = View.GONE
            }
        }
    }

    private fun loadLatestAnnouncement() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val latestAnnouncement = NewsRepository.fetchLatestAnnouncement()
                
                if (latestAnnouncement != null) {
                    binding.tvLatestAnnouncementTitle.text = latestAnnouncement.title
                    binding.tvLatestAnnouncementContent.text = latestAnnouncement.content
                    
                    // Load image if available
                    if (!latestAnnouncement.imageUrl.isNullOrBlank()) {
                        binding.ivLatestAnnouncementImage.visibility = View.VISIBLE
                        binding.ivLatestAnnouncementImage.load(latestAnnouncement.imageUrl) {
                            crossfade(true)
                            placeholder(R.drawable.ic_launcher_foreground)
                            error(R.drawable.ic_launcher_foreground)
                        }
                    } else {
                        binding.ivLatestAnnouncementImage.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showStatusCard(request: Request) {

        binding.llStatusContainer.visibility = View.VISIBLE
        binding.flStatusCardHolder.removeAllViews()

        val cardBinding = ItemStatusCardBinding.inflate(
            layoutInflater,
            binding.flStatusCardHolder,
            true
        )

        cardBinding.root.setOnClickListener {
            // Navigate to RequestFragment where history is now displayed
            findNavController().navigate(R.id.nav_request)
        }

        cardBinding.apply {

            tvStatusDocName.text = request.type

            val statusText = request.status.displayName

            tvStatusLabel.text =
                "STATUS: ${statusText.uppercase(Locale.ROOT)}"

            // Status Colors
            val statusColor = when (statusText.lowercase(Locale.ROOT)) {
                "pending" -> R.color.brgy_yellow
                "approved" -> R.color.brgy_blue
                "rejected" -> R.color.brgy_red
                "done" -> R.color.brgy_green
                "expired" -> R.color.text_secondary
                else -> R.color.text_white
            }

            tvStatusLabel.setTextColor(
                ContextCompat.getColor(requireContext(), statusColor)
            )

            // Show download button only for Approved or Done
            btnDownloadFile.visibility =
                if (
                    statusText.equals("Approved", ignoreCase = true) ||
                    statusText.equals("Done", ignoreCase = true)
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }
    }

    private fun getGreetingMessage(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 0..11 -> "Good morning,"
            in 12..17 -> "Good afternoon,"
            else -> "Good evening,"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
