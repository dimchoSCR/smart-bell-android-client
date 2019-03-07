package apps.dcoder.smartbellcontrol.work

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import apps.dcoder.smartbellcontrol.prefs.PreferenceKeys
import apps.dcoder.smartbellcontrol.restapiclient.RetrofitAPIs
import retrofit2.Response
import java.io.IOException

class SendFCMAppTokenWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    private val bellAPI = RetrofitAPIs.bellAPI
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

    companion object {
        const val KEY_DATA_TOKEN = "FirebaseToken"
        const val TAG_SEND_TOKEN_WORKER = "SendTokenWorker"
    }

    override fun doWork(): Result {

        val token = inputData.getString(KEY_DATA_TOKEN)

        if (token == null) {
            Log.e("FirebaseDK", "Token reference must not be null!")
            return Result.failure()
        }

        val appGUID = sharedPreferences.getString(PreferenceKeys.PREFERENCE_KEY_APP_GUID, null)
        if (appGUID == null) {
            Log.e("FirebaseDK", "AppGUID must not be null!")
            return Result.failure()
        }

        val requestParams = mapOf("appGUID" to appGUID, "firebaseToken" to token)
        try {
            val response: Response<Void> = bellAPI.registerAppForPushNotifications(requestParams).execute()

            if(!response.isSuccessful) {
                Log.e(
                    "FirebaseDK",
                    response.errorBody()
                        ?.string() ?: "Unknown error occurred while registering app for push notifications!"
                )

                return Result.failure()
            }
        } catch (err: IOException) {
            Log.d("FirebaseDK", "Retrying because of exception: ${err.message}")
            // Retry sending token with exponential backoff
            return Result.retry()
        } catch (err: Exception) {
            Log.e("Firebase DK", "Error occurred while sending renewed token to server!", err)
            return Result.failure()
        }

        Log.d("FirebaseDK", "Successfully sent token to server!")
        return Result.success()
    }
}