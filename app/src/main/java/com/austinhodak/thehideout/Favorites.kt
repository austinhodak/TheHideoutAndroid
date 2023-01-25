package com.austinhodak.thehideout

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.*
import com.michaelflisar.materialpreferences.core.SettingsModel
import com.michaelflisar.materialpreferences.core.interfaces.StorageSetting
import com.michaelflisar.materialpreferences.datastore.DataStoreStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Favorites : SettingsModel(DataStoreStorage(name = "favorites")) {

    val items by stringSetPref(extras.favoriteItems?.toSet() ?: emptySet(), "favoriteItems")
    val barters by intSetPref(emptySet(), "favoriteBarters")
    val crafts by stringSetPref(emptySet(), "favoriteCrafts")

}

fun <T> StorageSetting<Set<T>>.contains(id: T): Boolean {
    return value.contains(id)
}

fun <T> StorageSetting<Set<T>>.add(scope: LifecycleCoroutineScope, id: T) {
    scope.launch(Dispatchers.IO) {
        update(value.plus(id))
    }
}

fun <T> StorageSetting<Set<T>>.remove(scope: LifecycleCoroutineScope, id: T) {
    scope.launch(Dispatchers.IO) {
        update(value.minus(id))
    }
}

@Composable
fun <T> StorageSetting<T>.observeAsState(): State<T?> = observeAsState(value)

@Composable
fun <R, T : R> StorageSetting<T>.observeAsState(initial: R): State<R> {

    val lifecycleOwner = LocalLifecycleOwner.current.lifecycleScope
    val state = remember { mutableStateOf(initial) }
    DisposableEffect(this, lifecycleOwner) {
        val observer = Observer<T> { state.value = it }
        observe(lifecycleOwner) {
            observer.onChanged(it)
        }

        onDispose {  }
    }
    return state
}
