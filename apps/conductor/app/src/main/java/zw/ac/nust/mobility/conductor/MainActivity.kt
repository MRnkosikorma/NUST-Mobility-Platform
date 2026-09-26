package zw.ac.nust.mobility.conductor

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay

// ══════════════════════════════════════════════════════════════════════════════
//  TYPOGRAPHY — Roboto Bold (system SansSerif = Roboto on Android)
// ══════════════════════════════════════════════════════════════════════════════
private val Roboto = FontFamily.SansSerif

private val NustTypography = Typography(
    displayLarge   = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 57.sp),
    headlineLarge  = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 28.sp),
    titleLarge     = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 22.sp),
    titleMedium    = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 16.sp),
    bodyLarge      = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium     = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge     = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 14.sp),
    labelMedium    = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 12.sp),
    labelSmall     = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 11.sp),
)

// ══════════════════════════════════════════════════════════════════════════════
//  COLOUR SYSTEM — NUST brand
// ══════════════════════════════════════════════════════════════════════════════
private val NUSTBlue       = Color(0xFF0C6CA4)
private val NUSTSky        = Color(0xFF00C6FB)
private val Background     = Color(0xFF060C1A)
private val SurfaceCard    = Color(0xFF0F1829)
private val GlassWhite     = Color(0x14FFFFFF)
private val CardEdge       = Color(0xFF1A2540)
private val OnLight        = Color(0xFFFFFFFF)
private val Muted          = Color(0xFFB0BBC8)
private val GreenOk        = Color(0xFF10B981)
private val RedAlert       = Color(0xFFEF4444)
private val Amber          = Color(0xFFF59E0B)
private val AmberSurface   = Color(0xFF1C1600)
private val OfflineBanner  = Color(0xFFF59E0B)   // amber for offline header

private val SchemeConductDark = darkColorScheme(
    primary      = NUSTSky,
    onPrimary    = Color.Black,
    secondary    = NUSTBlue,
    background   = Background,
    surface      = SurfaceCard,
    onBackground = OnLight,
    onSurface    = OnLight,
    surfaceVariant = CardEdge,
    onSurfaceVariant = Muted
)

private val SchemeConductLight = androidx.compose.material3.lightColorScheme(
    primary      = NUSTBlue,
    onPrimary    = Color.White,
    secondary    = Color(0xFF0096C7),
    background   = Color(0xFFF4F6F9),
    surface      = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111827),
    onSurface    = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF4B5563)
)

// Shared gradient brushes
private val HeroBrush get() = Brush.linearGradient(listOf(NUSTBlue, NUSTSky))
private val CardBrush get() = Brush.linearGradient(listOf(NUSTSky.copy(alpha = 0.5f), CardEdge))

// ══════════════════════════════════════════════════════════════════════════════
//  NFC TRANSACTION STATES  (mirrors the 6-step flow diagram)
// ══════════════════════════════════════════════════════════════════════════════
enum class NfcFlowStep {
    READY,           // 1 — static ring, "Waiting for Student Tap"
    WAITING,         // 2 — animated pulsing ring, "Waiting for Tap…"
    CARD_DETECTED,   // 3 — card icon in ring, "Reading credential…"
    VERIFYING,       // 4 — progress bar, "Verifying Student…"
    PROCESSING,      // 5 — fare display + progress, "Processing Payment…"
    SUCCESS          // 6 — green circle, "Payment Successful"
}

