package apps.dcoder.smartbellcontrol.restapiclient

import apps.dcoder.smartbellcontrol.restapiclient.model.BellStatus
import apps.dcoder.smartbellcontrol.restapiclient.model.RawMelodyInfo
import apps.dcoder.smartbellcontrol.restapiclient.model.RawRingEntry
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

    @GET("melodies/log")
    fun getRingLogEntries(@Query("compSign") compSign: String,
                          @Query("timeString") timeString: String): Call<List<RawRingEntry>>

    @GET("melodies/log")
    fun getAllLogEntries(): Call<List<RawRingEntry>>

    @FormUrlEncoded
    @PUT("notifications/register")
    fun registerAppForPushNotifications(@FieldMap params: Map<String, String>): Call<Void>

    @FormUrlEncoded
    @PUT("melodies/donotdisturb/rules")
    fun scheduleDoNotDisturb(
        @Field("days") days: ArrayList<Int>,
        @Field("startTime") startTime: Long,
        @Field("endTime") endTime: Long,
        @Field("endTomorrow") endTomorrow: Boolean
    ): Call<Void>


    @GET("melodies/donotdisturb/status")
    fun getDoNotDisturbStatus(): Call<BellStatus.DoNotDisturbStatus>
}