package apps.dcoder.smartbellcontrol.restapiclient

import apps.dcoder.smartbellcontrol.restapiclient.model.RawMelodyInfo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface SmartBellAPI {
    @Multipart
    @POST("melodies/upload")
    fun uploadMelody(@Part file: MultipartBody.Part): Call<Void>

    @PUT("melodies/update/ringtone")
    fun updateRingtone(@Body name: RequestBody): Call<ResponseBody>

    @GET("melodies/")
    fun getAvailableMelodies(): Call<List<RawMelodyInfo>>
}