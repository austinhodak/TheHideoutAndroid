package com.austinhodak.thehideout.flea_market.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.checkbox.checkBoxPrompt
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.*
import com.austinhodak.tarkovapi.type.ItemSourceName
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.*
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.hideoutList
import com.austinhodak.thehideout.questPrefs
import com.austinhodak.thehideout.quests.QuestDetailActivity
import com.austinhodak.thehideout.utils.*
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.accompanist.glide.rememberGlidePainter
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.stfalcon.imageviewer.StfalconImageViewer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlin.math.roundToLong

@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@AndroidEntryPoint
class FleaItemDetail : GodActivity() {

    private val viewModel: FleaViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo
    private lateinit var itemID: String

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        //WindowCompat.setDecorFitsSystemWindows(window, false)

        itemID = intent.getStringExtra("id") ?: "5447a9cd4bdc2dbd208b4567"
        viewModel.getItemByID(itemID)

        setContent {
            ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {

                HideoutTheme {
                    val scaffoldState = rememberScaffoldState()
                    var selectedNavItem by remember { mutableStateOf(0) }

                    val item = viewModel.item.observeAsState()

                    val quests by tarkovRepo.getQuestsWithItemID("%${item.value?.id}%").collectAsState(emptyList())
                    val barters by tarkovRepo.getBartersWithItemID("%${item.value?.id}%").collectAsState(emptyList())
                    val crafts by tarkovRepo.getCraftsWithItemID("%${item.value?.id}%").collectAsState(emptyList())

                    val userData by viewModel.userData.observeAsState()

                    val items = listOf(
                        NavItem("Item", R.drawable.ic_baseline_storefront_24),
                        NavItem("Barters", R.drawable.ic_baseline_compare_arrows_24, barters.isNotEmpty()),
                        NavItem("Crafts", R.drawable.ic_baseline_handyman_24, crafts.isNotEmpty()),
                        NavItem("Quests", R.drawable.ic_baseline_assignment_24, quests.isNotEmpty())
                    )

                    var isFavorited by remember {
                        mutableStateOf(questPrefs.favoriteItems?.contains(itemID))
                    }

                    questPrefs.preference.registerOnSharedPreferenceChangeListener { sharedPreferences, s ->
                        if (s == "FAVORITE_ITEMS") {
                            isFavorited = questPrefs.favoriteItems?.contains(itemID)
                        }
                    }
                    
                    rememberSystemUiController().setNavigationBarColor(DarkPrimary)

                    Scaffold(
                        scaffoldState = scaffoldState,
                        topBar = {
                            FleaDetailToolbar(
                                item = item.value,
                                actions = {
                                    /*IconButton(onClick = {}) {
                                        Icon(painter = painterResource(id = R.drawable.icons8_add_list_96), contentDescription = "Add to List")
                                    }*/
                                    IconButton(onClick = {
                                        isFavorited = if (isFavorited == true) {
                                            questPrefs.removeFavorite(itemID)
                                            false
                                        } else {
                                            questPrefs.addFavorite(itemID)
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
                                        item.value?.pricing?.wikiLink?.let { WikiItem(url = it) }
                                        OverflowMenuItem(text = "Add to Needed Items") {
                                            item.value?.pricing?.addToNeededItemsDialog(this@FleaItemDetail)
                                        }
                                    }
                                }
                            ) { finish() }
                        },
                        bottomBar = { FleaBottomNav(selected = selectedNavItem, items) { selectedNavItem = it } },
                        floatingActionButton = {
                            //if (isDebug())
                                FloatingActionButton(onClick = {
                                    item.value?.pricing?.addToCartDialog(this)
                                }) {
                                    Icon(
                                        painterResource(id = R.drawable.ic_baseline_add_shopping_cart_24),
                                        contentDescription = "Add to Shopping Cart",
                                        tint = MaterialTheme.colors.onSecondary
                                    )
                                }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            Crossfade(targetState = selectedNavItem) {
                                when (it) {
                                    0 -> {
                                        LazyColumn(
                                            contentPadding = PaddingValues(top = 4.dp, bottom = 60.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            item {
                                                Card1(item = item.value)
                                                if (!item.value?.pricing?.sellFor?.filter { it.price != 0 }.isNullOrEmpty()) TradersSellCard(
                                                    title = "SELL PRICES",
                                                    item = item.value,
                                                    item.value?.pricing?.sellFor
                                                )
                                                if (!item.value?.pricing?.buyFor?.filter { it.price != 0 }.isNullOrEmpty()) TradersBuyCard(
                                                    title = "BUY PRICES",
                                                    item = item.value,
                                                    item.value?.pricing?.buyFor
                                                )
                                                if (userData?.items?.containsKey(itemID) == true) {
                                                    val needed = userData?.items?.get(itemID)
                                                    if (needed?.hideoutObjective == null && needed?.questObjective == null && needed?.user == null) {
                                                        userRefTracker("items/$itemID").removeValue()
                                                    }

                                                    NeededCard(title = "NEEDED", item = item.value, needed, tarkovRepo)
                                                }
                                                Card(
                                                    modifier = Modifier
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        .fillMaxWidth(),
                                                    backgroundColor = Color(0xFE1F1F1F)
                                                ) {
                                                    Chart()
                                                }
                                                //FleaFeeCalc(item.value)
                                            }
                                            item {
                                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                                    Text(
                                                        modifier = Modifier.padding(top = 8.dp),
                                                        text = item.value?.getUpdatedTime() ?: "",
                                                        style = MaterialTheme.typography.caption,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    1 -> {
                                        BartersPage(item = item.value, barters, userData)
                                    }
                                    2 -> {
                                        CraftsPage(item = item.value, crafts, userData)
                                    }
                                    3 -> QuestsPage(item = item.value, quests, tarkovRepo, userData)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Chart() {
        val benderFont = ResourcesCompat.getFont(this, R.font.bender)

        Column {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = "PRICE HISTORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = Bender,
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
                )
            }

            AndroidView(factory = {
                val chart = LineChart(it)

                val formatter: ValueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase): String {
                        val simpleDateFormat = SimpleDateFormat("MM/dd")
                        val dateString = simpleDateFormat.format(value)
                        return dateString
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
                chart.description.isEnabled = false
                chart.legend.textColor = resources.getColor(R.color.white)

                chart.setNoDataText("No price history found.")
                chart.legend.isEnabled = false

                //chart.xAxis.setDrawAxisLine(false)

                //Set fonts
                chart.legend.typeface = benderFont
                chart.xAxis.typeface = benderFont
                chart.axisLeft.typeface = benderFont

                chart.axisLeft.valueFormatter = object : ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                        val format = DecimalFormat("###,##0")

                        return "${format.format(value)}₽"
                    }
                }

                //chart.data = lineData
                //chart.invalidate()
                chart
            },  modifier = Modifier
                .padding(start = 8.dp, end = 4.dp, bottom = 16.dp)
                .fillMaxWidth()
                .height(200.dp)
            ) { chart ->
                fleaFirebase.child("items/${itemID}/").orderByKey().limitToLast(20).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.children.map {
                            Entry(it.key?.toFloat() ?: 0f, (it.value as Long).toFloat())
                        }

                        val dataSet = LineDataSet(data, "Prices")
                        dataSet.fillColor = resources.getColor(R.color.pastel_orange)
                        dataSet.setDrawFilled(true)
                        dataSet.color = resources.getColor(R.color.pastel_orange)
                        dataSet.setCircleColor(resources.getColor(R.color.md_orange_400))
                        dataSet.valueTypeface = benderFont
                        dataSet.setDrawValues(false)


                        val lineData = LineData(dataSet)
                        lineData.setValueTextColor(resources.getColor(R.color.white))

                        chart.data = lineData
                        chart.invalidate()
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }
        }
    }

    @Composable
    private fun FleaFeeCalc(
        item: Item?
    ) {
        var fee by remember { mutableStateOf(getTax(item?.pricing?.avg24hPrice?.toString(), item)) }
        var text by remember { mutableStateOf(item?.pricing?.avg24hPrice?.toString()) }

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            Column {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "FLEA MARKET FEE CALCULATOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 8.dp, top = 16.dp, start = 16.dp, end = 16.dp)
                    )
                }
                OutlinedTextField(
                    value = text ?: "",
                    onValueChange = {
                        fee = getTax(it, item)
                        text = it
                    },
                    label = {
                        Text("Sale Price")
                    },
                    leadingIcon = {
                        Icon(painter = painterResource(id = R.drawable.ic_ruble_currency_sign), contentDescription = "Ruble")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )
            }

        }
    }

    private fun getTax(value: String?, item: Item?): Int? {
        if (value.isNullOrBlank()) return 0
        val price = value.replace("[^0-9]".toRegex(), "").toLong()
        return item?.pricing?.calculateTax(price)
    }

    private fun formatPriceString(value: Int?): String {
        val formatter = DecimalFormat("#,###,###")
        return formatter.format(value)
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
            contentPadding = PaddingValues(vertical = 4.dp)
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
                            text = "${quest.getObjective(item?.id)?.number ?: "Key"} Needed",
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
        userData: User?
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(items = crafts) { craft ->
                CraftItem(craft, userData)
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    fun CraftItem(craft: Craft, userData: User?) {
        val rewardItem = craft.rewardItems?.firstOrNull()?.item
        val reward = craft.rewardItems?.firstOrNull()
        val requiredItems = craft.requiredItems
        val context = LocalContext.current

        val alpha = if (userData == null || userData.isHideoutModuleComplete(craft.getSourceID(hideoutList.hideout) ?: 0)) {
            ContentAlpha.high
        } else {
            ContentAlpha.high
        }

        CompositionLocalProvider(LocalContentAlpha provides alpha) {
            Card(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                backgroundColor = Color(0xFE1F1F1F),
                onClick = {
                    if (rewardItem?.id != itemID) {
                        context.openActivity(FleaItemDetail::class.java) {
                            putString("id", rewardItem?.id)
                        }
                    }
                },
            ) {
                Column {
                    if (userData == null || userData.isHideoutModuleComplete(craft.getSourceID(hideoutList.hideout) ?: 0)) {

                    } else {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(color = Red400)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Warning, contentDescription = "", tint = Color.Black, modifier = Modifier
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
                                rememberImagePainter(
                                    rewardItem?.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
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
    }

    @ExperimentalMaterialApi
    @Composable
    private fun BartersPage(
        item: Item?,
        barters: List<Barter>,
        userData: User?
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(items = barters ?: emptyList()) { barter ->
                BarterItem(barter)
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun BarterItem(
        barter: Barter
    ) {
        val rewardItem = barter.rewardItems?.firstOrNull()?.item
        val requiredItems = barter.requiredItems

        val context = LocalContext.current

        Card(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            backgroundColor = Color(0xFE1F1F1F),
            onClick = {
                if (rewardItem?.id != itemID) {
                    context.openActivity(FleaItemDetail::class.java) {
                        putString("id", rewardItem?.id)
                    }
                }
            }
        ) {
            Column {
                Row(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        rememberImagePainter(data = rewardItem?.iconLink
                            ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"),
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
                    rememberImagePainter(
                        item?.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"
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

        Card(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            Row(
                Modifier.height(IntrinsicSize.Max)
            ) {
                Rectangle(color = color, modifier = Modifier.fillMaxHeight())
                Column(Modifier.padding(bottom = 8.dp)) {
                    Row(
                        Modifier
                            .padding(start = 16.dp, end = 16.dp)
                            .height(IntrinsicSize.Min),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            rememberImagePainter(item?.pricing?.iconLink ?: ""),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .width(52.dp)
                                .height(52.dp)
                                .border((0.25).dp, color = BorderColor)
                                .clickable {
                                    StfalconImageViewer
                                        .Builder(
                                            context,
                                            listOf(item?.pricing?.imageLink)
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
                        )
                        Column(
                            Modifier
                                .padding(horizontal = 16.dp)
                                .weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            /*Text(
                            text = "${item?.Name}",
                            style = MaterialTheme.typography.h6,
                            //fontSize = 16.sp
                        )*/
                            Text(
                                text = "Last Price: ${item?.pricing?.lastLowPrice?.asCurrency()}",
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
                    BasicStatRow(title = "WEIGHT", text = "${item?.Weight}kg")
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
                        TraderPriceListGridItem(item, index == 0, false)
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
        prices: List<Pricing.BuySellPrice>?
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
                    itemsIndexed(prices?.filter { it.price != 0 }?.sortedBy { it.price }
                        ?: emptyList()) { index, item ->
                        TraderPriceListGridItem(item, index == 0, true)
                    }
                }
                /*prices?.sortedByDescending { it.fragments.itemPrice.price }?.forEachIndexed { index, item ->
                TraderPriceListItem(item.fragments.itemPrice, index == 0)
            }*/
            }
        }
    }

    @Composable
    private fun TraderPriceListGridItem(
        item: Pricing.BuySellPrice,
        isHighest: Boolean = false,
        isBuy: Boolean
    ) {
        val isUnlocked = if (isBuy) {
            item.isRequirementMet()
        } else {
            true
        }
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

    @SuppressLint("CheckResult")
    private fun showFeeDialog(item: Item?) {
        var intel3 = viewModel.userData.value?.isHideoutModuleComplete(17) ?: false
        MaterialDialog(this@FleaItemDetail).show {
            title(text = "Fee Calculator")
            message(text = "Listing Fee: ${item?.pricing?.calculateTax(intel = intel3)?.asCurrency()}\nProfit: ${(item?.pricing?.lastLowPrice?.minus(item.pricing?.calculateTax(intel = intel3) ?: 0)?.asCurrency())}")
            checkBoxPrompt(text = "Intel Center 3", isCheckedDefault = intel3) {
                intel3 = it
                try {
                    val price = this.getInputField().text.toString().toLong()
                    val tax = item?.pricing?.calculateTax(price, it) ?: 0
                    this.message(text = "Listing Fee: ${tax.asCurrency()}\nProfit: ${(price-tax).toInt().asCurrency()}")
                } catch (e: Exception) {

                }
            }
            input (prefill = item?.pricing?.lastLowPrice.toString(), hint = "List Price", inputType = InputType.TYPE_CLASS_NUMBER, waitForPositiveButton = false) { materialDialog, charSequence ->
                try {
                    val price = charSequence.toString().toLong()
                    val tax = item?.pricing?.calculateTax(price, intel3) ?: 0
                    materialDialog.message(text = "Listing Fee: ${tax.asCurrency()}\nProfit: ${(price-tax).toInt().asCurrency()}")
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