package com.austinhodak.thehideout.news

import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.news.models.NewsItem
import kotlinx.coroutines.launch

@Composable
fun NewsScreen(navViewModel: NavViewModel) {
    val scaffoldState = rememberScaffoldState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var data by remember { mutableStateOf<NewsItem?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            val apiService = NewsAPIService.getInstance()
            try {
                data = apiService.getTodos()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            MainToolbar(
                title = "News",
                navViewModel = navViewModel
            )
        }
    ) {
        LazyColumn (
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            items(items = data?.data ?: emptyList()) { item ->
                Article(item)
            }
        }
    }
}

@Composable
private fun Article(item: NewsItem.Data) {
    Card (
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        val imgUrl = item.content

        Column(
            Modifier.padding(16.dp)
        ) {
            HtmlText(html = item.content)
        }

    }
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = {
            it.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
            it.movementMethod = LinkMovementMethod.getInstance()
        }
    )
}