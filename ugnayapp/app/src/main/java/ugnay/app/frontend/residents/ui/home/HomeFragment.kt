package ugnay.app.frontend.residents.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
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
import ugnay.app.frontend.residents.ui.request.RequestFormActivity
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
        setupServiceCardClicks()
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
            changeFlow.collect { loadRequestStatus() }
        }
    }

    private fun setupAnnouncementRealtimeListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            val channel = SupabaseConfig.client.channel("announcements_channel")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "announcements"
            }
            channel.subscribe()
            changeFlow.collect { loadLatestAnnouncement() }
        }
    }

    private fun setupUI() {
        val user = LoginRepository.getCurrentUser()
        if (user != null) {
            binding.tvFirstName.text = "${user.firstName} ${user.lastName}".trim()
            binding.tvGreeting.text = getGreetingMessage()
            if (!user.profilePictureUrl.isNullOrBlank()) {
                binding.ivResidentProfile.apply {
                    imageTintList = null
                    load(user.profilePictureUrl) {
                        crossfade(true)
                        transformations(CircleCropTransformation())
                    }
                }
            } else {
                setDefaultProfileImage()
            }
        }
    }

    private fun setDefaultProfileImage() {
        binding.ivResidentProfile.apply {
            setImageResource(R.drawable.ic_person)
            imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.brgy_blue)
        }
    }

    private fun setupServiceCardClicks() {
        binding.cardHomeIndigency.setOnClickListener { openForm("Certificate of Indigency") }
        binding.cardHomeResidency.setOnClickListener { openForm("Certificate of Residency") }
        binding.cardHomeBrgyId.setOnClickListener { openForm("Barangay ID") }
        binding.cardHomeClearance.setOnClickListener { openForm("Barangay Clearance") }
        binding.cardHomeOthers.setOnClickListener { openForm("Others") }
    }

    private fun openForm(type: String) {
        val intent = Intent(requireContext(), RequestFormActivity::class.java)
        intent.putExtra("REQUEST_TYPE", type)
        startActivity(intent)
    }

    private fun loadRequestStatus() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val latestRequest = RequestRepository.getResidentRequests()
                    .sortedByDescending { it.startDate }
                    .firstOrNull()

                if (latestRequest != null) showStatusCard(latestRequest)
                else binding.llStatusContainer.visibility = View.GONE
            } catch (e: Exception) {
                binding.llStatusContainer.visibility = View.GONE
            }
        }
    }

    private fun loadLatestAnnouncement() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val news = NewsRepository.fetchLatestAnnouncement()
                if (news != null) {
                    // Update Title with Priority if it exists
                    val priorityPrefix = if (!news.priority.isNullOrBlank() && news.priority != "Normal")
                        "[${news.priority.uppercase()}] " else ""

                    binding.tvLatestAnnouncementTitle.text = priorityPrefix + news.title
                    binding.tvLatestAnnouncementContent.text = news.content

                    // Display relative time using your existing model function
                    binding.tvAnnouncementTime.text = news.relativeDuration() ?: "Just now"

                    // Handle Image
                    if (!news.imageUrl.isNullOrBlank()) {
                        binding.cvAnnouncementImageContainer.visibility = View.VISIBLE
                        binding.ivLatestAnnouncementImage.load(news.imageUrl)
                    } else {
                        binding.cvAnnouncementImageContainer.visibility = View.GONE
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
        val cardBinding = ItemStatusCardBinding.inflate(layoutInflater, binding.flStatusCardHolder, true)

        cardBinding.apply {
            tvStatusDocName.text = request.type
            val statusText = request.status.displayName
            tvStatusLabel.text = "STATUS: ${statusText.uppercase(Locale.ROOT)}"

            // Set Color logic remains the same
            btnDownloadFile.visibility = if (statusText.equals("Approved", true) ||
                statusText.equals("Done", true)) View.VISIBLE else View.GONE
        }
    }

    private fun getGreetingMessage(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good morning,"
        in 12..17 -> "Good afternoon,"
        else -> "Good evening,"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}