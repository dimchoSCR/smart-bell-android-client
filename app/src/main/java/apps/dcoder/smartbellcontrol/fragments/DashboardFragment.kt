package apps.dcoder.smartbellcontrol.fragments

import android.os.Build
import android.os.Bundle
import android.transition.AutoTransition
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.restapiclient.model.RingEntry
import apps.dcoder.smartbellcontrol.viewmodels.BellDashboardViewModel
import kotlinx.android.synthetic.main.dashboard_fragment.view.*
import kotlinx.android.synthetic.main.fragment_log_details.view.*
import kotlinx.android.synthetic.main.ring_log_list_item.view.*

class DashboardFragment: Fragment() {

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.dashboard_fragment, container, false)

        val logDetailsFragment = LogDetailsFragment()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.cvLog.transitionName = getString(R.string.transition_expand)
            logDetailsFragment.sharedElementReturnTransition = AutoTransition()
            logDetailsFragment.sharedElementEnterTransition = AutoTransition()
        }

        view.pbLoading.visibility = View.VISIBLE

        val dashboardViewModel = ViewModelProviders.of(activity!!).get(BellDashboardViewModel::class.java)
//        dashboardViewModel.loadRingLog()

        dashboardViewModel.errorLiveData.observe(this, Observer {
            view.pbLoading.visibility = View.GONE
            view.tvError.text = it
        })

        dashboardViewModel.logEntriesLiveData.observe(this, Observer {
            view.tvInfo.visibility = View.VISIBLE
            view.ltLoad.visibility = View.GONE

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
}