package com.austinhodak.thehideout.ammunition

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.models.AmmoBallistic
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.plusMinus
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel
import com.austinhodak.thehideout.ballistics
import com.austinhodak.thehideout.calculator.CalculatorHelper
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.components.OverflowMenu
import com.austinhodak.thehideout.compose.components.OverflowMenuItem
import com.austinhodak.thehideout.compose.components.WikiItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.pickers.PickerActivity
import com.austinhodak.thehideout.utils.*
import com.bumptech.glide.Glide
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.stfalcon.imageviewer.StfalconImageViewer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import kotlin.math.roundToInt

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class AmmoDetailActivity : GodActivity() {

    private val ammoViewModel: AmmoViewModel by viewModels()

    private var armorPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                intent?.getSerializableExtra("item")?.let {
                    if (it is Item) {
                        ammoViewModel.selectArmor(it)
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val ammoID = intent.getStringExtra("ammoID") ?: "5fd20ff893a8961fc660a954"
        ammoViewModel.getAmmo(ammoID)

        Firebase.crashlytics.setCustomKey("ammoID", ammoID)

        val ballisticsData = ballistics.getAmmo(ammoID)
        Timber.d(ballisticsData.toString())

        setContent {
            val ammo by ammoViewModel.ammoDetails.observeAsState()
            val scaffoldState = rememberScaffoldState()

            val selectedArmor by ammoViewModel.selectedArmor.observeAsState()
            val navController = rememberNavController()

            val fabOffsetHeightPx = remember { mutableStateOf(1f) }
            val nestedScrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {

                        val delta = available.y
                        val newOffset = fabOffsetHeightPx.value + (delta / 100)
                        fabOffsetHeightPx.value = newOffset

                        return Offset.Zero
                    }
                }
            }

            HideoutTheme {
                val systemUiController = rememberSystemUiController()
                systemUiController.setSystemBarsColor(
                    color = MaterialTheme.colors.primary,
                )

                Scaffold(
                    modifier = Modifier
                        .nestedScroll(nestedScrollConnection),
                    scaffoldState = scaffoldState,
                    topBar = {
                        Column {
                            AmmoDetailToolbar(
                                title = ammo?.pricing?.name ?: "Error Loading...",
                                onBackPressed = { finish() },
                                actions = {
                                    OverflowMenu {
                                        ammo?.pricing?.wikiLink?.let { WikiItem(url = it) }
                                        OverflowMenuItem(text = "Add to Cart") {
                                            ammo?.pricing?.addToCartDialog(this@AmmoDetailActivity)
                                        }
                                    }
                                }
                            )
                            if (ammo == null) {
                                LinearProgressIndicator(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(2.dp),
                                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                    backgroundColor = Color.Transparent
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            openActivity(CalculatorMainActivity::class.java) {
                                putSerializable("ammo", ammo)
                            }
                        }, modifier = Modifier
                                .alpha(
                                    fabOffsetHeightPx.value
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_calculate_24),
                                contentDescription = "Open Calculator",
                                tint = Color.Black
                            )
                        }
                    },
                    bottomBar = {
                        AmmoBottomNav(navController = navController)
                    },
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavigationScreens.Info.route
                    ) {
                        composable(BottomNavigationScreens.Info.route) {
                            ammo?.let {
                                LazyColumn(
                                    contentPadding = PaddingValues(
                                        top = 4.dp,
                                        start = 8.dp,
                                        end = 8.dp,
                                        bottom = 64.dp
                                    )
                                ) {
                                    item {
                                        AmmoDetailCard(ammo = ammo!!)
                                    }
                                    if (ammo?.pricing?.buyFor?.isNotEmpty() == true) {
                                        item {
                                            PricingCard(pricing = ammo?.pricing!!)
                                        }
                                    }
                                    item {
                                        ArmorPenCard(ammo = ammo!!, selectedArmor)
                                    }
                                }
                            }
                        }
                        composable(BottomNavigationScreens.Table.route) {
                            TableScreen(
                                ammo,
                                ballisticsData
                            )
                        }
                        composable(BottomNavigationScreens.Charts.route) {
                            ChartsScreen(
                                ammo,
                                ballisticsData
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TableScreen(ammo: Ammo?, ballisticsData: AmmoBallistic?) {
        LazyColumn(
            contentPadding = PaddingValues(top = 4.dp, bottom = 64.dp)
        ) {
            ballisticsData?.ballistics?.sortedBy { it.range.toInt() }?.forEach {
                item {
                    BallisticCard(it)
                }
            }
        }
    }

    @Composable
    private fun BallisticCard(ballistics: AmmoBallistic.Ballistics) {
        Card(
            Modifier
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column(
                Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "${ballistics.range} M",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                Column {
                    BallisticLine(title = "VELOCITY", entry = "${ballistics.velocity} m/s")
                    BallisticLine(title = "DAMAGE", entry = "${ballistics.damage}")
                    BallisticLine(title = "PEN POWER", entry = "${ballistics.penetration}")
                    BallisticLine(title = "DROP", entry = "${ballistics.drop} cm")
                    BallisticLine(title = "TIME OF FLIGHT", entry = "${ballistics.tof} s")
                }
            }
        }
    }

    @Composable
    private fun BallisticLine(
        modifier: Modifier = Modifier,
        title: String,
        entry: String
    ) {
        Row(
            modifier = modifier.padding(horizontal = 0.dp, vertical = 4.dp),
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
                text = entry,
                style = MaterialTheme.typography.body1,
                fontSize = 14.sp
            )
        }
    }

    @Composable
    private fun ChartsScreen(ammo: Ammo?, ballisticsData: AmmoBallistic?) {
        LazyColumn(
            contentPadding = PaddingValues(top = 4.dp, bottom = 64.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    backgroundColor = Color(0xFE1F1F1F)
                ) {
                    TrajectoryChart(ballisticsData)
                }
                Card(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    backgroundColor = Color(0xFE1F1F1F)
                ) {
                    VelocityChart(ballisticsData)
                }
                /*Card(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxWidth(),
                    backgroundColor = Color(0xFE1F1F1F)
                ) {
                    DamageChart(ballisticsData)
                }*/
            }
        }
    }

    @Composable
    private fun TrajectoryChart(ballisticsData: AmmoBallistic?) {
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
                        text = "TRAJECTORY",
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
            }

            val benderFont = ResourcesCompat.getFont(this@AmmoDetailActivity, R.font.bender)
            AndroidView(
                factory = {
                    val chart = LineChart(it)

                    val formatter: ValueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase): String {
                            return "${value.toInt()} m"
                        }
                    }

                    chart.axisLeft.valueFormatter = formatter
                    chart.xAxis.valueFormatter = formatter
                    chart.xAxis.textColor = resources.getColor(R.color.white)
                    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    chart.axisLeft.textColor = resources.getColor(R.color.white)
                    chart.axisRight.isEnabled = false

                    chart.setDrawGridBackground(false)
                    //chart.xAxis.setDrawGridLines(false)
                    chart.xAxis.setCenterAxisLabels(true)
                    //chart.axisLeft.setDrawGridLines(false)
                    chart.setTouchEnabled(false)
                    chart.isScaleYEnabled = false
                    chart.description.isEnabled = false
                    chart.legend.textColor = resources.getColor(R.color.white)

                    chart.legend.isEnabled = false

                    //chart.xAxis.setDrawAxisLine(false)

                    //Set fonts
                    chart.legend.typeface = benderFont
                    chart.xAxis.typeface = benderFont
                    chart.axisLeft.typeface = benderFont
                    chart.setNoDataTextTypeface(benderFont)

                    //val mv = PriceChartMarkerView(this@FleaItemDetail, R.layout.price_chart_marker_view)
                    //chart.marker = mv

                    chart
                }, modifier = Modifier
                    .padding(start = 8.dp, end = 0.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .height(200.dp)
            ) { chart ->
                //if (chart.data != null) return@AndroidView
                val data = ballisticsData?.ballistics?.map {
                    val entry = Entry(it.range.toFloat(), it.drop.replace(",", "").toFloat() / 100)
                    entry.data = it
                    entry
                }

                val dataSet = LineDataSet(data, "Range (m)")
                dataSet.fillColor = resources.getColor(R.color.md_red_400)
                dataSet.setDrawFilled(false)
                dataSet.color = resources.getColor(R.color.md_red_400)
                dataSet.setCircleColor(resources.getColor(R.color.md_red_500))
                dataSet.valueTypeface = benderFont
                dataSet.setDrawValues(false)
                dataSet.setDrawCircles(false)
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

                val lineData = LineData(dataSet)
                lineData.setValueTextColor(resources.getColor(R.color.white))

                chart.data = lineData
                //chart.animateY(250, Easing.EaseOutQuad)
            }
        }
    }

    @Composable
    private fun VelocityChart(ballisticsData: AmmoBallistic?) {
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
                        text = "VELOCITY",
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
            }

            val benderFont = ResourcesCompat.getFont(this@AmmoDetailActivity, R.font.bender)
            AndroidView(
                factory = {
                    val chart = LineChart(it)

                    val formatter: ValueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase): String {
                            return "${value.toInt()} m"
                        }
                    }

                    val formatter2: ValueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase): String {
                            return "${value.toInt()} m/s"
                        }
                    }

                    chart.axisLeft.valueFormatter = formatter2
                    chart.xAxis.valueFormatter = formatter
                    chart.xAxis.textColor = resources.getColor(R.color.white)
                    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    chart.axisLeft.textColor = resources.getColor(R.color.white)
                    chart.axisRight.isEnabled = false

                    chart.setDrawGridBackground(false)
                    //chart.xAxis.setDrawGridLines(false)
                    chart.xAxis.setCenterAxisLabels(true)
                    //chart.axisLeft.setDrawGridLines(false)
                    chart.setTouchEnabled(false)
                    chart.isScaleYEnabled = false
                    chart.description.isEnabled = false
                    chart.legend.textColor = resources.getColor(R.color.white)

                    chart.legend.isEnabled = false

                    //chart.xAxis.setDrawAxisLine(false)

                    //Set fonts
                    chart.legend.typeface = benderFont
                    chart.xAxis.typeface = benderFont
                    chart.axisLeft.typeface = benderFont
                    chart.setNoDataTextTypeface(benderFont)

                    //val mv = PriceChartMarkerView(this@FleaItemDetail, R.layout.price_chart_marker_view)
                    //chart.marker = mv

                    chart
                }, modifier = Modifier
                    .padding(start = 8.dp, end = 0.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .height(200.dp)
            ) { chart ->
                //if (chart.data != null) return@AndroidView
                val data = ballisticsData?.ballistics?.map {
                    val entry = Entry(it.range.toFloat(), it.velocity.toFloat())
                    entry.data = it
                    entry
                }

                val dataSet = LineDataSet(data, "Range (m)")
                dataSet.fillColor = resources.getColor(R.color.md_red_400)
                dataSet.setDrawFilled(false)
                dataSet.color = resources.getColor(R.color.md_red_400)
                dataSet.setCircleColor(resources.getColor(R.color.md_red_500))
                dataSet.valueTypeface = benderFont
                dataSet.setDrawValues(false)
                dataSet.setDrawCircles(false)
                dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

                val lineData = LineData(dataSet)
                lineData.setValueTextColor(resources.getColor(R.color.white))

                chart.data = lineData
                //chart.animateY(250, Easing.EaseOutQuad)
            }
        }
    }

    @Composable
    private fun DamageChart(ballisticsData: AmmoBallistic?) {
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
                        text = "DAMAGE",
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
            }

            val benderFont = ResourcesCompat.getFont(this@AmmoDetailActivity, R.font.bender)
            AndroidView(
                factory = {
                    val chart = LineChart(it)

                    val formatter: ValueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase): String {
                            return "${value.toInt()} m"
                        }
                    }

                    val formatter2: ValueFormatter = object : ValueFormatter() {
                        override fun getAxisLabel(value: Float, axis: AxisBase): String {
                            return "${value.toInt()}"
                        }
                    }

                    chart.axisLeft.valueFormatter = formatter2
                    chart.xAxis.valueFormatter = formatter
                    chart.xAxis.textColor = resources.getColor(R.color.white)
                    chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                    chart.axisLeft.textColor = resources.getColor(R.color.white)
                    chart.axisRight.isEnabled = false

                    chart.setDrawGridBackground(false)
                    //chart.xAxis.setDrawGridLines(false)
                    chart.xAxis.setCenterAxisLabels(true)
                    //chart.axisLeft.setDrawGridLines(false)
                    chart.setTouchEnabled(false)
                    chart.isScaleYEnabled = false
                    chart.description.isEnabled = false
                    chart.legend.textColor = resources.getColor(R.color.white)

                    chart.legend.isEnabled = false

                    //chart.xAxis.setDrawAxisLine(false)

                    //Set fonts
                    chart.legend.typeface = benderFont
                    chart.xAxis.typeface = benderFont
                    chart.axisLeft.typeface = benderFont
                    chart.setNoDataTextTypeface(benderFont)

                    //val mv = PriceChartMarkerView(this@FleaItemDetail, R.layout.price_chart_marker_view)
                    //chart.marker = mv

                    chart
                }, modifier = Modifier
                    .padding(start = 8.dp, end = 0.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .height(200.dp)
            ) { chart ->
                //if (chart.data != null) return@AndroidView
                val data = ballisticsData?.ballistics?.map {
                    val entry = Entry(it.range.toFloat(), it.damage.toFloat())
                    entry.data = it
                    entry
                }

                val dataSet = LineDataSet(data, "Range (m)")
                dataSet.fillColor = resources.getColor(R.color.md_red_400)
                dataSet.setDrawFilled(false)
                dataSet.color = resources.getColor(R.color.md_red_400)
                dataSet.setCircleColor(resources.getColor(R.color.md_red_500))
                dataSet.valueTypeface = benderFont
                dataSet.setDrawValues(false)
                dataSet.setDrawCircles(false)

                val lineData = LineData(dataSet)
                lineData.setValueTextColor(resources.getColor(R.color.white))

                chart.data = lineData
                //chart.animateY(250, Easing.EaseOutQuad)
            }
        }
    }

    @Composable
    private fun AmmoDetailCard(
        ammo: Ammo
    ) {
        val context = LocalContext.current

        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column {
                Row(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        rememberImagePainter(ammo.pricing?.getIcon() ?: ""),
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
                                        listOf(ammo.pricing?.imageLink)
                                    ) { view, image ->
                                        Glide
                                            .with(view)
                                            .load(image)
                                            .into(view)
                                    }
                                    .withHiddenStatusBar(false)
                                    .show()
                            }
                    )
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Last Price: ${ammo.pricing?.getPrice()?.asCurrency()}",
                            style = MaterialTheme.typography.subtitle1,
                            fontSize = 16.sp
                        )
                        Row {
                            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                                Text(
                                    text = "Last 48h: ",
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Light
                                )
                                Text(
                                    text = "${ammo.pricing?.changeLast48h}%",
                                    style = MaterialTheme.typography.caption,
                                    //color = if (ammo.pricing?.changeLast48h ?: 0.0 > 0.0) Green500 else if (ammo.pricing?.changeLast48h ?: 0.0 < 0.0) Red500 else Color.Unspecified,
                                    color = ammo.pricing?.changeLast48h?.asColor()
                                        ?: Color.Unspecified,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }
                    }
                }
                Divider(color = DividerDark)
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    DataRow(
                        title = "DAMAGE",
                        value = Pair(ammo.ballistics?.damage, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "ARMOR DAMAGE %",
                        value = Pair(ammo.ballistics?.armorDamage, MaterialTheme.colors.onSurface)
                    )
                    DataRow(
                        title = "PENETRATION",
                        value = Pair(
                            ammo.ballistics?.penetrationPower?.roundToInt(),
                            MaterialTheme.colors.onSurface
                        )
                    )
                    DataRow(
                        title = "RECOIL",
                        value = Pair(
                            ammo.ballistics?.recoil?.plusMinus(),
                            ammo.ballistics?.recoil?.getColor(true, MaterialTheme.colors.onSurface)
                        )
                    )
                    DataRow(
                        title = "ACCURACY",
                        value = Pair(
                            ammo.ballistics?.accuracy?.plusMinus(),
                            ammo.ballistics?.accuracy?.getColor(
                                false,
                                MaterialTheme.colors.onSurface
                            )
                        )
                    )
                    DataRow(
                        title = "PROJECTILE SPEED",
                        value = Pair(
                            "${ammo.ballistics?.initialSpeed} m/s",
                            MaterialTheme.colors.onSurface
                        )
                    )
                    if (ammo.ballistics?.tracer == true) {
                        DataRow(
                            title = "TRACER",
                            value = Pair(
                                "${ammo.ballistics?.tracerColor?.uppercase()}",
                                MaterialTheme.colors.onSurface
                            )
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun ArmorPenCard(
        ammo: Ammo,
        selectedArmor: Item?
    ) {

        val maxDurability = selectedArmor?.MaxDurability?.toFloat() ?: 1f
        var sliderPosition by remember { mutableStateOf(maxDurability) }
        val context = LocalContext.current
        var chance: Double? by remember { mutableStateOf(null) }

        if (selectedArmor != null) {
            sliderPosition = maxDurability
        }

        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "ARMOR PENETRATION CHANCE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    )
                }
                Card(
                    modifier = Modifier.padding(12.dp),
                    shape = RoundedCornerShape(16.dp),
                    onClick = {
                        Intent(context, PickerActivity::class.java).apply {
                            putExtra("type", "armorAll")
                        }.let {
                            armorPickerLauncher.launch(it)
                        }
                    }
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            Modifier.weight(1f)
                        ) {
                            Text(
                                text = selectedArmor?.Name ?: "Select Armor",
                                style = MaterialTheme.typography.body1,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedArmor?.armorZone?.joinToString(", ") ?: "None",
                                fontWeight = FontWeight.Light,
                                fontSize = 10.sp
                            )
                        }
                        Column {
                            Text(
                                text = selectedArmor?.MaxDurability?.toString() ?: "",
                                style = MaterialTheme.typography.h5
                            )
                        }
                    }
                }
                Divider(color = DividerDark)
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "DURABILITY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    )
                }
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 16.dp),
                        value = sliderPosition,
                        valueRange = 0f..maxDurability,
                        onValueChange = {
                            if (selectedArmor != null) {
                                sliderPosition = it
                                chance = CalculatorHelper.penChance(
                                    ammo.toSimAmmo(),
                                    selectedArmor?.toSimArmor((maxDurability * sliderPosition).toDouble())
                                )
                            }
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colors.secondary,
                            activeTrackColor = MaterialTheme.colors.secondary
                        )
                    )
                    Text(
                        modifier = Modifier.width(40.dp),
                        text = sliderPosition.roundToInt().toString(),
                        style = MaterialTheme.typography.h5,
                        textAlign = TextAlign.End
                    )
                }
                Divider(color = DividerDark)
                Row(
                    Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHANCE TO PENETRATE ARMOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${chance?.roundToInt() ?: "-"}%",
                        style = MaterialTheme.typography.h5
                    )
                }
            }
        }
    }

    sealed class BottomNavigationScreens(
        val route: String,
        val resourceId: String,
        val icon: ImageVector? = null,
        @DrawableRes val iconDrawable: Int? = null
    ) {
        object Info :
            BottomNavigationScreens("Info", "Info", null, R.drawable.ic_baseline_info_24)

        object Table :
            BottomNavigationScreens("Table", "Stats", null, R.drawable.ic_baseline_table_chart_24)

        object Charts :
            BottomNavigationScreens("Charts", "Charts", null, R.drawable.ic_baseline_bar_chart_24)
    }

    @Composable
    fun AmmoBottomNav(
        navController: NavController
    ) {
        val items = listOf(
            BottomNavigationScreens.Info,
            BottomNavigationScreens.Table,
            BottomNavigationScreens.Charts
        )

        BottomNavigation(
            backgroundColor = Color(0xFE1F1F1F),
            elevation = 6.dp
        ) {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            items.forEachIndexed { _, item ->
                BottomNavigationItem(
                    icon = {
                        if (item.icon != null) {
                            Icon(item.icon, "")
                        } else {
                            Icon(
                                painter = painterResource(id = item.iconDrawable!!),
                                contentDescription = item.resourceId
                            )
                        }
                    },
                    label = { Text(item.resourceId) },
                    selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                    alwaysShowLabel = true, // This hides the title for the unselected items
                    onClick = {
                        try {
                            if (currentDestination?.route == item.route) return@BottomNavigationItem
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } catch (e: Exception) {
                            Timber.e(e)
                        }
                    },
                    selectedContentColor = MaterialTheme.colors.secondary,
                    unselectedContentColor = Color(0x99FFFFFF),
                )
            }
        }
    }
}

@Composable
fun DataRow(
    title: String,
    value: Pair<Any?, Color?>?
) {
    Row(
        Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.caption
        )
        Text(
            text = value?.first.toString(),
            style = MaterialTheme.typography.subtitle2,
            color = value?.second ?: MaterialTheme.colors.onSurface
        )
    }
}

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun PricingCard(
    pricing: Pricing
) {
    val context = LocalContext.current
    Card(
        Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .clickable {
                context.openActivity(FleaItemDetail::class.java) {
                    putString("id", pricing.id)
                }
            },
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
    ) {
        Column(
            Modifier.padding(16.dp)
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = "PRICING",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light,
                    fontFamily = Bender,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Column {
                pricing.buyFor?.forEach { item ->
                    DataRow(
                        title = "${item.getTitle().toUpperCase(Locale.current)} ", value = Pair(
                            item.price?.asCurrency(if (item.source == "peacekeeper") "D" else "R"),
                            MaterialTheme.colors.onSurface
                        )
                    )
                }
            }
        }
    }
}