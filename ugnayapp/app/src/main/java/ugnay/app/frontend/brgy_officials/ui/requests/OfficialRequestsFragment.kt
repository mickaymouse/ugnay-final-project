package ugnay.app.frontend.brgy_officials.ui.requests

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayoutMediator
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.data.Address
import ugnay.app.databinding.DialogPageRequestBinding
import ugnay.app.databinding.DialogPageResidentInfoBinding
import coil.load
import coil.transform.CircleCropTransformation
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.launch
import ugnay.app.R
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.Request
import ugnay.app.backend.residents.data.RequestStatus
import ugnay.app.backend.residents.data.UserRepository
import ugnay.app.backend.residents.request.RequestRepository
import ugnay.app.databinding.FragmentOfficialRequestsBinding
import ugnay.app.databinding.DialogResidentProfileBinding

class OfficialRequestsFragment : Fragment() {

    private var _binding: FragmentOfficialRequestsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: OfficialRequestAdapter

    private var allRequestsList: List<Request> = emptyList()
    private var activeFilterStatus: String = "ALL"
    private var currentSearchQuery: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOfficialRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        loadRequests()
        setupRealtimeListener()
        loadOfficialHeader()

        // FIXED: Correctly matching the XML element ID 'etSearchRequest'
        binding.etSearchRequest.addTextChangedListener { text ->
            currentSearchQuery = text.toString()
            applyFilterAndSearch()
        }
    }

    private fun loadOfficialHeader() {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentUser = ugnay.app.backend.residents.login.LoginRepository.getCurrentUser()
            if (currentUser != null && !currentUser.profilePictureUrl.isNullOrBlank()) {
                binding.ivOfficialProfilePicRequests.load(currentUser.profilePictureUrl) {
                    crossfade(true)
                    placeholder(R.drawable.ic_person)
                    error(R.drawable.ic_person)
                    listener(onSuccess = { _, _ ->
                        binding.ivOfficialProfilePicRequests.imageTintList = null
                    })
                }
            }
        }
    }

    private fun setupFilters() {
        // Highlighting selected category helper states
        val buttons = listOf(binding.btnFilterAll, binding.btnFilterPending, binding.btnFilterApproved)

        fun updateButtonStates(selected: Button) {
            buttons.forEach { it.setBackgroundColor(Color.parseColor("#2D2D2D")) }
            selected.setBackgroundColor(Color.parseColor("#6495ED"))
        }

        updateButtonStates(binding.btnFilterAll)

        binding.btnFilterAll.setOnClickListener {
            activeFilterStatus = "ALL"
            updateButtonStates(binding.btnFilterAll)
            applyFilterAndSearch()
        }
        binding.btnFilterPending.setOnClickListener {
            activeFilterStatus = "PENDING"
            updateButtonStates(binding.btnFilterPending)
            applyFilterAndSearch()
        }
        binding.btnFilterApproved.setOnClickListener {
            activeFilterStatus = "APPROVED"
            updateButtonStates(binding.btnFilterApproved)
            applyFilterAndSearch()
        }
    }

    private fun setupRealtimeListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            val channel = SupabaseConfig.client.channel("requests_channel")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "requests"
            }
            channel.subscribe()
            changeFlow.collect {
                loadRequests()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = OfficialRequestAdapter(
            requests = emptyList(),
            onViewProfile = { request -> showResidentProfile(request) }
        )
        binding.rvOfficialRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOfficialRequests.adapter = adapter
    }

    private fun loadRequests() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE
                RequestRepository.autoExpireRequests()

                allRequestsList = RequestRepository.getAllRequests().sortedByDescending { it.startDate }
                applyFilterAndSearch()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading requests: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun applyFilterAndSearch() {
        var filtered = allRequestsList

        // 1. Status Filter Tab Rule
        if (activeFilterStatus != "ALL") {
            filtered = filtered.filter { it.status.name == activeFilterStatus }
        }

        // 2. Text Search Input Filter Rule
        if (currentSearchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                (it.fullName?.contains(currentSearchQuery, ignoreCase = true) ?: false) ||
                        (it.type?.contains(currentSearchQuery, ignoreCase = true) ?: false)
            }
        }

        adapter.updateRequests(filtered)
    }

    private fun showResidentProfile(request: Request) {
        val userId = request.userId ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = UserRepository.getUserById(userId)
                val address = UserRepository.getAddressByUserId(userId)

                if (user != null) {
                    val dialog = Dialog(requireContext())
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    val dialogBinding = DialogResidentProfileBinding.inflate(layoutInflater)
                    dialog.setContentView(dialogBinding.root)
                    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

                    dialogBinding.apply {
                        val fullName = buildString {
                            append(user.firstName)
                            if (!user.middleName.isNullOrBlank()) append(" ${user.middleName}")
                            append(" ${user.lastName}")
                            if (!user.nameSuffix.isNullOrBlank()) append(" ${user.nameSuffix}")
                        }
                        tvProfileName.text = fullName
                        tvProfileType.text = user.userType?.name ?: "Resident"

                        // Load profile picture if available
                        if (!user.profilePictureUrl.isNullOrBlank()) {
                            ivResidentPhoto.load(user.profilePictureUrl) {
                                crossfade(true)
                                transformations(CircleCropTransformation())
                                placeholder(R.drawable.ic_person)
                                error(R.drawable.ic_person)
                                listener(onSuccess = { _, _ ->
                                    ivResidentPhoto.imageTintList = null
                                })
                            }
                        } else {
                            ivResidentPhoto.setImageResource(R.drawable.ic_person)
                            ivResidentPhoto.imageTintList = android.content.res.ColorStateList.valueOf(
                                resources.getColor(R.color.brgy_blue, null)
                            )
                        }
                        
                        val pagerAdapter = ProfileViewPagerAdapter(request, user, address) {
                            showStatusPopup(it, request, dialog)
                        }
                        viewPagerProfile.adapter = pagerAdapter
                        
                        TabLayoutMediator(tabLayoutIndicator, viewPagerProfile) { _, _ -> }.attach()

                        btnClose.setOnClickListener { dialog.dismiss() }
                    }

                    dialog.show()
                    dialog.window?.setLayout((resources.displayMetrics.widthPixels * 0.9).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showStatusPopup(view: View, request: Request, dialog: Dialog) {
        val popup = PopupMenu(requireContext(), view)
        RequestStatus.entries.forEach { status -> popup.menu.add(status.displayName) }
        popup.setOnMenuItemClickListener { item ->
            val selectedStatus = RequestStatus.fromString(item.title.toString())
            updateRequestStatus(request.requestId ?: return@setOnMenuItemClickListener false, selectedStatus)
            dialog.dismiss()
            true
        }
        popup.show()
    }

    private fun updateRequestStatus(requestId: String, status: RequestStatus) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                RequestRepository.updateRequestStatus(requestId, status)
                Toast.makeText(requireContext(), "Status updated to ${status.displayName}", Toast.LENGTH_SHORT).show()
                loadRequests()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- VIEWPAGER ADAPTER FOR PROFILE DIALOG ---
    private class ProfileViewPagerAdapter(
        private val request: Request,
        private val user: User,
        private val address: Address?,
        private val onUpdateStatus: (View) -> Unit
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            private const val TYPE_REQUEST = 0
            private const val TYPE_RESIDENT = 1
        }

        override fun getItemViewType(position: Int): Int = position

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return if (viewType == TYPE_REQUEST) {
                val binding = DialogPageRequestBinding.inflate(inflater, parent, false)
                RequestViewHolder(binding)
            } else {
                val binding = DialogPageResidentInfoBinding.inflate(inflater, parent, false)
                ResidentViewHolder(binding)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is RequestViewHolder) {
                holder.binding.apply {
                    tvRequestFullName.text = request.fullName
                    tvRequestTypeInfo.text = request.type
                    tvRequestPurposeInfo.text = "Purpose: ${request.purpose}"
                    tvRequestStartDate.text = "Submitted on: ${request.startDate ?: "N/A"}"
                    btnUpdateStatusDialog.setOnClickListener { onUpdateStatus(it) }
                }
            } else if (holder is ResidentViewHolder) {
                holder.binding.apply {
                    tvProfileEmail.text = user.emailAddress ?: "No email"
                    tvProfileContact.text = user.contactNumber ?: "No contact"
                    tvProfileAddress.text = address?.let {
                        "${it.purok}, ${it.barangay}, ${it.municipality}"
                    } ?: "No address"
                    tvProfileGender.text = user.gender?.name ?: "N/A"
                    tvProfileCivilStatus.text = user.civilStatus?.name ?: "N/A"
                    tvProfileBirthdate.text = user.birthdate ?: "N/A"
                    tvProfileBirthplace.text = user.birthplace ?: "N/A"
                    tvProfileNationality.text = user.nationality ?: "Filipino"
                }
            }
        }

        override fun getItemCount(): Int = 2

        class RequestViewHolder(val binding: DialogPageRequestBinding) : RecyclerView.ViewHolder(binding.root)
        class ResidentViewHolder(val binding: DialogPageResidentInfoBinding) : RecyclerView.ViewHolder(binding.root)
    }

    // --- RECYCLERVIEW ADAPTER COMPONENT ---
    private class OfficialRequestAdapter(
        private var requests: List<Request>,
        private val onViewProfile: (Request) -> Unit
    ) : RecyclerView.Adapter<OfficialRequestAdapter.RequestViewHolder>() {

        class RequestViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvName: TextView = view.findViewById(R.id.tvItemRequestName)
            val tvType: TextView = view.findViewById(R.id.tvItemRequestType)
            val tvDate: TextView = view.findViewById(R.id.tvItemRequestDate)
            val tvStatusBadge: TextView = view.findViewById(R.id.tvItemRequestStatusBadge)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
            val mainView = LayoutInflater.from(parent.context).inflate(R.layout.fragment_official_requests, parent, false)
            val rowView = mainView.findViewById<ViewGroup>(R.id.itemRequestTemplate)
            (rowView.parent as ViewGroup).removeView(rowView)
            rowView.visibility = View.VISIBLE
            return RequestViewHolder(rowView)
        }

        override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
            val request = requests[position]
            holder.tvName.text = request.fullName ?: "Unknown Resident"
            holder.tvType.text = request.type ?: "Document Request"
            holder.tvDate.text = request.startDate ?: "Date unlisted"

            // Stylize status badging colors dynamically
            val status = request.status
            holder.tvStatusBadge.text = status.displayName

            val badgeColor = when (status.name) {
                "APPROVED" -> "#2E7D32" // Balanced Dark Green
                "REJECTED", "EXPIRED" -> "#C62828" // Dark Red
                else -> "#EF6C00" // Warm Amber Orange/Yellow for Pending
            }
            holder.tvStatusBadge.background.setTint(Color.parseColor(badgeColor))

            holder.itemView.setOnClickListener { onViewProfile(request) }
        }

        override fun getItemCount(): Int = requests.size

        fun updateRequests(newRequests: List<Request>) {
            this.requests = newRequests
            notifyDataSetChanged()
        }
    }
}