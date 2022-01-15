package com.austinhodak.thehideout

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.annotation.ExperimentalCoilApi
import com.afollestad.materialdialogs.MaterialDialog
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.enums.ItemTypes
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.ammunition.AmmunitionListScreen
import com.austinhodak.thehideout.bitcoin.BitcoinPriceScreen
import com.austinhodak.thehideout.calculator.CalculatorMainActivity
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.currency.CurrenyConverterScreen
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
import com.austinhodak.thehideout.news.NewsScreen
import com.austinhodak.thehideout.provisions.ProvisionListScreen
import com.austinhodak.thehideout.quests.QuestMainScreen
import com.austinhodak.thehideout.quests.viewmodels.QuestMainViewModel
import com.austinhodak.thehideout.skills.CharacterSkillsScreen
import com.austinhodak.thehideout.team.TeamManagementActivity
import com.austinhodak.thehideout.tools.SensitivityCalculatorScreen
import com.austinhodak.thehideout.tools.viewmodels.SensitivityViewModel
import com.austinhodak.thehideout.traders.RestockTimersScreen
import com.austinhodak.thehideout.traders.TraderScreen
import com.austinhodak.thehideout.utils.acceptTeamInvite
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.austinhodak.thehideout.utils.restartNavActivity
import com.austinhodak.thehideout.views.MainDrawer
import com.austinhodak.thehideout.weapons.WeaponListScreen
import com.austinhodak.thehideout.weapons.builder.WeaponLoadoutScreen
import com.austinhodak.thehideout.weapons.builder.viewmodel.WeaponLoadoutViewModel
import com.austinhodak.thehideout.weapons.detail.WeaponDetailActivity
import com.austinhodak.thehideout.weapons.mods.ModsListScreen
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.skydoves.only.only
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
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
    private val sensitivityViewModel: SensitivityViewModel by viewModels()

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) {
        val response = it.idpResponse

        if (it.resultCode == RESULT_OK) {
            //USER SIGNED IN
            restartNavActivity()
        } else {
            if (response?.error?.errorCode == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT) {
                val nonAnonymousCredential = response.credentialForLinking
                nonAnonymousCredential?.let {
                    FirebaseAuth.getInstance().signInWithCredential(nonAnonymousCredential).addOnSuccessListener {
                        restartNavActivity()
                    }
                }
            }
        }
    }

    private val weaponDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == RESULT_OK) {
            val caliber = res.data?.getStringExtra("caliber")
            if (caliber != null) {
                navViewModel.drawerItemSelected(Pair((101).toLong(), caliber))
                navViewModel.clearSearch()
                navViewModel.setSearchOpen(false)
            }
        }
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        //super.onBackPressed()
        if (navViewModel.isDrawerOpen.value == true) {
            navViewModel.setDrawerOpen(false)
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Press BACK again to exit.", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupReviewPopup()

        val data = intent.extras
        data?.let {
            it.getString("url")?.openWithCustomTab(this)
        }

        setupDynamicLinks()

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
                        MainDrawer(navViewModel = navViewModel, lifeCycleOwner, this@NavActivity, apolloClient)
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
                        composable("bitcoin") {
                            BitcoinPriceScreen(navViewModel, tarkovRepo)
                        }
                        composable("currency_converter") {
                            CurrenyConverterScreen(navViewModel, tarkovRepo)
                        }
                        composable("sensitivity") {
                            SensitivityCalculatorScreen(navViewModel, tarkovRepo, sensitivityViewModel, armorPickerLauncher)
                        }
                        composable("trader/{trader}") {
                            TraderScreen(
                                it.arguments?.getString("trader"),
                                navViewModel,
                                tarkovRepo,
                                apolloClient
                            )
                        }
                        composable("news") {
                            NewsScreen(
                                navViewModel
                            )
                        }
                        composable("skills") {
                            CharacterSkillsScreen(navViewModel = navViewModel)
                        }
                        composable("restock") {
                            RestockTimersScreen(
                                navViewModel,
                                apolloClient
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
                                val customLayout = AuthMethodPickerLayout
                                    .Builder(R.layout.login_picker)
                                    .setEmailButtonId(R.id.login_email)
                                    .setPhoneButtonId(R.id.login_phone)
                                    .setGoogleButtonId(R.id.login_google)
                                    .setGithubButtonId(R.id.login_github)
                                    .setFacebookButtonId(R.id.login_facebook)
                                    .build()
                                //openActivity(LoginActivity::class.java)
                                val providers = arrayListOf(
                                    AuthUI.IdpConfig.EmailBuilder().build(),
                                    AuthUI.IdpConfig.PhoneBuilder().build(),
                                    AuthUI.IdpConfig.GoogleBuilder().build(),
                                    AuthUI.IdpConfig.GitHubBuilder().build(),
                                    AuthUI.IdpConfig.FacebookBuilder().build(),
                                    //AuthUI.IdpConfig.FacebookBuilder().build(),
                                    //AuthUI.IdpConfig.TwitterBuilder().build(),
                                    //AuthUI.IdpConfig.MicrosoftBuilder().build(),
                                    //AuthUI.IdpConfig.GitHubBuilder().build(),
                                )
                                val signInIntent = AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .enableAnonymousUsersAutoUpgrade()
                                    .setAuthMethodPickerLayout(customLayout)
                                    .setTheme(R.style.LoginTheme2)
                                    .setLogo(R.drawable.hideout_shadow_1)
                                    .setIsSmartLockEnabled(false)
                                    //.setAlwaysShowSignInMethodScreen(true)
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

                    coroutineScope.launch {
                        //TODO Default drawer selection still stays selected, needs fixed but works for now.
                        delay(100)
                        if (intent.hasExtra("fromNoti") && intent.hasExtra("trader")) {
                            val trader = "trader/${intent.getStringExtra("trader")?.lowercase() ?: "prapor"}"
                            when (intent.getStringExtra("trader")?.lowercase() ?: "prapor") {
                                "prapor" -> navViewModel.drawerItemSelected(Pair((501).toLong(), trader))
                                "therapist" -> navViewModel.drawerItemSelected(Pair((502).toLong(), trader))
                                "skier" -> navViewModel.drawerItemSelected(Pair((503).toLong(), trader))
                                "peacekeeper" -> navViewModel.drawerItemSelected(Pair((504).toLong(), trader))
                                "mechanic" -> navViewModel.drawerItemSelected(Pair((505).toLong(), trader))
                                "ragman" -> navViewModel.drawerItemSelected(Pair((506).toLong(), trader))
                                "jaeger" -> navViewModel.drawerItemSelected(Pair((507).toLong(), trader))
                            }
                        }
                    }

                    //navViewModel.updateCurrentNavRoute(navBackStackEntry?.destination?.route.toString())
                }
            }
        }
    }

    private fun setupDynamicLinks() {
        Firebase.dynamicLinks.getDynamicLink(intent).addOnSuccessListener(this) { pendingDynamicLinkData ->
            if (pendingDynamicLinkData != null) {
                var deepLink = pendingDynamicLinkData.link
                MaterialDialog(this).show {
                    title(text = "Join Team?")
                    message(text = "You've clicked an invite link, do you want to join this team?")
                    positiveButton(text = "JOIN") {
                        deepLink?.acceptTeamInvite {
                            Toast.makeText(this@NavActivity, "Team joined successfully!", Toast.LENGTH_SHORT).show()
                            openActivity(TeamManagementActivity::class.java)
                        }
                    }
                    negativeButton(text = "NEVERMIND")
                }

                Timber.d(deepLink?.lastPathSegment)
            }
        }
    }

    private fun setupReviewPopup() {
        val manager = ReviewManagerFactory.create(this)
        only("reviewPopup", times = 5) {
            onDone {
                val request = manager.requestReviewFlow()
                request.addOnSuccessListener { reviewInfo ->
                    Timber.d("LAUNCHING REVIEW")
                    manager.launchReviewFlow(this@NavActivity, reviewInfo)
                }
            }
        }
    }

    private val armorPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val intent = result.data
            intent?.getSerializableExtra("item")?.let {
                if (it is Item) {
                    if (it.itemType == ItemTypes.HELMET) {
                        sensitivityViewModel.selectHelmet(it)
                    } else {
                        sensitivityViewModel.selectArmor(it)
                    }
                }
            }
        }
    }
}