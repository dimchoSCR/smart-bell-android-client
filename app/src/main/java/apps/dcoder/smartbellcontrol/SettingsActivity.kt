package apps.dcoder.smartbellcontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import apps.dcoder.smartbellcontrol.prefs.AppPreferenceFragment

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.settings_container, AppPreferenceFragment())
                .commit()
        }
    }

}
