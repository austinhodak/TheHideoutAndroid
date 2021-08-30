package com.austinhodak.thehideout.compose.components

import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import com.austinhodak.tarkovapi.ItemQuery

@Composable
fun ClothingDetailCard(item: ItemQuery.Item?) {
    Card {
        /*Column {
            Row(
                Modifier.padding(16.dp)
            ) {
                Image(
                    rememberGlidePainter(request = item?.fragments?.itemFragment?.iconLink),
                    contentDescription = null,
                    modifier = Modifier
                        .width(38.dp)
                        .height(38.dp)
                )
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = item?.fragments?.itemFragment?.shortName ?: "",
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Last Price: ${item?.fragments?.itemFragment?.avg24hPrice?.asCurrency()}",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
        }*/
    }
}