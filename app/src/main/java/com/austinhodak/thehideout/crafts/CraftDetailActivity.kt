package com.austinhodak.thehideout.crafts

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
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
import com.austinhodak.thehideout.flea_market.detail.AvgPriceRow
import com.austinhodak.thehideout.flea_market.detail.SavingsRow
import com.austinhodak.thehideout.utils.*

class CraftDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val craft = intent.getSerializableExtra("craft") as Craft

        setContent {
            HideoutTheme {
                var isFavorited by remember {
                    mutableStateOf(Favorites.crafts.contains(craft.id))
                }

                Favorites.crafts.observe(lifecycleScope) {
                    isFavorited = it.contains(craft.id)
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "Craft",
                                        color = MaterialTheme.colors.onPrimary,
                                        style = MaterialTheme.typography.h6,
                                        maxLines = 1,
                                        fontSize = 18.sp,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "${craft.rewardItem()?.item?.name}",
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
                                        craft.id.let {
                                            Favorites.crafts.remove(lifecycleScope, it)
                                        }
                                        false
                                    } else {
                                        craft.id.let {
                                            Favorites.crafts.add(lifecycleScope, it)
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
                            ModuleCard(craft)
                            craft.rewardItem()?.let {
                                RewardCard(it)
                            }
                            craft.requiredItems?.let {
                                RequireCard(list = it)
                            }
                            CalculationCard(craft = craft)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun CalculationCard(craft: Craft) {
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
                AvgPriceRow(title = "COST", price = craft.totalCost())
                SavingsRow(title = "FLEA THROUGHPUT/H", price = craft.getFleaThroughput())
                SavingsRow(title = "ESTIMATED PROFIT", price = craft.estimatedProfit())
                SavingsRow(title = "ESTIMATED PROFIT/H", price = craft.estimatedProfitPerHour())
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
                            text = "YOU NEED",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
                }
                list.sortedWith(
                    compareBy({ it?.isTool() }, {it?.item?.shortName})
                ).forEach { item ->
                    item?.item?.let { pricing ->
                        CompactItem(
                            item = pricing, extras = ItemSubtitle(
                                iconText = item.count?.toString(),
                                showPriceInSubtitle = true,
                                subtitle = " (${(item.count?.times(pricing.getCheapestBuyRequirements().price ?: 0))?.asCurrency()})"
                            ), if (item.isTool()) ToolBlue else BorderColor
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun RewardCard(reward: Craft.CraftItem) {
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
                reward.item?.let {
                    CompactItem(
                        item = it, ItemSubtitle(
                            iconText = reward.count?.toString(),
                            subtitleComposable = {
                                SmallSellPrice(pricing = it)
                                Text(
                                    text = " (${(reward.count?.times(it.getHighestSellRequirements().price ?: 0))?.asCurrency()})",
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Light,
                                )
                            }
                        )
                    )
                }
            }
        }
    }

    data class ItemSubtitle(
        val iconText: String? = null,
        val subtitle: String? = null,
        val showPriceInSubtitle: Boolean? = false,
        val subtitleComposable: @Composable (() -> Unit)? = null
    )



    @Composable
    private fun ModuleCard(craft: Craft?) {
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
                        painter = painterResource(id = craft?.getSourceName()?.getHideoutIcon() ?: R.drawable.air_filtering_unit_portrait), contentDescription = null,
                        Modifier
                            .size(42.dp)
                    )
                    Column(
                        Modifier.padding(start = 16.dp)
                    ) {
                        Text(text = "${craft?.getSourceName()} Level ${craft?.getSourceLevel()}", style = MaterialTheme.typography.h6, fontSize = 18.sp)
                        CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                            Text(text = "${craft?.getCraftingTime("%02d Hours %02d Minutes (${craft.getFinishTime()})")}", style = MaterialTheme.typography.caption)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactItem(item: Pricing, extras: CraftDetailActivity.ItemSubtitle? = null, color: Color = BorderColor) {
    val context = LocalContext.current
    var showPrices by remember {
        mutableStateOf(false)
    }
    AnimatedContent(
        targetState = showPrices,
        transitionSpec = { fadeIn() with fadeOut() },
        modifier = Modifier.clickable {
            context.openFleaDetail(item.id)
        }
    ) { expanded ->
        if (expanded) {
            Column(
                modifier = Modifier
                    .padding(top = 0.dp, start = 8.dp, end = 8.dp, bottom = 8.dp)
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
                                .border((0.25).dp, color = color)
                        )
                        if (color == ToolBlue) {
                            Image(painter = painterResource(id = R.drawable.icons8_wrench_50), contentDescription = null, modifier = Modifier.size(16.dp).padding(2.dp).align(Alignment.BottomEnd))
                        } else {
                            extras?.iconText?.let {
                                Text(
                                    text = it,
                                    Modifier
                                        .clip(RoundedCornerShape(topStart = 5.dp))
                                        .background(color)
                                        .padding(start = 3.dp, end = 2.dp, top = 1.dp, bottom = 1.dp)
                                        .align(Alignment.BottomEnd),
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 9.sp
                                )
                            }
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
                if (color == ToolBlue) {
                    Text("Auxiliary Tool, will return to stash.", color = ToolBlue, modifier = Modifier.padding(start = 16.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                .border((0.25).dp, color = color)
                        )
                        if (color == ToolBlue) {
                            Image(painter = painterResource(id = R.drawable.icons8_wrench_50), contentDescription = null, modifier = Modifier.size(16.dp).padding(2.dp).align(Alignment.BottomEnd))
                        } else {
                            extras?.iconText?.let {
                                Text(
                                    text = it,
                                    Modifier
                                        .clip(RoundedCornerShape(topStart = 5.dp))
                                        .background(color)
                                        .padding(start = 3.dp, end = 2.dp, top = 1.dp, bottom = 1.dp)
                                        .align(Alignment.BottomEnd),
                                    style = MaterialTheme.typography.caption,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 9.sp
                                )
                            }
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