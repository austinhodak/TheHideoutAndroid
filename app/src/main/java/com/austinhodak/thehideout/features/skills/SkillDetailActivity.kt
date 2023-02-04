package com.austinhodak.thehideout.features.skills

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import com.austinhodak.tarkovapi.models.Skill
import com.austinhodak.tarkovapi.utils.color
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.components.OverflowMenu
import com.austinhodak.thehideout.compose.components.WikiItem
import com.austinhodak.thehideout.ui.theme.Bender
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.ui.theme.HideoutTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@AndroidEntryPoint
class SkillDetailActivity : GodActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val skill = intent.getSerializableExtra("skill") as Skill? ?: defaultSkill

        setContent {
            HideoutTheme {
                Scaffold(
                    topBar = {
                        AmmoDetailToolbar(
                            title = skill.name,
                            onBackPressed = {
                                finish()
                            },
                            actions = {
                                OverflowMenu {
                                    WikiItem(url = skill.wiki)
                                }
                            }
                        )
                    },
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        if (!skill.live) {
                            item {
                                NotLiveCard()
                            }
                        }
                        item {
                            TopCard(skill = skill)
                        }
                        if (skill.effects.isNotEmpty()) {
                            item {
                                EffectsCard(skill = skill)
                            }
                        }
                        if (skill.raise.isNotEmpty()) {
                            item {
                                HowToRaiseCard(skill = skill)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun NotLiveCard() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 4.dp
                ),
            backgroundColor = "FFA726".color
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_warning_24),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Black
                )
                Text(
                    text = "Planned Feature • Not in Game",
                    modifier = Modifier.padding(start = 16.dp),
                    color = Color.Black,
                    style = MaterialTheme.typography.subtitle2
                )
            }
        }
    }

    @Composable
    private fun TopCard(skill: Skill) {
        Card(
            modifier = Modifier.padding(vertical = 4.dp),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column {
                Row(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        rememberAsyncImagePainter(skill.icon),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .width(64.dp)
                            .height(64.dp)
                            .border((0.25).dp, color = BorderColor)
                    )
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${skill.type} Skill",
                            style = MaterialTheme.typography.h6,
                            fontSize = 16.sp
                        )
                        CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                            Text(
                                text = skill.description,
                                style = MaterialTheme.typography.caption,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun EffectsCard(skill: Skill) {
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "EFFECTS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                Column {
                    skill.effects.forEach {
                        Text(
                            text = "• $it",
                            modifier = Modifier.padding(top = 4.dp),
                            color = if (it.contains("Elite level:")) "c78b2c".color else Color.Unspecified,
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun HowToRaiseCard(skill: Skill) {
        Card(
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary
        ) {
            Column(
                Modifier.padding(16.dp)
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = "HOW TO RAISE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Column {
                    skill.raise.forEach {
                        Text(
                            text = "• $it",
                            modifier = Modifier.padding(top = 4.dp),
                            color = if (it.contains("Elite level:", false)) "c78b2c".color else Color.Unspecified,
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
            }
        }
    }

    private val defaultSkill = Skill(
        effects = listOf(
            "Decreases chance of fractures (-60% at elite level)",
            "Increases offline regeneration",
            "Decreases energy consumption (-30% at elite level)",
            "Decreases dehydration rate (-30% at elite level)",
            "Elite level: Damage absorption",
            "Elite level: Poison immunity level"
        ),
        icon = "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/12/Skill_physical_health.png/revision/latest?cb=20170329164834",
        name = "Health",
        raise = listOf(
            "Whenever you gain EXP for Strength, Endurance or Vitality you also gain EXP for Health.",
            "Completing the quest An Apple a Day Keeps the Doctor Away (+2 levels)"
        ),
        type = "Physical",
        live = true,
        description = "Good health speeds up the recovery from the damage sustained in the raids, decreases the probability of fractures and lowers the energy and dehydration rate.",
        wiki = ""
    )
}