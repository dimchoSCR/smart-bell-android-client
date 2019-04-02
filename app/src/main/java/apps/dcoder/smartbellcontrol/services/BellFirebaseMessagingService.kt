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
import apps.dcoder.smartbellcontrol.BellDashboardActivity
import apps.dcoder.smartbellcontrol.MainActivity
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.formatRawRingEntry
import apps.dcoder.smartbellcontrol.restapiclient.model.RawRingEntry
import apps.dcoder.smartbellcontrol.utils.toLocalizedPointSeparatedString
import apps.dcoder.smartbellcontrol.work.SendFCMAppTokenWorker
import apps.dcoder.smartbellcontrol.work.Workers
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.CollectionType




class BellFirebaseMessagingService : FirebaseMessagingService() {

    private lateinit var notifier: Notifier

    private enum class NotificationType {
        RING, MESSAGE
    }

    override fun onCreate() {
        notifier = Notifier()
    }

    companion object {

        private const val NOTIFICATION_CHANNEL_ID = "Bell"
        private const val NOTIFICATION_CHANNEL_NAME = "Ring notifications"
        private const val NOTIFICATION_DATE_TIME_FORMAT = "HH:mm, dd MMM"

        private const val NOTIFICATION_ID = 123
        private const val DISTURB_NOTIFICATION_ID = 124
        private const val REQUEST_CODE_GO_TO_LOG = 15

        private const val FIREBASE_NOTIFICATION_TYPE = "NotificationType"
        private const val KEY_NOTIFICATION_DATA = "Data"

        public const val EXTRA_OPEN_LOG_FRAGAMENT = "Log"
    }

    private inner class Notifier {
        private val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fun createNotificationChannel(isInDoNotDisturb: Boolean) {
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
                    enableVibration(!isInDoNotDisturb)
                    setImportance(importance)
                }

                notificationManager.createNotificationChannel(channel)
            }
        }

        fun constructNotification(title: String?, contentText: String?, isInDoNotDisturb: Boolean): Notification {
            createNotificationChannel(isInDoNotDisturb)

            return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID).apply {
                setSmallIcon(R.drawable.ic_ring_app_color_primary_24dp)
                setContentTitle(title)
                setAutoCancel(true)
                setContentText(contentText)
                setContentIntent(createPendingIntent<BellDashboardActivity>())
                priority = NotificationCompat.PRIORITY_HIGH

                if (!isInDoNotDisturb) {
                    setVibrate(longArrayOf(1000, 1000))
                    setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                }
            }.build()
        }

        fun constructDoNotDisturbReportNotification(content: String): Notification {
            createNotificationChannel(true)

            return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID).apply {
                setSmallIcon(R.drawable.ic_ring_app_color_primary_24dp)
                setContentTitle(getString(R.string.ring_notification_title))
                setContentText(getString(R.string.missed_ring))
                setAutoCancel(false)
                .setStyle(NotificationCompat.BigTextStyle()
                        .bigText(content))
                setContentIntent(createPendingIntent<BellDashboardActivity>())
                priority = NotificationCompat.PRIORITY_HIGH

            }.build()
        }

        fun notifyUser(notificationId: Int, notification: Notification) {
            notificationManager.notify(notificationId, notification)
        }
    }

    private inline fun<reified T> createPendingIntent(): PendingIntent {
        val intentToLog = Intent(applicationContext, T::class.java)
        intentToLog.putExtra(EXTRA_OPEN_LOG_FRAGAMENT, true)

        return PendingIntent.getActivity(
            applicationContext,
            REQUEST_CODE_GO_TO_LOG,
            intentToLog,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
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
                if (data[KEY_NOTIFICATION_DATA] == null) {
                    Log.e("FirebaseDK", "Notification payload should include DoNotDisturb status!")
                    return
                }

                val isInDoNotDisturb = data[KEY_NOTIFICATION_DATA]!!.toBoolean()
                if (!isInDoNotDisturb && !data[KEY_NOTIFICATION_DATA].equals("false", true)) {
                    Log.e("FirebaseDK", "DoNotDisturb payload should be of type boolean!")
                    return
                }

                val notification = notifier.constructNotification(
                    getString(R.string.ring_notification_title),
                    getString(R.string.ring_notification_content),
                    isInDoNotDisturb
                )

                notifier.notifyUser(NOTIFICATION_ID, notification)
                Log.d("FirebaseDK", "Notification constructed from data message")
            }

            NotificationType.MESSAGE -> {
                Log.d("FirebaseDK", "Message received from server!")
                if (data[KEY_NOTIFICATION_DATA] == null) {
                    Log.e("FirebaseDK", "Notification payload should include ring data!")
                    return
                }

                val objectMapper = ObjectMapper()
                val collectionType: CollectionType = objectMapper.typeFactory.constructCollectionType(
                    List::class.java, RawRingEntry::class.java
                )

                val rawRingEntryList = objectMapper.readValue<List<RawRingEntry>>(data[KEY_NOTIFICATION_DATA], collectionType)
                val ringTimes: List<String> = rawRingEntryList.map {
                    formatRawRingEntry(it, NOTIFICATION_DATE_TIME_FORMAT).formattedDateTime
                }.distinct()

                val notificationContent = ringTimes.toLocalizedPointSeparatedString(applicationContext)
                Log.e("FirebaseDK", notificationContent)

                notifier.notifyUser(
                    DISTURB_NOTIFICATION_ID,
                    notifier.constructDoNotDisturbReportNotification(notificationContent)
                )
            }
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
