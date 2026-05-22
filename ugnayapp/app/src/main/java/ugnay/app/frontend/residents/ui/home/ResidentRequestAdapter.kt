package ugnay.app.frontend.residents.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ugnay.app.R
import ugnay.app.backend.residents.data.Request
import ugnay.app.backend.residents.data.RequestStatus
import ugnay.app.databinding.ItemOfficialRequestBinding

class ResidentRequestAdapter(
    private var requests: List<Request>
) : RecyclerView.Adapter<ResidentRequestAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemOfficialRequestBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOfficialRequestBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val request = requests[position]
        holder.binding.apply {
            tvResidentName.text = request.type
            tvRequestType.text = "Purpose: ${request.purpose}"
            tvRequestPurpose.text = "Requested on: ${request.startDate}"
            
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

            // Hide action button and divider for residents
            btnAction.visibility = android.view.View.GONE
            viewDivider.visibility = android.view.View.GONE
        }
    }

    override fun getItemCount() = requests.size

    fun updateRequests(newRequests: List<Request>) {
        requests = newRequests
        notifyDataSetChanged()
    }
}
