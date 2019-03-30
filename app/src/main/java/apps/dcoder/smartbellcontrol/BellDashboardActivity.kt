package apps.dcoder.smartbellcontrol

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.lifecycle.ViewModelProviders
import apps.dcoder.smartbellcontrol.fragments.DashboardFragment
import apps.dcoder.smartbellcontrol.viewmodels.BellDashboardViewModel


class BellDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.bell_dashboard)

        val dashboardViewModel = ViewModelProviders.of(this).get(BellDashboardViewModel::class.java)
        dashboardViewModel.loadRingLog()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, DashboardFragment())
                .commit()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.app_tool_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.item_settings -> {
                val openSettingsIntent = Intent(this, SettingsActivity::class.java)
                startActivity(openSettingsIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
