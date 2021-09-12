package com.austinhodak.thehideout

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.ammunition.AmmunitionListScreen
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.compose.theme.itemGreen
import com.austinhodak.thehideout.flea_market.FleaMarketScreen
import com.austinhodak.thehideout.flea_market.viewmodels.FleaVM
import com.austinhodak.thehideout.gear.GearListScreen
import com.austinhodak.thehideout.hideout.HideoutMainScreen
import com.austinhodak.thehideout.hideout.viewmodels.HideoutMainViewModel
import com.austinhodak.thehideout.keys.KeyListScreen
import com.austinhodak.thehideout.keys.viewmodels.KeysViewModel
import com.austinhodak.thehideout.medical.MedicalListScreen
import com.austinhodak.thehideout.provisions.ProvisionListScreen
import com.austinhodak.thehideout.quests.QuestMainScreen
import com.austinhodak.thehideout.quests.viewmodels.QuestMainViewModel
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.austinhodak.thehideout.views.MainDrawer
import com.austinhodak.thehideout.weapons.WeaponListScreen
import com.austinhodak.thehideout.weapons.mods.ModsListScreen
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.*
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.MapUI
import ovh.plrapps.mapcompose.ui.layout.Fill
import ovh.plrapps.mapcompose.ui.state.MapState
import timber.log.Timber
import java.io.BufferedInputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

@ExperimentalCoroutinesApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class NavActivity : AppCompatActivity() {

    private val navViewModel: NavViewModel by viewModels()
    private val fleaViewModel: FleaVM by viewModels()
    private val questViewModel: QuestMainViewModel by viewModels()
    private val hideoutViewModel: HideoutMainViewModel by viewModels()
    private val keysViewModel: KeysViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->

    }

    @Composable
    fun MapContainer(
        modifier: Modifier = Modifier, viewModel: NavViewModel
    ) {

    }

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tileStreamProvider = makeTileStreamProvider()

        val state: MapState by mutableStateOf(
            /* Notice how we increase the worker count when performing HTTP requests */
            MapState(8, 32512, 32512, tileStreamProvider, workerCount = 16).apply {
                minimumScaleMode = Fill
                shouldLoopScale = true
                scale = 0f
                //X = 0.4706
                //Y = 0.3434

                //LAT Y 0.9268
                //LON X -0.7491

                //DEAD SCAV
                //Y: "0.66941039301040" //Y .5290
                //X: "-0.77709660313690" //X .4520

                //DORM 203
                //Y: "0.92680509170080 //Y .3434
                //X: "-0.74914442273439" //X .4706
                addMarker("test", 0.4520, 0.5290) {
                    Icon(
                        painterResource(id = R.drawable.icons8_key_100),
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = Color(0xCC2196F3)
                    )
                }
                onTap { x, y ->
                    Toast.makeText(this@NavActivity, "$x $y", Toast.LENGTH_SHORT).show()
                }
            }
        )

        setContent {
            val scaffoldState = rememberScaffoldState()
            val coroutineScope = rememberCoroutineScope()
            val lifeCycleOwner = this
            val navController = rememberAnimatedNavController()
            val systemUiController = rememberSystemUiController()

            //val selectedItem by navViewModel.selectedDrawerItem.observeAsState(null)

            val tag: String = navViewModel.selectedDrawerItem.value?.tag?.toString() ?: questPrefs.openingPageTag

            navViewModel.isDrawerOpen.observe(lifeCycleOwner) { isOpen ->
                coroutineScope.launch {
                    if (isOpen) {
                        scaffoldState.drawerState.open()
                    } else {
                        scaffoldState.drawerState.close()
                    }
                }
            }

            HideoutTheme {
                systemUiController.setSystemBarsColor(
                    color = MaterialTheme.colors.primary,
                )

                Scaffold(
                    scaffoldState = scaffoldState,
                    drawerContent = {
                        MainDrawer(navViewModel = navViewModel)
                    },
                    drawerScrimColor = Color(0xFF121212)
                ) {
                    AnimatedNavHost(
                        navController = navController,
                        startDestination = tag,
                        enterTransition = { _, _ ->
                            fadeIn(animationSpec = tween(0))
                        },
                        exitTransition = { _, _ ->
                            fadeOut(animationSpec = tween(0))
                        }
                    ) {
                        composable("ammunition/{caliber}") {
                            AmmunitionListScreen(
                                caliber = it.arguments?.getString("caliber", null),
                                navViewModel = navViewModel,
                                tarkovRepo = tarkovRepo
                            )
                        }
                        composable("gear/{category}") {
                            GearListScreen(
                                category = it.arguments?.getString("category", null),
                                navViewModel = navViewModel,
                                tarkovRepo = tarkovRepo
                            )
                        }
                        composable("keys") {
                            KeyListScreen(
                                navViewModel = navViewModel,
                                tarkovRepo,
                                keysViewModel
                            )
                        }
                        composable("medical") {
                            MedicalListScreen(
                                tarkovRepo,
                                navViewModel = navViewModel
                            )
                        }
                        composable("food") {
                            ProvisionListScreen(navViewModel = navViewModel, tarkovRepo = tarkovRepo)
                        }
                        composable("flea") {
                            FleaMarketScreen(navViewModel = navViewModel, fleaViewModel = fleaViewModel, tarkovRepo = tarkovRepo)
                        }
                        composable("weaponmods") {
                            ModsListScreen(
                                tarkovRepo,
                                navViewModel
                            )
                        }
                        composable("weapons/{classID}") {
                            WeaponListScreen(
                                classID = it.arguments?.getString("classID") ?: "assaultRifle",
                                navViewModel,
                                tarkovRepo
                            )
                        }
                        composable("quests") {
                            QuestMainScreen(
                                navViewModel,
                                questViewModel,
                                tarkovRepo
                            )
                        }
                        composable("hideout") {
                            HideoutMainScreen(
                                navViewModel,
                                hideoutViewModel,
                                tarkovRepo
                            )
                        }
                        composable("maps") {
                            Box(
                                Modifier
                                    .fillMaxSize()
                                    .background(Red400)
                            ) {
                                MapUI(Modifier.background(itemGreen), state = state)
                            }
                        }
                    }

                    navViewModel.selectedDrawerItem.observe(lifeCycleOwner) { selectedItem ->
                        if (selectedItem == null) return@observe
                        val route = selectedItem.tag.toString()
                        val identifier = selectedItem.identifier.toInt()
                        when {
                            route == "activity:sim" -> openActivity(CalculatorMainActivity::class.java)
                            route.contains("url:") -> {
                                route.split(":")[1].openWithCustomTab(this)
                            }
                            identifier == 999 -> {
                                val providers = arrayListOf(
                                    AuthUI.IdpConfig.EmailBuilder().build(),
                                    AuthUI.IdpConfig.PhoneBuilder().build(),
                                    AuthUI.IdpConfig.GoogleBuilder().build(),
                                    //AuthUI.IdpConfig.FacebookBuilder().build(),
                                    AuthUI.IdpConfig.TwitterBuilder().build(),
                                    AuthUI.IdpConfig.MicrosoftBuilder().build(),
                                    AuthUI.IdpConfig.GitHubBuilder().build(),
                                )
                                val signInIntent = AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .enableAnonymousUsersAutoUpgrade()
                                    .build()
                                signInLauncher.launch(signInIntent)
                            }
                            else -> navController.navigate(route) {

                            }
                        }
                    }
                }
            }
        }
    }

    private fun makeTileStreamProvider() =
        TileStreamProvider { row, col, zoomLvl ->
            Timber.d("$row $col $zoomLvl")
            try {
                //val url = URL("https://plrapps.ovh:8080/mapcompose-tile/$zoomLvl/$row/$col.jpg")

                val xx = when (zoomLvl + 8) {
                    8 -> "127"
                    9 -> (254 + row).toString()
                    10 -> (508 + row).toString()
                    11 -> (1016 + row).toString()
                    12 -> (2032 + row).toString()
                    13 -> (4064 + row).toString()
                    14 -> (8128 + row).toString()
                    15 -> (16256 + row).toString()
                    else -> "127"
                }

                val yy = when (zoomLvl + 8) {
                    8 -> "8/127"
                    9 -> (254 + col).toString()
                    10 -> (508 + col).toString()
                    11 -> (1016 + col).toString()
                    12 -> (2032 + col).toString()
                    13 -> (4064 + col).toString()
                    14 -> (8128 + col).toString()
                    15 -> (16256 + col).toString()
                    else -> "8/127"
                }

                Timber.d("https://cdn.mapgenie.io/images/tiles/tarkov/customs/default-v3/${zoomLvl + 8}/$yy/$xx.png")

                val x = 1017 + row
                val y = (1016 + col).toString()
                val url = URL("https://cdn.mapgenie.io/images/tiles/tarkov/customs/default-v3/${zoomLvl + 8}/$yy/$xx.png")
                val connection = url.openConnection() as HttpURLConnection
                connection.useCaches = true
                connection.doInput = true
                connection.connect()
                BufferedInputStream(connection.inputStream)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}