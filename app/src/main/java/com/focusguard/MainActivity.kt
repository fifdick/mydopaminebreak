package com.focusguard

import android.Manifest
import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * MainActivity — checks for the UsageStats permission and starts MonitorService.
 *
 * If UsageStats access is not granted the user is shown a button that deep-links
 * to the system Settings screen where it can be granted. Once granted, the app
 * requests POST_NOTIFICATIONS (Android 13+) and then starts MonitorService.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var permissionButton: Button

    private var wasPermissionGranted = false

    // Launched only on API 33+ when POST_NOTIFICATIONS has not been granted yet.
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Start the service regardless of outcome; notifications may be silenced
            // if denied, but the service can still operate.
            launchService()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple programmatic layout — no XML layout file needed
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(64, 128, 64, 64)
        }

        statusText = TextView(this).apply {
            textSize = 18f
            setPadding(0, 0, 0, 48)
        }

        permissionButton = Button(this).apply {
            text = getString(R.string.grant_permission)
            setOnClickListener { openUsageAccessSettings() }
        }

        layout.addView(statusText)
        layout.addView(permissionButton)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        val hasPermission = hasUsageStatsPermission()

        if (hasPermission) {
            statusText.text = getString(R.string.monitoring_active)
            permissionButton.isEnabled = false
            // Only start when transitioning from no-permission → permission, or if the service died.
            if (!wasPermissionGranted || !isServiceRunning(MonitorService::class.java)) {
                startMonitorService()
            }
        } else {
            statusText.text = getString(R.string.permission_rationale)
            permissionButton.isEnabled = true
        }

        wasPermissionGranted = hasPermission
    }

    // ─── Permission checks ────────────────────────────────────────────────────

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun openUsageAccessSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    // ─── Service start ────────────────────────────────────────────────────────

    private fun startMonitorService() {
        // On Android 13+ request POST_NOTIFICATIONS before starting the service so
        // that the foreground-service notification and alert notifications are visible.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        launchService()
    }

    private fun launchService() {
        if (!isServiceRunning(MonitorService::class.java)) {
            ContextCompat.startForegroundService(this, Intent(this, MonitorService::class.java))
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        return manager.getRunningServices(Integer.MAX_VALUE)
            .any { it.service.className == serviceClass.name }
    }
}
