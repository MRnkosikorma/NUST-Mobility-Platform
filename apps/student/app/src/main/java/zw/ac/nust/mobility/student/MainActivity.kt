package zw.ac.nust.mobility.student

import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
//  TYPOGRAPHY — Roboto Bold (system sans-serif = Roboto on Android)
// ══════════════════════════════════════════════════════════════════════════════
private val Roboto = FontFamily.SansSerif

private val NustTypography = Typography(
    displayLarge   = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 57.sp),
    displayMedium  = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 45.sp),
    displaySmall   = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 36.sp),
    headlineLarge  = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 28.sp),
    headlineSmall  = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 24.sp),
    titleLarge     = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 22.sp),
    titleMedium    = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 16.sp),
    titleSmall     = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 14.sp),
    bodyLarge      = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium     = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall      = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge     = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 14.sp),
    labelMedium    = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 12.sp),
    labelSmall     = TextStyle(fontFamily = Roboto, fontWeight = FontWeight.Bold,   fontSize = 11.sp),
)

// ══════════════════════════════════════════════════════════════════════════════
//  ONE UI 6 COLOUR SYSTEM — NUST brand & ergonomic palette
// ══════════════════════════════════════════════════════════════════════════════
private val CyberTurquoise = Color(0xFF06B6D4)   // Primary Action Accent
private val ElectricBlue   = Color(0xFF3B82F6)   // Structural Accent
private val DeepPurple     = Color(0xFF8B5CF6)   // Premium HMAC Security Accent
private val EmeraldGreen   = Color(0xFF10B981)   // Success / Active Balance
private val WarmOrange     = Color(0xFFF97316)   // Pending / Warning Status
private val RichCrimson    = Color(0xFFDC2626)   // Alert / Error State
private val PitchBlack     = Color(0xFF000000)   // Pitch Black Background
private val DeepSlate      = Color(0xFF060C1A)   // Page Background
private val SurfaceCard    = Color(0xFF0F1829)   // Focus Block Card Background
private val GlassWhite     = Color(0x14FFFFFF)   // Glassmorphism surface
private val CardEdge       = Color(0xFF1A2F4D)   // Focus Block Border
private val OnLight        = Color(0xFFFFFFFF)
private val Muted          = Color(0xFFB0BBC8)
private val GreenOk        = EmeraldGreen
private val RedAlert       = RichCrimson
private val Amber          = WarmOrange
private val AmberSurface   = Color(0xFF1C1600)

private val SchemeStudentDark = darkColorScheme(
    primary      = CyberTurquoise,
    onPrimary    = Color.Black,
    secondary    = ElectricBlue,
    background   = PitchBlack,
    surface      = SurfaceCard,
    onBackground = OnLight,
    onSurface    = OnLight,
    surfaceVariant = CardEdge,
    onSurfaceVariant = Muted
)

private val SchemeStudentLight = androidx.compose.material3.lightColorScheme(
    primary      = ElectricBlue,
    onPrimary    = Color.White,
    secondary    = CyberTurquoise,
    background   = Color(0xFFF4F6F9),
    surface      = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111827),
    onSurface    = Color(0xFF1F2937),
    surfaceVariant = Color(0xFFE5E7EB),
    onSurfaceVariant = Color(0xFF4B5563)
)

// Reusable gradients
private val HeroBrush  get() = Brush.linearGradient(listOf(ElectricBlue, CyberTurquoise))
private val CardBrush  get() = Brush.linearGradient(listOf(CyberTurquoise.copy(alpha = 0.5f), CardEdge))

// ══════════════════════════════════════════════════════════════════════════════
//  ACTIVITY
// ══════════════════════════════════════════════════════════════════════════════
class MainActivity : ComponentActivity() {

