package apps.dcoder.smartbellcontrol

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import apps.dcoder.smartbellcontrol.util.getBytes
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.File

private const val BELL_API_BASE_URL = "http://192.168.1.91:8080/"

class MainViewModel(private val appContext: Application) : AndroidViewModel(appContext), Callback<Void> {

    private val retrofit: Retrofit = Retrofit.Builder()
         .baseUrl(BELL_API_BASE_URL)
         .addConverterFactory(JacksonConverterFactory.create())
         .build()

    private val bellAPI: SmartBellAPI = retrofit.create(SmartBellAPI::class.java)

    val status = MutableLiveData<String>()

    fun uploadFile(uriToAudioFile: Uri?) {
        val uriToAudioFileString = uriToAudioFile!!.path
        Log.d("DK",uriToAudioFileString)

        val indexOfLastSeparator = uriToAudioFileString.lastIndexOf(File.separatorChar)
        val audioFileName = uriToAudioFileString.substring(indexOfLastSeparator + 1)
        val mediaType: String? = appContext.contentResolver.getType(uriToAudioFile)
        val inputStream = appContext.contentResolver.openInputStream(uriToAudioFile)
        val audioBytes = inputStream.getBytes()

        val requestFile: RequestBody = RequestBody.create(MediaType.parse(mediaType), audioBytes)
        val filePart: MultipartBody.Part = MultipartBody.Part.createFormData("file", audioFileName, requestFile)

        val retrofitCall: Call<Void> = bellAPI.uploadMelody(filePart)
        retrofitCall.enqueue(this)
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