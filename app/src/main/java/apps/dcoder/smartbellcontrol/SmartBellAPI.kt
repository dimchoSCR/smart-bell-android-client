package apps.dcoder.smartbellcontrol

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface SmartBellAPI {
    @Multipart
    @POST("melodies/upload")
    fun uploadMelody(@Part file: MultipartBody.Part): Call<Void>
}