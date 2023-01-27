package com.austinhodak.thehideout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.adapty.Adapty
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.tarkovtracker.TTRepository
import com.austinhodak.tarkovapi.utils.ttSyncEnabled
import com.austinhodak.thehideout.utils.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
open class GodActivity : ComponentActivity() {

    @Inject
    lateinit var apolloClient: ApolloClient

    @Inject
    lateinit var ttRepository: TTRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch(Dispatchers.IO) {
            setupServerNotifications()
            setupRestockTopics()
            setupScreenOn()
            setupPlayerInfoSync()
            setupRaidAlerts()
        }

        /*Adapty.getPurchaserInfo { purchaserInfo, error ->
            if (error == null) {
                lifecycleScope.launch {
                    UserSettingsModel.isPremiumUser.update(purchaserInfo?.accessLevels?.get("premium")?.isActive == true)
                }
            }
        }*/
    }

    private fun setupRaidAlerts() {
        UserSettingsModel.pcHardwareID.observe(lifecycleScope) { id ->
            if (id.isNotEmpty()) {
                Firebase.messaging.subscribeToTopic("raid-${id}")
            }
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            ttSyncEnabledPremium {
                /*if (it)
                syncTT(lifecycleScope, ttRepository)*/
            }
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            ttSyncEnabledPremium {
               /* if (it)
                syncTT(lifecycleScope, ttRepository)*/
            }
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
        }
    }

    private fun setupPlayerInfoSync() {
        fsUser.value?.let { user ->
            val displayName = UserSettingsModel.playerIGN.value
            if (user.displayName != displayName) {
                userFirestore()?.update("displayName", displayName)
            }

            val discordUsername = UserSettingsModel.discordName.value
            if (user.discordUsername != discordUsername) {
                userFirestore()?.update("discordUsername", discordUsername)
            }

            val playerLevel = UserSettingsModel.playerLevel.value
            if (user.playerLevel != playerLevel) {
                userFirestore()?.update("playerLevel", playerLevel)
            }
        }
    }

    private fun setupScreenOn() {
        UserSettingsModel.keepScreenOn.observe(lifecycleScope) { keepOn ->
            keepScreenOn(keepOn)
        }
    }

    private fun setupServerNotifications() {
        UserSettingsModel.serverStatusNotifications.observe(lifecycleScope) {
            if (it) {
                Firebase.messaging.subscribeToTopic("serverStatus")
            } else {
                Firebase.messaging.unsubscribeFromTopic("serverStatus")
            }
        }
    }

    private fun setupRestockTopics() {
        Firebase.messaging.unsubscribeFromTopic("praporRestock")
        Firebase.messaging.unsubscribeFromTopic("therapistRestock")
        Firebase.messaging.unsubscribeFromTopic("skierRestock")
        Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock")
        Firebase.messaging.unsubscribeFromTopic("mechanicRestock")
        Firebase.messaging.unsubscribeFromTopic("ragmanRestock")
        Firebase.messaging.unsubscribeFromTopic("jaegerRestock")

        UserSettingsModel.traderRestockTime.observe(lifecycleScope) {
            val restockTime = it.name
            val prapor = UserSettingsModel.praporRestockAlert.value
            val therapist = UserSettingsModel.therapistRestockAlert.value
            val skier = UserSettingsModel.skierRestockAlert.value
            val peacekeeper = UserSettingsModel.peacekeeperRestockAlert.value
            val mechanic = UserSettingsModel.mechanicRestockAlert.value
            val ragman = UserSettingsModel.ragmanRestockAlert.value
            val jaeger = UserSettingsModel.jaegerRestockAlert.value

            Timber.d(restockTime)

            if (prapor) {
                Firebase.messaging.unsubscribeFromTopic("praporRestock1")
                Firebase.messaging.unsubscribeFromTopic("praporRestock2")
                Firebase.messaging.unsubscribeFromTopic("praporRestock5")
                Firebase.messaging.unsubscribeFromTopic("praporRestock10")
                Firebase.messaging.unsubscribeFromTopic("praporRestock15")
                Firebase.messaging.unsubscribeFromTopic("praporRestock30")

                Firebase.messaging.subscribeToTopic("praporRestock$restockTime")
            } else {
                Firebase.messaging.unsubscribeFromTopic("praporRestock")
                Firebase.messaging.unsubscribeFromTopic("praporRestock1")
                Firebase.messaging.unsubscribeFromTopic("praporRestock2")
                Firebase.messaging.unsubscribeFromTopic("praporRestock5")
                Firebase.messaging.unsubscribeFromTopic("praporRestock10")
                Firebase.messaging.unsubscribeFromTopic("praporRestock15")
                Firebase.messaging.unsubscribeFromTopic("praporRestock30")
            }

            if (therapist) {
                Firebase.messaging.unsubscribeFromTopic("therapistRestock1")
                Firebase.messaging.unsubscribeFromTopic("therapistRestock2")
                Firebase.messaging.unsubscribeFromTopic("therapistRestock5")
                Firebase.messaging.unsubscribeFromTopic("therapistRestock10")
                Firebase.messaging.unsubscribeFromTopic("therapistRestock15")
                Firebase.messaging.unsubscribeFromTopic("therapistRestock30")

                Firebase.messaging.subscribeToTopic("therapistRestock$restockTime")
            } else {
                Firebase.messaging.unsubscribeFromTopic("therapistRestock")
                Firebase.messaging.unsubscribeFromTopic("therapistRestock1")
                Firebase.messaging.unsubscribeFromTopic("therapistRestock2")
                Firebase.messaging.unsubscribeFromTopic("therapistRestock5")
                Firebase.messaging.unsubscribeFromTopic("therapistRestock10")
                Firebase.messaging.unsubscribeFromTopic("therapistRestock15")
                Firebase.messaging.unsubscribeFromTopic("therapistRestock30")
            }

            if (skier) {
                Firebase.messaging.unsubscribeFromTopic("skierRestock1")
                Firebase.messaging.unsubscribeFromTopic("skierRestock2")
                Firebase.messaging.unsubscribeFromTopic("skierRestock5")
                Firebase.messaging.unsubscribeFromTopic("skierRestock10")
                Firebase.messaging.unsubscribeFromTopic("skierRestock15")
                Firebase.messaging.unsubscribeFromTopic("skierRestock30")

                Firebase.messaging.subscribeToTopic("skierRestock$restockTime")
            } else {
                Firebase.messaging.unsubscribeFromTopic("skierRestock")
                Firebase.messaging.unsubscribeFromTopic("skierRestock1")
                Firebase.messaging.unsubscribeFromTopic("skierRestock2")
                Firebase.messaging.unsubscribeFromTopic("skierRestock5")
                Firebase.messaging.unsubscribeFromTopic("skierRestock10")
                Firebase.messaging.unsubscribeFromTopic("skierRestock15")
                Firebase.messaging.unsubscribeFromTopic("skierRestock30")
            }

            if (mechanic) {
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock1")
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock2")
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock5")
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock10")
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock15")
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock30")

                Firebase.messaging.subscribeToTopic("mechanicRestock$restockTime")
            } else {
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock")
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock1")
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock2")
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock5")
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock10")
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock15")
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock30")
            }

            if (peacekeeper) {
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock1")
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock2")
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock5")
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock10")
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock15")
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock30")

                Firebase.messaging.subscribeToTopic("peacekeeperRestock$restockTime")
            } else {
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock")
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock1")
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock2")
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock5")
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock10")
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock15")
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock30")
            }

            if (ragman) {
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock1")
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock2")
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock5")
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock10")
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock15")
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock30")

                Firebase.messaging.subscribeToTopic("ragmanRestock$restockTime")
            } else {
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock")
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock1")
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock2")
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock5")
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock10")
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock15")
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock30")
            }

            if (jaeger) {
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock1")
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock2")
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock5")
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock10")
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock15")
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock30")

                Firebase.messaging.subscribeToTopic("jaegerRestock$restockTime")
            } else {
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock")
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock1")
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock2")
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock5")
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock10")
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock15")
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock30")
            }
        }
    }
}