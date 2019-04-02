package apps.dcoder.smartbellcontrol.prefs

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.preference.*
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.restapiclient.model.BellStatus
import apps.dcoder.smartbellcontrol.restapiclient.model.PlaybackMode
import apps.dcoder.smartbellcontrol.viewmodels.SettingsViewModel

class AppPreferenceFragment: PreferenceFragmentCompat() {
    private lateinit var viewModel: SettingsViewModel

    private lateinit var playbackModePref: ListPreference
    private lateinit var playbackDuration: EditTextPreference
    private lateinit var ringVolume: SeekBarPreference

    private companion object {
        private const val KEY_PREFERENCE_DO_NOT_DISTURB = "key_do_not_disturb_prefs"
        private const val KEY_PREFERENCE_PLAYBACK_MODE = "key_playback_mode_pref"
        private const val KEY_PREFERENCE_PLAYBACK_DURATION = "key_playback_duration"
        private const val KEY_PREFERENCE_RING_VOLUME = "key_volume"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    private fun setUpPlaybackPreference(defaultPrefIndex: Int) {

        when (BellStatus.coreStatus.playbackMode) {
            PlaybackMode.MODE_STOP_AFTER_DELAY.name -> playbackDuration.isEnabled = true
            else -> {}
        }

        playbackModePref.setValueIndex(defaultPrefIndex)

        playbackModePref.setOnPreferenceChangeListener { _, newValue ->
            viewModel.setPlaybackMode(newValue as String)
            playbackDuration.isEnabled = !playbackDuration.isEnabled
            true
        }
    }

    private fun setUpPlaybackDurationPreference() {
        playbackDuration.text = Integer.toString(BellStatus.coreStatus.playbackTime)
        playbackDuration.setOnBindEditTextListener { editText -> editText.inputType = InputType.TYPE_CLASS_NUMBER }

        playbackDuration.setOnPreferenceChangeListener {_, newValue ->
            val intValue = Integer.parseInt(newValue as String)
            if (intValue < 10 || intValue > 60) {
                val toast = Toast.makeText(context, getString(R.string.err_playback_duration), Toast.LENGTH_LONG)
                toast.view.background.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                toast.show()

                return@setOnPreferenceChangeListener false
            }

            viewModel.setPlaybackDuration(intValue)
            true
        }
    }

    private fun setUpRingVolumePreference() {
        ringVolume.value = BellStatus.coreStatus.ringVolume
        ringVolume.setOnPreferenceChangeListener { _, newValue ->
            viewModel.setRingVolume(newValue as Int)

            true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        playbackModePref = findPreference(KEY_PREFERENCE_PLAYBACK_MODE)!!
        playbackDuration = findPreference(KEY_PREFERENCE_PLAYBACK_DURATION)!!
        ringVolume = findPreference(KEY_PREFERENCE_RING_VOLUME)!!

        findPreference<Preference>(KEY_PREFERENCE_DO_NOT_DISTURB)?.setOnPreferenceClickListener {
            fragmentManager!!.beginTransaction()
                .replace(R.id.settings_container, DoNotDisturbPrefFragment())
                .addToBackStack(null)
                .commit()

            true
        }

        val defaultPrefIndex = playbackModePref.entryValues.indexOf(BellStatus.coreStatus.playbackMode)

        viewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        viewModel.successNotifyLiveData.observe(this, Observer {
            if (!it.consumed) {
                Toast.makeText(context, getString(R.string.setting_change_success), Toast.LENGTH_LONG).show()
                it.consume()
            }
        })

        viewModel.errorNotifyLiveData.observe(this, Observer {
            if (!it.consumed) {
                val toast = Toast.makeText(context, getString(R.string.setting_change_failure), Toast.LENGTH_LONG)
                toast.view.background.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN)
                toast.show()

                playbackModePref.setValueIndex(defaultPrefIndex)
                it.consume()
            }
        })

        setUpPlaybackPreference(defaultPrefIndex)
        setUpPlaybackDurationPreference()
        setUpRingVolumePreference()
    }
}