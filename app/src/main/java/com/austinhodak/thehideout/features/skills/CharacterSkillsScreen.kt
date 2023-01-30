package com.austinhodak.thehideout.features.skills

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.FleaVisiblePrice
import com.austinhodak.tarkovapi.models.Skill
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.color
import com.austinhodak.tarkovapi.utils.openActivity
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.*
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.skillsList
import com.austinhodak.thehideout.utils.addToCartDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CharacterSkillsScreen(
    navViewModel: NavViewModel
) {
    val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)
    val searchKey by navViewModel.searchKey.observeAsState("")

    Scaffold(
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
                    title = "Character Skills",
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
        val skills = skillsList.skills.sortedBy { it.name }.filter { it.live }.filter { it.name.contains(searchKey, true) || it.type.contains(searchKey, true) }

        LazyColumn(
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(items = skills, key = { it.name} ) { skill ->
                SkillCard(skill, Modifier.animateItemPlacement())
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SkillCard(skill: Skill, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Card(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        //border = BorderStroke(1.dp, color = color),
        onClick = {
                  context.openActivity(SkillDetailActivity::class.java) {
                      putSerializable("skill", skill)
                  }
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
                    rememberAsyncImagePainter(skill.icon),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
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
                        text = skill.name,
                        style = MaterialTheme.typography.h6,
                        fontSize = 17.sp
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        val text = if (skill.live) {
                            skill.type
                        } else {
                            "${skill.type} (Future Skill)"
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.caption,
                            //fontSize = 10.sp
                        )
                    }
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

enum class SkillType() {
    PHYSICAL,
    MENTAL,
    COMBAT,
    PRACTICAL,
    SPECIAL_USEC,
    SPECIAL_BEAR
}