package apps.dcoder.smartbellcontrol.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtil {
    fun formatTimeStringUsingCurrentLocale(timeString: String, inputFormat: String, outputFormat: String): String {

        return try {
            val parsableTimeString = timeString.replace(Regex("Z$"), "+00:00")
            val calendar = Calendar.getInstance()
            calendar.time = SimpleDateFormat(inputFormat, Locale.getDefault()).parse(parsableTimeString)

            SimpleDateFormat(outputFormat, Locale.getDefault()).format(calendar.time)

        } catch (e: Exception) {
            Log.e("DisturbDK", "Could not parse and localize time string!", e)
            timeString
        }

    }
}