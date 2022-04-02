package com.austinhodak.thehideout.pickers

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.ammunition.AmmoCard
import com.austinhodak.thehideout.calculator.CalculatorHelper
import com.austinhodak.thehideout.calculator.models.Character
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.theme.DarkPrimary
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.gear.GearCard
import com.austinhodak.thehideout.pickers.viewmodels.PickerViewModel
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.weapons.WeaponCard
import com.austinhodak.thehideout.weapons.builder.WeaponBuilderActivity
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import java.io.Serializable
import javax.inject.Inject

@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class PickerActivity : GodActivity() {

    private val pickerViewModel: PickerViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideoutTheme {

                val scope = rememberCoroutineScope()
                val context = LocalContext.current

                var list by remember { mutableStateOf<List<Any>?>(null) }

                val searchKey by pickerViewModel.searchKey.observeAsState("")
                val isSearchOpen by pickerViewModel.isSearchOpen.observeAsState(false)

                val systemUiController = rememberSystemUiController()
                systemUiController.setNavigationBarColor(DarkPrimary)

                LaunchedEffect(scope) {
                    intent.getStringExtra("type")?.let {
                        when (it) {
                            "ammo" -> tarkovRepo.getAllAmmo.collect { ammoList ->
                                list = ammoList.filter { it.ballistics?.damage != 0 }
                            }
                            "armor" -> tarkovRepo.getItemsByTypesArmor(
                                listOf(
                                    ItemTypes.ARMOR,
                                    ItemTypes.RIG
                                )
                            ).collect { armorList ->
                                list = armorList.filter { item ->
                                    item.cArmorClass() > 0
                                }
                            }
                            "helmet" -> tarkovRepo.getItemsByTypesArmor(
                                listOf(
                                    ItemTypes.HELMET
                                )
                            ).collect { armorList ->
                                list = armorList.filter { item ->
                                    item.cArmorClass() > 0
                                }
                            }
                            "character" -> {
                                list = CalculatorHelper.getCharacters(context)
                            }
                            "armorAll" -> tarkovRepo.getItemsByTypesArmor(
                                listOf(
                                    ItemTypes.ARMOR,
                                    ItemTypes.RIG,
                                    ItemTypes.HELMET
                                )
                            ).collect { armorList ->
                                list = armorList.filter { item ->
                                    item.cArmorClass() > 0
                                }
                            }
                            "weapons" -> {
                                tarkovRepo.getAllWeapons().collect {
                                    list = it.filter {
                                        it.pricing != null
                                    }
                                }
                            }
                            else -> {
                                finish()
                            }
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        if (isSearchOpen) {
                            SearchToolbar(
                                onClosePressed = {
                                    pickerViewModel.setSearchOpen(false)
                                    pickerViewModel.clearSearch()
                                },
                                onValue = {
                                    pickerViewModel.setSearchKey(it)
                                }
                            )
                        } else {
                            AmmoDetailToolbar(
                                title = "Choose",
                                onBackPressed = { finish() },
                                actions = {
                                    IconButton(onClick = {
                                        pickerViewModel.setSearchOpen(true)
                                    }) {
                                        Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                                    }
                                }
                            )
                        }
                    }
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        val data = 
                        list?.filter {
                            when (it) {
                                is Ammo -> it.shortName?.contains(searchKey, true) == true || it.name?.contains(searchKey, true) == true
                                is Item -> {
                                    it.ShortName?.contains(searchKey, true) == true
                                            || it.Name?.contains(searchKey, true) == true
                                            || it.armorClass?.contains(searchKey, true) == true
                                }
                                is Character -> it.name.contains(searchKey, true)
                                is Weapon -> {
                                    it.ShortName?.contains(searchKey, true) == true
                                            || it.Name?.contains(searchKey, true) == true
                                }
                                else -> false
                            }
                        }?.sortedBy {
                            when (it) {
                                is Ammo -> it.shortName
                                is Item -> it.ShortName
                                is Character -> null
                                is Weapon -> it.ShortName
                                else -> null
                            }
                        }

                        
                        items(items = data ?: emptyList(), key = {
                            when (it) {
                                is Ammo -> {
                                    it.id
                                }
                                is Item -> {
                                    it.id
                                }
                                is Character -> {
                                    it.name
                                }
                                is Weapon -> {
                                    it.id
                                }
                                else -> ""
                            }
                        }) {
                            when (it) {
                                is Ammo -> {
                                    AmmoCard(ammo = it, Modifier.padding(vertical = 4.dp).animateItemPlacement()) {
                                        selectedItem(it)
                                    }
                                }
                                is Item -> {
                                    GearCard(item = it, Modifier.animateItemPlacement()) {
                                        selectedItem(it)
                                    }
                                }
                                is Character -> {
                                    CharacterCard(character = it, Modifier.animateItemPlacement()) {
                                        selectedItem(it)
                                    }
                                }
                                is Weapon -> {
                                    WeaponCard(weapon = it, Modifier.animateItemPlacement()) { id ->
                                        selectedItem(it)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun CharacterCard(
        character: Character,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(vertical = 4.dp),
            border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
            elevation = 0.dp,
            onClick = onClick
        ) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = character.name, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium, maxLines = 1)
                //ICON
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = character.health.toString(), style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium)
                    Text(text = "HEALTH", style = MaterialTheme.typography.overline)
                }
            }
        }
    }

    @ExperimentalAnimationApi
    private fun selectedItem(item: Serializable) {
        if (item is Weapon && intent.action == "loadoutBuild") {
            this.openActivity(WeaponBuilderActivity::class.java) {
                putSerializable("weapon", item)
            }
            finish()
        } else {
            setResult(RESULT_OK, Intent().apply {
                putExtra("item", item)
            }).also {
                finish()
            }
        }
    }
}