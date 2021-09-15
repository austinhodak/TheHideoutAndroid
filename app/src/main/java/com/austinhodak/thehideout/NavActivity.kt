package com.austinhodak.thehideout

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.ammunition.AmmunitionListScreen
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.flea_market.FleaMarketScreen
import com.austinhodak.thehideout.flea_market.viewmodels.FleaVM
import com.austinhodak.thehideout.gear.GearListScreen
import com.austinhodak.thehideout.hideout.HideoutMainScreen
import com.austinhodak.thehideout.hideout.viewmodels.HideoutMainViewModel
import com.austinhodak.thehideout.keys.KeyListScreen
import com.austinhodak.thehideout.keys.viewmodels.KeysViewModel
import com.austinhodak.thehideout.map.MapsActivity
import com.austinhodak.thehideout.medical.MedicalListScreen
import com.austinhodak.thehideout.provisions.ProvisionListScreen
import com.austinhodak.thehideout.quests.QuestMainScreen
import com.austinhodak.thehideout.quests.viewmodels.QuestMainViewModel
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.austinhodak.thehideout.views.MainDrawer
import com.austinhodak.thehideout.weapons.WeaponListScreen
import com.austinhodak.thehideout.weapons.detail.WeaponDetailActivity
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
import timber.log.Timber
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

    private val weaponDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == RESULT_OK) {
            val caliber = res.data?.getStringExtra("caliber")
            if (caliber != null) {
                navViewModel.drawerItemSelected(Pair((101).toLong(), caliber))
            }
        }
    }

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scaffoldState = rememberScaffoldState()
            val coroutineScope = rememberCoroutineScope()
            val lifeCycleOwner = this
            val navController = rememberAnimatedNavController()
            val systemUiController = rememberSystemUiController()

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
                        MainDrawer(navViewModel = navViewModel, lifeCycleOwner)
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
                            Timber.d("MEDICAL SELECTED")

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
                            ) {
                                val intent = Intent(this@NavActivity, WeaponDetailActivity::class.java).apply {
                                    putExtra("weaponID", it)
                                }
                                weaponDetailLauncher.launch(intent)
                            }
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

                        }
                    }

                    navViewModel.selectedDrawerItem.observe(lifeCycleOwner) { selectedItem ->
                        if (selectedItem == null) return@observe
                        val route = selectedItem.tag.toString()
                        val identifier = selectedItem.identifier.toInt()
                        when {
                            route == "activity:sim" -> openActivity(CalculatorMainActivity::class.java)
                            route == "activity:map" -> openActivity(MapsActivity::class.java)
                            route.contains("url:") -> {
                                route.split(":")[1].openWithCustomTab(this@NavActivity)
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
                            else -> {
                                Timber.d(route)
                                navController.navigate(route) {

                                }
                            }
                        }
                    }

                    navViewModel.selectedDrawerItemIdentifier.observe(lifeCycleOwner) {
                        if (it != null)
                        navController.navigate(it.second)
                    }
                }
            }
        }
    }
}