package apps.dcoder.smartbellcontrol

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*

private const val CODE_REQUEST_AUDIO_FILE: Int = 1

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize firebase cloud messaging
        FirebaseApp.initializeApp(this)

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

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("DK", "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg ="Current firebase token: $token"
                Log.e("DK", msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            })

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
}
