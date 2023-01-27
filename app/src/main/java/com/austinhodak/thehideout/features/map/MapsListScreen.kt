package com.austinhodak.thehideout.features.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.MapEnums
import com.austinhodak.tarkovapi.MapsQuery
import com.austinhodak.tarkovapi.utils.openActivity
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ui.common.LoadingItem
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.theme.White

@Composable
fun MapsListScreen(navViewModel: NavViewModel, apolloClient: ApolloClient) {
    val scaffoldState = rememberScaffoldState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)
    val searchKey by navViewModel.searchKey.observeAsState("")

    var mapData: MapsQuery.Data? by remember { mutableStateOf(null) }

    LaunchedEffect(key1 = "maps") {
        try {
            val data = apolloClient.query(MapsQuery()).execute().data
            mapData = data
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            if (isSearchOpen) {
                SearchToolbar(
                    onClosePressed = {
                        navViewModel.setSearchOpen(false)
                        navViewModel.clearSearch()
                    },
                    onValue = {
                        navViewModel.setSearchKey(it)
                    }
                )
            } else {
                MainToolbar(
                    title = "Maps",
                    navViewModel = navViewModel
                )
            }
        }
    ) {
        if (mapData == null) {
            LoadingItem()
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
        ) {
            mapData?.maps?.forEach {
                item {
                    it?.let {
                        MapCard(it)
                    }
                }
            }
        }
    }
}

@Composable
fun MapCard(mapData: MapsQuery.Map) {

    val context = LocalContext.current
    Card(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth(),
        //border = BorderStroke(1.dp, color = color),
        onClick = {
            context.openActivity(MapDetailActivity::class.java) {
                putString("id", mapData.id)
            }
        },
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        Row(
            Modifier.padding(all = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = MapEnums.values().find { it.int.toString() == mapData.tarkovDataId }?.icon ?: R.drawable.icons8_map_96),
                contentDescription = "",
                modifier = Modifier.size(40.dp)
            )
            Column(
                Modifier.padding(start = 16.dp)
            ) {
                Text(
                    text = mapData.name ?: "",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium,
                    color = White
                )
                Text(
                    text = "${mapData.players} Players • ${mapData.raidDuration} Minutes",
                    style = MaterialTheme.typography.caption,
                    fontSize = 10.sp,
                    color = White
                )
            }
        }
    }
}

fun MapsQuery.Boss.icon(): Int {
    return when(name) {
        "Reshala" -> R.drawable.reshala
        "Death Knight" -> R.drawable.knightportrait
        "Big Pipe" -> R.drawable.bigpipeportrait
        "Cultist Priest" -> R.drawable.cultist
        "Tagilla" -> R.drawable.tagilla
        "Killa" -> R.drawable.killa_portrait
        "Shturman" -> R.drawable.shturman_portrait
        "Sanitar" -> R.drawable.sanitar_portrait
        "Birdeye" -> R.drawable.birdeyeportrait
        "Glukhar" -> R.drawable.gluhar
        "Rogue" -> R.drawable.rogue
        else -> R.drawable.rogue
    }
}