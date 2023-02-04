package com.austinhodak.thehideout.features.medical

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.austinhodak.tarkovapi.models.Stim
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.features.ammunition.PricingCard
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.features.flea_market.detail.BasicStatRow
import com.austinhodak.thehideout.stims
import com.austinhodak.thehideout.ui.theme.Bender
import com.austinhodak.thehideout.ui.theme.HideoutTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class MedDetailActivity : AppCompatActivity() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val itemID = intent.getStringExtra("id") ?: "5c0e530286f7747fa1419862"
        /*val type = item.itemType ?: ItemTypes.MED
        val isMed = type == ItemTypes.MED
        val isStim = type == ItemTypes.STIM*/

        setContent {
            val item by tarkovRepo.getItemByID(itemID).collectAsState(initial = null)
            val effects = if (item?.StimulatorBuffs != null) {
                stims.getStim(item?.StimulatorBuffs!!)
            } else null
            HideoutTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row {
                                    Image(
                                        painter = rememberAsyncImagePainter(item?.pricing?.getTransparentIcon()),
                                        contentDescription = null,
                                        Modifier.size(36.dp)
                                    )
                                    Column(Modifier.padding(start = 16.dp)) {
                                        Text(
                                            text = "${item?.ShortName}",
                                            color = MaterialTheme.colors.onPrimary,
                                            style = MaterialTheme.typography.h6,
                                            maxLines = 1,
                                            fontSize = 18.sp,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "(${item?.Name})",
                                            color = MaterialTheme.colors.onPrimary,
                                            style = MaterialTheme.typography.caption,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            },
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            }
                        )
                    }
                ) {
                    if (item == null) {
                        return@Scaffold
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        item {
                            BasicStatsCard(item = item!!)
                            if (item!!.getBuffs(effects).any { it.type == "buff" }) {
                                BuffsCard(item = item!!, effects)
                            }
                            if (item!!.getBuffs(effects).any { it.type == "debuff" }) {
                                DeBuffsCard(item = item!!, effects)
                            }
                            if (item?.pricing?.buyFor?.isNotEmpty() == true) {
                                PricingCard(pricing = item?.pricing!!)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun BasicStatsCard(item: Item) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "STATS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
                }
                BasicStatRow(title = "WEIGHT", text = item.getFormattedWeight())
                BasicStatRow(title = "USE TIME", text = "${item.medUseTime?.roundToInt()}s")
                BasicStatRow(title = "HP POOL", text = "${item.MaxHpResource?.roundToInt()}")
                BasicStatRow(title = "MAX PER USE", text = "${item.hpResourceRate?.roundToInt()}")
            }
        }
    }

    @Composable
    private fun BuffsCard(item: Item, effects: Stim?) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        top = 16.dp,
                        start = 0.dp,
                        end = 16.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "BUFFS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                item.getBuffs(effects).filter { it.type == "buff" }.sortedBy { it.icon == null }.forEach { effect ->
                    Row(
                        modifier = Modifier.padding(end = 16.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        effect.icon?.let {
                            Image(painter = painterResource(id = it), contentDescription = null, modifier = Modifier.padding(start = 16.dp).size(16.dp))
                        }
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = effect.title.uppercase(),
                                style = MaterialTheme.typography.body1,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp).weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                        //Spacer(modifier = Modifier.weight(1f))
                        if (effect.color != null) {
                            Text(
                                text = effect.value ?: "",
                                style = MaterialTheme.typography.body1,
                                fontSize = 14.sp,
                                color = effect.color!!
                            )
                        } else {
                            Text(
                                text = effect.value ?: "",
                                style = MaterialTheme.typography.body1,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DeBuffsCard(item: Item, effects: Stim?) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                Modifier.padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        top = 16.dp,
                        start = 0.dp,
                        end = 16.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "DEBUFFS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
                item.getBuffs(effects).filter { it.type == "debuff" }.sortedBy { it.icon == null }.forEach { effect ->
                    Row(
                        modifier = Modifier.padding(end = 16.dp, top = 4.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        effect.icon?.let {
                            Image(painter = painterResource(id = it), contentDescription = null, modifier = Modifier.padding(start = 16.dp).size(16.dp))
                        }
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = effect.title.uppercase(),
                                style = MaterialTheme.typography.body1,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp).weight(1f),
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                        }
                        //Spacer(modifier = Modifier.weight(1f))
                        if (effect.color != null) {
                            Text(
                                text = effect.value ?: "",
                                style = MaterialTheme.typography.body1,
                                fontSize = 14.sp,
                                color = effect.color!!
                            )
                        } else {
                            Text(
                                text = effect.value ?: "",
                                style = MaterialTheme.typography.body1,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        }
    }

}