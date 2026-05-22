package ugnay.app.frontend.brgy_officials.ui.requests

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.collect
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.Request
import ugnay.app.backend.residents.data.RequestStatus
import ugnay.app.backend.residents.data.UserRepository
import ugnay.app.backend.residents.request.RequestRepository
import ugnay.app.databinding.FragmentOfficialRequestsBinding
import ugnay.app.databinding.DialogResidentProfileBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OfficialRequestsFragment : Fragment() {

    private var _binding: FragmentOfficialRequestsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: OfficialRequestAdapter

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
        loadRequests()
        setupRealtimeListener()
    }

    private fun setupRealtimeListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            val channel = SupabaseConfig.client.channel("requests_channel")
            val changeFlow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "requests"
            }

            channel.subscribe()

            changeFlow.collect {
                // When any change happens in the requests table, reload the list
                loadRequests()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = OfficialRequestAdapter(
            requests = emptyList(),
            onViewProfile = { request ->
                showResidentProfile(request)
            },
            onStatusUpdate = { request, newStatus ->
                updateRequestStatus(request.requestId ?: return@OfficialRequestAdapter, newStatus)
            }
        )
        binding.rvOfficialRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.rvOfficialRequests.adapter = adapter
    }

    private fun showResidentProfile(request: Request) {
        val userId = request.userId
        if (userId == null) {
            Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
            return
        }

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
                        // Request Info
                        tvRequestFullName.text = request.fullName
                        tvRequestTypeInfo.text = request.type
                        tvRequestPurposeInfo.text = request.purpose
                        tvRequestStartDate.text = request.startDate ?: "N/A"

                        // Resident Profile Info
                        tvProfileName.text = "${user.firstName} ${user.lastName}"
                        tvProfileType.text = user.userType?.name ?: "Resident"
                        tvProfileEmail.text = user.emailAddress ?: "No email"
                        tvProfileContact.text = user.contactNumber ?: "No contact"
                        
                        val addressText = if (address != null) {
                            "${address.purok}, ${address.barangay}, ${address.municipality}"
                        } else {
                            "No address provided"
                        }
                        tvProfileAddress.text = addressText

                        btnUpdateStatusDialog.setOnClickListener {
                            showStatusPopup(it, request, dialog)
                        }

                        btnClose.setOnClickListener { dialog.dismiss() }
                    }

                    dialog.show()
                    
                    dialog.window?.setLayout(
                        (resources.displayMetrics.widthPixels * 0.9).toInt(),
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                } else {
                    Toast.makeText(requireContext(), "Resident data not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showStatusPopup(view: View, request: Request, dialog: Dialog) {
        val popup = PopupMenu(requireContext(), view)
        RequestStatus.entries.forEach { status ->
            popup.menu.add(status.displayName)
        }

        popup.setOnMenuItemClickListener { item ->
            val selectedStatus = RequestStatus.fromString(item.title.toString())
            updateRequestStatus(request.requestId ?: return@setOnMenuItemClickListener false, selectedStatus)
            dialog.dismiss()
            true
        }
        popup.show()
    }

    private fun loadRequests() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // First, auto-expire old requests in the database
                RequestRepository.autoExpireRequests()
                
                // Then fetch all requests
                var requests = RequestRepository.getAllRequests()
                
                // Sort by startDate descending
                requests = requests.sortedByDescending { it.startDate }

                adapter.updateRequests(requests)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading requests: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateRequestStatus(requestId: String, status: RequestStatus) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                RequestRepository.updateRequestStatus(requestId, status)
                Toast.makeText(requireContext(), "Status updated to ${status.displayName}", Toast.LENGTH_SHORT).show()
                loadRequests() // Refresh the UI automatically after database update
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error updating status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
