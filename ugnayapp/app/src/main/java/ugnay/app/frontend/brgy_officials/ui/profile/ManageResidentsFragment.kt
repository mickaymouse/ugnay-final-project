package ugnay.app.frontend.brgy_officials.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch
import ugnay.app.R
import ugnay.app.backend.residents.SupabaseConfig
import ugnay.app.backend.residents.data.User
import ugnay.app.backend.residents.data.UserType
import ugnay.app.databinding.FragmentManageResidentsBinding

class ManageResidentsFragment : Fragment() {

    private var _binding: FragmentManageResidentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var residentAdapter: ResidentAdapter
    private var fullResidentList: List<User> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageResidentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        fetchResidentsFromDatabase()

        binding.etSearchResident.addTextChangedListener { text ->
            filterList(text.toString())
        }
    }

    private fun setupRecyclerView() {
        residentAdapter = ResidentAdapter(emptyList())
        binding.rvResidents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = residentAdapter
        }
    }

    private fun fetchResidentsFromDatabase() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                binding.progressBar.visibility = View.VISIBLE

                val response = SupabaseConfig.client.from("users").select()
                val allUsers = response.decodeList<User>()

                fullResidentList = allUsers.filter { it.userType == UserType.RESIDENT }
                residentAdapter.updateData(fullResidentList)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch data: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun filterList(query: String) {
        val filteredList = fullResidentList.filter { resident ->
            val fullName = "${resident.firstName} ${resident.lastName}"
            fullName.contains(query, ignoreCase = true) ||
                    (resident.userId?.contains(query, ignoreCase = true) ?: false)
        }
        residentAdapter.updateData(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // --- RECYCLERVIEW ADAPTER USING TEMPLATE INFLATION ---
    private class ResidentAdapter(private var residents: List<User>) :
        RecyclerView.Adapter<ResidentAdapter.ResidentViewHolder>() {

        class ResidentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imgAvatar: ImageView = view.findViewById(R.id.imgResidentAvatar)
            val tvName: TextView = view.findViewById(R.id.tvItemName)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResidentViewHolder {
            val rowView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_resident_row, parent, false)

            return ResidentViewHolder(rowView)
        }

        override fun onBindViewHolder(holder: ResidentViewHolder, position: Int) {
            val resident = residents[position]

            // 1. Bind Name
            holder.tvName.text = holder.itemView.context.getString(R.string.resident_full_name, resident.firstName, resident.lastName).trim()

            // 2. Image Loading
            if (!resident.profilePictureUrl.isNullOrBlank()) {
                holder.imgAvatar.load(resident.profilePictureUrl) {
                    crossfade(true)
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_person)
                    error(R.drawable.ic_person)
                }
                holder.imgAvatar.setPadding(0, 0, 0, 0)
            } else {
                holder.imgAvatar.setImageResource(R.drawable.ic_person)
                holder.imgAvatar.setPadding(4, 4, 4, 4)
            }
        }

        override fun getItemCount(): Int = residents.size

        fun updateData(newResidents: List<User>) {
            this.residents = newResidents
            notifyDataSetChanged()
        }
    }
}