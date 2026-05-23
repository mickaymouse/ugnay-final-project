package ugnay.app.frontend.residents.ui.news

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ugnay.app.R
import ugnay.app.backend.residents.data.News

class NewsAdapter(private var newsList: List<News>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_news_item_title)
        val priority: TextView = itemView.findViewById(R.id.tv_news_item_priority)
        val content: TextView = itemView.findViewById(R.id.tv_news_item_content)
        val duration: TextView = itemView.findViewById(R.id.tv_news_item_duration)
        // Note: Make sure tv_news_item_date exists in your XML if you want to display it
        // If you removed it from the header, remove this line.
        val image: ImageView = itemView.findViewById(R.id.iv_news_item_image)
        val imageContainer: View = itemView.findViewById(R.id.cv_news_image_container)
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
}