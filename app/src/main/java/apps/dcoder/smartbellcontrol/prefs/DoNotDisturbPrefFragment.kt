package apps.dcoder.smartbellcontrol.prefs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.controls.ValueSelectView
import apps.dcoder.smartbellcontrol.dialogs.DaysDialogFragment
import apps.dcoder.smartbellcontrol.dialogs.TimePickerDialogFragment
import apps.dcoder.smartbellcontrol.utils.TimeExtensions
import apps.dcoder.smartbellcontrol.viewmodels.SettingsViewModel
import kotlinx.android.synthetic.main.do_not_disturb_layout.*
import kotlinx.android.synthetic.main.do_not_disturb_layout.view.*
import kotlin.collections.ArrayList

class DoNotDisturbPrefFragment: Fragment() {

    private val timeFormatterArray: List<String> = listOf("0", "")

    private val dayInputArray: ArrayList<Int> = arrayListOf()
    private val startTimeArr: ArrayList<Int> = arrayListOf()
    private val endTimeArr: ArrayList<Int> = arrayListOf()

    private fun openTimePickerDialogFragment(fragmentManager: FragmentManager, timeArr: ArrayList<Int>, requestCode: Int) {
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
                validateTime(timeArrayList, endTimeArr)
                startTimeArr.clear()
                startTimeArr.addAll(timeArrayList)
            }

