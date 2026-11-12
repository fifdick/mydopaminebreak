package com.focusguard

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import java.util.concurrent.atomic.AtomicInteger

/**
 * MonitorService — foreground service that polls AdaptiveDetector every 60 seconds.
 * When aimless switching is detected it fires a notification with a message from
 * NotificationMessages.
 */
class MonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var detector: AdaptiveDetector
    private lateinit var notificationManager: NotificationManager
    private var polling = false

    companion object {
        private const val CHANNEL_SERVICE = "focusguard_service"
        private const val CHANNEL_ALERT   = "focusguard_alert"
        private const val NOTIF_ID_SERVICE = 1
        private const val POLL_INTERVAL_MS = 60_000L // 60 seconds
        private val alertNotifCounter = AtomicInteger(2) // start at 2 (1 reserved for service)
    }

    private val pollRunnable = object : Runnable {
        override fun run() {
            checkBehavior()
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    override fun onCreate() {
        super.onCreate()
        detector = AdaptiveDetector(this)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIF_ID_SERVICE, buildServiceNotification())
        if (!polling) {
            polling = true
            handler.post(pollRunnable)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        polling = false
        handler.removeCallbacks(pollRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ─── Detection logic ──────────────────────────────────────────────────────

    private fun checkBehavior() {
        if (detector.isAimlessSwitching()) {
            val message = NotificationMessages.getRandomMessage()
            showAlertNotification(message.text)
        }
    }

    // ─── Notifications ────────────────────────────────────────────────────────

    private fun createNotificationChannels() {
        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            getString(R.string.service_channel_name),
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = getString(R.string.service_channel_description)
        }

        val alertChannel = NotificationChannel(
            CHANNEL_ALERT,
            "FocusGuard Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Aimless phone use alerts"
        }

        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(alertChannel)
    }

    private fun buildServiceNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_SERVICE)
            .setContentTitle(getString(R.string.monitoring_active))
            .setContentText(getString(R.string.monitoring_description))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun showAlertNotification(message: String) {
        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val fullScreenIntent = PendingIntent.getActivity(
            this, alertNotifCounter.get(),
            Intent(this, AlertActivity::class.java).apply {
                putExtra(AlertActivity.EXTRA_MESSAGE, message)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ALERT)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(tapIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(alertNotifCounter.getAndIncrement(), notification)
    }
}
