package com.austinhodak.thehideout.status

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.ServerStatusQuery
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.models.ServerStatus
import com.austinhodak.tarkovapi.models.StatusOrange
import com.austinhodak.tarkovapi.models.toObj
import com.austinhodak.thehideout.NavActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.DividerDark
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.utils.openStatusSite
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@ExperimentalFoundationApi
@AndroidEntryPoint
class ServerStatusActivity : AppCompatActivity() {

    @Inject
    lateinit var apolloClient: ApolloClient

    override fun onBackPressed() {
        if (intent.hasExtra("fromNoti")) {
            val intent = Intent(this, NavActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideoutTheme {
                val scope = rememberCoroutineScope()

                var status: ServerStatus? by remember { mutableStateOf(null) }

                LaunchedEffect("") {
                    try {
                        status = apolloClient.query(ServerStatusQuery()).data?.status?.toObj()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                var showOnHomeCheck by remember { mutableStateOf(UserSettingsModel.showStatusOnHomeScreen.value) }

                var notificationsEnabled by remember { mutableStateOf(UserSettingsModel.serverStatusNotifications.value) }

                UserSettingsModel.showStatusOnHomeScreen.observe(scope) {
                    showOnHomeCheck = it
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Server Status") },
                            navigationIcon = {
                                IconButton(onClick = {
                                    onBackPressed()
                                }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            actions = {
                                IconButton(onClick = {
                                    notificationsEnabled = !notificationsEnabled
                                    scope.launch {
                                        UserSettingsModel.serverStatusNotifications.update(notificationsEnabled)
                                    }
                                }) {
                                    if (notificationsEnabled) {
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
                                IconButton(onClick = {
                                    scope.launch {
                                        status = apolloClient.query(ServerStatusQuery()).data?.status?.toObj()
                                    }
                                    Toast.makeText(this@ServerStatusActivity, "Status updated.", Toast.LENGTH_SHORT).show()
                                }) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_baseline_refresh_24),
                                        contentDescription = ""
                                    )
                                }
                            }
                        )
                    }
                ) {
                    if (status == null) {
                        LoadingItem()
                    }
                    status?.let {

                        LazyColumn(
                            contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            //Only show if general message is not cleared.
                            if (status?.isDegraded() == true) {
                                item {
                                    GlobalStatusCard(status = status)
                                }
                            }
                            if (status?.currentStatuses?.isNullOrEmpty() == false) {
                                item {
                                    CurrentStatusCard(status = status)
                                }
                            }
                            if (status?.messages?.isNullOrEmpty() == false) {
                                status?.messages?.forEachIndexed { index, message ->
                                    item {
                                        MessageCard(message, index)
                                    }
                                }
                            }
                            /*item {
                                Card(
                                    backgroundColor = if (isSystemInDarkTheme()) Color(
                                        0xFE1F1F1F
                                    ) else MaterialTheme.colors.primary,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    onClick = {
                                        scope.launch {
                                            UserSettingsModel.showStatusOnHomeScreen.update(!UserSettingsModel.showStatusOnHomeScreen.value)
                                        }
                                    }
                                ) {
                                    Row(
                                        Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Show Status Updates on Home Screen",
                                            //color = Color.White,
                                            style = MaterialTheme.typography.subtitle2,
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Switch(checked = showOnHomeCheck, onCheckedChange = {
                                            scope.launch {
                                                UserSettingsModel.showStatusOnHomeScreen.update(it)
                                            }
                                        })
                                    }
                                }
                            }*/
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun MessageCard(message: ServerStatus.Message, index: Int) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            border = BorderStroke(0.5.dp, if (message.isResolved()) Color.Transparent else message.getColor())
        ) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = message.getIcon()),
                        contentDescription = "",
                        tint = message.getColor(),
                        modifier = Modifier.size(24.dp),
                    )
                    Column {
                        Row {
                            Text(
                                text = message.getStatusDescription(),
                                color = message.getColor(),
                                modifier = Modifier.padding(start = 16.dp),
                                style = MaterialTheme.typography.subtitle2
                            )
                            Text(
                                text = " • ${message.getMessageTime()}",
                                color = message.getColor(),
                                style = MaterialTheme.typography.subtitle2
                            )
                        }
                        Text(
                            text = "Status: ${message.getStatus()}",
                            //color = message.getColor(),
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
                Divider(color = DividerDark, modifier = Modifier.padding(vertical = 16.dp))
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = message.content ?: "",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }

    @Composable
    private fun GlobalStatusCard(status: ServerStatus?) {
        val statusText = if (status?.generalStatus?.message?.isEmpty() == true && status.generalStatus?.status == 2) {
            "Partial problems with the server"
        } else {
            status?.generalStatus?.message
        }
        Card(
            backgroundColor = status?.currentStatusColor()!!,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icons8_cloud_alert_96),
                    contentDescription = "",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = statusText ?: "",
                    color = Color.Black,
                    style = MaterialTheme.typography.subtitle2,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }

    @Composable
    private fun CurrentStatusCard(status: ServerStatus?) {
        Card(
            backgroundColor = if (isSystemInDarkTheme()) Color(
                0xFE1F1F1F
            ) else MaterialTheme.colors.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            onClick = {
                openStatusSite(this)
            }
        ) {
            Column {
                Row(
                    modifier = Modifier.padding(
                        bottom = 8.dp,
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "CURRENT SERVICE STATUS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
                }

                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = 4.dp,
                        bottom = 12.dp
                    )
                ) {
                    status?.currentStatuses?.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(
                                        CircleShape
                                    )
                                    .background(item.getColor())
                            )
                            Column(
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = item.name ?: "",
                                    modifier = Modifier.padding(start = 16.dp),
                                    style = MaterialTheme.typography.subtitle2
                                )
                                Text(
                                    text = item.getStatusDescription(),
                                    modifier = Modifier.padding(start = 16.dp),
                                    style = MaterialTheme.typography.caption
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}