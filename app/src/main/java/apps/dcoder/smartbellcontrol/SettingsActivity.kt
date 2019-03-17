package apps.dcoder.smartbellcontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity(), PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragmentArgs = pref.extras
        val prefFragment = supportFragmentManager.fragmentFactory.instantiate(
            classLoader,
            pref.fragment,
            fragmentArgs
        )

        prefFragment.arguments = fragmentArgs
        prefFragment.setTargetFragment(caller, 0)

        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_settings, prefFragment)
            .addToBackStack(null)
            .commit()

        return true
    }

}
