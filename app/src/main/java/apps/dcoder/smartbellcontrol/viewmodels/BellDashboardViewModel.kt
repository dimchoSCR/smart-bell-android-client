package apps.dcoder.smartbellcontrol.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import apps.dcoder.smartbellcontrol.Event
import apps.dcoder.smartbellcontrol.R
import apps.dcoder.smartbellcontrol.formatRawRingEntry
import apps.dcoder.smartbellcontrol.restapiclient.RetrofitAPIs
import apps.dcoder.smartbellcontrol.restapiclient.model.BellStatus
import apps.dcoder.smartbellcontrol.restapiclient.model.RawRingEntry
import apps.dcoder.smartbellcontrol.restapiclient.model.RingEntry
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BellDashboardViewModel(private val appContext: Application): AndroidViewModel(appContext) {

    private val bellAPI = RetrofitAPIs.bellAPI
    private val backingErrorLiveData = MutableLiveData<Event<String>>()
    private val backingErrorLiveDataBellStatus = MutableLiveData<Event<String>>()
    private val backingLogEntriesLiveData = MutableLiveData<List<RingEntry>>()
    private val backingBellStatusLiveData = MutableLiveData<BellStatus>()

    companion object {
        private const val LOG_DATE_TIME_FORMAT = "dd MMMM yyyy, HH:mm:ss "
    }

    val errorLiveData: LiveData<Event<String>>
        get() = backingErrorLiveData

    val errorLiveDataBellStatus: LiveData<Event<String>>
        get() = backingErrorLiveDataBellStatus

    val logEntriesLiveData: LiveData<List<RingEntry>>
        get() = backingLogEntriesLiveData

    val bellStatusLiveData: LiveData<BellStatus>
        get() = backingBellStatusLiveData

    fun loadRingLog() {
        bellAPI.getAllLogEntries().enqueue(object: Callback<List<RawRingEntry>> {
            override fun onFailure(call: Call<List<RawRingEntry>>, t: Throwable) {
                Log.e("LogDK", "Getting ring log failed", t)
                backingErrorLiveData.value = Event(appContext.getString(R.string.error_connecting_to_bell))
            }

            override fun onResponse(call: Call<List<RawRingEntry>>, response: Response<List<RawRingEntry>>) {
                if(response.isSuccessful) {
                    Log.d("LogDK", response.message() ?: "Getting log successful")
                    val rawRingEntries = response.body()
                    if (rawRingEntries == null) {
                        backingErrorLiveData.value = Event(appContext.getString(R.string.unknown_error))
                        return
                    }

                    val ringEntries = rawRingEntries.map { formatRawRingEntry(it, LOG_DATE_TIME_FORMAT) }
                    backingLogEntriesLiveData.value = ringEntries

                } else {
                    Log.e("LogDK", response.errorBody()?.string() ?: "Unknown error occurred!")
                    backingErrorLiveData.value = Event(appContext.getString(R.string.internal_bell_error))
                }
            }

        })
    }

    fun loadBellStatus() {

        bellAPI.getBellStatus().enqueue(object: Callback<BellStatus> {
            override fun onFailure(call: Call<BellStatus>, t: Throwable) {
                Log.e("BellStatusDK", "Getting bell status failed", t)
                backingErrorLiveDataBellStatus.value = Event(appContext.getString(R.string.error_connecting_to_bell))
            }

            override fun onResponse(call: Call<BellStatus>, response: Response<BellStatus>) {
                if(response.isSuccessful) {
                    Log.d("BellStatusDK", response.message() ?: "Getting bell status successful")
                    val bellStatus = response.body()
                    if (bellStatus == null) {
                        backingErrorLiveDataBellStatus.value = Event(appContext.getString(R.string.unknown_error))
                        return
                    }

                    backingBellStatusLiveData.value = bellStatus

                } else {
                    Log.e("BellStatusDK", response.errorBody()?.string() ?: "Unknown error occurred!")
                    backingErrorLiveDataBellStatus.value = Event(appContext.getString(R.string.internal_bell_error))
                }
            }

        })
    }

}