package com.austinhodak.thehideout.ammunition

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.plusMinus
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.calculator.CalculatorHelper
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.DividerDark
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.pickers.PickerActivity
import com.austinhodak.thehideout.utils.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class AmmoDetailActivity : ComponentActivity() {

    private val ammoViewModel: AmmoViewModel by viewModels()

    private var armorPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                intent?.getSerializableExtra("item")?.let {
                    if (it is Item) {
                        ammoViewModel.selectArmor(it)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ammoID = intent.getStringExtra("ammoID") ?: "5fd20ff893a8961fc660a954"
        ammoViewModel.getAmmo(ammoID)

        setContent {
            val ammo by ammoViewModel.ammoDetails.observeAsState()
            val scaffoldState = rememberScaffoldState()

            val selectedArmor by ammoViewModel.selectedArmor.observeAsState()

            HideoutTheme {
                val systemUiController = rememberSystemUiController()
                systemUiController.setSystemBarsColor(
                    color = MaterialTheme.colors.primary,
                )

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        Column {
                            AmmoDetailToolbar(
                                title = ammo?.pricing?.name ?: "Error Loading...",
                                onBackPressed = { finish() }
                            )
                            if (ammo == null) {
                                LinearProgressIndicator(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(2.dp),
                                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                    backgroundColor = Color.Transparent
                                )
                            }
                        }
                    }
                ) {
                    if (ammo != null) {
                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            item {
                                AmmoDetailCard(ammo = ammo!!)
                            }
                            item {
                                PricingCard(ammo = ammo!!)
                            }
                            item {
                                ArmorPenCard(ammo = ammo!!, selectedArmor)
                            }
                        }
                    }
                }
            }
        }

        //selectedArmor = ArmorHelper.getArmors(this).find { it.name == "Galvion Caiman" }
        //armorSelected()
    }

    private fun armorSelected() {
        /*binding.calcChestTitle.text = "${selectedArmor?.name} • Class ${selectedArmor?.level}"
        binding.calcChestSubtitle.text = selectedArmor?.zones?.joinToString(separator = ", ")
        binding.textView24.text = selectedArmor?.hitpoints.toString()

        binding.ammoDetailArmorPenSeekbar.valueTo = selectedArmor?.hitpoints?.toFloat() ?: 0.0f
        binding.ammoDetailArmorPenSeekbar.value = selectedArmor?.hitpoints?.toFloat() ?: 0.0f*/
    }

    /*private fun updatePenChance(durability: Float? = null) {
        if (ammo != null) {
            val chance = CalculatorHelper.penChance(
                ammo?.toCAmmo()!!,
                selectedArmor?.getCArmor(durability?.toDouble())!!
            )

            Timber.d(chance.toString())

            binding.textView23.text = "${String.format("%.2f", chance)}%"
        }
    }*/

    /*private fun updateData() {
        getCaliberData()
        binding.ammo = ammo

        *//*if (ammo?.prices?.isNotEmpty() == true) {
            SlimAdapter.create().register<AmmoOld.AmmoPriceModel>(R.layout.item_ammo_detail_trader_price) { price, i ->
                i.text(R.id.ammoDetailTraderPriceTitle, price.getTraderString())
                i.text(R.id.ammoDetailTraderPriceTV, price.getPrice())
            }.attachTo(binding.ammoDetailTraderPriceRV).updateData(ammo?.prices)
            binding.ammoDetailTraderPriceRV.layoutManager = LinearLayoutManager(this)
        } else {
            binding.ammoDetailSoldByCard.visibility = View.GONE
        }*//*

        binding.ammoDetailArmorPenSeekbar.addOnChangeListener { slider, value, fromUser ->
            binding.ammoDetailPenChanceCurrentDurability.text = String.format("%.2f", value)
            updatePenChance(value)
        }

        updatePenChance()

        *//*binding.calcChestCard.setOnClickListener {
            launchPicker(CalculatorPickerActivity.ItemType.ARMOR)
        }*//*
    }*/

    /*private fun launchPicker(itemType: CalculatorPickerActivity.ItemType) {
        val intent = Intent(this, CalculatorPickerActivity::class.java)
        intent.putExtra("itemType", itemType)
        resultLauncher.launch(intent)
    }*/

    @Composable
    private fun AmmoDetailCard(
        ammo: Ammo
    ) {
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        rememberImagePainter(ammo.pricing?.gridImageLink),
                        contentDescription = null,
                        modifier = Modifier
                            .width(38.dp)
                            .height(38.dp)
                    )
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = getCaliberName(ammo.Caliber),
                        style = MaterialTheme.typography.subtitle1
                    )
                }
                Divider(color = DividerDark)
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    DataRow(
                        title = "DAMAGE",
                        value = Pair(ammo.ballistics?.damage, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "ARMOR DAMAGE %",
                        value = Pair(ammo.ballistics?.armorDamage, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "PENETRATION",
                        value = Pair(
                            ammo.ballistics?.penetrationPower?.roundToInt(),
                            MaterialTheme.colors.onSurface
                        )
                    )
                    DataRow(
                        title = "RECOIL",
                        value = Pair(
                            ammo.ballistics?.recoil?.plusMinus(),
                            ammo.ballistics?.recoil?.getColor(true, MaterialTheme.colors.onSurface)
                        )
                    )
                    DataRow(
                        title = "ACCURACY",
                        value = Pair(
                            ammo.ballistics?.accuracy?.plusMinus(),
                            ammo.ballistics?.accuracy?.getColor(
                                false,
                                MaterialTheme.colors.onSurface
                            )
                        )
                    )
                    DataRow(
                        title = "PROJECTILE SPEED",
                        value = Pair(
                            "${ammo.ballistics?.initialSpeed} m/s",
                            MaterialTheme.colors.onSurface
                        )
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

    @ExperimentalFoundationApi
    @Composable
    private fun PricingCard(
        ammo: Ammo
    ) {
        val context = LocalContext.current
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .clickable {
                    context.openActivity(FleaItemDetail::class.java) {
                        putString("id", ammo.pricing?.id)
                    }
                },
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "PRICING",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Column {
                    ammo.pricing?.buyFor?.forEach { item ->
                        DataRow(
                            title = "${item.getTitle().toUpperCase(Locale.current)} ", value = Pair(
                                item.price?.asCurrency(if (item.source == "peacekeeper") "D" else "R"),
                                MaterialTheme.colors.onSurface
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ArmorPenCard(
        ammo: Ammo,
        selectedArmor: Item?
    ) {

        val maxDurability = selectedArmor?.MaxDurability?.toFloat() ?: 1f
        var sliderPosition by remember { mutableStateOf(maxDurability) }
        val context = LocalContext.current
        var chance: Double? by remember { mutableStateOf(0.0) }

        if (selectedArmor != null) {
            sliderPosition = maxDurability
        }

        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column() {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "ARMOR PENETRATION CHANCE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    )
                }
                Card(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        Intent(context, PickerActivity::class.java).apply {
                            putExtra("type", "armorAll")
                        }.let {
                            armorPickerLauncher.launch(it)
                        }
                    }
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            Modifier.weight(1f)
                        ) {
                            Text(
                                text = selectedArmor?.Name ?: "Select Armor",
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedArmor?.armorZone?.joinToString(", ") ?: "None",
                                fontWeight = FontWeight.Light,
                                fontSize = 10.sp
                            )
                        }
                        Column {
                            Text(text = selectedArmor?.MaxDurability?.toString() ?: "", style = MaterialTheme.typography.h5)
                        }
                    }
                }
                Divider(color = DividerDark)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "DURABILITY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                        value = sliderPosition,
                        valueRange = 0f..maxDurability,
                        onValueChange = {
                            sliderPosition = it
                            chance = CalculatorHelper.penChance(
                                ammo.toSimAmmo(),
                                selectedArmor?.toSimArmor((maxDurability * sliderPosition).toDouble())
                            )
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colors.secondary,
                            activeTrackColor = MaterialTheme.colors.secondary
                        )
                    )
                    Text(
                        modifier = Modifier.width(40.dp),
                        text = sliderPosition.roundToInt().toString(),
                        style = MaterialTheme.typography.h5,
                        textAlign = TextAlign.End
                    )
                }
                Divider(color = DividerDark)
                Row(
                    Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHANCE TO PENETRATE ARMOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "${chance?.roundToInt()}%", style = MaterialTheme.typography.h5)
                }
            }
        }
    }
}