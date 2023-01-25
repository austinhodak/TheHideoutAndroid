package com.austinhodak.thehideout.features.news

import android.content.Context
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.QuoteSpan
import android.widget.TextView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpannable
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ui.common.LoadingItem
import com.austinhodak.thehideout.compose.components.MainToolbar
import com.austinhodak.thehideout.compose.components.SearchToolbar
import com.austinhodak.thehideout.compose.theme.DividerDark
import com.austinhodak.thehideout.features.news.models.NewsItem
import com.austinhodak.thehideout.utils.ImageGetter
import com.austinhodak.thehideout.utils.openWithCustomTab
import com.austinhodak.thehideout.ui.legacy.CustomQuoteSpan
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

    val isSearchOpen by navViewModel.isSearchOpen.observeAsState(false)
    val searchKey by navViewModel.searchKey.observeAsState("")

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            if (isSearchOpen) {
                SearchToolbar(
                    onClosePressed = {
                        navViewModel.setSearchOpen(false)
                        navViewModel.clearSearch()
                    },
                    onValue = {
                        navViewModel.setSearchKey(it)
                    }
                )
            } else {
                MainToolbar(
                    title = "News",
                    navViewModel = navViewModel,
                    actions = {
                        IconButton(onClick = {
                            navViewModel.setSearchOpen(true)
                        }) {
                            Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.White)
                        }
                    }
                )
            }
        }
    ) {
        if (data == null) {
            LoadingItem()
        }
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            val list = data?.data?.filter {
                it.content.contains(searchKey, true) ||
                        it.account.service.contains(searchKey, true) ||
                        it.account.developer.group.contains(searchKey, true) ||
                        it.account.developer.nick.contains(searchKey, true)
            }?.sortedByDescending { it.timestamp }
            items(items = list ?: emptyList()) { item ->
                Article(item)
            }
        }
    }
}

@Composable
private fun Article(item: NewsItem.Data) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(),
        backgroundColor = Color(0xFE1F1F1F),
        onClick = {
            item.url.openWithCustomTab(context)
        }
    ) {
        Column(
            modifier = Modifier.padding(start = 0.dp, end = 0.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = item.account.getIcon()),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                )
                Column {
                    Row {
                        Text(
                            text = item.account.getTitle(),
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.subtitle2
                        )
                    }
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = item.getMessageTime(),
                            modifier = Modifier.padding(start = 16.dp),
                            style = MaterialTheme.typography.caption
                        )
                    }
                }
            }
            Divider(color = DividerDark, modifier = Modifier.padding(vertical = 16.dp))
            HtmlText(html = item.content, modifier = Modifier.padding(horizontal = 16.dp))
        }

    }
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context -> TextView(context) },
        update = {
            val benderFont = ResourcesCompat.getFont(it.context, R.font.bender)
            val imageGetter = ImageGetter(it.context.resources, it)
            val html = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY, imageGetter, null)
            it.text = replaceQuoteSpans(html.trim().toSpannable(), it.context)
            it.movementMethod = LinkMovementMethod.getInstance()

            it.typeface = benderFont
        }
    )
}

private fun replaceQuoteSpans(spannable: Spannable, context: Context): Spannable {
    val quoteSpans = spannable.getSpans(0, spannable.length, QuoteSpan::class.java)
    for (quoteSpan in quoteSpans) {
        val start = spannable.getSpanStart(quoteSpan)
        val end = spannable.getSpanEnd(quoteSpan)
        val flags = spannable.getSpanFlags(quoteSpan)
        spannable.removeSpan(quoteSpan)
        spannable.setSpan(
            CustomQuoteSpan(
                context.getColor(R.color.fui_transparent),
                context.getColor(R.color.md_grey_500),
                10f,
                60f
            ),
            start,
            end,
            flags
        )
    }
    return spannable
}