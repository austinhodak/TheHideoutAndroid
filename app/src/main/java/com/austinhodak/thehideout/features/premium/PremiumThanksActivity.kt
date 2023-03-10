package com.austinhodak.thehideout.features.premium

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import com.airbnb.lottie.compose.*
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ui.theme.HideoutTheme
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.austinhodak.thehideout.utils.restartNavActivity
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
class PremiumThanksActivity : AppCompatActivity() {

    @OptIn(ExperimentalFoundationApi::class)
    override fun onBackPressed() {
        if (intent.hasExtra("restart")) {
            restartNavActivity()
        } else {
            super.onBackPressed()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideoutTheme {
                val uiController = rememberSystemUiController()
                uiController.setNavigationBarColor(Color(0xFF1F1F1F))
                uiController.setStatusBarColor(Color(0xFF1F1F1F))
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {

                            },
                            backgroundColor = Color.Transparent,
                            modifier = Modifier.statusBarsPadding(),
                            navigationIcon = {
                                IconButton(onClick = {
                                    if (intent.hasExtra("restart")) {
                                        restartNavActivity()
                                    } else {
                                        onBackPressed()
                                    }
                                }) {
                                    Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White)
                                }
                            },
                            elevation = 0.dp,
                            actions = {

                            }
                        )
                    },
                    backgroundColor = Color(0xFF1F1F1F)
                ) { padding ->
                    Box(
                        Modifier.fillMaxSize(),
                    ) {
                        FireworksLottie(modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.6f)) {

                        }
                        Column(
                            Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            var progress by remember {
                                mutableStateOf(0f)
                            }

                            val weight: Float by animateFloatAsState(if (progress == 1f) 1f else 1f)

                            ThanksLottie(modifier = Modifier.weight(1f)) {
                                progress = it
                            }

                            AnimatedVisibility(visible = progress == 1f) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = 16.dp)
                                        .defaultMinSize(minHeight = 300.dp),
                                    verticalArrangement = Arrangement.Bottom
                                ) {
                                    Spacer(modifier = Modifier.padding(vertical = 32.dp))
                                    Button(
                                        onClick = {
                                            "https://discord.gg/YQW36z29z6".openWithCustomTab(this@PremiumThanksActivity)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                            .height(64.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF738ADB))
                                    ) {
                                        //Icon(painter = painterResource(id = R.drawable.icons8_discord_96), contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                                        //Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(getString(R.string.join_our_discord), fontSize = 18.sp, style = MaterialTheme.typography.button)
                                            Text(getString(R.string.get_supporter_role), fontSize = 10.sp, style = MaterialTheme.typography.caption)
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            "https://twitter.com/austin6561".openWithCustomTab(this@PremiumThanksActivity)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                            .height(64.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF1DA1F2))
                                    ) {
                                        //Icon(painter = painterResource(id = R.drawable.icons8_discord_96), contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                                        //Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(getString(R.string.follow_twitter), fontSize = 18.sp, style = MaterialTheme.typography.button)
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            "https://www.twitch.tv/theeeelegend".openWithCustomTab(this@PremiumThanksActivity)
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp, end = 16.dp, bottom = 0.dp)
                                            .height(64.dp),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF9146FF))
                                    ) {
                                        //Icon(painter = painterResource(id = R.drawable.icons8_discord_96), contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                                        //Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(getString(R.string.follow_twitch), fontSize = 18.sp, style = MaterialTheme.typography.button)
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun FireworksLottie(modifier: Modifier, progress: (Float) -> Unit) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.fireworks))
        val p by animateLottieCompositionAsState(
            composition, iterations = LottieConstants
                .IterateForever
        )
        progress.invoke(p)
        LottieAnimation(
            composition = composition,
            modifier = modifier
        )
    }

    @Composable
    fun ThanksLottie(modifier: Modifier, progress: (Float) -> Unit) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.lottie_thanks_1))
        val p by animateLottieCompositionAsState(composition)
        progress.invoke(p)
        LottieAnimation(
            composition = composition,
            progress = p,
            modifier = modifier
        )
    }
}