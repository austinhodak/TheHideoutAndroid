package com.austinhodak.thehideout

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.ammunition.AmmunitionListScreen
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.flea_market.FleaMarketScreen
import com.austinhodak.thehideout.flea_market.viewmodels.FleaVM
import com.austinhodak.thehideout.gear.GearListScreen
import com.austinhodak.thehideout.keys.KeyListScreen
import com.austinhodak.thehideout.medical.MedicalListScreen
import com.austinhodak.thehideout.provisions.ProvisionListScreen
import com.austinhodak.thehideout.quests.QuestMainScreen
import com.austinhodak.thehideout.quests.viewmodels.QuestMainViewModel
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.austinhodak.thehideout.views.MainDrawer
import com.austinhodak.thehideout.weapons.WeaponListScreen
import com.austinhodak.thehideout.weapons.mods.ModsListScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
    @Inject lateinit var tarkovRepo: TarkovRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val scaffoldState = rememberScaffoldState()
            val coroutineScope = rememberCoroutineScope()
            val lifeCycleOwner = this
            val navController = rememberAnimatedNavController()
            val systemUiController = rememberSystemUiController()

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
                        startDestination = "quests",
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
                                tarkovRepo = tarkovRepo)
                        }
                        composable("keys") {
                            KeyListScreen(
                                navViewModel = navViewModel,
                                tarkovRepo
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
                    }

                    navViewModel.selectedDrawerItem.observe(lifeCycleOwner) { selectedTag ->
                        if (selectedTag == null) return@observe
                        val route = selectedTag.first
                        val identifier = selectedTag.second
                        when {
                            route == "activity:sim" -> openActivity(CalculatorMainActivity::class.java)
                            route.contains("url:") -> {
                                route.split(":")[1].openWithCustomTab(this)
                            }
                            else -> navController.navigate(selectedTag.first) {

                            }
                        }
                    }
                }
            }
        }
    }
}