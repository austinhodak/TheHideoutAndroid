package com.austinhodak.thehideout.tools

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.pickers.PickerActivity
import com.austinhodak.thehideout.tools.viewmodels.SensitivityViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.roundToInt

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun SensitivityCalculatorScreen(navViewModel: NavViewModel, tarkovRepo: TarkovRepo, sensitivityViewModel: SensitivityViewModel, armorPickerLauncher: ActivityResultLauncher<Intent>) {

    val selectedArmor by sensitivityViewModel.selectedArmor.observeAsState()
    val selectedHelmet by sensitivityViewModel.selectedHelmet.observeAsState()

    val change by sensitivityViewModel.change.observeAsState()
    val newDPI by sensitivityViewModel.newDPI.observeAsState()
    val newHipfire by sensitivityViewModel.newHipfire.observeAsState()

    val userDPI = UserSettingsModel.dpi.value
    val userHipfire = UserSettingsModel.hipfireSens.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Sensitivity Calculator", modifier = Modifier.padding(end = 16.dp))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navViewModel.isDrawerOpen.value = true
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = null, tint = White)
                    }
                },
                backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                elevation = 0.dp,
                actions = {
                    IconButton(onClick = {
                        //Reset health.

                    }) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Color.White)
                    }
                }
            )
        },
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HelmetCard(selectedArmor = selectedHelmet, armorPickerLauncher)
            ArmorCard(selectedArmor = selectedArmor, armorPickerLauncher)

            Spacer(modifier = Modifier.weight(1f))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                //modifier = Modifier.padding(end = 16.dp)
                modifier = Modifier.width(100.dp)
            ) {
                Text(
                    text = "$change",
                    style = MaterialTheme.typography.h4,
                    color = Red400
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "% CHANGE",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Light,
                        fontSize = 10.sp
                    )
                }
            }

            Divider(
                color = DividerDark,
                modifier = Modifier.padding(top = 16.dp, end = 0.dp, start = 0.dp, bottom = 16.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    //modifier = Modifier.padding(end = 16.dp)
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(
                        text = "$newDPI",
                        style = MaterialTheme.typography.h4,
                        color = Green400
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "NEW DPI",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "($userDPI)",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = "OR",
                    modifier = Modifier,
                    textAlign = TextAlign.Center
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    //modifier = Modifier.padding(end = 16.dp),
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(
                        text = "$newHipfire",
                        style = MaterialTheme.typography.h4,
                        color = Green400
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "NEW HIPFIRE",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "($userHipfire)",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            /*Divider(
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    //modifier = Modifier.padding(end = 16.dp)
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(
                        text = "$userDPI",
                        style = MaterialTheme.typography.h5
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "DPI",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = "OR",
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    color = Color.Transparent
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    //modifier = Modifier.padding(end = 16.dp),
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(
                        text = "$userHipfire",
                        style = MaterialTheme.typography.h5
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "HIPFIRE",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                }
            }*/

            /*Text(text = userDPI.toString())
            Text(text = userHipfire)
            Text(text = change.toString())

            Text(text = "New DPI: $newDPI")
            Text(text = "New Hipfire: $newHipfire")*/
        }
    }
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
private fun HelmetCard(selectedArmor: Item?, armorPickerLauncher: ActivityResultLauncher<Intent>) {
    val context = LocalContext.current
    BottomCard({
        Intent(context, PickerActivity::class.java).apply {
            putExtra("type", "helmet")
        }.let {
            armorPickerLauncher.launch(it)
        }
    }) {
        Row(
            Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedArmor?.pricing?.iconLink != null) {
                Image(
                    painter = rememberImagePainter(data = selectedArmor.pricing?.iconLink),
                    contentDescription = "",
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.icons8_helmet_96),
                    contentDescription = "",
                    modifier = Modifier.size(40.dp),
                    tint = White
                )
            }

            Column(
                Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = selectedArmor?.ShortName ?: "No Helmet",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium, color = White
                )
                Text(
                    text = selectedArmor?.armorZone?.joinToString(", ") ?: "Select a Helmet",
                    style = MaterialTheme.typography.caption,
                    fontSize = 10.sp,
                    color = White
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                //modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    text = "${selectedArmor?.mousePenalty?.roundToInt() ?: 0}",
                    style = MaterialTheme.typography.h6,
                    fontSize = 18.sp,
                    color = Red400
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "PENALTY",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Light,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
private fun ArmorCard(selectedArmor: Item?, armorPickerLauncher: ActivityResultLauncher<Intent>) {
    val context = LocalContext.current
    BottomCard({
        Intent(context, PickerActivity::class.java).apply {
            putExtra("type", "armor")
        }.let {
            armorPickerLauncher.launch(it)
        }
    }) {
        Row(
            Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedArmor?.pricing?.iconLink != null) {
                Image(
                    painter = rememberImagePainter(data = selectedArmor.pricing?.iconLink),
                    contentDescription = "",
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.icons8_bulletproof_vest_100),
                    contentDescription = "",
                    modifier = Modifier.size(40.dp),
                    tint = White
                )
            }
            Column(
                Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = selectedArmor?.ShortName ?: "No Chest Armor",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium,
                    color = White
                )
                Text(
                    text = selectedArmor?.armorZone?.joinToString(", ") ?: "Select Chest Armor",
                    style = MaterialTheme.typography.caption,
                    fontSize = 10.sp,
                    color = White
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                //modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(
                    text = "${selectedArmor?.mousePenalty?.roundToInt() ?: 0}",
                    style = MaterialTheme.typography.h6,
                    fontSize = 18.sp,
                    color = Red400
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "PENALTY",
                        style = MaterialTheme.typography.caption,
                        fontWeight = FontWeight.Light,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun BottomCard(
    onClick: () -> Unit,
    color: Color = DarkerGrey,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        backgroundColor = color,
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        content = content
    )
}