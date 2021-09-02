package com.austinhodak.thehideout.medical

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.utils.asCurrency
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun MedicalListScreen (
    tarkovRepo: TarkovRepo,
    navViewModel: NavViewModel
) {
    val titles: List<String> = listOf("MEDS", "STIMS")

    val pagerState = rememberPagerState(pageCount = titles.size)
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                MainToolbar(
                    title = "Medical",
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
        HorizontalPager(state = pagerState) { page ->
            val data = tarkovRepo.getItemsByType(if (page == 0) ItemTypes.MED else ItemTypes.STIM).collectAsState(initial = emptyList())
            val items = data.value.filter { it.pricing != null && it.pricing?.gridImageLink != null }.sortedBy { it.ShortName }
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(items = items) { item ->
                    MedCard(item = item)
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun MedCard(
    item: Item
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 4.dp),
        border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color(0xFF313131) else Color(0xFFDEDEDE)),
        elevation = 0.dp,
        onClick = {

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
                    rememberImagePainter(item.pricing?.gridImageLink),
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
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "Last Price: ${item.getPrice().asCurrency()}",
                            style = MaterialTheme.typography.caption
                        )
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