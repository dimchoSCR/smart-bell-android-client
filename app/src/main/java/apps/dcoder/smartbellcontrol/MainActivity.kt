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
import kotlinx.android.synthetic.main.activity_main.*

private const val CODE_REQUEST_AUDIO_FILE: Int = 1

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Opens a song picker window
        btnUploadMelody.setOnClickListener{ pickMelody() }

        // Listen for service response
        val viewModel: MainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.status.observe(this, Observer {
            progressBar.visibility = View.GONE
            tvStatus.text = it
        })

//        btnSetAsRingtone.setOnClickListener{
//            progressBar.visibility = View.VISIBLE
//            viewModel.setAsRingtone("The_Stratosphere_MP3.mp3")
//        }

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
                val viewModel: MainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
                viewModel.uploadFile(data?.data)
                progressBar.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "Picking cancelled!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
