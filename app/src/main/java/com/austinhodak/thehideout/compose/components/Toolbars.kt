package com.austinhodak.thehideout.compose.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R


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

@Composable
fun QuestDetailToolbar(
    title: String,
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    onBackPressed: () -> Unit,
    actions: @Composable() (RowScope.() -> Unit) = {}
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                /*Image(
                    painter = painterResource(id = traderIcon ?: R.drawable.prapor_portrait),
                    contentDescription = "",
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(40.dp)
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))*/
                Text(title)
            }
        },
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

@Composable
fun SearchToolbar(
    initialValue: String = "",
    hint: String = "Search",
    onClosePressed: () -> Unit,
    onValue: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }

    TopAppBar(
        title = {
            BasicTextField(
                cursorBrush = SolidColor(Color.White),
                value = text,
                onValueChange = {
                    text = it
                    onValue(it)
                },
                textStyle = TextStyle(color = Color.White, fontSize = 18.sp),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        if (text.isEmpty()) {
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Text(text = hint, fontSize = 18.sp)
                            }
                        }
                    }

                    innerTextField()
                },
                modifier = Modifier.focusRequester(focusRequester)
            )

            DisposableEffect(Unit) {
                focusRequester.requestFocus()
                onDispose { }
            }
        },
        navigationIcon = {
            IconButton(onClick = {
                onClosePressed()
            }) {
                Icon(Icons.Filled.Close, contentDescription = null)
            }
        },
        backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
        actions = {
            IconButton(onClick = { }) {
                Icon(painterResource(id = R.drawable.ic_baseline_manage_search_24), contentDescription = null, tint = Color.White)
            }
        }
    )
}

/*
@Preview
@Composable
fun SearchToolbarPreview() {
    SearchToolbar(onClosePressed = {}) {}
}*/
