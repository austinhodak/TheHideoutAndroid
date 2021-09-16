package com.austinhodak.thehideout.pickers.viewmodels

import android.content.Context
import com.austinhodak.thehideout.SearchViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class PickerViewModel @Inject constructor(
    @ApplicationContext application: Context
) : SearchViewModel() {

}