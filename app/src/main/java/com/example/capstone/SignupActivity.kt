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

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)

        setupFooter()

        findViewById<MaterialButton>(R.id.btnCreate).setOnClickListener {
            val name = findViewById<EditText>(R.id.inputName).text?.toString().orEmpty()

            try {
                startActivity(
                    Intent(this, DashboardActivity::class.java)
                        .putExtra(DashboardActivity.EXTRA_NAME, name)
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
        val prefix = getString(R.string.signup_footer_prefix)
        val action = getString(R.string.signup_footer_action)

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
                startActivity(Intent(this@SignupActivity, LoginActivity::class.java))
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
}
