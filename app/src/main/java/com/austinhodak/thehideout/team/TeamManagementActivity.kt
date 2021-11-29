package com.austinhodak.thehideout.team

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.adapty.models.ProductModel
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.DarkGrey
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.compose.theme.Red400
import com.austinhodak.thehideout.firebase.Team
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.team.viewmodels.TeamManagementViewModel
import com.austinhodak.thehideout.utils.questsFirebase
import com.austinhodak.thehideout.utils.uid
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.dynamiclinks.ShortDynamicLink
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLink
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TeamManagementActivity : GodActivity() {

    private val viewModel: TeamManagementViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HideoutTheme {

                val userData by viewModel.userData.observeAsState()

                Scaffold(
                    topBar = {
                        TopBar()
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            text = {
                                Text(text = "JOIN TEAM", color = Color.Black)
                            },
                            onClick = {

                            },
                            icon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_link_24),
                                    contentDescription = "",
                                    tint = Color.Black
                                )
                            }
                        )
                    }
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        userData?.teams?.forEach {
                            val teamID = it.key
                            item {
                                TeamCard(teamID)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TeamCard(teamID: String?) {
        var team by remember { mutableStateOf<Team?>(null) }

        questsFirebase.child("teams/$teamID").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    team = snapshot.getValue(Team::class.java)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        team?.let {
            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .fillMaxWidth(),
                backgroundColor = DarkGrey
            ) {
                Column(
                    Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = it.name ?: "", style = MaterialTheme.typography.h6)
                        Spacer(modifier = Modifier.weight(1f))
                        if (it.members?.get(uid())?.owner == true) {
                            IconButton(onClick = { }, Modifier.size(20.dp)) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_edit_24), contentDescription = "Edit Team")
                            }
                        }
                    }

                    it.members?.forEach {
                        TeamMemberItem(id = it.key)
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(modifier = Modifier.padding(end = 8.dp), onClick = {
                            createInviteLink(team, teamID)
                        }) {
                            Text(text = "LEAVE", color = Color.Gray)
                        }
                        TextButton(onClick = {
                            createInviteLink(team, teamID)
                        }) {
                            Text(text = "INVITE", color = Red400)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun TeamMemberItem(id: String) {
        var teamMember by remember { mutableStateOf<User?>(null) }
        questsFirebase.child("users/$id")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        teamMember = snapshot.getValue(User::class.java)
                        teamMember?.uid = id
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        teamMember?.let {
            Row(
                Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    "${it.getUsername()}${
                        if (it.uid == uid()) {
                            " (You)"
                        } else ""
                    }"
                )
            }
        }
    }

    private fun createInviteLink(team: Team?, teamID: String?) {
        if (team != null && teamID != null) {
            val shortLinkTask =
                Firebase.dynamicLinks.shortLinkAsync(ShortDynamicLink.Suffix.SHORT) {
                    link = Uri.parse("https://thehideout.io/invite/$teamID")
                    domainUriPrefix = "https://thehideout.io/invite"
                    androidParameters("com.austinhodak.thehideout") {

                    }
                }.addOnSuccessListener {
                    it.shortLink?.let { it1 -> shareLink(it1) }
                }

        }
    }

    private fun shareLink(link: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, link.toString())

        startActivity(Intent.createChooser(intent, "Share Link"))
    }

    @Composable
    private fun TopBar() {
        TopAppBar(
            title = { Text("Team Management") },
            navigationIcon = {
                IconButton(onClick = {
                    onBackPressed()
                }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                }
            },
            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
            actions = {
                IconButton(onClick = {

                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_baseline_add_circle_24),
                        contentDescription = "",
                        tint = Color.White
                    )
                }
            }
        )
    }
}