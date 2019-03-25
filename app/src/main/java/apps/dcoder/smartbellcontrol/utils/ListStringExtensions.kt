package apps.dcoder.smartbellcontrol.utils

import android.content.Context
import apps.dcoder.smartbellcontrol.R

fun List<String>.toLocalizedPointSeparatedString(context: Context): String {
    val list = this
    val builder = StringBuilder()
    for (i in 0 until list.size) {
        val content = "${i + 1}. ${context.getString(R.string.missed_ring_at, list[i])}\n"
        builder.append(content)
    }

    return builder.toString()
}