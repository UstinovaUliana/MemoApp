package com.example.memoapp.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.example.memoapp.helper.NotificationHelper
import com.example.memoapp.helper.NotificationHelper.Companion.NOTIFICATION_ID
import com.example.memoapp.model.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


const val SERVICE_COMMAND = "Command"
const val NOTIFICATION_TEXT = "NotificationText"

class TimerService: Service(), CoroutineScope {

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

    private val helper by lazy { NotificationHelper(this) }

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        intent?.extras?.run {
            when (getSerializable(SERVICE_COMMAND) as TimerState) {
                TimerState.START -> startTimer()
                TimerState.PAUSE -> pauseTimerService()
                TimerState.STOP -> endTimerService()
                else -> return START_NOT_STICKY
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
        job.cancel()
    }

    private fun startTimer(elapsedTime: Int? = null) {
        serviceState = TimerState.START

        startedAtTimestamp = elapsedTime ?: 0

        startForeground(NotificationHelper.NOTIFICATION_ID, helper.getNotification())

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
        stopForeground(true)
        stopSelf()
    }

    private fun startCoroutineTimer() {
        launch(coroutineContext) {
            handler.post(runnable)
        }
    }
}