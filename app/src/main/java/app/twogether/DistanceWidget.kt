package app.twogether

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews

class DistanceWidget : AppWidgetProvider() {
    override fun onUpdate(c: Context, m: AppWidgetManager, ids: IntArray) = refresh(c)
    companion object {
        fun refresh(c: Context) {
            val p = U.prefs(c); val m = AppWidgetManager.getInstance(c)
            val ids = m.getAppWidgetIds(ComponentName(c, DistanceWidget::class.java))
            val v = RemoteViews(c.packageName, R.layout.widget_distance)
            val d = p.getFloat("dist", -1f)
            v.setTextViewText(R.id.who, "\u2764 ${p.getString("pname", "Your love")}")
            v.setTextViewText(R.id.dist, if (d < 0) "--" else U.dist(d))
            v.setTextViewText(R.id.days, U.days(p.getString("start", null))?.let { "$it days together" } ?: "Set your day one")
            ids.forEach { m.updateAppWidget(it, v) }
        }
    }
}
