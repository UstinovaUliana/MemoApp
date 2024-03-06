package com.example.memoapp.service

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.example.memoapp.R
import com.example.memoapp.model.MusicState
import java.util.*

class MusicService : Service() {
    private var musicState = MusicState.STOP
    private var musicMediaPlayer: MediaPlayer? = null

    private val songs: List<Int> = listOf(
        R.raw.driving_ambition,
        R.raw.beautiful_dream
    )
    private var randomSongs = mutableListOf<Int>()

    private val binder by lazy { MusicBinder() }

    override fun onBind(p0: Intent?): IBinder = binder

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    fun runAction(state: MusicState) {
        musicState = state
        when(state) {
            MusicState.PLAY -> startMusic()
            MusicState.PAUSE -> pauseMusic()
            MusicState.STOP -> stopMusic()
            MusicState.SHUFFLE_SONGS -> shuffleSongs()
        }
    }

    private fun initializeMediaPlayer() {
        if (randomSongs.isEmpty()) {
            randomizeSongs()

        } else {

        }
        musicMediaPlayer = MediaPlayer.create(this,randomSongs.first()).apply {
            isLooping = true
        }
    }

    fun getNameOfSong(): String =
        resources.getResourceEntryName(randomSongs.first())
            .replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.ENGLISH)
                else it.toString()
            }.replace("_", " ")

    private fun startMusic() {
        initializeMediaPlayer()
        musicMediaPlayer?.start()
    }

    private fun pauseMusic() {
        musicMediaPlayer?.pause()
    }

    private fun stopMusic() {
        musicMediaPlayer?.run {
            stop()
            release()
        }
    }

    private fun shuffleSongs() {
        musicMediaPlayer?.run {
            stop()
            release()
        }
        randomizeSongs()
        startMusic()
    }

    private fun randomizeSongs() {
        randomSongs.clear()
        randomSongs.addAll(songs.shuffled())
    }


}