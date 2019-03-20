package apps.dcoder.smartbellcontrol.dialogs

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import apps.dcoder.smartbellcontrol.R

class DaysDialogFragment internal constructor(): DialogFragment() {

    private fun initializeLayout(container: ViewGroup, args: Bundle) {
        val daysArray = args.getStringArray(KEY_CONTENT_ARRAY) ?: throw IllegalStateException("Proper arguments not provided!")

        val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        layoutParams.setMargins(64, 8, 64, 8)

        for (day in daysArray) {
            val currentCheckbox = CheckBox(context)
            currentCheckbox.text = day
            currentCheckbox.layoutParams = layoutParams

            container.addView(currentCheckbox)
        }
    }

    companion object {
        const val KEY_CONTENT_ARRAY = "ContentArray"

        fun create(content: Array<String>): DaysDialogFragment {
            val args = Bundle()
            args.putStringArray(KEY_CONTENT_ARRAY, content)

            val daysDialog = DaysDialogFragment()
            daysDialog.arguments = args

            return daysDialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)
        val customDialogView = activity!!.layoutInflater.inflate(R.layout.days_dialog_layout, null, false)

        initializeLayout(customDialogView as ViewGroup, arguments!!)
        builder.setTitle(context!!.getString(R.string.week_days_list_pref_title))
            .setView(customDialogView)
            .setPositiveButton(android.R.string.ok) { dialog, id ->
                for (i in 0 until customDialogView.childCount) {
                    if ((customDialogView.getChildAt(i) as CheckBox).isChecked) {
                        Log.e("DisturbDK", "Checked at pos: $i")
                    }
                }
            }

        return builder.create()
    }
}