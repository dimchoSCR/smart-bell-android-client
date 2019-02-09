package apps.dcoder.smartbellcontrol

import android.content.Context
import android.view.View
import apps.dcoder.smartbellcontrol.restapiclient.model.MelodyInfo
import apps.dcoder.smartbellcontrol.restapiclient.model.RawMelodyInfo
import apps.dcoder.smartbellcontrol.restapiclient.model.utils.AudioUtils
import apps.dcoder.smartbellcontrol.restapiclient.model.utils.FileSizeUtil

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