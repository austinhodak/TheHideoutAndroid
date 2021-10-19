package com.austinhodak.thehideout.weapons.builder

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.adapty.Adapty
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Mod
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.tarkovapi.type.ItemType
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.WeaponBuild
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.firebase.WeaponBuildFirestore
import com.austinhodak.thehideout.utils.*
import com.austinhodak.thehideout.views.EditorProgress
import com.austinhodak.thehideout.weapons.builder.viewmodel.WeaponBuilderViewModel
import com.austinhodak.thehideout.weapons.mods.ModDetailActivity
import com.austinhodak.thehideout.weapons.mods.ModPickerActivity
import com.austinhodak.thehideout.weapons.mods.StatItem
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.roundToInt

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class WeaponBuilderActivity : AppCompatActivity() {

    val viewModel: WeaponBuilderViewModel by viewModels()
    var loaded = false

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val mod = result.data?.getSerializableExtra("item") as Mod
            val slotID = result.data?.getStringExtra("id")!!
            val type = result.data?.getStringExtra("type")!!
            val parent = result.data?.getStringExtra("parent")!!

            val slot = result.data?.getSerializableExtra("slot") as Weapon.Slot

            Timber.d("RESULT LAUNCHER")
            viewModel.updateMod(mod, slotID)
        }
    }

    @Composable
    fun BuildCostItem(
        pricing: Pricing,
        type: ItemTypes?
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(start = 0.dp, top = 2.dp, bottom = 2.dp)
                    .fillMaxWidth()
                    .clickable {
                        type?.let {
                            if (it == ItemTypes.WEAPON) {
                                openWeaponDetail(pricing.id)
                            } else {
                                openActivity(ModDetailActivity::class.java) {
                                    putString("id", pricing.id)
                                }
                            }
                        }

                        if (type == null) {
                            openActivity(ModDetailActivity::class.java) {
                                putString("id", pricing.id)
                            }
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    rememberImagePainter(
                        pricing.iconLink ?: ""
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .width(38.dp)
                        .height(38.dp)
                        .border((0.25).dp, color = BorderColor)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 8.dp)
                        .weight(1f)
                ) {
                    Text(
                        text = "${pricing.shortName}",
                        style = MaterialTheme.typography.body2,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    /*CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                    Text(
                        text = item.getPrice().asCurrency(),
                        style = MaterialTheme.typography.caption,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                    )
                 }*/
                }
                Spacer(Modifier.weight(1f))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 0.dp)
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = pricing.getCheapestBuyRequirements()?.getPriceAsCurrency() ?: "",
                            style = MaterialTheme.typography.caption,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Image(
                        painter = rememberImagePainter(data = pricing.getCheapestBuyRequirements()?.traderImage(true)),
                        contentDescription = "Trader",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HideoutTheme {

                val systemUiController = rememberSystemUiController()
                val userLoadouts by viewModel.userLoadouts.observeAsState()

                val scope = rememberCoroutineScope()
                val scaffoldState = rememberBottomSheetScaffoldState()

                val intent by remember { mutableStateOf(intent) }

                var build = viewModel.buildState
                //val build by viewModel.buildStateLive.observeAsState()

                intent.getSerializableExtra("weapon")?.let {
                    Timber.d((it as Weapon).toString())
                    //viewModel.setParentWeapon(it as Weapon)
                    viewModel.setParentWeapon(it.id)
                }

                intent.getSerializableExtra("build")?.let {
                    Timber.d("LOAD BUILD INTENT")
                    it as WeaponBuildFirestore
                    if (!loaded)
                        viewModel.loadBuild(it)
                }

                val savedBuild = if (intent.getSerializableExtra("build") is WeaponBuildFirestore) intent.getSerializableExtra("build") as WeaponBuildFirestore else null

                val parentWeapon = build?.parentWeapon

                Timber.d("SCAFFOLD")

                systemUiController.setNavigationBarColor(DarkPrimary)

                BottomSheetScaffold(
                    scaffoldState = scaffoldState,
                    sheetContent = {
                        var totalPriceOpen by remember { mutableStateOf(false) }

                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp, top = 4.dp)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 0.dp, bottomStart = 0.dp))
                                    .background(Color(0xFF282828))
                                    .combinedClickable(
                                        onClick = {
                                            if (scaffoldState.bottomSheetState.isCollapsed) {
                                                scope.launch {
                                                    scaffoldState.bottomSheetState.expand()
                                                }
                                            } else {
                                                scope.launch {
                                                    scaffoldState.bottomSheetState.collapse()
                                                }
                                            }
                                        },
                                        onLongClick = {
                                            MaterialDialog(this@WeaponBuilderActivity).show {
                                                title(text = "Add to Shopping Cart?")
                                                message(text = "This will add these items to the shopping cart on the Flea Market screen.")
                                                positiveButton(text = "ADD") {
                                                    build?.parentWeapon?.pricing?.addToCart()
                                                    build?.mods?.values?.forEach { mod ->
                                                        val pricing = mod.mod?.pricing
                                                        pricing?.addToCart()
                                                    }

                                                    /*module.require
                                                        ?.filter { it?.type == "item" }
                                                        ?.forEach {
                                                            val itemID = it?.name
                                                            val quantity = it?.quantity ?: 0
                                                            if (quantity > 500) return@forEach
                                                            userRefTracker("items/$itemID/hideoutObjective/${it?.id?.addQuotes()}").setValue(quantity)
                                                        }*/
                                                }
                                                negativeButton(text = "CANCEL")
                                            }
                                        }
                                    )
                                    .padding(bottom = 0.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier,
                                ) {
                                    Text(
                                        "Total Build Cost: ~${build?.totalCostFleaMarket()?.asCurrency()}",
                                        style = MaterialTheme.typography.subtitle1,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(start = 16.dp)
                                    )
                                    Spacer(Modifier.weight(1f))
                                    Icon(
                                        painter = if (totalPriceOpen) painterResource(id = R.drawable.ic_baseline_keyboard_arrow_up_24) else painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
                                        contentDescription = "",
                                        modifier = Modifier.clickable {
                                            totalPriceOpen = !totalPriceOpen
                                        }.padding(end = 16.dp, top = 16.dp, start = 16.dp, bottom = 16.dp)
                                    )
                                }
                                AnimatedVisibility(visible = totalPriceOpen) {
                                    Column(
                                        Modifier.padding(top = 0.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                                    ) {
                                        build?.parentWeapon?.pricing?.let {
                                            BuildCostItem(pricing = it, ItemTypes.WEAPON)
                                        }
                                        build?.mods?.values?.sortedBy {
                                            it.mod?.pricing?.getCheapestBuyRequirements()?.source
                                        }?.forEach { mod ->
                                            val pricing = mod.mod?.pricing
                                            pricing?.let {
                                                BuildCostItem(pricing = pricing, mod.mod?.itemType)
                                            }
                                        }
                                    }
                                }
                            }
                            //Text("WEIGHT: ${build?.totalWeight()} KG")
                            AndroidView(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp), factory = {
                                EditorProgress(it)
                            }, update = {
                                it.apply {
                                    pg.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.editor_stat, null)
                                    icon.setImageResource(R.drawable.icons8_weight_kg_24)

                                    nameTV.text = "WEIGHT"
                                    valueTV.text = "${build?.totalWeight()?.round(2)} KG"
                                    pg.max = 50
                                    pg.progress = build?.totalWeight()?.roundToInt() ?: 0
                                }
                            })
                            AndroidView(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp), factory = {
                                EditorProgress(it)
                            }, update = {
                                it.apply {
                                    pg.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.editor_stat, null)
                                    icon.setImageResource(R.drawable.ic_baseline_fitness_center_24)

                                    nameTV.text = "ERGONOMICS"
                                    valueTV.text = build?.totalErgo().toString()
                                    pg.max = 100
                                    pg.progress = build?.totalErgo() ?: 0
                                }
                            })
                            AndroidView(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp), factory = {
                                EditorProgress(it)
                            }, update = {
                                it.apply {
                                    pg.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.editor_stat_blue, null)

                                    icon.setImageResource(R.drawable.ic_baseline_arrow_upward_24)

                                    nameTV.text = "VERTICAL RECOIL"
                                    valueTV.text = build?.totalVerticalRecoil().toString()
                                    pg.max = 1000
                                    pg.progress = build?.totalVerticalRecoil() ?: 0

                                    pg.secondaryProgress = build?.parentWeapon?.RecoilForceUp ?: 0
                                }
                            })
                            AndroidView(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp), factory = {
                                EditorProgress(it)
                            }, update = {
                                it.apply {
                                    pg.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.editor_stat_blue, null)

                                    icon.setImageResource(R.drawable.ic_baseline_arrow_back_24)

                                    nameTV.text = "HORIZONTAL RECOIL"
                                    valueTV.text = build?.totalHorizontalRecoil().toString()
                                    pg.max = 1000
                                    pg.progress = build?.totalHorizontalRecoil() ?: 0

                                    pg.secondaryProgress = build?.parentWeapon?.RecoilForceBack ?: 0
                                }
                            })
                            AndroidView(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp), factory = {
                                EditorProgress(it)
                            }, update = {
                                it.apply {

                                    if (build?.totalVelocity() ?: 0 > build?.ammo?.ballistics?.initialSpeed ?: 0) {
                                        pg.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.editor_stat_blue, null)

                                        pg.secondaryProgress = build?.totalVelocity() ?: 0
                                        pg.progress = build?.ammo?.ballistics?.initialSpeed ?: 0
                                    } else if (build?.totalVelocity() ?: 0 < build?.ammo?.ballistics?.initialSpeed ?: 0) {
                                        pg.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.editor_stat_red, null)

                                        pg.progress = build?.totalVelocity() ?: 0
                                        pg.secondaryProgress = build?.ammo?.ballistics?.initialSpeed ?: 0
                                    } else {
                                        pg.progressDrawable = ResourcesCompat.getDrawable(resources, R.drawable.editor_stat, null)

                                        pg.progress = build?.totalVelocity() ?: 0
                                        pg.secondaryProgress = build?.ammo?.ballistics?.initialSpeed ?: 0
                                    }


                                    icon.setImageResource(R.drawable.icons8_ammo_100)

                                    nameTV.text = "MUZZLE VELOCITY"
                                    valueTV.text = "${build?.totalVelocity()} m/s"
                                    pg.max = 1500
                                }
                            })

                            Row(Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = {

                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(top = 4.dp, end = 4.dp),
                                    enabled = false,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        backgroundColor = Color.Transparent,
                                        contentColor = Color.Gray
                                    )
                                ) {
                                    Text(text = "MAKE PUBLIC", color = Red400)
                                }
                                OutlinedButton(
                                    onClick = {
                                        if (userLoadouts?.size ?: 0 >= 5 && intent.getSerializableExtra("build") == null) {
                                            isPremium {
                                                if (it) {
                                                    viewModel.saveBuild {
                                                        Toast.makeText(this@WeaponBuilderActivity, "Saved!", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Adapty.getPaywalls { paywalls, products, error ->
                                                        val premium = products?.find { it.skuDetails?.sku == "premium_1" }?.let {
                                                            it.purchase(this@WeaponBuilderActivity) { purchaserInfo, purchaseToken, googleValidationResult, product, error ->
                                                                if (error != null) {
                                                                    Toast.makeText(this@WeaponBuilderActivity, "Error upgrading.", Toast.LENGTH_SHORT).show()
                                                                }
                                                                isPremium {
                                                                    if (it) {
                                                                        viewModel.saveBuild {
                                                                            Toast.makeText(this@WeaponBuilderActivity, "Saved!", Toast.LENGTH_SHORT).show()
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            //Toast.makeText(this@WeaponBuilderActivity, "Limited to 5 loadouts during beta.", Toast.LENGTH_SHORT).show()
                                        } else {
                                            viewModel.saveBuild {
                                                Toast.makeText(this@WeaponBuilderActivity, "Saved!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(top = 4.dp, start = 4.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        backgroundColor = Color.Transparent,
                                        contentColor = Color.Gray
                                    )
                                ) {
                                    Text(text = "SAVE", color = White)
                                }
                            }
                        }
                    },
                    snackbarHost = {
                        SnackbarHost(it) { data ->
                            Snackbar(
                                snackbarData = data
                            )
                        }
                    },
                    sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    sheetBackgroundColor = DarkPrimary,
                    sheetPeekHeight = 62.dp,
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = if (savedBuild == null) parentWeapon?.Name ?: "" else savedBuild.name ?: "",
                                        color = MaterialTheme.colors.onPrimary,
                                        style = MaterialTheme.typography.h6,
                                        maxLines = 1,
                                        fontSize = 18.sp,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "(${parentWeapon?.ShortName ?: ""})",
                                        color = MaterialTheme.colors.onPrimary,
                                        style = MaterialTheme.typography.caption,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            backgroundColor = if (isSystemInDarkTheme()) DarkPrimary else MaterialTheme.colors.primary,
                            modifier = Modifier.statusBarsPadding(),
                            navigationIcon = {
                                IconButton(onClick = {
                                    onBackPressed()
                                }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            actions = {
                                /* OverflowMenu {
                                     OverflowMenuItem(text = "Save") {
                                         FirebaseFirestore.getInstance().collection("loadouts").add(build?.toFirestore()!!).addOnFailureListener {
                                             Timber.e(it)
                                         }
                                     }
                                     OverflowMenuItem(text = "Reset") {
                                         viewModel.resetBuild()
                                     }
                                 }*/
                            }
                        )
                    }
                ) {
                    if (build == null) return@BottomSheetScaffold
                    LazyColumn(contentPadding = PaddingValues(top = 4.dp, bottom = 66.dp)) {
                        parentWeapon?.Slots?.sortedBy { it._name }?.forEach {
                            if (it.getSubIds()?.count() == 1 && it._required == true && intent.getSerializableExtra("build") == null) {
                                //viewModel.updateMod(it.getSubIds()?.first(), it._id, it._parent)
                            }
                            item {
                                ModSlot(slot = it, build = build)
                                //ModSlot2(slot = it, build = build)
                            }
                        }
                    }
                }
            }
        }
    }

    @ExperimentalFoundationApi
    @Composable
    fun ModSlot2(slot: Weapon.Slot, build: WeaponBuild?) {
        val mod = slot.mod

        Card(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .fillMaxWidth()
                .combinedClickable(onClick = {
                    val intent = Intent(this, ModPickerActivity::class.java)
                    intent.putExtra(
                        "ids", slot
                            .getSubIds()
                            ?.joinToString(";")
                    )
                    intent.putExtra("type", slot._name)
                    intent.putExtra("parent", slot._parent)
                    intent.putExtra("id", slot._id)
                    intent.putExtra(
                        "conflictingItems", build
                            ?.allConflictingItems()
                            ?.joinToString(";")
                    )

                    intent.putExtra("slot", slot)
                    resultLauncher.launch(intent)
                }, onLongClick = {
                    mod?.let {
                        //viewModel.removeMod(it.mod, slot._id, slot._parent)
                    }
                }),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
            border = if (slot._required == true) BorderStroke(0.25.dp, Red400) else BorderStroke(0.25.dp, BorderColor),
            elevation = 0.dp,
        ) {
            Column {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = slot.getName(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Row(
                        Modifier.padding(start = 16.dp, bottom = 8.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (mod != null) {
                            Image(
                                rememberImagePainter(mod.pricing?.iconLink),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .width(40.dp)
                                    .height(40.dp)
                                    .border(0.25.dp, BorderColor)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = mod?.ShortName ?: "None",
                                style = MaterialTheme.typography.subtitle1
                            )
                            SmallBuyPrice(mod?.pricing)
                            /*CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Text(
                                    text = "Last Price: ${mod?.getPrice()?.asCurrency() ?: "-"}",
                                    style = MaterialTheme.typography.caption
                                )
                            }*/
                        }
                        mod?.let {
                            Column(
                                Modifier.width(IntrinsicSize.Min),
                            ) {
                                StatItem(value = mod.Recoil, title = "REC", mod.Recoil?.getColor(true, MaterialTheme.colors.onSurface))
                                StatItem(value = mod.Ergonomics, title = "ERG", mod.Ergonomics?.getColor(false, MaterialTheme.colors.onSurface))
                                StatItem(value = mod.Accuracy, title = "ACC", mod.Accuracy?.getColor(false, MaterialTheme.colors.onSurface))
                            }
                        }
                    }

                    mod?.Slots?.sortedBy { it._name }?.forEach {
                        if (it.getSubIds()?.isNullOrEmpty() == true) {
                            return@forEach
                        }
                        if (it.getSubIds()?.count() == 1 && it._required == true) {
                            viewModel.updateMod(it.getSubIds()?.first(), it._id, it._parent)
                        }
                        ModSlot2(slot = it, build = build)
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    @ExperimentalFoundationApi
    @Composable
    fun ModSlot(slot: Weapon.Slot, build: WeaponBuild?) {
        val buildSlot = build?.mods?.get(slot._id)

        Card(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .fillMaxWidth()
                .combinedClickable(onClick = {
                    val intent = Intent(this, ModPickerActivity::class.java)
                    intent.putExtra(
                        "ids", slot
                            .getSubIds()
                            ?.joinToString(";")
                    )
                    intent.putExtra("type", slot._name)
                    intent.putExtra("parent", slot._parent)
                    intent.putExtra("id", slot._id)
                    intent.putExtra(
                        "conflictingItems", build
                            ?.allConflictingItems()
                            ?.joinToString(";")
                    )
                    intent.putExtra("slot", slot)
                    resultLauncher.launch(intent)
                }, onLongClick = {
                    buildSlot?.let {
                        MaterialDialog(this).show {
                            listItems(items = listOf("View Mod Details", "Remove")) { dialog, index, text ->
                                when (index) {
                                    0 -> {
                                        it.mod?.id?.let { it1 -> openModDetail(it1) }
                                    }
                                    1 -> {
                                        loaded = true
                                        viewModel.removeMod(it.mod, slot._id, slot._parent, slot)
                                    }
                                }
                            }
                        }
                    }
                }),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
            border = if (slot._required == true) BorderStroke(0.25.dp, Red400) else BorderStroke(0.25.dp, BorderColor),
            elevation = 0.dp,
        ) {
            Column {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = slot.getName(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                Column(modifier = Modifier.padding(bottom = 4.dp)) {
                    Row(
                        Modifier.padding(start = 16.dp, bottom = 8.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (buildSlot?.mod != null) {
                            Image(
                                rememberImagePainter(buildSlot.mod?.pricing?.iconLink),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .width(40.dp)
                                    .height(40.dp)
                                    .border(0.25.dp, BorderColor)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${buildSlot?.mod?.Name ?: "None"}",
                                style = MaterialTheme.typography.subtitle1,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            buildSlot?.mod?.pricing?.let {
                                SmallBuyPrice(buildSlot?.mod?.pricing)
                            }
                            /*CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Text(
                                    text = "Last Price: ${buildSlot?.mod?.getPrice()?.asCurrency() ?: "-"}",
                                    style = MaterialTheme.typography.caption
                                )
                            }*/
                        }
                        buildSlot?.mod?.let {
                            val item = it
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

                    buildSlot?.mod?.Slots?.sortedBy { it._name }?.forEach {
                        if (it.getSubIds()?.isNullOrEmpty() == true) {
                            return@forEach
                        }
                        if (it.getSubIds()?.count() == 1 && it._required == true && intent.getSerializableExtra("build") == null) {
                            //viewModel.updateMod(it.getSubIds()?.first(), it._id, it._parent)
                        }
                        ModSlot(slot = it, build = build)
                    }
                }
            }
        }
    }
}