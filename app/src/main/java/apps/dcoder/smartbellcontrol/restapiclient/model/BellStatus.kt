package apps.dcoder.smartbellcontrol.restapiclient.model

object BellStatus {
    private val doNotDisturbStatus: DoNotDisturbStatus

    class DoNotDisturbStatus internal constructor() {
        var days: IntArray? = null

        var isInDoNotDisturb: Boolean = false
        var isEndTomorrow: Boolean = false

        var startTimeMillis: Long = 0
        var endTimeMillis: Long = 0
    }

    init {
        this.doNotDisturbStatus = DoNotDisturbStatus()
    }
}
