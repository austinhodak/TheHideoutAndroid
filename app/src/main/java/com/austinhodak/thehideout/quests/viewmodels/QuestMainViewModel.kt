package com.austinhodak.thehideout.quests.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.quests.QuestFilter
import com.austinhodak.thehideout.quests.models.Traders
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.*

@HiltViewModel
class QuestMainViewModel @Inject constructor(
    private val repository: TarkovRepo
) : ViewModel() {

    private val _view = MutableLiveData(QuestFilter.AVAILABLE)
    val view = _view

    fun setView(int: QuestFilter) {
        _view.value = int
    }

    private val _selectedTrader = MutableLiveData(Traders.PRAPOR)
    val selectedTrader = _selectedTrader

    fun selectTrader(trader: Traders) {
        _selectedTrader.value = trader
    }

    val pmcElimsTotal = MutableLiveData(0)
    val scavElimsTotal = MutableLiveData(0)
    val questItemsTotal = MutableLiveData(0)
    val questFIRItemsTotal = MutableLiveData(0)
    val handoverItemsTotal = MutableLiveData(0)
    val placedTotal = MutableLiveData(0)
    val pickupTotal = MutableLiveData(0)

    private suspend fun updateTotals() {

        repository.getAllQuests().collect { quests ->
            pmcElimsTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "kill" && objective.target?.contains("PMCs") == true) {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            scavElimsTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "kill" && objective.target?.contains("Scavs") == true) {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            questItemsTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "find" || objective.type == "collect" && objective.number!! < 500) {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            questFIRItemsTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "find" && objective.number!! < 500) {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            handoverItemsTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.number!! >= 500) total += 1
                    if (objective.type == "collect" && objective.number!! < 500) {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            placedTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "place" || objective.type == "mark") {
                        total += objective.number ?: 0
                    }
                }
                total
            }

            pickupTotal.value = quests.sumOf { quest ->
                var total = 0
                for (objective in quest.objective!!) {
                    if (objective.type == "pickup") {
                        total += objective.number ?: 0
                    }
                }
                total
            }
        }
    }

    init {
        viewModelScope.launch {
            updateTotals()
        }
    }

}