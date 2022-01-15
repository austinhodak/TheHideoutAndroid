package com.austinhodak.thehideout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo3.ApolloClient
import com.austinhodak.tarkovapi.ServerStatusQuery
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.models.toObj
import com.austinhodak.thehideout.utils.keepScreenOn
import com.austinhodak.thehideout.utils.userRefTracker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
open class GodActivity : AppCompatActivity() {

    @Inject
    lateinit var apolloClient: ApolloClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UserSettingsModel.serverStatusNotifications.observe(lifecycleScope) {
            if (it) {
                Firebase.messaging.subscribeToTopic("serverStatus")
            } else {
                Firebase.messaging.unsubscribeFromTopic("serverStatus")
            }
        }

        setupRestockTopics()

        UserSettingsModel.keepScreenOn.observe(lifecycleScope) { keepOn ->
            keepScreenOn(keepOn)
        }

        /*UserSettingsModel.playerIGN.observe(lifecycleScope) { name ->
            Firebase.auth.currentUser?.let { user ->
                val profileUpdate = userProfileChangeRequest {
                    displayName = name
                }

                if (user.displayName != name) {
                    user.updateProfile(profileUpdate)
                    userRefTracker("displayName").setValue(name)
                }
            }
        }*/

        /*Firebase.auth.currentUser?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                UserSettingsModel.playerIGN.update(it.displayName ?: "")
                userRefTracker("displayName").setValue(it.displayName)
            }
        }*/

        userRefTracker("displayName").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.value != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        UserSettingsModel.playerIGN.update(snapshot.value as String)
                    }
                }

                UserSettingsModel.playerIGN.observe(lifecycleScope) { name ->
                    userRefTracker("displayName").setValue(name)
                    updateDisplayName(name)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        userRefTracker("discordUsername").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.value != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        UserSettingsModel.discordName.update(snapshot.value as String)
                    }
                }


                UserSettingsModel.discordName.observe(lifecycleScope) { name ->
                    userRefTracker("discordUsername").setValue(name)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        userRefTracker("playerLevel").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.value != null) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        UserSettingsModel.playerLevel.update((snapshot.value as Long).toInt())
                    }
                }


                UserSettingsModel.playerLevel.observe(lifecycleScope) { name ->
                    userRefTracker("playerLevel").setValue(name)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun setupRestockTopics() {
        UserSettingsModel.praporRestockAlert.observe(lifecycleScope) {
            if (it) {
                Firebase.messaging.subscribeToTopic("praporRestock")
            } else {
                Firebase.messaging.unsubscribeFromTopic("praporRestock")
            }
        }

        UserSettingsModel.therapistRestockAlert.observe(lifecycleScope) {
            if (it) {
                Firebase.messaging.subscribeToTopic("therapistRestock")
            } else {
                Firebase.messaging.unsubscribeFromTopic("therapistRestock")
            }
        }

        UserSettingsModel.skierRestockAlert.observe(lifecycleScope) {
            if (it) {
                Firebase.messaging.subscribeToTopic("skierRestock")
            } else {
                Firebase.messaging.unsubscribeFromTopic("skierRestock")
            }
        }

        UserSettingsModel.peacekeeperRestockAlert.observe(lifecycleScope) {
            if (it) {
                Firebase.messaging.subscribeToTopic("peacekeeperRestock")
            } else {
                Firebase.messaging.unsubscribeFromTopic("peacekeeperRestock")
            }
        }

        UserSettingsModel.mechanicRestockAlert.observe(lifecycleScope) {
            if (it) {
                Firebase.messaging.subscribeToTopic("mechanicRestock")
            } else {
                Firebase.messaging.unsubscribeFromTopic("mechanicRestock")
            }
        }

        UserSettingsModel.ragmanRestockAlert.observe(lifecycleScope) {
            if (it) {
                Firebase.messaging.subscribeToTopic("ragmanRestock")
            } else {
                Firebase.messaging.unsubscribeFromTopic("ragmanRestock")
            }
        }

        UserSettingsModel.jaegerRestockAlert.observe(lifecycleScope) {
            if (it) {
                Firebase.messaging.subscribeToTopic("jaegerRestock")
            } else {
                Firebase.messaging.unsubscribeFromTopic("jaegerRestock")
            }
        }
    }

    private fun updateDisplayName(name: String) {
        Firebase.auth.currentUser?.let { user ->
            val profileUpdate = userProfileChangeRequest {
                displayName = name
            }

            if (user.displayName != name) {
                user.updateProfile(profileUpdate)
            }
        }
    }
}