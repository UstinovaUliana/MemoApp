package com.example.memoapp.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class TimerState: Parcelable {
    INITIALIZED,
    START,
    PAUSE,
    STOP
}