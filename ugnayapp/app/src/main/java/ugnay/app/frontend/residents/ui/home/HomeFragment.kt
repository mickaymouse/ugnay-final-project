package ugnay.app.frontend.residents.ui.home

import android.content.res.ColorStateList
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import coil.transform.CircleCropTransformation
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
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
        loadLatestAnnouncements()
        setupRealtimeListener()
        setupAnnouncementRealtimeListener()

        binding.tvSeeMoreAnnouncements.setOnClickListener {
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
            changeFlow.collect { loadLatestAnnouncements() }
        }
    }

    private fun setupUI() {
        val user = LoginRepository.getCurrentUser()
        if (user != null) {
            binding.tvFirstName.text = getString(R.string.resident_full_name, user.firstName, user.lastName).trim()
            binding.tvGreeting.text = getGreetingMessage()

            // Display Contact Information
            binding.tvResidentPhone.text = user.contactNumber ?: "+63 --- --- ----"
            binding.tvResidentEmail.text = user.emailAddress ?: "---@guintas.ph"

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
            } catch (_: Exception) {
                binding.llStatusContainer.visibility = View.GONE
            }
        }
    }

    private fun loadLatestAnnouncements() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val newsList = NewsRepository.fetchNews().take(3)
                binding.llAnnouncementsContainer.removeAllViews()

                if (newsList.isEmpty()) {
                    binding.tvAnnouncementPlaceholder.visibility = View.VISIBLE
                    binding.tvAnnouncementPlaceholder.text = getString(R.string.no_recent_announcements)
                } else {
                    binding.tvAnnouncementPlaceholder.visibility = View.GONE
                    newsList.forEach { news ->
                        val newsView = LayoutInflater.from(requireContext())
                            .inflate(R.layout.item_news, binding.llAnnouncementsContainer, false)
                        
                        val tvTitle = newsView.findViewById<TextView>(R.id.tv_news_item_title)
                        val tvContent = newsView.findViewById<TextView>(R.id.tv_news_item_content)
                        val tvDuration = newsView.findViewById<TextView>(R.id.tv_news_item_duration)
                        val ivAuthor = newsView.findViewById<ImageView>(R.id.iv_news_author_image)
                        val tvAuthorName = newsView.findViewById<TextView>(R.id.tv_news_author_name)
                        val tvAuthorPos = newsView.findViewById<TextView>(R.id.tv_news_author_position)
                        
                        tvTitle.text = news.title
                        tvContent.text = news.content
                        tvDuration.text = news.relativeDuration() ?: "Recent"

                        // Fetch Author Info
                        if (!news.userId.isNullOrEmpty()) {
                            viewLifecycleOwner.lifecycleScope.launch {
                                val user = ugnay.app.backend.residents.data.UserRepository.getUserById(news.userId)
                                if (user != null) {
                                    tvAuthorName.text = getString(R.string.resident_full_name, user.firstName, user.lastName)
                                    tvAuthorPos.text = if (user.userType == ugnay.app.backend.residents.data.UserType.BARANGAY_OFFICIAL) "Barangay Official" else "Resident"
                                    
                                    if (!user.profilePictureUrl.isNullOrEmpty()) {
                                        ivAuthor.load(user.profilePictureUrl) {
                                            crossfade(true)
                                            transformations(CircleCropTransformation())
                                            placeholder(R.drawable.ic_person)
                                            error(R.drawable.ic_person)
                                            listener(onSuccess = { _, _ ->
                                                ivAuthor.imageTintList = null
                                            })
                                        }
                                    } else {
                                        ivAuthor.setImageResource(R.drawable.ic_person)
                                        ivAuthor.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.brgy_blue))
                                    }
                                }
                            }
                        }
                        
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
                            findNavController().navigate(R.id.nav_news)
                        }
                        
                        binding.llAnnouncementsContainer.addView(newsView)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvAnnouncementPlaceholder.text = getString(R.string.error_loading_announcements)
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
            tvStatusLabel.text = getString(R.string.status_label, statusText.uppercase(Locale.ROOT))

            // Set Color logic remains the same
            btnDownloadFile.visibility = if (statusText.equals("Approved", true) ||
                statusText.equals("Done", true)) View.VISIBLE else View.GONE
        }
    }

    private fun getGreetingMessage(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> getString(R.string.greeting_morning)
        in 12..17 -> getString(R.string.greeting_afternoon)
        else -> getString(R.string.greeting_evening)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}