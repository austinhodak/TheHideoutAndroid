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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.utils.asCurrency
import com.google.accompanist.glide.rememberGlidePainter

@ExperimentalMaterialApi
@Composable
fun FleaItem(
    item: Item,
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
        }
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
                    rememberGlidePainter(request = item.pricing?.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"),
                    contentDescription = null,
                    modifier = Modifier
                        .width(38.dp)
                        .height(38.dp)
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
                    Text(
                        text = item.getPrice().asCurrency(),
                        style = MaterialTheme.typography.h6,
                        fontSize = 15.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "${item.getPricePerSlot().asCurrency()}/slot",
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp
                        )
                    }
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "${item.pricing?.changeLast48h}%",
                            style = MaterialTheme.typography.caption,
                            color = if (item.pricing?.changeLast48h ?: 0.0 > 0.0) Green500 else if (item.pricing?.changeLast48h ?: 0.0 < 0.0) Red500 else Color.Unspecified,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

/*@ExperimentalMaterialApi
@Preview
@Composable
fun FleaItemPreview() {
    FleaItem(item = Item(
        ""
    ))
}*/

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