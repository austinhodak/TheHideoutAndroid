package com.austinhodak.thehideout.features.currency

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.DarkGrey
import com.austinhodak.thehideout.compose.theme.White
import kotlin.math.roundToLong

@Composable
fun CurrenyConverterScreen(
    navViewModel: NavViewModel,
    tarkovRepo: TarkovRepo
) {
    val context = LocalContext.current

    var dollar by rememberSaveable { mutableStateOf("") }
    var euro by rememberSaveable { mutableStateOf("") }
    var rouble by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Currency Converter", modifier = Modifier.padding(end = 16.dp))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        navViewModel.isDrawerOpen.value = true
                    }) {
                        Icon(Icons.Filled.Menu, contentDescription = null, tint = White)
                    }
                },
                backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                elevation = 0.dp,
                actions = {

                }
            )
        },
        backgroundColor = MaterialTheme.colors.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                value = dollar,
                onValueChange = {
                    dollar = it.filter { it.isDigit() }
                    rouble = dollarToRouble(it.toLongOrNull()).toString()
                    euro = dollarToEuro(it.toLongOrNull()).toString()
                },
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_attach_money_24), contentDescription = "")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                label = { Text("Dollars") },
                colors = TextFieldDefaults.textFieldColors(focusedLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium))
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                value = euro,
                onValueChange = {
                    euro = it.filter { it.isDigit() }
                    dollar = euroToDollar(it.toLongOrNull()).toString()
                    rouble = euroToRouble(it.toLongOrNull()).toString()
                },
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_baseline_euro_24), contentDescription = "")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                label = { Text("Euros") },
                colors = TextFieldDefaults.textFieldColors(focusedLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium))
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                value = rouble,
                onValueChange = {
                    rouble = it.filter { it.isDigit() }
                    dollar = roubleToDollar(it.toLongOrNull()).toString()
                    euro = roubleToEuro(it.toLongOrNull()).toString()
                },
                leadingIcon = {
                    Icon(painter = painterResource(id = R.drawable.ic_ruble_currency_sign), contentDescription = "")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                label = { Text("Roubles") },
                colors = TextFieldDefaults.textFieldColors(focusedLabelColor = MaterialTheme.colors.onSurface.copy(ContentAlpha.medium))
            )
            /*Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                text = "$1 = 121₽\n" +
                        "€1 = 141₽\n" +
                        "$1 : €1 = 0.858"
            )*/
        }
    }
}

fun roubleToDollar(r: Long?): Long {
    return r?.let { it / 121 } ?: 0
}

fun dollarToRouble(r: Long?): Long {
    return r?.let { it * 121 } ?: 0
}

fun roubleToEuro(r: Long?): Long {
    return r?.let { it / 141 } ?: 0
}

fun euroToRouble(r: Long?): Long {
    return r?.let { it * 141 } ?: 0
}

fun dollarToEuro(r: Long?): Long {
    return (r?.let { it * 0.858 })?.roundToLong() ?: 0
}

fun euroToDollar(r: Long?): Long {
    return (r?.let { it / 0.858 })?.roundToLong() ?: 0
}