package com.example.memoapp.service

import android.os.Handler
import android.os.Looper
import com.example.memoapp.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


const val SERVICE_COMMAND = "Command"
const val NOTIFICATION_TEXT = "NotificationText"

class TimerService: CoroutineScope {

    var serviceState: TimerState = TimerState.INITIALIZED
    private var currentTime: Int = 0
    private var startedAtTimestamp: Int = 0
    set(value) {
        currentTime = value
        field = value
    }

    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable = object : Runnable {
        override fun run() {
            currentTime++
            broadcastUpdate()
            handler.postDelayed(this, 1000)
        }
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private fun startTimer(elapsedTime: Int? = null) {
        serviceState = TimerState.START

        startedAtTimestamp = elapsedTime ?: 0

        broadcastUpdate()

        startCoroutineTimer()
    }

    private fun broadcastUpdate() {
        if (serviceState == TimerState.START) {
            val elapseTime = (currentTime - startedAtTimestamp)
        } else if (serviceState == TimerState.PAUSE) {

        }
    }

    private fun pauseTimerService() {
        serviceState = TimerState.PAUSE
        handler.removeCallbacks(runnable)
        broadcastUpdate()
    }

    private fun endTimerService() {
        serviceState = TimerState.STOP
        handler.removeCallbacks(runnable)
        job.cancel()
        broadcastUpdate()
        stopService()
    }

    private fun stopService() {

    }

    private fun startCoroutineTimer() {
        launch(coroutineContext) {
            handler.post(runnable)
        }
    }
}