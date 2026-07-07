package com.trendscope.app

import android.content.Intent
import android.content.SharedPreferences
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
import com.trendscope.app.translate.TranslationManager
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var allArticles = mutableListOf<Article>()
    private var currentTab = 0
    private var currentLang = ""
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
        prefs = getSharedPreferences("trendscope_prefs", MODE_PRIVATE)
        currentLang = prefs.getString("selected_lang", "") ?: ""

        setupTabs()
        setupCategoryGrid()
        loadCachedArticles()
        binding.tabLayout.getTabAt(0)?.select()

        binding.fabRefresh.setOnClickListener { refreshAllFeeds() }

        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            searchArticles(binding.etSearch.text.toString())
            true
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_language) {
                showLanguageSelector()
                true
            } else false
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
            intent.putExtra("lang", currentLang)
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
            showArticlesList(allArticles.sortedByDescending { it.pubDateMillis })
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
                Toast.makeText(this@MainActivity, getString(R.string.no_bookmarks), Toast.LENGTH_SHORT).show()
            }
            showArticlesList(bookmarks)
        }
    }

    private fun showArticlesList(articles: List<Article>) {
        binding.rvArticles.adapter = ArticleAdapter(articles, { article ->
            val intent = Intent(this, ArticleActivity::class.java)
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
        }, currentLang)
    }

    private fun refreshAllFeeds() {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvStatus.text = getString(R.string.status_starting)
        binding.tvStatus.visibility = android.view.View.VISIBLE
        allArticles.clear()

        scope.launch {
            withContext(Dispatchers.IO) {
                RssParser.fetchAllFeeds(
                    onProgress = { category, source ->
                        launch(Dispatchers.Main) {
                            binding.tvStatus.text = "${getString(R.string.status_fetching)} $category - $source..."
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
            showArticlesList(allArticles.sortedByDescending { it.pubDateMillis })
            Toast.makeText(this@MainActivity, getString(R.string.loading_articles, allArticles.size), Toast.LENGTH_SHORT).show()
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

    private fun showLanguageSelector() {
        val languages = TranslationManager.getSupportedLanguages()
        val items = languages.map { TranslationManager.getDisplayName(it) + " (" + TranslationManager.getEnglishName(it) + ")" }.toTypedArray()

        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_language))
            .setItems(items) { _, which ->
                val code = languages[which]
                setLanguage(code)
            }
            .setNegativeButton("English") { _, _ -> setLanguage("") }
            .show()
    }

    private fun setLanguage(code: String) {
        currentLang = code
        prefs.edit().putString("selected_lang", code).apply()
        if (code.isNotEmpty()) {
            translateAllArticles(code)
        } else {
            refreshCurrentView()
        }
    }

    private fun translateAllArticles(targetLang: String) {
        binding.progressBar.visibility = android.view.View.VISIBLE
        binding.tvStatus.text = getString(R.string.status_downloading_model)
        binding.tvStatus.visibility = android.view.View.VISIBLE

        TranslationManager.downloadModel(targetLang,
            onSuccess = {
                binding.tvStatus.text = getString(R.string.status_translating)
                scope.launch {
                    val toTranslate = withContext(Dispatchers.IO) {
                        db.articleDao().getArticlesNotInLang(targetLang)
                    }
                    translateBatch(toTranslate, targetLang)
                }
            },
            onFailure = { e ->
                binding.progressBar.visibility = android.view.View.GONE
                binding.tvStatus.visibility = android.view.View.GONE
                Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun translateBatch(articles: List<Article>, targetLang: String) {
        scope.launch {
            var count = 0
            val total = articles.size
            for (article in articles) {
                count++
                val statusText = getString(R.string.status_translating_count, count, total)
                launch(Dispatchers.Main) { binding.tvStatus.text = statusText }
                TranslationManager.translate(article.title, targetLang,
                    onResult = { translatedTitle ->
                        TranslationManager.translate(article.description, targetLang,
                            onResult = { translatedDesc ->
                                TranslationManager.translate(article.content.ifEmpty { article.description }, targetLang,
                                    onResult = { translatedContent ->
                                        scope.launch {
                                            withContext(Dispatchers.IO) {
                                                db.articleDao().updateTranslation(
                                                    article.link, translatedTitle, translatedDesc, translatedContent, targetLang
                                                )
                                            }
                                        }
                                    },
                                    onError = {}
                                )
                            },
                            onError = {}
                        )
                    },
                    onError = {}
                )
            }
            delay(2000)
            refreshCurrentView()
            binding.progressBar.visibility = android.view.View.GONE
            binding.tvStatus.visibility = android.view.View.GONE
            Toast.makeText(this@MainActivity, getString(R.string.translation_complete), Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshCurrentView() {
        scope.launch {
            val articles = withContext(Dispatchers.IO) { db.articleDao().getAllArticles() }
            allArticles.clear()
            allArticles.addAll(articles)
            when (currentTab) {
                1 -> showArticlesList(allArticles.sortedByDescending { it.pubDateMillis })
                2 -> showBookmarks()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
