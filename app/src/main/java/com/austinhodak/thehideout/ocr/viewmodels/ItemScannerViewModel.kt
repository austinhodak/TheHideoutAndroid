package com.austinhodak.thehideout.ocr.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.mpp.currentTimeMillis
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.google.mlkit.vision.text.Text
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemScannerViewModel @Inject constructor(
    private val tarkovRepo: TarkovRepo
) : ViewModel() {

    private val _allItems = MutableLiveData<List<Item>>(null)
    val allItems = _allItems

    private val allAmmo = MutableLiveData<List<Ammo>>(null)

    private val _scannedItems = MutableLiveData<Map<Long, Any>>(mutableMapOf())
    val scannedItems = _scannedItems

    private var lastScannedItem: Long = 0

    var isScrolling = false
    var isScanning = MutableLiveData<Boolean>(true)

    fun setScan(boolean: Boolean) {
        isScanning.value = boolean
    }

    fun processText(result: Text) {
        if (lastScannedItem < (currentTimeMillis() - (10 * 1000))) {
            clearAll()
        }

        val found: MutableMap<Long, Any> = mutableMapOf()

        viewModelScope.launch(Dispatchers.IO) {
            for (block in result.textBlocks) {
                for (line in block.lines) {
                    val lineText = line.text
                    _allItems.value?.find {
                        if (lineText.equals("fuel", true)) {
                            if (result.textBlocks.any { it.lines.any { it.text.contains("/100", true) } }) {
                                //Large fuel can
                                return@find it.id == "5d1b36a186f7742523398433"
                            } else {
                                return@find it.id == "5d1b371186f774253763a656"
                            }
                        } else {
                            it.Name.equals(lineText, true) || it.ShortName.equals(lineText, true)
                        }
                    }?.let { item ->
                        if (item.itemType == ItemTypes.AMMO) {
                            allAmmo.value?.find { it.id == item.id }?.let {
                                found[currentTimeMillis()] = it
                            }
                        } else {
                            found[currentTimeMillis()] = item
                        }
                        lastScannedItem = currentTimeMillis()
                    }
                }
            }

            found.forEach { (t, i) ->
                if (_scannedItems.value?.containsValue(i) == false) {
                    viewModelScope.launch(Dispatchers.Main) {
                        _scannedItems.value = _scannedItems.value?.plus(Pair(currentTimeMillis(), i))?.toSortedMap(compareBy { it })
                    }
                }
            }
        }
    }

    fun clearAll() {
        _scannedItems.value = mutableMapOf()
    }

    init {
        viewModelScope.launch {
            tarkovRepo?.getAllItemsOnce()?.let {
                _allItems.value = it
            }
            tarkovRepo?.getAllAmmo?.collect {
                allAmmo.value = it
            }
        }
    }
}