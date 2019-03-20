package apps.dcoder.smartbellcontrol.prefs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.controls.ValueSelectView
import apps.dcoder.smartbellcontrol.dialogs.DaysDialogFragment
import apps.dcoder.smartbellcontrol.dialogs.TimePickerDialogFragment
import kotlinx.android.synthetic.main.do_not_disturb_layout.*

class DoNotDisturbPrefFragment: Fragment() {
    companion object {
        private const val TAG_TIME_PICKER_FRAGMENT = "TimePicker"
        private const val TAG_DAY_PICKER_FRAGMENT = "DayPicker"

        const val REQUEST_CODE_PICK_START_TIME = 1
        const val REQUEST_CODE_PICK_END_TIME = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.do_not_disturb_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ValueSelectView>(R.id.vsStartTime).setOnClickListener {
            val timePickerDialog = TimePickerDialogFragment()
            timePickerDialog.setTargetFragment(this, REQUEST_CODE_PICK_START_TIME)

            timePickerDialog.show(fragmentManager!!, TAG_TIME_PICKER_FRAGMENT)
        }

        view.findViewById<ValueSelectView>(R.id.vsEndTime).setOnClickListener {
            val timePickerDialog = TimePickerDialogFragment()
            timePickerDialog.setTargetFragment(this, REQUEST_CODE_PICK_END_TIME)

            timePickerDialog.show(fragmentManager!!, TAG_TIME_PICKER_FRAGMENT)
        }

        view.findViewById<ValueSelectView>(R.id.vsDays).setOnClickListener {
            val daysFragmentDialog = DaysDialogFragment.
                create(context!!.resources.getStringArray(R.array.week_days_list_pref_entries_arr))

            daysFragmentDialog.show(fragmentManager!!, TAG_DAY_PICKER_FRAGMENT)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_PICK_START_TIME) {
            // Make sure the request was successful
            if (resultCode == TimePickerDialogFragment.RESULT_CODE_TIME_SET && data != null) {
                if (!data.hasExtra(TimePickerDialogFragment.EXTRA_HOUR_OF_DAY) ||
                    !data.hasExtra(TimePickerDialogFragment.EXTRA_MINUTE)) {

                    throw IllegalStateException("Missing integer extras: hourOfDay and minute!")
                }

                val hourOfDay = data.getIntExtra(TimePickerDialogFragment.EXTRA_HOUR_OF_DAY, 0)
                val minute = data.getIntExtra(TimePickerDialogFragment.EXTRA_MINUTE, 0)

                vsStartTime.description = "$hourOfDay:$minute"
            }
        } else if (requestCode == REQUEST_CODE_PICK_END_TIME) {
            // Make sure the request was successful
            if (resultCode == TimePickerDialogFragment.RESULT_CODE_TIME_SET && data != null) {
                if (!data.hasExtra(TimePickerDialogFragment.EXTRA_HOUR_OF_DAY) ||
                    !data.hasExtra(TimePickerDialogFragment.EXTRA_MINUTE)) {

                    throw IllegalStateException("Missing integer extras: hourOfDay and minute!")
                }

                val hourOfDay = data.getIntExtra(TimePickerDialogFragment.EXTRA_HOUR_OF_DAY, 0)
                val minute = data.getIntExtra(TimePickerDialogFragment.EXTRA_MINUTE, 0)

                vsEndTime.description = "$hourOfDay:$minute"
            }
        }
    }
}