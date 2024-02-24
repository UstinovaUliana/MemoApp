package com.example.memoapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.memoapp.R
import com.example.memoapp.model.CardState
import com.example.memoapp.model.IconModel
import com.example.memoapp.model.Level
import java.util.*

class MainViewModel: ViewModel() {

    var isReceiverRegistered: Boolean = false
    var isForegroundServiceRunning: Boolean = false
        set(value) {
            field = value
            updateGridVisibility.value = Unit
        }
    var isMusicServiceBound: Boolean = false

    var elapsedTime: Int = 0
    var lastOpenedCard: IconModel? = null
    var pairsSum: Int = 0
    var pairs: Int = 0
        set(value) {
            field = value

            // reset last card
            if (value == 0) lastOpenedCard = null
        }

    val closeCards = MutableLiveData<IconModel>()
    val pairMatch = MutableLiveData<IconModel>()
    val updateGridVisibility = MutableLiveData<Unit>()
    val showSuccessDialog = MutableLiveData<Unit>()

    private val iconList = listOf(
        IconModel(R.drawable.ic_tie_fighter),
        IconModel(R.drawable.ic_stormtropper),
        IconModel(R.drawable.ic_chew),
        IconModel(R.drawable.ic_combo),
        IconModel(R.drawable.ic_bb),
        IconModel(R.drawable.ic_rd),
        IconModel(R.drawable.ic_darth_maul),
    )

    fun getNumberOfColumns(level:Level): Int =
        when (level) {
            Level.BEGINNER, Level.INTERMEDIATE -> 2
            else -> 3
        }

    fun gerRandomItems(numberOfCards: Int): List<IconModel> {
        //?
        val randomItems = iconList.shuffled().take(numberOfCards)
        val duplicates = randomItems.map {
            it.copy(id = UUID.randomUUID())
        }.toMutableList()

        return with(randomItems + duplicates) {
            shuffled()
            map{it.state = CardState.CLOSED}
            toMutableList()
        }
    }

    internal fun checkIsMatchFound(clickedItem: IconModel) {
        lastOpenedCard?.let{
            if (it.res == clickedItem.res) {
                pairMatch.value = clickedItem
                twoCardsMatched()
            } else {
                closeCards.value = clickedItem
            }
        } ?: kotlin.run {
            lastOpenedCard = clickedItem
        }
    }


    private fun twoCardsMatched() {
        pairs++
        if (pairs == pairsSum) {
            showSuccessDialog.postValue(Unit)
        }
    }
}