// ══════════════════════════════════════════════════════════════════════════════
//  HAPTIC HELPER
// ══════════════════════════════════════════════════════════════════════════════
private fun vibrate(ctx: android.content.Context, pattern: LongArray) {
    try {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            (ctx.getSystemService(android.content.Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                .defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            (ctx.getSystemService(android.content.Context.VIBRATOR_SERVICE) as Vibrator)
                .vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    } catch (_: Exception) {}
}

// ══════════════════════════════════════════════════════════════════════════════
//  ACTIVITY
// ══════════════════════════════════════════════════════════════════════════════
class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private val vm: ConductorViewModel by viewModels()
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setContent {
            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }
            val colors = if (isDarkTheme) SchemeConductDark else SchemeConductLight
            MaterialTheme(colorScheme = colors, typography = NustTypography) {
                Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
                    ConductorApp(vm, isDarkTheme = isDarkTheme, onToggleTheme = { isDarkTheme = !isDarkTheme })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(this, this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) return
        var processed = false
        val isoDep = IsoDep.get(tag)
        if (isoDep != null) {
            try {
                isoDep.connect()
                val sel = byteArrayOf(0x00,0xA4.toByte(),0x04,0x00,0x07,
                    0xF0.toByte(),0x01,0x02,0x03,0x04,0x05,0x06)
                val resp = isoDep.transceive(sel)
                if (resp != null && resp.size >= 2 &&
                    resp[resp.size-2] == 0x90.toByte() && resp[resp.size-1] == 0x00.toByte()) {
                    val payload = String(resp.copyOfRange(0, resp.size-2), Charsets.UTF_8)
                    runOnUiThread { vm.processDemoCredential(payload) }
                    processed = true
                }
                isoDep.close()
            } catch (e: Exception) { Log.d("NFC", "ISO-DEP: ${e.message}") }
        }
        if (!processed) {
            tag.id?.let { uid ->
                if (uid.isNotEmpty())
                    runOnUiThread { vm.processDemoCredential("card_uid:${uid.joinToString("") { "%02X".format(it) }}") }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  ROOT APP
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConductorApp(
    vm: ConductorViewModel = viewModel(),
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val statusColor by animateColorAsState(
        targetValue   = if (vm.connectivity == Connectivity.ONLINE) GreenOk else Amber,
        animationSpec = tween(500), label = "conn"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = { ConductorTopBar(vm, statusColor, isDarkTheme, onToggleTheme) },
            bottomBar = {
                ConductorBottomBar(
                    selectedTab      = vm.selectedTab,
                    pendingCount     = vm.pending.size,
                    vehicleConfirmed = vm.vehicleConfirmed,
                    onTabSelected    = { idx ->
                        if (idx == 0 && !vm.vehicleConfirmed) return@ConductorBottomBar
                        vm.selectedTab = idx
                    }
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).background(Background)) {
                when (vm.selectedTab) {
                    0 -> DashboardScreen(vm)
                    1 -> VehicleSelectionScreen(vm)
                    2 -> OfflineQueueScreen(vm)
                }
            }
        }

        // ── Full-screen tap result overlay ────────────────────────────────────
        AnimatedVisibility(
            visible  = vm.showResultOverlay,
            enter    = fadeIn(tween(150)) + scaleIn(tween(150), initialScale = 0.94f),
            exit     = fadeOut(tween(250)) + scaleOut(tween(250), targetScale = 0.97f),
            modifier = Modifier.fillMaxSize()
        ) {
            val (bg, icon, headline, sub) = when (vm.outcome) {
                ScanOutcome.CONFIRMED   -> Quad(GreenOk, "✓", "BOARDED",  "Fare debited — ${vm.context.fareLabel}")
                ScanOutcome.DECLINED    -> Quad(RedAlert, "✗", "DECLINED", vm.declineReason ?: "Invalid credential")
                ScanOutcome.PROVISIONAL -> Quad(Amber, "◌", "QUEUED",   "Offline — syncs when connected (${vm.pending.size} pending)")
                ScanOutcome.READY       -> Quad(Color.Transparent, "", "", "")
            }
            LaunchedEffect(vm.showResultOverlay) {
                if (!vm.showResultOverlay) return@LaunchedEffect
                val pattern = when (vm.outcome) {
                    ScanOutcome.CONFIRMED   -> longArrayOf(0, 60)
                    ScanOutcome.DECLINED    -> longArrayOf(0, 100, 60, 100)
                    ScanOutcome.PROVISIONAL -> longArrayOf(0, 80)
                    else                   -> longArrayOf()
                }
                if (pattern.isNotEmpty()) vibrate(ctx, pattern)
                delay(2200L)
                vm.dismissResultOverlay()
            }
            Box(
                modifier         = Modifier.fillMaxSize().background(bg.copy(alpha = 0.94f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(40.dp)) {
                    Text(icon,     fontSize = 80.sp, color = OnLight, fontFamily = Roboto, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    Text(headline, fontSize = 36.sp, fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(8.dp))
                    Text(sub,      fontSize = 16.sp, fontFamily = Roboto, color = OnLight.copy(alpha = 0.85f), textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// Helper data class for overlay
private data class Quad(val bg: Color, val icon: String, val headline: String, val sub: String)

// ══════════════════════════════════════════════════════════════════════════════
//  TOP APP BAR
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConductorTopBar(
    vm: ConductorViewModel,
    statusColor: Color,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(HeroBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text(vm.conductorName.take(2).uppercase(), fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 14.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Mundo Conductor", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onBackground)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(statusColor))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (vm.connectivity == Connectivity.ONLINE) "Online" else "Offline",
                            fontFamily = Roboto, fontSize = 11.sp, color = statusColor
                        )
                        Spacer(Modifier.width(8.dp))
                        if (vm.context.registration.isNotEmpty()) {
                            Text("· ${vm.context.registration}", fontFamily = Roboto, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        },
        actions = {
            // Theme toggle button
            Box(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onToggleTheme() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(if (isDarkTheme) "☀️" else "🌙", fontSize = 14.sp)
            }
            // Online status chip
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (vm.connectivity == Connectivity.ONLINE) GreenOk else Amber)
                    .clickable { vm.toggleConnectivity() }
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    if (vm.connectivity == Connectivity.ONLINE) "Online" else "Offline",
                    fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = OnLight
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.onBackground)
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  BOTTOM NAVIGATION BAR
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun ConductorBottomBar(
    selectedTab: Int, pendingCount: Int,
    vehicleConfirmed: Boolean, onTabSelected: (Int) -> Unit
) {
    Box(
        modifier         = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier        = Modifier.fillMaxWidth().border(1.dp, CardEdge, RoundedCornerShape(32.dp)),
            shape           = RoundedCornerShape(32.dp),
            color           = SurfaceCard,
            shadowElevation = 8.dp
        ) {
            Row(Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                val locked = !vehicleConfirmed
                // Scanner
                NavPill(
                    modifier = Modifier.weight(1f),
                    label    = if (locked) "🔒 Scanner" else "Scanner",
                    selected = selectedTab == 0,
                    color    = if (locked) Muted.copy(0.4f) else null,
                    enabled  = !locked
                ) { onTabSelected(0) }
                // Vehicle
                NavPill(
                    modifier = Modifier.weight(1f),
                    label    = if (!vehicleConfirmed) "Vehicle ●" else "Vehicle",
                    selected = selectedTab == 1,
                    color    = if (!vehicleConfirmed) Amber else null
                ) { onTabSelected(1) }
                // Queue
                NavPill(
                    modifier = Modifier.weight(1f),
                    label    = if (pendingCount > 0) "Queue ($pendingCount)" else "Queue",
                    selected = selectedTab == 2,
                    color    = if (pendingCount > 0) Amber else null
                ) { onTabSelected(2) }
            }
        }
    }
}

@Composable
private fun NavPill(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    color: Color? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (selected && enabled) HeroBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontFamily = Roboto,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize   = 13.sp,
            color      = color ?: if (selected && enabled) OnLight else Muted
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SCREEN 1 — DASHBOARD  (Online mode — mirrors "Dashboard 01 of 04")
//  Now hosts the full 6-step NFC transaction flow diagram inline
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun DashboardScreen(vm: ConductorViewModel) {
    // Local NFC flow state — drives the 6-step animation
    var nfcStep by remember { mutableStateOf(NfcFlowStep.READY) }

    LazyColumn(
        modifier            = Modifier.fillMaxSize().padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        // Offline mode banner (shown only when offline)
        item {
            AnimatedVisibility(visible = vm.connectivity == Connectivity.OFFLINE) {
                OfflineBannerStrip(vm)
            }
        }

        // Connectivity + vehicle status row
        item { ConnectivityStatusBar(vm) }
        item { ActiveVehiclePill(vm) }

        // ── NFC Scanner Card — shows the 6-step flow state ────────────────────
        item {
            NfcScannerCard(
                vm     = vm,
                step   = nfcStep,
                onStep = { nfcStep = it }
            )
        }

        // Session stats
        item { SessionStatsRow(vm.pending.size) }

        item { PilotBanner() }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ── Offline banner strip (Figma "Offline Mode" amber header) ─────────────────
@Composable
private fun OfflineBannerStrip(vm: ConductorViewModel) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(OfflineBanner, RoundedCornerShape(14.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Mundo Conductor", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                Text("Offline Mode — Cached verification", fontFamily = Roboto, fontSize = 11.sp, color = Color.Black.copy(alpha = 0.7f))
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(Color.Black.copy(alpha = 0.15f))
                    .clickable { vm.syncOfflineBatch() }.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Retry Sync", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
            }
        }
    }
}

// ── Connectivity status bar ───────────────────────────────────────────────────
@Composable
private fun ConnectivityStatusBar(vm: ConductorViewModel) {
    val isOnline   = vm.connectivity == Connectivity.ONLINE
    val hasPending = vm.pending.isNotEmpty()
    val (bgColor, label) = when {
        isOnline && !hasPending -> NUSTBlue.copy(0.15f) to "Online — real-time validation active"
        isOnline && hasPending  -> GreenOk.copy(0.15f)  to "Syncing ${vm.pending.size} offline items…"
        else                    -> Amber.copy(0.15f)     to "Offline — ${vm.pending.size} queued. Connect to sync."
    }
    Row(
        modifier          = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bgColor).padding(14.dp, 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(if (isOnline) GreenOk else Amber))
        Spacer(Modifier.width(8.dp))
        Text(label, fontFamily = Roboto, fontSize = 12.sp, color = Muted, modifier = Modifier.weight(1f))
        if (!isOnline && hasPending) {
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(GreenOk)
                    .clickable { vm.syncOfflineBatch() }.padding(10.dp, 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Sync →", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnLight)
            }
        }
    }
}

// ── Active vehicle pill ───────────────────────────────────────────────────────
@Composable
private fun ActiveVehiclePill(vm: ConductorViewModel) {
    GlassCard {
        Row(
            modifier              = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp))
                        .background(HeroBrush),
                    contentAlignment = Alignment.Center
                ) { Text("🚌", fontSize = 18.sp) }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        if (vm.context.registration.isEmpty()) "No vehicle selected" else vm.context.registration,
                        fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 15.sp
                    )
                    Text(
                        if (vm.context.routeName.isEmpty()) "Go to Vehicle tab" else vm.context.routeName,
                        fontFamily = Roboto, fontSize = 11.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    if (vm.context.registration.isNotEmpty())
                        Text("Fare: ${vm.context.fareLabel}", fontFamily = Roboto, fontSize = 12.sp, color = NUSTSky, fontWeight = FontWeight.Bold)
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (vm.connectivity == Connectivity.ONLINE) GreenOk.copy(0.2f) else Amber.copy(0.2f))
                    .clickable { vm.toggleConnectivity() }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    if (vm.connectivity == Connectivity.ONLINE) "● Online" else "◌ Offline",
                    fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 12.sp,
                    color      = if (vm.connectivity == Connectivity.ONLINE) GreenOk else Amber
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  NFC SCANNER CARD — 6-step NFC transaction flow
//  Mirrors the "NFC Transaction State Flow — Conductor View" diagram exactly
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun NfcScannerCard(vm: ConductorViewModel, step: NfcFlowStep, onStep: (NfcFlowStep) -> Unit) {
    // Pulse animation for WAITING step
    val infiniteTransition = rememberInfiniteTransition(label = "nfc")
    val pulse by infiniteTransition.animateFloat(
        initialValue  = 0.85f, targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "pulse"
    )
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue  = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label         = "alpha"
    )
    // Progress animation for VERIFYING / PROCESSING
    val progressAnim by infiniteTransition.animateFloat(
        initialValue  = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label         = "progress"
    )
    // Shimmer for SUCCESS card
    val shimmerX by infiniteTransition.animateFloat(
        initialValue  = -300f, targetValue  = 600f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label         = "shimmer"
    )

    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, CardBrush, RoundedCornerShape(24.dp)),
        shape    = RoundedCornerShape(24.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier            = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    when (step) {
                        NfcFlowStep.READY         -> "Validate Student Ticket"
                        NfcFlowStep.WAITING       -> "Waiting for Tap…"
                        NfcFlowStep.CARD_DETECTED -> "Card Detected"
                        NfcFlowStep.VERIFYING     -> "Verifying Student…"
                        NfcFlowStep.PROCESSING    -> "Processing Payment…"
                        NfcFlowStep.SUCCESS       -> "Payment Successful"
                    },
                    fontFamily  = Roboto,
                    fontWeight  = FontWeight.Bold,
                    fontSize    = 18.sp,
                    color       = when (step) {
                        NfcFlowStep.SUCCESS -> GreenOk
                        NfcFlowStep.WAITING -> NUSTSky
                        else                -> OnLight
                    }
                )
                // NFC active badge
                if (step == NfcFlowStep.READY || step == NfcFlowStep.WAITING) {
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(GreenOk.copy(0.15f)).padding(8.dp, 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(GreenOk))
                        Spacer(Modifier.width(4.dp))
                        Text("NFC Active", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = GreenOk)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            // ── Central animated element — changes per step ───────────────────
            AnimatedContent(
                targetState   = step,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
                label         = "nfcContent"
            ) { currentStep ->
                when (currentStep) {

                    // Step 1 — READY: static ring with NFC label
                    NfcFlowStep.READY -> {
                        Box(
                            modifier         = Modifier.size(180.dp).clip(CircleShape)
                                .background(SurfaceCard)
                                .border(2.dp, CardEdge, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier.size(70.dp).clip(CircleShape).background(CardEdge),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("NFC", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Muted)
                                }
                            }
                        }
                    }

                    // Step 2 — WAITING: pulsing multi-ring (blue)
                    NfcFlowStep.WAITING -> {
                        Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                            // Outer pulse ring
                            Box(
                                modifier = Modifier.size(180.dp).scale(pulse).clip(CircleShape)
                                    .border(2.dp, NUSTSky.copy(alpha = ringAlpha * 0.4f), CircleShape)
                            )
                            Box(
                                modifier = Modifier.size(140.dp).clip(CircleShape)
                                    .border(3.dp, NUSTSky.copy(alpha = ringAlpha * 0.7f), CircleShape)
                            )
                            // Inner filled circle with NFC
                            Box(
                                modifier         = Modifier.size(90.dp).clip(CircleShape).background(NUSTBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("NFC", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnLight)
                            }
                        }
                    }

                    // Step 3 — CARD DETECTED: card icon in blue ring
                    NfcFlowStep.CARD_DETECTED -> {
                        Box(modifier = Modifier.size(180.dp), contentAlignment = Alignment.Center) {
                            Box(
                                modifier         = Modifier.size(180.dp).clip(CircleShape)
                                    .background(NUSTSky.copy(alpha = 0.15f))
                                    .border(2.dp, NUSTSky.copy(alpha = 0.5f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Card icon (simplified with box)
                                    Box(
                                        modifier = Modifier.size(width = 56.dp, height = 40.dp)
                                            .background(NUSTBlue, RoundedCornerShape(8.dp))
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Box(Modifier.size(10.dp).clip(CircleShape).background(NUSTSky))
                                }
                            }
                        }
                    }

                    // Step 4 — VERIFYING: progress bar below a dot
                    NfcFlowStep.VERIFYING -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(24.dp).clip(CircleShape).background(NUSTSky))
                            Spacer(Modifier.height(20.dp))
                            Text("Verifying Student…", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NUSTSky)
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress      = { progressAnim },
                                modifier      = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color         = NUSTSky,
                                trackColor    = CardEdge
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("25% · Checking credential info", fontFamily = Roboto, fontSize = 11.sp, color = Muted)
                        }
                    }

                    // Step 5 — PROCESSING: fare card + progress
                    NfcFlowStep.PROCESSING -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Fare card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(HeroBrush, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(GlassWhite),
                                        contentAlignment = Alignment.Center
                                    ) { Text("💳", fontSize = 18.sp) }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text("Fare · ${vm.context.fareLabel}", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnLight)
                                        Text("Processing Payment…", fontFamily = Roboto, fontSize = 12.sp, color = OnLight.copy(0.75f))
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            LinearProgressIndicator(
                                progress      = { progressAnim },
                                modifier      = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color         = GreenOk,
                                trackColor    = CardEdge
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("60% · Deducting from balance", fontFamily = Roboto, fontSize = 11.sp, color = Muted)
                        }
                    }

                    // Step 6 — SUCCESS: green circle + student details
                    NfcFlowStep.SUCCESS -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Animated green success circle
                            Box(
                                modifier = Modifier.size(120.dp).clip(CircleShape)
                                    .background(GreenOk.copy(alpha = 0.15f))
                                    .border(3.dp, GreenOk, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✓", fontSize = 48.sp, color = GreenOk, fontFamily = Roboto, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(16.dp))
                            // Student detail card
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .background(GreenOk.copy(0.1f), RoundedCornerShape(16.dp))
                                    .border(1.dp, GreenOk.copy(0.3f), RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("Nkosivathi Kema", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnLight)
                                    Text("Student · NUST 2023/1 Info", fontFamily = Roboto, fontSize = 12.sp, color = Muted)
                                    HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Fare", fontFamily = Roboto, fontSize = 11.sp, color = Muted)
                                            Text(vm.context.fareLabel.ifEmpty { "\$0.50" }, fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnLight)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Balance After", fontFamily = Roboto, fontSize = 11.sp, color = Muted)
                                            Text("\$42.50", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GreenOk)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // Subtitle below central element
            Text(
                when (step) {
                    NfcFlowStep.READY         -> "Hold student's phone back-to-back with this device"
                    NfcFlowStep.WAITING       -> "Hold card next to device"
                    NfcFlowStep.CARD_DETECTED -> "Reading credential…"
                    NfcFlowStep.VERIFYING     -> "Checking student info…"
                    NfcFlowStep.PROCESSING    -> "Deducting from student balance"
                    NfcFlowStep.SUCCESS       -> "Balance updated successfully"
                },
                fontFamily = Roboto, fontSize = 13.sp, color = Muted, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            if (step == NfcFlowStep.READY)
                Text("— or tap a physical NUST card —", fontFamily = Roboto, fontSize = 11.sp, color = Muted.copy(0.55f), textAlign = TextAlign.Center)

            Spacer(Modifier.height(20.dp))

            // ── Step-cycling CTA button ───────────────────────────────────────
            when (step) {
                NfcFlowStep.READY -> {
                    // Simulate Tap → triggers actual ViewModel scan + steps forward
                    Button(
                        onClick  = {
                            onStep(NfcFlowStep.WAITING)
                        },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = NUSTBlue)
                    ) {
                        Text("Simulate Tap", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnLight)
                    }
                }
                NfcFlowStep.WAITING -> {
                    // Auto-advance to Card Detected
                    LaunchedEffect(step) {
                        delay(1500L)
                        onStep(NfcFlowStep.CARD_DETECTED)
                    }
                    Text("Scan Next", fontFamily = Roboto, fontSize = 12.sp, color = NUSTSky, textAlign = TextAlign.Center)
                }
                NfcFlowStep.CARD_DETECTED -> {
                    LaunchedEffect(step) { delay(1000L); onStep(NfcFlowStep.VERIFYING) }
                }
                NfcFlowStep.VERIFYING -> {
                    LaunchedEffect(step) { delay(2000L); onStep(NfcFlowStep.PROCESSING) }
                }
                NfcFlowStep.PROCESSING -> {
                    LaunchedEffect(step) {
                        delay(1800L)
                        vm.processDemoCredential()
                        onStep(NfcFlowStep.SUCCESS)
                    }
                }
                NfcFlowStep.SUCCESS -> {
                    // "Scan Next" button
                    Button(
                        onClick  = { onStep(NfcFlowStep.READY) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = NUSTBlue)
                    ) {
                        Text("Scan Next", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnLight)
                    }
                }
            }
        }
    }
}

// ── Session stats row ─────────────────────────────────────────────────────────
@Composable
private fun SessionStatsRow(pendingCount: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatChip("Validated", "143",          GreenOk,  Modifier.weight(1f))
        StatChip("Queued",    "$pendingCount", Amber,    Modifier.weight(1f))
        StatChip("Declined",  "0",            RedAlert,  Modifier.weight(1f))
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color, modifier: Modifier) {
    Card(
        modifier = modifier.border(1.dp, CardEdge, RoundedCornerShape(14.dp)),
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(value, fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = color)
            Text(label, fontFamily = Roboto, fontSize = 10.sp, color = Muted)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SCAN SUCCESS / DECLINED overlays — shown via Figma cards too
//  (These appear as part of the scanner card when step == SUCCESS or from vm)
// ══════════════════════════════════════════════════════════════════════════════

// ── Scan Success card (Figma Frame 2 — "Passenger Verified") ─────────────────
@Composable
fun ScanSuccessCard(vm: ConductorViewModel, onScanNext: () -> Unit) {
    GlassCard {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Green verified circle
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape)
                    .background(GreenOk.copy(0.15f)).border(3.dp, GreenOk, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✓", fontSize = 34.sp, color = GreenOk, fontFamily = Roboto, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Text("Passenger Verified", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = GreenOk)
            Spacer(Modifier.height(16.dp))
            // Student info
            Text("Nkosivathi Kema", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnLight)
            Text("Student · NUST 2023/1 Info", fontFamily = Roboto, fontSize = 12.sp, color = Muted)
            Spacer(Modifier.height(4.dp))
            Text("Route 7 — NUST → City Centre", fontFamily = Roboto, fontSize = 12.sp, color = Muted)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Fare Removed", fontFamily = Roboto, fontSize = 11.sp, color = Muted)
                    Text("\$1.00", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnLight)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Remaining Balance", fontFamily = Roboto, fontSize = 11.sp, color = Muted)
                    Text("\$41.50", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = GreenOk)
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(GreenOk.copy(0.12f)).border(1.dp, GreenOk.copy(0.3f), RoundedCornerShape(12.dp)).padding(12.dp)
            ) {
                Text("✓ Approved", fontFamily = Roboto, fontWeight = FontWeight.Bold, color = GreenOk, fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text("Pilot mode — no real money", fontFamily = Roboto, fontSize = 10.sp, color = Muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick  = onScanNext,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = NUSTBlue)
            ) {
                Text("Scan Next", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnLight)
            }
        }
    }
}

// ── Scan Failed card (Figma Frame 3 — "Transaction Declined") ────────────────
@Composable
fun ScanFailedCard(reason: String, onRetry: () -> Unit) {
    GlassCard {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            // Red X circle
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape)
                    .background(RedAlert.copy(0.15f)).border(3.dp, RedAlert, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("✕", fontSize = 26.sp, color = RedAlert, fontFamily = Roboto, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Text("Transaction Declined", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = RedAlert)
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
            Spacer(Modifier.height(12.dp))

            // Error detail rows
            listOf(
                "Student Name"     to "Nkosivathi Kema",
                "Student ID"       to "2023/1 Info",
                "Error Code"       to "ERR-402",
                "Route"            to "Route 7 — NUST → City Centre",
                "Attempted Fare"   to "\$1.00",
                "Account Balance"  to "\$0.00",
                "Decline Reason"   to reason.ifEmpty { "Insufficient balance" },
                "Timestamp"        to "Today · 09:14"
            ).forEach { (label, value) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(label, fontFamily = Roboto, fontSize = 12.sp, color = Muted)
                    Text(value, fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnLight)
                }
            }

            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(RedAlert.copy(0.1f)).border(1.dp, RedAlert.copy(0.3f), RoundedCornerShape(12.dp)).padding(12.dp)
            ) {
                Text("⚠  Advise student to top up via NUST mobility app", fontFamily = Roboto, fontSize = 12.sp, color = RedAlert)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick  = onRetry,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape    = RoundedCornerShape(16.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = RedAlert)
            ) {
                Text("Try Again", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnLight)
            }
        }
    }
}

// ── Offline mode scanner card (Figma Frame 4) ─────────────────────────────────
@Composable
fun OfflineScannerCard(vm: ConductorViewModel, step: NfcFlowStep, onStep: (NfcFlowStep) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "offlinePulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue  = 0.9f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "offPulse"
    )
    GlassCard {
        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Ready to Scan (Offline)", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Amber)
            Spacer(Modifier.height(4.dp))
            Text("Cached verification active · ${vm.pending.size} queued", fontFamily = Roboto, fontSize = 12.sp, color = Muted)
            Spacer(Modifier.height(18.dp))
            // Amber pulsing ring
            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier.size(160.dp).scale(pulse).clip(CircleShape)
                        .border(3.dp, Amber.copy(alpha = 0.4f), CircleShape)
                )
                Box(
                    modifier         = Modifier.size(100.dp).clip(CircleShape).background(Amber.copy(0.15f)).border(2.dp, Amber, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NFC", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Amber)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Hold student's phone next to device", fontFamily = Roboto, fontSize = 13.sp, color = Muted, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            // Sync row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(HeroBrush)
                        .clickable { onStep(NfcFlowStep.WAITING) }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Scan Offline", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnLight) }
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard).border(1.dp, CardEdge, RoundedCornerShape(14.dp))
                        .clickable { vm.syncOfflineBatch() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Retry Sync", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Muted) }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SCREEN 2 — VEHICLE SELECTION
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun VehicleSelectionScreen(vm: ConductorViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Spacer(Modifier.height(4.dp)) }
        item {
            Column {
                Text("Assign Vehicle & Route", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = OnLight)
                if (!vm.vehicleConfirmed) {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(Amber.copy(0.15f)).padding(12.dp, 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📌", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Select your vehicle before scanning passengers", fontFamily = Roboto, fontSize = 13.sp, color = Amber)
                    }
                }
            }
        }
        items(vm.availableVehicles) { veh ->
            val active = veh.registration == vm.context.registration
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, if (active) NUSTSky.copy(0.6f) else CardEdge, RoundedCornerShape(18.dp)),
                shape    = RoundedCornerShape(18.dp),
                colors   = CardDefaults.cardColors(containerColor = if (active) NUSTBlue.copy(0.12f) else SurfaceCard)
            ) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                                .background(if (active) HeroBrush else Brush.linearGradient(listOf(CardEdge, CardEdge))),
                            contentAlignment = Alignment.Center
                        ) { Text("🚌", fontSize = 20.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(veh.registration, fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 15.sp)
                            Text(veh.routeName, fontFamily = Roboto, fontSize = 11.sp, color = Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text("Fare: ${veh.fareLabel}", fontFamily = Roboto, fontSize = 12.sp, color = NUSTSky, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                            .background(if (active) HeroBrush else Brush.linearGradient(listOf(CardEdge, CardEdge)))
                            .clickable { vm.selectVehicle(veh) }
                            .heightIn(min = 48.dp).padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (active) "✓ Active" else "Select", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnLight)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SCREEN 3 — OFFLINE QUEUE
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun OfflineQueueScreen(vm: ConductorViewModel) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Spacer(Modifier.height(4.dp)) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Offline Queue", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = OnLight)
                if (vm.pending.isNotEmpty()) {
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(HeroBrush)
                            .clickable { vm.syncOfflineBatch() }.heightIn(min = 48.dp).padding(14.dp, 10.dp),
                        contentAlignment = Alignment.Center
                    ) { Text("↺ Sync All", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnLight) }
                }
            }
        }
        if (vm.pending.isEmpty()) {
            item {
                GlassCard {
                    Column(Modifier.padding(32.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✓", fontSize = 40.sp, color = GreenOk)
                        Spacer(Modifier.height(8.dp))
                        Text("All synced", fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 16.sp)
                        Text("No pending offline items.", fontFamily = Roboto, fontSize = 13.sp, color = Muted)
                    }
                }
            }
        } else {
            items(vm.pending) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth().border(1.dp, Amber.copy(0.4f), RoundedCornerShape(16.dp)),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = CardDefaults.cardColors(containerColor = Amber.copy(0.06f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(36.dp).clip(CircleShape).background(Amber.copy(0.2f)), contentAlignment = Alignment.Center) {
                            Text("#${item.sequence}", fontFamily = Roboto, fontWeight = FontWeight.Bold, color = Amber, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.id, fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 13.sp)
                            Text("Token: ${item.credentialToken.take(24)}…", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Muted)
                        }
                        Text("PENDING", fontFamily = Roboto, color = Amber, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SHARED COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun GlassCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, CardEdge, RoundedCornerShape(20.dp)),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) { content() }
}

@Composable
private fun PilotBanner() = Box(
    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
        .background(AmberSurface).border(1.dp, Amber.copy(0.35f), RoundedCornerShape(12.dp)).padding(14.dp)
) {
    Text(
        "⚠  Pilot mode — fictional credentials & fare simulation. Not live.",
        fontFamily = Roboto, fontSize = 12.sp, color = Amber
    )
}
