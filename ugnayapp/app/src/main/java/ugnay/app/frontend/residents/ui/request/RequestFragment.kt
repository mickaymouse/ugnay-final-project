package ugnay.app.frontend.residents.ui.request

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.Request
import ugnay.app.backend.residents.data.RequestStatus
import ugnay.app.backend.residents.request.RequestRepository
import ugnay.app.databinding.FragmentResidentsRequestBinding
import ugnay.app.frontend.residents.ui.home.ResidentRequestAdapter

class RequestFragment : Fragment() {

    private var _binding: FragmentResidentsRequestBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: ResidentRequestAdapter
    private var allRequests: List<Request> = emptyList()
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResidentsRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilters()
        setupSearch()
        loadRequests()
        setupRealtimeListener()
        setupCardClicks()
        addCardAnimations()
    }

    private fun setupRealtimeListener() {
        viewLifecycleOwner.lifecycleScope.launch {
            val channel = SupabaseConfig.client.channel("resident_requests_history_channel")
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
        adapter = ResidentRequestAdapter(emptyList())
        binding.rvRequestHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvRequestHistory.adapter = adapter
    }

    private fun setupFilters() {
        val statuses = listOf("All Status") + RequestStatus.entries.map { it.displayName }
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, statuses)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatusFilter.adapter = spinnerAdapter

        binding.spinnerStatusFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterRequests()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSearch() {
        binding.etSearchHistory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // Debounce search
                    filterRequests()
                }
            }
        })
    }

    private fun loadRequests() {
        lifecycleScope.launch {
            try {
                allRequests = RequestRepository.getResidentRequests()
                filterRequests()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun filterRequests() {
        val query = binding.etSearchHistory.text.toString().trim().lowercase()
        val selectedStatus = binding.spinnerStatusFilter.selectedItem?.toString() ?: "All Status"

        val filteredList = allRequests.filter { request ->
            val matchesSearch = request.type?.lowercase()?.contains(query) == true || 
                               request.purpose?.lowercase()?.contains(query) == true
            
            val matchesStatus = selectedStatus == "All Status" || request.status.displayName == selectedStatus
            
            matchesSearch && matchesStatus
        }.sortedByDescending { it.startDate }

        adapter.updateRequests(filteredList)
        binding.tvEmptyHistory.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openForm(type: String) {
        val intent = Intent(requireContext(), RequestFormActivity::class.java)
        intent.putExtra("REQUEST_TYPE", type)
        startActivity(intent)
    }

    private fun setupCardClicks() {
        val grid = binding.glRequests
        
        for (i in 0 until grid.childCount) {
            val child = grid.getChildAt(i)
            if (child is CardView) {
                child.setOnClickListener {
                    val type = when (i) {
                        0 -> "Certificate of Indigency"
                        1 -> "Certificate of Residency"
                        2 -> "Barangay ID"
                        3 -> "Barangay Clearance"
                        else -> "Others"
                    }
                    openForm(type)
                }
            }
        }
    }

    private fun addCardAnimations() {
        val grid = binding.glRequests
        for (i in 0 until grid.childCount) {
            val child = grid.getChildAt(i)
            if (child is CardView) {
                child.alpha = 0f
                child.translationY = 30f

                child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(i * 80L)
                    .setDuration(300)
                    .start()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
