package apps.dcoder.smartbellcontrol.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.media.RingtoneManager
import apps.dcoder.smartbellcontrol.R


class BellFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var notificationManager: NotificationManager

    override fun onCreate() {
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    companion object {
        private const val CHANNEL_ID = "Bell"
        private const val NOTIFICATION_ID = 123
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Ring notifications"
            val descriptionText = "Smart bell ring notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun constructNotification(title: String?, contentText: String?): Notification {
        createNotificationChannel()

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        return NotificationCompat.Builder(applicationContext, CHANNEL_ID).apply {
            setSmallIcon(R.drawable.notification_icon_background)
            setSound(defaultSoundUri)
            setVibrate(longArrayOf(1000, 1000))
            setContentTitle(title)
            setContentText(contentText)
            priority = NotificationCompat.PRIORITY_HIGH
        }.build()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        Log.d("DK", "Push message received")
        if (remoteMessage == null) {
            Log.e("DK", "Empty push message received")
            return
        }

        if(remoteMessage.notification != null) {
            val title = remoteMessage.notification?.title ?: "Empty"
            val contentText = remoteMessage.notification?.body ?: "Empty"

            val notification = constructNotification(title, contentText)
            notificationManager.notify(NOTIFICATION_ID, notification)

            Log.d("DK", "Notification constructed from message type \'Notification\'")
        }

    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)

        Log.d("DK", "Firebase token: $token")
    }
}
