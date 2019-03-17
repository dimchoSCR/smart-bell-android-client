package apps.dcoder.smartbellcontrol.prefs

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import apps.dcoder.smartbellcontrol.R

class AppPreferenceFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}