package com.austinhodak.thehideout.flea_market.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.austinhodak.tarkovapi.fragment.ItemFragment
import com.austinhodak.tarkovapi.fragment.ItemPrice
import com.austinhodak.tarkovapi.fragment.TaskItem
import com.austinhodak.tarkovapi.room.TarkovDatabase
import com.austinhodak.tarkovapi.room.models.Barter
import com.austinhodak.tarkovapi.room.models.Craft
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.tarkovapi.type.ItemSourceName
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.Rectangle
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.utils.*
import com.google.accompanist.glide.rememberGlidePainter
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToInt

class FleaItemDetail : ComponentActivity() {

    private val viewModel: FleaViewModel by viewModels()
    private lateinit var itemID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        itemID = intent.getStringExtra("id") ?: "54491c4f4bdc2db1078b4568"

        setContent {
            TheHideoutTheme {
                val scaffoldState = rememberScaffoldState()
                var selectedNavItem by remember { mutableStateOf(0) }
                val database = TarkovDatabase.getDatabase(this, lifecycleScope)
                val scope = rememberCoroutineScope()

                val item = database.ItemDao().getByID(itemID).observeAsState()

                val quests = database.QuestDao().getQuestsWithItemID("%${item.value?.id}%").observeAsState().value
                val barters = database.BarterDao().getBartersWithItemID("%${item.value?.id}%").observeAsState().value
                val crafts = database.CraftDao().getCraftsWithItemID("%${item.value?.id}%").observeAsState().value

                val items = listOf(
                    NavItem("Item", R.drawable.ic_baseline_shopping_cart_24),
                    NavItem("Barters", R.drawable.ic_baseline_compare_arrows_24, barters?.isNotEmpty()),
                    NavItem("Crafts", R.drawable.ic_baseline_handyman_24, crafts?.isNotEmpty()),
                    NavItem("Quests", R.drawable.ic_baseline_assignment_24, quests?.isNotEmpty())
                )

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = { HideoutToolbar(viewModel = viewModel, item = item.value) { finish() } },
                    bottomBar = { FleaBottomNav(selected = selectedNavItem, items) { selectedNavItem = it } },
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Crossfade(targetState = selectedNavItem) {
                            when (it) {
                                0 -> {
                                    LazyColumn(
                                        contentPadding = PaddingValues(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        item {
                                            Card1(item = item.value)
                                            if (!item.value?.pricing?.sellFor?.filter { it.fragments.itemPrice.price != 0 }
                                                    .isNullOrEmpty()) TradersSellCard(
                                                title = "SELL PRICES",
                                                item = item.value,
                                                item.value?.pricing?.sellFor
                                            )
                                            if (!item.value?.pricing?.buyFor?.filter { it.fragments.itemPrice.price != 0 }
                                                    .isNullOrEmpty()) TradersBuyCard(title = "BUY PRICES", item = item.value, item.value?.pricing?.buyFor)
                                        }
                                        /*item {
                                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                                Text(
                                                    modifier = Modifier.padding(top = 4.dp),
                                                    text = item.value?.getUpdatedTime() ?: "",
                                                    style = MaterialTheme.typography.caption,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }*/
                                    }
                                }
                                1 -> {
                                    BartersPage(item = item.value, database)
                                }
                                2 -> {
                                    CraftsPage(item = item.value, database)
                                }
                                3 -> QuestsPage(item = item.value, database, scope, quests)
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun QuestsPage(
    item: Item?,
    database: TarkovDatabase,
    scope: CoroutineScope,
    quests: List<Quest>?
) {

    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(items = quests ?: emptyList()) { quest ->
//            val isRequired = quest.isRequiredForKappa(database).observeAsState().value
//            Text("${quest.title} - ${isRequired == 1}")
            QuestItem(quest = quest, item = item, database)
        }
    }
}

@Composable
private fun QuestItem(
    quest: Quest,
    item: Item?,
    database: TarkovDatabase,
) {

    val isKappa = quest.isRequiredForKappa(database).observeAsState().value

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                    Text(
                        text = "LEVEL ${quest.requirement?.level} • ${if (isKappa == true) "KAPPA" else "NOT KAPPA"}",
                        style = MaterialTheme.typography.overline
                    )
                }
                Text(
                    text = quest.title ?: "",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = quest.getMaps(),
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(top = 0.dp)
                )
            }
            Column {
                Box {
                    Image(
                        rememberGlidePainter(
                            request = quest.giver?.name?.traderIcon() ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .width(80.dp)
                            .height(80.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CraftsPage(
    item: Item?,
    database: TarkovDatabase
) {
    val crafts = database.CraftDao().getCraftsWithItemID("%${item?.id}%").observeAsState().value
    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(items = crafts ?: emptyList()) { craft ->
            CraftItem(craft)
        }
    }
}

@Composable
private fun CraftItem(craft: Craft) {
    val rewardItem = craft.rewardItems?.firstOrNull()?.item?.fragments?.itemFragment
    val requiredItems = craft.requiredItems

    Card(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column {
            Row(
                Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Image(
                        rememberGlidePainter(
                            request = rewardItem?.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .width(38.dp)
                            .height(38.dp)
                            .border((0.25).dp, color = BorderColor)
                    )
                }
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = rewardItem?.name ?: "",
                        style = MaterialTheme.typography.h6,
                        fontSize = 16.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                        Text(
                            text = craft.source,
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                    CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                        Text(
                            text = "${rewardItem?.avg24hPrice?.asCurrency()} @ Flea Market",
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

@Composable
private fun BartersPage(
    item: Item?,
    database: TarkovDatabase
) {
    val barters = database.BarterDao().getBartersWithItemID("%${item?.id}%").observeAsState().value
    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(items = barters ?: emptyList()) { barter ->
            BarterItem(barter)
        }
    }
}

@Composable
private fun BarterItem(barter: Barter) {
    val rewardItem = barter.rewardItems?.firstOrNull()?.item?.fragments?.itemFragment
    val requiredItems = barter.requiredItems

    Card(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column {
            Row(
                Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    rememberGlidePainter(
                        request = rewardItem?.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
                    ),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
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
                        text = rewardItem?.name ?: "",
                        style = MaterialTheme.typography.h6,
                        fontSize = 16.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                        Text(
                            text = barter.source,
                            style = MaterialTheme.typography.caption,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Light,
                        )
                    }
                    CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                        Text(
                            text = "${rewardItem?.avg24hPrice?.asCurrency()} @ Flea Market",
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
            AvgPriceRow(title = "COST", price = barter.totalCost())
            SavingsRow(title = "ESTIMATED SAVINGS", price = barter.estimatedProfit())
            Spacer(modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun BarterCraftCostItem(taskItem: TaskItem) {
    val item = taskItem.item.fragments.itemFragment
    Row(
        modifier = Modifier.padding(start = 16.dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            rememberGlidePainter(
                request = item.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
            ),
            contentDescription = null,
            modifier = Modifier
                .width(38.dp)
                .height(38.dp)
                .border((0.25).dp, color = BorderColor)
        )
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${item.shortName}",
                style = MaterialTheme.typography.body1
            )
            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                Text(
                    text = "${taskItem.count.roundToInt()} x ${item.avg24hPrice?.asCurrency()} = ${(taskItem.count * item.avg24hPrice!!).roundToInt().asCurrency()}",
                    style = MaterialTheme.typography.caption,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Light,
                )
            }
        }
    }
}

@Composable
fun FleaBottomNav(
    selected: Int,
    items: List<NavItem>,
    onItemSelected: (Int) -> Unit
) {

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.primary
    ) {
        items.forEachIndexed { index, item ->
            BottomNavigationItem(
                icon = { Icon(painter = painterResource(id = item.icon), contentDescription = null) },
                label = { Text(item.title) },
                selected = selected == index,
                onClick = { onItemSelected(index) },
                selectedContentColor = MaterialTheme.colors.secondary,
                unselectedContentColor = if (item.enabled == true) Color(0x99FFFFFF) else Color(0x33FFFFFF),
                enabled = item.enabled ?: true
            )
        }
    }
}

@Composable
fun HideoutToolbar(
    modifier: Modifier = Modifier,
    viewModel: FleaViewModel,
    item: Item?,
    onNavIconPressed: () -> Unit = { },
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row {
                Text(
                    text = item?.name ?: "The Hideout",
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.h6
                )
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        elevation = 0.dp,
        navigationIcon = {
            IconButton(onClick = onNavIconPressed) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
            }
        }
    )
}

@Composable
private fun Card1(
    item: Item?
) {
    val color = when (item?.BackgroundColor) {
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
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            Modifier.height(IntrinsicSize.Min)
        ) {
            Rectangle(color = color, modifier = Modifier.fillMaxHeight())
            Column {
                Row(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        rememberGlidePainter(
                            request = item?.pricing?.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
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
                            text = "Last Price: ${item?.getPrice()?.asCurrency()}",
                            style = MaterialTheme.typography.h6,
                            fontSize = 16.sp
                        )
                        CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                            Text(
                                text = "${item?.getPricePerSlot()?.asCurrency()}/slot",
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
                AvgPriceRow(title = "LOW 24H PRICE", price = item?.pricing?.low24hPrice)
                AvgPriceRow(title = "AVG 24H PRICE", price = item?.pricing?.avg24hPrice)
                AvgPriceRow(title = "HIGH 24H PRICE", price = item?.pricing?.high24hPrice, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
}

@Composable
private fun AvgPriceRow(
    title: String,
    price: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = title,
                style = MaterialTheme.typography.body1,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = price?.asCurrency() ?: "",
            style = MaterialTheme.typography.body1,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun TradersSellCard(
    title: String,
    item: Item?,
    prices: List<ItemFragment.SellFor>?
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Column() {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = Bender,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp, start = 16.dp, end = 16.dp)
                )
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                itemsIndexed(prices?.filter { it.fragments.itemPrice.price != 0 }?.sortedByDescending { it.fragments.itemPrice.price }
                    ?: emptyList()) { index, item ->
                    TraderPriceListGridItem(item.fragments.itemPrice, index == 0)
                }
            }
            /*prices?.sortedByDescending { it.fragments.itemPrice.price }?.forEachIndexed { index, item ->
                TraderPriceListItem(item.fragments.itemPrice, index == 0)
            }*/
        }
    }
}

@Composable
private fun TradersBuyCard(
    title: String,
    item: Item?,
    prices: List<ItemFragment.BuyFor>?
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Column() {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = Bender,
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp, start = 16.dp, end = 16.dp)
                )
            }
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
            ) {
                itemsIndexed(prices?.filter { it.fragments.itemPrice.price != 0 }?.sortedBy { it.fragments.itemPrice.price }
                    ?: emptyList()) { index, item ->
                    TraderPriceListGridItem(item.fragments.itemPrice, index == 0)
                }
            }
            /*prices?.sortedByDescending { it.fragments.itemPrice.price }?.forEachIndexed { index, item ->
                TraderPriceListItem(item.fragments.itemPrice, index == 0)
            }*/
        }
    }
}

@Composable
private fun TraderPriceListItem(
    item: ItemPrice,
    isHighest: Boolean = false
) {
    Row(
        modifier = Modifier.padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = item.toName(true)?.toUpperCase(Locale.current) ?: "",
                style = MaterialTheme.typography.body1,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = if (item.source == ItemSourceName.PEACEKEEPER) {
                item.price?.convertRtoUSD()?.asCurrency("D") ?: ""
            } else {
                item.price?.asCurrency() ?: ""
            },
            style = MaterialTheme.typography.body1,
            fontSize = 14.sp,
            fontWeight = if (isHighest) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun TraderPriceListGridItem(
    item: ItemPrice,
    isHighest: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .size(72.dp)
                .padding(bottom = 8.dp),
            painter = rememberGlidePainter(
                request = item.traderImage()
            ),
            contentDescription = "Prapor",

            )
        Text(
            text = if (item.source == ItemSourceName.PEACEKEEPER) {
                item.price?.convertRtoUSD()?.asCurrency("D") ?: ""
            } else {
                item.price?.asCurrency() ?: ""
            },
            style = MaterialTheme.typography.body1,
            fontSize = 12.sp,
            fontWeight = if (isHighest) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun SavingsRow(
    title: String,
    price: Int?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = title,
                style = MaterialTheme.typography.body1,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = price?.asCurrency() ?: "",
            style = MaterialTheme.typography.body1,
            color = if (price ?: 1 >= 1) Green500 else Red400,
            fontSize = 14.sp
        )
    }
}

data class NavItem(
    val title: String,
    @DrawableRes val icon: Int,
    val enabled: Boolean? = true
)