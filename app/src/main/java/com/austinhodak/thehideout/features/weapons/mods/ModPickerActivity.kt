package com.austinhodak.thehideout.features.weapons.mods

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.repository.ModsRepo
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Mod
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.compose.theme.DarkPrimary
import com.austinhodak.thehideout.ui.theme.HideoutTheme
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.utils.getColor
import com.austinhodak.thehideout.features.weapons.mods.viewmodel.ModPickerViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@ExperimentalCoilApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class ModPickerActivity : GodActivity() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    @Inject
    lateinit var modsRepo: ModsRepo

    private val viewModel: ModPickerViewModel by viewModels()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modList = intent.getStringExtra("ids")?.split(";")
        val conflictingItems = intent.getStringExtra("conflictingItems")?.split(";")
        val type = intent.getStringExtra("type")
        val parent = intent.getStringExtra("parent")
        val id = intent.getStringExtra("id")

        val slot = intent.getSerializableExtra("slot") as Weapon.Slot

        Timber.d(modList.toString())

        if (modList == null) finish()

        setContent {
            HideoutTheme {
                var list by remember { mutableStateOf(emptyList<Mod>()) }

                var sort by remember { mutableStateOf(0) }

                val scaffoldState = rememberScaffoldState()
                val scope = rememberCoroutineScope()

                val systemUiController = rememberSystemUiController()

                val searchKey by viewModel.searchKey.observeAsState("")
                val isSearchOpen by viewModel.isSearchOpen.observeAsState(false)

                LaunchedEffect(key1 = "mod") {
                    scope.launch {
                        modsRepo.getModsByIDs(modList?.toList()!!).first {
                            list = it
                            true
                        }
                    }
                }

                systemUiController.setNavigationBarColor(DarkPrimary)

                var modPickerShowAvailable by remember { mutableStateOf(UserSettingsModel.modPickerShowAvailable.value) }

                UserSettingsModel.modPickerShowAvailable.observe(scope) {
                    modPickerShowAvailable = it
                }

                var data = list.sortedBy {
                    conflictingItems?.contains(it.id) == true
                }.filter {
                    if (searchKey.isBlank()) return@filter true
                    it.ShortName?.contains(searchKey, ignoreCase = true) == true || it.Name?.contains(searchKey, ignoreCase = true) == true
                }.filter {
                    if (modPickerShowAvailable) {
                        it.pricing?.isAbleToPurchase() == true
                    } else {
                        true
                    }
                }

                Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            Column {
                                if (isSearchOpen) {
                                    SearchToolbar(
                                            onClosePressed = {
                                                viewModel.setSearchOpen(false)
                                                viewModel.clearSearch()
                                            },
                                            onValue = {
                                                viewModel.setSearchKey(it)
                                            }
                                    )
                                } else {
                                    AmmoDetailToolbar(
                                            title = "${data.size} Mods",
                                            onBackPressed = { finish() },
                                            actions = {
                                                IconButton(onClick = { viewModel.setSearchOpen(true) }) {
                                                    Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                                                }
                                                IconButton(onClick = {
                                                    val items = listOf("Name", "Price: Low to High", "Price: High to Low", "Recoil", "Ergonomics", "Accuracy")
                                                    MaterialDialog(this@ModPickerActivity).show {
                                                        title(text = "Sort By")
                                                        listItemsSingleChoice(items = items, initialSelection = sort) { _, index, _ ->
                                                            sort = index
                                                        }
                                                        checkBoxPrompt(text = "Only show purchasable mods.", isCheckedDefault = modPickerShowAvailable) {
                                                            scope.launch {
                                                                UserSettingsModel.modPickerShowAvailable.update(it)
                                                            }
                                                        }
                                                    }
                                                }) {
                                                    Icon(painterResource(id = R.drawable.ic_baseline_sort_24), contentDescription = "Sort", tint = Color.White)
                                                }
                                            }
                                    )
                                }
                                /*if (weapon == null) {
                                    LinearProgressIndicator(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(2.dp),
                                        color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                        backgroundColor = Color.Transparent
                                    )
                                }*/
                            }
                        }
                ) {
                    LazyColumn(
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {

                        data = when (sort) {
                            0 -> data.sortedBy { it.ShortName }
                            1 -> data.sortedBy { it.pricing?.lastLowPrice }
                            2 -> data.sortedByDescending { it.pricing?.lastLowPrice }
                            3 -> data.sortedBy { it.Recoil }
                            4 -> data.sortedByDescending { it.Ergonomics }
                            5 -> data.sortedByDescending { it.Accuracy }
                            else -> data.sortedBy { it.ShortName }
                        }

                        items(items = data) { item ->
                            ModsBasicCard(item = item, conflicts = conflictingItems) {
                                setResult(RESULT_OK, Intent().apply {
                                    putExtra("id", id)
                                    putExtra("item", item)
                                    putExtra("type", type)
                                    putExtra("parent", parent)
                                    putExtra("slot", slot)
                                })
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun ModsBasicCard(
            item: Mod,
            conflicts: List<String>?,
            clicked: (item: Mod) -> Unit = {}
    ) {
        val doesConflict = conflicts?.contains(item.id) == true
        Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(vertical = 4.dp),
                border = BorderStroke(
                        1.dp,
                        if (doesConflict) Red400 else if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)
                ),
                elevation = 0.dp,
                onClick = {
                    if (conflicts?.contains(item.id) == true) {
                        Toast.makeText(this, "Mods conflicts with another mod.", Toast.LENGTH_SHORT).show()
                    } else {
                        clicked(item)
                    }
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
                        rememberAsyncImagePainter(item.pricing?.getCleanIcon()),
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
                        when (item.parent) {
                            "5448bc234bdc2d3c308b4569" -> {
                                StatItem(value = item.getMagSize(), title = "MAG", null)
                                StatItem(value = item.Ergonomics, title = "ERG", item.Ergonomics?.getColor(false, MaterialTheme.colors.onSurface))
                                //StatItem(value = item.Ergonomics, title = "LOAD", item.Ergonomics?.getColor(false, MaterialTheme.colors.onSurface))
                            }
                            "55818add4bdc2d5b648b456f",
                            "55818ad54bdc2ddc698b4569",
                            "55818acf4bdc2dde698b456b",
                            "55818ac54bdc2d5b648b456e",
                            "55818ae44bdc2dde698b456c",
                            "55818aeb4bdc2ddc698b456a" -> {
                                StatItem(value = item.Ergonomics, title = "ERG", item.Ergonomics?.getColor(false, MaterialTheme.colors.onSurface))
                                StatItem(value = "${item.SightingRange?.roundToInt()}m", title = "RNG")
                                //StatItem(value = item.SightingRange, title = "MAG")
                            }
                            "550aa4dd4bdc2dc9348b4569",
                            "550aa4cd4bdc2dd8348b456c",
                            "555ef6e44bdc2de9068b457e" -> {
                                StatItem(value = item.Recoil, title = "REC", item.Recoil?.getColor(true, MaterialTheme.colors.onSurface))
                                StatItem(value = item.Ergonomics, title = "ERG", item.Ergonomics?.getColor(false, MaterialTheme.colors.onSurface))
                                StatItem(value = item.Velocity, title = "VEL", item.Velocity?.getColor(false, MaterialTheme.colors.onSurface))
                            }
                            else -> {
                                StatItem(value = item.Recoil, title = "REC", item.Recoil?.getColor(true, MaterialTheme.colors.onSurface))
                                StatItem(value = item.Ergonomics, title = "ERG", item.Ergonomics?.getColor(false, MaterialTheme.colors.onSurface))
                                //StatItem(value = item.Accuracy, title = "ACC", item.Accuracy?.getColor(false, MaterialTheme.colors.onSurface))
                            }
                        }
                    }
                }
            }
        }
    }
}