package zw.ac.nust.mobility.conductor.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class ValidationRequestDto(
    val opaqueToken: String,
    val vehicleRegistration: String,
    val routeId: String = "rt_nust_cbd",
    val fareMinor: Int = 100,
    val isOfflineMode: Boolean = false
)

data class ValidationResponseDto(
    val validationId: String,
    val outcome: String,
    val reason: String? = null,
    val timestamp: String,
    val fareMinor: Int,
    val currency: String
)

data class NfcVerifyRequestDto(
    val cardUid: String,
    val vehicleRegistration: String,
    val routeId: String = "rt_nust_cbd",
    val fareMinor: Int = 100,
    val isOfflineMode: Boolean = false
)

data class NfcVerifyResponseDto(
    val validationId: String,
    val outcome: String,
    val reason: String? = null,
    val cardUid: String,
    val userId: String? = null,
    val timestamp: String,
    val fareMinor: Int,
    val currency: String
)

interface MundoConductorApi {
    @POST("v1/conductor/validations")
    suspend fun validateScan(
        @Body req: ValidationRequestDto,
        @Header("Authorization") token: String = "Bearer token_usr_conductor_001"
    ): Response<ValidationResponseDto>

    @POST("v1/nfc/cards/verify")
    suspend fun verifyNfcCard(
        @Body req: NfcVerifyRequestDto,
        @Header("Authorization") token: String = "Bearer token_usr_conductor_001"
    ): Response<NfcVerifyResponseDto>
}

object ConductorApiClient {
    private var baseUrl: String = "http://10.0.2.2:3000/"

    fun setBaseUrl(url: String) {
        baseUrl = if (url.endsWith("/")) url else "$url/"
    }

    val api: MundoConductorApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MundoConductorApi::class.java)
    }
}
