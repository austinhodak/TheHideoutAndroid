package com.austinhodak.thehideout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.thehideout.utils.keepScreenOn
import com.austinhodak.thehideout.utils.userRefTracker
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class GodActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        UserSettingsModel.keepScreenOn.observe(lifecycleScope) { keepOn ->
            keepScreenOn(keepOn)
        }

        UserSettingsModel.playerIGN.observe(lifecycleScope) { name ->
            Firebase.auth.currentUser?.let { user ->
                val profileUpdate = userProfileChangeRequest {
                    displayName = name
                }

                if (user.displayName != name) {
                    user.updateProfile(profileUpdate)
                    userRefTracker("displayName").setValue(name)
                }
            }
        }

        Firebase.auth.currentUser?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                UserSettingsModel.playerIGN.update(it.displayName ?: "")
                userRefTracker("displayName").setValue(it.displayName)
            }
        }

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
    }
}