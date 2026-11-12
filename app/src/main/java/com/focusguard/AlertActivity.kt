package com.focusguard

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * AlertActivity — full-screen aggressive alert shown when aimless switching is detected.
 * Turns the screen on and shows over the lock screen so the message is impossible to miss.
 */
class AlertActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MESSAGE = "focusguard_message"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Turn screen on and show over the lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }

        setContentView(R.layout.activity_alert)

        val message = intent.getStringExtra(EXTRA_MESSAGE)
            ?: getString(R.string.app_name)

        findViewById<TextView>(R.id.tvMessage).text = message
        findViewById<Button>(R.id.btnDismiss).setOnClickListener { finish() }
    }
}
