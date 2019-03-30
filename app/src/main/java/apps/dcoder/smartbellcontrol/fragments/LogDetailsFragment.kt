package apps.dcoder.smartbellcontrol.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.adapters.RingEntryAdapter
import apps.dcoder.smartbellcontrol.viewmodels.BellDashboardViewModel
import kotlinx.android.synthetic.main.fragment_log_details.*

import kotlinx.android.synthetic.main.fragment_log_details.view.*

class LogDetailsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_log_details, container, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.rvLog.transitionName = getString(R.string.transition_expand)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val linearLayoutManager = LinearLayoutManager(context)
        val ringEntryAdapter = RingEntryAdapter(listOf())
        with (view.rvLog) {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = ringEntryAdapter
        }

        val dashboardViewModel = ViewModelProviders.of(activity!!).get(BellDashboardViewModel::class.java)
        dashboardViewModel.logEntriesLiveData.observe(this, Observer { ringEntries ->
            ringEntryAdapter.ringEntries = ringEntries
            ringEntryAdapter.notifyDataSetChanged()
            view.ltSwipeRefresh.isRefreshing = false
        })

        dashboardViewModel.errorLiveData.observe(this, Observer {
            ltSwipeRefresh.isRefreshing = false
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        ltSwipeRefresh.setColorSchemeResources(R.color.colorAccent)
        ltSwipeRefresh.setOnRefreshListener {
            dashboardViewModel.loadRingLog()
        }
    }
}
