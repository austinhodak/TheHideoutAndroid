package com.austinhodak.thehideout.features.premium

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import coil.annotation.ExperimentalCoilApi
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.features.premium.viewmodels.PremiumPusherState
import com.austinhodak.thehideout.features.premium.viewmodels.PremiumViewModel
import com.austinhodak.thehideout.ui.theme3.HideoutTheme3
import com.austinhodak.thehideout.ui.theme3.premium_gradient_color
import com.austinhodak.thehideout.ui.theme3.rubik
import com.austinhodak.thehideout.utils.openActivity
import com.google.accompanist.pager.ExperimentalPagerApi
import com.maxkeppeker.sheets.core.models.base.rememberSheetState
import com.maxkeppeler.sheets.state.StateDialog
import com.maxkeppeler.sheets.state.models.ProgressIndicator
import com.maxkeppeler.sheets.state.models.State
import com.maxkeppeler.sheets.state.models.StateConfig
import com.qonversion.android.sdk.dto.products.QProductDuration
import com.qonversion.android.sdk.dto.products.QProductType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalPagerApi
@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class PremiumPusherActivity : ComponentActivity() {

    @OptIn(
        ExperimentalLayoutApi::class,
        ExperimentalAnimationGraphicsApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val viewModel: PremiumViewModel = mavericksViewModel()

            HideoutTheme3(
                darkTheme = true,
                dynamicColor = false
            ) {
                val snackbarState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                val offers by viewModel.collectAsState(PremiumPusherState::mainOffering)
                val snackbar by viewModel.collectAsState(PremiumPusherState::snackbarText)
                val isProcessing by viewModel.collectAsState(PremiumPusherState::isProcessingPurchase)
                val entitlements by viewModel.collectAsState(PremiumPusherState::entitlement)

                if (entitlements != null) {
                    if (intent.hasExtra("setResult")) {
                        openActivity(PremiumThanksActivity::class.java)
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        openActivity(PremiumThanksActivity::class.java) {
                            putBoolean("restart", true)
                        }
                        finish()
                        //restartNavActivity()
                    }
                }

                val state = State.Loading(labelText = "Processing...", ProgressIndicator.Linear())
                val sheetState = rememberSheetState()
                StateDialog(
                    state = sheetState,
                    config = StateConfig(state = state)
                )

                LaunchedEffect(isProcessing) {
                    scope.launch {
                        if (isProcessing)
                            sheetState.show()
                        else sheetState.hide()
                    }
                }

                LaunchedEffect(snackbar) {
                    if (snackbar != null) {
                        scope.launch {
                            snackbarState.showSnackbar(
                                snackbar!!
                            )
                        }
                    }
                }


                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarState) },
                    modifier = Modifier
                        .fillMaxSize()
                ) { padding ->
                    Image(
                        painter = painterResource(id = R.drawable.g7z8a1hc71f51),
                        contentDescription = "Background Image",
                        alignment = Center,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    0f to premium_gradient_color.copy(alpha = 0f),
                                    0.4f to premium_gradient_color,
                                    1f to premium_gradient_color
                                )
                            )
                    )
                    AnimatedVisibility(
                        visible = offers?.products == null,
                        modifier = Modifier
                            .fillMaxSize()
                            .consumedWindowInsets(padding)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Center)
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = offers?.products != null,
                        enter = EnterTransition.None,
                        exit = ExitTransition.None,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding(),
                        ) {
                            Row(
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(top = 8.dp),
                                verticalAlignment = CenterVertically
                            ) {
                                //Exit button
                                FilledIconButton(
                                    onClick = {
                                        finish()
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color.Black.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                //Restore Button
                                TextButton(onClick = {
                                    viewModel.restorePurchases()
                                }) {
                                    Text(
                                        text = "Restore",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            //Spacer(modifier = Modifier.weight(1f))
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 32.dp),
                                horizontalAlignment = CenterHorizontally,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                val image =
                                    AnimatedImageVector.animatedVectorResource(R.drawable.animated_icon)
                                var atEnd by remember { mutableStateOf(false) }

                                LaunchedEffect(image) {
                                    atEnd = true
                                }

                                Image(
                                    painter = rememberAnimatedVectorPainter(image, atEnd),
                                    contentDescription = "Your content description",
                                    modifier = Modifier
                                        .height(120.dp)
                                        .width(120.dp)
                                    //.padding(bottom = 64.dp)
                                )
                                Text(
                                    text = "The Hideout",
                                    fontSize = 34.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontFamily = rubik
                                )
                                Text(
                                    text = "Unlimited Access to Premium Content & Features",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.padding(top = 12.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontFamily = rubik,
                                    textAlign = TextAlign.Center
                                )
                                val points = listOf(
                                    "No Ads. Ever.",
                                    "Custom Map Markers",
                                    "Unlimited Price Alerts",
                                    "1 Year Price History",
                                    //"Tarkov Tracker Integration",
                                    "Discord Role",
                                    "More coming soon!"
                                )
                                //Spacer(modifier = Modifier.padding(top = 64.dp))
                                Column(
                                    modifier = Modifier
                                        .padding(
                                            top = 12.dp, bottom = 12.dp
                                        )
                                ) {
                                    points.forEach {
                                        Row(
                                            verticalAlignment = CenterVertically,
                                            horizontalArrangement = Arrangement.Start,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.ArrowForward,
                                                null,
                                                modifier = Modifier.padding(end = 16.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Text(
                                                text = it,
                                                fontFamily = rubik,
                                                fontWeight = FontWeight.Light,
                                                fontSize = 18.sp,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                                //Spacer(modifier = Modifier.padding(top = 64.dp))
                                Column(
                                    modifier = Modifier
                                        .animateEnterExit(
                                            enter = slideInVertically { it }
                                        )
                                        .padding(bottom = 32.dp)
                                ) {
                                    offers?.products?.sortedByDescending { it.skuDetail?.priceAmountMicros }
                                        ?.forEach { offer ->
                                            if (offer.type == QProductType.InApp) {
                                                Button(
                                                    onClick = {
                                                        viewModel.purchase(
                                                            this@PremiumPusherActivity,
                                                            offer
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .padding(top = 8.dp)
                                                        .fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(8.dp),
                                                        horizontalAlignment = CenterHorizontally
                                                    ) {
                                                        Text(
                                                            text = "${offer.prettyPrice}/Lifetime",
                                                            style = MaterialTheme.typography.titleLarge,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            fontFamily = rubik
                                                        )
                                                        Text(
                                                            text = "Most Popular",
                                                            fontWeight = FontWeight.Normal,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            fontFamily = rubik,
                                                            fontSize = 16.sp
                                                        )
                                                    }
                                                }
                                            }
                                            if (offer.type == QProductType.Trial) {
                                                Button(
                                                    onClick = {
                                                        viewModel.purchase(
                                                            this@PremiumPusherActivity,
                                                            offer
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .padding(top = 8.dp)
                                                        .fillMaxWidth(),
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = MaterialTheme.colorScheme.onPrimary
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(8.dp),
                                                        horizontalAlignment = CenterHorizontally
                                                    ) {
                                                        val title = when (offer.duration) {
                                                            QProductDuration.Monthly -> {
                                                                "${offer.prettyPrice}/Month"
                                                            }

                                                            QProductDuration.Annual -> {
                                                                "${offer.prettyPrice}/Year"
                                                            }

                                                            else -> {
                                                                ""
                                                            }
                                                        }
                                                        Text(
                                                            text = title,
                                                            style = MaterialTheme.typography.titleLarge,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            fontFamily = rubik
                                                        )
                                                        Text(
                                                            text = "with 7-day Free Trial",
                                                            fontWeight = FontWeight.Normal,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            fontFamily = rubik,
                                                            fontSize = 16.sp
                                                        )
                                                    }
                                                }
                                            }
                                            if (offer.type == QProductType.Subscription) {
                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.purchase(
                                                            this@PremiumPusherActivity,
                                                            offer
                                                        )
                                                    },
                                                    modifier = Modifier
                                                        .padding(top = 8.dp)
                                                        .fillMaxWidth(),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        color = MaterialTheme.colorScheme.onSecondary
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(8.dp),
                                                        horizontalAlignment = CenterHorizontally
                                                    ) {
                                                        val title = when (offer.duration) {
                                                            QProductDuration.Monthly -> {
                                                                "Monthly"
                                                            }

                                                            QProductDuration.Annual -> {
                                                                "Yearly"
                                                            }

                                                            else -> {
                                                                ""
                                                            }
                                                        }
                                                        Text(
                                                            text = "${offer.prettyPrice}",
                                                            style = MaterialTheme.typography.titleLarge,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            fontFamily = rubik
                                                        )
                                                        Text(
                                                            text = title,
                                                            fontWeight = FontWeight.Normal,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                            fontFamily = rubik,
                                                            fontSize = 16.sp
                                                        )
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
        }
    }
}


