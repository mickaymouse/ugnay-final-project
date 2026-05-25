package ugnay.app.frontend.brgy_officials.ui.news

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ugnay.app.R
import ugnay.app.backend.residents.data.News
import ugnay.app.backend.residents.data.UserType
import ugnay.app.backend.residents.data.UserRepository

class OfficialNewsAdapter(
    private var newsList: List<News>,
    private val onEditClick: (News) -> Unit = {},
    private val onDeleteClick: (News) -> Unit = {}
) : RecyclerView.Adapter<OfficialNewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_news_item_title)
        val priority: TextView = itemView.findViewById(R.id.tv_news_item_priority)
        val content: TextView = itemView.findViewById(R.id.tv_news_item_content)
        val duration: TextView = itemView.findViewById(R.id.tv_news_item_duration)
        val image: ImageView = itemView.findViewById(R.id.iv_news_item_image)
        val imageContainer: View = itemView.findViewById(R.id.cv_news_image_container)
        val authorName: TextView = itemView.findViewById(R.id.tv_news_author_name)
        val authorImage: ImageView = itemView.findViewById(R.id.iv_news_author_image)
        val authorPosition: TextView = itemView.findViewById(R.id.tv_news_author_position)
        val menuButton: ImageButton = itemView.findViewById(R.id.btn_news_menu)
        val editedIndicator: TextView = itemView.findViewById(R.id.tv_news_edited_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false)
        return NewsViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val news = newsList[position]

        holder.title.text = news.title
        holder.content.text = news.content

        // Priority logic: Show if priority is not Normal/Null
        holder.priority.visibility = if (!news.priority.isNullOrBlank() && news.priority != "Normal") View.VISIBLE else View.GONE

        // Set Duration
        holder.duration.text = news.relativeDuration() ?: ""

        // Show edited indicator if the post has been edited
        if (!news.editedBy.isNullOrBlank()) {
            holder.editedIndicator.visibility = View.VISIBLE
            holder.editedIndicator.text = "(Edited by ${news.editedBy})"
        } else {
            holder.editedIndicator.visibility = View.GONE
        }

        // Show menu button for official posts
        holder.menuButton.visibility = View.VISIBLE
        holder.menuButton.setOnClickListener {
            showPopupMenu(holder.menuButton, news)
        }

        // Fetch and Bind Author Info
        if (!news.userId.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.Main).launch {
                val user = withContext(Dispatchers.IO) {
                    UserRepository.getUserById(news.userId)
                }
                if (user != null) {
                    holder.authorName.text = "${user.firstName} ${user.lastName}"
                    holder.authorPosition.text = if (user.userType == UserType.BARANGAY_OFFICIAL) "Barangay Official" else "Resident"
                    
                    if (!user.profilePictureUrl.isNullOrEmpty()) {
                        holder.authorImage.load(user.profilePictureUrl) {
                            crossfade(true)
                            transformations(CircleCropTransformation())
                            placeholder(R.drawable.ic_person)
                            error(R.drawable.ic_person)
                            listener(onSuccess = { _, _ ->
                                holder.authorImage.imageTintList = null
                            })
                        }
                    } else {
                        holder.authorImage.setImageResource(R.drawable.ic_person)
                        holder.authorImage.imageTintList = ColorStateList.valueOf(holder.itemView.context.getColor(R.color.brgy_blue))
                    }
                } else {
                    holder.authorName.text = "Barangay Official"
                    holder.authorPosition.text = "Barangay Hall"
                    holder.authorImage.setImageResource(R.drawable.ic_person)
                    holder.authorImage.imageTintList = ColorStateList.valueOf(holder.itemView.context.getColor(R.color.brgy_blue))
                }
            }
        } else {
            holder.authorName.text = "Barangay Official"
            holder.authorPosition.text = "Barangay Hall"
            holder.authorImage.setImageResource(R.drawable.ic_person)
            holder.authorImage.imageTintList = ColorStateList.valueOf(holder.itemView.context.getColor(R.color.brgy_blue))
        }

        // Image Handling
        if (!news.imageUrl.isNullOrBlank()) {
            holder.imageContainer.visibility = View.VISIBLE
            holder.image.load(news.imageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_foreground)
                error(R.drawable.ic_launcher_foreground)
            }
        } else {
            holder.imageContainer.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = newsList.size

    fun updateData(newList: List<News>) {
        newsList = newList
        notifyDataSetChanged()
    }

    private fun showPopupMenu(anchor: View, news: News) {
        val popupMenu = PopupMenu(anchor.context, anchor)
        popupMenu.menuInflater.inflate(R.menu.menu_post_options, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    onEditClick(news)
                    true
                }
                R.id.action_delete -> {
                    onDeleteClick(news)
                    true
                }
                else -> false
            }
        }
        
        popupMenu.show()
    }
}
