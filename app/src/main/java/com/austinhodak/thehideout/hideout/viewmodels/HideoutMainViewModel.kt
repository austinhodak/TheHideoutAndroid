package com.austinhodak.thehideout.hideout.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.hideout.HideoutFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HideoutMainViewModel @Inject constructor(
    private val repository: TarkovRepo
) : ViewModel() {

    private val _view = MutableLiveData(HideoutFilter.CURRENT)
    val view = _view

    fun setView(int: HideoutFilter) {
        _view.value = int
    }
}