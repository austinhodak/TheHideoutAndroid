package com.austinhodak.thehideout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.thehideout.utils.keepScreenOn
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
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
                }
            }
        }

        Firebase.auth.currentUser?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                UserSettingsModel.playerIGN.update(it.displayName ?: "")
            }
        }
    }
}