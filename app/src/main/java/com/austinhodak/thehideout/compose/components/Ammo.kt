package com.austinhodak.thehideout.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.austinhodak.tarkovapi.room.models.AmmoItem
import com.austinhodak.thehideout.utils.asCurrency
import com.google.accompanist.glide.rememberGlidePainter
import kotlin.math.roundToInt

@ExperimentalMaterialApi
@Composable
fun AmmoDetailCard(item: AmmoItem?) {
    Card(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).height(72.dp),
        onClick = {

        }
    ) {
        Column {
            Row(
                Modifier.padding( end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    Modifier
                        .padding(end = 16.dp)
                        .width(3.dp)
                        .fillMaxHeight()
                ) {
                    /*ArmorBox(Armor6, Modifier.weight(1f))
                    ArmorBox(Armor6, Modifier.weight(1f))
                    ArmorBox(Armor5, Modifier.weight(1f))
                    ArmorBox(Armor3, Modifier.weight(1f))
                    ArmorBox(Armor2, Modifier.weight(1f))
                    ArmorBox(Armor0, Modifier.weight(1f))*/
                }
                Image(
                    rememberGlidePainter(request = item?.pricing?.gridImageLink),
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
                        text = item?.shortName ?: "",
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Last Price: ${item?.getPrice()?.asCurrency()}",
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
                Column(
                    Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item?.ballistics?.damage.toString(),
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "DAMAGE",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = item?.ballistics?.penetrationPower?.roundToInt().toString(),
                        style = MaterialTheme.typography.h6,
                        fontSize = 18.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "PEN",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArmorBox(
    color: Color,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .background(color)
            .fillMaxWidth(),
    ) {
        Column {
            Box (
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.1f))
                    .fillMaxWidth()
                    .height(1.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Box (
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.1f))
                    .fillMaxWidth()
                    .height(1.dp)
            )
        }

    }
}