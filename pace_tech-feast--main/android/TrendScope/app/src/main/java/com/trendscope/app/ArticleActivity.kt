package com.trendscope.app

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.trendscope.app.data.AppDatabase
import com.trendscope.app.databinding.ActivityArticleBinding
import kotlinx.coroutines.*

class ArticleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArticleBinding
    private lateinit var db: AppDatabase
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var link = ""
    private var isBookmarked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        link = intent.getStringExtra("link") ?: return
        val title = intent.getStringExtra("title") ?: ""
        val source = intent.getStringExtra("source") ?: ""

        db = AppDatabase.getInstance(this)
        binding.toolbar.title = title.take(60)
        binding.toolbar.setNavigationOnClickListener { finish() }

        checkBookmarkStatus()
        setupWebView()

        binding.fabBookmark.setOnClickListener { toggleBookmark() }
        binding.fabShare.setOnClickListener {
            val sendIntent = android.content.Intent().apply {
                action = android.content.Intent.ACTION_SEND
                putExtra(android.content.Intent.EXTRA_TEXT, link)
                type = "text/plain"
            }
            startActivity(android.content.Intent.createChooser(sendIntent, "Share"))
        }
    }

    private fun setupWebView() {
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            userAgentString = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36"
        }
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.progressBar.visibility = View.VISIBLE
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                binding.progressBar.visibility = View.GONE
            }
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }
        }
        binding.webView.loadUrl(link)
    }

    private fun checkBookmarkStatus() {
        scope.launch {
            isBookmarked = withContext(Dispatchers.IO) {
                db.articleDao().isBookmarked(link) ?: false
            }
            updateBookmarkIcon()
        }
    }

    private fun toggleBookmark() {
        scope.launch {
            withContext(Dispatchers.IO) {
                db.articleDao().toggleBookmark(link)
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

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) binding.webView.goBack()
        else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
