package com.austinhodak.thehideout.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatSpinner
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.imageLoader
import coil.request.ImageRequest
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.afollestad.materialdialogs.input.getInputField
import com.afollestad.materialdialogs.input.input
import com.afollestad.materialdialogs.list.listItems
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.BuildConfig
import com.austinhodak.thehideout.NavActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.BorderColor
import com.austinhodak.thehideout.compose.theme.Green500
import com.austinhodak.thehideout.compose.theme.Red500
import com.austinhodak.thehideout.features.ammunition.AmmoDetailActivity
import com.austinhodak.thehideout.features.calculator.models.CAmmo
import com.austinhodak.thehideout.features.calculator.models.CArmor
import com.austinhodak.thehideout.features.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.features.premium.PremiumPusherActivity
import com.austinhodak.thehideout.features.quests.QuestDetailActivity
import com.austinhodak.thehideout.features.weapons.detail.WeaponDetailActivity
import com.austinhodak.thehideout.features.weapons.mods.ModDetailActivity
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.material.textfield.TextInputEditText
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.internal.toLongOrDefault
import timber.log.Timber
import java.text.DecimalFormat
import java.util.concurrent.ExecutionException
import kotlin.math.round

fun userFirestore() = uid()?.let {
    Timber.d(it)
    Firebase.firestore.collection("users").document(it)
}

@SuppressLint("CheckResult")
fun Context.showDialog(vararg item: Pair<String, () -> Unit>) {
    MaterialDialog(this).show {
        listItems(items = item.map { it.first }) { dialog, index, text ->
            val i = item.find { it.first == text }?.second!!
            i()
        }
    }
}

fun isDebug(): Boolean = BuildConfig.DEBUG

fun Int.roubleToDollar(): Int {
    return this / 121
}

fun Number.asColor(reverse: Boolean = false): Color {
    return when {
        this.toDouble() > 0.0 -> if (!reverse) Green500 else Red500
        this.toDouble() < 0.0 -> if (!reverse) Red500 else Green500
        else -> Color.Unspecified
    }
}

fun Boolean?.asBlocks(): String {
    return if (this == true) "YES" else "NO"
}

fun Item.toSimArmor(customDurability: Double? = null): CArmor {
    return CArmor(
        `class` = armorClass?.toInt() ?: 1,
        bluntThroughput = BluntThroughput!!,
        durability = customDurability ?: Durability?.toDouble()!!,
        maxDurability = MaxDurability?.toDouble()!!,
        resistance = (armorClass?.toInt()?.times(10))?.toDouble()!!,
        destructibility = destructibility(),
        zones = armorZone!!
    )
}

fun Ammo.toSimAmmo(): CAmmo {
    val ballistics = this.ballistics
    return CAmmo(
        ballistics?.projectileCount ?: 1,
        ballistics?.damage?.toDouble() ?: 0.0,
        ballistics?.penetrationPower ?: 0.0,
        (ballistics?.armorDamage ?: 1.0).toDouble() * (ballistics?.projectileCount ?: 1) / 100
    )
}

fun String.getMedIcon(): Int? {
    return when (this) {
        "LightBleeding" -> R.drawable.light_bleeding_icon
        "HeavyBleeding" -> R.drawable.heavy_bleeding_icon
        "FreshWounds" -> R.drawable.fresh_wound_icon
        "Fracture" -> R.drawable.fracture_icon
        "Pain" -> R.drawable.pain_icon
        "Contusion" -> R.drawable.contusion_icon
        "Tremor" -> R.drawable.tremor_icon
        else -> null
    }
}

fun Double.getColor(reverse: Boolean = false, surfaceColor: Color): Color {
    return if (this == 0.0 || this.isNaN()) {
        surfaceColor
    } else if (this > 0.0) {
        if (reverse) Red500 else Green500
    } else {
        if (reverse) Green500 else Red500
    }
}

