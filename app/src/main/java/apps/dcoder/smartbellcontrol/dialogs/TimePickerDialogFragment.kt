package apps.dcoder.smartbellcontrol.dialogs

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import apps.dcoder.smartbellcontrol.prefs.DoNotDisturbPrefFragment
import java.util.*

class TimePickerDialogFragment: DialogFragment(), TimePickerDialog.OnTimeSetListener {

    companion object {
        const val RESULT_CODE_TIME_SET = 2
        const val EXTRA_HOUR_OF_DAY = "HourOfDay"
        const val EXTRA_MINUTE = "Minute"
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val data = Intent()
        data.putExtra(EXTRA_HOUR_OF_DAY, hourOfDay)
        data.putExtra(EXTRA_MINUTE, minute)

        targetFragment!!.onActivityResult(
            targetRequestCode,
            RESULT_CODE_TIME_SET,
            data
        )
    }
}