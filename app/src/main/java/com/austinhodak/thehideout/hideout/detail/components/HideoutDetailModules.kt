import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.austinhodak.thehideout.compose.components.EmptyText
import com.austinhodak.thehideout.compose.components.SmallBuyPrice
import com.austinhodak.thehideout.compose.theme.Bender
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.compose.theme.DarkerGrey
import com.austinhodak.thehideout.crafts.CompactItem
import com.austinhodak.thehideout.crafts.CraftDetailActivity
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.hideout.HideoutRequirementModule
import com.austinhodak.thehideout.hideout.HideoutRequirementTrader
import com.austinhodak.thehideout.skillsList
import com.austinhodak.thehideout.utils.fadeImagePainter
import com.austinhodak.thehideout.utils.openActivity
import com.google.accompanist.pager.PagerState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber

@Composable
fun HideoutDetailModuleScreen(items: List<Item>, pagerStateCrafts: PagerState, modules: List<Hideout.Module?>?, station: Hideout.Station?) {
    val module = modules?.find { it?.level == pagerStateCrafts.currentPage + 1 }

    Timber.d(module.toString())

    val requirements = module?.require?.groupBy { it?.toString() }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
        modifier = Modifier.fillMaxSize()
    ) {

        requirements?.forEach { (entry, value) ->
            item {
                HideoutDetailModule(
                    entry,
                    value,
                    items,
                    module
                )
            }
        }
    }
    if (requirements.isNullOrEmpty()) {
        EmptyText(text = "No requirements for this module.")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HideoutDetailModule(type: String?, requirements: List<Hideout.Module.Require?>, items: List<Item>, module: Hideout.Module) {
    val totalModuleCost = requirements.sumOf { requirement ->
        val item = items.find { requirement?.name == it.id }
        (requirement?.quantity ?: 0).times(item?.pricing?.getCheapestBuyRequirements()?.getPriceAsRoubles() ?: 0)
    }
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
                    if (totalModuleCost <= 0) {
                        Text(
                            text = type.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    } else {
                        Text(
                            text = "${type.toString()} (${totalModuleCost.asCurrency()})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light,
                            fontFamily = Bender,
                        )
                    }
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
                        "item" -> {
                            items.find { it.id == requirement.name }?.pricing?.let { pricing ->
                                CompactItem(
                                    item = pricing, extras = CraftDetailActivity.ItemSubtitle(
                                        iconText = requirement.quantity?.toString(),
                                        showPriceInSubtitle = true,
                                        subtitle = " (${(requirement.quantity?.times(pricing.getCheapestBuyRequirements().price ?: 0))?.asCurrency()})"
                                    )
                                )
                            }
                            //HideoutRequirementItem(requirement = requirement, items.find { it.id == requirement.name })
                        }
                        "module" -> HideoutRequirementModule(module = module, requirement = requirement, userData = null)
                        "skill" -> HideoutRequirementSkill(module, requirement)
                    }
                }
            }
        }
    }
}

@Composable
fun HideoutRequirementSkill(module: Hideout.Module, requirement: Hideout.Module.Require) {
    val skill = skillsList.getSkill(requirement.name.toString())

    Row(
        modifier = Modifier
            .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            fadeImagePainter(skill?.icon),
            contentDescription = null,
            modifier = Modifier
                .width(38.dp)
                .height(38.dp)
                .border(
                    (0.25).dp,
                    color = BorderColor
                )
                .background(DarkerGrey)
        )
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${requirement.name} Level ${requirement.quantity}",
                style = MaterialTheme.typography.body1
            )
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
        Box {
            Image(
                rememberImagePainter(
                    pricing?.getCleanIcon()
                ),
                contentDescription = null,
                modifier = Modifier
                    .width(38.dp)
                    .height(38.dp)
                    .border((0.25).dp, color = BorderColor)
            )
            Text(
                text = "${requirement.quantity}",
                Modifier
                    .clip(RoundedCornerShape(topStart = 5.dp))
                    .background(BorderColor)
                    .padding(start = 3.dp, end = 2.dp, top = 1.dp, bottom = 1.dp)
                    .align(Alignment.BottomEnd),
                style = MaterialTheme.typography.caption,
                fontWeight = FontWeight.Medium,
                fontSize = 9.sp
            )
        }
        Column(
            modifier = Modifier.padding(start = 16.dp)
        ) {
            Text(
                text = "${pricing?.shortName}",
                style = MaterialTheme.typography.body1
            )

//            val cheapestBuy = pricing?.getCheapestBuyRequirements()?.copy()
//            if (cheapestBuy?.currency == "USD") {
//                cheapestBuy.price =  cheapestBuy.price?.fromDtoR()?.roundToInt()
//            } else if (cheapestBuy?.currency == "EUR") {
//                cheapestBuy.price = euroToRouble(cheapestBuy.price?.toLong()).toInt()
//            }

            CompositionLocalProvider(LocalContentAlpha provides 0.6f) {
                Row {
                    SmallBuyPrice(pricing = pricing)
                    Text(
                        text = " (${((requirement.quantity ?: 1).times(pricing?.getCheapestBuyRequirements()?.price ?: 0)).asCurrency()})",
                        style = MaterialTheme.typography.caption,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Light,
                    )
                }
            }
        }
    }
}