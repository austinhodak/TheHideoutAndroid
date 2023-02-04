package com.austinhodak.thehideout.features.bosses

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.MapEnums
import com.austinhodak.tarkovapi.MapsQuery
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ui.common.LoadingItem
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.theme.BorderColor
import timber.log.Timber

@Composable
fun BossesListScreen(navViewModel: NavViewModel, apolloClient: ApolloClient) {
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

    mapData?.maps?.first()?.bosses?.first()?.name

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
                    title = "Bosses",
                    navViewModel = navViewModel,
                    actions = {
                        IconButton(onClick = {
                            navViewModel.setSearchOpen(true)
                        }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                        }
                    }
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
            mapData?.maps?.flatMap { it?.bosses!! }?.distinctBy { it?.name }?.filterNot { it?.name == "Rogue" || it?.name == "Raider" }?.forEach {
                Timber.d(it.toString())
                item {
                    it?.let {
                        BossCard(it, mapData!!)
                    }
                }
            }
        }
    }
}

@Composable
fun BossCard(boss: MapsQuery.Boss, mapData: MapsQuery.Data) {
    val mapsForBoss = mapData.maps.filter {
        it?.bosses?.any { it?.name == boss.name } == true
    }

    val context = LocalContext.current
    Card(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        //border = BorderStroke(1.dp, color = color),
        onClick = {

        },
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        Column {
            Row(
                Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*Rectangle(
                    color = when (skill.type) {
                        "Physical" -> "1B5E20".color
                        "Mental" -> "4A148C".color
                        "Combat" -> "BF360C".color
                        "Practical" -> "F57F17".color
                        "BEAR" -> "B71C1C".color
                        "USEC" -> "0D47A1".color
                        else -> Color.Transparent
                    }, modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 16.dp)
                )*/
                Image(
                    rememberAsyncImagePainter(boss.icon()),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .width(48.dp)
                        .height(48.dp)
                        .border((0.25).dp, color = BorderColor),
                    contentScale = ContentScale.Crop
                )
                Column(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${boss.name}",
                        style = MaterialTheme.typography.h6,
                        fontSize = 17.sp
                    )
                    Row(

                    ) {
                        mapsForBoss.forEach { map ->
                            Image(
                                painter = painterResource(id = MapEnums.values().find { it.int.toString() == map?.tarkovDataId }?.icon ?: R.drawable.icons8_map_96),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    /*CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        val text = "${mapsForBoss.joinToString { it?.name ?: "" }}"
                        Text(
                            text = text,
                            style = MaterialTheme.typography.caption,
                            //fontSize = 10.sp
                        )
                    }*/
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {

                }
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
        else -> R.drawable.ic_blank
    }
}