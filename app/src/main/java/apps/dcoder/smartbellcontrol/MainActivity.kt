package apps.dcoder.smartbellcontrol

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import apps.dcoder.smartbellcontrol.adapters.MelodyInfoAdapter
import apps.dcoder.smartbellcontrol.restapiclient.model.BellStatus
import apps.dcoder.smartbellcontrol.viewmodels.MainViewModel
import apps.dcoder.smartbellcontrol.viewmodels.SettingsViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_load.view.*
import kotlinx.android.synthetic.main.list_item_melody_info.view.*

private const val CODE_REQUEST_AUDIO_FILE: Int = 1

class MainActivity : AppCompatActivity(), MelodyInfoAdapter.OnRecyclerItemClickListener {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    private var trackingTouch = false

    private val handler = Handler()

    private inner class PrePlayStopRunnable(val position: Int) : Runnable {
        override fun run() {
            mainViewModel.stopPrePlay(position)
        }
    }

    private lateinit var prePlayStopRunnable: Runnable

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

    private fun resetPrePlayUI(itemView: View) {
        itemView.ivStop.visibility = View.INVISIBLE
        itemView.ivPlay.visibility = View.VISIBLE
    }

    companion object {
        private const val DELAY_BEFORE_PRE_PLAY_STOP = 30000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)

