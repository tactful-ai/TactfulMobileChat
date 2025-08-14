package com.tactful.tactfulmobilechat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class WidgetActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget)

        // Optional: show an "Up" button in the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val url = intent.getStringExtra("URL") ?: "about:blank"
        val progress = findViewById<View>(R.id.progress)
        webView = findViewById(R.id.webView)

        // Minimal, safe defaults
        with(webView.settings) {
            javaScriptEnabled = true           // most widgets need this
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                progress?.visibility = View.GONE
            }
        }
        webView.webChromeClient = object : WebChromeClient() {}

        progress?.visibility = View.VISIBLE
        webView.loadUrl(url)

        // Back button: go back in WebView history first, then close Activity
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack()
                else finish()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        // Clean up WebView to avoid leaks
        try {
            webView.stopLoading()
//            webView.webChromeClient = null
//            webView.webViewClient = null
            webView.loadUrl("about:blank")
            webView.destroy()
        } catch (_: Exception) { }
        super.onDestroy()
    }
}