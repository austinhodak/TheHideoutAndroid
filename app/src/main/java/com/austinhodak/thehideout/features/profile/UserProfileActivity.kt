package com.austinhodak.thehideout.features.profile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.austinhodak.tarkovapi.Levels
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ui.theme.Bender
import com.austinhodak.thehideout.ui.theme.HideoutTheme
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.features.flea_market.detail.FleaItemDetail
import kotlinx.coroutines.launch

@ExperimentalFoundationApi
class UserProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            HideoutTheme {
                val items = listOf(
                    FleaItemDetail.NavItem("Info", R.drawable.ic_outline_info_24),
                    FleaItemDetail.NavItem("Traders", R.drawable.ic_baseline_group_24),
                    FleaItemDetail.NavItem("Game", R.drawable.ic_baseline_mouse_24),
                )

                var selectedNavItem by remember { mutableStateOf(0) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(text = "Trader Settings")
                            },
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            actions = {
                                /*OverflowMenu {
                                    OverflowMenuItem(text = getString(R.string.log_out)) {

                                    }
                                }*/
                            }
                        )
                    },
                    bottomBar = {
                        //BottomBar(selectedNavItem, items) { selectedNavItem = it }
                    }
                ) {
                    Box(modifier = Modifier.padding(it)) {
                        TradersScreen()
                        /*Crossfade(targetState = selectedNavItem) {
                            when (it) {
                                0 -> {
                                    InfoScreen()
                                }
                                1 -> {
                                    TradersScreen()
                                }
                                2 -> {
                                    GameScreen()
                                }
                            }
                        }*/
                    }
                }
            }
        }
    }

    @Composable
    private fun InfoScreen() {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    backgroundColor = Color(0xFE1F1F1F)
                ) {
                    Column(
                        Modifier.padding(bottom = 12.dp)
                    ) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = "INFORMATION",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Light,
                                fontFamily = Bender,
                                modifier = Modifier.padding(bottom = 10.dp, top = 16.dp, start = 16.dp, end = 16.dp)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text("In Game Name", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Normal)
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text("No name set.", style = MaterialTheme.typography.body2)
                                }
                            }
                            Icon(Icons.Filled.Edit, null)
                        }
                        Row(
                            modifier = Modifier
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text("Discord Username", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Normal)
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text("No name set.", style = MaterialTheme.typography.body2)
                                }
                            }
                            Icon(Icons.Filled.Edit, null)
                        }
                        Row(
                            modifier = Modifier
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text("Game Edition", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Normal)
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text("Standard", style = MaterialTheme.typography.body2)
                                }
                            }
                            Icon(Icons.Filled.Edit, null)
                        }
                        Row(
                            modifier = Modifier
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text("Player Level", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Normal)
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text("71", style = MaterialTheme.typography.body2)
                                }
                            }
                            Icon(Icons.Filled.Edit, null)
                        }
                    }
                }
            }
            item {
                Card(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    backgroundColor = Color(0xFE1F1F1F)
                ) {

                }
            }
        }
    }

    @Composable
    private fun TradersScreen() {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(items = Traders.values().filterNot { it.id == "Fence" }) { trader ->
                TraderItem(
                    trader
                )
            }
        }
    }

    @Composable
    private fun TraderItem(trader: Traders) {
        val scope = rememberCoroutineScope()

        val pref = when (trader) {
            Traders.PRAPOR -> UserSettingsModel.praporLevel
            Traders.THERAPIST -> UserSettingsModel.therapistLevel
            Traders.SKIER -> UserSettingsModel.skierLevel
            Traders.PEACEKEEPER -> UserSettingsModel.peacekeeperLevel
            Traders.MECHANIC -> UserSettingsModel.mechanicLevel
            Traders.RAGMAN -> UserSettingsModel.ragmanLevel
            Traders.JAEGER -> UserSettingsModel.jaegerLevel
            else -> null
        }

        var traderLevel by remember { mutableStateOf(pref?.value) }

        pref?.observe(scope) {
            traderLevel = it
        }

        Card(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = Color(0xFE1F1F1F),
            onClick = {

            }
        ) {
            Column {
                Row(
                    Modifier.padding(end = 8.dp).height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = trader.icon),
                        contentDescription = "",
                        modifier = Modifier.fillMaxHeight().width(IntrinsicSize.Min).aspectRatio(1f)
                    )
                    Column(
                        Modifier
                            .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
                            .height(IntrinsicSize.Min)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(text = trader.id, style = MaterialTheme.typography.subtitle1, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(
                            Modifier.padding(top = 8.dp).width(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Chip(text = "LL1", selected = traderLevel == Levels.`1`) {
                                scope.launch {
                                    pref?.update(Levels.`1`)
                                }
                            }
                            Chip(text = "LL2", selected = traderLevel == Levels.`2`) {
                                scope.launch {
                                    pref?.update(Levels.`2`)
                                }
                            }
                            Chip(text = "LL3", selected = traderLevel == Levels.`3`) {
                                scope.launch {
                                    pref?.update(Levels.`3`)
                                }
                            }
                            Chip(text = "LL4", selected = traderLevel == Levels.`4`) {
                                scope.launch {
                                    pref?.update(Levels.`4`)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun GameScreen() {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    backgroundColor = Color(0xFE1F1F1F)
                ) {
                    Column(
                        Modifier.padding(bottom = 12.dp)
                    ) {
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = "MOUSE SETTINGS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Light,
                                fontFamily = Bender,
                                modifier = Modifier.padding(bottom = 10.dp, top = 16.dp, start = 16.dp, end = 16.dp)
                            )
                        }
                        Row(
                            modifier = Modifier
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text("Mouse DPI", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Normal)
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text("400", style = MaterialTheme.typography.body2)
                                }
                            }
                            Icon(Icons.Filled.Edit, null)
                        }
                        Row(
                            modifier = Modifier
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text("In Game Mouse Sensitivity", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Normal)
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text("Not set.", style = MaterialTheme.typography.body2)
                                }
                            }
                            Icon(Icons.Filled.Edit, null)
                        }
                        Row(
                            modifier = Modifier
                                .clickable { }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier.weight(1f)
                            ) {
                                Text("In Game Mouse Sensitivity", style = MaterialTheme.typography.body1, fontWeight = FontWeight.Normal)
                                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                    Text("Not set.", style = MaterialTheme.typography.body2)
                                }
                            }
                            Icon(Icons.Filled.Edit, null)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun Chip(
        selected: Boolean = false,
        text: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        Surface(
            color = when {
                selected -> Red400
                else -> Color(0xFF2F2F2F)
            },
            contentColor = when {
                selected -> Color.Black
                else -> Color.White
            },
            shape = RoundedCornerShape(12.dp),
            modifier = modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable {
                    onClick()
                }
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }


    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun BottomBar(
        selected: Int,
        items: List<FleaItemDetail.NavItem>,
        onItemSelected: (Int) -> Unit
    ) {

        BottomNavigation(
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            items.forEachIndexed { index, item ->
                BottomNavigationItem(
                    icon = { Icon(painter = painterResource(id = item.icon), contentDescription = null) },
                    label = { Text(item.title) },
                    selected = selected == index,
                    onClick = { onItemSelected(index) },
                    selectedContentColor = MaterialTheme.colors.secondary,
                    unselectedContentColor = if (item.enabled == true) Color(0x99FFFFFF) else Color(0x33FFFFFF),
                    enabled = item.enabled ?: true,
                )
            }
        }
    }
}