    private lateinit var studentViewModel: StudentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        setContent {
            studentViewModel = viewModel()

            val nfcState = when {
                nfcAdapter == null    -> NfcState.UNSUPPORTED
                !nfcAdapter.isEnabled -> NfcState.OFF
                else                  -> NfcState.READY
            }
            studentViewModel.updateNfcState(nfcState)

            DisposableEffect(Unit) {
                MobilityCardService.onTapComplete = {
                    runOnUiThread { studentViewModel.recordTap(TapResult.Success()) }
                }
                onDispose { MobilityCardService.onTapComplete = null }
            }

            val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
            var isDarkTheme by remember { mutableStateOf(systemDark) }
            val colors = if (isDarkTheme) SchemeStudentDark else SchemeStudentLight
            var showSplash by remember { mutableStateOf(true) }

            MaterialTheme(colorScheme = colors, typography = NustTypography) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (showSplash) Color.Black else colors.background
                ) {
                    androidx.compose.animation.AnimatedContent(
                        targetState = showSplash,
                        transitionSpec = { fadeIn(tween(400)) togetherWith fadeOut(tween(400)) },
                        label = "splashTransition"
                    ) { splash ->
                        if (splash) {
                            MundoSplashScreen(
                                appName = "Mundo Student",
                                onFinished = { showSplash = false }
                            )
                        } else {
                            StudentApp(
                                studentViewModel,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  ROOT APP — 4-tab scaffold
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentApp(
    vm: StudentViewModel = viewModel(),
    isDarkTheme: Boolean = true,
    onToggleTheme: () -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar         = { NustTopBar(vm, isDarkTheme, onToggleTheme) },
            bottomBar      = { NustBottomBar(vm.selectedTab) { vm.selectedTab = it } }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Crossfade(targetState = vm.selectedTab, animationSpec = tween(300), label = "tabs") { tab ->
                    when (tab) {
                        0 -> HomeScreen(vm)
                        1 -> DigitalPassScreen(vm)
                        2 -> PaymentScreen(vm)
                        3 -> TransportCardScreen(vm)
                    }
                }
            }
        }

        // ── Full-screen boarding confirmation overlay ──────────────────────────
        val tap = vm.lastTapResult
        AnimatedVisibility(
            visible  = tap != null,
            enter    = fadeIn(tween(200)) + scaleIn(tween(200), initialScale = 0.92f),
            exit     = fadeOut(tween(300)) + scaleOut(tween(300), targetScale = 0.96f),
            modifier = Modifier.fillMaxSize()
        ) {
            if (tap is TapResult.Success) {
                LaunchedEffect(tap) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    delay(1800L)
                    vm.clearTapResult()
                }
                Box(
                    modifier         = Modifier.fillMaxSize().background(GreenOk.copy(alpha = 0.94f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✓", fontSize = 80.sp, color = OnLight, fontFamily = Roboto, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Text("BOARDED", fontSize = 36.sp, fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight)
                        Spacer(Modifier.height(8.dp))
                        Text("${tap.amountLabel} deducted · ${tap.vehicleReg}", fontSize = 16.sp, fontFamily = Roboto, color = OnLight.copy(alpha = 0.85f))
                        Spacer(Modifier.height(4.dp))
                        Text(vm.formattedBalance() + " remaining", fontSize = 14.sp, fontFamily = Roboto, color = OnLight.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  TOP APP BAR
// ══════════════════════════════════════════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NustTopBar(
    vm: StudentViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(HeroBrush),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NS", fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 13.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Mundo Student", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text(vm.userEmail, fontFamily = Roboto, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        actions = {
            // Theme toggle button
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onToggleTheme() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(if (isDarkTheme) "☀️" else "🌙", fontSize = 14.sp)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor    = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  BOTTOM NAVIGATION BAR — pill style, 4 tabs matching Figma frames
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun NustBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Home", "Pass", "Pay", "Card")
    Box(
        modifier         = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier        = Modifier.fillMaxWidth().border(1.dp, CardEdge, RoundedCornerShape(32.dp)),
            shape           = RoundedCornerShape(32.dp),
            color           = SurfaceCard.copy(alpha = 0.96f),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth().padding(6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                tabs.forEachIndexed { index, label ->
                    val selected = selectedTab == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (selected) HeroBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                            .clickable { onTabSelected(index) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text       = label,
                            fontFamily = Roboto,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize   = 13.sp,
                            color      = if (selected) OnLight else Muted
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SCREEN 1 — HOME (One UI 6 Ergonomic 30/70 View/Interaction Split)
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun HomeScreen(vm: StudentViewModel) {
    LazyColumn(
        modifier            = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        // ── TOP 30% VIEWING AREA: Large Header & Student Avatar ───────────────
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "NUST Transport",
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = OnLight,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        "Wallet & Boarding Pass",
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = CyberTurquoise,
                        letterSpacing = (-0.5).sp
                    )
                }
                Box(
                    modifier = Modifier.size(52.dp).clip(CircleShape).background(HeroBrush).border(2.dp, CyberTurquoise.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("NK", fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 18.sp)
                }
            }
        }

        // ── BOTTOM 70% INTERACTION AREA: One UI 6 Squircle Focus Blocks ───────
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, CardEdge, RoundedCornerShape(28.dp)),
                shape    = RoundedCornerShape(28.dp),
                colors   = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeroBrush, RoundedCornerShape(28.dp))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(CircleShape).background(GlassWhite),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("NK", fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 15.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Hello,", fontFamily = Roboto, fontSize = 12.sp, color = OnLight.copy(alpha = 0.75f))
                                Text("Nkosivathi Kema", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnLight)
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Text("Available Transport Balance", fontFamily = Roboto, fontSize = 12.sp, color = OnLight.copy(alpha = 0.8f))
                        Text(
                            vm.formattedBalance(),
                            fontFamily  = Roboto,
                            fontWeight  = FontWeight.Bold,
                            fontSize    = 38.sp,
                            color       = OnLight
                        )
                        Spacer(Modifier.height(16.dp))
                        // Quick top-up row with Cyber-Turquoise pill buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("+$1" to 100, "+$2" to 200, "+$5" to 500).forEach { (label, amt) ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CyberTurquoise)
                                        .clickable { vm.mockTopUp(amt) }
                                        .padding(horizontal = 16.dp, vertical = 10.dp)
                                ) {
                                    Text(label, fontFamily = Roboto, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── NFC pass row ──────────────────────────────────────────────────────
        item {
            GlassCard {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("NFC Pass", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnLight)
                        Text("Tap to board", fontFamily = Roboto, fontSize = 12.sp, color = Muted)
                    }
                    NfcStatusBadge(vm.nfcState)
                }
            }
        }

        // ── Top-up board ──────────────────────────────────────────────────────
        item {
            GlassCard {
                Column(Modifier.padding(18.dp)) {
                    Text("Top-up Board", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnLight)
                    Spacer(Modifier.height(4.dp))
                    Text("Add credit to your transport wallet", fontFamily = Roboto, fontSize = 12.sp, color = Muted)
                    Spacer(Modifier.height(14.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("USD 1.00" to 100, "USD 2.00" to 200, "USD 5.00" to 500).forEach { (label, amt) ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(HeroBrush)
                                    .clickable { vm.mockTopUp(amt) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // ── Recent journeys ───────────────────────────────────────────────────
        item {
            Text("Recent Journeys", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = OnLight)
        }
        items(vm.journeys.value) { journey ->
            CompactJourneyRow(journey)
        }

        item { PilotBanner() }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun CompactJourneyRow(journey: Journey) {
    Row(
        modifier          = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(8.dp).clip(CircleShape).background(NUSTSky)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(journey.route, fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(journey.date, fontFamily = Roboto, fontSize = 11.sp, color = Muted)
        }
        Text(journey.amount, fontFamily = Roboto, fontWeight = FontWeight.Bold, color = RedAlert, fontSize = 13.sp)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SCREEN 2 — DIGITAL PASS  (mirrors "Digital Pass 02 of 04" Figma frame)
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun DigitalPassScreen(vm: StudentViewModel) {
    var secondsLeft by remember { mutableIntStateOf(60) }
    LaunchedEffect(vm.credentialId) {
        MobilityCardService.activeToken = vm.opaqueToken
        secondsLeft = 60
        while (secondsLeft > 0) { delay(1000L); secondsLeft-- }
        vm.refreshCredential()
    }
    val progress by animateFloatAsState(
        targetValue   = secondsLeft / 60f,
        animationSpec = tween(800),
        label         = "timerRing"
    )
    val ringColor = when {
        secondsLeft > 30 -> NUSTSky
        secondsLeft > 15 -> Amber
        else             -> RedAlert
    }

    LazyColumn(
        modifier            = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        // ── University header banner ──────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeroBrush, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("National University of Science & Technology", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnLight, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(4.dp))
                    Text("NUST Transport Pass", fontFamily = Roboto, fontSize = 12.sp, color = OnLight.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                }
            }
        }

        // ── Student profile strip ─────────────────────────────────────────────
        item {
            GlassCard {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(56.dp).clip(CircleShape).background(HeroBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("NK", fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 20.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Nkosivathi Kema", fontFamily = Roboto, fontWeight = FontWeight.Bold, color = OnLight, fontSize = 15.sp)
                        Text(vm.userEmail, fontFamily = Roboto, fontSize = 11.sp, color = Muted)
                        Spacer(Modifier.height(4.dp))
                        Text("Air Condition School of Computing", fontFamily = Roboto, fontSize = 11.sp, color = Muted)
                    }
                    // Active badge
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(GreenOk.copy(alpha = 0.2f)).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Active", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = GreenOk)
                    }
                }
            }
        }

        // ── QR ticket with countdown ring ─────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBrush, RoundedCornerShape(24.dp)),
                shape  = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Column(
                    modifier            = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    NfcStatusBadge(vm.nfcState)
                    Spacer(Modifier.height(18.dp))

                    Box(modifier = Modifier.size(220.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val sw = 10f
                            drawArc(ringColor.copy(alpha = 0.15f), -90f, 360f,       false, style = Stroke(sw, cap = StrokeCap.Round))
                            drawArc(ringColor,                     -90f, 360f * progress, false, style = Stroke(sw, cap = StrokeCap.Round))
                        }
                        QrGraphicBox(vm.credentialId)
                    }

                    Spacer(Modifier.height(14.dp))
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(ringColor.copy(alpha = 0.15f)).padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Refreshes in ${secondsLeft}s", fontFamily = Roboto, fontWeight = FontWeight.Bold, color = ringColor, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("ID: ${vm.credentialId}", color = Muted, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
                    Spacer(Modifier.height(16.dp))

                    OutlinedButton(
                        onClick  = vm::refreshCredential,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape    = RoundedCornerShape(14.dp),
                        border   = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = HeroBrush)
                    ) {
                        Text("↺  Refresh Pass", fontFamily = Roboto, fontWeight = FontWeight.Bold, color = NUSTSky, fontSize = 14.sp)
                    }
                }
            }
        }

        item { PilotBanner() }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SCREEN 3 — PAYMENT  (mirrors "Payment 03 of 04" Figma frame)
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun PaymentScreen(vm: StudentViewModel) {
    // Simulate "payment successful" state for demonstration
    var paymentDone by remember { mutableStateOf(true) }

    LazyColumn(
        modifier            = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        // ── Payment status card ───────────────────────────────────────────────
        item {
            GlassCard {
                Column(
                    modifier            = Modifier.fillMaxWidth().padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Green check circle
                    Box(
                        modifier = Modifier.size(72.dp).clip(CircleShape).background(
                            if (paymentDone) GreenOk else Amber
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (paymentDone) "✓" else "…", fontSize = 30.sp, color = OnLight, fontWeight = FontWeight.Bold, fontFamily = Roboto)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (paymentDone) "Payment Successful" else "Processing…",
                        fontFamily  = Roboto,
                        fontWeight  = FontWeight.Bold,
                        fontSize    = 20.sp,
                        color       = if (paymentDone) GreenOk else Amber
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Total Amount", fontFamily = Roboto, fontSize = 12.sp, color = Muted)
                    Text("\$0.50", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 36.sp, color = OnLight)
                }
            }
        }

        // ── Payment details ───────────────────────────────────────────────────
        item {
            GlassCard {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PaymentDetailRow("From", "Nkosivathi Kema")
                    HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
                    PaymentDetailRow("To", "Route 7 — NUST → City Centre")
                    HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
                    PaymentDetailRow("Ref. ID", "RK-0021")
                    HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
                    PaymentDetailRow("Balance Before", vm.formattedBalance())
                    HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
                    PaymentDetailRow("Amount Paid", "\$0.50")
                    HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
                    PaymentDetailRow("Confirmation No.", "IDs.Numbacola")
                }
            }
        }

        // ── Journey booked banner ─────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GreenOk.copy(alpha = 0.12f))
                    .border(1.dp, GreenOk.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Text(
                    "✓  Journey Booked — NFC pass updated",
                    fontFamily = Roboto, fontWeight = FontWeight.Bold, color = GreenOk, fontSize = 13.sp
                )
            }
        }

        // ── Done CTA ─────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(HeroBrush)
                    .clickable { paymentDone = !paymentDone }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Done", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnLight)
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun PaymentDetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = Roboto, fontSize = 12.sp, color = Muted)
        Text(value, fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SCREEN 4 — TRANSPORT CARD  (mirrors "Transport Card 04 of 04" Figma frame)
// ══════════════════════════════════════════════════════════════════════════════
@Composable
private fun TransportCardScreen(vm: StudentViewModel) {
    // Animated shimmer for the card chip
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue   = -300f,
        targetValue    = 600f,
        animationSpec  = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label          = "shimmerX"
    )

    LazyColumn(
        modifier            = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(Modifier.height(4.dp)) }

        // ── Physical transport card replica ───────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(HeroBrush)
            ) {
                // Shimmer sweep
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors    = listOf(Color.Transparent, OnLight.copy(alpha = 0.08f), Color.Transparent),
                            start     = Offset(shimmerX, 0f),
                            end       = Offset(shimmerX + 300f, size.height)
                        )
                    )
                }
                Column(
                    modifier = Modifier.fillMaxSize().padding(22.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                        Column {
                            Text("Transport Card", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnLight.copy(alpha = 0.8f))
                            Text("NUST", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = OnLight)
                        }
                        // NFC icon
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(GlassWhite),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("◎", fontSize = 18.sp, color = OnLight, fontFamily = Roboto)
                        }
                    }
                    Column {
                        Text("•••• •••• •••• 3847", fontFamily = FontFamily.Monospace, fontSize = 18.sp, color = OnLight, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("CARD HOLDER", fontFamily = Roboto, fontSize = 9.sp, color = OnLight.copy(alpha = 0.6f))
                                Text("Nkosivathi Kema", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnLight)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("EXPIRES", fontFamily = Roboto, fontSize = 9.sp, color = OnLight.copy(alpha = 0.6f))
                                Text("12/27", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnLight)
                            }
                        }
                    }
                }
            }
        }

        // ── Credit balance info ───────────────────────────────────────────────
        item {
            GlassCard {
                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Card Balance", fontFamily = Roboto, fontSize = 12.sp, color = Muted)
                        Text(vm.formattedBalance(), fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = NUSTSky)
                    }
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(HeroBrush).padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text("Admin", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnLight)
                    }
                }
            }
        }

        // ── Card details grid ─────────────────────────────────────────────────
        item {
            GlassCard {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Card Details", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = OnLight)
                    Spacer(Modifier.height(4.dp))
                    CardDetailRow("Card Number", "•••• •••• •••• 3847")
                    HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
                    CardDetailRow("Card Amount", vm.formattedBalance())
                    HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
                    CardDetailRow("Issue Date",  "01 Dec 2025")
                    HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
                    CardDetailRow("Expiry Date", "12 Dec 2027")
                    HorizontalDivider(color = CardEdge, thickness = 0.5.dp)
                    CardDetailRow("Card Holder", "Nkosivathi Kema")
                }
            }
        }

        // ── Linked accounts ───────────────────────────────────────────────────
        item {
            Text("Linked Accounts", fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnLight)
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LinkedAccountRow(
                    initial  = "E",
                    name     = "EcoCash Mobile Money",
                    sub      = "+263 77X XXX XXX",
                    active   = true
                )
                LinkedAccountRow(
                    initial  = "Z",
                    name     = "ZimSwitch Bank Debit",
                    sub      = "Bank Transfer Gateway",
                    active   = false,
                    isError  = true
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun CardDetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, fontFamily = Roboto, fontSize = 12.sp, color = Muted)
        Text(value, fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = OnLight)
    }
}

@Composable
private fun LinkedAccountRow(initial: String, name: String, sub: String, active: Boolean, isError: Boolean = false) {
    val accent = if (isError) RedAlert else GreenOk
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(42.dp).clip(CircleShape).background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(initial, fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = accent)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = OnLight)
            Text(sub,  fontFamily = Roboto, fontSize = 11.sp, color = Muted)
        }
        Box(
            modifier = Modifier.clip(CircleShape).background(accent.copy(alpha = 0.15f)).padding(8.dp)
        ) {
            Text(if (active) "›" else "!", fontFamily = Roboto, fontWeight = FontWeight.Bold, color = accent, fontSize = 14.sp)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  SHARED COMPONENTS
// ══════════════════════════════════════════════════════════════════════════════

/** One UI 6 Focus Block Card wrapper with 28dp squircle curvature */
@Composable
private fun GlassCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().border(1.dp, CardEdge, RoundedCornerShape(28.dp)),
        shape    = RoundedCornerShape(28.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) { content() }
}

/** NFC status pill badge */
@Composable
private fun NfcStatusBadge(state: NfcState) {
    val (dot, label, desc) = when (state) {
        NfcState.READY       -> Triple(GreenOk,  "NFC Active · Hold to board",  "NFC ready")
        NfcState.OFF         -> Triple(Amber,    "NFC Off — enable in Settings", "NFC disabled")
        NfcState.UNSUPPORTED -> Triple(RedAlert, "No NFC — use QR code",         "No NFC hardware")
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(dot.copy(alpha = 0.15f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .semantics { contentDescription = desc }
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(6.dp))
        Text(label, fontFamily = Roboto, color = dot, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/** Centred QR placeholder inside the countdown ring with Deep Purple HMAC badge */
@Composable
private fun QrGraphicBox(id: String) = Box(
    modifier = Modifier.size(175.dp).background(OnLight, RoundedCornerShape(22.dp)).padding(10.dp),
    contentAlignment = Alignment.Center
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier         = Modifier.size(125.dp).background(PitchBlack, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("QR TICKET", color = CyberTurquoise, textAlign = TextAlign.Center, fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(Modifier.height(4.dp))
                // Deep Purple HMAC badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DeepPurple)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text("HMAC SHA-256", color = Color.White, textAlign = TextAlign.Center, fontFamily = Roboto, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(id, color = Color.Black, fontSize = 8.sp, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/** Pilot mode notice */
@Composable
private fun PilotBanner() = Box(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(AmberSurface)
        .border(1.dp, Amber.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
        .padding(14.dp)
) {
    Text(
        "⚠  Pilot mode — fictional NUST balance & credentials. No live money custody.",
        fontFamily = Roboto, fontSize = 12.sp, color = Amber
    )
}

// ══════════════════════════════════════════════════════════════════════════════
//  STARTUP SCREEN & 3D GLOSSY RED CAPSULE PROGRESS BAR
// ══════════════════════════════════════════════════════════════════════════════

/** 3D Glossy Red Capsule Progress Bar with pitch-black overlay container */
@Composable
fun MundoCapsuleProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350, easing = LinearEasing),
        label = "capsuleProgress"
    )

    // Dark recessed track container (capsule shape)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF12141A))
            .border(1.dp, Color(0xFF222632), RoundedCornerShape(999.dp))
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (animatedProgress > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .shadow(
                        elevation = 10.dp,
                        shape = RoundedCornerShape(999.dp),
                        ambientColor = Color(0xFFFF2D37),
                        spotColor = Color(0xFFFF2D37)
                    )
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF6B6B), // Top gloss shine highlight
                                Color(0xFFE61C24), // Middle vibrant red
                                Color(0xFF9E0B11)  // Bottom dark depth shadow
                            )
                        )
                    )
            ) {
                // Top white gloss sheen (3D plastic highlight line)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .padding(horizontal = 6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.55f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        )
                )
            }
        }
    }
}

/** Pitch black startup splash screen with centered logo & 3D glossy capsule progress bar */
@Composable
fun MundoSplashScreen(
    appName: String = "Mundo Student",
    onFinished: () -> Unit
) {
    var progress by remember { mutableFloatStateOf(0f) }
    var statusText by remember { mutableStateOf("Initializing System...") }

    LaunchedEffect(Unit) {
        statusText = "Initializing NFC Security Kernel..."
        progress = 0.25f
        delay(450L)

        statusText = "Loading Campus Routes & Fares..."
        progress = 0.60f
        delay(550L)

        statusText = "Syncing Mobility Ledger..."
        progress = 0.88f
        delay(450L)

        statusText = "System Ready"
        progress = 1.0f
        delay(350L)

        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000)), // Pitch black matching logo background
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Mundo Logo Image
            Image(
                painter = painterResource(id = R.drawable.mundo_logo),
                contentDescription = "Mundo Logo",
                modifier = Modifier
                    .size(160.dp)
                    .shadow(16.dp, CircleShape, ambientColor = Color.Red, spotColor = Color.Red)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = appName,
                fontFamily = Roboto,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "NUST Campus Mobility Platform",
                fontFamily = Roboto,
                fontSize = 13.sp,
                color = Color(0xFF9E9E9E)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Progress Section at Bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = statusText,
                        fontFamily = Roboto,
                        fontSize = 12.sp,
                        color = Color(0xFFB0BBC8)
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFFFF4D4D)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3D Capsule progress bar
                MundoCapsuleProgressBar(progress = progress)
            }
        }
    }
}

