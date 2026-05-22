package ugnay.app.frontend.residents.ui.news

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ugnay.app.R
import ugnay.app.backend.residents.data.News

class NewsAdapter(private var newsList: List<News>) :
    RecyclerView.Adapter<NewsAdapter.NewsViewHolder>() {

    class NewsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_news_item_title)
        val priority: TextView = itemView.findViewById(R.id.tv_news_item_priority)
        val content: TextView = itemView.findViewById(R.id.tv_news_item_content)
        val date: TextView = itemView.findViewById(R.id.tv_news_item_date)
        val duration: TextView = itemView.findViewById(R.id.tv_news_item_duration)
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
        holder.priority.visibility = if (!news.priority.isNullOrBlank() && news.priority != "Normal") View.VISIBLE else View.GONE
        holder.date.text = news.datePosted?.take(10) ?: ""
        holder.duration.text = news.relativeDuration() ?: ""
    }

    override fun getItemCount(): Int = newsList.size

    fun updateData(newList: List<News>) {
        newsList = newList
        notifyDataSetChanged()
    }
}
