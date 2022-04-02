package com.austinhodak.thehideout.traders

import android.annotation.SuppressLint
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
import androidx.compose.ui.unit.dp
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.exception.ApolloNetworkException
import com.austinhodak.tarkovapi.TraderResetTimersQuery
import com.austinhodak.tarkovapi.TraderRestockTime
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.models.TraderReset
import com.austinhodak.tarkovapi.models.toObj
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.theme.Green500
import com.austinhodak.thehideout.compose.theme.Red400
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@SuppressLint("CheckResult")
@Composable
fun RestockTimersScreen(
    navViewModel: NavViewModel,
    apolloClient: ApolloClient
) {

    val scaffoldState = rememberScaffoldState()
    var resetTimers: TraderReset? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect("restock") {
        while (true) {
            try {
                Timber.d("Reloading")
                val newData = apolloClient.query(TraderResetTimersQuery()).execute().data?.toObj()
                if (newData != resetTimers) {
                    resetTimers = newData
                }
                delay(1000 * 60)

            } catch (e: ApolloNetworkException) {
                //Most likely no internet connection.
                e.printStackTrace()
                delay(1000 * 5)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                delay(1000 * 5)
            }
        }
    }

    var globalRestockAlert by remember { mutableStateOf(UserSettingsModel.globalRestockAlert.value) }

    UserSettingsModel.globalRestockAlert.observe(scope) {
        globalRestockAlert = it
    }

    var globalRestockAlertAppOpen by remember { mutableStateOf(UserSettingsModel.globalRestockAlertAppOpen.value) }

    UserSettingsModel.globalRestockAlertAppOpen.observe(scope) {
        globalRestockAlertAppOpen = it
    }

    var restockTime by remember { mutableStateOf(UserSettingsModel.traderRestockTime.value) }

    UserSettingsModel.traderRestockTime.observe(scope) {
        restockTime = it
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MainToolbar(
                title = "Trader Restock Timers",
                navViewModel = navViewModel,
                actions = {

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
        }
    ) {
        if (resetTimers == null) {
            LoadingItem()
        }

        resetTimers?.let {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                item {
                    Card(
                        backgroundColor = if (isSystemInDarkTheme()) Color(
                            0xFE1F1F1F
                        ) else MaterialTheme.colors.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            scope.launch {
                                UserSettingsModel.globalRestockAlert.update(!UserSettingsModel.globalRestockAlert.value)
                            }
                        }
                    ) {
                        Row(
                            Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Global Restock Notifications",
                                //color = Color.White,
                                style = MaterialTheme.typography.subtitle1,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(checked = globalRestockAlert, onCheckedChange = {
                                scope.launch {
                                    UserSettingsModel.globalRestockAlert.update(it)
                                }
                            })
                        }
                    }
                }
                items(items = Traders.values().filterNot { it.id == "Fence" }) { trader ->
                    TraderItem(
                        trader,
                        resetTimers,
                        scaffoldState
                    )
                }
                item {
                    Card(
                        backgroundColor = if (isSystemInDarkTheme()) Color(
                            0xFE1F1F1F
                        ) else MaterialTheme.colors.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(vertical = 4.dp),
                        onClick = {
                            if (globalRestockAlert) {
                                MaterialDialog(context).show {
                                    listItemsSingleChoice(items = TraderRestockTime.values().map {
                                        if (it == TraderRestockTime.`1`) {
                                            "$it Minute"
                                        } else {
                                            "$it Minutes"
                                        }
                                    }, initialSelection = TraderRestockTime.values().indexOf(restockTime)) { _, index, _ ->
                                        scope.launch {
                                            UserSettingsModel.traderRestockTime.update(TraderRestockTime.values()[index])
                                        }
                                    }
                                }
                            } else {
                                scope.launch {
                                    val result = scaffoldState.snackbarHostState.showSnackbar("Global Restock Notifications are off.", actionLabel = "TURN ON")
                                    if (result == SnackbarResult.ActionPerformed) {
                                        UserSettingsModel.globalRestockAlert.update(true)
                                    }
                                }
                            }
                        }
                    ) {
                        Row(
                            Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "When to notify.",
                                //color = Color.White,
                                style = MaterialTheme.typography.subtitle1,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(text = restockTime.let {
                                if (it == TraderRestockTime.`1`) {
                                    "$it Minute"
                                } else {
                                    "$it Minutes"
                                }
                            })
                        }
                    }
                }
                item {
                    Card(
                        backgroundColor = if (isSystemInDarkTheme()) Color(
                            0xFE1F1F1F
                        ) else MaterialTheme.colors.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        onClick = {
                            if (globalRestockAlert) {
                                scope.launch {
                                    UserSettingsModel.globalRestockAlertAppOpen.update(!UserSettingsModel.globalRestockAlertAppOpen.value)
                                }
                            } else {
                                scope.launch {
                                    val result = scaffoldState.snackbarHostState.showSnackbar("Global Restock Notifications are off.", actionLabel = "TURN ON")
                                    if (result == SnackbarResult.ActionPerformed) {
                                        UserSettingsModel.globalRestockAlert.update(true)
                                    }
                                }
                            }
                        }
                    ) {
                        Row(
                            Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Only Notify When App is Open",
                                //color = Color.White,
                                style = MaterialTheme.typography.subtitle1,
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Switch(checked = globalRestockAlertAppOpen, onCheckedChange = {
                                scope.launch {
                                    UserSettingsModel.globalRestockAlertAppOpen.update(it)
                                }
                            }, enabled = globalRestockAlert)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TraderItem(trader: Traders, resetTimers: TraderReset?, scaffoldState: ScaffoldState) {
    val scope = rememberCoroutineScope()

    val pref = when (trader) {
        Traders.PRAPOR -> UserSettingsModel.praporRestockAlert
        Traders.THERAPIST -> UserSettingsModel.therapistRestockAlert
        Traders.SKIER -> UserSettingsModel.skierRestockAlert
        Traders.PEACEKEEPER -> UserSettingsModel.peacekeeperRestockAlert
        Traders.MECHANIC -> UserSettingsModel.mechanicRestockAlert
        Traders.RAGMAN -> UserSettingsModel.ragmanRestockAlert
        Traders.JAEGER -> UserSettingsModel.jaegerRestockAlert
        else -> null
    }

    var alertEnabled by remember { mutableStateOf(pref?.value) }

    var globalAlertEnabled by remember { mutableStateOf(UserSettingsModel.globalRestockAlert.value) }

    UserSettingsModel.globalRestockAlert.observe(scope) {
        globalAlertEnabled = it
    }

    pref?.observe(scope) {
        alertEnabled = it
    }

    var resetTime: String? by remember { mutableStateOf(null) }

    LaunchedEffect(key1 = "timer") {
        while (true) {
            resetTime = resetTimers?.getTrader(trader.id)?.getResetTimeSpan()
            delay(1000)
        }
    }

    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        backgroundColor = Color(0xFE1F1F1F),
        onClick = {
            if (globalAlertEnabled) {
                scope.launch {
                    pref?.update(!pref.value)
                }
            } else {
                scope.launch {
                    val result = scaffoldState.snackbarHostState.showSnackbar("Global Restock Notifications are off.", actionLabel = "TURN ON")
                    if (result == SnackbarResult.ActionPerformed) {
                        UserSettingsModel.globalRestockAlert.update(true)
                    }
                }
            }
        }
    ) {
        Column {
            Row(
                Modifier.padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = trader.icon),
                    contentDescription = "",
                    modifier = Modifier.size(72.dp)
                )
                Column(
                    Modifier
                        .padding(start = 16.dp)
                        .height(72.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(text = trader.id, style = MaterialTheme.typography.subtitle1)
                    Text(text = resetTime ?: "", style = MaterialTheme.typography.body2)
                }
                Switch(checked = alertEnabled ?: false, onCheckedChange = {
                    scope.launch {
                        pref?.update(it)
                    }
                }, enabled = globalAlertEnabled)
            }
        }
    }
}