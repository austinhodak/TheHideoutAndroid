package com.austinhodak.thehideout.features.weapons.builder

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ui.common.EmptyText
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.compose.theme.DividerDark
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.firebase.WeaponBuildFirestore
import com.austinhodak.thehideout.features.pickers.PickerActivity
import com.austinhodak.thehideout.utils.*
import com.austinhodak.thehideout.features.weapons.builder.viewmodel.WeaponLoadoutViewModel

@ExperimentalCoilApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun WeaponLoadoutScreen(loadoutViewModel: WeaponLoadoutViewModel, navViewModel: NavViewModel, tarkovRepo: TarkovRepo) {

    val context = LocalContext.current

    val navController = rememberNavController()
    val scaffoldState = rememberScaffoldState()

    val userLoadouts by loadoutViewModel.userLoadouts.observeAsState()
    val isSearchOpen by loadoutViewModel.isSearchOpen.observeAsState(false)

    val searchKey by loadoutViewModel.searchKey.observeAsState("")

    Scaffold(
        scaffoldState = scaffoldState,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                context.openWeaponPicker(PickerActivity::class.java) {
                    putString("type", "weapons")
                }
            }) {
                Icon(Icons.Filled.Add, contentDescription = "New", tint = Color.Black)
            }
        },
        topBar = {
            if (isSearchOpen) {
                SearchToolbar(
                    onClosePressed = {
                        loadoutViewModel.setSearchOpen(false)
                        loadoutViewModel.clearSearch()
                    },
                    onValue = {
                        loadoutViewModel.setSearchKey(it)
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text("Loadouts", modifier = Modifier.padding(end = 16.dp))
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navViewModel.isDrawerOpen.value = true
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = null, tint = White)
                        }
                    },
                    backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                    elevation = 0.dp,
                    actions = {
                        IconButton(onClick = {
                            loadoutViewModel.setSearchOpen(true)
                        }) {
                            Icon(Icons.Filled.Search, contentDescription = null, tint = White)
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (userLoadouts == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colors.secondary
                )
            }
        } else {
            if (userLoadouts.isNullOrEmpty()) {
                EmptyText(text = "No Loadouts Found.\nCreate one from the weapon details screen!")
                return@Scaffold
            }

            val loadouts = userLoadouts!!.filter {
                it.name?.contains(searchKey, true) == true
                        || it.weapon?.name?.contains(searchKey, true) == true
                        || it.weapon?.shortName?.contains(searchKey, true) == true
            }

            LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                loadouts.forEach {
                    item {
                        LoadoutItem(it, tarkovRepo)
                    }
                }
            }
        }
    }
}

@SuppressLint("CheckResult")
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
private fun LoadoutItem(build: WeaponBuildFirestore, tarkovRepo: TarkovRepo) {
    val context = LocalContext.current
    val weapon by tarkovRepo.getWeaponByID(build.weapon?.id ?: "").collectAsState(initial = null)
    if (weapon == null) return
    Card(
        Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .fillMaxWidth()
            .combinedClickable(onClick = {
                context.openActivity(WeaponBuilderActivity::class.java) {
                    putSerializable("build", build)
                }
            }, onLongClick = {
                MaterialDialog(context).show {
                    listItems(items = listOf("Rename", "Delete")) { dialog, index, text ->
                        when (index) {
                            0 -> {
                                MaterialDialog(context).show {
                                    title(text = "Build Name")
                                    input(prefill = build.name, hint = "Name") { dialog2, text2 ->
                                        build.updateName(text2.toString())
                                    }
                                    positiveButton(text = "Save")
                                }
                            }
                            1 -> {
                                build.delete()
                            }
                        }
                    }
                }
            }),
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
    ) {
        Column(
            Modifier
                .fillMaxSize()

        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    rememberImagePainter(weapon?.pricing?.getCleanIcon()),
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
                            text = "${build.name}",
                            style = MaterialTheme.typography.subtitle1
                        )
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = getCaliberShortName(weapon?.ammoCaliber),
                                style = MaterialTheme.typography.overline
                            )
                        }
                    }
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "${weapon?.ShortName}",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
            Divider(color = DividerDark, modifier = Modifier.padding(bottom = 12.dp))
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                DataRow(
                    title = "WEIGHT",
                    value = Pair("${build.stats?.weight?.round(2)} KG", MaterialTheme.colors.onSurface)
                )
                DataRow(
                    title = "ERGONOMICS",
                    value = Pair("${build.stats?.ergonomics}", MaterialTheme.colors.onSurface)
                )
                DataRow(
                    title = "VERTICAL RECOIL",
                    value = Pair("${build.stats?.verticalRecoil}", MaterialTheme.colors.onSurface)
                )
                DataRow(
                    title = "HORIZONTAL RECOIL",
                    value = Pair("${build.stats?.horizontalRecoil}", MaterialTheme.colors.onSurface)
                )
                DataRow(
                    title = "PRICE",
                    value = Pair("${build.stats?.costRoubles?.asCurrency()}", MaterialTheme.colors.onSurface)
                )
            }
        }
    }
}

@Composable
private fun DataRow(
    title: String,
    value: Pair<Any?, Color?>?
) {
    Row(
        Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.caption
        )
        Text(
            text = value?.first.toString(),
            style = MaterialTheme.typography.subtitle2,
            color = value?.second ?: MaterialTheme.colors.onSurface
        )
    }
}

sealed class BottomNavigationScreens(
    val route: String,
    val resourceId: String,
    val icon: ImageVector? = null,
    @DrawableRes val iconDrawable: Int? = null
) {
    object Overview : BottomNavigationScreens("Overview", "Overview", null, R.drawable.ic_baseline_dashboard_24)
}