fun Pricing.BuySellPrice.traderImage(showLevel: Boolean? = true): String {
    //Flea Market Icon
    if (this.source == "fleaMarket") return "https://tarkov.dev/images/traders/flea-market-portrait.png"

    when {
        requirements.isNullOrEmpty() || showLevel == false -> {
            return when (this.source) {
                "prapor" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/6/6b/Prapor_Portrait.png/revision/latest?cb=20180425012550"
                "therapist" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/c/c7/Therapist_Portrait.png/revision/latest?cb=20221124232039"
                "fence" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f7/Fence_Portrait.png/revision/latest?cb=20230120170036"
                "skier" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/0/0a/Skier_Portrait.png/revision/latest?cb=20180425012638"
                "peacekeeper" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/0/08/Peacekeeper_Portrait.png/revision/latest?cb=20180108000742"
                "mechanic" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/ef/Mechanic_Portrait.png/revision/latest?cb=20180425012513"
                "ragman" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/8/8b/Ragman_Portrait.png/revision/latest?cb=20180425012401"
                "jaeger" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/d/d1/Jaeger_Portrait.png/revision/latest?cb=20191102063456"
                "lightkeeper" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/b/b1/Lightkeeper_Portrait.png/revision/latest?cb=20230120170122"
                else -> "https://tarkov.dev/images/traders/prapor-icon.jpg"
            }
        }
        requirements.first().type == "loyaltyLevel" || isQuestLocked() -> {
            val level = requirements.find { it.type == "loyaltyLevel" }?.value ?: 1
            Timber.d("Loyalty level: $level")
            return when (this.source) {
                "prapor" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/fc/Prapor_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110125"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/7/75/Prapor_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110134"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/6/64/Prapor_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110141"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f1/Prapor_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110153"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/fc/Prapor_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110125"
                }
                "therapist" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/fb/Therapist_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110312"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/5/5f/Therapist_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110321"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f6/Therapist_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110328"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Therapist_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110338"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Therapist_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110338"
                }
                "fence" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f7/Fence_Portrait.png/revision/latest/scale-to-width-down/127?cb=20180425012754"
                "ragman" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/e5/Ragman_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110204"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/2/20/Ragman_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110215"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/ed/Ragman_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110221"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/1c/Ragman_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110230"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/e5/Ragman_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110204"
                }
                "peacekeeper" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Peacekeeper_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110041"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/96/Peacekeeper_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110052"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/95/Peacekeeper_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110059"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/3/3e/Peacekeeper_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110108"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Peacekeeper_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110041"
                }
                "skier" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/eb/Skier_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110238"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/12/Skier_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110248"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/6/65/Skier_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110257"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/e8/Skier_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110304"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/eb/Skier_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110238"
                }
                "mechanic" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/3/3f/Mechanic_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822105848"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/9b/Mechanic_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822105910"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/b/b8/Mechanic_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110019"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/b/bd/Mechanic_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110029"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/3/3f/Mechanic_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822105848"
                }
                "jaeger" -> when (level) {
                    1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/d/d4/Jaeger_1_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221027"
                    2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/18/Jaeger_2_icon.png/revision/latest/scale-to-width-down/130?cb=20191101214208"
                    3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/ed/Jaeger_3_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221028"
                    4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f2/Jaeger_4_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221026"
                    else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/d/d4/Jaeger_1_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221027"
                }
                else -> "https://tarkov.dev/images/prapor-icon.jpg"
            }
        }
        else -> {
            return "https://tarkov.dev/images/flea-market-icon.jpg"
        }
    }
}


@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
fun Activity.restartNavActivity() {
    val intent = Intent(this, NavActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK and Intent.FLAG_ACTIVITY_CLEAR_TASK)
    startActivity(intent)
    finishAffinity()
}

fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
}

