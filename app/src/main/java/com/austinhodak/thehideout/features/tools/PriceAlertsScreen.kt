package com.austinhodak.thehideout.features.tools

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItems
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.*
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.firebase.PriceAlert
import com.austinhodak.thehideout.ui.common.EmptyText
import com.austinhodak.thehideout.ui.common.LoadingItem
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.openFleaDetail
import com.austinhodak.thehideout.utils.questsFirebase
import com.austinhodak.thehideout.utils.uid
import com.austinhodak.thehideout.widgets.WidgetPickerActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PriceAlertsScreen(
    navViewModel: NavViewModel,
    tarkovRepo: TarkovRepo
) {

    val context = LocalContext.current
    val scaffoldState = rememberScaffoldState()
    val scope = rememberCoroutineScope()

    var priceAlertsGlobalNotifications by remember { mutableStateOf(UserSettingsModel.priceAlertsGlobalNotifications.value) }

    var list by remember { mutableStateOf<List<PriceAlert>?>(null) }
    var itemList by remember { mutableStateOf<List<Item>?>(null) }


    LaunchedEffect(key1 = "") {
        questsFirebase.child("priceAlerts").orderByChild("uid").equalTo(uid()).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || !snapshot.hasChildren()) {
                    list = emptyList()
                    itemList = emptyList()
                    return
                }

                list = snapshot.children.map {
                    val alert = it.getValue<PriceAlert>()!!
                    alert.reference = it.ref
                    alert
                }
                val itemIDs = list?.map { it.itemID ?: "" } ?: emptyList()
                scope.launch {
                    tarkovRepo.getItemByID(itemIDs).collect {
                        itemList = it
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MainToolbar(
                title = "Price Alerts",
                navViewModel = navViewModel,
                actions = {
                    IconButton(onClick = {
                        priceAlertsGlobalNotifications = !priceAlertsGlobalNotifications
                        scope.launch {
                            UserSettingsModel.serverStatusNotifications.update(priceAlertsGlobalNotifications)
                        }
                    }) {
                        if (priceAlertsGlobalNotifications) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_baseline_notifications_active_24),
                                contentDescription = ""
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_baseline_notifications_off_24),
                                contentDescription = ""
                            )
                        }
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
            FloatingActionButton(onClick = {
                //Open item picker.
                context.openActivity(WidgetPickerActivity::class.java) {
                    putBoolean("priceAlert", true)
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_notification_add_24),
                    contentDescription = "Add Alert",
                    tint = Color.Black
                )
            }
        }
    ) {
        if (itemList == null || list == null) {
            LoadingItem()
        } else if (itemList?.isEmpty() == null || list?.isEmpty() == true) {
            EmptyText(text = "No alerts set.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(items = itemList ?: emptyList()) { item ->
                    PriceAlertItem(item = item, onClick = {
                        context.openFleaDetail(it)
                    }, longClick = {}, list?.filter { it.itemID.equals(item.id) })
                }
            }
        }
    }
}

@SuppressLint("CheckResult")
@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
private fun PriceAlertItem(
    item: Item,
    onClick: (String) -> Unit,
    longClick: (String) -> Unit,
    priceAlerts: List<PriceAlert>?,
) {

    val context = LocalContext.current

    val color = when (item.BackgroundColor) {
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
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { onClick(item.id) },
                onLongClick = { longClick(item.id) }
            ),
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        Row(
            Modifier
                .height(IntrinsicSize.Max)
        ) {
            Rectangle(
                color = color, modifier = Modifier
                    .fillMaxHeight()
            )
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 0.dp)
                ) {
                    Image(
                        rememberImagePainter(
                            item.pricing?.getCleanIcon()
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp)
                            .border((0.25).dp, color = BorderColor)
                    )
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.pricing?.name ?: "",
                            style = MaterialTheme.typography.h6,
                            fontSize = 15.sp
                        )
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            val text = if (item.pricing?.noFlea == false) {
                                item.getUpdatedTime()
                            } else {
                                "${item.getUpdatedTime()} • Not on Flea"
                            }
                            Text(
                                text = text,
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        val price = item.getPrice()

                        Text(
                            text = price.asCurrency(),
                            style = MaterialTheme.typography.h6,
                            fontSize = 15.sp
                        )
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = "${item.getPricePerSlot(price).asCurrency()}/slot",
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp
                            )
                        }
                        TraderSmall(item = item.pricing)
                    }
                }
                Divider(color = DividerDark)
                priceAlerts?.forEachIndexed { i, alert ->
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
                                    MaterialDialog(context).show {
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