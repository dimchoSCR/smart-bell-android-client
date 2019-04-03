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
import apps.dcoder.smartbellcontrol.fragments.DashboardFragment
import apps.dcoder.smartbellcontrol.fragments.LogDetailsFragment
import apps.dcoder.smartbellcontrol.prefs.PreferenceKeys
import apps.dcoder.smartbellcontrol.services.BellFirebaseMessagingService
import apps.dcoder.smartbellcontrol.viewmodels.BellDashboardViewModel
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import java.util.*


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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bell_dashboard)

        requestGooglePlayServicesAvailability()
        generateAndStoreAppInstanceGUID()

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
                .addToBackStack(null)
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
                startActivity(Intent(this, MainActivity::class.java))
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}
