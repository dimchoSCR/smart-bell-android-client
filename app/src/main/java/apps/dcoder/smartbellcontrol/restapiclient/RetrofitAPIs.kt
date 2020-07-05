package apps.dcoder.smartbellcontrol.restapiclient

import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory

object RetrofitAPIs {
//    private const val BELL_API_BASE_URL = "http://192.168.1.91:8080/"
    private const val BELL_API_BASE_URL = "http://192.168.43.50:8080/"

    private inline fun<reified T> buildRetrofitAPI(baseURL: String): T {
        return Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(JacksonConverterFactory.create())
            .build()
            .create(T::class.java)
    }

    val bellAPI: SmartBellAPI = buildRetrofitAPI(BELL_API_BASE_URL)
}