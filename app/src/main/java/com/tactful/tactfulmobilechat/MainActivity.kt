package com.tactful.tactfulmobilechat
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val prefs by lazy { getSharedPreferences("widget_prefs", MODE_PRIVATE) }
    private val PREF_KEY_URL = "url"
    private val defaultUrl = "https://your-widget.example.com/embed"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inputUrl = findViewById<EditText>(R.id.inputUrl)
        val txtCurrent = findViewById<TextView>(R.id.txtCurrentUrl)
        val btnClear = findViewById<Button>(R.id.btnClearUrl)
        val fab = findViewById<FloatingActionButton>(R.id.fabWidget)

        // Load saved URL
        val saved = prefs.getString(PREF_KEY_URL, defaultUrl).orEmpty()
        inputUrl.setText(saved)
        txtCurrent.text = "Current URL: $saved"

        // Update text + save
        inputUrl.doAfterTextChanged { editable ->
            val raw = editable?.toString()?.trim().orEmpty()
            val normalized = normalizeUrl(raw)
            txtCurrent.text = if (normalized.isEmpty())
                "Current URL: (not set)" else "Current URL: $normalized"
            prefs.edit().putString(PREF_KEY_URL, normalized).apply()
        }

        // Clear button: resets everything
        btnClear.setOnClickListener {
            inputUrl.setText("")
            txtCurrent.text = "Current URL: (not set)"
            prefs.edit().remove(PREF_KEY_URL).apply()
        }

        // FAB click: open widget
        fab.setOnClickListener {
            val url = prefs.getString(PREF_KEY_URL, "").orEmpty()
            if (url.isBlank()) {
                Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, WidgetActivity::class.java).apply {
                putExtra("URL", url)
            })
        }
    }

    private fun normalizeUrl(input: String): String {
        if (input.isBlank()) return ""
        val lower = input.lowercase()
        val hasScheme = lower.startsWith("http://") || lower.startsWith("https://")
        return if (hasScheme) input else "https://$input"
    }
}