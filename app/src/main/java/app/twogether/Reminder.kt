package app.twogether

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.time.LocalDate
import java.util.concurrent.TimeUnit

class ReminderWorker(c: Context, p: WorkerParameters) : Worker(c, p) {
    override fun doWork(): Result {
        val c = applicationContext; val p = U.prefs(c)
        val n = U.next(p.getString("start", null)) ?: return Result.success()
        val early = p.getInt("early", 3)
        val key = "${LocalDate.now()}-${n.third}"
        val text = when {
            n.third == 0L -> "Happy ${n.second}-month anniversary! \uD83D\uDC96"
            early > 0 && n.third == early.toLong() -> "$early days until your ${n.second}-month anniversary \uD83C\uDF81"
            else -> null
        }
        if (text != null && p.getString("lastRem", "") != key) {
            p.edit().putString("lastRem", key).apply()
            val nm = c.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel("rem", "Reminders", NotificationManager.IMPORTANCE_HIGH))
            nm.notify(2, NotificationCompat.Builder(c, "rem").setSmallIcon(R.drawable.ic_heart)
                .setContentTitle("Twogether").setContentText(text).setAutoCancel(true).build())
        }
        DistanceWidget.refresh(c)
        return Result.success()
    }
    companion object {
        fun schedule(c: Context) = WorkManager.getInstance(c).enqueueUniquePeriodicWork(
            "rem", ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<ReminderWorker>(6, TimeUnit.HOURS).build())
    }
}
