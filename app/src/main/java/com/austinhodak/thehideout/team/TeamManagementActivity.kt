package com.austinhodak.thehideout.team

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.ColorPalette
import com.afollestad.materialdialogs.color.colorChooser
import com.afollestad.materialdialogs.input.input
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.DarkGrey
import com.austinhodak.thehideout.compose.theme.DividerDark
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
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamManagementActivity : GodActivity() {

    private val viewModel: TeamManagementViewModel by viewModels()

    @SuppressLint("CheckResult")
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
                        /*ExtendedFloatingActionButton(
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
                        )*/
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

    @SuppressLint("CheckResult")
    @Composable
    private fun TeamCard(teamID: String?) {
        var team by remember { mutableStateOf<Team?>(null) }

        questsFirebase.child("teams/$teamID").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    team = snapshot.getValue(Team::class.java)
                    team?.id = teamID
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
                    Modifier.padding(top = 16.dp, start = 0.dp, end = 0.dp, bottom = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        Text(text = it.name ?: "", style = MaterialTheme.typography.h6)
                        Spacer(modifier = Modifier.weight(1f))
                        if (it.members?.get(uid())?.owner == true) {
                            IconButton(onClick = {
                                MaterialDialog(this@TeamManagementActivity).show {
                                    title(text = "Change Team Name")
                                    input(
                                        prefill = "${it.name}",
                                        hint = "Team Name"
                                    ) { materialDialog, charSequence ->
                                        it.updateName(charSequence.toString())
                                    }
                                    positiveButton(text = "SAVE")
                                    negativeButton(text = "CANCEL")
                                }
                            }, Modifier.size(20.dp)) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_edit_24),
                                    contentDescription = "Edit Team"
                                )
                            }
                        }
                    }

                    Divider(color = DividerDark)

                    it.members?.forEach {
                        TeamMemberItem(id = it.key, it.value, teamID)
                    }

                    Divider(color = DividerDark)

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp, top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            modifier = Modifier.padding(end = 8.dp),
                            onClick = {
                                MaterialDialog(this@TeamManagementActivity).show {
                                    title(text = "Leave Team?")
                                    message(text = "You cannot undo this action, if you are the only member, the team will be deleted.")
                                    positiveButton(text = "LEAVE") {
                                        team?.leave()
                                    }
                                    negativeButton(text = "NEVERMIND")
                                }
                            }
                        ) {
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

    @SuppressLint("CheckResult")
    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun TeamMemberItem(id: String, value: Team.MemberSettings, teamID: String?) {
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
                Modifier
                    .height(48.dp)
                    .combinedClickable(
                        onClick = {
                            MaterialDialog(this).show {
                                title(text = "Your Color")
                                colorChooser(
                                    colors = ColorPalette.Primary,
                                    subColors = ColorPalette.PrimarySub,
                                    initialSelection = android.graphics.Color.parseColor(value.color)
                                ) { _, color ->
                                    value.updateColor("#${Integer.toHexString(color).substring(2)}", teamID!!, id)
                                }
                                positiveButton(text = "SAVE")
                            }
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Rectangle(color = value.getColorM(), modifier = Modifier.fillMaxHeight())
                Row(
                    Modifier
                        .padding(start = 16.dp, end = 16.dp)
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${it.getUsername()}${
                            if (it.uid == uid()) {
                                " (You)"
                            } else ""
                        }", fontWeight = if (it.uid == uid()) FontWeight.Bold else FontWeight.Normal
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (it.uid == uid()) {
                        Icon(
                            painterResource(id = R.drawable.icons8_crown_96),
                            contentDescription = "Owner",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
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

    @SuppressLint("CheckResult")
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
                    MaterialDialog(this@TeamManagementActivity).show {
                        title(text = "Create New Team")
                        input(
                            hint = "Team Name"
                        ) { materialDialog, charSequence ->
                            val key = questsFirebase.child("teams").push().key
                            key?.let {
                                uid()?.let {
                                    val newTeam = Team(
                                        name = charSequence.toString(),
                                        members = mapOf(
                                            uid()!! to Team.MemberSettings(
                                                color = "#F44336",
                                                owner = true
                                            )
                                        )
                                    )

                                    val childUpdates = hashMapOf(
                                        "users/${uid()!!}/teams/$key" to true,
                                        "teams/$key" to newTeam
                                    )

                                    questsFirebase.updateChildren(childUpdates)
                                }
                            }
                        }
                        positiveButton(text = "SAVE")
                        negativeButton(text = "CANCEL")
                    }
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

    @Composable
    fun Rectangle(
        color: Color,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .width(2.dp)
                .clip(RectangleShape)
                .background(color)
        )
    }
}