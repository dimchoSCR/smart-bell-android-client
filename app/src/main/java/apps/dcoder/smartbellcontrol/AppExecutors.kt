package apps.dcoder.smartbellcontrol

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

object AppExecutors {
    val singleExecutor: Executor = Executors.newSingleThreadExecutor()

    object MainThreadExecutor : Executor {

        private val mainHandler: Handler = Handler(Looper.getMainLooper())
        override fun execute(task: Runnable) {
            mainHandler.post(task)
        }

    }
}