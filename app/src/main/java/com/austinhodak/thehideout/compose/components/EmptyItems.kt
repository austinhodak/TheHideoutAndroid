package com.austinhodak.thehideout.compose.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun EmptyText(
    text: String
) {
    Box(
        Modifier.fillMaxSize()
    ) {

        Text(text = text, modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.caption, textAlign = TextAlign.Center)
    }
}