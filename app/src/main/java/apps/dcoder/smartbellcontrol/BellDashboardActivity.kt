package apps.dcoder.smartbellcontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import androidx.work.WorkManager
import apps.dcoder.smartbellcontrol.fragments.DashboardFragment
import apps.dcoder.smartbellcontrol.fragments.LogDetailsFragment
import apps.dcoder.smartbellcontrol.prefs.PreferenceKeys
import apps.dcoder.smartbellcontrol.services.BellFirebaseMessagingService
import apps.dcoder.smartbellcontrol.viewmodels.BellDashboardViewModel
import apps.dcoder.smartbellcontrol.work.SendFCMAppTokenWorker
import apps.dcoder.smartbellcontrol.work.Workers
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import java.util.*
import androidx.work.WorkInfo
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId

class BellDashboardActivity : AppCompatActivity() {

    private fun initializePushNotificationService() {
        // Initialize firebase cloud messaging
        FirebaseApp.initializeApp(this)
        Log.d("FirebaseDK", "Initialized firebase for app")
    }

    private fun requestGooglePlayServicesAvailability(reinitializeFCM: Boolean = true) {

        val googleApiAvailability = GoogleApiAvailability.getInstance()
        googleApiAvailability.makeGooglePlayServicesAvailable(this)
            .addOnSuccessListener {
                if (reinitializeFCM) {
                    initializePushNotificationService()
                }
            }
            .addOnFailureListener { finish() }

    }

    private fun generateAndStoreAppInstanceGUID() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (!sharedPreferences.contains(PreferenceKeys.PREFERENCE_KEY_APP_GUID)) {
            val appGUID = UUID.randomUUID().toString()
            sharedPreferences.edit()
                .putString(PreferenceKeys.PREFERENCE_KEY_APP_GUID, appGUID)
                .apply()
        }
    }

    private fun ensureFireBaseTokenIsRegistered() {
        val workManager = WorkManager.getInstance()
        val workInfo = workManager.getWorkInfosByTag(SendFCMAppTokenWorker.TAG_SEND_TOKEN_WORKER)

        if (!workInfo.get().isEmpty() && workInfo.get()[0].state != WorkInfo.State.SUCCEEDED) {
            // Cancel an old deferred job if it is still not completed and start a new one
            workManager.cancelAllWorkByTag(SendFCMAppTokenWorker.TAG_SEND_TOKEN_WORKER)

            FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener(OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e("FirebaseDK", "getInstanceId failed", task.exception)
                        return@OnCompleteListener
                    }

                    // Get new Instance ID token
                    val token = task.result!!.token
                    Log.d("FirebaseDK", "Token read. Rescheduling work!")

                    val workRequest = Workers.createUpdateTokenWorkRequest(token)
                    workManager.enqueue(workRequest)
                })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bell_dashboard)

        requestGooglePlayServicesAvailability()
        generateAndStoreAppInstanceGUID()
        ensureFireBaseTokenIsRegistered()

        val dashboardViewModel = ViewModelProviders.of(this).get(BellDashboardViewModel::class.java)
        dashboardViewModel.loadRingLog()
        dashboardViewModel.loadBellStatus()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, DashboardFragment())
                .commit()
        }

        if (intent != null && intent.hasExtra(BellFirebaseMessagingService.EXTRA_OPEN_LOG_FRAGAMENT)) {
            supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, LogDetailsFragment())
                .commit()

        }
    }

    override fun onResume() {
        super.onResume()
        requestGooglePlayServicesAvailability(false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.app_tool_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.item_settings -> {
                val openSettingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(openSettingsIntent)
                true
            }

            R.id.item_melodies -> {
                startActivity(Intent(this, MelodyActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
