package com.austinhodak.thehideout.hideout.detail.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.tarkovapi.room.models.Craft
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.fromDtoR
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.compose.components.EmptyText
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.currency.euroToRouble
import com.austinhodak.thehideout.flea_market.detail.AvgPriceRow
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.flea_market.detail.SavingsRow
import com.austinhodak.thehideout.utils.openActivity
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
fun CraftsPage(crafts: List<Craft>, navViewModel: NavViewModel, pagerState: PagerState, modules: List<Hideout.Module?>?, station: Hideout.Station?, noCrafts: (Boolean) -> Unit) {
    val searchKey by navViewModel.searchKey.observeAsState("")
    val selectedModule = modules?.getOrNull(pagerState.currentPage)

    val list = crafts.filter {
        if (pagerState.currentPage == modules?.size) {
            //ALL TAB
            it.source?.contains(station?.getName().toString(), true) == true
        } else {
            it.source.equals(selectedModule.toString(), true)
        }
    }.filter {
        it.rewardItem()?.item?.name?.contains(searchKey, true) == true ||
                it.rewardItem()?.item?.shortName?.contains(searchKey, true) == true
    }.sortedBy { it.rewardItem()?.item?.name }

    if (list.isEmpty()) {
        EmptyText(text = "No crafts found.")
        noCrafts(true)
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(top = 4.dp, bottom = 64.dp)
    ) {

        items(list) {
            CraftItem(craft = it)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CraftItem(craft: Craft) {
    val rewardItem = craft.rewardItems?.firstOrNull()?.item
    val reward = craft.rewardItems?.firstOrNull()
    val requiredItems = craft.requiredItems
    val context = LocalContext.current

    val alpha = ContentAlpha.high

    CompositionLocalProvider(LocalContentAlpha provides alpha) {
        Card(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            backgroundColor = Color(0xFE1F1F1F),
            onClick = {
                context.openActivity(FleaItemDetail::class.java) {
                    putString("id", rewardItem?.id)
                }
            },
        ) {
            Column {
                /*if (userData == null || userData.isHideoutModuleComplete(craft.getSourceID(hideoutList.hideout) ?: 0)) {

                } else {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(color = Red400)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Warning, contentDescription = "", tint = Color.Black, modifier = Modifier
                                .height(20.dp)
                                .width(20.dp)
                        )
                        Text(
                            text = "${craft.source?.uppercase()} NOT BUILT",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 34.dp)
                        )
                    }
                }*/

                Row(
                    Modifier
                        .padding(16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Image(
                            rememberImagePainter(
                                rewardItem?.getCleanIcon()
                            ),
                            contentDescription = null,
                            modifier = Modifier
                                .width(38.dp)
                                .height(38.dp)
                                .border((0.25).dp, color = BorderColor)
                        )
                        Text(
                            text = "${reward?.count}",
                            Modifier
                                .clip(RoundedCornerShape(topStart = 5.dp))
                                .background(BorderColor)
                                .padding(start = 3.dp, end = 2.dp, top = 1.dp, bottom = 1.dp)
                                .align(Alignment.BottomEnd),
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Medium,
                            fontSize = 9.sp
                        )
                    }
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${rewardItem?.name} (x${reward?.count})",
                            style = MaterialTheme.typography.h6,
                            fontSize = 16.sp
                        )
                        CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                            Text(
                                text = "${craft.source} • ${craft.getCraftingTime()}",
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Light,
                            )
                        }
                        CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                            val highestSell = rewardItem?.getHighestSell()

                            Text(
                                text = "${highestSell?.getPriceAsCurrency()} @ ${highestSell?.getTitle()} (${highestSell?.price?.times(reward?.count ?: 1)?.asCurrency()})",
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Light,
                            )
                        }
                    }
                }
                Divider(
                    modifier = Modifier.padding(bottom = 8.dp),
                    color = Color(0x1F000000)
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "NEEDS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp, start = 16.dp, end = 16.dp)
                    )
                }
                requiredItems?.forEach { taskItem ->
                    BarterCraftCostItem(taskItem)
                }
                Divider(
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    color = Color(0x1F000000)
                )
                AvgPriceRow(title = "COST", price = craft.totalCost())
                SavingsRow(title = "ESTIMATED PROFIT", price = craft.estimatedProfit())
                SavingsRow(title = "ESTIMATED PROFIT PER HOUR", price = craft.estimatedProfitPerHour())
                Spacer(modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun BarterCraftCostItem(taskItem: Craft.CraftItem?) {
    val item = taskItem?.item
    val context = LocalContext.current

    val cheapestBuy = item?.getCheapestBuyRequirements()?.copy()
    if (cheapestBuy?.currency == "USD") {
        cheapestBuy.price = cheapestBuy.price?.fromDtoR()?.roundToInt()
    } else if (cheapestBuy?.currency == "EUR") {
        cheapestBuy.price = euroToRouble(cheapestBuy.price?.toLong()).toInt()
    }
    Row(
        modifier = Modifier
            .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
            .clickable {
                context.openActivity(FleaItemDetail::class.java) {
                    putString("id", item?.id)
                }
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Image(
                rememberImagePainter(
                    item?.getCleanIcon()
                ),
                contentDescription = null,
                modifier = Modifier
                    .width(38.dp)
                    .height(38.dp)
                    .border((0.25).dp, color = BorderColor)
            )
            Text(
                text = "${taskItem?.count}",
                Modifier
                    .clip(RoundedCornerShape(topStart = 5.dp))
                    .background(BorderColor)
                    .padding(start = 3.dp, end = 2.dp, top = 1.dp, bottom = 1.dp)
                    .align(Alignment.BottomEnd),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp
            )
        }
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${item?.shortName}",
                style = MaterialTheme.typography.body1
            )
            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                Row {
                    SmallBuyPrice(pricing = taskItem?.item)
                    Text(
                        text = " (${(taskItem?.count?.times(cheapestBuy?.price ?: 0))?.asCurrency()})",
                        style = MaterialTheme.typography.caption,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                    )
                }
            }
        }
    }
}