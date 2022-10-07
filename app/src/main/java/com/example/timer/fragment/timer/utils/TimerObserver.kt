package com.example.timer.fragment.timer.utils

import android.content.*
import android.media.MediaPlayer
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.timer.R
import com.example.timer.model.Timer
import com.example.timer.service.TimerService
import com.example.timer.model.Sequence
import android.speech.tts.TextToSpeech
import java.util.*
import kotlin.collections.ArrayList


class TimerObserver(
    private val timerList: Array<Timer>,
    context: Context,
    sequence: Sequence,
    firstInit: Boolean) {

    private var _title: MutableLiveData<String> = MutableLiveData(timerList[0].title)
    val title: LiveData<String>
        get() = _title

    private var _timerIndex: MutableLiveData<Int> = MutableLiveData(0)
    val timerIndex: LiveData<Int>
        get() = _timerIndex

    private var _remainingTime: MutableLiveData<Int> = MutableLiveData(timerList[0].duration)
    val remainingTime: LiveData<Int>
        get() = _remainingTime

    //val voice_on = sequence.voice_on
    var countdown = -1;

    private var _status: MutableLiveData<TimerStatus> = MutableLiveData(TimerStatus.PAUSE)
    val status: LiveData<TimerStatus>
        get() = _status

    private lateinit var mService: TimerService
    private var mBound: Boolean = false

    private var prev_title = ""
    private var prev_num = ""

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.LocalBinder
            mService = binder.getService()
            mService.myInit(timerList);

            mBound = true
            _status.value = mService.getStatus()
            _timerIndex.value = mService.getIndex()
            _title.value = mService.getTitle()
            _remainingTime.value = mService.getRemainingTime()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    val player: MediaPlayer = MediaPlayer.create(context, R.raw.timer_sound)

    private var receiver: BroadcastReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == TimerService.TICK) {
                _title.value = intent.getStringExtra(TimerService.TITLE)
                _timerIndex.value = intent.getIntExtra(TimerService.TIMER_INDEX, 0)
                _remainingTime.value = intent.getIntExtra(TimerService.REMAINING_TIME, 0)

                if (intent.getBooleanExtra("LAST_TICKS", false)) {
                    player.start()
                }
            }
            else if (intent.action == TimerService.FINISH) {
                val title = intent.getStringExtra("TITLE")
                if (title == "") {
                    _status.value = TimerStatus.PAUSE
//                    mService.myInit(timerList, voice_on)

                } else {
//                    Log.d("*** TimerObserver ***", "sound")
                    player.start()
                }
            }
        }
    }

    init{
        val intent = Intent(context, TimerService::class.java)
        if (firstInit) {
            Log.d("*** TimerObserver ***", "FIRST INIT")
            intent.putParcelableArrayListExtra(TimerService.TIMER_LIST, timerList.toCollection(ArrayList()))
            intent.putExtra("VOICE_ON", sequence.voice_on)
            context.startService(intent)
//            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        else{
            Log.d("*** TimerObserver ***", "NOT FIRST INIT")
        }
        start()
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun register(context: Context){
        val filter = IntentFilter()
        filter.addAction(TimerService.TICK);
        filter.addAction(TimerService.FINISH);
        context.registerReceiver(receiver, filter)
    }

    fun unregister(context: Context){
        context.unregisterReceiver(receiver)
    }

    fun start() {
        if (mBound){
            mService.startTimer()
            if (mService.timerIndex >= mService.timerList.size - 1 && mService.getRemainingTime() < 1) {
                mService.nextTimer()
            }
            _status.value = TimerStatus.RUN
        }
    }

    fun stop(){
        if (mBound){
            mService.stopTimer()
            _status.value = TimerStatus.PAUSE
        }
    }

    fun previousTimer(){
        if (mBound){
            mService.previousTimer()
            _title.value = mService.getTitle()
            _timerIndex.value = mService.getIndex()
            _remainingTime.value = mService.getRemainingTime()
        }
    }

    fun nextTimer(){
        if (mBound){
            mService.nextTimer()
            _title.value = mService.getTitle()
            _timerIndex.value = mService.getIndex()
            _remainingTime.value = mService.getRemainingTime()
        }
    }

    fun delete(context: Context){
        if (mBound){
            mService.deleteTimer()
        }
    }
}

const val PREFS_TIMER = "timer"
const val KEY_SEQUENCE = "sequence"

enum class TimerStatus{
    PAUSE, RUN
}