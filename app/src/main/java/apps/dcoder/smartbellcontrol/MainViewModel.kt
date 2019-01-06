package apps.dcoder.smartbellcontrol

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import apps.dcoder.smartbellcontrol.util.getBytesAsync
import apps.dcoder.smartbellcontrol.util.getFileName
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.lang.IllegalStateException

private const val BELL_API_BASE_URL = "http://192.168.1.91:8080/"

class MainViewModel(private val appContext: Application) : AndroidViewModel(appContext), Callback<Void> {

    private val retrofit: Retrofit = Retrofit.Builder()
         .baseUrl(BELL_API_BASE_URL)
         .addConverterFactory(JacksonConverterFactory.create())
         .build()

    private val bellAPI: SmartBellAPI = retrofit.create(SmartBellAPI::class.java)

    private fun sendUploadRequest(fileBytes: ByteArray, fileName: String, mediaType: String?) {

        val requestFile: RequestBody = RequestBody.create(MediaType.parse(mediaType), fileBytes)
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData("file", fileName, requestFile)

        val retrofitCall: Call<Void> = bellAPI.uploadMelody(filePart)
        retrofitCall.enqueue(this)
    }

    val status = MutableLiveData<String>()

    fun uploadFile(uriToAudioFile: Uri?) {

        val audioFileName = uriToAudioFile!!.getFileName()
        val mediaType: String? = appContext.contentResolver.getType(uriToAudioFile)

        val inputStream = appContext
            .contentResolver
            .openInputStream(uriToAudioFile) ?: throw IllegalStateException("Can not get input stream from media uri!")

        inputStream.getBytesAsync { fileBytes -> sendUploadRequest(fileBytes, audioFileName, mediaType) }

    }

    override fun onResponse(call: Call<Void>, response: Response<Void>) {
        Log.d("DK", "Upload successful!")
        status.value = "Upload was successful!"
    }

    override fun onFailure(call: Call<Void>, t: Throwable) {
        Log.e("DK", "Upload failed!", t)
        status.value = "Upload failed!"
    }
}