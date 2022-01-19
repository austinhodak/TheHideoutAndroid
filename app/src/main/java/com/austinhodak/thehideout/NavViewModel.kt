package com.austinhodak.thehideout

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.utils.Time
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor(
    private val tarkovRepo: TarkovRepo
) : SearchViewModel() {

    private val _isDrawerOpen = MutableLiveData(false)
    val isDrawerOpen = _isDrawerOpen

    fun setDrawerOpen(isOpen: Boolean) {
        isDrawerOpen.value = isOpen
    }

    private val _currentNavRoute = MutableLiveData<String>(null)
    val currentNavRoute = _currentNavRoute

    fun updateCurrentNavRoute(route: String) {
        _currentNavRoute.value = route
    }

    private val _selectedDrawerItem = MutableLiveData<IDrawerItem<*>>(null)
    val selectedDrawerItem = _selectedDrawerItem

    fun drawerItemSelected(item: IDrawerItem<*>) {
        _selectedDrawerItem.value = item
    }

    private val _selectedDrawerItemIdentifier = MutableLiveData<Pair<Long, String>>(null)
    val selectedDrawerItemIdentifier = _selectedDrawerItemIdentifier

    fun drawerItemSelected(identifier: Pair<Long, String>) {
        _selectedDrawerItemIdentifier.value = identifier
    }

    private val _timeLeft = MutableLiveData("00:00:00")
    val timeLeft: LiveData<String> = _timeLeft

    private val _timeRight = MutableLiveData("00:00:00")
    val timeRight: LiveData<String> = _timeRight

    private suspend fun startGameTimers() {
        withContext(Dispatchers.Main) {
            while (true) {
                _timeLeft.value = Time.realTimeToTarkovTime(true)
                _timeRight.value = Time.realTimeToTarkovTime(false)
                delay(1000.div(7).toLong())
            }
        }
    }

    private val _allItems = MutableLiveData<List<Item>>(null)
    val allItems = _allItems

    init {
        viewModelScope.launch {
            startGameTimers()
        }

        viewModelScope.launch {
            _allItems.value = tarkovRepo.getAllItemsOnce()
        }
    }
}

