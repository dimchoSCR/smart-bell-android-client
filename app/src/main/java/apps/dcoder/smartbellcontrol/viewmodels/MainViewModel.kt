package apps.dcoder.smartbellcontrol.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import apps.dcoder.smartbellcontrol.formatRawMelodyInfo
import apps.dcoder.smartbellcontrol.restapiclient.RetrofitAPIs
import apps.dcoder.smartbellcontrol.restapiclient.SmartBellAPI
import apps.dcoder.smartbellcontrol.restapiclient.model.RawMelodyInfo
import apps.dcoder.smartbellcontrol.restapiclient.model.MelodyInfo
import apps.dcoder.smartbellcontrol.utils.getBytesAsync
import apps.dcoder.smartbellcontrol.utils.getFileName
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.lang.IllegalStateException

class MainViewModel(private val appContext: Application) : AndroidViewModel(appContext) {

    private val bellAPI: SmartBellAPI = RetrofitAPIs.bellAPI

    private fun sendUploadRequest(fileBytes: ByteArray, fileName: String, mediaType: String?) {
        // Create necessary parameter for the bell api call
        val requestFile: RequestBody = RequestBody.create(MediaType.parse(mediaType), fileBytes)
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData("file", fileName, requestFile)

        // Make the async bell api call
        val retrofitCall: Call<Void> = RetrofitAPIs.bellAPI.uploadMelody(filePart)
        retrofitCall.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    Log.d("DK", response.message() ?: "Melody upload successful!")
                    status.value = "Upload was successful!"
                } else {
                    Log.e("DK", response.errorBody()?.string() ?: "Unknown error occurred!")
                    status.value = "Upload unsuccessful because of a server error!"
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("DK", "Upload failed!", t)
                status.value = "Upload failed!"
            }
        })
    }

    val status = MutableLiveData<String>()
    val melodyList = MutableLiveData<List<MelodyInfo>>()

    fun uploadFile(uriToAudioFile: Uri?) {
        // Extract content information from uri
        val audioFileName = uriToAudioFile!!.getFileName() // Kotlin extension function
        val mediaType: String? = appContext.contentResolver.getType(uriToAudioFile)

        val inputStream = appContext
            .contentResolver
            .openInputStream(uriToAudioFile) ?: throw IllegalStateException("Can not get input stream from media uri!")

        // Sends upload request after melody bytes have been read from the input stream
        // Uses a kotlin extension function
        inputStream.getBytesAsync { fileBytes ->
            inputStream.close()
            sendUploadRequest(fileBytes, audioFileName, mediaType)

        }

    }

    fun setAsRingtone(melodyName: String) {

        val body: RequestBody = RequestBody.create(MediaType.parse("text/plain"), melodyName)
        val retrofitCall: Call<ResponseBody> = bellAPI.updateRingtone(body)

        retrofitCall.enqueue(object : Callback<ResponseBody> {

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("DK", "Setting ringtone failed failed!", t)
                status.value = "Setting melody failed!"
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful) {
                    Log.d("DK", "Setting ringtone was successful!")
                    status.value = response.body()!!.string()
                } else {
                    Log.d("DK", response.errorBody()?.string() ?: "Unknown error")
                    status.value = "Ringtone not set because of a server error."
                }
            }

        })

    }

    fun loadMelodyList() {
        val retrofitCall: Call<List<RawMelodyInfo>> = bellAPI.getAvailableMelodies()
        retrofitCall.enqueue(object : Callback<List<RawMelodyInfo>> {
            override fun onFailure(call: Call<List<RawMelodyInfo>>, t: Throwable) {
                Log.e("DK", "Retrieving melody list failed failed!", t)
                status.value = "Retrieving melodies failed!"
            }

            override fun onResponse(call: Call<List<RawMelodyInfo>>, response: Response<List<RawMelodyInfo>>) {
                if(response.isSuccessful) {
                    Log.d("DK", "Retrieving melody list was successful!")
                    status.value = "Retrieving melody list was successful!"
                    val melodies = response.body()
                    val localizedMelodies = melodies?.map {
                            rawMelodyInfo ->
                        formatRawMelodyInfo(appContext, rawMelodyInfo)
                    } ?: throw Throwable("List of melody info should not be null")

                    melodyList.value = localizedMelodies
                } else {
                    Log.d("DK", response.errorBody()?.string() ?: "Unknown error")
                    status.value = "Retrieving failed because of a server error."
                }
            }
        })
    }
}