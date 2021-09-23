package com.austinhodak.thehideout.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.graphics.Color
import coil.annotation.ExperimentalCoilApi
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.tarkovapi.type.ItemSourceName
import com.austinhodak.thehideout.BuildConfig
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.AmmoDetailActivity
import com.austinhodak.thehideout.calculator.models.CAmmo
import com.austinhodak.thehideout.calculator.models.CArmor
import com.austinhodak.thehideout.compose.theme.Green500
import com.austinhodak.thehideout.compose.theme.Red500
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.weapons.detail.WeaponDetailActivity
import com.austinhodak.thehideout.weapons.mods.ModDetailActivity
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import timber.log.Timber
import java.text.NumberFormat
import java.util.*
import kotlin.math.round

fun isDebug(): Boolean = BuildConfig.DEBUG

fun Double.asColor(reverse: Boolean = false): Color {
    return when {
        this > 0.0 -> if (!reverse) Green500 else Red500
        this < 0.0 -> if (!reverse) Red500 else Green500
        else -> Color.Unspecified
    }
}

fun Int.asColor(reverse: Boolean = false): Color {
    return when {
        this > 0.0 -> if (!reverse) Green500 else Red500
        this < 0.0 -> if (!reverse) Red500 else Green500
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

fun Pricing.BuySellPrice.traderImage(): String {
    //Flea Market Icon
    if (this.source == "fleaMarket") return "https://tarkov-tools.com/images/flea-market-icon.jpg"
    Timber.d(this.toString())
    when {
        requirements.isNullOrEmpty() -> {
            return when (this.source) {
                ItemSourceName.prapor.rawValue -> "https://tarkov-tools.com/images/prapor-icon.jpg"
                ItemSourceName.therapist.rawValue -> "https://tarkov-tools.com/images/therapist-icon.jpg"
                ItemSourceName.fence.rawValue -> "https://tarkov-tools.com/images/fence-icon.jpg"
                ItemSourceName.skier.rawValue -> "https://tarkov-tools.com/images/skier-icon.jpg"
                ItemSourceName.peacekeeper.rawValue -> "https://tarkov-tools.com/images/peacekeeper-icon.jpg"
                ItemSourceName.mechanic.rawValue -> "https://tarkov-tools.com/images/mechanic-icon.jpg"
                ItemSourceName.ragman.rawValue -> "https://tarkov-tools.com/images/ragman-icon.jpg"
                ItemSourceName.jaeger.rawValue -> "https://tarkov-tools.com/images/jaeger-icon.jpg"
                else -> "https://tarkov-tools.com/images/prapor-icon.jpg"
            }
        }
        requirements.first().type == "loyaltyLevel" -> {
            val level = requirements.first().value
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
                else -> "https://tarkov-tools.com/images/prapor-icon.jpg"
            }
        }
        else -> {
            return "https://tarkov-tools.com/images/flea-market-icon.jpg"
        }
    }
}

fun AmmoCalibers(): List<String> = arrayListOf(
    "Caliber762x35",
    "Caliber86x70",
    "Caliber366TKM",
    "Caliber1143x23ACP",
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
    "Caliber9x39",
)

fun <T> Context.openActivity(it: Class<T>, extras: Bundle.() -> Unit = {}) {
    val intent = Intent(this, it)
    intent.putExtras(Bundle().apply(extras))
    startActivity(intent)
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

fun getCaliberName(caliber: String?): String {
    return when (caliber) {
        "Caliber762x35" -> ".300 Blackout"
        "Caliber86x70" -> ".338 Lapua"
        "Caliber366TKM" -> ".366 TKM"
        "Caliber1143x23ACP" -> ".45 ACP"
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

fun Int.getPrice(c: String): String {
    val currency = c.replace("R", "₽").replace("D", "$").replace("E", "€")
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance(getCurrencyString(currency))
    return if (currency == "₽") {
        "${format.format(this).replace("RUB", "")}₽"
    } else {
        format.format(this)
    }

}

fun Double.getPrice(c: String): String {
    val currency = c.replace("R", "₽").replace("D", "$").replace("E", "€")
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance(getCurrencyString(currency))
    return if (currency == "₽") {
        "${format.format(this).replace("RUB", "")}₽"
    } else {
        format.format(this)
    }

}

fun Long.getPrice(c: String): String {
    val currency = c.replace("R", "₽").replace("D", "$").replace("E", "€")
    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 0
    format.currency = Currency.getInstance(getCurrencyString(currency))
    return if (currency == "₽") {
        "${format.format(this).replace("RUB", "")}₽"
    } else {
        format.format(this)
    }

}

fun String.openWithCustomTab(context: Context) {
    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(this))
}

fun getCurrencyString(string: String): String {
    return when (string) {
        "$" -> "USD"
        "€" -> "EURO"
        "₽" -> "RUB"
        else -> "RUB"
    }
}

fun getCaliberShortName(caliber: String?): String {
    return when (caliber) {
        "Caliber762x35" -> ".300"
        "Caliber86x70" -> ".338"
        "Caliber366TKM" -> ".366 TKM"
        "Caliber1143x23ACP" -> ".45 ACP"
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
        else -> "Unknown Ammo Type"
    }
}

fun userRefTracker(ref: String? = null): DatabaseReference {
    return questsFirebase.child("users/${Firebase.auth.uid}/$ref/")
}

fun uid(): String? {
    return Firebase.auth.uid
}

fun pushToken(token: String) {
    if (Firebase.auth.currentUser != null) {
        userRefTracker("token").setValue(token)
    }
}

fun log(event: String, itemID: String, itemName: String, contentType: String) {
    Firebase.analytics.logEvent(event) {
        param(FirebaseAnalytics.Param.ITEM_ID, itemID)
        param(FirebaseAnalytics.Param.ITEM_NAME, itemName)
        param(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
    }
}

fun logScreen(name: String) {
    Firebase.analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
        param(FirebaseAnalytics.Param.SCREEN_NAME, name)
        param(FirebaseAnalytics.Param.SCREEN_CLASS, name)
    }
}

fun Int.addQuotes(): String {
    return "\"$this\""
}

fun Any.addQuotes(): String {
    return "\"$this\""
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

val questsFirebase = Firebase.database("https://hideout-tracker.firebaseio.com").reference

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