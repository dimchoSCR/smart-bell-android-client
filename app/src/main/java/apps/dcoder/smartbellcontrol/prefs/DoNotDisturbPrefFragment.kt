package apps.dcoder.smartbellcontrol.prefs

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import apps.dcoder.smartbellcontrol.R

class DoNotDisturbPrefFragment: PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.do_not_disturb_prefs, rootKey)
    }
}