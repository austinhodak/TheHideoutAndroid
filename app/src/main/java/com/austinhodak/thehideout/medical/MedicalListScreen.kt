package com.austinhodak.thehideout.medical

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.openActivity
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.utils.getMedIcon
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun MedicalListScreen (
    tarkovRepo: TarkovRepo,
    navViewModel: NavViewModel
) {
    val titles: List<String> = listOf(stringResource(R.string.meds), stringResource(R.string.stims))

    val pagerState = rememberPagerState(pageCount = titles.size)
    val coroutineScope = rememberCoroutineScope()

    var data by remember {
        mutableStateOf(listOf<Item>())
    }

    LaunchedEffect("meds") {
        val list = tarkovRepo.getItemsByTypes(listOf(ItemTypes.MED, ItemTypes.STIM))
        data = list
    }

    Scaffold(
        topBar = {
            Column {
                MainToolbar(
                    title = stringResource(id = R.string.medical),
                    navViewModel = navViewModel,
                    elevation = 0.dp
                )
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, tabPositions), color = Red400)
                    },
                ) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(title) },
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            selectedContentColor = Red400,
                            unselectedContentColor = White
                        )
                    }
                }
            }
        }
    ) {
        AnimatedContent(targetState = data.isNullOrEmpty()) {
            if (it) {
                LoadingItem()
            } else {
                HorizontalPager(state = pagerState) { page ->
                    MedList(data, page)
                }
            }
        }
    }
}

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
private fun MedList(
    data: List<Item>?,
    page: Int
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.fillMaxHeight()
    ) {
        val items = data!!
            .filter { it.itemType == if (page == 0) ItemTypes.MED else ItemTypes.STIM }
            .filter { it.pricing != null && it.pricing?.gridImageLink != null }
            .sortedBy { it.ShortName }

        items(items = items, key = { it.id }) { item ->
            MedCard(item = item, Modifier.animateItemPlacement())
        }
    }
}

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
private fun MedCard(
    item: Item,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {
            context.openActivity(MedDetailActivity::class.java) {
                putString("id", item.pricing?.id)
            }
        },
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    rememberImagePainter(item.pricing?.getCleanIcon()),
                    contentDescription = null,
                    modifier = Modifier
                        .width(40.dp)
                        .height(40.dp)
                )
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${item.ShortName}",
                        style = MaterialTheme.typography.subtitle1
                    )
                    SmallBuyPrice(pricing = item.pricing)
                }
                Row {
                    if (item.effects_damage != null) {
                        item.effects_damage?.keys()?.forEach {
                            //val effect = item.effects_damage?.getJSONObject(it)
                            val icon = it.getMedIcon()
                            icon?.let {
                                Icon(painter = painterResource(id = icon), contentDescription = "Med", tint = Color.Unspecified, modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
                /*Column(
                    Modifier.width(IntrinsicSize.Max),
                ) {
                    StatItem(value = item.medUseTime, title = "USE TIME")
                    StatItem(value = item.MaxHpResource, title = "USES/HP")
                    StatItem(value = item.hpResourceRate, title = "HP/USE")
                }*/
            }
        }
    }
}