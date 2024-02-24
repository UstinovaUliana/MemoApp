package com.example.memoapp.model

import androidx.annotation.DrawableRes
import java.util.*

data class IconModel(
    @DrawableRes val res: Int,
    var id: UUID = UUID.randomUUID(),
    var state: CardState = CardState.CLOSED
)
