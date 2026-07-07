package com.trendscope.app

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.trendscope.app.data.AppDatabase
import com.trendscope.app.data.Article
import com.trendscope.app.databinding.ActivityArticleBinding
import kotlinx.coroutines.*

class ArticleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArticleBinding
    private lateinit var db: AppDatabase
    private lateinit var article: Article
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isBookmarked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        article = Article(
            link = intent.getStringExtra("link") ?: return,
            title = intent.getStringExtra("title") ?: "",
            description = intent.getStringExtra("description") ?: "",
            pubDate = intent.getStringExtra("pubDate") ?: "",
            imageUrl = intent.getStringExtra("imageUrl") ?: "",
            source = intent.getStringExtra("source") ?: "",
            category = intent.getStringExtra("category") ?: "",
            content = intent.getStringExtra("content") ?: ""
        )

        db = AppDatabase.getInstance(this)
        binding.toolbar.title = ""
        binding.toolbar.setNavigationOnClickListener { finish() }

        displayArticle()
        checkBookmarkStatus()

        binding.fabBookmark.setOnClickListener { toggleBookmark() }
        binding.fabShare.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${article.title}\n\n${article.link}")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(sendIntent, "Share"))
        }

        binding.tvOpenOriginal.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.link))
            startActivity(intent)
        }
    }

    private fun displayArticle() {
        binding.tvArticleTitle.text = Html.fromHtml(article.title, Html.FROM_HTML_MODE_COMPACT).toString()
        binding.tvArticleSource.text = article.source
        binding.tvArticleDate.text = article.pubDate
        val content = Html.fromHtml(
            if (article.description.isNotEmpty()) article.description else article.content,
            Html.FROM_HTML_MODE_COMPACT
        ).toString()
        binding.tvArticleContent.text = content

        if (article.imageUrl.isNotEmpty()) {
            binding.ivArticleImage.visibility = View.VISIBLE
            Thread {
                try {
                    val conn = java.net.URL(article.imageUrl).openConnection()
                    conn.connectTimeout = 5000
                    val input = conn.getInputStream()
                    val bitmap = BitmapFactory.decodeStream(input)
                    input.close()
                    binding.ivArticleImage.post { binding.ivArticleImage.setImageBitmap(bitmap) }
                } catch (_: Exception) { }
            }.start()
        }
    }

    private fun checkBookmarkStatus() {
        scope.launch {
            isBookmarked = withContext(Dispatchers.IO) {
                db.articleDao().isBookmarked(article.link) ?: false
            }
            updateBookmarkIcon()
        }
    }

    private fun toggleBookmark() {
        scope.launch {
            withContext(Dispatchers.IO) {
                db.articleDao().toggleBookmark(article.link)
            }
            isBookmarked = !isBookmarked
            updateBookmarkIcon()
            Toast.makeText(this@ArticleActivity,
                if (isBookmarked) "Bookmarked" else "Removed bookmark",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateBookmarkIcon() {
        binding.fabBookmark.setImageResource(
            if (isBookmarked) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
