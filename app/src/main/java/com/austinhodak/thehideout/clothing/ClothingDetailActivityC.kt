package com.austinhodak.thehideout.clothing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.austinhodak.tarkovapi.ItemQuery
import com.austinhodak.thehideout.flea_market.viewmodels.FleaVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class ClothingDetailActivityC : ComponentActivity() {

    private val fleaVM: FleaVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val id = intent.getStringExtra("id") ?: ""

        setContent {

            fleaVM.getItemByID(id)
            /*val item = TarkovApi().getTarkovClient(this).query(ItemQuery(id)).toFlow().collectAsState(initial = null)

            TheHideoutTheme {
                ClothingScaffold(item.value?.data) {
                    onBackPressed()
                }
            }*/
        }
    }
}

@ExperimentalCoroutinesApi
@Composable
private fun ClothingScaffold(
    item: ItemQuery.Data?,
    onNavIconPressed: () -> Unit = { }
) {

    /*Scaffold(
        topBar = { ClothingTopBar(
            item?.item,
            onNavIconPressed = onNavIconPressed
        ) }
    ) {
        ClothingDetailCard(
            item = item?.item
        )
    }*/
}

@Composable
private fun ClothingTopBar(
    item: ItemQuery.Item?,
    modifier: Modifier = Modifier,
    onNavIconPressed: () -> Unit = { }
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Row {
                /*Text(
                    text = item?.fragments?.itemFragment?.shortName ?: "Backpack",
                    color = MaterialTheme.colors.onPrimary,
                    style = MaterialTheme.typography.h6
                )*/
            }
        },
        backgroundColor = MaterialTheme.colors.primary,
        elevation = 0.dp,
        navigationIcon = {
            IconButton(onClick = onNavIconPressed) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
            }
        }
    )
}
