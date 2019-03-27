package apps.dcoder.smartbellcontrol

import android.content.Context
import android.view.View
import apps.dcoder.smartbellcontrol.restapiclient.model.MelodyInfo
import apps.dcoder.smartbellcontrol.restapiclient.model.RawMelodyInfo
import apps.dcoder.smartbellcontrol.restapiclient.model.RawRingEntry
import apps.dcoder.smartbellcontrol.restapiclient.model.RingEntry
import apps.dcoder.smartbellcontrol.restapiclient.model.utils.AudioUtils
import apps.dcoder.smartbellcontrol.restapiclient.model.utils.FileSizeUtil
import apps.dcoder.smartbellcontrol.utils.DateTimeUtil

fun formatRawMelodyInfo(context: Context, rawMelodyInfo: RawMelodyInfo): MelodyInfo {
    val formattedFileSize = FileSizeUtil.toHumanReadableSize(rawMelodyInfo.fileSize, context)
    val formattedDuration = AudioUtils.toHumanReadableDuration(rawMelodyInfo.duration, context)
    val isRingtoneDrawableVisibility: Int = if(rawMelodyInfo.isRingtone) View.VISIBLE else View.INVISIBLE

    return MelodyInfo(
        rawMelodyInfo.melodyName,
        formattedFileSize,
        formattedDuration,
        rawMelodyInfo.contentType,
        isRingtoneDrawableVisibility
    )
}

fun formatRawRingEntry(rawRingEntry: RawRingEntry): RingEntry {
    val inputFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
    val outputFormat = "HH:mm, dd MMM"

    val formattedDateTime = DateTimeUtil.formatTimeStringUsingCurrentLocale(rawRingEntry.dateTime, inputFormat, outputFormat)
    return RingEntry(rawRingEntry.id, rawRingEntry.melodyName, formattedDateTime)
}