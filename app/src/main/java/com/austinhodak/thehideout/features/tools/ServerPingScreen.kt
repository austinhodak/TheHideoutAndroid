package com.austinhodak.thehideout.features.tools

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.models.Server
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.*
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.extras
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.stealthcopter.networktools.Ping
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.math.roundToLong

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ServerPingScreen(
    navViewModel: NavViewModel,
    tarkovRepo: TarkovRepo
) {
    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    var isPinging by remember { mutableStateOf(false) }
    var servers by remember {
        mutableStateOf(extras.servers.sortedBy { it.name })
    }

    var isRefreshing by remember {
        mutableStateOf(false)
    }

    var sort by remember {
        mutableStateOf(0)
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MainToolbar(
                title = "Server Pings",
                navViewModel = navViewModel,
                actions = {
                    IconButton(onClick = {
                        val items = listOf("Name", "Region", "Ping")
                        MaterialDialog(context).show {
                            title(text = "Sort By")
                            listItemsSingleChoice(items = items, initialSelection = sort) { _, index, _ ->
                                sort = index
                            }
                        }
                    }) {
                        Icon(painterResource(id = R.drawable.ic_baseline_sort_24), contentDescription = "", tint = Color.White)
                    }
                }
            )
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
        floatingActionButton = {
            /*FloatingActionButton(onClick = {
                isPinging = !isPinging
            }) {
                if (isPinging) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_pause_24),
                        contentDescription = "",
                        tint = Color.Black
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_play_arrow_24),
                        contentDescription = "",
                        tint = Color.Black
                    )
                }
            }*/
        }
    ) {
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                scope.launch(Dispatchers.IO) {
                    servers.forEachIndexed { index, it ->
                        val server = it.copy()
                        //Timber.d("PINGING ${it.name}")
                        server.ping = Ping.onAddress(it.ip).doPing().timeTaken.roundToLong()
                        //Timber.d("RESULT ${it.name} / ${it.ping}")

                        val stupid = servers.toMutableList()
                        stupid[index] = server
                        servers = stupid
                        if (index == servers.lastIndex) {
                            isRefreshing = false
                        }
                    }
                }
            },
        ) {
            LazyColumn(contentPadding = PaddingValues(vertical = 4.dp)) {
                val data = when (sort) {
                    0 -> servers.sortedBy { it.name }
                    1 -> servers.sortedBy { it.region }
                    2 -> servers.sortedBy { it.ping }
                    else -> servers.sortedBy { it.ping }
                }
                items(data) {
                    ServerCard(it, scope)
                }
            }
        }
    }

    LaunchedEffect(key1 = "ping") {
        launch(Dispatchers.IO) {
            servers.forEachIndexed { index, it ->
                val server = it.copy()
                //Timber.d("PINGING ${it.name}")
                server.ping = Ping.onAddress(it.ip).doPing().timeTaken.roundToLong()
                //Timber.d("RESULT ${it.name} / ${it.ping}")

                val stupid = servers.toMutableList()
                stupid[index] = server
                servers = stupid
            }
        }
    }
}

@Composable
private fun ServerCard(server: Server, scope: CoroutineScope) {

    Card(
        backgroundColor = if (isSystemInDarkTheme()) Color(
            0xFE1F1F1F
        ) else MaterialTheme.colors.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 12.dp).height(IntrinsicSize.Min)
        ) {
            Rectangle(color = server.pingColor(), modifier = Modifier
                .fillMaxHeight()
                .padding(end = 16.dp))
            Text(text = server.region, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = server.name, style = MaterialTheme.typography.subtitle1, modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "${if (server.ping == (0).toLong()) "-" else server.ping}", color = server.pingColor(), style = MaterialTheme.typography.button)
        }
    }
}