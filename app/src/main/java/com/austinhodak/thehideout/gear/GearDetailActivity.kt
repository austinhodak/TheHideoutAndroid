package com.austinhodak.thehideout.gear

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.DataRow
import com.austinhodak.thehideout.ammunition.PricingCard
import com.austinhodak.thehideout.calculator.CalculatorHelper
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.components.OverflowMenu
import com.austinhodak.thehideout.compose.components.WikiItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.gear.viewmodels.GearViewModel
import com.austinhodak.thehideout.pickers.PickerActivity
import com.austinhodak.thehideout.utils.*
import com.bumptech.glide.Glide
import com.stfalcon.imageviewer.StfalconImageViewer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.roundToInt

@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalCoilApi
@AndroidEntryPoint
class GearDetailActivity : ComponentActivity() {

    private val gearViewModel: GearViewModel by viewModels()

    private var ammoPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                intent?.getSerializableExtra("item")?.let {
                    if (it is Ammo) {
                        gearViewModel.selectAmmo(it)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gearID = intent.getStringExtra("id") ?: "5e4abb5086f77406975c9342"
        gearViewModel.getGear(gearID)

        setContent {
            HideoutTheme {
                val scaffoldState = rememberScaffoldState()
                val gear by gearViewModel.gearDetails.observeAsState()

                val selectedAmmo by gearViewModel.selectedAmmo.observeAsState()

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        Column {
                            AmmoDetailToolbar(
                                title = gear?.pricing?.name ?: "Error Loading...",
                                onBackPressed = { finish() },
                                actions = {
                                    OverflowMenu {
                                        gear?.pricing?.wikiLink?.let { WikiItem(url = it) }
                                    }
                                }
                            )
                            if (gear == null) {
                                LinearProgressIndicator(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(2.dp),
                                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                    backgroundColor = Color.Transparent
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        if (gear?.armorClass?.toInt() ?: 0 > 0 && gear?.itemType != ItemTypes.GLASSES) {
                            FloatingActionButton(onClick = {
                                openActivity(CalculatorMainActivity::class.java) {
                                    if (gear?.itemType == ItemTypes.ARMOR || gear?.itemType == ItemTypes.RIG) {
                                        putSerializable("armor", gear)
                                    } else if (gear?.itemType == ItemTypes.HELMET) {
                                        putSerializable("helmet", gear)
                                    }
                                }
                            }) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_calculate_24), contentDescription = "Open Calculator", tint = Color.Black)
                            }
                        }
                    }
                ) {
                    if (gear == null) return@Scaffold

                    LazyColumn(contentPadding = PaddingValues(top = 4.dp, start = 8.dp, end = 8.dp, bottom = 64.dp)) {
                        item {
                            GearInfoCard(item = gear!!)
                        }
                        item {
                            PricingCard(pricing = gear?.pricing!!)
                        }
                        if (gear?.armorClass?.toInt() ?: 0 > 0) {
                            item {
                                ArmorPenCard(gear = gear!!, selectedAmmo)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun GearInfoCard(
        item: Item
    ) {
        val itemType = item.itemType
        val context = LocalContext.current
        val color = when (item.BackgroundColor) {
            "blue" -> itemBlue
            "grey" -> itemGrey
            "red" -> itemRed
            "orange" -> itemOrange
            "default" -> itemDefault
            "violet" -> itemViolet
            "yellow" -> itemYellow
            "green" -> itemGreen
            "black" -> itemBlack
            else -> itemDefault
        }

        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column {

                Row(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        rememberImagePainter(item.pricing?.iconLink ?: ""),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .width(52.dp)
                            .height(52.dp)
                            .border((0.25).dp, color = BorderColor)
                            .clickable {
                                StfalconImageViewer
                                    .Builder(context, listOf(item.pricing?.imageLink)) { view, image ->
                                        Glide
                                            .with(view)
                                            .load(image)
                                            .into(view)
                                    }
                                    .withHiddenStatusBar(false)
                                    .withBackgroundColor(color.toArgb())
                                    .show()
                            }
                    )
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Last Price: ${item.getPrice().asCurrency()}",
                            style = MaterialTheme.typography.subtitle1,
                            fontSize = 16.sp
                        )
                        CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                            Text(
                                text = "${
                                    item.getPricePerSlot().asCurrency()
                                }/slot • ${item.getTotalSlots()} Slots (${item.Width}x${item.Height})",
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Light,
                            )
                        }
                        Row {
                            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                Text(
                                    text = "Last 48h: ",
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Light
                                )
                                Text(
                                    text = "${item.pricing?.changeLast48h}%",
                                    style = MaterialTheme.typography.caption,
                                    color = if (item.pricing?.changeLast48h ?: 0.0 > 0.0) Green500 else if (item.pricing?.changeLast48h ?: 0.0 < 0.0) Red500 else Color.Unspecified,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                    }
                }
                Divider(color = DividerDark)
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    when {
                        itemType == ItemTypes.ARMOR || itemType == ItemTypes.RIG && item.armorClass?.toInt() ?: 0 > 0 -> {
                            DataRow(title = "ARMOR CLASS", value = Pair(item.armorClass, null))
                            DataRow(title = "MATERIAL", value = Pair(item.materialName(), null))
                            DataRow(title = "DURABILITY", value = Pair(item.Durability, null))
                            DataRow(title = "EFFECTIVE DURABILITY", value = Pair((item.Durability?.div(item.destructibility())?.roundToInt()), null))
                            DataRow(title = "ARMOR ZONES", value = Pair(item.armorZone?.joinToString(", ")?.uppercase(), null))
                            Divider(color = DividerDark, modifier = Modifier.padding(vertical = 8.dp))
                            DataRow(title = "WEIGHT", value = Pair("${item.Weight} KG", null))
                            DataRow(title = "SIZE", value = Pair("${item.Width}x${item.Height}", null))
                            DataRow(title = "ERGONOMICS", value = Pair(item.weaponErgonomicPenalty, item.weaponErgonomicPenalty?.asColor()))
                            DataRow(title = "MOVEMENT SPEED", value = Pair("${item.speedPenaltyPercent?.roundToInt()}%", item.speedPenaltyPercent?.asColor()))
                            DataRow(title = "TURN SPEED", value = Pair("${item.mousePenalty?.roundToInt()}%", item.mousePenalty?.asColor()))
                            Divider(color = DividerDark, modifier = Modifier.padding(vertical = 8.dp))
                            DataRow(title = "OUTER SLOTS", value = Pair("${item.getTotalSlots()}", null))
                            if (itemType == ItemTypes.RIG) DataRow(title = "INNER SLOTS", value = Pair("${item.getInternalSlots()}", null))
                            if (itemType == ItemTypes.RIG) DataRow(
                                title = "EFFICIENCY",
                                value = Pair(String.format("%.2f", item.getInternalSlots()?.toDouble()?.div(item.getTotalSlots().toDouble())), null)
                            )
                            DataRow(title = "BLOCKS ARMOR", value = Pair(item.BlocksArmorVest.asBlocks(), null))
                        }
                        itemType == ItemTypes.HELMET && item.armorClass?.toInt() ?: 0 > 0 -> {
                            DataRow(title = "ARMOR CLASS", value = Pair(item.armorClass, null))
                            DataRow(title = "MATERIAL", value = Pair(item.materialName(), null))
                            DataRow(title = "DURABILITY", value = Pair(item.Durability, null))
                            DataRow(title = "EFFECTIVE DURABILITY", value = Pair((item.Durability?.div(item.destructibility())?.roundToInt()), null))
                            DataRow(title = "ARMOR ZONES", value = Pair(item.armorZone?.joinToString(", ")?.uppercase(), null))
                            DataRow(title = "HEAD SEGMENTS", value = Pair(item.headSegments?.joinToString(", ")?.uppercase(), null))
                            Divider(color = DividerDark, modifier = Modifier.padding(vertical = 8.dp))
                            DataRow(title = "WEIGHT", value = Pair("${item.Weight} KG", null))
                            DataRow(title = "SIZE", value = Pair("${item.Width}x${item.Height}", null))
                            DataRow(title = "ERGONOMICS", value = Pair(item.weaponErgonomicPenalty, item.weaponErgonomicPenalty?.asColor()))
                            DataRow(title = "MOVEMENT SPEED", value = Pair("${item.speedPenaltyPercent?.roundToInt()}%", item.speedPenaltyPercent?.asColor()))
                            DataRow(title = "TURN SPEED", value = Pair("${item.mousePenalty?.roundToInt()}%", item.mousePenalty?.asColor()))
                            Divider(color = DividerDark, modifier = Modifier.padding(vertical = 8.dp))
                            DataRow(title = "BLOCKS HEADSET", value = Pair(item.BlocksEarpiece.asBlocks(), null))
                            DataRow(title = "BLOCKS EYEWEAR", value = Pair(item.BlocksEyewear.asBlocks(), null))
                            DataRow(title = "BLOCKS FACE COVER", value = Pair(item.BlocksFaceCover.asBlocks(), null))
                        }
                        itemType == ItemTypes.BACKPACK || itemType == ItemTypes.RIG && item.armorClass?.toInt() ?: 0 == 0 -> {
                            DataRow(title = "WEIGHT", value = Pair("${item.Weight} KG", null))
                            DataRow(title = "SIZE", value = Pair("${item.Width}x${item.Height}", null))
                            DataRow(title = "OUTER SLOTS", value = Pair("${item.getTotalSlots()}", null))
                            DataRow(title = "INNER SLOTS", value = Pair("${item.getInternalSlots()}", null))
                            DataRow(title = "EFFICIENCY", value = Pair(String.format("%.2f", item.getInternalSlots()?.toDouble()?.div(item.getTotalSlots().toDouble())), null))
                        }
                        itemType == ItemTypes.HEADSET -> {
                            DataRow(title = "WEIGHT", value = Pair("${item.Weight} KG", null))
                            DataRow(title = "SIZE", value = Pair("${item.Width}x${item.Height}", null))
                            DataRow(title = "BLOCKS HEADWEAR", value = Pair(item.BlocksHeadwear.asBlocks(), null))
                            DataRow(title = "BLOCKS EYEWEAR", value = Pair(item.BlocksEyewear.asBlocks(), null))
                            DataRow(title = "BLOCKS FACE COVER", value = Pair(item.BlocksFaceCover.asBlocks(), null))
                        }
                        itemType == ItemTypes.GLASSES && item.armorClass?.toInt() ?: 0 == 0 -> {
                            DataRow(title = "WEIGHT", value = Pair("${item.Weight} KG", null))
                            DataRow(title = "SIZE", value = Pair("${item.Width}x${item.Height}", null))
                            DataRow(title = "BLINDNESS PROTECTION", value = Pair("${item.BlindnessProtection?.times(100)?.roundToInt()}%", null))
                        }
                        itemType == ItemTypes.GLASSES && item.armorClass?.toInt() ?: 0 > 0 -> {
                            DataRow(title = "ARMOR CLASS", value = Pair(item.armorClass, null))
                            DataRow(title = "MATERIAL", value = Pair(item.materialName(), null))
                            DataRow(title = "DURABILITY", value = Pair(item.Durability, null))
                            DataRow(title = "EFFECTIVE DURABILITY", value = Pair((item.Durability?.div(item.destructibility())?.roundToInt()), null))
                            DataRow(title = "ARMOR ZONES", value = Pair(item.armorZone?.joinToString(", ")?.uppercase(), null))
                            DataRow(title = "HEAD SEGMENTS", value = Pair(item.headSegments?.joinToString(", ")?.uppercase(), null))
                            Divider(color = DividerDark, modifier = Modifier.padding(vertical = 8.dp))
                            DataRow(title = "WEIGHT", value = Pair("${item.Weight} KG", null))
                            DataRow(title = "SIZE", value = Pair("${item.Width}x${item.Height}", null))
                            DataRow(title = "BLINDNESS PROTECTION", value = Pair("${item.BlindnessProtection?.times(100)?.roundToInt()}%", null))
                        }
                        itemType == ItemTypes.FACECOVER || itemType == ItemTypes.HELMET && item.armorClass?.toInt() ?: 0 == 0 -> {
                            DataRow(title = "WEIGHT", value = Pair("${item.Weight} KG", null))
                            DataRow(title = "SIZE", value = Pair("${item.Width}x${item.Height}", null))
                            DataRow(title = "BLOCKS HEADWEAR", value = Pair(item.BlocksHeadwear.asBlocks(), null))
                            DataRow(title = "BLOCKS HEADSET", value = Pair(item.BlocksHeadwear.asBlocks(), null))
                            DataRow(title = "BLOCKS EYEWEAR", value = Pair(item.BlocksEyewear.asBlocks(), null))
                            DataRow(title = "BLOCKS FACE COVER", value = Pair(item.BlocksFaceCover.asBlocks(), null))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun ArmorPenCard(
        gear: Item,
        selectedAmmo: Ammo?
    ) {

        val maxDurability = gear.MaxDurability?.toFloat() ?: 1f
        var sliderPosition by remember { mutableStateOf(maxDurability) }
        val context = LocalContext.current
        var chance: Double? by remember { mutableStateOf(null) }

        if (selectedAmmo != null) {
            sliderPosition = maxDurability

            /*if (chance == null) {
                chance = CalculatorHelper.penChance(
                    selectedAmmo.toSimAmmo(),
                    gear.toSimArmor((maxDurability * sliderPosition).toDouble())
                )
            }*/
        }

        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "AMMO PENETRATION CHANCE",
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
                            putExtra("type", "ammo")
                        }.let {
                            ammoPickerLauncher.launch(it)
                        }
                    }
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        selectedAmmo?.pricing?.gridImageLink?.let {
                            Image(
                                rememberImagePainter(it),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .width(38.dp)
                                    .height(38.dp)
                                    .border((0.25).dp, color = BorderColor)
                            )
                        }
                        Column(
                            Modifier.weight(1f)
                        ) {
                            Text(
                                text = selectedAmmo?.name ?: "Select Ammo",
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.Bold
                            )
                            selectedAmmo?.ballistics?.let {
                                Text(
                                    text = "Damage: ${it.damage} • Armor Damage: ${it.armorDamage} • Penetration ${it.penetrationPower}",
                                    fontWeight = FontWeight.Light,
                                    fontSize = 10.sp
                                )
                            }
                        }
                        Column {
                            //Text(text = selectedAmmo?.ballistics?.penetrationPower?.toString() ?: "", style = MaterialTheme.typography.h5)
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
                            if (selectedAmmo != null) {
                                sliderPosition = it
                                chance = CalculatorHelper.penChance(
                                    selectedAmmo.toSimAmmo(),
                                    gear.toSimArmor((maxDurability * sliderPosition).toDouble())
                                )
                            }
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
                    Text(text = "${chance?.roundToInt() ?: "-"}%", style = MaterialTheme.typography.h5)
                }
            }
        }
    }
}