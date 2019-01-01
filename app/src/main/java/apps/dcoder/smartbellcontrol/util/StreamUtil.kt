package apps.dcoder.smartbellcontrol.util

import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.InputStream



fun InputStream.getBytes(): ByteArray {
    val inputStream = this
    val outputStream = ByteArrayOutputStream()
//    val buffer: ByteArray = ByteArray(BUFFER_SIZE)
//    var length = 0
//    do{
//        length = inputStream.read(buffer,0, buffer.size)
//        outputStream.write(buffer,0, length)
//    } while (length != -1)

    val bytesCopied = inputStream.copyTo(outputStream)
    Log.d("DK", "Bytes copied: $bytesCopied")

    return outputStream.toByteArray()
}