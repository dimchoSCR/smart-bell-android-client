package apps.dcoder.smartbellcontrol.utils

import java.util.*

object TimeExtensions{
    fun extractCurrentTimeUTCMillis(timeArray: ArrayList<Int>): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, timeArray[0])
        calendar.set(Calendar.MINUTE, timeArray[1])
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.timeInMillis
    }

    fun getTimeArrayFromUTCMillis(millis: Long): ArrayList<Int> {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.timeInMillis = millis

        return arrayListOf(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))

    }
}