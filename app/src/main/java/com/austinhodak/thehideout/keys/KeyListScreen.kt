package com.austinhodak.thehideout.keys

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.openActivity
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.theme.Green500
import com.austinhodak.thehideout.firebase.FSUser
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.fsUser
import com.austinhodak.thehideout.keys.viewmodels.KeysViewModel
import com.skydoves.only.onlyOnce
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@SuppressLint("CheckResult")
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun KeyListScreen(
    navViewModel: NavViewModel,
    tarkovRepo: TarkovRepo,
    keysViewModel: KeysViewModel
) {

    val keys by tarkovRepo.getItemsByType(ItemTypes.KEY).collectAsState(initial = emptyList())
    val isSearchOpen by keysViewModel.isSearchOpen.observeAsState(false)
    val sort by keysViewModel.sortBy.observeAsState()
    val context = LocalContext.current
    val searchKey by keysViewModel.searchKey.observeAsState("")
    val userData by fsUser.observeAsState()

    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            Column {
                if (isSearchOpen) {
                    SearchToolbar(
                        onClosePressed = {
                            keysViewModel.setSearchOpen(false)
                            keysViewModel.clearSearch()
                        },
                        onValue = {
                            keysViewModel.setSearchKey(it)
                        }
                    )
                } else {
                    MainToolbar(
                        title = "Keys",
                        navViewModel = navViewModel,
                        actions = {
                            IconButton(onClick = {
                                keysViewModel.setSearchOpen(true)
                            }) {
                                Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                            }
                            IconButton(onClick = {
                                val items = listOf(
                                    "Name",
                                    "Price: Low to High",
                                    "Price: High to Low"
                                )
                                MaterialDialog(context).show {
                                    title(text = "Sort By")
                                    listItemsSingleChoice(items = items, initialSelection = sort ?: 0) { _, index, _ ->
                                        keysViewModel.setSort(index)
                                    }
                                }
                            }) {
                                Icon(painterResource(id = R.drawable.ic_baseline_sort_24), contentDescription = "Sort Ammo", tint = Color.White)
                            }
                        }
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(
                    backgroundColor = Green500,
                    snackbarData = data
                )
            }
        }
    ) {

        val data = when (sort) {
            0 -> keys.sortedBy { it.Name }
            1 -> keys.sortedBy { it.pricing?.getCheapestBuyRequirements()?.getPriceAsRoubles() }
            2 -> keys.sortedByDescending { it.pricing?.getCheapestBuyRequirements()?.getPriceAsRoubles() }
            else -> keys
        }.filter {
            it.ShortName?.contains(searchKey, ignoreCase = true) == true || it.Name?.contains(searchKey, ignoreCase = true) == true ||
                    if (searchKey.equals("have", true) || searchKey.equals("owned", true)) {
                        userData?.hasKey(it) == true
                    } else {
                        false
                    }
        }.filter { it.pricing != null }

        AnimatedContent(targetState = data.isNullOrEmpty()) {
            if (it) {
                LoadingItem()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(items = data, key = { it.id }) { key ->
                        KeyCard(
                            key,
                            userData,
                            scaffoldState,
                            keysViewModel,
                            Modifier.animateItemPlacement()
                        )
                    }
                }
            }
        }
    }

    onlyOnce("keysTip") {
        onDo {
            scope.launch {
                scaffoldState.snackbarHostState.showSnackbar("Tip: Long press on a key to mark as owned!")
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun KeyCard(
    item: Item,
    userData: FSUser?,
    scaffoldState: ScaffoldState,
    keysViewModel: KeysViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onLongClick = {
                    if (userData?.hasKey(item) == false) {
                        scope.launch {
                            scaffoldState.snackbarHostState.showSnackbar("Key marked as owned.")
                        }
                    }
                    userData?.let { keysViewModel.toggleKey(item, it) }
                },
                onClick = {
                    context.openActivity(FleaItemDetail::class.java) {
                        putString("id", item.id)
                    }
                }
            ),
        border = BorderStroke(1.dp, Color(0xFF313131)),
        elevation = 0.dp
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    rememberImagePainter(item.pricing?.getIcon()),
                    contentDescription = null,
                    modifier = Modifier
                        .width(38.dp)
                        .height(38.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${item.Name}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    SmallBuyPrice(pricing = item.pricing)
                }
                Column(
                    Modifier
                        .align(Alignment.Top)
                        .padding(start = 16.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    if (userData?.hasKey(item) == true) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(id = R.drawable.ic_baseline_check_circle_24),
                            contentDescription = "",
                            tint = Green500
                        )
                    }
                }
            }
        }
    }
}