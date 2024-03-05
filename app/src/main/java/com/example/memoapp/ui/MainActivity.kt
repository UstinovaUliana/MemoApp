package com.example.memoapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.memoapp.R
import com.example.memoapp.databinding.ActivityMainBinding
import com.example.memoapp.helper.SharedPrefs
import com.example.memoapp.helper.onClick
import com.example.memoapp.helper.secondsToTime
import com.example.memoapp.model.Level
import com.example.memoapp.model.Level.Companion.getLevel
import com.example.memoapp.model.MusicState
import com.example.memoapp.model.TimerState
import com.example.memoapp.service.*
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

    private val timerReceiver: TimerReceiver by lazy { TimerReceiver() }

    inner class TimerReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
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
            registerReceiver(timerReceiver, IntentFilter(TIMER_ACTION))
            mainViewModel.isReceiverRegistered = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (mainViewModel.isReceiverRegistered) {
            unregisterReceiver(timerReceiver)
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

    private fun sendCommandToBoundService(state: MusicState) {
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

    private fun sendCommandToForegroundService(timerState: TimerState) {
        mainViewModel.isForegroundServiceRunning = timerState != TimerState.STOP
        ContextCompat.startForegroundService(this, getServiceIntent(timerState))
    }

    private fun getServiceIntent(command: TimerState) =
        Intent(this, TimerService::class.java).apply {
            putExtra(SERVICE_COMMAND, command as Parcelable)
        }


    private fun setupUi() {
        with(binding) {
            gridview.adapter = adapter
            btnPlay.onClick {
                prepareCardView()
                sendCommandToForegroundService(TimerState.START)
            }
            btnPlayMusic.onClick {
                sendCommandToBoundService(MusicState.PLAY)
            }
            btnPauseMusic.onClick {
                sendCommandToBoundService(MusicState.PAUSE)
            }
            btnStopMusic.onClick {
                sendCommandToBoundService(MusicState.STOP)
            }
            btnShuffleMusic.onClick {
                getNameOfSong()
            }
            btnQuit.onClick {
                binding.tvTime.clearComposingText()
                sendCommandToForegroundService(TimerState.STOP)
            }
        }

        updateVisibility()
    }

    private fun updateVisibility() {
        with(binding) {
            val btnPlayVisible = if (mainViewModel.isForegroundServiceRunning) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }

            val gridVisible = if (mainViewModel.isForegroundServiceRunning) {
                View.VISIBLE
            } else {
                View.INVISIBLE
            }
            btnPlay.apply {
                visibility = btnPlayVisible
                text = String.format(
                    getString(R.string.play_p_level),
                    getLevel(sharedPrefs.getStoredLevel())?.name
                )
            }
            gridview.visibility = gridVisible
            tvTime.visibility = gridVisible
            btnQuit.visibility = gridVisible
        }
    }

    private fun prepareCardView() {
        val currentLevel = getLevel(sharedPrefs.getStoredLevel()) ?: Level.BEGINNER

        mainViewModel.pairs = 0
        mainViewModel.pairsSum = currentLevel.numberOfCards

        binding.gridview.numColumns = mainViewModel.getNumberOfColumns(currentLevel)

        adapter.updateData(mainViewModel.gerRandomItems(sharedPrefs.getStoredLevel()))
    }

    private fun enableButtons(state: MusicState) {
        val songPlays = state == MusicState.PLAY || state == MusicState.SHUFFLE_SONGS
        with(binding) {
            btnPlayMusic.isEnabled = !songPlays
            btnPauseMusic.isEnabled = songPlays
            btnStopMusic.isEnabled = songPlays
            btnShuffleMusic.isEnabled = songPlays
            btnSongName.apply {
                isEnabled = songPlays
                btnSongName.visibility = if (songPlays) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            }
        }
    }

    private fun updateUi(elapsedTime: Int) {
        mainViewModel.elapsedTime = elapsedTime
        binding.tvTime.text = elapsedTime.secondsToTime()
    }

    fun checkProgress() {
        val currentLevel = getLevel(sharedPrefs.getStoredLevel())
        if (currentLevel == Level.NONE) {
            showResetProgressDialog()
        }
    }

    private fun storeLevel() {
        val currentLevel = getLevel(sharedPrefs.getStoredLevel())
        currentLevel?.let {
            sharedPrefs.storeLevel(
                when (it) {
                    Level.BEGINNER -> Level.INTERMEDIATE
                    Level.INTERMEDIATE -> Level.ADVANCED
                    Level.ADVANCED -> Level.EXPERT
                    Level.EXPERT -> Level.NONE
                    Level.NONE -> Level.BEGINNER
                }
            )
        }
    }

    private fun informUser(state: MusicState) {
        @StringRes val res = when (state) {
            MusicState.PLAY -> R.string.music_started
            MusicState.PAUSE -> R.string.music_paused
            MusicState.STOP -> R.string.music_stopped
            MusicState.SHUFFLE_SONGS -> R.string.songs_shuffled

        }

        Toast.makeText(this, res, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccessDialog() {
        showDialog(
            String.format(getString(R.string.well_done_your_time_is_p), mainViewModel.elapsedTime),
            getString(R.string.click_ok_for_proceeding_to_the_next_level),
        ) {
            checkProgress()
        }
    }

    private fun showResetProgressDialog() {
        showDialog(
            getString(R.string.you_have_finished_all_levels),
            getString(R.string.click_ok_to_reset_progress),
        ) {
            sharedPrefs.storeLevel(Level.BEGINNER)
        }
    }

    private fun showDialog(
        title: String,
        message: String,
        action: () -> Unit
    ) {
        with(alertBuilder) {
            setCancelable(false)
            setTitle(title)
            setMessage(message)
            setPositiveButton(getString(R.string.ok)) { _, _ ->
                action()
            }
            show()
        }
    }

}