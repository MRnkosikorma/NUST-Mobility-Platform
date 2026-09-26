package zw.ac.nust.mobility.student

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zw.ac.nust.mobility.student.network.StudentApiClient
import zw.ac.nust.mobility.student.network.TopUpRequestDto
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

// ── NFC state exposed to the UI ───────────────────────────────────────────────
enum class NfcState { READY, OFF, UNSUPPORTED }

// ── Result after a successful HCE tap deactivation ───────────────────────────
sealed class TapResult {
    data class Success(
        val vehicleReg: String = "NUST-BUS-01",
        val amountLabel: String = "USD 1.00",
        val timestamp: String = "just now"
    ) : TapResult()
    object Failure : TapResult()
}

// ── Journey domain model ──────────────────────────────────────────────────────
data class Journey(
    val id: String,
    val route: String,
    val date: String,
    val amount: String,
    val status: String,
    val vehicleReg: String
)

// ── ViewModel ────────────────────────────────────────────────────────────────
class StudentViewModel : ViewModel() {
    var userEmail by mutableStateOf("student@nust.ac.zw")
        private set

    var balanceMinor by mutableIntStateOf(500)
        private set

    var credentialId by mutableStateOf(newCredentialId())
        private set

    var opaqueToken by mutableStateOf(newOpaqueToken())
        private set

    var credentialExpiresAt by mutableStateOf(Instant.now().plus(60, ChronoUnit.SECONDS))
        private set

    var secondsRemaining by mutableIntStateOf(60)
        private set

    var selectedTab by mutableIntStateOf(0)

    // NFC hardware state — updated by MainActivity
    var nfcState by mutableStateOf(NfcState.READY)

    // Tap result overlay — set by MobilityCardService broadcast, auto-cleared by UI
    var lastTapResult by mutableStateOf<TapResult?>(null)
        private set

    val journeys = mutableStateOf(
        listOf(
            Journey("j1", "NUST Main Campus — Bulawayo CBD", "Today · 08:12", "USD 1.00", "CONFIRMED", "NUST-BUS-01"),
            Journey("j2", "NUST Main Campus — Selbourne Park", "Yesterday · 16:45", "USD 0.50", "CONFIRMED", "NUST-BUS-02"),
            Journey("j3", "NUST Main Campus — Hillside", "10 Sep · 12:30", "USD 0.75", "CONFIRMED", "COMM-BUS-88"),
        )
    )

    fun refreshCredential() {
        viewModelScope.launch {
            try {
                val res = StudentApiClient.api.issueCredential()
                if (res.isSuccessful && res.body() != null) {
                    val body = res.body()!!
                    credentialId = body.credentialId
                    opaqueToken = body.opaqueToken
                    credentialExpiresAt = Instant.now().plus(body.ttlSeconds.toLong(), ChronoUnit.SECONDS)
                    secondsRemaining = body.ttlSeconds
                    return@launch
                }
            } catch (_: Exception) {}
            // Fallback offline credential generation
            credentialId = newCredentialId()
            opaqueToken = newOpaqueToken()
            credentialExpiresAt = Instant.now().plus(60, ChronoUnit.SECONDS)
            secondsRemaining = 60
        }
    }

    /** Called from MobilityCardService broadcast receiver when HCE tap completes. */
    fun recordTap(result: TapResult) {
        lastTapResult = result
        if (result is TapResult.Success) {
            balanceMinor = maxOf(0, balanceMinor - 100)
            syncBalanceWithServer()
        }
    }

    /** Called by the UI overlay after it has auto-dismissed. */
    fun clearTapResult() {
        lastTapResult = null
    }

    fun updateNfcState(state: NfcState) {
        nfcState = state
    }

    fun mockTopUp(amountMinor: Int = 100) {
        balanceMinor += amountMinor
        viewModelScope.launch {
            try {
                val res = StudentApiClient.api.topUpBalance(TopUpRequestDto(amountMinor))
                if (res.isSuccessful && res.body() != null) {
                    balanceMinor = res.body()!!.balanceMinor
                }
            } catch (_: Exception) {}
        }
    }

    fun syncBalanceWithServer() {
        viewModelScope.launch {
            try {
                val res = StudentApiClient.api.getBalance()
                if (res.isSuccessful && res.body() != null) {
                    balanceMinor = res.body()!!.balanceMinor
                }
            } catch (_: Exception) {}
        }
    }

    fun formattedBalance(): String = "USD ${"%.2f".format(balanceMinor / 100.0)}"

    private fun newCredentialId(): String = "cred_${UUID.randomUUID().toString().replace("-", "").take(12)}"

    private fun newOpaqueToken(): String {
        val payload = "eyJ1c2VySWQiOiJ1c3Jfc3R1ZGVudF8wMDEiLCJpbnN0aXR1dGlvbklkIjoibnVzdCJ9"
        val sig = UUID.randomUUID().toString().replace("-", "").take(16)
        return "$payload.$sig"
    }
}
