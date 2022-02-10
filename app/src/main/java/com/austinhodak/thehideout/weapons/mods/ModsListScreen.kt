package com.austinhodak.thehideout.weapons.mods

import android.view.ViewGroup
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.openActivity
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.AmmoCard
import com.austinhodak.thehideout.ammunition.AmmoDetailActivity
import com.austinhodak.thehideout.compose.components.EmptyText
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.utils.getColor
import com.austinhodak.thehideout.utils.modParent
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.openModDetail
import com.michaelflisar.materialpreferences.core.initialisation.SettingSetup.context
import com.mikepenz.materialdrawer.model.ExpandableDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.iconRes
import com.mikepenz.materialdrawer.model.interfaces.nameText
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun ModsListScreen(
    tarkovRepo: TarkovRepo,
    navViewModel: NavViewModel
) {
    val scaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf(Triple("bipod", 1001, "Bipod")) }
    //val data = tarkovRepo.getItemsByType(ItemTypes.MOD).collectAsState(initial = emptyList())
    val data by navViewModel.allItems.observeAsState(null)

    val searchKey by navViewModel.searchKey.observeAsState("")
    val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)

    BackdropScaffold(
        scaffoldState = scaffoldState,
        gesturesEnabled = !isSearchOpen,
        headerHeight = 128.dp,
        appBar = {
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
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Mods",
                                color = MaterialTheme.colors.onPrimary,
                                style = MaterialTheme.typography.h6,
                                maxLines = 1,
                                fontSize = 18.sp,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = selectedCategory.third,
                                color = MaterialTheme.colors.onPrimary,
                                style = MaterialTheme.typography.caption,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navViewModel.isDrawerOpen.value = true
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = null)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navViewModel.setSearchOpen(true) }) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = Color.White
                            )
                        }
                    },
                    backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                    elevation = 0.dp
                )
            }
        }, backLayerContent = {
            AndroidView(factory = { context ->
                val drawer = MaterialDrawerSliderView(context)
                val bender = ResourcesCompat.getFont(context, R.font.bender)

                val bipods = SecondaryDrawerItem().apply {
                    tag = "bipod"; level = 1; identifier = 1001; nameText = "Bipods"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val foregrips = SecondaryDrawerItem().apply {
                    tag = "foregrips"; level = 1; identifier = 1002; nameText = "Foregrips"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val flashlights = SecondaryDrawerItem().apply {
                    tag = "flashlights"; level = 1; identifier = 1003; nameText = "Flashlights"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val tac = SecondaryDrawerItem().apply {
                    tag = "tac"; level = 1; identifier = 1004; nameText = "Tactical Combo Devices"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val aux = SecondaryDrawerItem().apply {
                    tag = "aux"; level = 1; identifier = 1005; nameText = "Auxiliary Parts"; typeface = bender; iconRes = R.drawable.ic_blank
                }

                val muzzle_adapters = SecondaryDrawerItem().apply {
                    tag = "muzzle_adapters"; level = 1; identifier = 1006; nameText = "Muzzle Adapters"; typeface = bender; iconRes =
                    R.drawable.ic_blank
                }
                val muzzle_flash = SecondaryDrawerItem().apply {
                    tag = "muzzle_flash"; level = 1; identifier = 1007; nameText = "Flash Hiders"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val muzzle_brakes = SecondaryDrawerItem().apply {
                    tag = "muzzle_brakes"; level = 1; identifier = 1008; nameText = "Flash Brakes"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val muzzle_suppressor = SecondaryDrawerItem().apply {
                    tag = "muzzle_suppressor"; level = 1; identifier = 1009; nameText = "Suppressors"; typeface = bender; iconRes =
                    R.drawable.ic_blank
                }

                val gear_handles = SecondaryDrawerItem().apply {
                    tag = "gear_handles"; level = 1; identifier = 1010; nameText = "Charging Handles"; typeface = bender; iconRes =
                    R.drawable.ic_blank
                }
                val gear_mags = SecondaryDrawerItem().apply {
                    tag = "gear_mags"; level = 1; identifier = 1011; nameText = "Magazines"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val gear_mounts = SecondaryDrawerItem().apply {
                    tag = "gear_mounts"; level = 1; identifier = 1012; nameText = "Mounts"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val gear_stocks = SecondaryDrawerItem().apply {
                    tag = "gear_stocks"; level = 1; identifier = 1013; nameText = "Stocks & Chassis"; typeface = bender; iconRes = R.drawable.ic_blank
                }

                val vital_barrels = SecondaryDrawerItem().apply {
                    tag = "vital_barrels"; level = 1; identifier = 1014; nameText = "Barrels"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val vital_gas = SecondaryDrawerItem().apply {
                    tag = "vital_gas"; level = 1; identifier = 1015; nameText = "Gas Blocks"; typeface = bender; iconRes =
                    R.drawable.ic_blank
                }
                val vital_handguards = SecondaryDrawerItem().apply {
                    tag = "vital_handguards"; level = 1; identifier = 1016; nameText = "Handguards"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val vital_grips = SecondaryDrawerItem().apply {
                    tag = "vital_grips"; level = 1; identifier = 1017; nameText = "Pistol Grips"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val vital_receivers = SecondaryDrawerItem().apply {
                    tag = "vital_receivers"; level = 1; identifier = 1018; nameText = "Receivers & Slides"; typeface = bender; iconRes =
                    R.drawable.ic_blank
                }

                val sights_assault = SecondaryDrawerItem().apply {
                    tag = "sights_assault"; level = 1; identifier = 1019; nameText = "Assault Scopes"; typeface = bender; iconRes =
                    R.drawable.ic_blank
                }
                val sights_reflex = SecondaryDrawerItem().apply {
                    tag = "sights_reflex"; level = 1; identifier = 1020; nameText = "Reflex Sights"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val sights_compact = SecondaryDrawerItem().apply {
                    tag = "sights_compact"; level = 1; identifier = 1021; nameText = "Compact Reflex Sights"; typeface = bender; iconRes =
                    R.drawable.ic_blank
                }
                val sights_iron = SecondaryDrawerItem().apply {
                    tag = "sights_iron"; level = 1; identifier = 1022; nameText = "Iron Sights"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val sights_scopes = SecondaryDrawerItem().apply {
                    tag = "sights_scopes"; level = 1; identifier = 1023; nameText = "Scopes"; typeface = bender; iconRes = R.drawable.ic_blank
                }
                val sights_special = SecondaryDrawerItem().apply {
                    tag = "sights_special"; level = 1; identifier = 1024; nameText = "Special Scopes"; typeface = bender; iconRes =
                    R.drawable.ic_blank
                }

                val items = listOf(
                    bipods,
                    foregrips,
                    flashlights,
                    tac,
                    aux,
                    muzzle_adapters,
                    muzzle_brakes,
                    muzzle_flash,
                    muzzle_suppressor,
                    gear_handles,
                    gear_mags,
                    gear_mounts,
                    gear_stocks,
                    sights_assault,
                    sights_reflex,
                    sights_compact,
                    sights_iron,
                    sights_scopes,
                    sights_special,
                    vital_barrels,
                    vital_gas,
                    vital_grips,
                    vital_handguards,
                    vital_receivers
                )

                drawer.apply {
                    itemAdapter.add(
                        ExpandableDrawerItem().apply {
                            typeface = bender
                            isSelectable = false
                            nameText = "Functional"
                            iconRes = R.drawable.ic_blank
                            isIconTinted = true
                            subItems = mutableListOf(
                                bipods,
                                foregrips,
                                flashlights,
                                tac,
                                aux
                            )
                        },
                        ExpandableDrawerItem().apply {
                            typeface = bender
                            isSelectable = false
                            nameText = "Muzzle"
                            iconRes = R.drawable.ic_blank
                            isIconTinted = true
                            subItems = mutableListOf(
                                muzzle_adapters,
                                muzzle_brakes,
                                muzzle_flash,
                                muzzle_suppressor
                            )
                        },
                        ExpandableDrawerItem().apply {
                            typeface = bender
                            isSelectable = false
                            nameText = "Gear"
                            iconRes = R.drawable.ic_blank
                            isIconTinted = true
                            subItems = mutableListOf(
                                gear_handles,
                                gear_mags,
                                gear_mounts,
                                gear_stocks
                            )
                        },
                        ExpandableDrawerItem().apply {
                            typeface = bender
                            isSelectable = false
                            nameText = "Sights"
                            iconRes = R.drawable.ic_blank
                            isIconTinted = true
                            subItems = mutableListOf(
                                sights_assault,
                                sights_reflex,
                                sights_compact,
                                sights_iron,
                                sights_scopes,
                                sights_special
                            )
                        },
                        ExpandableDrawerItem().apply {
                            typeface = bender
                            isSelectable = false
                            nameText = "Vital Parts"
                            iconRes = R.drawable.ic_blank
                            isIconTinted = true
                            subItems = mutableListOf(
                                vital_barrels,
                                vital_gas,
                                vital_grips,
                                vital_handguards,
                                vital_receivers
                            )
                        }
                    )
                    recyclerView.isVerticalFadingEdgeEnabled = false
                    recyclerView.isVerticalScrollBarEnabled = false
                    expandableExtension.isOnlyOneExpandedItem = true
                    customWidth = ViewGroup.LayoutParams.MATCH_PARENT
                }
                drawer.onDrawerItemClickListener = { _, item, _ ->
                    if (item.isSelectable) {


                        selectedCategory = Triple(item.tag.toString(), item.identifier.toInt(), items.find { it.identifier == item.identifier }?.name?.textString?.toString() ?: "")
                        scope.launch {
                            scaffoldState.conceal()
                        }
                    }
                    true
                }
                drawer
            }, update = {
                it.setSelection(selectedCategory.second.toLong(), false)
            }, modifier = Modifier.wrapContentHeight())
        }, frontLayerContent = {
            if (isSearchOpen) {
                val items = data?.filter { it.pricing != null }?.filter {
                    it.itemType == ItemTypes.MOD
                }?.filter {
                    if (searchKey.isBlank()) return@filter false
                    it.ShortName?.contains(searchKey, ignoreCase = true) == true || it.Name?.contains(searchKey, ignoreCase = true) == true
                }?.sortedBy { it.ShortName }

                Scaffold {
                    if (items.isNullOrEmpty()) {
                        EmptyText("Search Mods")
                        return@Scaffold
                    }

                    LazyColumn(
                        Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        items(items = items, key = { it.id }) { item ->
                            ModsBasicCard(item = item, Modifier.animateItemPlacement()) {
                                context.openModDetail(item.id)
                            }
                        }
                    }
                }
            } else {
                Scaffold(
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            scope.launch {
                                scaffoldState.reveal()
                            }
                        }) {
                            Icon(
                                painterResource(
                                    id = R.drawable.ic_baseline_menu_open_24
                                ),
                                contentDescription = "Localized description",
                                tint = Color.Black
                            )
                        }
                    }
                ) {
                    val items = when {
                        else -> data?.filter { it.pricing != null && it.parent == selectedCategory.first.modParent() }
                            ?.sortedBy { it.ShortName }
                    }
                    if (data.isNullOrEmpty()) {
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
                        Column {
                            //Text(text = "Mod")
                            LazyColumn(
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                items(items = items ?: emptyList(), key = { it.id }) { item ->
                                    ModsBasicCard(item = item, Modifier.animateItemPlacement()) {
                                        context.openModDetail(it.id)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun ModsBasicCard(
    item: Item,
    modifier: Modifier = Modifier,
    clicked: (item: Item) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {
            clicked(item)
        },
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    rememberImagePainter(item.pricing?.getCleanIcon()),
                    contentDescription = null,
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${item.ShortName}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    /*CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Last Price: ${item.getPrice().asCurrency()}",
                            style = MaterialTheme.typography.caption
                        )
                    }*/
                    SmallBuyPrice(pricing = item.pricing)
                }
                Column(
                    Modifier.width(IntrinsicSize.Min),
                ) {
                    StatItem(value = item.Recoil, title = "REC", item.Recoil?.getColor(true, MaterialTheme.colors.onSurface))
                    StatItem(value = item.Ergonomics, title = "ERG", item.Ergonomics?.getColor(false, MaterialTheme.colors.onSurface))
                    StatItem(value = item.Accuracy, title = "ACC", item.Accuracy?.getColor(false, MaterialTheme.colors.onSurface))
                }
            }
        }
    }
}

@Composable
fun StatItem(
    value: Any?,
    title: String,
    color: Color? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = "$value",
            style = MaterialTheme.typography.h6,
            fontSize = 14.sp,
            modifier = Modifier.padding(end = 8.dp),
            color = color ?: MaterialTheme.colors.onSurface,
            textAlign = TextAlign.End
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = title,
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Light,
                fontSize = 10.sp,
                textAlign = TextAlign.End
            )
        }
    }
}