fun <T> Context.openWeaponPicker(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    intent.action = "loadoutBuild"
    startActivity(intent)
}

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
fun Context.openQuestDetail(id: String) {
    this.openActivity(QuestDetailActivity::class.java) {
        putString("questID", id)
    }
}

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
fun Context.openFleaDetail(id: String) {
    this.openActivity(FleaItemDetail::class.java) {
        putString("id", id)
    }
}

@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
fun Context.openWeaponDetail(id: String) {
    this.openActivity(WeaponDetailActivity::class.java) {
        putString("weaponID", id)
    }
}

@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
fun Context.openAmmunitionDetail(id: String) {
    this.openActivity(AmmoDetailActivity::class.java) {
        putString("ammoID", id)
    }
}

@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
fun Context.openModDetail(id: String) {
    this.openActivity(ModDetailActivity::class.java) {
        putString("id", id)
    }
}

@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
fun Context.launchPremiumPusher() {
    this.openActivity(PremiumPusherActivity::class.java)
}

@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
fun Context.launchPremiumPusherResult() {
    this.openActivity(PremiumPusherActivity::class.java) {
        putString("setResult", "true")
    }
}

@ExperimentalPagerApi
@ExperimentalAnimationApi
@ExperimentalCoroutinesApi
@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalMaterialApi
fun Context.launchPremium() {
    this.openActivity(PremiumPusherActivity::class.java)
}

fun AmmoCalibers(): List<String> = arrayListOf(
    "Caliber762x35",
    "Caliber86x70",
    "Caliber366TKM",
    "Caliber1143x23ACP",
    "Caliber9x33R",
    "Caliber127x55",
    "Caliber12g",
    "Caliber20g",
    "Caliber23x75",
    "Caliber46x30",
    "Caliber40x46",
    "Caliber545x39",
    "Caliber556x45NATO",
    "Caliber57x28",
    "Caliber762x25TT",
    "Caliber762x39",
    "Caliber762x51",
    "Caliber762x54R",
    "Caliber9x18PM",
    "Caliber9x19PARA",
    "Caliber9x21",
    "Caliber9x39"
)

fun getCaliberName(caliber: String?): String {
    return when (caliber) {
        "Caliber762x35" -> ".300 Blackout"
        "Caliber86x70" -> ".338 Lapua"
        "Caliber366TKM" -> ".366 TKM"
        "Caliber1143x23ACP" -> ".45 ACP"
        "Caliber9x33R" -> ".357 Magnum"
        "Caliber127x55" -> "12.7x55mm"
        "Caliber12g" -> "12 Gauge"
        "Caliber20g" -> "20 Gauge"
        "Caliber23x75" -> "23x75mm"
        "Caliber46x30" -> "4.6x30mm"
        "Caliber40x46" -> "40x46mm"
        "Caliber545x39" -> "5.45x39mm"
        "Caliber556x45NATO" -> "5.56x45mm NATO"
        "Caliber57x28" -> "5.7x28mm FN"
        "Caliber762x25TT" -> "7.62x25mm Tokarev"
        "Caliber762x39" -> "7.62x39mm"
        "Caliber762x51" -> "7.62x51mm NATO"
        "Caliber762x54R" -> "7.62x54mmR"
        "Caliber9x18PM" -> "9x18mm Makarov"
        "Caliber9x19PARA" -> "9x19mm Parabellum"
        "Caliber9x21" -> "9x21mm Gyurza"
        "Caliber9x39" -> "9x39mm"
        else -> "Unknown Ammo Type"
    }
}

fun String.openWithCustomTab(context: Context) {
    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(this))
}

