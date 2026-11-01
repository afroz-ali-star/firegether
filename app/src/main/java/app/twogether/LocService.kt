package app.twogether

import android.app.*
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/** Foreground service: shares my location and keeps distance + widget fresh in the background. */
class LocService : Service() {
    private val db = FirebaseFirestore.getInstance()
    private var reg: ListenerRegistration? = null
    private lateinit var client: FusedLocationProviderClient
    private var mine: Location? = null
    private var other: Location? = null
    private val cb = object : LocationCallback() {
        override fun onLocationResult(r: LocationResult) {
            val l = r.lastLocation ?: return; mine = l
            val p = U.prefs(this@LocService)
            db.document("couples/${p.getString("pair", "")}/loc/${p.getString("phone", "")}").set(
                mapOf("lat" to l.latitude, "lng" to l.longitude, "ts" to System.currentTimeMillis(),
                    "name" to p.getString("name", "")))
            update()
        }
    }
    private fun update() {
        val a = mine ?: return; val b = other ?: return
        U.prefs(this).edit().putFloat("dist", a.distanceTo(b)).apply()
        DistanceWidget.refresh(this)
    }
    override fun onStartCommand(i: Intent?, f: Int, id: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("loc", "Live distance", NotificationManager.IMPORTANCE_LOW))
        val n = NotificationCompat.Builder(this, "loc").setSmallIcon(R.drawable.ic_heart)
            .setContentTitle("Twogether").setContentText("Sharing live distance with your love").setOngoing(true).build()
        if (Build.VERSION.SDK_INT >= 29) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION) else startForeground(1, n)
        val p = U.prefs(this); val me = p.getString("phone", "") ?: ""
        reg?.remove()
        reg = db.collection("couples/${p.getString("pair", "")}/loc").addSnapshotListener { q, _ ->
            q?.documents?.firstOrNull { it.id != me }?.let {
                val la = it.getDouble("lat") ?: return@let; val lo = it.getDouble("lng") ?: return@let
                other = Location("x").apply { latitude = la; longitude = lo }
                p.edit().putString("pname", it.getString("name")).apply(); update()
            }
        }
        client = LocationServices.getFusedLocationProviderClient(this)
        val req = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 15000).setMinUpdateDistanceMeters(10f).build()
        try { client.requestLocationUpdates(req, cb, Looper.getMainLooper()) } catch (_: SecurityException) { stopSelf() }
        return START_STICKY
    }
    override fun onDestroy() { reg?.remove(); if (::client.isInitialized) client.removeLocationUpdates(cb) }
    override fun onBind(i: Intent?): IBinder? = null
}
