package com.austinhodak.thehideout

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.ammunition.AmmunitionListScreen
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.flea_market.FleaMarketScreen
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.gear.GearListScreen
import com.austinhodak.thehideout.gear.viewmodels.GearViewModel
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
import com.austinhodak.thehideout.weapons.builder.WeaponLoadoutScreen
import com.austinhodak.thehideout.weapons.builder.viewmodel.WeaponLoadoutViewModel
import com.austinhodak.thehideout.weapons.detail.WeaponDetailActivity
import com.austinhodak.thehideout.weapons.mods.ModsListScreen
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalPagerApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@AndroidEntryPoint
class NavActivity : GodActivity() {

    private val navViewModel: NavViewModel by viewModels()
    private val fleaViewModel: FleaViewModel by viewModels()
    private val questViewModel: QuestMainViewModel by viewModels()
    private val hideoutViewModel: HideoutMainViewModel by viewModels()
    private val keysViewModel: KeysViewModel by viewModels()
    private val gearViewModel: GearViewModel by viewModels()
    private val loadoutViewModel: WeaponLoadoutViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) {

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

    override fun onBackPressed() {
        //super.onBackPressed()
        if (navViewModel.isDrawerOpen.value == true) {
            navViewModel.setDrawerOpen(false)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.extras
        data?.let {
            it.getString("url")?.openWithCustomTab(this)
        }

        setContent {
            val scaffoldState = rememberScaffoldState()
            val coroutineScope = rememberCoroutineScope()
            val lifeCycleOwner = this
            val navController = rememberNavController()
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

            //val navBackStackEntry by navController.currentBackStackEntryAsState()

            HideoutTheme {
                systemUiController.setSystemBarsColor(
                    color = MaterialTheme.colors.primary,
                )

                /*when (val currentRoute = navBackStackEntry?.destination?.route) {
                    // do something with currentRoute
                }*/

                /*val navBackStackEntry by navController.currentBackStackEntryAsState()
                navBackStackEntry?.destination?.route?.let {
                    //navViewModel.updateCurrentNavRoute(it)
                }*/

                Scaffold(
                    scaffoldState = scaffoldState,
                    drawerContent = {
                        MainDrawer(navViewModel = navViewModel, lifeCycleOwner)
                    },
                    drawerScrimColor = Color(0xFF121212)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = tag,
                        /*enterTransition = { _, _ ->
                            fadeIn(animationSpec = tween(0))
                        },
                        exitTransition = { _, _ ->
                            fadeOut(animationSpec = tween(0))
                        }*/
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
                                tarkovRepo = tarkovRepo,
                                gearViewModel
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
                        composable("weaponloadouts") {
                            WeaponLoadoutScreen(
                                    loadoutViewModel,
                                    navViewModel,
                                    tarkovRepo
                            )
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
                                    //AuthUI.IdpConfig.TwitterBuilder().build(),
                                    //AuthUI.IdpConfig.MicrosoftBuilder().build(),
                                    //AuthUI.IdpConfig.GitHubBuilder().build(),
                                )
                                val signInIntent = AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    //.enableAnonymousUsersAutoUpgrade()
                                    .setTheme(R.style.LoginTheme)
                                    .build()
                                signInLauncher.launch(signInIntent)
                            }
                            else -> {
                                navController.navigate(route) {
                                    //launchSingleTop = true
                                    restoreState = true
                                    navController.popBackStack()
                                }
                            }
                        }
                    }

                    navViewModel.selectedDrawerItemIdentifier.observe(lifeCycleOwner) {
                        if (it != null)
                            navController.navigate(it.second)
                    }

                    //navViewModel.updateCurrentNavRoute(navBackStackEntry?.destination?.route.toString())
                }
            }
        }
    }
}