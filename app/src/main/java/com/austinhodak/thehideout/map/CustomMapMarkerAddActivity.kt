package com.austinhodak.thehideout.map

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.compose.theme.White
import com.austinhodak.thehideout.currency.*
import com.austinhodak.thehideout.map.models.CustomMarker
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.austinhodak.thehideout.utils.userFirestore

import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment
import com.google.accompanist.flowlayout.SizeMode
import com.google.accompanist.imageloading.rememberDrawablePainter
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import timber.log.Timber
import java.lang.reflect.Field

class CustomMapMarkerAddActivity : AppCompatActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var customMarker = intent.getParcelableExtra<CustomMarker>("marker") as CustomMarker
        val map = intent.getStringExtra("map")

        setContent {
            HideoutTheme {
                var title: String? by rememberSaveable { mutableStateOf(customMarker.title) }
                var description: String? by rememberSaveable { mutableStateOf(customMarker.description) }
                var icon: String? by rememberSaveable { mutableStateOf(customMarker.icon) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = if (customMarker.id == null) {
                                            "Add Custom Marker"
                                        } else {
                                            "Edit Custom Marker"
                                        },
                                        color = MaterialTheme.colors.onPrimary,
                                        style = MaterialTheme.typography.h6,
                                        maxLines = 1,
                                        fontSize = 18.sp,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            actions = {
                                if (!customMarker.id.isNullOrEmpty()) {
                                    IconButton(onClick = {
                                        userFirestore()?.collection("markers")?.document(customMarker.id!!)?.delete()?.addOnSuccessListener {
                                            Toast.makeText(this@CustomMapMarkerAddActivity, "Marker deleted.", Toast.LENGTH_SHORT).show()
                                            finish()
                                        }?.addOnFailureListener {
                                            Toast.makeText(this@CustomMapMarkerAddActivity, "Error deleting marker. Please try again.", Toast.LENGTH_SHORT).show()
                                            Firebase.crashlytics.recordException(it)
                                        }
                                    }) {
                                        Icon(painter = painterResource(id = R.drawable.ic_baseline_delete_24), contentDescription = null, tint = White)
                                    }
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            if (title.isNullOrEmpty()) {
                                Toast.makeText(this, "Title cannot be empty.", Toast.LENGTH_SHORT).show()
                                return@FloatingActionButton
                            }

                            if (icon.isNullOrEmpty()) {
                                Toast.makeText(this, "Must select an icon.", Toast.LENGTH_SHORT).show()
                                return@FloatingActionButton
                            }

                            //Add marker if all fields are filled.
                            if (customMarker.id.isNullOrEmpty()) {
                                //New marker, not editing.
                                userFirestore()?.collection("markers")?.add(customMarker)?.addOnSuccessListener {
                                    Toast.makeText(this, "Marker added!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }?.addOnFailureListener {
                                    Toast.makeText(this, "Error occurred, please try again later.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                userFirestore()?.collection("markers")?.document(customMarker.id!!)?.set(customMarker, SetOptions.merge())?.addOnSuccessListener {
                                    Toast.makeText(this, "Marker updated!", Toast.LENGTH_SHORT).show()
                                    finish()
                                }?.addOnFailureListener {
                                    Toast.makeText(this, "Error occurred, please try again later.", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(painterResource(id = R.drawable.ic_baseline_save_24), contentDescription = null, tint = Color.Black)
                        }
                    }
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        item {
                            TextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                value = title ?: "",
                                onValueChange = {
                                    title = it
                                    customMarker.title = it
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Words),
                                singleLine = true,
                                label = { Text("Title") },
                                colors = TextFieldDefaults.textFieldColors(focusedLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium)),
                            )
                        }
                        item {
                            TextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                value = description ?: "",
                                onValueChange = {
                                    description = it
                                    customMarker.description = it
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, capitalization = KeyboardCapitalization.Sentences),
                                singleLine = false,
                                label = { Text("Description") },
                                colors = TextFieldDefaults.textFieldColors(focusedLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium))
                            )
                        }
                        item {
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .fillMaxWidth(),
                                backgroundColor = Color(0xFE1F1F1F)
                            ) {
                                Column(
                                    Modifier.padding(bottom = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(bottom = 16.dp, top = 16.dp, start = 16.dp, end = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                            Text(
                                                text = "SELECT ICON",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Light,
                                                fontFamily = Bender,
                                            )
                                        }
                                        Spacer(Modifier.weight(1f))
                                        IconButton(onClick = {
                                            "https://discord.gg/YQW36z29z6".openWithCustomTab(this@CustomMapMarkerAddActivity)
                                        }, modifier = Modifier.size(20.dp)) {
                                            Icon(painter = painterResource(id = R.drawable.ic_baseline_feedback_24), contentDescription = null)
                                        }
                                    }

                                    FlowRow(
                                        mainAxisSize = SizeMode.Expand,
                                        mainAxisAlignment = MainAxisAlignment.SpaceEvenly
                                    ) {
                                        val drawablesFields: Array<Field> = com.austinhodak.tarkovapi.R.drawable::class.java.fields

                                        drawablesFields.filter { it.name.contains("icon_") && !it.name.contains("notification", true) && !it.name.contains("_checkmark_") }
                                            .filterNot {
                                                it.name.contains("icon_back_") || it.name.contains("icon_custom_black") || it.name.contains("icon_custom_white")
                                                        || it.name.contains("icon_settings")
                                            }.forEach {
                                                Image(
                                                    painter = rememberDrawablePainter(drawable = ResourcesCompat.getDrawable(resources, it.getInt(null), null)),
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .alpha(if (icon == null) 1f else if (icon == it.name) 1f else 0.2f)
                                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                                        .clickable {
                                                            icon = it.name
                                                            customMarker.icon = it.name
                                                        }
                                                )
                                            }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}