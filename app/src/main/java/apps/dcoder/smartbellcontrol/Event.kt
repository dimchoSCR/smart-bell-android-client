package apps.dcoder.smartbellcontrol

class Event<out T> (private val content: T?) {

    var consumed = false
        private set

    fun getContentIfNotConsumed(): T? {
        if(!consumed) {
            consumed = true
            return content
        }

        return null
    }

    fun consume() {
        consumed = true
    }

    fun peekContent(): T? = content
}