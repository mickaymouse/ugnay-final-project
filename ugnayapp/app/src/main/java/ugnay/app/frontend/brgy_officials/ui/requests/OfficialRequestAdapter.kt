package ugnay.app.frontend.brgy_officials.ui.requests

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ugnay.app.R
import ugnay.app.backend.residents.data.Request
import ugnay.app.backend.residents.data.RequestStatus
import ugnay.app.databinding.ItemOfficialRequestBinding

class OfficialRequestAdapter(
    private var requests: List<Request>,
    private val onViewProfile: (Request) -> Unit,
    private val onStatusUpdate: (Request, RequestStatus) -> Unit
) : RecyclerView.Adapter<OfficialRequestAdapter.RequestViewHolder>() {

    class RequestViewHolder(val binding: ItemOfficialRequestBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemOfficialRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RequestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.binding.apply {
            tvResidentName.text = request.fullName
            tvRequestType.text = request.type
            tvRequestPurpose.text = "Purpose: ${request.purpose}"
            
            val status = request.status
            tvRequestStatus.text = status.displayName.uppercase()

            val statusColor = when (status) {
                RequestStatus.PENDING -> R.color.brgy_yellow
                RequestStatus.APPROVED -> R.color.brgy_blue
                RequestStatus.REJECTED -> R.color.brgy_red
                RequestStatus.DONE -> R.color.brgy_green
                RequestStatus.EXPIRED -> R.color.text_secondary
            }
            tvRequestStatus.setTextColor(ContextCompat.getColor(root.context, statusColor))

            // Clicking the card opens the profile/details dialog
            root.setOnClickListener {
                onViewProfile(request)
            }

            btnAction.setOnClickListener {
                showStatusPopup(it, request)
            }
        }
    }

    private fun showStatusPopup(view: android.view.View, request: Request) {
        val popup = PopupMenu(view.context, view)
        RequestStatus.entries.forEach { status ->
            popup.menu.add(status.displayName)
        }

        popup.setOnMenuItemClickListener { item ->
            val selectedStatus = RequestStatus.fromString(item.title.toString())
            onStatusUpdate(request, selectedStatus)
            true
        }
        popup.show()
    }

    override fun getItemCount() = requests.size

    fun updateRequests(newRequests: List<Request>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}
