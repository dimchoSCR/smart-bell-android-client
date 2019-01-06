package apps.dcoder.smartbellcontrol.util

import android.util.Log
import apps.dcoder.smartbellcontrol.AppExecutors
import java.io.ByteArrayOutputStream
import java.io.InputStream

fun InputStream.getBytesAsync(onGetBytesSuccess: (ByteArray) -> Unit) {

    val inputStream = this
    val outputStream = ByteArrayOutputStream()

    AppExecutors.singleExecutor.execute {
        val bytesCopied = inputStream.copyTo(outputStream)
        Log.d("DK", "Bytes copied: $bytesCopied")

        AppExecutors.MainThreadExecutor.execute {
            onGetBytesSuccess(outputStream.toByteArray())
            outputStream.close()
        }

    }
}