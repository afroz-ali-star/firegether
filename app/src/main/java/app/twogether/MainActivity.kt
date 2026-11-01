package app.twogether

import android.Manifest
import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

val Pink = Color(0xFFFF5D8F); val Lilac = Color(0xFFB583FF); val Peach = Color(0xFFFF9A8B)

class MainActivity : ComponentActivity() {
    override fun onCreate(b: Bundle?) {
        super.onCreate(b); enableEdgeToEdge()
        setContent {
            val dark = isSystemInDarkTheme()
            MaterialTheme(colorScheme = if (dark) darkColorScheme(primary = Pink, surface = Color(0xFF2B1A26), onSurface = Color(0xFFFFE9F0),
                background = Color(0xFF1D1019), onBackground = Color(0xFFFFE9F0), onSurfaceVariant = Color(0xFFC3A0B0))
            else lightColorScheme(primary = Pink, surface = Color.White, onSurface = Color(0xFF3D1F2B),
                background = Color(0xFFFFF1F4), onBackground = Color(0xFF3D1F2B), onSurfaceVariant = Color(0xFF8A6A76))) { Root { App() } }
        }
    }
}

@Composable fun mute() = MaterialTheme.colorScheme.onSurfaceVariant

// ---------- visual building blocks ----------
@Composable fun Root(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val t = rememberInfiniteTransition(label = "bg")
    val s by t.animateFloat(0f, 1f, infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse), label = "s")
    val a = if (dark) Color(0xFF1D1019) else Color(0xFFFFF1F4); val b = if (dark) Color(0xFF2A1633) else Color(0xFFF3E9FF)
    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(a, lerp(a, b, s), b)))) {
        FloatingHearts()
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onBackground) { content() }
    }
}

@Composable fun FloatingHearts() {
    val t = rememberInfiniteTransition(label = "fh")
    BoxWithConstraints(Modifier.fillMaxSize()) {
        repeat(7) { i ->
            val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(7000 + i * 900, easing = LinearEasing), initialStartOffset = StartOffset(i * 800)), label = "p$i")
            Text("\u2764", color = Pink.copy(alpha = (1f - p) * .22f), fontSize = (14 + i * 3).sp,
                modifier = Modifier.offset(x = maxWidth * (0.05f + 0.13f * i), y = maxHeight * (1f - p)))
        }
    }
}

@Composable fun Radar(modifier: Modifier = Modifier) {
    val t = rememberInfiniteTransition(label = "r")
    val ps = (0..2).map { t.animateFloat(0f, 1f, infiniteRepeatable(tween(2400, easing = LinearEasing), initialStartOffset = StartOffset(it * 800)), label = "r$it") }
    Canvas(modifier) {
        ps.forEach { val p = it.value; drawCircle(Pink.copy(alpha = (1f - p) * .55f), radius = size.minDimension / 2 * p, style = Stroke(4.dp.toPx())) }
        drawCircle(Pink, radius = 14.dp.toPx())
    }
}

@Composable fun LinkLine(active: Boolean) {
    val t = rememberInfiniteTransition(label = "ll")
    val p by t.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing)), label = "p")
    Canvas(Modifier.fillMaxWidth().height(44.dp)) {
        val y = size.height / 2; val x0 = 16.dp.toPx(); val x1 = size.width - 16.dp.toPx()
        drawLine(Pink.copy(alpha = .35f), Offset(x0, y), Offset(x1, y), strokeWidth = 4.dp.toPx(), cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f)))
        drawCircle(Pink, 10.dp.toPx(), Offset(x0, y)); drawCircle(Lilac, 10.dp.toPx(), Offset(x1, y))
        if (active) drawCircle(Peach, 6.dp.toPx(), Offset(x0 + (x1 - x0) * p, y))
    }
}

@Composable fun Ring(progress: Float, big: String, small: String) {
    val a by animateFloatAsState(progress, tween(1400, easing = FastOutSlowInEasing), label = "ring")
    Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = 14.dp.toPx(); val st = Stroke(w, cap = StrokeCap.Round)
            val tl = Offset(w / 2, w / 2); val sz = Size(size.width - w, size.height - w)
            drawArc(Pink.copy(alpha = .15f), 0f, 360f, false, tl, sz, style = st)
            drawArc(brush = Brush.sweepGradient(listOf(Pink, Lilac, Pink)), startAngle = -90f, sweepAngle = 360f * a, useCenter = false, topLeft = tl, size = sz, style = st)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(big, fontSize = 38.sp, fontWeight = FontWeight.ExtraBold, color = Pink); Text(small, fontSize = 12.sp, color = mute())
        }
    }
}

