package com.example.capstone

import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    private var debugTapCount = 0
    private var lastDebugTapAt = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        setupFooter()
        setupCrashViewerEasterEgg()

        findViewById<MaterialButton>(R.id.btnSignIn).setOnClickListener {
            val email = findViewById<EditText>(R.id.inputEmail).text?.toString().orEmpty()
            val fallbackName = email.substringBefore('@').takeIf { it.isNotBlank() }
                ?.replaceFirstChar { it.uppercase() }
                ?: "User"

            try {
                startActivity(
                    Intent(this, DashboardActivity::class.java)
                        .putExtra(DashboardActivity.EXTRA_NAME, fallbackName)
                )
                finish()
            } catch (_: Throwable) {
                try {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } catch (_: Throwable) {
                    Toast.makeText(this, "Could not continue. Please restart app.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupFooter() {
        val footer = findViewById<TextView>(R.id.footer)
        val prefix = getString(R.string.login_footer_prefix)
        val action = getString(R.string.login_footer_action)

        val spannable = SpannableStringBuilder()
            .append(prefix)
            .append(action)

        val start = prefix.length
        val end = start + action.length

        val navy = ContextCompat.getColor(this, R.color.safeready_navy)

        spannable.setSpan(ForegroundColorSpan(navy), start, end, 0)
        spannable.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, end, 0)
        spannable.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, SignupActivity::class.java))
                finish()
            }

            override fun updateDrawState(ds: android.text.TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }, start, end, 0)

        footer.text = spannable
        footer.movementMethod = LinkMovementMethod.getInstance()
        footer.highlightColor = android.graphics.Color.TRANSPARENT
    }

    private fun setupCrashViewerEasterEgg() {
        val title = findViewById<TextView>(R.id.title)
        title.setOnClickListener {
            val now = android.os.SystemClock.elapsedRealtime()
            if (now - lastDebugTapAt > 1200) {
                debugTapCount = 0
            }
            lastDebugTapAt = now
            debugTapCount++
            if (debugTapCount >= 5) {
                debugTapCount = 0
                startActivity(Intent(this, CrashViewerActivity::class.java))
            }
        }
    }
}