fun getCaliberShortName(caliber: String?): String {
    return when (caliber) {
        "Caliber762x35" -> ".300"
        "Caliber86x70" -> ".338"
        "Caliber366TKM" -> ".366 TKM"
        "Caliber1143x23ACP" -> ".45 ACP"
        "Caliber9x33R" -> ".357"
        "Caliber127x55" -> "12.7x55mm"
        "Caliber12g" -> "12G"
        "Caliber20g" -> "20G"
        "Caliber23x75" -> "23x75mm"
        "Caliber46x30" -> "4.6x30mm"
        "Caliber40x46" -> "40x46mm"
        "Caliber545x39" -> "5.45x39mm"
        "Caliber556x45NATO" -> "5.56x45mm"
        "Caliber57x28" -> "5.7x28mm"
        "Caliber762x25TT" -> "7.62x25mm"
        "Caliber762x39" -> "7.62x39mm"
        "Caliber762x51" -> "7.62x51mm"
        "Caliber762x54R" -> "7.62x54mmR"
        "Caliber9x18PM" -> "9x18mm"
        "Caliber9x19PARA" -> "9x19mm"
        "Caliber9x21" -> "9x21mm"
        "Caliber9x39" -> "9x39mm"
        else -> ""
    }
}

fun Int.addQuotes(): String {
    return "\"$this\""
}

fun Any.addQuotes(): String {
    return "\"$this\""
}

fun String.removeQuotes(): String {
    return replace("\"", "")
}

@DrawableRes
fun String.getObjectiveIcon(): Int {
    return when (this.lowercase()) {
        "kill" -> R.drawable.icons8_sniper_96
        "collect" -> R.drawable.ic_baseline_swap_horizontal_circle_24
        "pickup" -> R.drawable.icons8_upward_arrow_96
        "key" -> R.drawable.icons8_key_100
        "place" -> R.drawable.icons8_low_importance_96
        "mark" -> R.drawable.icons8_low_importance_96
        "locate" -> R.drawable.ic_baseline_location_searching_24
        "find" -> R.drawable.ic_baseline_check_circle_outline_24
        "reputation" -> R.drawable.ic_baseline_thumb_up_24
        "warning" -> R.drawable.ic_baseline_warning_24
        "skill" -> R.drawable.ic_baseline_fitness_center_24
        else -> R.drawable.icons8_sniper_96
    }
}

fun String.modParent(): String {
    return when (this) {
        "bipod" -> "55818afb4bdc2dde698b456d"
        "foregrips" -> "55818af64bdc2d5b648b4570"
        "flashlights" -> "55818b084bdc2d5b648b4571"
        "tac" -> "55818b164bdc2ddc698b456c"
        "aux" -> "550aa4dd4bdc2dc9348b4569"
        "muzzle_flash",
        "muzzle_brakes",
        "muzzle_adapters" -> "550aa4bf4bdc2dd6348b456b"
        "muzzle_suppressor" -> "550aa4cd4bdc2dd8348b456c"
        "gear_handles" -> "55818a6f4bdc2db9688b456b"
        "gear_mags" -> "5448bc234bdc2d3c308b4569"
        "gear_mounts" -> "55818b224bdc2dde698b456f"
        "gear_stocks" -> "55818a594bdc2db9688b456a"
        "vital_barrels" -> "555ef6e44bdc2de9068b457e"
        "vital_gas" -> "56ea9461d2720b67698b456f"
        "vital_handguards" -> "55818a104bdc2db9688b4569"
        "vital_grips" -> "55818a684bdc2ddd698b456d"
        "vital_receivers" -> "55818a304bdc2db5418b457d"
        "sights_assault" -> "55818add4bdc2d5b648b456f"
        "sights_reflex" -> "55818ad54bdc2ddc698b4569"
        "sights_compact" -> "55818acf4bdc2dde698b456b"
        "sights_iron" -> "55818ac54bdc2d5b648b456e"
        "sights_scopes" -> "55818ae44bdc2dde698b456c"
        "sights_special" -> "55818aeb4bdc2ddc698b456a"
        else -> ""
    }
}

