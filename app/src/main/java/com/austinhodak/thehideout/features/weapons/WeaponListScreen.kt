package com.austinhodak.thehideout.features.weapons

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.*
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.ui.common.EmptyText
import com.austinhodak.thehideout.utils.fadeImagePainterPlaceholder
import com.austinhodak.thehideout.utils.getCaliberShortName
import com.austinhodak.thehideout.ui.legacy.weaponCategories
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun WeaponListScreen(
    classID: String,
    navViewModel: NavViewModel,
    tarkovRepo: TarkovRepo,
    weaponClicked: (weaponID: String) -> Unit
) {
    val allWeapons by tarkovRepo.getAllWeapons().collectAsState(initial = emptyList())
    val data by tarkovRepo.getWeaponsByClass(classID).collectAsState(initial = emptyList())
    val weaponClass = weaponCategories.find { it.third == classID }

    val searchKey by navViewModel.searchKey.observeAsState("")
    val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)

    Scaffold(
        topBar = {
            Column {
                if (isSearchOpen) {
                    SearchToolbar(
                        onClosePressed = {
                            navViewModel.setSearchOpen(false)
                            navViewModel.clearSearch()
                        },
                        onValue = {
                            navViewModel.setSearchKey(it)
                        }
                    )
                } else {
                    MainToolbar(
                        title = stringResource(id = weaponClass?.first ?: R.string.weapons) ?: "",
                        navViewModel = navViewModel
                    ) {
                        IconButton(onClick = { navViewModel.setSearchOpen(true) }) {
                            Icon(Icons.Filled.Search, contentDescription = "Sort Ammo", tint = Color.White)
                        }
                    }
                }
            }
        }
    ) {
        when {
            isSearchOpen -> {
                WeaponSearchBody(searchKey = searchKey, data = allWeapons) {
                    weaponClicked(it)
                }
            }
            else -> {
                AnimatedContent(targetState = data.isNullOrEmpty()) {
                    if (it) {
                        LoadingItem()
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            items(items = data.sortedBy { it.ShortName }, key = { it.id }) { weapon ->
                                WeaponCard(weapon, Modifier.animateItemPlacement()) {
                                    weaponClicked(it)
                                }
                            }
                        }
                    }
                }
            }
        }

    }
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@Composable
fun WeaponSearchBody(
    searchKey: String,
    data: List<Weapon>,
    weaponClicked: (weaponID: String) -> Unit
) {
    val items = data.filter {
        if (searchKey.isBlank()) return@filter false
        it.ShortName?.contains(searchKey, ignoreCase = true) == true
                || it.Name?.contains(searchKey, ignoreCase = true) == true
    }.sortedBy { it.ShortName }.filter {
        it.pricing != null
    }

    if (items.isNullOrEmpty()) {
        EmptyText("Search Weapons")
        return
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
    ) {
        items(items = items, key = { it.id }) { weapon ->
            WeaponCard(weapon = weapon, Modifier.animateItemPlacement()) {
                weaponClicked(it)
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WeaponCard(
    weapon: Weapon,
    modifier: Modifier = Modifier,
    weaponClicked: (weaponID: String) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {
            if (weapon.weapClass == null) {
                Toast.makeText(context, "Details page coming soon.", Toast.LENGTH_SHORT).show()
                return@Card
            }
            weaponClicked(weapon.id)
        }
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
                    fadeImagePainterPlaceholder(weapon.pricing?.getCleanIcon()),
                    contentDescription = null,
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                        .border(0.25.dp, BorderColor)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "${weapon.ShortName}",
                            style = MaterialTheme.typography.subtitle1
                        )
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = getCaliberShortName(weapon.ammoCaliber),
                                style = MaterialTheme.typography.overline
                            )
                        }
                    }
                    SmallBuyPrice(pricing = weapon.pricing)
                }
            }
        }
    }
}