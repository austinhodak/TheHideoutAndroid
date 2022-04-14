package com.austinhodak.thehideout.flea_market.detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.austinhodak.tarkovapi.IconSelection
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.*
import com.austinhodak.tarkovapi.type.ItemSourceName
import com.austinhodak.tarkovapi.type.ItemType
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.openActivity
import com.austinhodak.thehideout.*
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.barters.BarterDetailActivity
import com.austinhodak.thehideout.compose.components.*
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.crafts.CompactItem
import com.austinhodak.thehideout.crafts.CraftDetailActivity
import com.austinhodak.thehideout.firebase.FSUser
import com.austinhodak.thehideout.firebase.PriceAlert
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.gear.GearDetailActivity
import com.austinhodak.thehideout.quests.QuestDetailActivity
import com.austinhodak.thehideout.utils.*
import com.austinhodak.thehideout.views.PriceChartMarkerView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.stfalcon.imageviewer.StfalconImageViewer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import javax.inject.Inject

@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@AndroidEntryPoint
class FleaItemDetail : GodActivity() {

    override fun onBackPressed() {
        if (intent.hasExtra("fromNoti")) {
            val intent = Intent(this, NavActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    private val viewModel: FleaViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo
    private lateinit var itemID: String

    lateinit var scaffoldState: ScaffoldState

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        //WindowCompat.setDecorFitsSystemWindows(window, false)

        itemID = intent.getStringExtra("id") ?: "5751a25924597722c463c472"
        viewModel.getItemByID(itemID)

        setContent {
            ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {

                HideoutTheme {
                    scaffoldState = rememberScaffoldState()
                    var selectedNavItem by remember { mutableStateOf(0) }

                    val item by viewModel.item.observeAsState()

                    //Timber.d(item?.getGridIDs()?.toString())

                    val quests by tarkovRepo.getQuestsWithItemID("%${item?.id}%").collectAsState(emptyList())
                    val barters by tarkovRepo.getBartersWithItemID("%${item?.id}%").collectAsState(emptyList())
                    val crafts by tarkovRepo.getCraftsWithItemID("%${item?.id}%").collectAsState(emptyList())
                    val craftsFiltered = crafts.filter {
                        it.rewardItem()?.item?.id == item?.id || it.requiredItems?.any { it?.item?.id == item?.id } == true
                    }

                    val bartersFiltered = barters.filter {
                        it.getRewardItem()?.containsItem?.any { it.item?.id == item?.id } == true || it.getRewardItem()?.id == item?.id || it.requiredItems?.any { it?.item?.id == item?.id } == true
                    }

                    var parents by remember {
                        mutableStateOf(listOf<Item>())
                    }

                    LaunchedEffect(key1 = "parents") {
                        parents = tarkovRepo.getItemsByContains("%${item?.id}%")
                    }

                    val parentItems = parents.filter { it.id != itemID }

                    val userData by viewModel.userData.observeAsState()
                    val userDataFS by fsUser.observeAsState()

                    var priceAlerts by remember { mutableStateOf<List<PriceAlert>?>(null) }

                    val items = listOf(
                        NavItem("Item", R.drawable.ic_baseline_storefront_24),
                        NavItem("Barters", R.drawable.ic_baseline_compare_arrows_24, bartersFiltered.isNotEmpty()),
                        NavItem("Crafts", R.drawable.ic_baseline_handyman_24, craftsFiltered.isNotEmpty()),
                        NavItem("Quests", R.drawable.ic_baseline_assignment_24, quests.isNotEmpty())
                    )

                    var isFavorited by remember {
                        mutableStateOf(extras.favoriteItems?.contains(itemID))
                    }

                    extras.preference.registerOnSharedPreferenceChangeListener { sharedPreferences, s ->
                        if (s == "FAVORITE_ITEMS") {
                            isFavorited = extras.favoriteItems?.contains(itemID)
                        }
                    }

                    if (isDebug()) {
                        Timber.d(itemID)
                    }

                    rememberSystemUiController().setNavigationBarColor(DarkPrimary)

                    LaunchedEffect(key1 = "") {
                        questsFirebase.child("priceAlerts").orderByChild("uid").equalTo(uid()).addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                priceAlerts = snapshot.children.map {
                                    val alert = it.getValue<PriceAlert>()!!
                                    alert.reference = it.ref
                                    alert
                                }.filter {
                                    it.itemID == itemID
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {

                            }
                        })
                    }

                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            FleaDetailToolbar(
                                item = item,
                                actions = {
                                    IconButton(onClick = {
                                        isFavorited = if (isFavorited == true) {
                                            extras.removeFavorite(itemID)
                                            false
                                        } else {
                                            extras.addFavorite(itemID)
                                            true
                                        }
                                    }) {
                                        if (isFavorited == true) {
                                            Icon(Icons.Filled.Favorite, contentDescription = null, tint = Pink500)
                                        } else {
                                            Icon(Icons.Filled.FavoriteBorder, contentDescription = null, tint = Color.White)
                                        }
                                    }
                                    OverflowMenu {
                                        item?.pricing?.wikiLink?.let { WikiItem(url = it) }
                                        OverflowMenuItem(text = "Add to Needed Items") {
                                            item?.pricing?.addToNeededItemsDialog(this@FleaItemDetail)
                                        }
                                        OverflowMenuItem(text = "Add Price Alert") {
                                            item?.pricing?.addPriceAlertDialog(this@FleaItemDetail)
                                        }
                                    }
                                }
                            ) { finish() }
                        },
                        bottomBar = { FleaBottomNav(selected = selectedNavItem, items) { selectedNavItem = it } },
                        floatingActionButton = {
                            if (UserSettingsModel.showAddToCardButton.value)
                                FloatingActionButton(onClick = {
                                    item?.pricing?.addToCartDialog(this)
                                }) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_baseline_add_shopping_cart_24),
                                        contentDescription = "Add to Shopping Cart",
                                        tint = MaterialTheme.colors.onSecondary
                                    )
                                }
                        },
                        snackbarHost = {
                            SnackbarHost(it) { data ->
                                Snackbar(
                                    backgroundColor = Red400,
                                    snackbarData = data,
                                    contentColor = Color.Black,
                                    actionColor = Color.Black
                                )
                            }
                        },
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            Crossfade(
                                targetState = selectedNavItem,
                                modifier = Modifier.fillMaxHeight()
                            ) {
                                when (it) {
                                    0 -> {
                                        LazyColumn(
                                            contentPadding = PaddingValues(top = 4.dp, bottom = 60.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            item {
                                                if (item?.pricing?.noFlea == true) {
                                                    Card(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(
                                                                horizontal = 8.dp,
                                                                vertical = 4.dp
                                                            ),
                                                        backgroundColor = Red400
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
                                                        ) {
                                                            Icon(
                                                                painter = painterResource(id = R.drawable.icons8_online_store_96),
                                                                contentDescription = "",
                                                                modifier = Modifier.size(24.dp),
                                                                tint = Color.Black
                                                            )
                                                            Text(
                                                                text = "Not Sold on Flea Market",
                                                                modifier = Modifier.padding(start = 16.dp),
                                                                color = Color.Black,
                                                                style = MaterialTheme.typography.subtitle2
                                                            )
                                                        }
                                                    }
                                                }
                                                Card1(item = item)
                                                if (!item?.pricing?.sellFor?.filter { it.price != 0 }.isNullOrEmpty()) TradersSellCard(
                                                    title = "SELL PRICES",
                                                    item = item,
                                                    item?.pricing?.sellFor
                                                )
                                                if (!item?.pricing?.buyFor?.filter { it.price != 0 }.isNullOrEmpty()) TradersBuyCard(
                                                    title = "BUY PRICES",
                                                    item = item,
                                                    item?.pricing?.buyFor,
                                                    fsUser
                                                )
                                                if (parentItems.isNotEmpty()) {
                                                    ParentItems(parents = parentItems)
                                                }
                                                if (item?.pricing?.hasChild() == true) {
                                                    ChildItems(item?.pricing?.containsItem)
                                                }
                                                if (userData?.items?.containsKey(itemID) == true) {
                                                    val needed = userData?.items?.get(itemID)
                                                    if (needed?.hideoutObjective == null && needed?.questObjective == null && needed?.user == null) {
                                                        userRefTracker("items/$itemID").removeValue()
                                                    }

                                                    NeededCard(title = "NEEDED", item = item, needed, tarkovRepo)
                                                }
                                                if (UserSettingsModel.showPriceGraph.value) {
                                                    Card(
                                                        modifier = Modifier
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                            .fillMaxWidth(),
                                                        backgroundColor = Color(0xFE1F1F1F)
                                                    ) {
                                                        Chart()
                                                    }
                                                }
                                                if (!priceAlerts.isNullOrEmpty()) {
                                                    PriceAlertCard(priceAlerts = priceAlerts!!, item)
                                                }
                                            }
                                            item {
                                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                                    Text(
                                                        modifier = Modifier.padding(top = 8.dp),
                                                        text = item?.getUpdatedTime() ?: "",
                                                        style = MaterialTheme.typography.caption,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    1 -> {
                                        BartersPage(item = item, bartersFiltered, userData)
                                    }
                                    2 -> {
                                        CraftsPage(item = item, craftsFiltered, userDataFS)
                                    }
                                    3 -> QuestsPage(item = item, quests, tarkovRepo, userData)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    @Composable
    fun PriceAlertCard(priceAlerts: List<PriceAlert>, item: Item?) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "PRICE ALERTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        item?.pricing?.addPriceAlertDialog(this@FleaItemDetail)
                    }, modifier = Modifier.size(20.dp)) {
                        Icon(painter = painterResource(id = R.drawable.ic_baseline_notification_add_24), contentDescription = "ADD")
                    }
                }

                Column(
                    modifier = Modifier.padding(
                        start = 0.dp,
                        end = 0.dp,
                        top = 4.dp,
                        bottom = 4.dp
                    )
                ) {
                    priceAlerts.forEachIndexed { i, alert ->
                        if (i != 0) {
                            Divider(color = DividerDarkLighter)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = {
                                        if (alert.persistent == true) {
                                            alert.reference
                                                ?.child("enabled")
                                                ?.setValue(alert.enabled != true)
                                        }
                                    },
                                    onLongClick = {
                                        MaterialDialog(this@FleaItemDetail).show {
                                            listItems(items = listOf("Delete")) { dialog, index, text ->
                                                when (text) {
                                                    "Delete" -> {
                                                        alert.reference?.removeValue()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                )
                                .padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 0.dp,
                                    bottom = 0.dp
                                )
                                .defaultMinSize(minHeight = 28.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = alert.getConditionIcon()),
                                contentDescription = "",
                                modifier = Modifier.size(20.dp),
                                tint = White
                            )
                            /*Text(
                                text = alert.getConditionString() ?: "",
                                style = MaterialTheme.typography.subtitle2,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 0.dp)
                            )*/
                            Text(
                                text = alert.price?.toInt()?.asCurrency() ?: "",
                                style = MaterialTheme.typography.subtitle2,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            if (alert.persistent == true) {
                                Switch(checked = alert.enabled ?: false, onCheckedChange = {
                                    alert.reference?.child("enabled")?.setValue(it)
                                })
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ParentItems(parents: List<Item>) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Column {
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
                            text = "PARENT ITEMS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(
                        start = 0.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 12.dp
                    )
                ) {
                    parents.forEach {
                        val childItem = it.pricing
                        Row(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
                                .fillMaxWidth()
                                .clickable {
                                    openActivity(FleaItemDetail::class.java) {
                                        putString("id", childItem?.id)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box {
                                Image(
                                    fadeImagePainter(
                                        childItem?.getCleanIcon()
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(38.dp)
                                        .height(38.dp)
                                        .border((0.25).dp, color = BorderColor)
                                )
                            }
                            Column(
                                modifier = Modifier.padding(start = 16.dp),
                            ) {
                                Text(
                                    text = "${childItem?.name}",
                                    style = MaterialTheme.typography.body1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        SmallBuyPrice(pricing = childItem)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ChildItems(containsItem: List<Pricing.Contains>?) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Column {
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
                            text = "CONTAINS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(
                        start = 0.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 12.dp
                    )
                ) {
                    containsItem?.forEach {
                        val childItem = it.item?.toPricing()
                        Row(
                            modifier = Modifier
                                .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
                                .fillMaxWidth()
                                .clickable {
                                    openActivity(FleaItemDetail::class.java) {
                                        putString("id", childItem?.id)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box {
                                Image(
                                    fadeImagePainter(
                                        childItem?.getCleanIcon()
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .width(38.dp)
                                        .height(38.dp)
                                        .border((0.25).dp, color = BorderColor)
                                )
                                Text(
                                    text = "${it.count}",
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
                                modifier = Modifier.padding(start = 16.dp),
                            ) {
                                Text(
                                    text = "${childItem?.name}",
                                    style = MaterialTheme.typography.body1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                                CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val cheapestBuy = childItem?.getCheapestBuyRequirements()
                                        SmallBuyPrice(pricing = childItem)
                                        Text(
                                            text = " (${(it.count?.times(cheapestBuy?.price ?: 0))?.asCurrency(cheapestBuy?.currency ?: "R")})",
                                            style = MaterialTheme.typography.caption,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Light,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Chip(
        selected: Boolean = false,
        text: String,
        onClick: () -> Unit
    ) {
        Surface(
            color = when {
                selected -> Red400
                else -> Color(0xFF2F2F2F)
            },
            contentColor = when {
                selected -> Color.Black
                else -> Color.White
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    onClick()
                }
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp
            )
        }
    }

    @Composable
    private fun Chart() {
        val benderFont = ResourcesCompat.getFont(this, R.font.bender)
        val ms7D = (604800000).toLong()
        val ms1M = 2629800000
        val ms3M = 7889400000
        val ms6M = 15778800000
        val ms1Y = 31557600000

        var selectedRange by remember {
            mutableStateOf((604800000).toLong())
        }

        Column {
            Row(
                Modifier.padding(
                    bottom = 0.dp,
                    top = 8.dp,
                    start = 16.dp,
                    end = 4.dp
                )
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "PRICE HISTORY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(
                            bottom = 0.dp,
                            top = 8.dp,
                            start = 0.dp,
                            end = 16.dp
                        )
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Chip(
                    text = "1Y",
                    selected = selectedRange == ms1Y
                ) {
                    isPremium {
                        if (it) {
                            selectedRange = ms1Y
                        } else {
                            launchPremiumPusher()
                        }
                    }
                }
                Chip(
                    text = "6M",
                    selected = selectedRange == ms6M
                ) {
                    isPremium {
                        if (it) {
                            selectedRange = ms6M
                        } else {
                            launchPremiumPusher()
                        }
                    }
                }
                Chip(
                    text = "3M",
                    selected = selectedRange == ms3M
                ) {
                    isPremium {
                        if (it) {
                            selectedRange = ms3M
                        } else {
                            launchPremiumPusher()
                        }
                    }
                }
                Chip(
                    text = "1M",
                    selected = selectedRange == ms1M
                ) {
                    isPremium {
                        if (it) {
                            selectedRange = ms1M
                        } else {
                            launchPremiumPusher()
                        }
                    }
                }
                Chip(
                    text = "7D",
                    selected = selectedRange == ms7D
                ) {
                    selectedRange = ms7D
                }
            }

            AndroidView(
                factory = {
                    val chart = LineChart(it)

                    val formatter: ValueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase): String {
                            val simpleDateFormat = SimpleDateFormat("MM/dd")
                            return simpleDateFormat.format(value)
                        }
                    }

                    chart.xAxis.valueFormatter = formatter
                    chart.xAxis.textColor = resources.getColor(R.color.white)
                    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    chart.axisLeft.textColor = resources.getColor(R.color.white)
                    chart.axisRight.isEnabled = false

                    chart.setDrawGridBackground(false)
                    //chart.xAxis.setDrawGridLines(false)
                    chart.xAxis.setCenterAxisLabels(true)
                    //chart.axisLeft.setDrawGridLines(false)
                    //chart.setTouchEnabled(false)
                    chart.isScaleYEnabled = false
                    chart.description.isEnabled = false
                    chart.legend.textColor = resources.getColor(R.color.white)

                    chart.setNoDataText("No price history found.")
                    chart.legend.isEnabled = false

                    //chart.xAxis.setDrawAxisLine(false)

                    //Set fonts
                    chart.legend.typeface = benderFont
                    chart.xAxis.typeface = benderFont
                    chart.axisLeft.typeface = benderFont
                    chart.setNoDataTextTypeface(benderFont)

                    chart.axisLeft.valueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                            val format = DecimalFormat("###,##0")
                            return "${format.format(value)}₽"
                        }
                    }

                    val mv = PriceChartMarkerView(this@FleaItemDetail, R.layout.price_chart_marker_view)
                    chart.marker = mv

                    chart
                }, modifier = Modifier
                    .padding(start = 8.dp, end = 0.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .height(200.dp)
            ) { chart ->
                //if (chart.data != null) return@AndroidView
                fleaFirebase.child("priceHistory/${itemID}").orderByKey().startAt("\"${(System.currentTimeMillis() - selectedRange)}\"").endAt(System.currentTimeMillis().addQuotes())
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (!snapshot.exists()) return
                            val data = snapshot.children.map {
                                val entry = Entry(it.key?.removeSurrounding("\"")?.toFloat() ?: 0f, (it.value as Long).toFloat())
                                entry.data = it
                                entry
                            }

                            val dataSet = LineDataSet(data, "Prices")
                            dataSet.fillColor = resources.getColor(R.color.md_red_400)
                            dataSet.setDrawFilled(true)
                            dataSet.color = resources.getColor(R.color.md_red_400)
                            dataSet.setCircleColor(resources.getColor(R.color.md_red_500))
                            dataSet.valueTypeface = benderFont
                            dataSet.setDrawValues(false)
                            dataSet.setDrawCircles(false)
                            dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

                            val lineData = LineData(dataSet)
                            lineData.setValueTextColor(resources.getColor(R.color.white))

                            chart.data = lineData
                            chart.animateY(250, Easing.EaseOutQuad)
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }
        }
    }

    private fun launchPremiumPusher() {
        launchPremiumPusherResult()
    }

    @Composable
    private fun NeededCard(
        title: String,
        item: Item?,
        needed: User.UNeededItem?,
        tarkovRepo: TarkovRepo
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            Column {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp, start = 16.dp, end = 16.dp)
                    )
                }
                if (needed?.questObjective != null) {
                    needed.questObjective?.forEach {
                        val quest by tarkovRepo.getQuestsWithObjectiveID("%id\":${it.key}%").collectAsState(null)

                        BasicStatRow(
                            title = ("${quest?.trader()?.id} Quest: ${quest?.title ?: "Unknown"}").toUpperCase(),
                            text = "${it.value}",
                            Modifier.clickable {
                                userRefTracker("items/$itemID/questObjective/${it.key}").removeValue()
                            }
                        )
                    }
                }
                if (needed?.hideoutObjective != null) {
                    needed.hideoutObjective?.forEach { hide ->
                        var hideoutModule = hideoutList.hideout?.modules?.find { it?.id == hide.key.removeSurrounding("\"").toInt() }
                        if (hideoutModule == null) {
                            val test = hideoutList.hideout?.modules?.find {
                                it?.require?.find { require ->
                                    require?.id == hide.key.removeSurrounding("\"").toInt()
                                } != null
                            }
                            if (test != null) hideoutModule = test
                        }

                        BasicStatRow(
                            title = hideoutModule.toString().toUpperCase(),
                            text = "${hide.value}",
                            Modifier.clickable {
                                userRefTracker("items/$itemID/hideoutObjective/${hide.key}").removeValue()
                            }
                        )
                    }
                }
                if (needed?.user != null) {
                    needed.user?.forEach { userKey ->
                        val user = userKey.value
                        BasicStatRow(
                            title = (user.reason ?: "User Defined Reason").toUpperCase(),
                            text = "${user.quantity}",
                            Modifier.clickable {
                                userRefTracker("items/$itemID/user/${userKey.key}").removeValue()
                            }
                        )
                    }
                }
                Divider(
                    modifier = Modifier.padding(top = 8.dp),
                    color = DividerDark
                )
                Row(
                    Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "FOUND", style = MaterialTheme.typography.subtitle2)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = {
                        if (needed?.has != 0)
                            userRefTracker("items/$itemID/has").setValue(ServerValue.increment(-1))
                    }) {
                        Icon(painter = painterResource(id = R.drawable.ic_baseline_remove_circle_24), contentDescription = "Minus")
                    }
                    Text(text = "${needed?.has ?: 0}/${needed?.getTotalNeeded()}")
                    IconButton(onClick = {
                        if (needed?.has != needed?.getTotalNeeded()) {
                            userRefTracker("items/$itemID/has").setValue(ServerValue.increment(1))
                        } else {
                            userRefTracker("items/$itemID").removeValue()
                        }
                    }) {
                        if (needed?.has != needed?.getTotalNeeded()) {
                            Icon(painter = painterResource(id = R.drawable.ic_baseline_add_circle_24), contentDescription = "Plus")
                        } else {
                            Icon(painter = painterResource(id = R.drawable.ic_baseline_check_circle_24), contentDescription = "Plus")
                        }
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun QuestsPage(
        item: Item?,
        quests: List<Quest>?,
        tarkovRepo: TarkovRepo,
        userData: User?
    ) {

        LazyColumn(
            contentPadding = PaddingValues(top = 4.dp, bottom = 60.dp),
        ) {
            items(items = quests ?: emptyList()) { quest ->
//            val isRequired = quest.isRequiredForKappa(database).observeAsState().value
//            Text("${quest.title} - ${isRequired == 1}")
                QuestItem(quest = quest, item = item, tarkovRepo)
            }
        }
    }

    @ExperimentalFoundationApi
    @ExperimentalMaterialApi
    @Composable
    private fun QuestItem(
        quest: Quest,
        item: Item?,
        database: TarkovRepo,
    ) {
        val context = LocalContext.current

        val objectiveTypes = quest.objective?.groupBy { it.type }

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = Color(0xFE1F1F1F),
            onClick = {
                context.openActivity(QuestDetailActivity::class.java) {
                    putString("questID", quest.id)
                }
            }
        ) {
            Column(

            ) {
                Row(
                    Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                            Text(
                                text = "LEVEL ${quest.requirement?.level}",
                                style = MaterialTheme.typography.overline
                            )
                        }
                        Text(
                            text = quest.title ?: "",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "${quest.getObjective(item?.id)?.number ?: "Item"} Needed",
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(top = 0.dp)
                        )
                    }
                    Column {
                        Box {
                            Image(
                                rememberImagePainter(quest.trader().icon),
                                contentDescription = null,
                                modifier = Modifier
                                    .width(72.dp)
                                    .height(72.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun CraftsPage(
        item: Item?,
        crafts: List<Craft>,
        userData: FSUser?
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 4.dp, bottom = 60.dp),
        ) {
            items(items = crafts) { craft ->
                CraftItem(craft, userData)
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun CraftItem(craft: Craft, userData: FSUser?) {
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
                    context.openActivity(CraftDetailActivity::class.java) {
                        putSerializable("craft", craft)
                    }
                    /*if (rewardItem?.id != itemID) {
                        context.openActivity(FleaItemDetail::class.java) {
                            putString("id", rewardItem?.id)
                        }
                    }*/
                },
            ) {
                Column {
                    if (userData == null || userData.progress?.isHideoutModuleCompleted(craft.getSourceID(hideoutList.hideout) ?: 0) == true) {

                    } else {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(color = Red400)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.BuildCircle, contentDescription = "", tint = Color.Black, modifier = Modifier
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
                    }

                    Row(
                        Modifier
                            .padding(16.dp)
                            .height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Image(
                                fadeImagePainter(
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
                            Timber.d(rewardItem.toString())
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
                    requiredItems?.forEach { item ->
                        item?.item?.let { pricing ->
                            CompactItem(
                                item = pricing, extras = CraftDetailActivity.ItemSubtitle(
                                    iconText = item.count?.toString(),
                                    showPriceInSubtitle = true,
                                    subtitle = " (${(item.count?.times(pricing.getCheapestBuyRequirements().price ?: 0))?.asCurrency()})"
                                )
                            )
                        }
                        //BarterCraftCostItem(taskItem)
                    }
                    Divider(
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
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

    @ExperimentalMaterialApi
    @Composable
    private fun BartersPage(
        item: Item?,
        barters: List<Barter>,
        userData: User?
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 4.dp, bottom = 60.dp),
        ) {
            items(items = barters ?: emptyList()) { barter ->
                BarterItem(barter, item)
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun BarterItem(
        barter: Barter,
        item: Item?
    ) {
        val rewardItem = barter.rewardItems?.firstOrNull()?.item
        val requiredItems = barter.requiredItems

        val context = LocalContext.current

        val isChildOfReward = item?.isChildOf(rewardItem) ?: false

        Card(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            backgroundColor = Color(0xFE1F1F1F),
            onClick = {
                context.openActivity(BarterDetailActivity::class.java) {
                    putSerializable("barter", barter)
                }
                /*if (rewardItem?.id != itemID) {
                    context.openActivity(FleaItemDetail::class.java) {
                        putString("id", rewardItem?.id)
                    }
                }*/
            }
        ) {
            Column {
                if (isChildOfReward) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(color = Blue400)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.SubdirectoryArrowRight, contentDescription = "", tint = Color.Black, modifier = Modifier
                                .height(20.dp)
                                .width(20.dp)
                        )
                        Text(
                            text = "YOU WILL RECEIVE ${item?.pricing?.shortName?.uppercase()} AS A MOD ON ${rewardItem?.shortName?.uppercase()}",
                            style = MaterialTheme.typography.caption,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 34.dp)
                        )
                    }
                }
                Row(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        fadeImagePainter(rewardItem?.getCleanIcon()),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .width(38.dp)
                            .height(38.dp)
                            .border((0.25).dp, color = BorderColor)
                    )
                    Column(
                        Modifier
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
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
                                text = barter.source ?: "",
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Light,
                            )
                        }
                        CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                            val highestSell = rewardItem?.getHighestSell()

                            Text(
                                text = "${highestSell?.getPriceAsCurrency()} @ ${highestSell?.getTitle()}",
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
                requiredItems?.forEach { item ->
                    item?.item?.let { pricing ->
                        CompactItem(
                            item = pricing, extras = CraftDetailActivity.ItemSubtitle(
                                iconText = item.count?.toString(),
                                showPriceInSubtitle = true,
                                subtitle = " (${(item.count?.times(pricing.getCheapestBuyRequirements().price ?: 0))?.asCurrency()})"
                            )
                        )
                    }
                    //BarterCraftCostItem(taskItem)
                }
                Divider(
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                    color = Color(0x1F000000)
                )
                AvgPriceRow(title = "COST", price = barter.totalCost())
                SavingsRow(title = "ESTIMATED SAVINGS", price = barter.estimatedProfit())
                Spacer(modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }

    @Composable
    private fun BarterCraftCostItem(taskItem: Craft.CraftItem?) {
        val item = taskItem?.item
        val context = LocalContext.current
        val cheapestBuy = item?.getCheapestBuyRequirements()

        Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
                .fillMaxWidth()
                .clickable {
                    if (item?.id != itemID) {
                        context.openActivity(FleaItemDetail::class.java) {
                            putString("id", item?.id)
                        }
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box {
                Image(
                    fadeImagePainter(
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

    @Composable
    fun FleaBottomNav(
        selected: Int,
        items: List<NavItem>,
        onItemSelected: (Int) -> Unit
    ) {

        BottomNavigation(
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            items.forEachIndexed { index, item ->
                BottomNavigationItem(
                    icon = { Icon(painter = painterResource(id = item.icon), contentDescription = null) },
                    label = { Text(item.title) },
                    selected = selected == index,
                    onClick = { onItemSelected(index) },
                    selectedContentColor = MaterialTheme.colors.secondary,
                    unselectedContentColor = if (item.enabled == true) Color(0x99FFFFFF) else Color(0x33FFFFFF),
                    enabled = item.enabled ?: true,
                )
            }
        }
    }

    @Composable
    fun FleaDetailToolbar(
        modifier: Modifier = Modifier,
        item: Item?,
        actions: @Composable (RowScope.() -> Unit) = {},
        onNavIconPressed: () -> Unit = { },
    ) {
        TopAppBar(
            modifier = modifier,
            title = {
                Column {
                    Text(
                        text = item?.Name ?: "The Hideout",
                        color = MaterialTheme.colors.onPrimary,
                        style = MaterialTheme.typography.h6,
                        maxLines = 1,
                        fontSize = 18.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "(${item?.ShortName})",
                        color = MaterialTheme.colors.onPrimary,
                        style = MaterialTheme.typography.caption,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            backgroundColor = MaterialTheme.colors.primary,
            elevation = 0.dp,
            navigationIcon = {
                IconButton(onClick = onNavIconPressed) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                }
            },
            actions = actions
        )
    }

    @SuppressLint("CheckResult")
    @Composable
    private fun Card1(
        item: Item?
    ) {
        val context = LocalContext.current
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
        val iconDisplay = UserSettingsModel.fleaIconDisplay.value

        val icon = when (iconDisplay) {
            IconSelection.ORIGINAL -> item?.pricing?.getCleanIcon()
            IconSelection.TRANSPARENT -> item?.pricing?.getTransparentIcon()
            IconSelection.GAME -> item?.pricing?.getIcon()
        }

        val border = when (iconDisplay) {
            IconSelection.ORIGINAL -> BorderColor
            IconSelection.TRANSPARENT -> Color.Unspecified
            IconSelection.GAME -> Color.Unspecified
        }

        Card(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            Row(
                //Modifier.height(IntrinsicSize.Max)
            ) {
                //Rectangle(color = color, modifier = Modifier.fillMaxHeight())
                Column(Modifier.padding(bottom = 8.dp)) {
                    Row(
                        Modifier
                            .padding(start = 16.dp, end = 16.dp),
                        //.height(IntrinsicSize.Max),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            fadeImagePainter(icon),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .width(52.dp)
                                .height(52.dp)
                                .border((0.25).dp, color = border)
                                .clickable {
                                    item?.pricing?.imageLink?.let {
                                        StfalconImageViewer
                                            .Builder(
                                                context,
                                                listOf(it)
                                            ) { view, image ->
                                                Glide
                                                    .with(view)
                                                    .load(image)
                                                    .into(view)
                                            }
                                            .withHiddenStatusBar(false)
                                            .withBackgroundColor(color.toArgb())
                                            .show()
                                    }
                                }
                        )
                        Column(
                            Modifier
                                .padding(horizontal = 16.dp)
                                .weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Last Price: ${item?.pricing?.getLastPrice()?.asCurrency()}",
                                style = MaterialTheme.typography.subtitle1,
                                fontSize = 16.sp
                            )
                            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                Text(
                                    text = "${
                                        item?.getPricePerSlot()?.asCurrency()
                                    }/slot • ${item?.getTotalSlots()} Slots (${item?.Width}x${item?.Height})",
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Light,
                                )
                            }
                            Row {
                                CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                    Text(
                                        text = "Last 48h: ",
                                        style = MaterialTheme.typography.caption,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Light
                                    )
                                    Text(
                                        text = "${item?.pricing?.changeLast48h}%",
                                        style = MaterialTheme.typography.caption,
                                        color = if (item?.pricing?.changeLast48h ?: 0.0 > 0.0) Green500 else if (item?.pricing?.changeLast48h ?: 0.0 < 0.0) Red500 else Color.Unspecified,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Light
                                    )
                                }
                            }
                        }

                        if (item?.pricing?.types?.contains(ItemType.ammo) == true) {
                            FloatingActionButton(
                                onClick = {
                                    this@FleaItemDetail.openAmmunitionDetail(itemID)
                                },
                                //backgroundColor = DarkPrimary,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icons8_ammo_100),
                                    contentDescription = "Ammo",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        if (item?.itemType == ItemTypes.WEAPON) {
                            FloatingActionButton(
                                onClick = {
                                    this@FleaItemDetail.openWeaponDetail(itemID)
                                },
                                //backgroundColor = DarkPrimary,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icons8_assault_rifle_100),
                                    contentDescription = "Weapon",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        if (item?.itemType == ItemTypes.MOD) {
                            FloatingActionButton(
                                onClick = {
                                    this@FleaItemDetail.openModDetail(itemID)
                                },
                                //backgroundColor = DarkPrimary,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icons8_assault_rifle_mod_96),
                                    contentDescription = "Mod",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        if (item?.itemType == ItemTypes.ARMOR) {
                            FloatingActionButton(
                                onClick = {
                                    this@FleaItemDetail.openActivity(GearDetailActivity::class.java) {
                                        putString("id", itemID)
                                    }
                                },
                                //backgroundColor = DarkPrimary,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icons8_bulletproof_vest_100),
                                    contentDescription = "Armor",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    Divider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = DividerDark
                    )
                    AvgPriceRow(title = "LOW 24H PRICE", price = item?.pricing?.low24hPrice)
                    AvgPriceRow(title = "AVG 24H PRICE", price = item?.pricing?.avg24hPrice)
                    AvgPriceRow(title = "HIGH 24H PRICE", price = item?.pricing?.high24hPrice)
                    SavingsRow(title = "INSTA PROFIT", price = item?.pricing?.getInstaProfit(), modifier = Modifier.padding(bottom = 8.dp))
                    Divider(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = DividerDark
                    )
                    BasicStatRow(title = "WEIGHT", text = item?.getFormattedWeight())
                    if (item?.pricing?.types?.contains(ItemType.container) == true) {
                        Divider(
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                            color = DividerDark
                        )
                        BasicStatRow(title = "CAPACITY", text = "${item.getTotalInternalSize()} Slots")
                        BasicStatRow(title = "PRICE/SLOT", text = "${(item.getPrice() / item.getTotalInternalSize()).asCurrency()}/slot")
                    }
                }
            }
        }
    }


    @Composable
    private fun TradersSellCard(
        title: String,
        item: Item?,
        prices: List<Pricing.BuySellPrice>?
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(bottom = 8.dp, top = 16.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        showFeeDialog(item)
                    }, modifier = Modifier.size(20.dp)) {
                        Icon(painter = painterResource(id = R.drawable.ic_baseline_calculate_24), contentDescription = "FEES")
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    itemsIndexed(prices?.filter { it.price != 0 }?.sortedByDescending { it.price }
                        ?: emptyList()) { index, item ->
                        TraderPriceListGridItem(item, index == 0, false, null)
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
        prices: List<Pricing.BuySellPrice>?,
        fsUser: LiveData<FSUser?>
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            Column {
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
                    itemsIndexed(prices?.filter { it.price != 0 }?.sortedBy { it.price } ?: emptyList()) { index, item ->
                        val isUnlocked = if (item.isQuestLocked()) {
                            val questID = item.requirements.find { it.type == "questCompleted" }?.value
                            fsUser.value?.progress?.isQuestCompleted(questID.toString()) == true
                        } else {
                            item.isRequirementMet()
                        }

                        TraderPriceListGridItem(item, if (index == 0 && isUnlocked) true else index == 1, true, fsUser.value)
                    }
                }
            }
        }
    }

    @Composable
    private fun TraderPriceListGridItem(
        item: Pricing.BuySellPrice,
        isHighest: Boolean = false,
        isBuy: Boolean,
        fsUser: FSUser?
    ) {
        val scope = rememberCoroutineScope()

        val isQuestLocked = if (item.isQuestLocked()) {
            val questID = item.requirements.find { it.type == "questCompleted" }?.value
            fsUser?.progress?.isQuestCompleted(questID.toString()) == false
        } else {
            false
        }

        val isUnlocked = if (!isBuy) true else if (item.isQuestLocked()) {
            !isQuestLocked
        } else {
            item.isRequirementMet()
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable(interactionSource = MutableInteractionSource(), indication = null, enabled = isQuestLocked, onClick = {
                scope.launch {
                    val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                        message = "Item is quest locked.",
                        actionLabel = "OPEN QUEST"
                    )
                    when (snackbarResult) {
                        SnackbarResult.ActionPerformed -> openQuestDetail(item.requirements.find { it.type == "questCompleted" }?.value.toString())
                        else -> {}
                    }
                }
            })
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .padding(bottom = 8.dp)
            ) {
                Image(
                    modifier = Modifier.size(72.dp),
                    painter = fadeImagePainter(url = item.traderImage()),
                    contentDescription = "Prapor",
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isQuestLocked) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = "",
                        modifier = Modifier
                            .size(14.dp)
                            .padding(end = 2.dp),
                        tint = Red400
                    )
                }

                Text(
                    text = if (item.source == ItemSourceName.peacekeeper.rawValue) {
                        if (isBuy) {
                            item.price?.asCurrency("D") ?: ""
                        } else {
                            item.price?.roubleToDollar()?.asCurrency("D") ?: ""
                        }
                    } else {
                        item.price?.asCurrency() ?: ""
                    },
                    style = MaterialTheme.typography.body1,
                    fontSize = 12.sp,
                    fontWeight = if (isHighest) FontWeight.Bold else FontWeight.Normal,
                    color = if (!isUnlocked) Red400 else Color.Unspecified
                )
            }

        }
    }

    @SuppressLint("CheckResult")
    private fun showFeeDialog(item: Item?) {
        var intel3 = fsUser.value?.progress?.isHideoutModuleCompleted(17) ?: false
        MaterialDialog(this@FleaItemDetail).show {
            title(text = "Fee Calculator")
            message(
                text = "Listing Fee: ${item?.pricing?.calculateTax(intel = intel3)?.asCurrency()}\nProfit: ${
                    (item?.pricing?.lastLowPrice?.minus(item.pricing?.calculateTax(intel = intel3) ?: 0)?.asCurrency())
                }"
            )
            checkBoxPrompt(text = "Intel Center 3", isCheckedDefault = intel3) {
                intel3 = it
                try {
                    val price = this.getInputField().text.toString().toLong()
                    val tax = item?.pricing?.calculateTax(price, it) ?: 0
                    this.message(text = "Listing Fee: ${tax.asCurrency()}\nProfit: ${(price - tax).toInt().asCurrency()}")
                } catch (e: Exception) {

                }
            }
            input(prefill = item?.pricing?.lastLowPrice.toString(), hint = "List Price", inputType = InputType.TYPE_CLASS_NUMBER, waitForPositiveButton = false) { materialDialog, charSequence ->
                try {
                    val price = charSequence.toString().toLong()
                    val tax = item?.pricing?.calculateTax(price, intel3) ?: 0
                    materialDialog.message(text = "Listing Fee: ${tax.asCurrency()}\nProfit: ${(price - tax).toInt().asCurrency()}")
                } catch (e: Exception) {

                }
            }
        }
    }

    data class NavItem(
        val title: String,
        @DrawableRes val icon: Int,
        val enabled: Boolean? = true
    )
}

@Composable
fun SavingsRow(
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

@Composable
fun AvgPriceRow(
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
            text = price?.asCurrency() ?: "-",
            style = MaterialTheme.typography.body1,
            fontSize = 14.sp
        )
    }
}

@Composable
fun BasicStatRow(
    title: String,
    text: String?,
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
            text = text ?: "",
            style = MaterialTheme.typography.body1,
            fontSize = 14.sp
        )
    }
}