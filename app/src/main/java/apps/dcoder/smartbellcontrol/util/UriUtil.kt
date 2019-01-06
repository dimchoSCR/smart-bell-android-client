package apps.dcoder.smartbellcontrol.util

import android.net.Uri
import android.util.Log
import java.io.File
import java.lang.IllegalStateException

fun Uri.getFileName(): String {

    val uri = this

    // For example: <Storage Type>(primary/SD card):<Folder>/<Folder>/<File Name>
    val uriLastPathSegment = uri.lastPathSegment
    Log.d("DK", uriLastPathSegment)

    val splitLastPathSegment: List<String> = uriLastPathSegment!!.split(":")
    if (splitLastPathSegment.size <= 1 || splitLastPathSegment.size > 2) {
        throw IllegalStateException("Too many parts in split uri segment!")
    }

    val filePath = splitLastPathSegment[1] // For example: <Folder>/<Folder>/<FileName>
    val lastIndexOfDelimiter = filePath.lastIndexOf(File.separatorChar)

    return filePath.substring(lastIndexOfDelimiter + 1)

}