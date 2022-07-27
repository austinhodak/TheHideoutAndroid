@file:OptIn(ExperimentalAnimationApi::class)

package com.austinhodak.thehideout.barters

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.room.models.Barter
import com.austinhodak.tarkovapi.room.models.Craft
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.*
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.components.SmallSellPrice
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.crafts.CraftDetailActivity
import com.austinhodak.thehideout.flea_market.detail.AvgPriceRow
import com.austinhodak.thehideout.flea_market.detail.SavingsRow
import com.austinhodak.thehideout.utils.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.math.exp

class BarterDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val barter = intent.getSerializableExtra("barter") as Barter

        setContent {
            HideoutTheme {
                var isFavorited by remember {
                    mutableStateOf(Favorites.barters.contains(barter.id ?: 0))
                }

                Favorites.barters.observe(lifecycleScope) {
                    isFavorited = it.contains(barter.id ?: 0)
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "Barter",
                                        color = MaterialTheme.colors.onPrimary,
                                        style = MaterialTheme.typography.h6,
                                        maxLines = 1,
                                        fontSize = 18.sp,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${barter.getRewardItem()?.name}",
                                        color = MaterialTheme.colors.onPrimary,
                                        style = MaterialTheme.typography.caption,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    isFavorited = if (isFavorited) {
                                        barter.id?.let {
                                            Favorites.barters.remove(lifecycleScope, it)
                                        }
                                        false
                                    } else {
                                        barter.id?.let {
                                            Favorites.barters.add(lifecycleScope, it)
                                        }
                                        true
                                    }
                                }) {
                                    if (isFavorited) {
                                        Icon(Icons.Filled.Favorite, contentDescription = null, tint = Pink500)
                                    } else {
                                        Icon(Icons.Filled.FavoriteBorder, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        )
                    }
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        item {
                            TraderCard(barter)
                            barter.getRewardItem()?.let {
                                RewardCard(item = it)
                            }
                            barter.requiredItems?.let {
                                RequireCard(it)
                            }
                            CalculationCard(barter = barter)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CalculationCard(barter: Barter) {
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
                            text = "THE NUMBERS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
                }
                AvgPriceRow(title = "COST", price = barter.totalCost())
                SavingsRow(title = "ESTIMATED SAVINGS", price = barter.estimatedProfit())
                SavingsRow(title = "INSTA PROFIT", price = barter.getInstaProfit())
            }
        }
    }

    @Composable
    private fun RequireCard(list: List<Craft.CraftItem?>) {
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
                        bottom = 12.dp,
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "HAND OVER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
                }
                list.forEach { item ->
                    item?.item?.let { pricing ->
                        CompactItem(
                            item = pricing, extras = ItemSubtitle(
                                iconText = item.count?.toString(),
                                showPriceInSubtitle = true,
                                subtitle = " (${(item.count?.times(pricing.getCheapestBuyRequirements().price ?: 0))?.asCurrency()})"
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun RewardCard(item: Pricing) {
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
                        bottom = 12.dp,
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "YOU RECEIVE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
                }
                //CompactItem(item = item)
                CompactItem(
                    item = item, ItemSubtitle(
                        subtitleComposable = {
                            SmallSellPrice(pricing = item)
                        }
                    )
                )
            }
        }
    }

    data class ItemSubtitle(
        val iconText: String? = null,
        val subtitle: String? = null,
        val showPriceInSubtitle: Boolean? = false,
        val subtitleComposable: @Composable (() -> Unit)? = null
    )

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class, ExperimentalCoroutinesApi::class, ExperimentalCoilApi::class)
    @Composable
    fun CompactItem(item: Pricing, extras: ItemSubtitle? = null) {
        var showPrices by remember {
            mutableStateOf(false)
        }
        AnimatedContent(
            targetState = showPrices,
            transitionSpec = { fadeIn() with fadeOut() },
            modifier = Modifier.clickable {
                openFleaDetail(item.id)
            }
        ) { expanded ->
            if (expanded) {
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .background(DividerDark, RoundedCornerShape(16.dp))
                        .padding(top = 16.dp, bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 0.dp, bottom = 4.dp, end = 16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box {
                            Image(
                                rememberImagePainter(
                                    item.getCleanIcon()
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(38.dp)
                                    .height(38.dp)
                                    .border((0.25).dp, color = BorderColor)
                            )
                            extras?.iconText?.let {
                                Text(
                                    text = it,
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
                        }
                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 0.dp)
                                .weight(1f)
                        ) {
                            Text(
                                text = "${item.name}",
                                style = MaterialTheme.typography.body1
                            )
                            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                Row {
                                    if (extras?.subtitleComposable != null) {
                                        extras.subtitleComposable?.let { it() }
                                    } else {
                                        if (extras?.showPriceInSubtitle == true) {
                                            SmallBuyPrice(pricing = item)
                                        }
                                        extras?.subtitle?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.caption,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Light,
                                            )
                                        }
                                    }
                                }
                            }

                        }
                        IconButton(onClick = { showPrices = !showPrices }, Modifier.size(24.dp)) {
                            if (item.sellFor?.isNullOrEmpty() == true && item.buyFor?.isNullOrEmpty() == true) {
                                return@IconButton
                            }
                            if (showPrices) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_down_24), contentDescription = null, Modifier.rotate(180f))
                            } else {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_down_24), contentDescription = null)
                            }
                        }
                    }
                    Column {
                        if (item.sellFor?.isNullOrEmpty() == false) {
                            Divider(color = DividerDark, modifier = Modifier.padding(top = 12.dp))
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Text(
                                    text = "SELL FOR",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light,
                                    fontFamily = Bender,
                                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                                )
                            }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 8.dp, top = 12.dp)
                            ) {
                                items(items = item.sellFor?.sortedByDescending { it.price } ?: emptyList()) { item: Pricing.BuySellPrice ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .padding(bottom = 8.dp),
                                            painter = fadeImagePainter(
                                                item.traderImage()
                                            ),
                                            contentDescription = null,
                                        )
                                        Text(
                                            text = item.price?.asCurrency() ?: "",
                                            style = MaterialTheme.typography.body1,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                        if (item.buyFor?.isNullOrEmpty() == false) {
                            Divider(color = DividerDark, modifier = Modifier.padding(top = 8.dp))
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Text(
                                    text = "BUY FOR",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Light,
                                    fontFamily = Bender,
                                    modifier = Modifier.padding(start = 16.dp, top = 16.dp)
                                )
                            }
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 8.dp, top = 12.dp)
                            ) {
                                items(items = item.buyFor?.sortedByDescending { it.price } ?: emptyList()) { item: Pricing.BuySellPrice ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Image(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .padding(bottom = 8.dp),
                                            painter = fadeImagePainter(
                                                item.traderImage()
                                            ),
                                            contentDescription = null,
                                        )
                                        Text(
                                            text = item.price?.asCurrency() ?: "",
                                            style = MaterialTheme.typography.body1,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Column {
                    Row(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 0.dp, bottom = 4.dp, end = 16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box {
                            Image(
                                rememberImagePainter(
                                    item.getCleanIcon()
                                ),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(38.dp)
                                    .height(38.dp)
                                    .border((0.25).dp, color = BorderColor)
                            )
                            extras?.iconText?.let {
                                Text(
                                    text = it,
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
                        }
                        Column(
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 0.dp)
                                .weight(1f)
                        ) {
                            Text(
                                text = "${item.shortName}",
                                style = MaterialTheme.typography.body1
                            )
                            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                Row {
                                    if (extras?.subtitleComposable != null) {
                                        extras.subtitleComposable?.let { it() }
                                    } else {
                                        if (extras?.showPriceInSubtitle == true) {
                                            SmallBuyPrice(pricing = item)
                                        }
                                        extras?.subtitle?.let {
                                            Text(
                                                text = it,
                                                style = MaterialTheme.typography.caption,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Light,
                                            )
                                        }
                                    }
                                }
                            }

                        }
                        IconButton(onClick = { showPrices = !showPrices }, Modifier.size(24.dp)) {
                            if (item.sellFor?.isNullOrEmpty() == true && item.buyFor?.isNullOrEmpty() == true) {
                                return@IconButton
                            }
                            if (showPrices) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_down_24), contentDescription = null, Modifier.rotate(180f))
                            } else {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_arrow_drop_down_24), contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TraderCard(barter: Barter?) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column {
                Row(
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = fadeImagePainter(url = "${barter?.source?.traderIconSource()}"), contentDescription = null,
                        Modifier
                            //.border(1.dp, BorderColor)
                            .size(52.dp)
                    )
                    Column(
                        Modifier.padding(start = 16.dp)
                    ) {
                        Text(text = "${barter?.getTraderName()} Level ${barter?.getTraderLevel()}", style = MaterialTheme.typography.h6, fontSize = 18.sp)
                        Text(text = "Current Level: ${barter?.getTraderName()?.currentTraderLevel()}", style = MaterialTheme.typography.body2)
                    }
                }
            }
        }
    }
}