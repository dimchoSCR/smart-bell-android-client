package apps.dcoder.smartbellcontrol.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.media.RingtoneManager
import androidx.work.*
import apps.dcoder.smartbellcontrol.MainActivity
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.work.SendFCMAppTokenWorker
import apps.dcoder.smartbellcontrol.work.Workers

class BellFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var notificationManager: NotificationManager

    private enum class NotificationType {
        RING, MESSAGE
    }

    override fun onCreate() {
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "Bell"
        private const val NOTIFICATION_CHANNEL_NAME = "Ring notifications"
        private const val NOTIFICATION_ID = 123
        private const val FIREBASE_NOTIFICATION_TYPE = "NotificationType"
        private const val REQUEST_CODE_GO_TO_LOG = 15
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = NOTIFICATION_CHANNEL_NAME
            val descriptionText = getString(R.string.ring_notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                setImportance(importance)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private inline fun<reified T> createPendingIntent(): PendingIntent {
        val intentToLog = Intent(applicationContext, T::class.java)
        return PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE_GO_TO_LOG,
            intentToLog,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun constructNotification(title: String?, contentText: String?): Notification {
        createNotificationChannel()

        return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID).apply {
            setSmallIcon(R.drawable.ic_ring_app_color_primary_24dp)
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            setVibrate(longArrayOf(1000, 1000))
            setContentTitle(title)
            setAutoCancel(true)
            setContentText(contentText)
            setContentIntent(createPendingIntent<MainActivity>())
            priority = NotificationCompat.PRIORITY_HIGH
        }.build()
    }

    private fun processDataPayload(data: MutableMap<String, String>) {

        Log.d("FirebaseDK", "Remote data received $data")
        val notificationTypeString = data[FIREBASE_NOTIFICATION_TYPE]
        if (notificationTypeString == null || notificationTypeString.isEmpty()) {
            Log.e("FirebaseDK", "Notification type should not be empty or null!")
            return
        }

        val notificationType = NotificationType.valueOf(notificationTypeString)
        Log.d("FirebaseDK", "Notification type: $notificationType")
        when (notificationType) {
            NotificationType.RING -> {
                val notification = constructNotification(
                    getString(R.string.ring_notification_title),
                    getString(R.string.ring_notification_content)
                )

                notificationManager.notify(NOTIFICATION_ID, notification)
                Log.d("FirebaseDK", "Notification constructed from data message")
            }

            NotificationType.MESSAGE -> Log.d("FirebaseDK", "Message received from server!")
        }

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        Log.d("DK", "Push message received")
        if (remoteMessage == null) {
            Log.e("DK", "Empty push message received")
            return
        }

        if (remoteMessage.data == null) {
            Log.e("FirebaseDK", "Unsupported notification type sent! Only data messages supported.")
            return
        }

        processDataPayload(remoteMessage.data)
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)

        if (token == null) {
            Log.e("FirebaseDK", "Token must not be null on renew!")
            return
        }

        Log.d("FirebaseDK", "On new token: $token")
        val sendTokenToServerWorkRequest = Workers.createUpdateTokenWorkRequest(token)
        val workManager = WorkManager.getInstance()

        // Cancel an old deferred job if it is still not completed
        workManager.cancelAllWorkByTag(SendFCMAppTokenWorker.TAG_SEND_TOKEN_WORKER)
        workManager.enqueue(sendTokenToServerWorkRequest)

    }

    override fun onDeletedMessages() {
        Log.d("FirebaseDK", "Deleted messages exist")
    }
}
