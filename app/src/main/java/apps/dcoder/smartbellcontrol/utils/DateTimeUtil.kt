package apps.dcoder.smartbellcontrol.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtil {
    fun formatTimeStringUsingCurrentLocale(timeString: String, inputFormat: String, outputDFormat: String): String {

        return try {
            val defaultCal = Calendar.getInstance()
            val deviceTimeZone = defaultCal.timeZone
            val offsetFromUTC = deviceTimeZone.getOffset(defaultCal.time.time)

            val calendar = Calendar.getInstance()
            calendar.time = SimpleDateFormat(inputFormat, Locale.getDefault()).parse(timeString)
            calendar.add(Calendar.MILLISECOND, offsetFromUTC)

            SimpleDateFormat(outputDFormat, Locale.getDefault()).format(calendar.time)

        } catch (e: Exception) {
            Log.e("DisturbDK", "Could not parse and localize time string!", e)
            timeString
        }

    }
}