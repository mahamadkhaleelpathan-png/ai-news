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

class ArticleAdapter(
    private val articles: List<Article>,
    private val onClick: (Article) -> Unit
) : RecyclerView.Adapter<ArticleAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView = view.findViewById(R.id.cardArticle)
        val tvTitle: TextView = view.findViewById(R.id.tvArticleTitle)
        val tvSource: TextView = view.findViewById(R.id.tvArticleSource)
        val tvDate: TextView = view.findViewById(R.id.tvArticleDate)
        val tvDesc: TextView = view.findViewById(R.id.tvArticleDesc)
        val ivImage: ImageView = view.findViewById(R.id.ivArticleImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_article, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]
        holder.tvTitle.text = Html.fromHtml(article.title, Html.FROM_HTML_MODE_COMPACT).toString()
        holder.tvSource.text = article.source
        holder.tvDate.text = article.pubDate
        holder.tvDesc.text = Html.fromHtml(article.description, Html.FROM_HTML_MODE_COMPACT).toString().take(150)
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

    override fun getItemCount() = articles.size
}
