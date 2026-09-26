package zw.ac.nust.mobility.student.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

data class BalanceResponseDto(
    val userId: String,
    val balanceMinor: Int,
    val currency: String,
    val updatedAt: String
)

data class TopUpRequestDto(
    val amountMinor: Int
)

data class CredentialResponseDto(
    val credentialId: String,
    val opaqueToken: String,
    val expiresAt: String,
    val ttlSeconds: Int
)

data class ApiJourneyDto(
    val id: String,
    val route: String,
    val amountFormatted: String,
    val dateFormatted: String,
    val status: String,
    val vehicleReg: String
)

data class JourneysResponseDto(
    val journeys: List<ApiJourneyDto>
)

interface MundoStudentApi {
    @GET("v1/me/balance")
    suspend fun getBalance(
        @Header("Authorization") token: String = "Bearer token_usr_student_001"
    ): Response<BalanceResponseDto>

    @POST("v1/me/top-up")
    suspend fun topUpBalance(
        @Body req: TopUpRequestDto,
        @Header("Authorization") token: String = "Bearer token_usr_student_001"
    ): Response<BalanceResponseDto>

    @POST("v1/me/credentials")
    suspend fun issueCredential(
        @Header("Authorization") token: String = "Bearer token_usr_student_001"
    ): Response<CredentialResponseDto>

    @GET("v1/me/journeys")
    suspend fun getJourneys(
        @Header("Authorization") token: String = "Bearer token_usr_student_001"
    ): Response<JourneysResponseDto>
}

object StudentApiClient {
    // 10.0.2.2 is Android Emulator alias for host localhost; configurable for local LAN IP
    private var baseUrl: String = "http://10.0.2.2:3000/"

    fun setBaseUrl(url: String) {
        baseUrl = if (url.endsWith("/")) url else "$url/"
    }

    val api: MundoStudentApi by lazy {
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
            .create(MundoStudentApi::class.java)
    }
}
