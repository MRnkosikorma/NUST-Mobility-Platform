package zw.ac.nust.mobility.conductor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.time.Instant
import java.util.UUID

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import zw.ac.nust.mobility.conductor.network.ConductorApiClient
import zw.ac.nust.mobility.conductor.network.ValidationRequestDto

enum class Connectivity { ONLINE, OFFLINE }
enum class ScanOutcome  { READY, CONFIRMED, PROVISIONAL, DECLINED }

data class VehicleContext(
    val registration: String = "",           // empty = no vehicle assigned yet
    val routeName:    String = "",
    val fareLabel:    String = "USD 1.00",
    val fareMinor:    Int    = 100
)

data class PendingValidation(
    val id:             String,
    val credentialToken: String,
    val createdAt:      Instant,
    val sequence:       Int,
    val fareMinor:      Int,
    val status:         String = "Pending Offline Sync"
)

class ConductorViewModel : ViewModel() {

    var userRole      by mutableStateOf("CONDUCTOR")
    var conductorName by mutableStateOf("Tinashe Moyo")

    var connectivity  by mutableStateOf(Connectivity.ONLINE)
        private set

    var context       by mutableStateOf(VehicleContext())
        private set

    var outcome       by mutableStateOf(ScanOutcome.READY)
        private set

    var declineReason by mutableStateOf<String?>(null)
        private set

    var pending       by mutableStateOf(emptyList<PendingValidation>())
        private set

    var selectedTab   by mutableIntStateOf(0)

    /** True once the conductor has explicitly selected a vehicle for this shift. */
    var vehicleConfirmed by mutableStateOf(false)
        private set

    /** Drives the full-screen tap result overlay in the UI (BR-2). */
    var showResultOverlay by mutableStateOf(false)
        private set

    val availableVehicles = listOf(
        VehicleContext("NUST-BUS-01",  "NUST Main Campus — Bulawayo CBD",   "USD 1.00", 100),
        VehicleContext("NUST-BUS-02",  "NUST Main Campus — Selbourne Park",  "USD 0.50",  50),
        VehicleContext("COMM-BUS-88",  "NUST Main Campus — Hillside",        "USD 0.75",  75)
    )

    fun selectVehicle(newContext: VehicleContext) {
        context          = newContext
        vehicleConfirmed = true
        outcome          = ScanOutcome.READY
    }

    fun toggleConnectivity() {
        connectivity = if (connectivity == Connectivity.ONLINE) Connectivity.OFFLINE else Connectivity.ONLINE
        outcome      = ScanOutcome.READY
    }

    fun processDemoCredential(opaqueToken: String = "eyJ1c2VySWQiOiJ1c3Jfc3R1ZGVudF8wMDEifQ.sig123") {
        if (connectivity == Connectivity.ONLINE) {
            viewModelScope.launch {
                try {
                    val req = ValidationRequestDto(
                        opaqueToken = opaqueToken,
                        vehicleRegistration = if (context.registration.isEmpty()) "NUST-BUS-01" else context.registration,
                        fareMinor = context.fareMinor,
                        isOfflineMode = false
                    )
                    val res = ConductorApiClient.api.validateScan(req)
                    if (res.isSuccessful && res.body() != null) {
                        val body = res.body()!!
                        outcome = when (body.outcome) {
                            "CONFIRMED" -> ScanOutcome.CONFIRMED
                            "PROVISIONAL" -> ScanOutcome.PROVISIONAL
                            else -> ScanOutcome.DECLINED
                        }
                        declineReason = body.reason
                    } else {
                        outcome = ScanOutcome.CONFIRMED
                        declineReason = null
                    }
                } catch (_: Exception) {
                    outcome = ScanOutcome.CONFIRMED
                    declineReason = null
                }
                showResultOverlay = true
            }
            return
        }

        // Bounded offline logic
        if (pending.size >= 10) {
            outcome           = ScanOutcome.DECLINED
            declineReason     = "You have 10 offline passengers queued. Please connect to the internet to sync before accepting more."
            showResultOverlay = true
            return
        }

        val next = PendingValidation(
            id              = "val_off_${UUID.randomUUID().toString().take(8)}",
            credentialToken = opaqueToken,
            createdAt       = Instant.now(),
            sequence        = pending.size + 1,
            fareMinor       = context.fareMinor
        )
        pending           = pending + next
        outcome           = ScanOutcome.PROVISIONAL
        declineReason     = null
        showResultOverlay = true
    }

    fun syncOfflineBatch() {
        viewModelScope.launch {
            try {
                for (item in pending) {
                    val req = ValidationRequestDto(
                        opaqueToken = item.credentialToken,
                        vehicleRegistration = if (context.registration.isEmpty()) "NUST-BUS-01" else context.registration,
                        fareMinor = item.fareMinor,
                        isOfflineMode = true
                    )
                    ConductorApiClient.api.validateScan(req)
                }
            } catch (_: Exception) {}
            pending           = emptyList()
            outcome           = ScanOutcome.CONFIRMED
            declineReason     = null
            showResultOverlay = false
        }
    }

    /** Called by the UI overlay after its auto-dismiss timer completes. */
    fun dismissResultOverlay() {
        showResultOverlay = false
    }

    fun clearOutcome() {
        outcome       = ScanOutcome.READY
        declineReason = null
    }
}
