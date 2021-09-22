package com.austinhodak.thehideout.weapons.builder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.repository.ModsRepo
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Mod
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.WeaponBuild
import com.austinhodak.thehideout.compose.components.OverflowMenu
import com.austinhodak.thehideout.compose.components.OverflowMenuItem
import com.austinhodak.thehideout.compose.components.WikiItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.utils.getColor
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.austinhodak.thehideout.views.EditorProgress
import com.austinhodak.thehideout.weapons.builder.viewmodel.WeaponBuilderViewModel
import com.austinhodak.thehideout.weapons.mods.ModPickerActivity
import com.austinhodak.thehideout.weapons.mods.StatItem
import com.google.accompanist.insets.statusBarsPadding
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class WeaponBuilderActivity : AppCompatActivity() {

    val viewModel: WeaponBuilderViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    @Inject
    lateinit var modRepo: ModsRepo

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val mod = result.data?.getSerializableExtra("item") as Mod
            val slotID = result.data?.getStringExtra("id")!!
            val type = result.data?.getStringExtra("type")!!
            val parent = result.data?.getStringExtra("parent")!!

            viewModel.updateMod(mod, slotID, parent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideoutTheme {
                val scope = rememberCoroutineScope()
                val scaffoldState = rememberBottomSheetScaffoldState()

                val build = viewModel.buildState
                viewModel.setParentWeapon("5bb2475ed4351e00853264e3")

                val parentWeapon = build?.parentWeapon

                BottomSheetScaffold(
                        sheetContent = {
                            var totalPriceOpen by remember { mutableStateOf(false) }

                            Column(
                                    Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                Column(
                                        Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 4.dp, top = 4.dp)
                                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 0.dp, bottomStart = 0.dp))
                                                .background(Color(0xFF282828))
                                                .clickable {
                                                    /*totalPriceOpen = !totalPriceOpen
                                            if (scaffoldState.bottomSheetState.isCollapsed) {
                                                scope.launch {
                                                    scaffoldState.bottomSheetState.expand()
                                                }
                                            }*/
                                                }
                                                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
                                ) {
                                    Text("Total Build Cost: ${build?.totalCostFleaMarket()?.asCurrency()}", style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium)
                                    /*Row (
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier,
                                    ) {

                                       *//* Icon(
                                        painter = if (totalPriceOpen) painterResource(id = R.drawable.ic_baseline_keyboard_arrow_up_24) else painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
                                        contentDescription = ""
                                    )*//*
                                }*/
                                    /*AnimatedVisibility(visible = totalPriceOpen) {
                                        Column {
                                            build?.mods?.forEach {

                                            }
                                        }
                                    }*/
                                }
                                //Text("WEIGHT: ${build?.totalWeight()} KG")
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

                                        icon.setImageResource(R.drawable.ic_baseline_arrow_back_24)

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

                                Row {
                                    OutlinedButton(onClick = {

                                    }) {

                                    }
                                }
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
                                                    text = parentWeapon?.Name ?: "Loading",
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
                                        OverflowMenu {
                                            OverflowMenuItem(text = "Save") {
                                                FirebaseFirestore.getInstance().collection("loadouts").add(build?.toFirestore()!!).addOnFailureListener {
                                                    Timber.e(it)
                                                }
                                            }
                                            OverflowMenuItem(text = "Reset") {
                                                viewModel.resetBuild()
                                            }
                                        }
                                    }
                            )
                        }
                ) {
                    LazyColumn(
                            contentPadding = PaddingValues(top = 4.dp, bottom = 66.dp)) {
                        parentWeapon?.Slots?.sortedBy { it._name }?.forEach {
                            if (it.getSubIds()?.count() == 1 && it._required == true) {
                                viewModel.updateMod(it.getSubIds()?.first(), it._id, it._parent)
                            }
                            item {
                                ModSlot(slot = it, build = build)
                            }
                        }
                    }
                }
            }
        }
    }

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
                            intent.putExtra("ids", slot
                                    .getSubIds()
                                    ?.joinToString(";"))
                            intent.putExtra("type", slot._name)
                            intent.putExtra("parent", slot._parent)
                            intent.putExtra("id", slot._id)
                            intent.putExtra("conflictingItems", build
                                    ?.allConflictingItems()
                                    ?.joinToString(";"))
                            resultLauncher.launch(intent)
                        }, onLongClick = {
                            buildSlot?.let {
                                viewModel.removeMod(it.mod, slot._id, slot._parent)
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
                                    text = "${buildSlot?.mod?.ShortName ?: "None"}",
                                    style = MaterialTheme.typography.subtitle1
                            )
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Text(
                                        text = "Last Price: ${buildSlot?.mod?.getPrice()?.asCurrency() ?: "-"}",
                                        style = MaterialTheme.typography.caption
                                )
                            }
                        }
                        buildSlot?.mod?.let {
                            Column(
                                    Modifier.width(IntrinsicSize.Min),
                            ) {
                                StatItem(value = buildSlot?.mod?.Recoil, title = "REC", buildSlot?.mod?.Recoil?.getColor(true, MaterialTheme.colors.onSurface))
                                StatItem(value = buildSlot?.mod?.Ergonomics, title = "ERG", buildSlot?.mod?.Ergonomics?.getColor(false, MaterialTheme.colors.onSurface))
                                StatItem(value = buildSlot?.mod?.Accuracy, title = "ACC", buildSlot?.mod?.Accuracy?.getColor(false, MaterialTheme.colors.onSurface))
                            }
                        }
                    }

                    buildSlot?.mod?.Slots?.sortedBy { it._name }?.forEach {
                        if (it.getSubIds()?.isNullOrEmpty() == true) {
                            return@forEach
                        }
                        if (it.getSubIds()?.count() == 1 && it._required == true) {
                            viewModel.updateMod(it.getSubIds()?.first(), it._id, it._parent)
                        }
                        ModSlot(slot = it, build = build)
                    }
                }
            }
        }
    }
}