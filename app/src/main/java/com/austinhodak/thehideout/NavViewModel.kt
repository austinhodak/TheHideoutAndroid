package com.austinhodak.thehideout

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.austinhodak.thehideout.utils.Time
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ovh.plrapps.mapcompose.api.enableRotation
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.api.scrollTo
import ovh.plrapps.mapcompose.api.shouldLoopScale
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class NavViewModel @Inject constructor() : ViewModel() {

    private val _isDrawerOpen = MutableLiveData(false)
    val isDrawerOpen = _isDrawerOpen

    fun setDrawerOpen(isOpen: Boolean) {
        isDrawerOpen.value = isOpen
    }

    private val _selectedDrawerItem = MutableLiveData<IDrawerItem<*>>(null)
    val selectedDrawerItem = _selectedDrawerItem

    fun drawerItemSelected(item: IDrawerItem<*>) {
        _selectedDrawerItem.value = item
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
                delay(1000/7)
            }
        }
    }

    init {
        viewModelScope.launch {
            startGameTimers()
        }
    }
}

