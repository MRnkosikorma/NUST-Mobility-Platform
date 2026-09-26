package zw.ac.nust.mobility.student

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

class MobilityCardService : HostApduService() {

    companion object {
        private const val TAG = "MobilityCardService"

        // SELECT AID APDU command (00 A4 04 00 07 F0 01 02 03 04 05 06)
        private val SELECT_AID_APDU = byteArrayOf(
            0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
            0x07.toByte(), 0xF0.toByte(), 0x01.toByte(), 0x02.toByte(),
            0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
        )

        private val SW_SUCCESS = byteArrayOf(0x90.toByte(), 0x00.toByte())
        private val SW_FAILURE = byteArrayOf(0x6A.toByte(), 0x82.toByte())

        @Volatile
        var activeToken: String =
            "eyJ1c2VySWQiOiJ1c3Jfc3R1ZGVudF8wMDEiLCJpbnN0aXR1dGlvbklkIjoibnVzdCJ9.default_sig"

        /**
         * MainActivity registers a callback here so the HCE service can notify the UI
         * when a successful tap-exchange completes — no broadcast library required.
         */
        @Volatile
        var onTapComplete: (() -> Unit)? = null

        /** Set true after SELECT AID succeeds so deactivation knows a tap actually occurred. */
        @Volatile
        private var tapSucceeded = false
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (commandApdu == null) return SW_FAILURE

        if (commandApdu.contentEquals(SELECT_AID_APDU)) {
            Log.d(TAG, "SELECT AID received — transmitting token over NFC.")
            tapSucceeded = true
            val payloadBytes = activeToken.toByteArray(Charsets.UTF_8)
            val response = ByteArray(payloadBytes.size + SW_SUCCESS.size)
            System.arraycopy(payloadBytes, 0, response, 0, payloadBytes.size)
            System.arraycopy(SW_SUCCESS, 0, response, payloadBytes.size, SW_SUCCESS.size)
            return response
        }

        return SW_FAILURE
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "HCE deactivated reason=$reason tapSucceeded=$tapSucceeded")
        if (tapSucceeded) {
            onTapComplete?.invoke()   // notify MainActivity on the HCE thread; UI posts to main
        }
        tapSucceeded = false
    }
}
