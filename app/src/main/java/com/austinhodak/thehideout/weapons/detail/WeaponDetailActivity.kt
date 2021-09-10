package com.austinhodak.thehideout.weapons.detail

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.thehideout.WeaponBuild
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.weapons.mods.ModPickerActivity
import com.austinhodak.thehideout.weapons.viewmodel.WeaponDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@ExperimentalMaterialApi
@AndroidEntryPoint
class WeaponDetailActivity : AppCompatActivity() {

    private val weaponViewModel: WeaponDetailViewModel by viewModels()

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

                val build by weaponViewModel.weaponBuild.observeAsState()

                Timber.d("TEST: ${build?.mods}")

                if (build == null && weapon != null) {
                    weaponViewModel.updateBuild(
                        WeaponBuild(
                            parentWeapon = weapon
                        )
                    )
                }

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
                    Column {
                        weapon?.Slots?.forEach {
                            ModSlot(slot = it, build)
                        }
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