        val linearLayoutManager = LinearLayoutManager(this)
        with (rvMelodies) {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = MelodyInfoAdapter(mutableListOf(), this@MainActivity)
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
        if (savedInstanceState == null) {
            mainViewModel.loadMelodyList()
        }

        mainViewModel.errorLiveData.observe(this, Observer { event ->
            if (!event.consumed) {
                val toast = Toast.makeText(this, getString(R.string.general_erro), Toast.LENGTH_LONG)
                toast.view.background.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                toast.show()

                ltLoadMelodies.visibility = View.VISIBLE
                ltLoadMelodies.pbLoading.visibility = View.GONE
                ltLoadMelodies.tvError.text = getString(R.string.error_connecting_to_bell)
                event.consume()
            }
        })

        mainViewModel.successLiveData.observe(this, Observer {
            if (!it.consumed) {
                ltLoadMelodies.visibility = View.GONE
                Toast.makeText(this, getString(R.string.operation_success), Toast.LENGTH_LONG).show()
                mainViewModel.loadMelodyList()
                it.consume()
            }
        })

        mainViewModel.melodyList.observe(this, Observer { melodies ->
            if (melodies.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
            } else {
                tvEmpty.visibility = View.GONE
            }

            ltLoadMelodies.visibility = View.GONE

            val adapter = rvMelodies.adapter as MelodyInfoAdapter
            adapter.melodies = melodies
            adapter.notifyDataSetChanged()
        })

        mainViewModel.currentRingtoneLiveData.observe(this, Observer{ event ->
            val ringtoneIndex = event.getContentIfNotConsumed()
            if (ringtoneIndex != null) {
                val melodiesAdapter = (rvMelodies.adapter as MelodyInfoAdapter)
                Toast.makeText(this, "Ringtone set successfully!", Toast.LENGTH_LONG).show()
                var indexOfOldRingtone: Int = -1

                // Find old ringtone index
                for (i in 0 until melodiesAdapter.melodies.size) {
                    if (melodiesAdapter.melodies[i].isRingtoneDrawableVisibility == View.VISIBLE) {
                        indexOfOldRingtone = i
                        break
                    }
                }

                if (indexOfOldRingtone == -1) {
                    Log.d("RingtoneDK", "Index of previous ringtone could not be found!")
                    melodiesAdapter.melodies[ringtoneIndex].isRingtoneDrawableVisibility = View.VISIBLE
                    melodiesAdapter.notifyItemChanged(ringtoneIndex)

                    return@Observer
                }

                melodiesAdapter.melodies[ringtoneIndex].isRingtoneDrawableVisibility = View.VISIBLE
                melodiesAdapter.notifyItemChanged(ringtoneIndex)

                melodiesAdapter.melodies[indexOfOldRingtone].isRingtoneDrawableVisibility = View.GONE
                melodiesAdapter.notifyItemChanged(indexOfOldRingtone)
            }
        })

        mainViewModel.prePlaySuccessLiveData.observe(this, Observer { event ->
            val prePlayPair = event.getContentIfNotConsumed()
            if (prePlayPair != null) {
                val layoutManager = rvMelodies.layoutManager!!
                if (layoutManager.childCount > 0) {
                    val itemView = layoutManager.findViewByPosition(prePlayPair.first)!!

                    if (prePlayPair.second) {
                        itemView.ivPlay.visibility = View.INVISIBLE
                        itemView.ivStop.visibility = View.VISIBLE

                    } else {
                        if (this::prePlayStopRunnable.isInitialized) {
                            handler.removeCallbacks(prePlayStopRunnable)
                        }

                        resetPrePlayUI(itemView)
                    }
                }
            }
        })

        mainViewModel.melodyDeletedLiveData.observe(this , Observer { event ->
            val melodyPosition = event.getContentIfNotConsumed()
            if (melodyPosition != null) {
                val adapter = rvMelodies.adapter as MelodyInfoAdapter
                adapter.melodies.removeAt(melodyPosition)
                adapter.notifyItemRemoved(melodyPosition)

                Toast.makeText(this, getString(R.string.melody_delete_success), Toast.LENGTH_LONG).show()
            }

            if (mainViewModel.melodyList.value.isNullOrEmpty()) {
                tvEmpty.visibility = View.VISIBLE
            }
        })
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
        when (requestCode) {
            CODE_REQUEST_AUDIO_FILE -> {
                if (resultCode == Activity.RESULT_OK) {
                    Log.d("DK", "Melody pick successful")
                    mainViewModel.uploadFile(data?.data)
                    ltLoadMelodies.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this, getString(R.string.pick_cancelled), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (this::prePlayStopRunnable.isInitialized) {
            handler.removeCallbacks(prePlayStopRunnable)
        }

        mainViewModel.stopPrePlay(0)
    }

    override fun onCardClick(position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.set_melody_as_ringtone))
            .setPositiveButton(android.R.string.yes) { dialog, id ->
                // Stop prePlay if running
                val prePlayPair = mainViewModel.prePlaySuccessLiveData.value
                if (prePlayPair != null && prePlayPair.peekContent()!!.second) {
                    val melodies = (rvMelodies.adapter as MelodyInfoAdapter).melodies
                    val layoutManager = rvMelodies.layoutManager!!
                    var playingSongIndex = 0
                    if (layoutManager.childCount == melodies.size) {
                        for (i in 0 until melodies.size) {
                            val currentItemView = layoutManager.findViewByPosition(i)
                            if (currentItemView!!.ivPlay.visibility == View.INVISIBLE) {
                                playingSongIndex = i
                                break
                            }
                        }
                    }

                    mainViewModel.stopPrePlay(playingSongIndex)
                }

                mainViewModel.setAsRingtone(position)
            }
            .setNegativeButton(android.R.string.no) { dialog, id ->
                Toast.makeText(this, getString(R.string.set_as_ringtone), Toast.LENGTH_LONG).show()
            }

        builder.create().show()
    }

    override fun onCardLongPress(view: View, position: Int) {
        val popUpMenu = PopupMenu(this, view)
        val inflater: MenuInflater = popUpMenu.menuInflater
        inflater.inflate(R.menu.melody_card_long_press_menu, popUpMenu.menu)

        popUpMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.item_delete -> {
                    mainViewModel.deleteMelody(position)
                    true
                }

                else -> {
                    Log.e("MenuDK", "No such menu item")
                    true
                }
            }
        }

        popUpMenu.show()
    }

    override fun onPlayClicked(position: Int) {
        val prePlayPair = mainViewModel.prePlaySuccessLiveData.value!!.peekContent()
        if (prePlayPair != null && !prePlayPair.second) {
            mainViewModel.prePlayMelody(position)
            prePlayStopRunnable = PrePlayStopRunnable(position)
            handler.postDelayed(prePlayStopRunnable, DELAY_BEFORE_PRE_PLAY_STOP)
        } else {
            Toast.makeText(this, getString(R.string.preplay_running), Toast.LENGTH_LONG).show()
        }
    }

    override fun onStopClicked(position: Int) {
        mainViewModel.stopPrePlay(position)
    }
}
