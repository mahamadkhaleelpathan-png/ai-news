package com.trendscope.app

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.trendscope.app.data.Article
import java.text.SimpleDateFormat
import java.util.*

class ArticleAdapter(
    private val articles: List<Article>,
    private val onClick: (Article) -> Unit,
    private val currentLang: String = ""
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ARTICLE = 1
    }

    private data class Item(val type: Int, val headerText: String = "", val article: Article? = null)

    private val items: List<Item> by lazy { buildItems() }

    private fun buildItems(): List<Item> {
        val result = mutableListOf<Item>()
        val groups = mutableMapOf<String, MutableList<Article>>()
        for (article in articles) {
            val header = getDayHeader(article.pubDateMillis)
            groups.getOrPut(header) { mutableListOf() }.add(article)
        }
        val order = listOf("Today", "Yesterday", "This Week", "This Month")
        val orderedKeys = order.filter { groups.containsKey(it) } + groups.keys.filter { it !in order }.sortedDescending()
        for (key in orderedKeys) {
            groups[key]?.let { groupArticles ->
                result.add(Item(TYPE_HEADER, headerText = key))
                for (article in groupArticles) {
                    result.add(Item(TYPE_ARTICLE, article = article))
                }
            }
        }
        return result
    }

    private fun getDayHeader(millis: Long): String {
        if (millis == 0L) return "Earlier"
        val now = Calendar.getInstance()
        val articleDate = Calendar.getInstance().apply { timeInMillis = millis }
        val diffDays = ((now.timeInMillis - articleDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()
        return when {
            diffDays == 0 -> "Today"
            diffDays == 1 -> "Yesterday"
            diffDays <= 7 -> "This Week"
            diffDays <= 30 -> "This Month"
            else -> {
                val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
                sdf.format(Date(millis))
            }
        }
    }

    override fun getItemViewType(position: Int): Int = items[position].type

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_day_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_article, parent, false)
            ArticleViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is HeaderViewHolder -> holder.text.text = item.headerText
            is ArticleViewHolder -> item.article?.let { bindArticle(holder, it) }
        }
    }

    private fun bindArticle(holder: ArticleViewHolder, article: Article) {
        val displayTitle = if (currentLang.isNotEmpty() && article.translateLang == currentLang && article.translatedTitle.isNotEmpty()) {
            article.translatedTitle
        } else {
            Html.fromHtml(article.title, Html.FROM_HTML_MODE_COMPACT).toString()
        }
        val displayDesc = if (currentLang.isNotEmpty() && article.translateLang == currentLang && article.translatedDescription.isNotEmpty()) {
            article.translatedDescription
        } else {
            Html.fromHtml(article.description, Html.FROM_HTML_MODE_COMPACT).toString().take(150)
        }
        holder.tvTitle.text = displayTitle
        holder.tvSource.text = article.source
        holder.tvDate.text = article.pubDate
        holder.tvDesc.text = displayDesc
        holder.card.setOnClickListener { onClick(article) }

        if (article.imageUrl.isNotEmpty()) {
            try {
                val target = holder.ivImage
                target.visibility = View.VISIBLE
                val url = article.imageUrl
                Thread {
                    try {
                        val conn = java.net.URL(url).openConnection()
                        conn.connectTimeout = 3000
                        val input = conn.getInputStream()
                        val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                        input.close()
                        target.post { target.setImageBitmap(bitmap) }
                    } catch (_: Exception) { }
                }.start()
            } catch (_: Exception) { }
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.tvDayHeader)
    }

    class ArticleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.cardArticle)
        val tvTitle: TextView = view.findViewById(R.id.tvArticleTitle)
        val tvSource: TextView = view.findViewById(R.id.tvArticleSource)
        val tvDate: TextView = view.findViewById(R.id.tvArticleDate)
        val tvDesc: TextView = view.findViewById(R.id.tvArticleDesc)
        val ivImage: ImageView = view.findViewById(R.id.ivArticleImage)
    }
}
