package com.example.timer.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.CountDownTimer
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.timer.fragment.timer.utils.TimerStatus
import com.example.timer.model.Timer
import java.util.*
import android.media.MediaPlayer
import com.example.timer.R


class TimerService : Service() {
    lateinit var timerList: Array<Timer>
    var timerIndex = 0
    private var deleted = false

    private var timer: CountDownTimer? = null
    private var remainingTime: Long = 0

    private val binder = LocalBinder()

    private var status = TimerStatus.PAUSE

//    private var voice_on = false
    private var prev_time = -1
    private var prev_title = ""

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d("*** TimerService ***", "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("*** TimerService ***", "onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d("*** TimerService ***", "OnStartCommand")
        return START_NOT_STICKY
    }

    fun myInit(timerList0: Array<Timer>){
        timerList = timerList0;
        timerIndex = 0
        remainingTime = timerList[timerIndex].duration * 1000L
    }

    fun getStatus() = status
    fun getIndex() = timerIndex
    fun getRemainingTime(): Int = ((remainingTime + 500) / 1000).toInt()
    fun getTitle() = if (timerIndex < timerList.size - 1 && timerIndex >= 0) timerList[timerIndex].title else ""

    fun startTimer(){
        Log.d("*** TimerService ***", "TIMER STARTED")
        status = TimerStatus.RUN
        timer = object: CountDownTimer(remainingTime, 1) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val intent = Intent()
                intent.action = TICK
                intent.putExtra(TITLE, timerList[minOf(timerIndex, timerList.size - 1)].title)
                intent.putExtra(TIMER_INDEX, timerIndex)
                val time = getRemainingTime()
                intent.putExtra(REMAINING_TIME, time)
//                intent.putExtra("VOICE_ON", voice_on)
                if (timerIndex + 1 == timerList.size) {
                    if (time < 5 && time != prev_time) {
                        Log.d("*** TimerService ***", "TIME = ${time}")
                        prev_time = time
                        intent.putExtra("LAST_TICKS", true)
                    }
                }
                sendBroadcast(intent)
            }

            override fun onFinish() {
                val intent = Intent()
                intent.action = FINISH
//                intent.putExtra("VOICE_ON", voice_on)
                var title = ""
                if (timerIndex + 1 < timerList.size) {
                    title = timerList[timerIndex + 1].title
                }
                if (title != prev_title) {
                    prev_title = title
                    intent.putExtra("TITLE", title)
//                    Log.d("*** TimerService ***", "TITLE = ${title}")
                }
                sendBroadcast(intent)

                timerIndex++
                if (timerIndex == timerList.size){
                }
                else{
                    remainingTime = timerList[minOf(timerIndex, timerList.size - 1)].duration * 1000L
                    startTimer()
                }
            }
        }.start()
    }

    fun stopTimer(){
        Log.d("*** TIMER ***", "TIMER STOPPED")
        status = TimerStatus.PAUSE
        timer?.cancel()
//        stopSelf()
    }

    fun previousTimer(){
        val needStart = status == TimerStatus.RUN
        stopTimer()

        if (timerIndex > 0) {
            timerIndex--
        }
        if (timerIndex >= timerList.size - 1) {
            timerIndex--
        }
        remainingTime = timerList[timerIndex].duration * 1000L
        if (needStart) startTimer()
    }

    fun nextTimer() {
        var needStart = status == TimerStatus.RUN
        stopTimer()
        if (timerIndex < timerList.size - 1){
            timerIndex++
        } else {
            timerIndex = 0
            needStart = true
        }
        remainingTime = timerList[timerIndex].duration * 1000L
        if (needStart) startTimer()
    }

    fun deleteTimer() {
        stopTimer()
        deleted = true;
    }

    companion object {
        var instance: TimerService? = null

        fun hasRunningTimer() = instance?.deleted == false

        const val TICK = "TICK"
        const val FINISH = "FINISH"

        const val TIMER_INDEX = "TIMER_INDEX"
        const val TIMER_LIST = "TIMER_LIST"
        const val TITLE = "TITLE"
        const val REMAINING_TIME = "REMAINING_TIME"
    }
}