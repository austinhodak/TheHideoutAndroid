import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.models.Hideout
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.tarkovapi.utils.fromDtoR
import com.austinhodak.thehideout.NavViewModel
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.currency.euroToRouble
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.hideout.HideoutRequirementModule
import com.austinhodak.thehideout.hideout.HideoutRequirementTrader
import com.austinhodak.thehideout.utils.openActivity
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import kotlin.math.roundToInt

@Composable
fun HideoutDetailModuleScreen(navViewModel: NavViewModel, pagerStateCrafts: PagerState, modules: List<Hideout.Module?>?, station: Hideout.Station?) {
    val module = modules?.find { it?.level == pagerStateCrafts.currentPage + 1 }

    Timber.d(module.toString())

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        module?.require?.groupBy { it?.toString() }?.forEach { (entry, value) ->
            item {
                HideoutDetailModule(
                    entry,
                    value,
                    navViewModel,
                    module
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HideoutDetailModule(type: String?, requirements: List<Hideout.Module.Require?>, navViewModel: NavViewModel, module: Hideout.Module) {
    Card(
        backgroundColor = if (isSystemInDarkTheme()) Color(
            0xFE1F1F1F
        ) else MaterialTheme.colors.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(
                    bottom = 8.dp,
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    Text(
                        text = type.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light,
                        fontFamily = Bender,
                    )
                }
            }

            Column(
                modifier = Modifier.padding(
                    start = 0.dp,
                    end = 0.dp,
                    top = 4.dp,
                    bottom = 12.dp
                )
            ) {
                requirements.forEach { requirement ->
                    when (requirement?.type) {
                        "trader" -> HideoutRequirementTrader(requirement = requirement, userData = null)
                        "item" -> HideoutRequirementItem(requirement = requirement, navViewModel.allItems.value?.find { it.id == requirement.name })
                        "module" -> HideoutRequirementModule(module = module, requirement = requirement, userData = null)
                    }
                }
            }
        }
    }
}

@ExperimentalCoilApi
@ExperimentalCoroutinesApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun HideoutRequirementItem(
    requirement: Hideout.Module.Require,
    item: Item?
) {
    val context = LocalContext.current

    val pricing = item?.pricing

    Row(
        modifier = Modifier
            .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth()
            .clickable {
                context.openActivity(FleaItemDetail::class.java) {
                    putString("id", item?.id)
                }
            }
            .combinedClickable(onClick = {

            }, onLongClick = {
                /*if (requirement.quantity ?: 0 > 500) return@combinedClickable
                MaterialDialog(context).show {
                    title(text = "Add to Needed Items?")
                    message(text = "This will add these items to the needed items list on the Flea Market screen.")
                    positiveButton(text = "ADD") {
                        userRefTracker("items/${item?.id}/hideoutObjective/${it?.id?.addQuotes()}").setValue(quantity)
                    }
                    negativeButton(text = "CANCEL")
                }*/
            }),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            rememberImagePainter(
                pricing?.getCleanIcon()
            ),
            contentDescription = null,
            modifier = Modifier
                .width(38.dp)
                .height(38.dp)
                .border(
                    (0.25).dp,
                    color = BorderColor
                )
        )
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${pricing?.shortName}",
                style = MaterialTheme.typography.body1
            )

            val cheapestBuy = pricing?.getCheapestBuyRequirements()?.copy()
            if (cheapestBuy?.currency == "USD") {
                cheapestBuy.price =  cheapestBuy.price?.fromDtoR()?.roundToInt()
            } else if (cheapestBuy?.currency == "EUR") {
                cheapestBuy.price = euroToRouble(cheapestBuy.price?.toLong()).toInt()
            }

            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                Row {
                    SmallBuyPrice(pricing = pricing)
                    Text(
                        text = " (${((requirement.quantity ?: 1).times(cheapestBuy?.price ?: 0)).asCurrency()})",
                        style = MaterialTheme.typography.caption,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                    )
                }
            }
        }
    }
}