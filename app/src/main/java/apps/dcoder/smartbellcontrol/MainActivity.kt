package apps.dcoder.smartbellcontrol

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import apps.dcoder.smartbellcontrol.adapters.MelodyInfoAdapter
import apps.dcoder.smartbellcontrol.restapiclient.model.BellStatus
import apps.dcoder.smartbellcontrol.viewmodels.MainViewModel
import apps.dcoder.smartbellcontrol.viewmodels.SettingsViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_load.view.*

private const val CODE_REQUEST_AUDIO_FILE: Int = 1

class MainActivity : AppCompatActivity() {
    private lateinit var mainViewModel: MainViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    private var trackingTouch = false

    private fun initVolume() {
        val ringVolume = BellStatus.coreStatus.ringVolume
        sbVolume.progress = ringVolume
        tvVolumeValue.text = Integer.toString(ringVolume)
        sbVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                trackingTouch = true
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                trackingTouch = false
                onProgressChanged(seekBar, seekBar!!.progress, false)
            }

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(!trackingTouch) {
                    settingsViewModel.setRingVolume(progress)
                } else {
                    tvVolumeValue.text = Integer.toString(progress)
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)

        val linearLayoutManager = LinearLayoutManager(this)
        with (rvMelodies) {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = MelodyInfoAdapter(listOf())
        }

        initVolume()

        settingsViewModel.successNotifyLiveData.observe(this, Observer {
            if (!it.consumed) {
                tvVolumeValue.text = Integer.toString(BellStatus.coreStatus.ringVolume)
                it.consume()
            }
        })

        settingsViewModel.errorNotifyLiveData.observe(this, Observer {
            if (!it.consumed) {
                val toast = Toast.makeText(this, getString(R.string.setting_change_failure), Toast.LENGTH_LONG)
                toast.view.background.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                toast.show()

                sbVolume.progress = BellStatus.coreStatus.ringVolume
                tvVolumeValue.text = Integer.toString(BellStatus.coreStatus.ringVolume)
                it.consume()
            }
        })

        ltLoadMelodies.pbLoading.visibility = View.VISIBLE

        // Listen for service response
        mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        mainViewModel.loadMelodyList()

        mainViewModel.errorLiveData.observe(this, Observer { event ->
            if (!event.consumed) {
                ltLoadMelodies.pbLoading.visibility = View.GONE
                ltLoadMelodies.tvError.text = getString(R.string.error_connecting_to_bell)
                event.consume()
            }
        })

        mainViewModel.successLiveData.observe(this, Observer {

            ltLoadMelodies.visibility = View.GONE
            Toast.makeText(this, "Operation successful", Toast.LENGTH_LONG).show()
            mainViewModel.loadMelodyList()
        })

        mainViewModel.melodyList.observe(this, Observer { melodies ->
            ltLoadMelodies.visibility = View.GONE

            val adapter = rvMelodies.adapter as MelodyInfoAdapter
            adapter.melodies = melodies.sortedBy { it.melodyName }
            adapter.notifyDataSetChanged()
        })

//        btnListMelodies.setOnClickListener {
//            progressBar.visibility = View.VISIBLE
//            mainViewModel.loadMelodyList()
//        }

//        btnSetAsRingtone.setOnClickListener{
//            progressBar.visibility = View.VISIBLE
//            mainViewModel.setAsRingtone("The_Stratosphere_MP3.mp3")
//        }
    }


    private fun pickMelody() {
        val pickMelodyIntent = Intent(Intent.ACTION_GET_CONTENT)
        pickMelodyIntent.type = "audio/*"
        startActivityForResult(pickMelodyIntent, CODE_REQUEST_AUDIO_FILE)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.melody_manager_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.item_upload -> {
                pickMelody()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CODE_REQUEST_AUDIO_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("DK", "Melody pick successful")
                mainViewModel.uploadFile(data?.data)
                ltLoadMelodies.visibility
            } else {
                Toast.makeText(this, "Picking cancelled!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
