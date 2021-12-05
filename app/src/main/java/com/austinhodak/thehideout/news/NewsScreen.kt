package com.austinhodak.thehideout.news

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.prof.rssparser.Article
import com.prof.rssparser.Channel
import com.prof.rssparser.Parser
import kotlinx.coroutines.launch

@Composable
fun NewsScreen(navViewModel: NavViewModel) {
    val scaffoldState = rememberScaffoldState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val parser = Parser.Builder()
        .context(context)
        .cacheExpirationMillis(24L * 60L * 60L * 100L) // one day
        .build()

    var data by remember { mutableStateOf<Channel?>(null) }

    LaunchedEffect(key1 = "") {
        scope.launch {
            try {
                val channel = parser.getChannel("https://developertracker.com/escape-from-tarkov/rss")
                data = channel
            } catch (e: Exception) {
                e.printStackTrace()
                // Handle the exception
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
        LazyColumn {
            data?.let {
                items(items = it.articles) { article ->
                    Article(article)
                }
            }
        }
    }
}

@Composable
private fun Article(article: Article) {
    Card {
        Text(text = article.title ?: "")
    }
}