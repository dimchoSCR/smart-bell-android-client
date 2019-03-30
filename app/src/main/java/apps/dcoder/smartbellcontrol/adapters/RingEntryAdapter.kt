package apps.dcoder.smartbellcontrol.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.restapiclient.model.RingEntry
import kotlinx.android.synthetic.main.ring_log_list_item.view.*

class RingEntryAdapter(var ringEntries: List<RingEntry>): RecyclerView.Adapter<RingEntryAdapter.RingEntryViewHolder>() {

    class RingEntryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val ringDateTime = itemView.tvDateTime!!
        val ringtoneName = itemView.tvRingtoneName!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RingEntryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.ring_log_list_item, parent, false)

        return RingEntryViewHolder(itemView)
    }

    override fun getItemCount(): Int {
       return ringEntries.size
    }

    override fun onBindViewHolder(holder: RingEntryViewHolder, position: Int) {
        ringEntries[position].apply {
            holder.ringDateTime.text = formattedDateTime
            holder.ringtoneName.text = melodyName
        }
    }
}