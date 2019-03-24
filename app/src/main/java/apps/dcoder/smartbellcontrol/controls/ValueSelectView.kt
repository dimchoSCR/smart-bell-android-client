package apps.dcoder.smartbellcontrol.controls

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import apps.dcoder.smartbellcontrol.R

class ValueSelectView(context: Context, attrs: AttributeSet): LinearLayout(context, attrs) {

    private val tvTitle: TextView
    private val tvDescription: TextView

    init {
        orientation = LinearLayout.VERTICAL
        val layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        layoutInflater.inflate(R.layout.value_select_view_layout, this, true)

        tvTitle = getChildAt(0) as TextView
        tvDescription = getChildAt(1) as TextView
    }

    init {
        val xmlAttributes: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.ValueSelectView, 0, 0)
        tvDescription.layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        tvTitle.text = xmlAttributes.getText(R.styleable.ValueSelectView_title)
        tvDescription.text = xmlAttributes.getText(R.styleable.ValueSelectView_description)
        xmlAttributes.recycle()
    }

    var description: CharSequence = tvDescription.text
        set(value){
            field = value
            tvDescription.text = value
        }

    fun setError(text: CharSequence?) {
        tvDescription.isFocusableInTouchMode = true
        tvDescription.requestFocus()
        tvDescription.error = text
    }

    fun hasError(): Boolean {
        return tvDescription.error != null && tvDescription.error.isNotEmpty()
    }
}