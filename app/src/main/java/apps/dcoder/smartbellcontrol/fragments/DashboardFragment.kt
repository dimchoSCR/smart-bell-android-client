package apps.dcoder.smartbellcontrol.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.transition.AutoTransition
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.restapiclient.model.BellStatus
import apps.dcoder.smartbellcontrol.restapiclient.model.PlaybackMode
import apps.dcoder.smartbellcontrol.restapiclient.model.RingEntry
import apps.dcoder.smartbellcontrol.utils.TimeExtensions
import apps.dcoder.smartbellcontrol.viewmodels.BellDashboardViewModel
import kotlinx.android.synthetic.main.dashboard_fragment.view.*
import kotlinx.android.synthetic.main.layout_load.view.*
import kotlinx.android.synthetic.main.ring_log_list_item.view.*

class DashboardFragment: Fragment() {

    private lateinit var dashboardViewModel: BellDashboardViewModel

    private val handler = Handler()
    private val uiProgressRunnable = Runnable {
        view!!.ltLoadF.visibility = View.VISIBLE
        view!!.ltLoadS.visibility = View.VISIBLE
    }

    private fun displayRecentRings(recents: List<RingEntry>, view: View) {
       if (recents.size == 2) {
            view.firstRingEntry.visibility = View.VISIBLE
            view.secondRingEntry.visibility = View.VISIBLE

            view.firstRingEntry.tvDateTime.text = recents[0].formattedDateTime
            view.firstRingEntry.tvRingtoneName.text = recents[0].melodyName

            view.secondRingEntry.tvDateTime.text = recents[1].formattedDateTime
            view.secondRingEntry.tvRingtoneName.text = recents[1].melodyName

            view.tvInfo.visibility = View.INVISIBLE

       } else if(!recents.isEmpty()){
           view.firstRingEntry.visibility = View.VISIBLE

           view.firstRingEntry.tvDateTime.text = recents[0].formattedDateTime
           view.firstRingEntry.tvRingtoneName.text = recents[0].melodyName

           view.tvInfo.visibility = View.INVISIBLE
       }
    }

    private fun updateBellStatusLayout(view: View) {
        view.cvBellStatus.tvCurrentRingtone.text = BellStatus.coreStatus.currentRingtone
        view.cvBellStatus.tvRingVolume.text = getFormattedRingVolume(BellStatus.coreStatus.ringVolume)

        setBellPlayBackMode(BellStatus.coreStatus, view.tvPlaybackMode)
        formatAndSetDoNotDisturbData(view.cvBellStatus.tvDoNotDisturbStatus, BellStatus.doNotDisturbStatus)
    }

    private fun setBellPlayBackMode(coreStatus: BellStatus.CoreStatus, tvPlaybackMode: TextView) {
        val playbackMode = PlaybackMode.valueOf(coreStatus.playbackMode)
        when (playbackMode) {
            PlaybackMode.MODE_STOP_ON_RELEASE -> tvPlaybackMode.text = getString(R.string.playback_mode_stop_on_release)

            PlaybackMode.MODE_STOP_AFTER_DELAY -> {
                val playbackTime = coreStatus.playbackTime
                tvPlaybackMode.text = getString(
                    R.string.playback_mode_delay_template, playbackTime)
            }
        }
    }

    private fun getFormattedRingVolume(ringVolume: Int): String {
        return getString(R.string.volume_template, ringVolume)
    }

    private fun formatAndSetDoNotDisturbData(tvDoNotDisturbStatus: TextView, doNotDisturbStatus: BellStatus.DoNotDisturbStatus) {
        val doNotDisturbOn = doNotDisturbStatus.isInDoNotDisturb
        val startTimeArr = TimeExtensions.getTimeArrayFromUTCMillis(doNotDisturbStatus.startTimeMillis)
        val endTimeArr = TimeExtensions.getTimeArrayFromUTCMillis(doNotDisturbStatus.endTimeMillis)

        val doNotDisturbStringId = if (doNotDisturbOn) {
            R.string.do_not_disturb_status_template_on
        } else {
            R.string.do_not_disturb_status_template_off
        }

        tvDoNotDisturbStatus.text = getString(
            doNotDisturbStringId,
            startTimeArr[0],
            startTimeArr[1],
            endTimeArr[0],
            endTimeArr[1]
        )
    }

    private fun refreshContents() {
        handler.postDelayed(uiProgressRunnable, 300L)

        dashboardViewModel.loadBellStatus()
        dashboardViewModel.loadRingLog()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)

        val view = layoutInflater.inflate(R.layout.dashboard_fragment, container, false)

        val logDetailsFragment = LogDetailsFragment()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.cvLog.transitionName = getString(R.string.transition_expand)
            logDetailsFragment.sharedElementReturnTransition = AutoTransition()
            logDetailsFragment.sharedElementEnterTransition = AutoTransition()
        }

        view.ltLoadF.pbLoading.visibility = View.VISIBLE
        view.ltLoadS.pbLoading.visibility = View.VISIBLE

        dashboardViewModel = ViewModelProviders.of(activity!!).get(BellDashboardViewModel::class.java)

        dashboardViewModel.errorLiveData.observe(this, Observer {
            view.ltLoadS.pbLoading.visibility = View.GONE
            val message = it.getContentIfNotConsumed()
            if (message != null) {
                view.ltLoadS.tvError.text = message
            }
        })

        dashboardViewModel.errorLiveDataBellStatus.observe(this, Observer {
            view.ltLoadF.pbLoading.visibility = View.GONE
            val message = it.getContentIfNotConsumed()
            if (message != null) {
                view.ltLoadF.tvError.text = message
            }
        })

        dashboardViewModel.bellStatusLiveData.observe(this, Observer { bellStatus ->
            handler.removeCallbacks(uiProgressRunnable)
            view.ltLoadF.visibility = View.GONE

            view.cvBellStatus.tvCurrentRingtone.text = bellStatus.coreStatus.currentRingtone
            view.cvBellStatus.tvRingVolume.text = getFormattedRingVolume(bellStatus.coreStatus.ringVolume)

            setBellPlayBackMode(bellStatus.coreStatus, view.tvPlaybackMode)
            formatAndSetDoNotDisturbData(view.cvBellStatus.tvDoNotDisturbStatus, bellStatus.doNotDisturbStatus)
        })

        dashboardViewModel.logEntriesLiveData.observe(this, Observer {
            handler.removeCallbacks(uiProgressRunnable)
            view.tvInfo.visibility = View.VISIBLE
            view.ltLoadS.visibility = View.GONE

            displayRecentRings(it.take(2), view)

            if (it.size > 2) {
                view.cvLog.setOnClickListener {
                    fragmentManager!!.beginTransaction()
                        .replace(android.R.id.content, logDetailsFragment)
                        .addSharedElement(view.cvLog, getString(R.string.transition_expand))
                        .addToBackStack(null)
                        .commit()
                }

                view.tvInfo.visibility = View.VISIBLE
                view.tvInfo.text = getString(R.string.tap_to_see_more)
            }
        })

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.dash_board_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_refresh -> {
                refreshContents()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onResume() {
        super.onResume()

        updateBellStatusLayout(view!!)
    }
}