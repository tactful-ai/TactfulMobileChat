# 📱 Tactful Widget – WebView Integration

This guide explains how to create a **WidgetActivity** that opens the **Tactful widget** inside a WebView and supports **file attachments** (`<input type="file">`).

---

## 1. Layout
📍 **Path:** `res/layout/activity_widget.xml`
```xml
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">

    <ProgressBar
        android:id="@+id/progress"
        style="?android:attr/progressBarStyleLarge"
        android:layout_gravity="center"
        android:visibility="gone"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />

    <WebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
</FrameLayout>
```

---

## 2. Activity
📍 **Path:** `WidgetActivity.kt`
```kotlin
class WidgetActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val cb = filePathCallback
        filePathCallback = null
        if (cb == null) return@registerForActivityResult
        if (result.resultCode != Activity.RESULT_OK || result.data == null) {
            cb.onReceiveValue(null); return@registerForActivityResult
        }
        val data = result.data!!
        val uris: Array<Uri>? = when {
            data.clipData != null -> {
                val clip = data.clipData!!
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

        val url = "https://webchat.tactful.ai/webchat/chatChannel.html#/?profileId=PROFILE_ID&token=PROFILE_TOKEN"
        val progress = findViewById<View>(R.id.progress)
        webView = findViewById(R.id.webView)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowContentAccess = true
        }

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                view: WebView?, callback: ValueCallback<Array<Uri>>?,
                params: FileChooserParams?
            ): Boolean {
                filePathCallback?.onReceiveValue(null)
                filePathCallback = callback
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    if (params?.mode == FileChooserParams.MODE_OPEN_MULTIPLE) {
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                    }
                }
                filePickerLauncher.launch(intent)
                return true
            }
        }

        progress?.visibility = View.VISIBLE
        webView.loadUrl(url)
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.destroy()
        filePathCallback?.onReceiveValue(null)
        filePathCallback = null
        super.onDestroy()
    }
}
```

---

## ✅ Key Deliverables
- WebView loads the Tactful widget URL.
- File attachment button in the widget opens system file picker.
- Multiple file selection supported if widget requests it.
- Clean-up ensures no memory leaks.

---

## ℹ️ Recommendation – Safe Area
Add this to root layouts (`activity_widget.xml`):
```xml
android:fitsSystemWindows="true"
```
👉 Prevents widget content from appearing under the **status bar** or **navigation bar**.  
