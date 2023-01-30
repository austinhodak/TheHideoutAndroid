package com.austinhodak.thehideout.compose.components

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import com.austinhodak.tarkovapi.FleaVisibleName
import com.austinhodak.tarkovapi.FleaVisiblePrice
import com.austinhodak.tarkovapi.FleaVisibleTraderPrice
import com.austinhodak.tarkovapi.IconSelection
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.utils.*

@SuppressLint("CheckResult")
@OptIn(ExperimentalFoundationApi::class)
@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun FleaItem(
    item: Item,
    priceDisplay: FleaVisiblePrice,
    iconDisplay: IconSelection,
    traderPrice: FleaVisibleTraderPrice,
    settings: List<Any>,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {

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

    val icon = when (iconDisplay) {
        IconSelection.ORIGINAL -> item.pricing?.getCleanIcon()
        IconSelection.TRANSPARENT -> item.pricing?.getTransparentIcon()
        IconSelection.GAME -> item.pricing?.getIcon()
    }

    val border = when (iconDisplay) {
        IconSelection.ORIGINAL -> BorderColor
        IconSelection.TRANSPARENT -> Color.Unspecified
        IconSelection.GAME -> Color.Unspecified
    }

    val displayName = when (settings.find { it is FleaVisibleName } as? FleaVisibleName ?: FleaVisibleName.NAME) {
        FleaVisibleName.NAME -> item.Name
        FleaVisibleName.SHORT_NAME -> item.ShortName
        else -> item.Name
    }

    Card(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = {
                    onClick(item.id)
                },
                onLongClick = {
                    context.showDialog(
                        Pair("Wiki Page") {
                            item.pricing?.wikiLink?.openWithCustomTab(context)
                        },
                        Pair("Add to Needed Items") {
                            item.pricing?.addToNeededItemsDialog(context)
                        },
                        Pair("Add Price Alert") {
                            item.pricing?.addPriceAlertDialog(context)
                        },
                        Pair("Add to Cart") {
                            item.pricing?.addToCartDialog(context)
                        },
                    )
                }
            ),
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        Column {
            Row(
                Modifier
                    .padding(end = 16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Rectangle(color = color, modifier = Modifier
                    .fillMaxHeight()
                    .padding(end = 16.dp))
                Image(
                    fadeImagePainterPlaceholder(
                        icon,
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .width(48.dp)
                        .height(48.dp)
                        .border((0.25).dp, color = border)
                )
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.ShortName ?: "",
                        style = MaterialTheme.typography.subtitle2,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colors.secondary
                    )
                    Text(
                        text = displayName ?: "",
                        style = MaterialTheme.typography.h6,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp,
                        lineHeight = 12.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        val text = if (item.pricing?.noFlea == false && item.pricing?.isDisabled() == false) {
                            item.getUpdatedTime()
                        } else {
                            if (item.pricing?.isDisabled() == true) {
                                "Disabled item, not available in game."
                            } else {
                                "Not available on flea market."
                            }
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    val price = when (priceDisplay) {
                        FleaVisiblePrice.DEFAULT -> item.getPrice()
                        FleaVisiblePrice.AVG -> item.pricing?.avg24hPrice
                        FleaVisiblePrice.HIGH -> item.pricing?.high24hPrice
                        FleaVisiblePrice.LOW -> item.pricing?.low24hPrice
                        FleaVisiblePrice.LAST -> item.pricing?.lastLowPrice
                    }

                    Text(
                        text = price?.asCurrency() ?: "",
                        style = MaterialTheme.typography.h6,
                        fontSize = 15.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "${item.getPricePerSlot(price ?: 0).asCurrency()}/slot",
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp
                        )
                    }
                    /*CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "${item.pricing?.changeLast48h}%",
                            style = MaterialTheme.typography.caption,
                            color = if (item.pricing?.changeLast48h ?: 0.0 > 0.0) Green500 else if (item.pricing?.changeLast48h ?: 0.0 < 0.0) Red500 else Color.Unspecified,
                            fontSize = 10.sp
                        )
                    }*/
                    TraderSmall(item = item.pricing, traderPrice)
/*                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val text = if (item.pricing?.noFlea == false && item.pricing?.isDisabled() == false) {
                            item.getUpdatedTime(true)
                        } else {
                            if (item.pricing?.isDisabled() == true) {
                                "Disabled item, not available in game."
                            } else {
                                "Flea Banned"
                            }
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp
                        )
                        Icon(
                            imageVector = Icons.Filled.Sync,
                            contentDescription = "Last Updated",
                            modifier = Modifier
                                .padding(start = 2.dp)
                                .size(12.dp),
                            tint = White
                        )
                    }
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {

                    }*/
                }
            }
        }
    }
}

@Composable
fun Rectangle(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(5.dp)
            .clip(RectangleShape)
            .background(color)
    )
}

@Composable
fun SmallBuyPrice(pricing: Pricing?) {
    val i = pricing?.getCheapestBuyRequirements() ?: return
    if (i.isZero()) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 0.dp)
    ) {
        Image(
            painter = fadeImagePainter(i.traderImage(false)),
            contentDescription = "Trader",
            modifier = Modifier.size(16.dp)
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = i.getPriceAsCurrency() ?: "",
                style = MaterialTheme.typography.caption,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun SmallSellPrice(pricing: Pricing?) {
    val i = pricing?.getHighestSellRequirements() ?: return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 0.dp)
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = i.traderImage(false)),
            contentDescription = "Trader",
            modifier = Modifier.size(16.dp)
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = i.getPriceAsCurrency() ?: "",
                style = MaterialTheme.typography.caption,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun TraderSmall(item: Pricing?, price: FleaVisibleTraderPrice = FleaVisibleTraderPrice.BEST_SELL) {
    val i = if (price == FleaVisibleTraderPrice.BEST_SELL) item?.getHighestSellTrader() else item?.getCheapestTrader()
    i?.let {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 0.dp)
        ) {
            when {
                item?.changeLast48h ?: 0.0 > 0.0 -> {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_up_24), contentDescription = "", modifier = Modifier.size(16.dp), tint = Green500)
                }
                item?.changeLast48h ?: 0.0 < 0.0 -> {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_down_24), contentDescription = "", modifier = Modifier.size(16.dp), tint = Red500)
                }
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = i.price?.asCurrency(i.currency ?: "R") ?: "",
                    style = MaterialTheme.typography.caption,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Image(
                painter = fadeImagePainter(
                    i
                ),
                contentDescription = "Trader",
                modifier = Modifier.size(16.dp)
            )
        }
    }

}

@Preview
@Composable
fun TraderSmallPreview() {
    TraderSmall(
        Pricing (
            id = "",
            name = null,
            shortName = null,
            iconLink = null,
            imageLink = null,
            gridImageLink = null,
            avg24hPrice = null,
            basePrice = 0,
            lastLowPrice = null,
            changeLast48h = null,
            low24hPrice = null,
            high24hPrice = null,
            updated = null,
            types = emptyList(),
            width = null,
            height = null,
            sellFor = emptyList(),
            buyFor = listOf(
                Pricing.BuySellPrice(
                    "therapist",
                    price = 1509,
                    requirements = listOf(
                        Pricing.BuySellPrice.Requirement(
                            "loyaltyLevel",
                            1
                        )
                    ),
                    ""
                )
            ),
            wikiLink = null,
            false,
            null
        )
    )
}