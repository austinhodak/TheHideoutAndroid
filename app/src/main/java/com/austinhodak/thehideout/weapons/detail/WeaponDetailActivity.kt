package com.austinhodak.thehideout.weapons.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.WeaponBuild
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.DividerDark
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.utils.getCaliberName
import com.austinhodak.thehideout.weapons.mods.ModPickerActivity
import com.austinhodak.thehideout.weapons.viewmodel.WeaponDetailViewModel
import com.bumptech.glide.Glide
import com.stfalcon.imageviewer.StfalconImageViewer
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@ExperimentalMaterialApi
@AndroidEntryPoint
class WeaponDetailActivity : AppCompatActivity() {

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

        val weaponID = intent.getStringExtra("weaponID") ?: "5bb2475ed4351e00853264e3"
        weaponViewModel.getWeapon(weaponID)

        setContent {
            HideoutTheme {
                val weapon by weaponViewModel.weaponDetails.observeAsState()
                val scaffoldState = rememberScaffoldState()

                val defaultAmmo by tarkovRepo.getAmmoByID(weapon?.defAmmo ?: "").collectAsState(initial = null)

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        Column {
                            AmmoDetailToolbar(
                                title = weapon?.pricing?.name ?: "Error Loading...",
                                onBackPressed = { finish() }
                            )
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
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        item {
                            weapon?.let { WeaponDetailCard(weapon = it) }
                        }
                        item {
                            if (weapon != null && defaultAmmo != null) {
                                AmmoCard(weapon = weapon!!, defaultAmmo = defaultAmmo!!)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun WeaponDetailCard(
        weapon: Weapon,
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
                        rememberImagePainter(weapon.pricing?.iconLink),
                        contentDescription = null,
                        modifier = Modifier
                            .width(52.dp)
                            .height(52.dp)
                            .clickable {
                                StfalconImageViewer
                                    .Builder(this@WeaponDetailActivity, listOf(weapon.pricing?.imageLink)) { view, image ->
                                        Glide
                                            .with(view)
                                            .load(image)
                                            .into(view)
                                    }
                                    .withHiddenStatusBar(false)
                                    .show()
                            }
                    )
                    Text(
                        modifier = Modifier.padding(start = 16.dp),
                        text = getCaliberName(weapon.ammoCaliber),
                        style = MaterialTheme.typography.subtitle1
                    )
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
                        value = Pair(weapon.bEffDist, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "ERGONOMICS",
                        value = Pair(weapon.Ergonomics, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "FIRING MODES",
                        value = Pair(weapon.weapFireType?.joinToString(", ")?.toUpperCase(Locale.current), MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "RATE OF FIRE",
                        value = Pair(weapon.bFirerate, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "SIGHTING RANGE",
                        value = Pair(weapon.IronSightRange, MaterialTheme.colors.onSurface)
                    )
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
            Column() {
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
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
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

    @Composable
    private fun PricingCard(
        pricing: Pricing
    ) {
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
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