@Composable fun Appear(i: Int, content: @Composable () -> Unit) {
    var v by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(i * 90L); v = true }
    AnimatedVisibility(v, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 4 }) { content() }
}

@Composable fun GradBtn(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    val src = remember { MutableInteractionSource() }; val pr by src.collectIsPressedAsState()
    val sc by animateFloatAsState(if (pr) .95f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "b")
    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp).height(54.dp).scale(sc).clip(RoundedCornerShape(18.dp))
        .background(Brush.horizontalGradient(if (enabled) listOf(Pink, Peach) else listOf(Color.Gray, Color.LightGray)))
        .clickable(interactionSource = src, indication = null, enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable fun Card2(content: @Composable ColumnScope.() -> Unit) = Card(
    Modifier.fillMaxWidth().padding(vertical = 7.dp), shape = RoundedCornerShape(26.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .88f)),
    elevation = CardDefaults.cardElevation(6.dp)
) { Column(Modifier.padding(18.dp), content = content) }

// ---------- app ----------
@Composable fun App() {
    val c = LocalContext.current; val p = U.prefs(c)
    var pair by remember { mutableStateOf(p.getString("pair", null)) }
    if (FirebaseAuth.getInstance().currentUser == null || pair == null) Login { pair = it }
    else Home {
        FirebaseAuth.getInstance().signOut(); c.stopService(Intent(c, LocService::class.java))
        p.edit().clear().apply(); pair = null
    }
}

@Composable fun Login(done: (String) -> Unit) {
    var step by remember { mutableStateOf(0) }
    var n by remember { mutableStateOf("") }; var ph by remember { mutableStateOf("") }
    var pp by remember { mutableStateOf("") }; var pw by remember { mutableStateOf("") }
    var err by remember { mutableStateOf("") }; var busy by remember { mutableStateOf(false) }
    val sc = rememberCoroutineScope(); val c = LocalContext.current
    fun clean(s: String) = s.filter { it.isDigit() || it == '+' }
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Twogether \uD83D\uDC9E", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Pink)
        Text("A private world for exactly two.", color = mute())
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(2) { i ->
                val w by animateDpAsState(if (i == step) 40.dp else 14.dp, spring(), label = "dot")
                Box(Modifier.height(8.dp).width(w).clip(RoundedCornerShape(8.dp)).background(if (i <= step) Pink else Pink.copy(alpha = .25f)))
            }
        }
        Spacer(Modifier.height(18.dp))
        AnimatedContent(step, transitionSpec = {
            if (targetState > initialState) (slideInHorizontally { it } + fadeIn()) togetherWith (slideOutHorizontally { -it } + fadeOut())
            else (slideInHorizontally { -it } + fadeIn()) togetherWith (slideOutHorizontally { it } + fadeOut())
        }, label = "step") { s ->
            Column {
                if (s == 0) {
                    Text("About you", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    OutlinedTextField(n, { n = it }, label = { Text("Your name") }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp))
                    OutlinedTextField(ph, { ph = it }, label = { Text("Your phone number") }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp))
                    OutlinedTextField(pw, { pw = it }, label = { Text("Password (6+ characters)") }, singleLine = true, shape = RoundedCornerShape(16.dp),
                        visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp))
                } else {
                    Text("Link with your love", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Enter their phone number. When they enter yours, your hearts connect.", color = mute(), fontSize = 13.sp)
                    OutlinedTextField(pp, { pp = it }, label = { Text("Partner's phone number") }, singleLine = true, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                }
            }
        }
        AnimatedVisibility(err.isNotEmpty()) { Text(err, color = Color(0xFFE0335F), modifier = Modifier.padding(top = 6.dp)) }
        Spacer(Modifier.height(8.dp))
        if (step == 0) GradBtn("Next") {
            if (n.isBlank() || clean(ph).length < 6 || pw.length < 6) err = "Add your name, phone number and a 6+ character password." else { err = ""; step = 1 }
        } else {
            GradBtn(if (busy) "Linking\u2026" else "Link our hearts", enabled = !busy) {
                val a = clean(ph); val b = clean(pp)
                if (b.length < 6 || a == b) { err = "Enter your partner's phone number (different from yours)."; return@GradBtn }
                busy = true; err = ""
                sc.launch {
                    try {
                        val auth = FirebaseAuth.getInstance(); val email = "$a@twogether.app"
                        try { auth.signInWithEmailAndPassword(email, pw).await() }
                        catch (e: Exception) {
                            try { auth.createUserWithEmailAndPassword(email, pw).await() } catch (e2: Exception) { throw Exception("Wrong password for this number.") }
                        }
                        val ids = listOf(a, b).sorted(); val pair = ids.joinToString("_")
                        val ref = FirebaseFirestore.getInstance().collection("couples").document(pair)
                        if (!ref.get().await().exists()) ref.set(mapOf("members" to ids, "start" to "", "early" to 3)).await()
                        U.prefs(c).edit().putString("pair", pair).putString("phone", a).putString("name", n.trim()).putString("partner", b).apply()
                        done(pair)
                    } catch (e: Exception) { err = e.message ?: "Could not connect" }
                    busy = false
                }
            }
            TextButton({ step = 0 }, Modifier.fillMaxWidth()) { Text("Back") }
        }
    }
}

