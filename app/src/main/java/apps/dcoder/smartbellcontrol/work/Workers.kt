package apps.dcoder.smartbellcontrol.work

import androidx.work.*
import java.util.concurrent.TimeUnit

object Workers {

    fun createUpdateTokenWorkRequest(token: String): OneTimeWorkRequest {

        val requestConstraints: Constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val tokenData = Data.Builder()
            .putString(SendFCMAppTokenWorker.KEY_DATA_TOKEN, token)
            .build()

        return OneTimeWorkRequest.Builder(SendFCMAppTokenWorker::class.java).apply {
                setConstraints(requestConstraints)
                setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    OneTimeWorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                setInputData(tokenData)
                addTag(SendFCMAppTokenWorker.TAG_SEND_TOKEN_WORKER)
            }.build()
    }
}