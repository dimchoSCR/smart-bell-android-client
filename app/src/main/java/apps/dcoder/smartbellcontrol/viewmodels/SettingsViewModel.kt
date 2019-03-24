package apps.dcoder.smartbellcontrol.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.restapiclient.RetrofitAPIs
import apps.dcoder.smartbellcontrol.restapiclient.model.BellStatus
import apps.dcoder.smartbellcontrol.utils.TimeExtensions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class SettingsViewModel(private val appContext: Application): AndroidViewModel(appContext) {
    private val bellAPI = RetrofitAPIs.bellAPI

    private val errorLiveData: MutableLiveData<String> = MutableLiveData()
    private val successLiveData: MutableLiveData<String> = MutableLiveData()

    private val doNotDisturbSettingsLiveData: MutableLiveData<BellStatus.DoNotDisturbStatus> = MutableLiveData()

    fun getSuccessLiveData(): LiveData<String> {
        return successLiveData
    }

    fun getErrorLiveData(): LiveData<String> {
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
                errorLiveData.value = appContext.getString(R.string.schedule_do_not_disturb_failed)
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if(response.isSuccessful) {
                    Log.d("DisturbDK", response.message() ?: "Setting do not disturb successful")
                    successLiveData.value = appContext.getString(R.string.scheduled_do_not_disturb)


                } else {
                    Log.e("DisturbDK", response.errorBody()?.string() ?: "Unknown error occurred!")
                    errorLiveData.value = appContext.getString(R.string.schedule_do_not_disturb_failed)
                }
            }

        })
    }

    fun getDoNotDisturbStatus() {
        bellAPI.getDoNotDisturbStatus().enqueue(object: Callback<BellStatus.DoNotDisturbStatus> {

            override fun onFailure(call: Call<BellStatus.DoNotDisturbStatus>, t: Throwable) {
                Log.e("DisturbDK", "Getting do not disturb settings failed", t)
                errorLiveData.value = appContext.getString(R.string.err_could_not_get_disturb_status)
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
                    errorLiveData.value = appContext.getString(R.string.schedule_do_not_disturb_failed)
                }
            }

        })
    }
}