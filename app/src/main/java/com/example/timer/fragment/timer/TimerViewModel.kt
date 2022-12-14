package com.example.timer.viewmodel

import android.app.Application
import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.*
import com.example.timer.model.Timer
import com.example.timer.fragment.timer.utils.TimerNotification
import com.example.timer.fragment.timer.utils.TimerObserver
import com.example.timer.model.Sequence

class TimerViewModel(
    application: Application,
    private val sequence: Sequence,
    private val timerList: Array<Timer>,
    firstInit: Boolean): AndroidViewModel(application) {

    var timerObserver: TimerObserver

    private val context: Context
        get() = getApplication<Application>().applicationContext

    private val notificationAdapter = TimerNotification()


    companion object{
        private var firstInit0: Boolean = true;
    }

    init{
        Log.d("*** TimerViewModel ***", "init")
        timerObserver = TimerObserver(timerList, context, sequence, firstInit0)
        firstInit0 = false;
        timerObserver.register(context)
    }

    fun start() = timerObserver.start()

    fun stop() = timerObserver.stop()

    fun previousTimer() = timerObserver.previousTimer()

    fun nextTimer() = timerObserver.nextTimer()


    fun stopNotification(){
        notificationAdapter.tryStopNotification(context)
    }

    fun startNotification(){
        notificationAdapter.startNotification(sequence, context, timerList.size)
    }

    fun deleteTimer(){
        notificationAdapter.tryStopNotification(context)
        timerObserver.unregister(context)
        timerObserver.delete(context)
    }

    override fun onCleared() {
        notificationAdapter.tryStopNotification(context)
        timerObserver.unregister(context)
    }
}

class TimerViewModelFactory(
    private val application: Application,
    private val sequence: Sequence,
    private val timerList: Array<Timer>,
    private val firstInit: Boolean
) : ViewModelProvider.AndroidViewModelFactory(application)  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            return TimerViewModel(application, sequence, timerList, firstInit) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}