package com.austinhodak.thehideout.flea_market.components

import android.annotation.SuppressLint
import android.text.InputType
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.type.ItemSourceName
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.getTraderLevel
import com.austinhodak.tarkovapi.utils.sourceTitle
import com.austinhodak.thehideout.compose.components.EmptyText
import com.austinhodak.thehideout.compose.components.LoadingItem
import com.austinhodak.thehideout.compose.components.Rectangle
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.flea_market.viewmodels.FleaViewModel
import com.austinhodak.thehideout.utils.openFleaDetail
import com.austinhodak.thehideout.utils.traderImage
import com.austinhodak.thehideout.utils.userRefTracker
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@SuppressLint("CheckResult")
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun ShoppingCartScreen(data: List<Item>?, userData: User?, fleaViewModel: FleaViewModel, paddingValues: PaddingValues) {
    val searchKey by fleaViewModel.searchKey.observeAsState("")

    val context = LocalContext.current

    val items = data?.filter { userData?.cart?.containsKey(it.id) == true }?.sortedByDescending {
        val quantity = userData?.cart?.get(it.id) ?: 1
        (it.pricing?.getPrice()?.times(quantity))
    }?.filter {
        it.ShortName?.contains(searchKey, ignoreCase = true) == true
                || it.Name?.contains(searchKey, ignoreCase = true) == true
                || it.itemType?.name?.contains(searchKey, ignoreCase = true) == true
    }

    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(visible = !items.isNullOrEmpty()) {
                ExtendedFloatingActionButton(
                    text = {
                        val total = items?.sumOf { item ->
                            val quantity = userData?.cart?.get(item.id) ?: 1
                            item.pricing?.getCheapestBuyRequirements()?.price?.times(quantity) ?: 0
                        }
                        Text(text = total?.asCurrency() ?: "Nothing in cart.", color = Color.Black)
                    },
                    onClick = {

                    },
                    modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
                )
            }
        },
    ) {
        if (items == null) {
            LoadingItem()
        } else if (items.isEmpty()) {
            EmptyText(text = "Nothing in cart.")
        } else {
            LazyColumn(contentPadding = PaddingValues(top = 4.dp, bottom = 4.dp)) {
                items(items = items) { item ->
                    val quantity = userData?.cart?.get(item.id) ?: 1
                    ShoppingFleaItem(item = item, quantity, onClick = {
                        context.openFleaDetail(it)
                    }, longClick = {
                        MaterialDialog(context).show {
                            listItems(items = listOf("Edit Quantity", "Remove from Cart")) { _, index, _ ->
                                when (index) {
                                    0 -> {
                                        MaterialDialog(context).show {
                                            title(text = "Item Quantity")
                                            input(prefill = quantity.toString(), hint = "Quantity", inputType = InputType.TYPE_CLASS_NUMBER) { _, text2 ->
                                                userRefTracker("cart/${item.id}").setValue(text2.toString().toInt())
                                            }
                                            positiveButton(text = "Save")
                                        }
                                    }
                                    1 -> {
                                        userRefTracker("cart/${item.id}").removeValue()
                                    }
                                }
                            }
                        }
                    })
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemPrice(
    item: Item,
    price: Pricing.BuySellPrice,
    quantity: Int
) {

}

@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
private fun ShoppingFleaItem(
    item: Item,
    quantity: Int = 1,
    onClick: (String) -> Unit,
    longClick: (String) -> Unit,
) {

    val color = when (item.BackgroundColor) {
        "blue" -> itemBlue
        "grey" -> itemGrey
        "red" -> itemRed
        "orange" -> itemOrange
        "default" -> itemDefault
        "violet" -> itemViolet
        "yellow" -> itemYellow
        "green" -> itemGreen
        "black" -> itemBlack
        else -> itemDefault
    }

    Card(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = { onClick(item.id) },
                onLongClick = { longClick(item.id) }
            ),
        backgroundColor = Color(0xFE1F1F1F)
    ) {
        Row(
            Modifier
                .height(IntrinsicSize.Max)
        ) {
            Rectangle(
                color = color, modifier = Modifier
                    .fillMaxHeight()
            )
            Column {
                Row (verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Image(
                        rememberImagePainter(item.pricing?.iconLink ?: "https://assets.tarkov-tools.com/5447a9cd4bdc2dbd208b4567-icon.jpg"),
                        contentDescription = null,
                        modifier = Modifier
                            .width(48.dp)
                            .height(48.dp)
                            .border((0.25).dp, color = BorderColor)
                    )
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "x$quantity ${item.pricing?.name}",
                            style = MaterialTheme.typography.h6,
                            fontSize = 15.sp
                        )
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = item.getUpdatedTime(),
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = (item.pricing?.getCheapestBuyRequirements()?.price?.times(quantity))?.asCurrency() ?: "",
                            style = MaterialTheme.typography.h6,
                            fontSize = 15.sp
                        )
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = "${item.pricing?.getCheapestBuyRequirements()?.price?.asCurrency()}/each",
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp
                            )
                        }
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = "${item.getPrice().asCurrency()} @ Flea",
                                style = MaterialTheme.typography.caption,
                                //color = if (item.pricing?.changeLast48h ?: 0.0 > 0.0) Green500 else if (item.pricing?.changeLast48h ?: 0.0 < 0.0) Red500 else Color.Unspecified,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Divider(color = DividerDark)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)) {
                    val buy = item.pricing?.getCheapestBuyRequirements()
                    Image(
                        rememberImagePainter(buy?.traderImage()),
                        contentDescription = null,
                        modifier = Modifier
                            .width(24.dp)
                            .height(24.dp)
                            .border((0.25).dp, color = BorderColor)
                    )
                    Text(
                        text = "${buy?.source?.sourceTitle()} ${buy?.requirements?.first()?.value?.getTraderLevel()}",
                        style = MaterialTheme.typography.body1,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = if (buy?.source == ItemSourceName.peacekeeper.rawValue) {
                            buy.price?.asCurrency("D") ?: ""
                        } else {
                            buy?.price?.asCurrency() ?: ""
                        },
                        style = MaterialTheme.typography.body1,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}