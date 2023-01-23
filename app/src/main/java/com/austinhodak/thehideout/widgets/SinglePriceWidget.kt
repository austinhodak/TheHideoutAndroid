package com.austinhodak.thehideout.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.RemoteViews
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.graphics.toArgb
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.NavActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.features.flea_market.detail.FleaItemDetail
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.AppWidgetTarget
import com.bumptech.glide.request.transition.Transition
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SinglePriceWidget : AppWidgetProvider() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, tarkovRepo)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // When the user deletes the widget, delete the preference associated with it.
        for (appWidgetId in appWidgetIds) {
            //deleteTitlePref(context, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        super.onReceive(context, intent)
    }
}


@OptIn(ExperimentalFoundationApi::class)
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int,
    tarkovRepo: TarkovRepo
) {

    Timber.d("Updating widgets!")

    val prefs = context.getSharedPreferences("widget", 0)

    CoroutineScope(Dispatchers.Main).launch {
        val item = tarkovRepo.getItemByID(prefs.getString("widget_$appWidgetId", "59faff1d86f7746c51718c9c") ?: "59faff1d86f7746c51718c9c").first()

        val pendingIntentTemplate = PendingIntent.getActivity(context, appWidgetId, Intent(context, FleaItemDetail::class.java).apply {
            putExtra("id", item.id)
            putExtra("fromNoti", true)
        }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val views = RemoteViews(context.packageName, R.layout.single_price_widget).apply {
            setOnClickPendingIntent(R.id.single_price_layout, pendingIntentTemplate)
        }

        val awt: AppWidgetTarget = object : AppWidgetTarget(context.applicationContext, R.id.single_price_image, views, appWidgetId) {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                super.onResourceReady(resource, transition)
            }
        }

        val options = RequestOptions().override(300, 300)

        Glide.with(context.applicationContext).asBitmap().load(item.pricing?.getTransparentIcon()).apply(options).into(awt)

        views.setTextViewText(R.id.single_price_name, item.getPrice().asCurrency())

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

        views.setInt(R.id.single_price_layout, "setBackgroundColor", color.toArgb())

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}