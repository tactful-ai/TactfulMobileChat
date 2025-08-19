package com.tactful.tactfulmobilechat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class WidgetActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    // Register a result launcher for the system file picker
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        val cb = filePathCallback
        filePathCallback = null

        if (cb == null) return@registerForActivityResult

        if (result.resultCode != Activity.RESULT_OK || result.data == null) {
            cb.onReceiveValue(null)         // user cancelled
            return@registerForActivityResult
        }

        val data: Intent = result.data!!
        val uris: Array<Uri>? = when {
            data.clipData != null -> {
                // Multiple selection
                val clip: ClipData = data.clipData!!
                Array(clip.itemCount) { i -> clip.getItemAt(i).uri }
            }
            data.data != null -> arrayOf(data.data!!)
            else -> null
        }
        cb.onReceiveValue(uris)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val url = intent.getStringExtra("URL") ?: "about:blank"
        val progress = findViewById<View>(R.id.progress)
        webView = findViewById(R.id.webView)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            cacheMode = WebSettings.LOAD_DEFAULT
            allowFileAccess = true
            allowContentAccess = true
            // If your widget is HTTPS but loads HTTP resources (not recommended), you may need:
            // mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                progress?.visibility = View.GONE
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            // This enables <input type="file"> in WebView
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                // Close any previous callbacks
                this@WidgetActivity.filePathCallback?.onReceiveValue(null)
                this@WidgetActivity.filePathCallback = filePathCallback

                // Build an OPEN_DOCUMENT intent (SAF; no storage permission needed)
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    // Accept any type; narrow it if your site expects specific types
                    type = "*/*"
                    // Support multiple if the site asked for it
                    if (fileChooserParams?.mode == FileChooserParams.MODE_OPEN_MULTIPLE) {
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }
                }

                // Optional: use acceptTypes from the page (e.g., ["image/*","application/pdf"])
                fileChooserParams?.acceptTypes
                    ?.firstOrNull { it.isNotBlank() }
                    ?.let { mime ->
                        intent.type = mime
                    }

                // Launch the picker
                filePickerLauncher.launch(intent)
                return true
            }
        }

        progress?.visibility = View.VISIBLE
        webView.loadUrl(url)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish(); return true
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.destroy()
        // If a chooser was open and activity is finishing, signal cancel
        filePathCallback?.onReceiveValue(null)
        filePathCallback = null
        super.onDestroy()
    }
}