fun Activity.keepScreenOn(keepOn: Boolean) {
    if (keepOn) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

@SuppressLint("CheckResult")
fun Pricing.addToCartDialog(context: Context) {
    val pricing = this
    MaterialDialog(context).show {
        var total: Long = pricing.getPrice().toLong()
        title(text = "Add to Cart")
        message(text = "Total: ${total.toInt().asCurrency()}")
        input(inputType = InputType.TYPE_CLASS_NUMBER, maxLength = 6, prefill = "1", hint = "Quantity", waitForPositiveButton = false) { dialog, text ->
            val quantity = text.toString().toLongOrNull()
            total = pricing.getPrice().times(quantity ?: 1) ?: 0
            dialog.message(text = "Total: ${total.toInt().asCurrency()}")
        }
        positiveButton(text = "ADD TO CART") {
            val text = it.getInputField().text
            try {
                val quantity = text.toString().toLong()
                pricing.addToCart(quantity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@SuppressLint("CheckResult")
fun Pricing.addToNeededItemsDialog(context: Context) {
    val pricing = this
    MaterialDialog(context).show {
        var total: Long = pricing.getPrice().toLong()
        title(text = "Add to Needed Items")
        message(text = "Total: ${total.toInt().asCurrency()}")
        input(inputType = InputType.TYPE_CLASS_NUMBER, maxLength = 6, prefill = "1", hint = "Quantity", waitForPositiveButton = false) { dialog, text ->
            val quantity = text.toString().toLongOrNull()
            total = pricing.getPrice().times(quantity ?: 1) ?: 0
            dialog.message(text = "Total: ${total.toInt().asCurrency()}")
        }
        positiveButton(text = "ADD TO NEEDED ITEMS") {
            val text = it.getInputField().text
            try {
                val quantity = text.toString().toLong()
                pricing.addToNeededItems(quantity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

@ExperimentalFoundationApi
@SuppressLint("CheckResult")
fun Pricing.addPriceAlertDialog(context: Context, done: (() -> Unit?)? = null) {
    questsFirebase.child("priceAlerts").orderByChild("uid").equalTo(uid()).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists() && snapshot.childrenCount >= 5) {
                return isPremium {
                    if (!it) {
                        context.launchPremiumPusher()
                        return@isPremium
                    }
                }
            }

            val pricing = this@addPriceAlertDialog
            val alertDialog = MaterialDialog(context).show {
                title(text = context.getString(R.string.price_alert_add))
                customView(R.layout.dialog_add_price_alert)
                positiveButton(text = context.getString(R.string.add)) { dialog ->
                    val alertAddView = dialog.getCustomView()
                    val spinner = alertAddView.findViewById<AppCompatSpinner>(R.id.addAlertSpinner)
                    val editText = alertAddView.findViewById<TextInputEditText>(R.id.addAlertTextField)

                    if (editText.text.toString().isEmpty()) {
                        editText.error = context.getString(R.string.error_empty)
                    } else {
                        editText.error = null
                        val price = editText?.text.toString().replace(",", "").toLongOrDefault(0)
                        val selected = when (spinner?.selectedItemPosition) {
                            0 -> "below"
                            else -> "above"
                        }
                        pricing.addPriceAlert(price, selected, true, dialog, context)
                        if (done != null) done()
                    }
                }
                negativeButton(text = context.getString(R.string.cancel)) {
                    dismiss()
                }
                noAutoDismiss()
            }
            val alertAddView = alertDialog.getCustomView()
            val editText = alertAddView.findViewById<TextInputEditText>(R.id.addAlertTextField)
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable?) {
                    editText.removeTextChangedListener(this)

                    try {
                        var givenstring = s.toString()
                        val longval: Long
                        if (givenstring.contains(",")) {
                            givenstring = givenstring.replace(",".toRegex(), "")
                        }
                        longval = givenstring.toLong()
                        val formatter = DecimalFormat("#,###,###")
                        val formattedString: String = formatter.format(longval)
                        editText.setText(formattedString)
                        editText.setSelection(editText.text?.length ?: 0)
                        // to place the cursor at the end of text
                    } catch (nfe: NumberFormatException) {
                        nfe.printStackTrace()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    editText.addTextChangedListener(this)
                }
            })
        }

        override fun onCancelled(error: DatabaseError) {

        }
    })

}

fun Pricing.addPriceAlert(price: Long, condition: String, persistent: Boolean, dialog: MaterialDialog, context: Context) {
    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
        val alertID = questsFirebase.child("priceAlerts").push().key
        alertID?.let {
            questsFirebase.child("priceAlerts").child(alertID).setValue(
                mapOf(
                    "condition" to condition,
                    "enabled" to true,
                    "itemID" to this.id,
                    "persistent" to persistent,
                    "price" to price,
                    "token" to token,
                    "uid" to uid(),
                    "created" to System.currentTimeMillis()
                )
            ).addOnSuccessListener {
                Toast.makeText(context, "Price Alert Added!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }
}

fun Pricing.addToCart(quantity: Long? = 1) {
    userRefTracker("cart/${this.id}").setValue(ServerValue.increment(quantity ?: 1))
}

fun Pricing.addToNeededItems(quantity: Long? = 1) {
    val token = FirebaseDatabase.getInstance().reference.push()
    userRefTracker("items/${this.id}/user/${token.key}/quantity").setValue(quantity)
}

fun isPremium(isPremium: (Boolean) -> Unit) {
    isPremium.invoke(UserSettingsModel.isPremiumUser.value)
}

fun isPremium(): Boolean {
    return UserSettingsModel.isPremiumUser.value
}

fun Uri.acceptTeamInvite(joined: () -> Unit) {
    val teamID = this.lastPathSegment
    userFirestore()?.update("teams.$teamID", true)?.addOnSuccessListener {
        questsFirebase.child("teams/$teamID/members/${uid()}/color").setValue("#F44336").addOnSuccessListener {
            joined()
        }
    }
}

fun isWorkScheduled(context: Context, tag: String): Boolean {
    val instance = WorkManager.getInstance(context)
    val statuses: ListenableFuture<List<WorkInfo>> = instance.getWorkInfosForUniqueWork(tag)
    return try {
        var running = false
        val workInfoList: List<WorkInfo> = statuses.get()
        for (workInfo in workInfoList) {
            val state = workInfo.state
            running = state == WorkInfo.State.RUNNING || state == WorkInfo.State.ENQUEUED
        }
        running
    } catch (e: ExecutionException) {
        e.printStackTrace()
        false
    } catch (e: InterruptedException) {
        e.printStackTrace()
        false
    }
}

fun isWorkRunning(context: Context, tag: String): Boolean {
    val instance = WorkManager.getInstance(context)
    val statuses: ListenableFuture<List<WorkInfo>> = instance.getWorkInfosForUniqueWork(tag)
    return try {
        var running = false
        val workInfoList: List<WorkInfo> = statuses.get()
        for (workInfo in workInfoList) {
            val state = workInfo.state
            running = state == WorkInfo.State.RUNNING
        }
        running
    } catch (e: ExecutionException) {
        e.printStackTrace()
        false
    } catch (e: InterruptedException) {
        e.printStackTrace()
        false
    }
}

fun openStatusSite(context: Context) {
    "https://status.escapefromtarkov.com/".openWithCustomTab(context)
}

fun ttSyncEnabledPremium(isEnabled: (Boolean) -> Unit) {
    if (isPremium() || isDebug()) {
        isEnabled(UserSettingsModel.ttAPIKey.value.isNotEmpty() && UserSettingsModel.ttSync.value)
    } else {
        isEnabled(false)
    }
}

fun ttSyncEnabledPremium(): Boolean {
    return if (isPremium() || isDebug()) {
        UserSettingsModel.ttAPIKey.value.isNotEmpty() && UserSettingsModel.ttSync.value
    } else {
        false
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun Context.openNotificationSettings(channelID: String) {
    val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
    intent.putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
    intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelID)
    startActivity(intent)
}

@Composable
fun fadeImagePainter(data: Any?) =
    rememberAsyncImagePainter(
        model = ImageRequest.Builder(
            LocalContext.current
        ).data(data).traderLevel(1).build(), LocalContext.current
            .imageLoader
    )

@Composable
fun fadeImagePainterPlaceholder(
    url: Any?
) = rememberAsyncImagePainter(model = url, LocalContext.current.imageLoader.unknown())

fun Modifier.border(): Modifier = this.border(0.25.dp, BorderColor)

fun String.traderIconSource(name: String? = this.split(" ")[0].lowercase(), level: Int? = this.split(" ")[1].replace("LL", "").toIntOrNull()): String {
    return when (name) {
        "prapor" -> when (level) {
            1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/fc/Prapor_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110125"
            2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/7/75/Prapor_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110134"
            3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/6/64/Prapor_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110141"
            4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f1/Prapor_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110153"
            else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/fc/Prapor_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110125"
        }
        "therapist" -> when (level) {
            1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/fb/Therapist_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110312"
            2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/5/5f/Therapist_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110321"
            3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f6/Therapist_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110328"
            4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Therapist_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110338"
            else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Therapist_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110338"
        }
        "fence" -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f7/Fence_Portrait.png/revision/latest/scale-to-width-down/127?cb=20180425012754"
        "ragman" -> when (level) {
            1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/e5/Ragman_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110204"
            2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/2/20/Ragman_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110215"
            3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/ed/Ragman_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110221"
            4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/1c/Ragman_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110230"
            else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/e5/Ragman_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110204"
        }
        "peacekeeper" -> when (level) {
            1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Peacekeeper_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110041"
            2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/96/Peacekeeper_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110052"
            3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/95/Peacekeeper_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110059"
            4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/3/3e/Peacekeeper_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110108"
            else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/a/af/Peacekeeper_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110041"
        }
        "skier" -> when (level) {
            1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/eb/Skier_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110238"
            2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/12/Skier_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110248"
            3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/6/65/Skier_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110257"
            4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/e8/Skier_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110304"
            else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/eb/Skier_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110238"
        }
        "mechanic" -> when (level) {
            1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/3/3f/Mechanic_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822105848"
            2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/9/9b/Mechanic_2_icon.png/revision/latest/scale-to-width-down/130?cb=20180822105910"
            3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/b/b8/Mechanic_3_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110019"
            4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/b/bd/Mechanic_4_icon.png/revision/latest/scale-to-width-down/130?cb=20180822110029"
            else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/3/3f/Mechanic_1_icon.png/revision/latest/scale-to-width-down/130?cb=20180822105848"
        }
        "jaeger" -> when (level) {
            1 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/d/d4/Jaeger_1_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221027"
            2 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/1/18/Jaeger_2_icon.png/revision/latest/scale-to-width-down/130?cb=20191101214208"
            3 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/e/ed/Jaeger_3_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221028"
            4 -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/f/f2/Jaeger_4_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221026"
            else -> "https://static.wikia.nocookie.net/escapefromtarkov_gamepedia/images/d/d4/Jaeger_1_icon.png/revision/latest/scale-to-width-down/130?cb=20191101221027"
        }
        else -> "https://tarkov.dev/images/prapor-icon.jpg"
    }
}

fun String.currentTraderLevel(): Int {
    return when (this.lowercase()) {
        "prapor" -> {
            return UserSettingsModel.praporLevel.value.toString().toInt()
        }
        "therapist" -> {
            return UserSettingsModel.therapistLevel.value.toString().toInt()
        }
        "fence" -> {
            return UserSettingsModel.fenceLevel.value.toString().toInt()
        }
        "skier" -> {
            return UserSettingsModel.skierLevel.value.toString().toInt()
        }
        "peacekeeper" -> {
            return UserSettingsModel.peacekeeperLevel.value.toString().toInt()
        }
        "mechanic" -> {
            return UserSettingsModel.mechanicLevel.value.toString().toInt()
        }
        "ragman" -> {
            return UserSettingsModel.ragmanLevel.value.toString().toInt()
        }
        "jaeger" -> {
            return UserSettingsModel.jaegerLevel.value.toString().toInt()
        }
        else -> 1
    }
}

fun String.getHideoutIcon(): Int {
    Timber.d(this.lowercase())
    return when (this.lowercase()) {
        "air filtering unit" -> com.austinhodak.tarkovapi.R.drawable.air_filtering_unit_portrait
        "bitcoin farm" -> com.austinhodak.tarkovapi.R.drawable.bitcoin_farm_portrait
        "booze generator" -> com.austinhodak.tarkovapi.R.drawable.booze_generator_portrait
        "generator" -> com.austinhodak.tarkovapi.R.drawable.generator_portrait
        "heating" -> com.austinhodak.tarkovapi.R.drawable.heating_portrait
        "illumination" -> com.austinhodak.tarkovapi.R.drawable.illumination_portrait
        "intelligence center" -> com.austinhodak.tarkovapi.R.drawable.intelligence_center_portrait
        "lavatory" -> com.austinhodak.tarkovapi.R.drawable.lavatory_portrait
        "library" -> com.austinhodak.tarkovapi.R.drawable.library_portrait
        "medstation" -> com.austinhodak.tarkovapi.R.drawable.medstation_portrait
        "nutrition unit" -> com.austinhodak.tarkovapi.R.drawable.nutrition_unit_portrait
        "rest space" -> com.austinhodak.tarkovapi.R.drawable.rest_space_portrait
        "scav case" -> com.austinhodak.tarkovapi.R.drawable.scav_case_portrait
        "security" -> com.austinhodak.tarkovapi.R.drawable.security_portrait
        "shooting range" -> com.austinhodak.tarkovapi.R.drawable.shooting_range_portrait
        "solar power" -> com.austinhodak.tarkovapi.R.drawable.solar_power_portrait
        "stash" -> com.austinhodak.tarkovapi.R.drawable.stash_portrait
        "vents" -> com.austinhodak.tarkovapi.R.drawable.vents_portrait
        "water collector" -> com.austinhodak.tarkovapi.R.drawable.water_collector_portrait
        "workbench" -> com.austinhodak.tarkovapi.R.drawable.workbench_portrait
        else -> com.austinhodak.tarkovapi.R.drawable.workbench_portrait
    }
}

fun <T> setCrashKeys(vararg keys: Pair<String, T>) {
    keys.forEach {
        if (it.second is Boolean) {
            Firebase.crashlytics.setCustomKey(it.first, it.second as Boolean)
        }
        if (it.second is Double) {
            Firebase.crashlytics.setCustomKey(it.first, it.second as Double)
        }
        if (it.second is Float) {
            Firebase.crashlytics.setCustomKey(it.first, it.second as Float)
        }
        if (it.second is Int) {
            Firebase.crashlytics.setCustomKey(it.first, it.second as Int)
        }
        if (it.second is Long) {
            Firebase.crashlytics.setCustomKey(it.first, it.second as Long)
        }
        if (it.second is String) {
            Firebase.crashlytics.setCustomKey(it.first, it.second as String)
        }
    }
}
infix fun Int.greaterThan(value: Int): Boolean {
    return this > value
}

infix fun Int.lessThan(value: Int): Boolean {
    return this < value
}

infix fun Int.greaterThanOrEqualTo(value: Int): Boolean {
    return this >= value
}

infix fun Int.lessThanOrEqualTo(value: Int): Boolean {
    return this <= value
}

infix fun Int.equalTo(value: Int): Boolean {
    return this == value
}

infix fun Int.notEqualTo(value: Int): Boolean {
    return this != value
}

infix fun Int.between(value: IntRange): Boolean {
    return this in value
}

infix fun Int.notBetween(value: IntRange): Boolean {
    return this !in value
}

