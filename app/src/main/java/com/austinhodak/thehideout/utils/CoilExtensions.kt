package com.austinhodak.thehideout.utils

import android.content.Context
import coil.ComponentRegistry
import coil.ImageLoader
import coil.map.Mapper
import coil.request.ImageRequest
import coil.request.Options
import coil.request.Parameters
import com.austinhodak.tarkovapi.room.models.Pricing
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.realm.converters.icon
import com.austinhodak.thehideout.realm.models.Trader
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoilExtensions @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private fun ImageLoader.Builder.baseComponents() = this.apply {
        components(
            ComponentRegistry.Builder()
                .add(TraderMapper())
                .add(OldTraderMapper())
                .build()
        )
    }

    private fun ImageLoader.Builder.unknownPlaceholder(
        placeholder: Int = R.drawable
            .unknown_item_icon
    ) =
        this.apply {
            placeholder(placeholder)
        }

    val crossFadeLoader: ImageLoader by lazy {
        ImageLoader.Builder(context)
            .crossfade(true)
            .baseComponents()
            .build()
    }

    class TraderMapper : Mapper<Trader, String> {
        override fun map(data: Trader, options: Options) = data.icon(showLevel = false)
    }

    class OldTraderMapper : Mapper<Pricing.BuySellPrice, String> {
        override fun map(data: Pricing.BuySellPrice, options: Options) =
            data.traderImage(showLevel = true)
    }

    class Test : Mapper<Pricing.BuySellPrice, String> {
        override fun map(data: Pricing.BuySellPrice, options: Options) =
            data.traderImage(showLevel = true)
    }
}

fun ImageRequest.Builder.traderLevel(level: Int) = this.parameters(
    Parameters.Builder().set("traderLevel", level).build()
)

fun ImageLoader.unknown() = this.newBuilder()
    .placeholder(R.drawable.unknown_item_icon)
    .build()