package com.example.memoapp.service

import android.media.MediaPlayer
import com.example.memoapp.R
import com.example.memoapp.model.MusicState

class MusicService {
    private var musicState = MusicState.STOP
    private var musicMediaPlayer: MediaPlayer? = null

    private val songs: List<Int> = listOf(
        R.raw.driving_ambition,
        R.raw.beautiful_dream
    )
    private var randomSongs = mutableListOf<Int>()

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
    }

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