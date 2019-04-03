package apps.dcoder.smartbellcontrol.viewmodels

import android.app.Application
import android.util.Log
import androidx.core.util.TimeUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import apps.dcoder.smartbellcontrol.Event
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.restapiclient.RetrofitAPIs
import apps.dcoder.smartbellcontrol.restapiclient.model.BellStatus
import apps.dcoder.smartbellcontrol.utils.TimeExtensions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Time
import java.util.*
import kotlin.collections.ArrayList

class SettingsViewModel(private val appContext: Application): AndroidViewModel(appContext) {
    private val bellAPI = RetrofitAPIs.bellAPI

    private val errorLiveData: MutableLiveData<Event<String>> = MutableLiveData()
    private val successLiveData: MutableLiveData<Event<String>> = MutableLiveData()
    private val backingErrorNotifyLiveData: MutableLiveData<Event<Void>> = MutableLiveData()
    private val backingSuccessNotifyLiveData: MutableLiveData<Event<Void>> = MutableLiveData()

    private val doNotDisturbSettingsLiveData: MutableLiveData<BellStatus.DoNotDisturbStatus> = MutableLiveData()

    private inner class CallStatusHandler(val onFailure: () -> Unit, val onSuccess: () -> Unit): Callback<Void> {

        override fun onFailure(call: Call<Void>, t: Throwable) {
            Log.e("SettingsDK", "Setting playback duration failed", t)
            onFailure()
        }

        override fun onResponse(call: Call<Void>, response: Response<Void>) {
            if (response.isSuccessful) {
                Log.d("SettingsDK", response.message() ?: "Getting playback duration successful")
                onSuccess()
            } else {
                Log.e("SettingsDK", response.errorBody()?.string() ?: "Unknown error occurred!")
                onFailure()
            }
        }
    }

    val errorNotifyLiveData: LiveData<Event<Void>>
        get() = backingErrorNotifyLiveData

    val successNotifyLiveData: LiveData<Event<Void>>
        get() = backingSuccessNotifyLiveData

    fun getSuccessLiveData(): LiveData<Event<String>> {
        return successLiveData
    }

    fun getErrorLiveData(): LiveData<Event<String>> {
        return errorLiveData
    }

    fun getDoNotDisturbSettingsLiveData(): LiveData<BellStatus.DoNotDisturbStatus> {
        return  doNotDisturbSettingsLiveData
    }

    fun setDoNotDisturbMode(daysArr: ArrayList<Int>,
                            startTimeArr: ArrayList<Int>,
                            endTimeArr: ArrayList<Int>,
                            endTomorrow: Boolean) {


        val startTimeMillis = TimeExtensions.extractCurrentTimeUTCMillis(startTimeArr)
        val endTimeMillis = TimeExtensions.extractCurrentTimeUTCMillis(endTimeArr)

        val call: Call<Void> = bellAPI.scheduleDoNotDisturb(daysArr, startTimeMillis, endTimeMillis, endTomorrow)
        call.enqueue(object : Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("DisturbDK", "Setting do not disturb failed!", t)
                errorLiveData.value = Event(appContext.getString(R.string.schedule_do_not_disturb_failed))
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    Log.d("DisturbDK", response.message() ?: "Setting do not disturb successful")
                    successLiveData.value = Event(appContext.getString(R.string.scheduled_do_not_disturb))

                    val cachedDoNotDisturbStatus = BellStatus.doNotDisturbStatus
                    cachedDoNotDisturbStatus.days = daysArr.toIntArray()
                    cachedDoNotDisturbStatus.startTimeMillis = startTimeMillis
                    cachedDoNotDisturbStatus.endTimeMillis = endTimeMillis
                    cachedDoNotDisturbStatus.isEndTomorrow = endTomorrow

                    val currentDateTime = Calendar.getInstance(TimeZone.getDefault())
                    val startDateTime = Calendar.getInstance(TimeZone.getDefault())
                    startDateTime.timeInMillis = startTimeMillis
                    val endDateTime = Calendar.getInstance(TimeZone.getDefault())
                    endDateTime.timeInMillis = endTimeMillis
                    if (endTomorrow) {
                        endDateTime.add(Calendar.DAY_OF_MONTH, 1)
                    }

                    cachedDoNotDisturbStatus.isInDoNotDisturb = currentDateTime.after(startDateTime) && currentDateTime.before(endDateTime)
                } else {
                    Log.e("DisturbDK", response.errorBody()?.string() ?: "Unknown error occurred!")
                    errorLiveData.value = Event(appContext.getString(R.string.schedule_do_not_disturb_failed))
                }
            }

        })
    }

    fun getDoNotDisturbStatus() {
        bellAPI.getDoNotDisturbStatus().enqueue(object: Callback<BellStatus.DoNotDisturbStatus> {

            override fun onFailure(call: Call<BellStatus.DoNotDisturbStatus>, t: Throwable) {
                Log.e("DisturbDK", "Getting do not disturb settings failed", t)
                errorLiveData.value = Event(appContext.getString(R.string.err_could_not_get_disturb_status))
            }

            override fun onResponse(
                call: Call<BellStatus.DoNotDisturbStatus>,
                response: Response<BellStatus.DoNotDisturbStatus>
            ) {
                if(response.isSuccessful) {
                    Log.d("DisturbDK", response.message() ?: "Getting settings for do not disturb successful")
                    doNotDisturbSettingsLiveData.value = response.body()

                } else {
                    Log.e("DisturbDK", response.errorBody()?.string() ?: "Unknown error occurred!")
                    errorLiveData.value = Event(appContext.getString(R.string.schedule_do_not_disturb_failed))
                }
            }

        })
    }

    fun setPlaybackMode(playbackMode: String) {
        bellAPI.setBellPlaybackMode(playbackMode).enqueue(
            CallStatusHandler(
                onFailure = { backingErrorNotifyLiveData.value = Event(null) },
                onSuccess = {
                    backingSuccessNotifyLiveData.value = Event(null)
                    BellStatus.coreStatus.playbackMode = playbackMode
                }
            )
        )
    }

    fun setPlaybackDuration(playbackDuration: Int) {
        bellAPI.setBellPlaybackDuration(playbackDuration).enqueue(
            CallStatusHandler(
                onFailure = { backingErrorNotifyLiveData.value = Event(null) },
                onSuccess = {
                    backingSuccessNotifyLiveData.value = Event(null)
                    BellStatus.coreStatus.playbackTime = playbackDuration
                }
            )
        )
    }

    fun setRingVolume(ringVolume: Int) {
        bellAPI.setRingVolume(ringVolume).enqueue(
            CallStatusHandler(
                onFailure = { backingErrorNotifyLiveData.value = Event(null)},
                onSuccess = {
                    backingSuccessNotifyLiveData.value = Event(null)
                    BellStatus.coreStatus.ringVolume = ringVolume
                })
        )
    }
}