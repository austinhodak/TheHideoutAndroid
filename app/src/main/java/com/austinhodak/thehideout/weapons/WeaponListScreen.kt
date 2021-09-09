package com.austinhodak.thehideout.weapons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Weapon
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.utils.getCaliberShortName
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.views.weaponCategories
import com.austinhodak.thehideout.weapons.detail.WeaponDetailActivity

@ExperimentalAnimationApi
@Composable
fun WeaponListScreen(
    classID: String,
    navViewModel: NavViewModel,
    tarkovRepo: TarkovRepo
) {
    val data = tarkovRepo.getWeaponsByClass(classID).collectAsState(initial = emptyList())
    val weaponClass = weaponCategories.find { it.third == classID }

    Scaffold(
        topBar = {
            Column {
                MainToolbar(
                    title = weaponClass?.first ?: "",
                    navViewModel = navViewModel
                )
            }
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(items = data.value.sortedBy { it.Name }) { weapon ->
                val visibleState = remember { MutableTransitionState(false) }
                visibleState.targetState = true
                AnimatedVisibility(
                    visibleState,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    WeaponCard(weapon)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WeaponCard(
    weapon: Weapon
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {
            context.openActivity(WeaponDetailActivity::class.java) {
                putString("weaponID", weapon.id)
            }
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    rememberImagePainter(weapon.pricing?.iconLink),
                    contentDescription = null,
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "${weapon.ShortName}",
                            style = MaterialTheme.typography.subtitle1
                        )
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = getCaliberShortName(weapon.ammoCaliber),
                                style = MaterialTheme.typography.overline
                            )
                        }
                    }
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Last Price: ${weapon.pricing?.getPrice()?.asCurrency()}",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }
    }
}