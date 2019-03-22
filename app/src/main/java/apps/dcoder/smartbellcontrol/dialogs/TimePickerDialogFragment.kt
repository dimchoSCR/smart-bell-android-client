package apps.dcoder.smartbellcontrol.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*
import kotlin.collections.ArrayList

class TimePickerDialogFragment internal constructor(): DialogFragment(), TimePickerDialog.OnTimeSetListener {

    companion object {
        private const val KEY_TIME_ARRAY = "TimeArray"

        const val RESULT_CODE_TIME_SET = 2
        const val EXTRA_TIME_ARRAY = "ExtraTimeArray"

        fun create(timeArray: ArrayList<Int>): TimePickerDialogFragment {
            val args = Bundle()
            args.putIntegerArrayList(KEY_TIME_ARRAY, timeArray)

            val timePickerFragment = TimePickerDialogFragment()
            timePickerFragment.arguments = args

            return timePickerFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val timeArray = arguments?.getIntegerArrayList(KEY_TIME_ARRAY)
            ?: throw IllegalStateException("Proper fragment arguments not provided!")

        val hour: Int
        val minute: Int
        if (timeArray.isEmpty()) {
            // Use the current time as the default values for the picker
            val c = Calendar.getInstance()
            hour = c.get(Calendar.HOUR_OF_DAY)
            minute = c.get(Calendar.MINUTE)
        } else {
            hour = timeArray[0]
            minute = timeArray[1]
        }

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val data = Intent()
        data.putExtra(EXTRA_TIME_ARRAY, arrayListOf(hourOfDay, minute))

        targetFragment!!.onActivityResult(
            targetRequestCode,
            RESULT_CODE_TIME_SET,
            data
        )
    }
}