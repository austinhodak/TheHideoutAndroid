package com.austinhodak.thehideout.bitcoin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.airbnb.lottie.compose.*
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.utils.openFleaDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun BitcoinPriceScreen(
    navViewModel: NavViewModel,
    tarkovRepo: TarkovRepo
) {

    val context = LocalContext.current

    val btc by tarkovRepo.getItemByID("59faff1d86f7746c51718c9c").collectAsState(initial = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Bitcoin Price", modifier = Modifier.padding(end = 16.dp))
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

                }
            )
        },
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberImagePainter(data = btc?.pricing?.gridImageLink), contentDescription = "", modifier = Modifier
                        .padding(end = 16.dp)
                        .size(38.dp)
                        .border((0.25).dp, color = BorderColor)
                        .clickable {
                            btc?.id?.let { it1 -> context.openFleaDetail(it1) }
                        }
                )
                Text(text = btc?.pricing?.getHighestSellTrader()?.getPriceAsCurrency() ?: "", style = MaterialTheme.typography.h3, modifier = Modifier)
            }

            Spacer(modifier = Modifier.weight(1f))
            BitcoinLottie(
                Modifier
                    //.align(Alignment.BottomCenter)
                    .size(200.dp)
            )
            //Text(text = btc?.getUpdatedTime() ?: "", style = MaterialTheme.typography.caption, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
fun BitcoinLottie(modifier: Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.bitcoin_rocket))
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = modifier
    )
}