            R.id.vsEndTime -> {
                validateTime(startTimeArr, timeArrayList)
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

    private fun setDays(daysValues: ArrayList<Int>, vsDays: ValueSelectView) {
        if (daysValues.isEmpty()) {
            return
        }

        // Remove TextView error
        vsDays.setError(null)

        dayInputArray.clear()
        dayInputArray.addAll(daysValues)

        val shortNameDaysArr = arrayListOf<String>()
        val daysContent: Array<String> = context!!.resources.getStringArray(R.array.week_days_list_pref_entries_arr)
        for (dayCode in daysValues) {
            shortNameDaysArr.add(daysContent[dayCode].take(3))
        }

        vsDays.description = shortNameDaysArr.joinToString()
    }

    private fun validateTime(startTimeArr: ArrayList<Int>, endTimeArr: ArrayList<Int>): Boolean {
        // Remove TextView error
        vsStartTime.setError(null)
        vsEndTime.setError(null)

        if (startTimeArr.isEmpty() || endTimeArr.isEmpty()) {
            return true
        }

        val startTimeMillis = TimeExtensions.extractCurrentTimeUTCMillis(startTimeArr)
        val endTimeMillis = TimeExtensions.extractCurrentTimeUTCMillis(endTimeArr)
        if (!cbEndTomorrow.isChecked && endTimeMillis < startTimeMillis) {
            vsEndTime.setError(getString(R.string.err_smaller_end_time))
            return false
        }

        if (startTimeMillis == endTimeMillis) {
            vsEndTime.setError(getString(R.string.err_equal_star_end_time))
            return false
        }

        return true
    }

    companion object {
        private const val TAG_TIME_PICKER_FRAGMENT = "TimePicker"
        private const val TAG_DAY_PICKER_FRAGMENT = "DayPicker"

        private const val REQUEST_CODE_PICK_START_TIME = 1
        private const val REQUEST_CODE_PICK_END_TIME = 2
        private const val REQUEST_CODE_PICK_DAYS = 3

        private const val EXTRA_CONTENT_DAYS = "ExtraContentDays"
        private const val EXTRA_CONTENT_START_TIME = "ExtraContentStartTime"
        private const val EXTRA_CONTENT_END_TIME = "ExtraContentEndTime"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.do_not_disturb_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        if (savedInstanceState != null) {

            val contentDays= savedInstanceState.getIntegerArrayList(EXTRA_CONTENT_DAYS)
                ?: throw IllegalStateException("Missing $EXTRA_CONTENT_DAYS in savedInstanceState!")
            val startTimeArr = savedInstanceState.getIntegerArrayList(EXTRA_CONTENT_START_TIME)
                ?: throw IllegalStateException("Missing $EXTRA_CONTENT_START_TIME in savedInstanceState!")
            val endTimeArr = savedInstanceState.getIntegerArrayList(EXTRA_CONTENT_END_TIME)
                ?: throw IllegalStateException("Missing $EXTRA_CONTENT_END_TIME in savedInstanceState!")

            setDays(contentDays, view.vsDays)
            setTime(startTimeArr, view.vsStartTime)
            setTime(endTimeArr, view.vsEndTime)
        }

        view.findViewById<ValueSelectView>(R.id.vsStartTime).setOnClickListener {
            openTimePickerDialogFragment(fragmentManager!!, startTimeArr, REQUEST_CODE_PICK_START_TIME)
        }

        view.findViewById<ValueSelectView>(R.id.vsEndTime).setOnClickListener {
            openTimePickerDialogFragment(fragmentManager!!, endTimeArr, REQUEST_CODE_PICK_END_TIME)
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

        view.findViewById<CheckBox>(R.id.cbEndTomorrow).setOnCheckedChangeListener { buttonView, isChecked ->
            if (validateTime(startTimeArr, endTimeArr)) {
                view.vsEndTime.setError(null)
            }
        }

        val settingsViewModel = ViewModelProviders.of(this).get(SettingsViewModel::class.java)
        settingsViewModel.getDoNotDisturbStatus()

        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            if (dayInputArray.isEmpty()) {
                view.vsDays.setError(getString(R.string.err_days_not_set))
                return@setOnClickListener
            }

            if (startTimeArr.isEmpty()) {
                view.vsStartTime.setError(getString(R.string.err_start_time_not_set))
                return@setOnClickListener
            }

            if (endTimeArr.isEmpty()) {
                view.vsEndTime.setError(getString(R.string.err_end_time_not_set))
                return@setOnClickListener
            }

            if (view.vsDays.hasError() || view.vsStartTime.hasError() || view.vsEndTime.hasError()) {
                return@setOnClickListener
            }

            val endTomorrow = view.cbEndTomorrow.isChecked
            settingsViewModel.setDoNotDisturbMode(dayInputArray, startTimeArr, endTimeArr, endTomorrow)
        }

        settingsViewModel.getDoNotDisturbSettingsLiveData().observe(this, Observer { bellStatus ->
            if (bellStatus.days != null) {
                val daysList = arrayListOf<Int>()
                for (i in 0 until bellStatus.days!!.size) {
                    daysList.add(bellStatus.days!![i])
                }

                setDays(daysList, view.vsDays)
            }

            if (bellStatus.endTimeMillis != -1L && bellStatus.startTimeMillis != -1L) {
                val startTimeArray = TimeExtensions.getTimeArrayFromUTCMillis(bellStatus.startTimeMillis)
                setTime(startTimeArray, view.vsStartTime)
                val endTimeArray = TimeExtensions.getTimeArrayFromUTCMillis(bellStatus.endTimeMillis)
                setTime(endTimeArray, view.vsEndTime)
            }

            view.cbEndTomorrow.isChecked = bellStatus.isEndTomorrow
            view.ltLoad.visibility = View.GONE
            view.tvError.text = ""
        })

        settingsViewModel.getSuccessLiveData().observe(this, Observer {
            Toast.makeText(context!!, it.getContentIfNotConsumed(), Toast.LENGTH_LONG).show()
        })

        settingsViewModel.getErrorLiveData().observe(this, Observer {
            val message = it.getContentIfNotConsumed()
            if (message != null) {
                view.tvError.text = message
            }

            view.pbLoading.visibility = View.GONE
        })
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

                val selectedDays = data.getIntegerArrayListExtra(DaysDialogFragment.EXTRA_SELECTED_DAYS)
                setDays(selectedDays, vsDays)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putIntegerArrayList(EXTRA_CONTENT_DAYS, dayInputArray)
        outState.putIntegerArrayList(EXTRA_CONTENT_START_TIME, startTimeArr)
        outState.putIntegerArrayList(EXTRA_CONTENT_END_TIME, endTimeArr)
    }
}