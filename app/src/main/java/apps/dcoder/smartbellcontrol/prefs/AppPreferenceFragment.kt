package apps.dcoder.smartbellcontrol.prefs

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import apps.dcoder.smartbellcontrol.R

class AppPreferenceFragment: PreferenceFragmentCompat() {
    companion object {
        private const val KEY_PREFERENCE_DO_NOT_DISTURB = "key_do_not_disturb_prefs"
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findPreference<Preference>(KEY_PREFERENCE_DO_NOT_DISTURB)?.setOnPreferenceClickListener {
            fragmentManager!!.beginTransaction()
                .replace(R.id.settings_container, DoNotDisturbPrefFragment())
                .addToBackStack(null)
                .commit()

            true
        }
    }
}