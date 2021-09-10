package com.austinhodak.thehideout.weapons.mods

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class ModPickerActivity : AppCompatActivity() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val modList = intent.getStringExtra("ids")?.split(";")
        val type = intent.getStringExtra("type")
        val parent = intent.getStringExtra("parent")
        val id = intent.getStringExtra("id")
        Timber.d(modList.toString())

        if (modList == null) finish()

        setContent {
            HideoutTheme {
                var list by remember { mutableStateOf(emptyList<Item>()) }

                val scaffoldState = rememberScaffoldState()
                val scope = rememberCoroutineScope()

                LaunchedEffect(key1 = "mod") {
                    scope.launch {
                        list = tarkovRepo.getItemByID(modList?.toList()!!)
                    }
                }

                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        Column {
                            AmmoDetailToolbar(
                                title = "${list.size} Mods",
                                onBackPressed = { finish() }
                            )
                            /*if (weapon == null) {
                                LinearProgressIndicator(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(2.dp),
                                    color = if (isSystemInDarkTheme()) Color.White else Color.Black,
                                    backgroundColor = Color.Transparent
                                )
                            }*/
                        }
                    }
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        items(items = list) { item ->
                            ModsBasicCard(item = item) {
                                setResult(RESULT_OK, Intent().apply {
                                    putExtra("id", id)
                                    putExtra("item", item)
                                    putExtra("type", type)
                                    putExtra("parent", parent)
                                })
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }
}