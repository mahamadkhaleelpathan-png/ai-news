package com.trendscope.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.trendscope.app.data.AppDatabase
import com.trendscope.app.data.Article
import com.trendscope.app.databinding.ActivityMainBinding
import com.trendscope.app.network.DomainFeeds
import com.trendscope.app.network.RssParser
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var allArticles = mutableListOf<Article>()
    private var currentTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        setupTabs()
        setupCategoryGrid()
        loadCachedArticles()
        binding.tabLayout.getTabAt(0)?.select()

        binding.fabRefresh.setOnClickListener { refreshAllFeeds() }

        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            searchArticles(binding.etSearch.text.toString())
            true
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentTab = tab?.position ?: 0
                when (currentTab) {
                    0 -> showCategories()
                    1 -> showAllArticles()
                    2 -> showBookmarks()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupCategoryGrid() {
        val categories = DomainFeeds.getAllCategories()
        binding.rvCategories.layoutManager = GridLayoutManager(this, 2)
        binding.rvCategories.adapter = CategoryAdapter(categories) { category ->
            val intent = Intent(this, CategoryActivity::class.java)
            intent.putExtra("category", category)
            startActivity(intent)
        }
    }

    private fun showCategories() {
        binding.rvCategories.visibility = android.view.View.VISIBLE
        binding.rvArticles.visibility = android.view.View.GONE
        binding.progressBar.visibility = android.view.View.GONE
        binding.tvStatus.visibility = android.view.View.GONE
    }

    private fun showAllArticles() {
        if (allArticles.isEmpty()) {
            refreshAllFeeds()
        } else {
            binding.rvCategories.visibility = android.view.View.GONE
            binding.rvArticles.visibility = android.view.View.VISIBLE
            binding.progressBar.visibility = android.view.View.GONE
            binding.tvStatus.visibility = android.view.View.GONE
            showArticlesList(allArticles.sortedByDescending { it.fetchedAt })
        }
    }

    private fun showBookmarks() {
        scope.launch {
            val bookmarks = withContext(Dispatchers.IO) { db.articleDao().getBookmarks() }
            binding.rvCategories.visibility = android.view.View.GONE
            binding.rvArticles.visibility = android.view.View.VISIBLE
            binding.progressBar.visibility = android.view.View.GONE
            binding.tvStatus.visibility = android.view.View.GONE
            if (bookmarks.isEmpty()) {
                Toast.makeText(this@MainActivity, "No bookmarks yet", Toast.LENGTH_SHORT).show()
            }
            showArticlesList(bookmarks)
        }
    }

    private fun showArticlesList(articles: List<Article>) {
        binding.rvArticles.adapter = ArticleAdapter(articles) { article ->
            val intent = Intent(this, ArticleActivity::class.java)
            intent.putExtra("link", article.link)
            intent.putExtra("title", article.title)
            intent.putExtra("source", article.source)
            startActivity(intent)
        }
    }

    private fun refreshAllFeeds() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvStatus.text = "Starting..."
        binding.tvStatus.visibility = android.view.View.VISIBLE
        allArticles.clear()

        scope.launch {
            withContext(Dispatchers.IO) {
                RssParser.fetchAllFeeds(
                    onProgress = { category, source ->
                        launch(Dispatchers.Main) {
                            binding.tvStatus.text = "Fetching $category - $source..."
                        }
                    },
                    onArticle = { article ->
                        allArticles.add(article)
                    }
                )
            }
            withContext(Dispatchers.IO) {
                db.articleDao().insertArticles(allArticles)
            }
            binding.progressBar.visibility = android.view.View.GONE
            binding.tvStatus.visibility = android.view.View.GONE
            binding.rvCategories.visibility = android.view.View.GONE
            binding.rvArticles.visibility = android.view.View.VISIBLE
            showArticlesList(allArticles.sortedByDescending { it.fetchedAt })
            Toast.makeText(this@MainActivity, "Loaded ${allArticles.size} articles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadCachedArticles() {
        scope.launch {
            val count = withContext(Dispatchers.IO) { db.articleDao().getCount() }
            if (count > 0) {
                val articles = withContext(Dispatchers.IO) { db.articleDao().getAllArticles() }
                allArticles.addAll(articles)
            }
        }
    }

    private fun searchArticles(query: String) {
        if (query.isBlank()) return
        scope.launch {
            binding.progressBar.visibility = android.view.View.VISIBLE
            val results = withContext(Dispatchers.IO) {
                db.articleDao().searchArticles(query)
            }
            binding.progressBar.visibility = android.view.View.GONE
            binding.rvCategories.visibility = android.view.View.GONE
            binding.rvArticles.visibility = android.view.View.VISIBLE
            showArticlesList(results)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
