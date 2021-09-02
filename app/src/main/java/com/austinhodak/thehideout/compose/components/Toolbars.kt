package com.austinhodak.thehideout.compose.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.ammunition.viewmodels.AmmoViewModel

@Composable
fun MainToolbar(
    title: String,
    navViewModel: NavViewModel,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    actions: @Composable() (RowScope.() -> Unit) = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = {
                navViewModel.isDrawerOpen.value = true
            }) {
                Icon(Icons.Filled.Menu, contentDescription = null)
            }
        },
        actions = actions,
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
        elevation = elevation
    )
}

@Composable
fun AmmoDetailToolbar(
    title: String,
    ammoViewModel: AmmoViewModel,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    onBackPressed: () -> Unit,
    actions: @Composable() (RowScope.() -> Unit) = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = {
                onBackPressed()
            }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null)
            }
        },
        actions = actions,
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
        elevation = elevation
    )
}