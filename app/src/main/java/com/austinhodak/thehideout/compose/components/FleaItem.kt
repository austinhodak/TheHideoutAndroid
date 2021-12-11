package com.austinhodak.thehideout.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.FleaVisiblePrice
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.utils.traderImage

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun FleaItem(
    item: Item,
    priceDisplay: FleaVisiblePrice,
    onClick: (String) -> Unit
) {

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

    Card(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        //border = BorderStroke(1.dp, color = color),
        onClick = {
            onClick(item.id)
        },
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
                    rememberImagePainter(item.pricing?.iconLink ?: "https://tarkov-tools.com/images/flea-market-icon.jpg"),
                    contentDescription = null,
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp)
                        .border((0.25).dp, color = BorderColor)
                )
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item.pricing?.name ?: "",
                        style = MaterialTheme.typography.h6,
                        fontSize = 15.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = item.getUpdatedTime(),
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(vertical = 16.dp)
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
                    TraderSmall(item = item.pricing)
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 0.dp)
    ) {
        Image(
            painter = rememberImagePainter(data = i?.traderImage(false)),
            contentDescription = "Trader",
            modifier = Modifier.size(16.dp)
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = i?.getPriceAsCurrency() ?: "",
                style = MaterialTheme.typography.caption,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
fun TraderSmall(item: Pricing?) {
    val i = item?.getHighestSellTrader()
    i?.let {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 0.dp)
        ) {
            when {
                item.changeLast48h ?: 0.0 > 0.0 -> {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_up_24), contentDescription = "", modifier = Modifier.size(16.dp), tint = Green500)
                }
                item.changeLast48h ?: 0.0 < 0.0 -> {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_down_24), contentDescription = "", modifier = Modifier.size(16.dp), tint = Red500)
                }
            }
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = i.price?.asCurrency() ?: "",
                    style = MaterialTheme.typography.caption,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
            Image(
                painter = rememberImagePainter(data = i.traderImage()),
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
                    )
                )
            ),
            wikiLink = null
        )
    )
}