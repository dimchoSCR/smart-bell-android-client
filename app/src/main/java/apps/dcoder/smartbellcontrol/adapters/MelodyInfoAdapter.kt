package apps.dcoder.smartbellcontrol.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.restapiclient.model.MelodyInfo
import kotlinx.android.synthetic.main.list_item_melody_info.view.*

class MelodyInfoAdapter(
    var melodies: MutableList<MelodyInfo>,
    private val itemClickListener: OnRecyclerItemClickListener
): RecyclerView.Adapter<MelodyInfoAdapter.MelodyInfoViewHolder>() {

    interface OnRecyclerItemClickListener {
        fun onCardClick(position: Int)
        fun onCardLongPress(view: View, position: Int)
        fun onPlayClicked(position: Int)
        fun onStopClicked(position: Int)
    }

    inner class MelodyInfoViewHolder(viewHolderRootView: View): RecyclerView.ViewHolder(viewHolderRootView) {
        val melodyName: TextView =  viewHolderRootView.tvMelodyName
        val melodyFileSize: TextView = viewHolderRootView.tvFileSize
        val melodyDuration: TextView = viewHolderRootView.tvDuration
        val isRingtoneDrawable: ImageView = viewHolderRootView.ivIsRingtone

        init {
            viewHolderRootView.setOnClickListener {
                itemClickListener.onCardClick(adapterPosition)
            }

            viewHolderRootView.setOnLongClickListener {
                itemClickListener.onCardLongPress(itemView, adapterPosition)
                true
            }

            viewHolderRootView.ivPlay.setOnClickListener {
                itemClickListener.onPlayClicked(adapterPosition)
            }

            viewHolderRootView.ivStop.setOnClickListener {
                itemClickListener.onStopClicked(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MelodyInfoViewHolder {
        val rootView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_melody_info, parent, false)

        return MelodyInfoViewHolder(rootView)
    }

    override fun getItemCount(): Int {
        return melodies.size
    }

    override fun onBindViewHolder(holder: MelodyInfoViewHolder, position: Int) {
        val stringResources = holder.itemView.resources

        melodies[position].apply {
            holder.melodyName.text = melodyName
            holder.melodyFileSize.text = stringResources.getString(R.string.file_size, formattedFileSize)
            holder.melodyDuration.text = stringResources.getString(R.string.melody_duration, formattedDuration)
            holder.isRingtoneDrawable.visibility = isRingtoneDrawableVisibility
        }
    }
}