@Composable fun Waiting(me: String, partner: String, onOut: () -> Unit) {
    Column(Modifier.fillMaxSize().systemBarsPadding().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Radar(Modifier.size(230.dp))
        Spacer(Modifier.height(24.dp))
        Text("Waiting for your love\u2026", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Pink)
        Text("Ask $partner to install Twogether and enter your number:", textAlign = TextAlign.Center, color = mute(), modifier = Modifier.padding(top = 8.dp))
        Text(me, fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
        TextButton(onOut) { Text("Log out") }
    }
}

@Composable fun Home(onOut: () -> Unit) {
    val c = LocalContext.current; val p = U.prefs(c); val db = FirebaseFirestore.getInstance()
    val pair = p.getString("pair", "") ?: ""; val me = p.getString("phone", "") ?: ""
    var start by remember { mutableStateOf(p.getString("start", "") ?: "") }
    var early by remember { mutableStateOf(p.getInt("early", 3)) }
    var dist by remember { mutableStateOf<Float?>(null) }
    var pname by remember { mutableStateOf("your love") }
    var seen by remember { mutableStateOf(0L) }
    var linked by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf(0) }
    val pulse by rememberInfiniteTransition(label = "h").animateFloat(1f, 1.3f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "s")

    DisposableEffect(pair) {
        ReminderWorker.schedule(c)
        db.document("couples/$pair/loc/$me").set(mapOf("name" to (p.getString("name", "") ?: ""), "joined" to System.currentTimeMillis()), SetOptions.merge())
        val r1 = db.collection("couples").document(pair).addSnapshotListener { s, _ ->
            s?.let { start = it.getString("start") ?: ""; early = (it.getLong("early") ?: 3).toInt()
                p.edit().putString("start", start).putInt("early", early).apply(); DistanceWidget.refresh(c) }
        }
        val r2 = db.collection("couples/$pair/loc").addSnapshotListener { q, _ ->
            val m = q?.documents?.firstOrNull { it.id == me }; val o = q?.documents?.firstOrNull { it.id != me }
            linked = o != null
            if (o != null) { pname = o.getString("name") ?: pname; seen = o.getLong("ts") ?: 0L }
            val a = m?.getDouble("lat"); val b = m?.getDouble("lng"); val x = o?.getDouble("lat"); val y = o?.getDouble("lng")
            dist = if (a != null && b != null && x != null && y != null) { val r = FloatArray(1); Location.distanceBetween(a, b, x, y, r); r[0] } else null
        }
        onDispose { r1.remove(); r2.remove() }
    }
    val perm = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r[Manifest.permission.ACCESS_FINE_LOCATION] == true) ContextCompat.startForegroundService(c, Intent(c, LocService::class.java))
    }
    if (!linked) { Waiting(me, p.getString("partner", "your partner") ?: "your partner", onOut); return }

    val nxt = U.next(start); val days = U.days(start)
    val shown by animateFloatAsState(dist ?: 0f, tween(900), label = "d")
    Scaffold(containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onBackground, bottomBar = {
        NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .92f)) {
            listOf("\uD83C\uDFE0" to "Home", "\uD83D\uDC91" to "Us", "\u2699\uFE0F" to "More").forEachIndexed { i, (ic, l) ->
                NavigationBarItem(tab == i, { tab = i }, { Text(ic, fontSize = 20.sp) }, label = { Text(l) })
            }
        }
    }) { pad ->
        AnimatedContent(tab, Modifier.padding(pad), transitionSpec = { fadeIn(tween(300)) + scaleIn(initialScale = .96f) togetherWith fadeOut(tween(150)) }, label = "tab") { t ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding().padding(18.dp)) {
                when (t) {
                    0 -> {
                        Appear(0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("You \u2764 $pname", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Pink)
                                    Text("Last seen ${U.ago(seen)}", fontSize = 12.sp, color = mute())
                                }
                                Text("\u2764", fontSize = 28.sp, color = Pink, modifier = Modifier.scale(pulse))
                            }
                        }
                        Appear(1) {
                            Card2 {
                                Text("DISTANCE BETWEEN YOU", fontSize = 12.sp, color = mute())
                                Text(if (dist == null) "--" else U.dist(shown), fontSize = 58.sp, fontWeight = FontWeight.ExtraBold, color = Pink)
                                LinkLine(dist != null)
                                Text(when {
                                    dist == null -> "Both of you need to share location"
                                    dist!! < 50 -> "You're together right now \uD83E\uDD70"
                                    else -> "About ${U.eta(dist!!)} apart by car \u2022 live"
                                }, color = mute(), fontSize = 13.sp)
                            }
                        }
                        Appear(2) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(Modifier.weight(1f)) { Card2 { Text("DAYS TOGETHER", fontSize = 11.sp, color = mute()); Text(days?.toString() ?: "--", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Lilac) } }
                                Box(Modifier.weight(1f)) { Card2 { Text("NEXT MONTHLY", fontSize = 11.sp, color = mute()); Text(nxt?.let { "${it.third}d" } ?: "--", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = Peach) } }
                            }
                        }
                        Appear(3) { GradBtn("Share live location & enable alerts") { perm.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)) } }
                    }
                    1 -> {
                        Appear(0) { Card2 { Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Next monthly anniversary", color = mute(), fontSize = 12.sp); Spacer(Modifier.height(10.dp))
                            Ring(U.progress(start), nxt?.let { "${it.third}d" } ?: "--", nxt?.let { "to month ${it.second}" } ?: "set day one")
                        } } }
                        Appear(1) {
                            Card2 {
                                val ms = days?.let { U.milestone(it) }
                                Text("Next milestone", color = mute(), fontSize = 12.sp)
                                Text(ms?.let { "Day ${it.first} \u2022 in ${it.second} days \uD83C\uDF89" } ?: "Set your day one to unlock milestones", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Appear(2) {
                            Card2 {
                                Text("Our day one", fontWeight = FontWeight.Bold); Text(start.ifEmpty { "Not set" }, color = mute())
                                OutlinedButton({ DatePickerDialog(c, { _, y, m, d -> db.collection("couples").document(pair).update("start", "%04d-%02d-%02d".format(y, m + 1, d)) }, 2024, 0, 1).show() },
                                    shape = RoundedCornerShape(14.dp)) { Text("Pick date") }
                                Spacer(Modifier.height(10.dp)); Text("Early reminder", fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf(0, 1, 3, 7).forEach { e -> FilterChip(early == e, { db.collection("couples").document(pair).update("early", e) }, { Text(if (e == 0) "Day of" else "$e d") }) }
                                }
                            }
                        }
                    }
                    else -> {
                        Appear(0) { Text("More", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Pink) }
                        Appear(1) {
                            Card2 {
                                Text("Home-screen widget", fontWeight = FontWeight.Bold)
                                Text("Live distance and days together, always on your home screen.", color = mute(), fontSize = 13.sp)
                                GradBtn("Add widget") {
                                    val m = AppWidgetManager.getInstance(c)
                                    if (m.isRequestPinAppWidgetSupported) m.requestPinAppWidget(ComponentName(c, DistanceWidget::class.java), null, null)
                                }
                            }
                        }
                        Appear(2) {
                            Card2 {
                                Text("Background sharing", fontWeight = FontWeight.Bold)
                                Text("If sharing stops on your phone, set Twogether battery usage to Unrestricted.", color = mute(), fontSize = 13.sp)
                                GradBtn("Allow location & alerts") { perm.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.POST_NOTIFICATIONS)) }
                            }
                        }
                        Appear(3) { TextButton(onOut, Modifier.fillMaxWidth()) { Text("Log out") } }
                    }
                }
            }
        }
    }
}
