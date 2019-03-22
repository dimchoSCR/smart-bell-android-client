package apps.dcoder.smartbellcontrol.prefs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.controls.ValueSelectView
import apps.dcoder.smartbellcontrol.dialogs.DaysDialogFragment
import apps.dcoder.smartbellcontrol.dialogs.TimePickerDialogFragment
import kotlinx.android.synthetic.main.do_not_disturb_layout.*
import kotlinx.android.synthetic.main.do_not_disturb_layout.view.*

class DoNotDisturbPrefFragment: Fragment() {

    private val timeFormatterArray: List<String> = listOf("0", "")

    private val dayInputArray: ArrayList<Int> = arrayListOf()
    private val startTimeArr: ArrayList<Int> = arrayListOf()
    private val endTimeArr: ArrayList<Int> = arrayListOf()

    private fun openTimePcikerDialogFragment(fragmentManager: FragmentManager, timeArr: ArrayList<Int>, requestCode: Int) {
        val timePickerDialog = TimePickerDialogFragment.create(timeArr)
        timePickerDialog.setTargetFragment(this, requestCode)

        timePickerDialog.show(fragmentManager, TAG_TIME_PICKER_FRAGMENT)
    }

    private fun getTimeArrayFromData(data: Intent): ArrayList<Int> {
        if (!data.hasExtra(TimePickerDialogFragment.EXTRA_TIME_ARRAY)) {
            throw IllegalStateException("Missing integer extras: hourOfDay and minute!")
        }

        return data.getIntegerArrayListExtra(TimePickerDialogFragment.EXTRA_TIME_ARRAY)
    }

    private fun setTime(timeArrayList: ArrayList<Int>, vsTimePicker: ValueSelectView) {
        if (timeArrayList.isEmpty()) {
            return
        }

        when (vsTimePicker.id) {
            R.id.vsStartTime -> {
                startTimeArr.clear()
                startTimeArr.addAll(timeArrayList)
            }

            R.id.vsEndTime -> {
                endTimeArr.clear()
                endTimeArr.addAll(timeArrayList)
            }

            else -> throw IllegalStateException("ValueSelectView id unrecognized!")
        }

        val hourOfDay = timeArrayList[0]
        val minute = timeArrayList[1]

        val hourFormat = timeFormatterArray[hourOfDay.toString().length - 1]
        val minuteFormat = timeFormatterArray[minute.toString().length - 1]
        vsTimePicker.description = "$hourFormat$hourOfDay:$minuteFormat$minute"
    }

    private fun setDays(daysContent: Array<String>, daysValues: ArrayList<Int>, vsDays: ValueSelectView) {
        dayInputArray.clear()
        dayInputArray.addAll(daysValues)

        val shortNameDaysArr = arrayListOf<String>()
        for (dayCode in daysValues) {
            shortNameDaysArr.add(daysContent[dayCode].take(3))
        }

        vsDays.description = shortNameDaysArr.joinToString()
    }


