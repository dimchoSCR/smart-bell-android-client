package apps.dcoder.smartbellcontrol.dialogs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import apps.dcoder.smartbellcontrol.R

class DaysDialogFragment internal constructor(): DialogFragment() {

    private lateinit var dayValues: IntArray

    private fun initializeLayout(container: ViewGroup, args: Bundle) {
        val daysArray = args.getStringArray(KEY_CONTENT_ARRAY)
            ?: throw IllegalStateException("Proper fragment arguments not provided!")
        dayValues = args.getIntArray(KEY_VALUES_ARRAY)
            ?: throw IllegalStateException("Proper fragment arguments not provided!")
        val selected = args.getIntegerArrayList(KEY_SELECTED_ARRAY)
            ?: throw IllegalStateException("Proper fragment arguments not provided!")

        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(64, 8, 64, 8)

        for (day in daysArray) {
            val currentCheckbox = CheckBox(context)
            currentCheckbox.text = day
            currentCheckbox.layoutParams = layoutParams

            container.addView(currentCheckbox)
        }

        for (i in 0 until selected.size) {
            val selectedBox = container.getChildAt(selected[i]) as CheckBox
            selectedBox.isChecked = true
        }

    }

    companion object {
        private const val KEY_CONTENT_ARRAY = "ContentArray"
        private const val KEY_VALUES_ARRAY = "ValuesArray"
        private const val KEY_SELECTED_ARRAY = "SelectedArray"

        const val EXTRA_SELECTED_DAYS = "ExtraSelectedDays"
        const val RESULT_CODE_DAYS_SET = 1

        fun create(content: Array<String>, values: IntArray, selected: ArrayList<Int>): DaysDialogFragment {
            val args = Bundle()
            args.putStringArray(KEY_CONTENT_ARRAY, content)
            args.putIntArray(KEY_VALUES_ARRAY, values)
            args.putIntegerArrayList(KEY_SELECTED_ARRAY, selected)

            val daysDialog = DaysDialogFragment()
            daysDialog.arguments = args

            return daysDialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        val customDialogView = activity!!.layoutInflater.inflate(R.layout.days_dialog_layout, null, false)

        if (arguments == null) {
            throw IllegalArgumentException("Fragment arguments can not be null!")
        }

        initializeLayout(customDialogView as ViewGroup, arguments!!)
        builder.setTitle(context!!.getString(R.string.week_days_list_pref_title))
            .setView(customDialogView)
            .setPositiveButton(android.R.string.ok) { dialog, id ->
                val resultSet = arrayListOf<Int>()
                for (i in 0 until customDialogView.childCount) {
                    if ((customDialogView.getChildAt(i) as CheckBox).isChecked) {
                        resultSet.add(dayValues[i])
                    }
                }

                val data = Intent()
                data.putExtra(EXTRA_SELECTED_DAYS, resultSet)

                targetFragment!!.onActivityResult(
                    targetRequestCode,
                    RESULT_CODE_DAYS_SET,
                    data
                )
            }

        return builder.create()
    }
}