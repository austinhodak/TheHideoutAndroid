package com.austinhodak.thehideout.features.map

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import coil.compose.rememberImagePainter
import coil.size.OriginalSize
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.ui.theme.Bender
import com.austinhodak.thehideout.ui.theme.HideoutTheme
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class StaticMapsActivity : AppCompatActivity() {
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            var selectedMap by remember {
                mutableStateOf(maps.find { it.map.equals(UserSettingsModel.defaultMap.value.id, true) })
            }

            var selectedMapImage by remember {
                mutableStateOf(maps.find { it.map.equals(UserSettingsModel.defaultMap.value.id, true) }?.images?.first())
            }

            val scope = rememberCoroutineScope()

            var isFullScreen by rememberSaveable {
                mutableStateOf(false)
            }

            HideoutTheme {
                ProvideWindowInsets {
                    val systemUiController = rememberSystemUiController()

                    if (!isFullScreen) {
                        systemUiController.setStatusBarColor(
                            Color(0xFE1F1F1F),
                            darkIcons = false
                        )
                        systemUiController.setNavigationBarColor(
                            DarkPrimary,
                        )
                    } else {
                        systemUiController.setStatusBarColor(
                            Color.Transparent,
                            darkIcons = false
                        )
                        systemUiController.setNavigationBarColor(
                            Color.Transparent,
                        )
                    }

                    Scaffold(
                        topBar = {
                            val density = LocalDensity.current
                            AnimatedVisibility(visible = !isFullScreen, enter = slideInVertically {
                                -it / 2
                            }, exit = slideOutVertically {
                                -it
                            }) {
                                Column(
                                    modifier = Modifier.statusBarsPadding()
                                ) {
                                    TopAppBar(
                                        title = { Text(selectedMap?.map ?: "") },
                                        navigationIcon = {
                                            IconButton(onClick = {
                                                finish()
                                            }) {
                                                Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                            }
                                        },
                                        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                                        elevation = 5.dp,
                                        actions = {

                                        }
                                    )
                                    AnimatedVisibility(visible = selectedMap?.images?.size ?: 1 > 1) {
                                        val pagerState = rememberPagerState()
                                        Tabs(
                                            pagerState = pagerState,
                                            scope = scope,
                                            items = selectedMap?.images?.map { it.name } ?: emptyList()
                                        ) {
                                            selectedMapImage = selectedMap?.images?.get(it)
                                        }
                                    }
                                }
                            }
                        }
                    ) { paddingValues ->
                        Box(Modifier.fillMaxSize()) {
                            selectedMapImage?.let {
                                ZoomableImage(
                                    painter = rememberImagePainter(it.image, builder = {
                                        crossfade(true)
                                        //size(OriginalSize)
                                    }),
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    minScale = 10f
                                )
                            }
                            Column(
                                if (isFullScreen)
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .statusBarsPadding()
                                else
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                FloatingActionButton(
                                    onClick = {
                                        MaterialDialog(this@StaticMapsActivity).show {
                                            title(text = "Choose Map")
                                            listItemsSingleChoice(
                                                items = maps.map { it.map },
                                                initialSelection = maps.indexOf(selectedMap)
                                            ) { _, _, text ->
                                                selectedMap = maps.find { it.map == text.toString() }
                                                selectedMapImage = maps.find { it.map.equals(text.toString(), true) }?.images?.first()
                                            }
                                        }
                                    },
                                    backgroundColor = DarkPrimary,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .size(40.dp),
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_map_24),
                                        contentDescription = "Choose Map",
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                FloatingActionButton(
                                    onClick = {
                                        isFullScreen = !isFullScreen
                                    },
                                    backgroundColor = DarkPrimary,
                                    modifier = Modifier
                                        .padding(vertical = 8.dp)
                                        .size(40.dp),
                                ) {
                                    Icon(
                                        painter = if (isFullScreen) rememberImagePainter(
                                            R.drawable.ic_baseline_fullscreen_exit_24,
                                            builder = { crossfade(true) }) else rememberImagePainter(R.drawable.ic_baseline_fullscreen_24, builder = { crossfade(true) }),
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ZoomableImage(
        painter: Painter,
        maxScale: Float = .30f,
        minScale: Float = 3f,
        contentScale: ContentScale = ContentScale.Fit,
        @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
        isRotation: Boolean = false,
        isZoomable: Boolean = true
    ) {
        val scale = remember { mutableStateOf(1f) }
        val rotationState = remember { mutableStateOf(1f) }
        val offsetX = remember { mutableStateOf(1f) }
        val offsetY = remember { mutableStateOf(1f) }

        Box(
            modifier = Modifier
                .clip(RectangleShape)
                .background(Color.Transparent)
                .pointerInput(Unit) {
                    if (isZoomable) {
                        forEachGesture {
                            awaitPointerEventScope {
                                awaitFirstDown()
                                do {
                                    val event = awaitPointerEvent()
                                    scale.value *= event.calculateZoom()
                                    if (scale.value > 1) {
                                        val offset = event.calculatePan()
                                        offsetX.value += offset.x
                                        offsetY.value += offset.y
                                        rotationState.value += event.calculateRotation()
                                    } else {
                                        scale.value = 1f
                                        offsetX.value = 1f
                                        offsetY.value = 1f
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        }
                    }
                }

        ) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = contentScale,
                modifier = modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        if (isZoomable) {
                            scaleX = maxOf(maxScale, minOf(minScale, scale.value))
                            scaleY = maxOf(maxScale, minOf(minScale, scale.value))
                            if (isRotation) {
                                rotationZ = rotationState.value
                            }
                            translationX = offsetX.value
                            translationY = offsetY.value
                        }
                    }
            )
        }
    }

    @ExperimentalMaterialApi
    @ExperimentalPagerApi
    @Composable
    private fun Tabs(
        pagerState: PagerState,
        scope: CoroutineScope,
        items: List<String>,
        tabSelected: (Int) -> Unit
    ) {
        TabRow(
            modifier = Modifier.fillMaxWidth(),
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                    color = Red400
                )
            },
        ) {
            items.forEachIndexed { index, string ->
                Tab(
                    text = { Text(string, fontFamily = Bender) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                        tabSelected(index)
                    },
                    selectedContentColor = Red400,
                    unselectedContentColor = White
                )
            }
        }
    }

    data class StaticMap(
        val map: String,
        val images: List<StaticMapImage>
    ) {
        data class StaticMapImage(
            val name: String,
            @DrawableRes
            val image: Int
        )
    }

    val maps = listOf(
        StaticMap(
            "Customs",
            listOf(
                StaticMap.StaticMapImage(
                    "3D",
                    R.drawable.customs
                ),
                StaticMap.StaticMapImage(
                    "2D",
                    R.drawable.customs_2d
                ),
                StaticMap.StaticMapImage(
                    "DORMS",
                    R.drawable.dorms
                )
            )
        ),
        StaticMap(
            "Factory",
            listOf(
                StaticMap.StaticMapImage(
                    "Original",
                    R.drawable.factory
                )
            )
        ),
        StaticMap(
            "Interchange",
            listOf(
                StaticMap.StaticMapImage(
                    "2D",
                    R.drawable.interchange
                ),
                StaticMap.StaticMapImage(
                    "Stashes",
                    R.drawable.interchange_stashes
                )
            )
        ),
        StaticMap(
            "Labs",
            listOf(
                StaticMap.StaticMapImage(
                    "2D",
                    R.drawable.labs
                )
            )
        ),
        StaticMap(
            "Lighthouse",
            listOf(
                StaticMap.StaticMapImage(
                    "3D",
                    R.drawable.lighthouse_vertical
                ),
                StaticMap.StaticMapImage(
                    "2D",
                    R.drawable.lighthouse_2d
                ),
                StaticMap.StaticMapImage(
                    "Landscape",
                    R.drawable.lighthouse_landscape
                )
            )
        ),
        StaticMap(
            "Reserve",
            listOf(
                StaticMap.StaticMapImage(
                    "3D",
                    R.drawable.reserve_3d
                ),
                StaticMap.StaticMapImage(
                    "Underground",
                    R.drawable.underground
                )
            )
        ),
        StaticMap(
            "Shoreline",
            listOf(
                StaticMap.StaticMapImage(
                    "2D",
                    R.drawable.shoreline
                ),
                StaticMap.StaticMapImage(
                    "Resort",
                    R.drawable.resort
                )
            )
        ),
        StaticMap(
            "Streets",
            listOf(
                StaticMap.StaticMapImage(
                    "3D",
                    R.drawable.streets_3d
                ),
                StaticMap.StaticMapImage(
                    "2D",
                    R.drawable.streets_2d
                ),
                StaticMap.StaticMapImage(
                    "2D Tycho",
                    R.drawable.streets_2d_tycho
                )
            )
        ),
        StaticMap(
            "Woods",
            listOf(
                StaticMap.StaticMapImage(
                    "2D",
                    R.drawable.woods
                )
            )
        )
    )
}