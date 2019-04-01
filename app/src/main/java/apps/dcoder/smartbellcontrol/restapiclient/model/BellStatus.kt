package apps.dcoder.smartbellcontrol.restapiclient.model

import com.fasterxml.jackson.annotation.JsonProperty

object BellStatus {
    val coreStatus: CoreStatus = CoreStatus()
    val doNotDisturbStatus: DoNotDisturbStatus = DoNotDisturbStatus()

    class CoreStatus internal constructor() {
        var currentRingtone: String = ""
        var playbackMode: String = ""
        var ringVolume: Int = -1
        var playbackTime: Int = -1
    }

    class DoNotDisturbStatus internal constructor() {
        var days: IntArray? = null

        var isInDoNotDisturb: Boolean = false
        var isEndTomorrow: Boolean = false

        var startTimeMillis: Long = 0
        var endTimeMillis: Long = 0
    }

    @JsonProperty("coreStatus")
    private fun unpackNestedCoreStatus(status: Map<String, Any>) {
        coreStatus.currentRingtone = status["currentRingtone"] as String
        coreStatus.playbackMode = status["playbackMode"] as String
        coreStatus.ringVolume = status["ringVolume"] as Int
        coreStatus.playbackTime = status["playbackTime"] as Int
    }

    @JsonProperty("doNotDisturbStatus")
    private fun unpackNestedDisturb(status: Map<String, Any>) {
        doNotDisturbStatus.days = (status["days"] as ArrayList<*>).filterIsInstance<Int>().toIntArray()
        doNotDisturbStatus.isInDoNotDisturb = status["inDoNotDisturb"] as Boolean
        doNotDisturbStatus.isEndTomorrow = status["endTomorrow"] as Boolean
        doNotDisturbStatus.startTimeMillis = status["startTimeMillis"] as Long
        doNotDisturbStatus.endTimeMillis = status["endTimeMillis"] as Long
    }
}
