package apps.dcoder.smartbellcontrol.restapiclient.model

data class MelodyInfo(
    val melodyName: String,
    val formattedFileSize: String,
    val formattedDuration: String,
    val contentType: String,
    var isRingtoneDrawableVisibility: Int
)