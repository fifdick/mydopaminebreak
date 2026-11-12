package com.focusguard

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.sqrt

/**
 * AdaptiveDetector — learns your behavior over time.
 *
 * PHASES:
 *   Days 0–6   → Learning phase. Just observing. Uses conservative fixed thresholds.
 *   Day 7+     → Adaptive phase. Triggers based on YOUR own baseline, not generic rules.
 *
 * WHAT IT TRACKS per 10-minute window:
 *   - switchRate      : app switches per minute
 *   - avgDwellMs      : average time per app (ms)
 *   - uniqueApps      : distinct apps opened
 *   - returnCount     : times you returned to the same app
 *
 * DETECTION MODEL:
 *   After learning phase, computes mean + stddev of your own sessions.
 *   Fires when current behavior is > 1.5 standard deviations from YOUR normal.
 *   Separate baselines per time-of-day bucket (morning / afternoon / evening / night)
 *   because your morning phone behavior is different from evening.
 *
 *   This is essentially z-score anomaly detection on your own behavioral data.
 *   You'll recognize the pattern.
 */
class AdaptiveDetector(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("adaptive_detector", Context.MODE_PRIVATE)

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    // Apps we ignore (launchers, system UI, keyboard, etc.)
    private val systemPackages = setOf(
        "com.android.launcher", "com.android.launcher3",
        "com.android.systemui", "com.google.android.inputmethod.latin",
        "com.samsung.android.honeyboard", "com.android.settings",
        "com.focusguard" // ignore our own app
    )

    // ─── Data classes ─────────────────────────────────────────────────────────

    data class WindowSnapshot(
        val switchRate: Float,      // switches per minute
        val avgDwellMs: Long,       // avg ms per app
        val uniqueApps: Int,        // distinct app count
        val returnCount: Int,       // same-app revisits
        val hourOfDay: Int          // 0–23
    )

    data class Baseline(
        val switchRateMean: Double,
        val switchRateStd: Double,
        val avgDwellMsMean: Double,
        val avgDwellMsStd: Double,
        val sampleCount: Int
    )

    // ─── Time buckets ─────────────────────────────────────────────────────────

    private fun timeBucket(hour: Int): String = when (hour) {
        in 5..11  -> "morning"
        in 12..17 -> "afternoon"
        in 18..22 -> "evening"
        else      -> "night"
    }

    // ─── Main detection entry point ───────────────────────────────────────────

    /**
     * Returns true if behavior in the last 10 minutes looks aimless.
     * Also records this snapshot for ongoing learning.
     */
    fun isAimlessSwitching(): Boolean {
        val snapshot = captureWindow(windowMinutes = 10) ?: return false
        recordSnapshot(snapshot) // always learn
        return evaluate(snapshot)
    }

    // ─── Capture current window ───────────────────────────────────────────────

    private fun captureWindow(windowMinutes: Long): WindowSnapshot? {
        val endTime = System.currentTimeMillis()
        val startTime = endTime - (windowMinutes * 60 * 1000)

        // queryEvents() gives individual transitions, not aggregates
        val events = usageStatsManager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()

        // Collect every ACTIVITY_RESUMED (foreground) event
        data class AppOpen(val packageName: String, val timestamp: Long)
        val opens = mutableListOf<AppOpen>()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
                && event.packageName !in systemPackages) {
                opens.add(AppOpen(event.packageName, event.timeStamp))
            }
        }

        if (opens.isEmpty()) return null

        val uniqueApps = opens.distinctBy { it.packageName }.size
        val switchRate = opens.size.toFloat() / windowMinutes

        // Avg dwell: time between consecutive opens as proxy
        val dwellTimes = opens.zipWithNext()
            .map { (a, b) -> b.timestamp - a.timestamp }
        val avgDwellMs = if (dwellTimes.isEmpty()) 60_000L
                         else dwellTimes.average().toLong()

        // returnCount: apps opened more than once — now actually meaningful
        val returnCount = opens.groupBy { it.packageName }
            .count { (_, list) -> list.size > 1 }

        val hour = java.util.Calendar.getInstance()
            .get(java.util.Calendar.HOUR_OF_DAY)

        return WindowSnapshot(switchRate, avgDwellMs, uniqueApps, returnCount, hour)
    }

    // ─── Evaluate snapshot against baseline ──────────────────────────────────

    private fun evaluate(snapshot: WindowSnapshot): Boolean {
        val dayCount = prefs.getInt("total_days_recorded", 0)

        return if (dayCount < 7) {
            // Learning phase: fixed conservative thresholds
            evaluateFixed(snapshot)
        } else {
            // Adaptive phase: your own baseline
            evaluateAdaptive(snapshot)
        }
    }

    /** Fixed thresholds used during the first 7 days. */
    private fun evaluateFixed(s: WindowSnapshot): Boolean {
        val tooManyApps    = s.uniqueApps >= 5
        val veryShortDwell = s.avgDwellMs < 45_000   // under 45 seconds average
        val compulsive     = s.returnCount >= 3

        return (tooManyApps && veryShortDwell) || compulsive
    }

    /**
     * Adaptive threshold: fire when current behavior exceeds
     * mean + 1.5 * stddev of YOUR historical switch rate,
     * OR dwell time drops below mean - 1.5 * stddev.
     *
     * Uses time-of-day bucket so morning ≠ evening baseline.
     */
    private fun evaluateAdaptive(snapshot: WindowSnapshot): Boolean {
        val bucket = timeBucket(snapshot.hourOfDay)
        val baseline = loadBaseline(bucket) ?: return evaluateFixed(snapshot)

        if (baseline.sampleCount < 10) return evaluateFixed(snapshot)

        val switchRateThreshold = baseline.switchRateMean + (1.5 * baseline.switchRateStd)
        val dwellThreshold      = baseline.avgDwellMsMean - (1.5 * baseline.avgDwellMsStd)

        val anomalousSwitch = snapshot.switchRate > switchRateThreshold
        val anomalousDwell  = snapshot.avgDwellMs  < dwellThreshold
        val compulsive      = snapshot.returnCount >= 3

        android.util.Log.d("AdaptiveDetector",
            "Bucket: $bucket | switchRate: ${snapshot.switchRate} > threshold: $switchRateThreshold = $anomalousSwitch | " +
            "dwell: ${snapshot.avgDwellMs} < threshold: $dwellThreshold = $anomalousDwell"
        )

        return (anomalousSwitch && anomalousDwell) || compulsive
    }

    // ─── Learning: record snapshots and recompute baselines ──────────────────

    /**
     * Stores each snapshot. Recomputes baseline every 20 snapshots.
     * Uses a rolling window of last 200 snapshots to adapt to behavior changes over time.
     */
    private fun recordSnapshot(snapshot: WindowSnapshot) {
        val bucket = timeBucket(snapshot.hourOfDay)
        val key = "snapshots_$bucket"

        val existing = try {
            JSONArray(prefs.getString(key, "[]"))
        } catch (e: Exception) { JSONArray() }

        val entry = JSONObject().apply {
            put("switchRate", snapshot.switchRate.toDouble())
            put("avgDwellMs", snapshot.avgDwellMs)
            put("timestamp", System.currentTimeMillis())
        }
        existing.put(entry)

        // Rolling window: keep last 200 samples per bucket
        val length = existing.length()
        val trimmed = if (length > 200) {
            val startIndex = length - 200
            JSONArray((startIndex until length).map { existing.getJSONObject(it) })
        } else existing

        prefs.edit().putString(key, trimmed.toString()).apply()

        // Recompute baseline every 20 new samples
        val totalSamples = prefs.getInt("total_samples_$bucket", 0) + 1
        prefs.edit().putInt("total_samples_$bucket", totalSamples).apply()

        if (totalSamples % 20 == 0) recomputeBaseline(bucket)

        // Track days
        val today = java.text.SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
            .format(java.util.Date())
        val knownDays = (prefs.getStringSet("known_days", mutableSetOf()) ?: mutableSetOf()).toMutableSet()
        if (knownDays.add(today)) {
            prefs.edit()
                .putStringSet("known_days", knownDays)
                .putInt("total_days_recorded", knownDays.size)
                .apply()
        }
    }

    private fun recomputeBaseline(bucket: String) {
        val key = "snapshots_$bucket"
        val snapshots = try {
            JSONArray(prefs.getString(key, "[]"))
        } catch (e: Exception) { return }

        if (snapshots.length() < 10) return

        val switchRates = (0 until snapshots.length())
            .map { snapshots.getJSONObject(it).getDouble("switchRate") }
        val dwellTimes = (0 until snapshots.length())
            .map { snapshots.getJSONObject(it).getLong("avgDwellMs").toDouble() }

        val baseline = Baseline(
            switchRateMean  = switchRates.mean(),
            switchRateStd   = switchRates.stdDev(),
            avgDwellMsMean  = dwellTimes.mean(),
            avgDwellMsStd   = dwellTimes.stdDev(),
            sampleCount     = snapshots.length()
        )

        saveBaseline(bucket, baseline)

        android.util.Log.d("AdaptiveDetector",
            "Baseline updated [$bucket]: switchRate=${baseline.switchRateMean}±${baseline.switchRateStd} " +
            "dwell=${baseline.avgDwellMsMean}±${baseline.avgDwellMsStd} (n=${baseline.sampleCount})"
        )
    }

    // ─── Baseline persistence ─────────────────────────────────────────────────

    private fun saveBaseline(bucket: String, baseline: Baseline) {
        val json = JSONObject().apply {
            put("switchRateMean",  baseline.switchRateMean)
            put("switchRateStd",   baseline.switchRateStd)
            put("avgDwellMsMean",  baseline.avgDwellMsMean)
            put("avgDwellMsStd",   baseline.avgDwellMsStd)
            put("sampleCount",     baseline.sampleCount)
        }
        prefs.edit().putString("baseline_$bucket", json.toString()).apply()
    }

    private fun loadBaseline(bucket: String): Baseline? {
        val json = try {
            JSONObject(prefs.getString("baseline_$bucket", "") ?: return null)
        } catch (e: Exception) { return null }

        return Baseline(
            switchRateMean  = json.getDouble("switchRateMean"),
            switchRateStd   = json.getDouble("switchRateStd"),
            avgDwellMsMean  = json.getDouble("avgDwellMsMean"),
            avgDwellMsStd   = json.getDouble("avgDwellMsStd"),
            sampleCount     = json.getInt("sampleCount")
        )
    }

    // ─── Stats helpers ────────────────────────────────────────────────────────

    private fun List<Double>.mean(): Double = if (isEmpty()) 0.0 else sum() / size

    private fun List<Double>.stdDev(): Double {
        if (size < 2) return 0.0
        val m = mean()
        return sqrt(map { (it - m) * (it - m) }.sum() / (size - 1))
    }

    // ─── Debug: expose current state ─────────────────────────────────────────

    fun debugState(): String {
        val days = prefs.getInt("total_days_recorded", 0)
        val phase = if (days < 7) "LEARNING (day $days/7)" else "ADAPTIVE"
        val buckets = listOf("morning", "afternoon", "evening", "night")
        val baselineInfo = buckets.joinToString("\n") { bucket ->
            val b = loadBaseline(bucket)
            val samples = prefs.getInt("total_samples_$bucket", 0)
            if (b != null)
                "  $bucket: switchRate=${String.format("%.2f", b.switchRateMean)}±${String.format("%.2f", b.switchRateStd)}, " +
                "dwell=${b.avgDwellMsMean.toLong()}ms±${b.avgDwellMsStd.toLong()}ms (n=$samples)"
            else
                "  $bucket: no baseline yet ($samples samples)"
        }
        return "Phase: $phase\nBaselines:\n$baselineInfo"
    }
}
