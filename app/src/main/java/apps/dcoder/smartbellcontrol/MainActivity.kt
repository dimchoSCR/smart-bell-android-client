package apps.dcoder.smartbellcontrol

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import apps.dcoder.smartbellcontrol.prefs.PreferenceKeys
import apps.dcoder.smartbellcontrol.services.BellFirebaseMessagingService
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

private const val CODE_REQUEST_AUDIO_FILE: Int = 1

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel

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
        setContentView(R.layout.activity_main)

        requestGooglePlayServicesAvailability()
        generateAndStoreAppInstanceGUID()

        val linearLayoutManager = LinearLayoutManager(this)
        with (rvMelodies) {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = MelodyInfoAdapter(listOf())
        }

        // Opens a song picker window
        btnUploadMelody.setOnClickListener{ pickMelody() }

        // Listen for service response
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.status.observe(this, Observer {
            progressBar.visibility = View.GONE
            tvStatus.text = it
        })

        viewModel.melodyList.observe(this, Observer { melodies ->
            val adapter = rvMelodies.adapter as MelodyInfoAdapter
            adapter.melodies = melodies
            adapter.notifyDataSetChanged()
        })

        btnListMelodies.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            viewModel.loadMelodyList()
        }

//        btnSetAsRingtone.setOnClickListener{
//            progressBar.visibility = View.VISIBLE
//            viewModel.setAsRingtone("The_Stratosphere_MP3.mp3")
//        }
    }

    override fun onResume() {
        super.onResume()
        requestGooglePlayServicesAvailability(false)
    }

    private fun pickMelody() {
        val pickMelodyIntent = Intent(Intent.ACTION_GET_CONTENT)
        pickMelodyIntent.type = "audio/*"
        startActivityForResult(pickMelodyIntent, CODE_REQUEST_AUDIO_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_REQUEST_AUDIO_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("DK", "Melody pick successful")
                viewModel.uploadFile(data?.data)
                progressBar.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Picking cancelled!", Toast.LENGTH_LONG).show();
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.app_tool_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return return when (item.itemId) {
            R.id.item_settings -> {
                val opneSettingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(opneSettingsIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
