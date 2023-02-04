package com.austinhodak.thehideout.features.bitcoin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import com.airbnb.lottie.compose.*
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.utils.addQuotes
import com.austinhodak.thehideout.utils.fleaFirebase
import com.austinhodak.thehideout.utils.openFleaDetail
import com.austinhodak.thehideout.ui.legacy.PriceChartMarkerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.text.DecimalFormat
import java.text.SimpleDateFormat

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
    var data: List<Entry>? by remember {
        mutableStateOf(null)
    }

    ProvideWindowInsets {
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

                    },
                    modifier = Modifier.statusBarsPadding()
                )
            },
            backgroundColor = MaterialTheme.colors.primary
        ) {
            Box {
                val benderFont = ResourcesCompat.getFont(context, R.font.bender)
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
                        chart.xAxis.textColor = context.resources.getColor(R.color.white)
                        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                        chart.axisLeft.textColor = context.resources.getColor(R.color.white)
                        chart.axisRight.isEnabled = false
                        chart.setScaleEnabled(false)

                        chart.setDrawGridBackground(false)
                        //chart.xAxis.setDrawGridLines(false)
                        chart.xAxis.setCenterAxisLabels(true)
                        //chart.axisLeft.setDrawGridLines(false)
                        //chart.setTouchEnabled(false)
                        chart.isScaleYEnabled = false
                        chart.description.isEnabled = false
                        chart.legend.textColor = context.resources.getColor(R.color.white)

                        chart.setNoDataText(null)

                        chart.legend.isEnabled = false

                        //chart.xAxis.setDrawAxisLine(false)

                        //Set fonts
                        chart.legend.typeface = benderFont
                        chart.xAxis.typeface = benderFont
                        chart.axisLeft.typeface = benderFont
                        chart.setNoDataTextTypeface(benderFont)

                        chart.axisLeft.setDrawLabels(false)
                        chart.xAxis.setDrawLabels(false)
                        chart.setDrawGridBackground(false)
                        chart.axisLeft.setDrawGridLines(false)
                        chart.xAxis.setDrawGridLines(false)
                        chart.minOffset = 0f

                        chart.axisLeft.valueFormatter = object : ValueFormatter() {
                            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                                val format = DecimalFormat("###,##0")
                                return "${format.format(value)}₽"
                            }
                        }

                        val mv = PriceChartMarkerView(context, R.layout.price_chart_marker_view)
                        chart.marker = mv

                        chart
                    }, modifier = Modifier
                        //.padding(start = 8.dp, end = 0.dp, bottom = 16.dp)
                        .fillMaxSize()
                ) { chart ->
                    //if (chart.data != null) return@AndroidView
                    val dataSet = LineDataSet(data, "Prices")
                    dataSet.fillColor = context.resources.getColor(R.color.md_red_400)
                    dataSet.setDrawFilled(true)
                    dataSet.color = context.resources.getColor(R.color.md_red_400)
                    dataSet.setCircleColor(context.resources.getColor(R.color.md_red_500))
                    dataSet.valueTypeface = benderFont
                    dataSet.setDrawValues(false)
                    dataSet.setDrawCircles(false)
                    dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

                    val lineData = LineData(dataSet)
                    lineData.setValueTextColor(context.resources.getColor(R.color.white))

                    chart.data = lineData
                    chart.animateY(0, Easing.EaseOutQuad)
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(model = btc?.pricing?.gridImageLink),
                            contentDescription = "",
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(38.dp)
                                .border((0.25).dp, color = BorderColor)
                                .clickable {
                                    btc?.id?.let { it1 -> context.openFleaDetail(it1) }
                                }
                        )
                        Text(text = btc?.pricing?.getHighestSellTrader()?.getPriceAsCurrency() ?: "", style = MaterialTheme.typography.h3.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 0f),
                                blurRadius = 8f
                            )
                        ), modifier = Modifier)
                    }
                }
            }

        }
    }

    fleaFirebase.child("priceHistory/59faff1d86f7746c51718c9c").orderByKey().startAt("\"${(System.currentTimeMillis() - (604800000 * 2))}\"").endAt(System.currentTimeMillis().addQuotes())
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || data != null) return
                data = snapshot.children.map {
                    val entry = Entry(it.key?.removeSurrounding("\"")?.toFloat() ?: 0f, (it.value as Long).toFloat())
                    entry.data = it
                    entry
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
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