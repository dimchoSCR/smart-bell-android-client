package apps.dcoder.smartbellcontrol.work

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.restapiclient.SmartBellAPI
import apps.dcoder.smartbellcontrol.restapiclient.model.RawRingEntry
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.sql.Timestamp
import java.util.*

class PollRinglogWorker(context: Context, workerParams: WorkerParameters): Worker(context, workerParams) {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.91:8080/")
        .addConverterFactory(JacksonConverterFactory.create())
        .build()

    private val bellAPI: SmartBellAPI = retrofit.create(SmartBellAPI::class.java)
    private val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var lastSavedStamp: String = Timestamp(Calendar.getInstance().timeInMillis).toString()

    private fun constructNotification(): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon_background)
            .setContentTitle("Ring, Ring ...")
            .setContentText("Someone's at your door!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Bell notifications"
            val descriptionText = "Smart bell notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun doWork(): Result {
        val sharedPreferences = applicationContext.getSharedPreferences("bellPrefs", Context.MODE_PRIVATE)
        val currentTime = sharedPreferences.getString("time", "2019-02-17 16:18:19.7")

        val retrofitCall = bellAPI.getRingLogEntries(">", currentTime)
        val response: Response<List<RawRingEntry>> = retrofitCall.execute()
        val ringList = response.body() ?: return Result.failure()

        if(ringList.isEmpty()) {
            return Result.success()
        }

        sharedPreferences.edit().putString("time", ringList[ringList.size - 1].dateTime).apply()

        val notification = constructNotification()

        notificationManager.notify(12, notification)

        return Result.success()
    }

    companion object {
        private const val CHANNEL_ID = "Bell"
    }

}