package com.example.memoapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.memoapp.R
import com.example.memoapp.databinding.ActivityMainBinding
import com.example.memoapp.helper.SharedPrefs
import com.example.memoapp.ui.adapter.IconAdapter
import com.example.memoapp.viewmodel.MainViewModel

const val TIMER_ACTION = "TimerAction"

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    private val sharedPrefs by lazy { SharedPrefs(this) }
    private val adapter by lazy { IconAdapter(this, mainViewModel::checkIsMatchFound) }
    private val alertBuilder by lazy { AlertDialog.Builder(this) }
    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private val timereReceiver: TimerReciever by lazy { TimerReceiver() }

    inner class TimerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent.action == TIMER_ACTION) updateUi(intent.getIntExtra(NOTIFICATION_TEXT, 0))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        observe()
        setupUi()
    }

    private fun observe() {
        mainViewModel.apply {
            updateGridVisibility.observe(this@MainActivity) {
                updateVisibility()
            }

            closeCards.observe(this@MainActivity) { clickedItem ->
                handler.postDelayed({
                    adapter.revertVisibility(mainViewModel.lastOpenedCard, clickedItem)
                    mainViewModel.lastOpenedCard = null
                }, 300)
            }

            pairMatch.observe(this@MainActivity) { clickedItem ->
                handler.postDelayed({
                    adapter.pairMatch(mainViewModel.lastOpenedCard, clickedItem)
                    mainViewModel.lastOpenedCard = null
                }, 300)
            }
            showSuccessDialog.observe(this@MainActivity) {
                storeLevel()
                sendCommandToForegroundService(TimerState.STOP)
                showSuccessDialog()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (!mainViewModel.isReceiverRegistered) {
            registerReceiver(timereReceiver, IntentFilter(TIMER_ACTION))
            mainViewModel.isReceiverRegistered = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (mainViewModel.isReceiverRegistered) {
            unregisterReceiver(timereReceiver)
            mainViewModel.isReceiverRegistered = false
        }
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onDestroy() {
        super.onDestroy()
        unbindMusicService()

        if (isFinishing && mainViewModel.isForegroundServiceRunning) {
            sendCommandToForegroundService(TimerState.PAUSE)
        }
    }

    private fun unbindMusicService() {
        if (mainViewModel.isMusicServiceBound) {

            mainViewModel.isMusicServiceBound = false
        }
    }

    private fun sendCommandToForegroundService(state: MusicState) {
        if (mainViewModel.isMusicServiceBound) {

            informUser(state)
            enableButtons(state)
        } else {
            Toast.makeText(this, R.string.service_is_not_bound, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getNameOfSong() {
        val message = if (mainViewModel.isMusicServiceBound) {
            getString(R.string.unknown)
        } else {
            getString(R.string.service_is_not_bound)
        }

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



}