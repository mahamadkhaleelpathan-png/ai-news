package com.trendscope.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.trendscope.app.data.Article
import com.trendscope.app.databinding.ActivityCategoryBinding
import com.trendscope.app.network.DomainFeeds
import com.trendscope.app.network.RssParser
import kotlinx.coroutines.*

class CategoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryBinding
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var articles = mutableListOf<Article>()
    private var currentLang = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val category = intent.getStringExtra("category") ?: return
        currentLang = intent.getStringExtra("lang") ?: ""
        binding.toolbar.title = category
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.rvArticles.layoutManager = LinearLayoutManager(this)
        loadArticles(category)
        binding.swipeRefresh.setOnRefreshListener { loadArticles(category) }
    }

    private fun loadArticles(category: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        articles.clear()

        scope.launch {
            withContext(Dispatchers.IO) {
                val feeds = DomainFeeds.getFeedsForCategory(category)
                for (url in feeds) {
                    val sourceName = url.replace("www.", "").let {
                        try { java.net.URL(it).host } catch (e: Exception) { it }
                    }
                    val parsed = RssParser.fetchAndParse(url, category, sourceName)
                    articles.addAll(parsed)
                }
            }
            binding.progressBar.visibility = android.view.View.GONE
            binding.swipeRefresh.isRefreshing = false
            binding.rvArticles.adapter = ArticleAdapter(
                articles = articles.sortedByDescending { it.pubDateMillis },
                onClick = { article ->
                    val intent = android.content.Intent(this@CategoryActivity, ArticleActivity::class.java)
                    intent.putExtra("link", article.link)
                    intent.putExtra("title", article.title)
                    intent.putExtra("description", article.description)
                    intent.putExtra("content", article.content)
                    intent.putExtra("pubDate", article.pubDate)
                    intent.putExtra("imageUrl", article.imageUrl)
                    intent.putExtra("source", article.source)
                    intent.putExtra("category", article.category)
                    intent.putExtra("lang", currentLang)
                    startActivity(intent)
                },
                currentLang = currentLang
            )
            Toast.makeText(this@CategoryActivity, getString(R.string.loading_articles, articles.size), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
