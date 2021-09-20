package com.austinhodak.thehideout.weapons.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.WeaponBuild
import com.austinhodak.thehideout.compose.components.OverflowMenu
import com.austinhodak.thehideout.compose.components.WikiItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.utils.getCaliberName
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.weapons.mods.ModPickerActivity
import com.austinhodak.thehideout.weapons.viewmodel.WeaponDetailViewModel
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.roundToInt

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class WeaponDetailActivity : GodActivity() {

    private val weaponViewModel: WeaponDetailViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val item = result.data?.getSerializableExtra("item") as Item
            val slotID = result.data?.getStringExtra("id")!!
            val type = result.data?.getStringExtra("type")!!

            val build = weaponViewModel.weaponBuild.value ?: WeaponBuild()

            build.mods?.put(
                slotID,
                WeaponBuild.BuildMod(
                    item = item,
                    id = type
                )
            )

            weaponViewModel.updateBuild(build.apply {
                id = "TEST"
            })
            //Timber.d(build.toString())
            Timber.d(weaponViewModel.weaponBuild.value.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val weaponID = intent.getStringExtra("weaponID") ?: "5bb2475ed4351e00853264e3"
        weaponViewModel.getWeapon(weaponID)

        setContent {
            HideoutTheme {
                ProvideWindowInsets {
                    val weapon by weaponViewModel.weaponDetails.observeAsState()
                    val scaffoldState = rememberScaffoldState()
                    val systemUiController = rememberSystemUiController()

                    systemUiController.setStatusBarColor(
                        Color.Transparent,
                        darkIcons = false
                    )

                    val defaultAmmo by tarkovRepo.getAmmoByID(weapon?.defAmmo ?: "").collectAsState(initial = null)

                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            Column {
                                Box {
                                    val painter = rememberImagePainter(
                                        weapon?.getTarkovMarketImageURL(),
                                        builder = {
                                            crossfade(true)
                                        }
                                    )
                                    Column {
                                        Image(
                                            painter,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .then(
                                                    if (painter.state is ImagePainter.State.Loading || painter.state is ImagePainter.State.Error) {
                                                        Modifier.height(0.dp)
                                                    } else {
                                                        (painter.state as? ImagePainter.State.Success)
                                                            ?.painter
                                                            ?.intrinsicSize
                                                            ?.let { intrinsicSize ->
                                                                Modifier.aspectRatio(intrinsicSize.width / intrinsicSize.height)
                                                            } ?: Modifier
                                                    }
                                                ),
                                            contentScale = ContentScale.FillWidth
                                        )
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .offset(y = (-4).dp)
                                                .background(if (painter.state is ImagePainter.State.Success) Color.Black else DarkPrimary)
                                                .padding(
                                                    start = 72.dp,
                                                    bottom = 16.dp,
                                                    top = if (painter.state is ImagePainter.State.Success) 0.dp else 56.dp
                                                )
                                        ) {
                                            Text(
                                                text = weapon?.Name ?: "Loading...",
                                                color = MaterialTheme.colors.onPrimary,
                                                style = MaterialTheme.typography.h6,
                                                maxLines = 1,
                                                fontSize = 18.sp,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "(${weapon?.ShortName})",
                                                color = MaterialTheme.colors.onPrimary,
                                                style = MaterialTheme.typography.caption,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    TopAppBar(
                                        title = { Spacer(modifier = Modifier.fillMaxWidth()) },
                                        backgroundColor = Color.Transparent,
                                        modifier = Modifier.statusBarsPadding(),
                                        navigationIcon = {
                                            IconButton(onClick = {
                                                onBackPressed()
                                            }) {
                                                Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                            }
                                        },
                                        elevation = 0.dp,
                                        actions = {
                                            OverflowMenu {
                                                weapon?.pricing?.wikiLink?.let { WikiItem(url = it) }
                                            }
                                        }
                                    )
                                }

                                if (weapon == null) {
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
                        LazyColumn(
                            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 4.dp)
                        ) {
                            item {
                                weapon?.let { WeaponDetailCard(weapon = it) }
                            }
                            item {
                                if (weapon != null && defaultAmmo != null) {
                                    AmmoCard(weapon = weapon!!, defaultAmmo = defaultAmmo!!)
                                }
                            }
                            item {
                                weapon?.pricing?.let {
                                    PricingCard(pricing = it)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @ExperimentalAnimationApi
    @Composable
    private fun WeaponDetailCard(
        weapon: Weapon,
    ) {
        var visible by remember {
            mutableStateOf(false)
        }
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .clickable { visible = !visible },
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column {
                Row(
                    Modifier.padding(0.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                }
                Divider(color = DividerDark)
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    DataRow(
                        title = "RECOIL VERTICAL",
                        value = Pair(weapon.RecoilForceUp, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "RECOIL HORIZONTAL",
                        value = Pair(weapon.RecoilForceBack, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "EFFECTIVE DISTANCE",
                        value = Pair(weapon.bEffDist?.roundToInt(), MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "ERGONOMICS",
                        value = Pair(weapon.Ergonomics?.roundToInt(), MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "RATE OF FIRE",
                        value = Pair(weapon.bFirerate?.roundToInt(), MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "SIGHTING RANGE",
                        value = Pair("${weapon.IronSightRange?.roundToInt()}m", MaterialTheme.colors.onSurface)
                    )
                    AnimatedVisibility(visible = visible) {
                        Column(
                            Modifier.padding(top = 4.dp, bottom = 0.dp)
                        ) {
                            Divider(color = DividerDark, modifier = Modifier.padding(bottom = 4.dp))
                            DataRow(
                                title = "DURABILITY",
                                value = Pair(weapon.Durability?.roundToInt(), MaterialTheme.colors.onSurface)
                            )
                            DataRow(
                                title = "WEIGHT",
                                value = Pair("${weapon.Weight} KG", MaterialTheme.colors.onSurface)
                            )
                            DataRow(
                                title = "SIZE",
                                value = Pair("${weapon.Width?.roundToInt()}x${weapon.Height?.roundToInt()}", MaterialTheme.colors.onSurface)
                            )
                            DataRow(
                                title = "FIRING MODES",
                                value = Pair(weapon.weapFireType?.joinToString(", ")?.toUpperCase(Locale.current), MaterialTheme.colors.onSurface)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun AmmoCard(
        weapon: Weapon,
        defaultAmmo: Ammo
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            backgroundColor = Color(0xFE1F1F1F),
            onClick = {
                setResult(RESULT_OK, Intent().putExtra("caliber", "ammunition/${weapon.ammoCaliber}"))
                finish()
            }
        ) {
            Column {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "AMMUNITION",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp, start = 16.dp, end = 16.dp)
                    )
                }
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
                ) {
                    DataRow(
                        title = "CALIBER",
                        value = Pair(getCaliberName(weapon.ammoCaliber), MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "DEFAULT AMMO",
                        value = Pair(defaultAmmo.name, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "MUZZLE VELOCITY",
                        value = Pair(defaultAmmo.ballistics?.getMuzzleVelocity(), MaterialTheme.colors.onSurface)
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
        pricing: Pricing
    ) {
        val context = LocalContext.current
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .clickable {
                    context.openActivity(FleaItemDetail::class.java) {
                        putString("id", pricing.id)
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
                    pricing.buyFor?.forEach { item ->
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
    fun ModSlot(slot: Weapon.Slot, build: WeaponBuild?) {
        val buildSlot = build?.mods?.get(slot._id)

        Card(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
            border = if (slot._required == true) BorderStroke(0.25.dp, Red400) else null,
            onClick = {
                val intent = Intent(this, ModPickerActivity::class.java)
                intent.putExtra("ids", slot.getSubIds()?.joinToString(";"))
                intent.putExtra("type", slot._name)
                intent.putExtra("parent", slot._parent)
                intent.putExtra("id", slot._id)
                resultLauncher.launch(intent)
            },
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = slot.getName(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Column {
                    Text(text = buildSlot?.item?.Name.toString())
                }
            }
        }
    }
}