    companion object {
        private const val TAG_TIME_PICKER_FRAGMENT = "TimePicker"
        private const val TAG_DAY_PICKER_FRAGMENT = "DayPicker"

        private const val REQUEST_CODE_PICK_START_TIME = 1
        private const val REQUEST_CODE_PICK_END_TIME = 2
        private const val REQUEST_CODE_PICK_DAYS = 3

        private const val EXTRA_DESCRIPTION_DAYS = "ExtraDescriptionDays"
        private const val EXTRA_DESCRIPTION_START_TIME = "ExtraDescriptionStartTime"
        private const val EXTRA_DESCRIPTION_END_TIME = "ExtraDescriptionEndTime"

        private const val EXTRA_CONTENT_DAYS = "ExtraContentDays"
        private const val EXTRA_CONTENT_START_TIME = "ExtraContentStartTime"
        private const val EXTRA_CONTENT_END_TIME = "ExtraContentEndTime"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val fragmentView = inflater.inflate(R.layout.do_not_disturb_layout, container, false)

        if (savedInstanceState != null) {
            val contentDays= savedInstanceState.getIntegerArrayList(EXTRA_CONTENT_DAYS)
                ?: throw IllegalStateException("Missing $EXTRA_CONTENT_DAYS in savedInstanceState!")
            val startTimeArr = savedInstanceState.getIntegerArrayList(EXTRA_CONTENT_START_TIME)
                ?: throw IllegalStateException("Missing $EXTRA_CONTENT_START_TIME in savedInstanceState!")
            val endTimeArr = savedInstanceState.getIntegerArrayList(EXTRA_CONTENT_END_TIME)
                ?: throw IllegalStateException("Missing $EXTRA_CONTENT_END_TIME in savedInstanceState!")

            setDays(
                context!!.resources.getStringArray(R.array.week_days_list_pref_entries_arr),
                contentDays,
                fragmentView.vsDays
            )

            setTime(startTimeArr, fragmentView.vsStartTime)
            setTime(endTimeArr, fragmentView.vsEndTime)

            fragmentView.vsDays.description = savedInstanceState.getString(EXTRA_DESCRIPTION_DAYS) as CharSequence
            fragmentView.vsStartTime.description = savedInstanceState.getString(EXTRA_DESCRIPTION_START_TIME) as CharSequence
            fragmentView.vsEndTime.description = savedInstanceState.getString(EXTRA_DESCRIPTION_END_TIME) as CharSequence
        }

        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ValueSelectView>(R.id.vsStartTime).setOnClickListener {
            openTimePcikerDialogFragment(fragmentManager!!, startTimeArr, REQUEST_CODE_PICK_START_TIME)
        }

        view.findViewById<ValueSelectView>(R.id.vsEndTime).setOnClickListener {
            openTimePcikerDialogFragment(fragmentManager!!, endTimeArr, REQUEST_CODE_PICK_END_TIME)
        }

        view.findViewById<ValueSelectView>(R.id.vsDays).setOnClickListener {
            val daysFragmentDialog = DaysDialogFragment.create(
                context!!.resources.getStringArray(R.array.week_days_list_pref_entries_arr),
                context!!.resources.getIntArray(R.array.week_days_list_pref_values_arr),
                dayInputArray
            )

            daysFragmentDialog.setTargetFragment(this, REQUEST_CODE_PICK_DAYS)
            daysFragmentDialog.show(fragmentManager!!, TAG_DAY_PICKER_FRAGMENT)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_PICK_START_TIME) {
            // Make sure the request was successful
            if (resultCode == TimePickerDialogFragment.RESULT_CODE_TIME_SET && data != null) {
                setTime(getTimeArrayFromData(data), vsStartTime)
            }
        } else if (requestCode == REQUEST_CODE_PICK_END_TIME) {
            // Make sure the request was successful
            if (resultCode == TimePickerDialogFragment.RESULT_CODE_TIME_SET && data != null) {
                setTime(getTimeArrayFromData(data), vsEndTime)
            }
        } else if (requestCode == REQUEST_CODE_PICK_DAYS && data != null) {
            if (resultCode == DaysDialogFragment.RESULT_CODE_DAYS_SET) {
                if (!data.hasExtra(DaysDialogFragment.EXTRA_SELECTED_DAYS)) {
                    throw IllegalStateException("Missing integer extras: days of week!")
                }

                val weekDaysArray = context!!.resources.getStringArray(R.array.week_days_list_pref_entries_arr)
                val selectedDays = data.getIntegerArrayListExtra(DaysDialogFragment.EXTRA_SELECTED_DAYS)

                setDays(weekDaysArray, selectedDays, vsDays)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putIntegerArrayList(EXTRA_CONTENT_DAYS, dayInputArray)
        outState.putIntegerArrayList(EXTRA_CONTENT_START_TIME, startTimeArr)
        outState.putIntegerArrayList(EXTRA_CONTENT_END_TIME, endTimeArr)

        outState.putString(EXTRA_DESCRIPTION_DAYS, vsDays.description.toString())
        outState.putString(EXTRA_DESCRIPTION_START_TIME, vsStartTime.description.toString())
        outState.putString(EXTRA_DESCRIPTION_END_TIME, vsEndTime.description.toString())
    }
}