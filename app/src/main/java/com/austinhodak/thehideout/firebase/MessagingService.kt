package com.austinhodak.thehideout.firebase

import android.app.ActivityManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.core.app.NotificationCompat
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.room.enums.Traders
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.NavActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.flea_market.detail.FleaItemDetail
import com.austinhodak.thehideout.status.ServerStatusActivity
import com.austinhodak.thehideout.utils.logNotification
import com.austinhodak.thehideout.utils.pushToken
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import timber.log.Timber

@ExperimentalFoundationApi
class MessagingService : FirebaseMessagingService() {

    private val notificationManager: NotificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Timber.d(remoteMessage.data.toString())

        if (remoteMessage.data.isNotEmpty()) {

            if (remoteMessage.data.containsKey("title") && remoteMessage.data.containsKey("content") && !remoteMessage.data.containsKey("restock") && !remoteMessage.data.containsKey("priceAlert")) {
                val title = remoteMessage.data["title"]
                val content = remoteMessage.data["content"]
                sendServerStatusNotification(title ?: "", content ?: "")
            } else if (remoteMessage.data.containsKey("data") && !remoteMessage.data.containsKey("restock") && !remoteMessage.data.containsKey("priceAlert")) {
                val data = JSONObject(remoteMessage.data.getValue("data"))
                Timber.d("$data")
                if (data.has("status")) {
                    val status = data.getJSONObject("status")
                    val oldStatus = data.optJSONObject("oldStatus")

                    val name = status.getString("name")
                    val message = status.getString("message")
                    val statusCode = status.getInt("status")
                    val oldStatusCode = oldStatus?.optInt("status", 0) ?: 0

                    val title = if (oldStatusCode > statusCode) {
                        "\uD83D\uDCC9 Server Status Update"
                    } else {
                        "\uD83D\uDCC8 Server Status Update"
                    }

                    if (UserSettingsModel.serverStatusUpdates.value)
                    sendServerStatusNotification(title, message)
                }

                if (data.has("message")) {
                    val message = data.getJSONObject("message")
                    val content = message.getString("content")
                    val time = message.getString("time")
                    val type = message.getInt("type")
                    val solveTime = message.getString("solveTime")

                    if (UserSettingsModel.serverStatusMessages.value)
                    sendServerStatusNotification("New Status Update", content)
                }
            } else if (remoteMessage.data.containsKey("restock")) {
                //User has restock notifications off, do nothing.
                if (!UserSettingsModel.globalRestockAlert.value) return
                if (UserSettingsModel.globalRestockAlertAppOpen.value) {
                    if (!isAppInforegrounded()) return
                }

                Timber.d("RESTOCK!")

                val d = JSONObject(remoteMessage.data.getValue("restock"))
                val data = d.getJSONObject("restock")

                val trader = Traders.values().find { it.id.equals(data.optString("trader", "prapor"), true) } ?: Traders.PRAPOR
                val title = remoteMessage.data["title"]
                val content = remoteMessage.data["content"]
                sendRestockNotification(title ?: "", content ?: "", trader)

                logNotification("notification_receive", trader.id, "TRADER_RESTOCK")
            } else if (remoteMessage.data.containsKey("priceAlert")) {
                if (!UserSettingsModel.priceAlertsGlobalNotifications.value) return

                val data = JSONObject(remoteMessage.data.getValue("priceAlert")).getJSONObject("priceAlert")
                val item = data.getJSONObject("item")
                val alert = data.getJSONObject("alert")

                Timber.d(data.toString())

                val whenText = when (alert["condition"] as String) {
                    "below" -> "dropped below"
                    "above" -> "risen above"
                    else -> ""
                }

                try {
                    val notiText = "${item["shortName"]} has $whenText your alert price of ${(alert["price"] as Int).asCurrency()}.\n\n" +
                            "Current Price: ${(item["lastLowPrice"] as Int).asCurrency()} \uD83D\uDE4C"

                    sendPriceAlertNotification("", notiText, item)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            } else if (remoteMessage.data.containsKey("raid")) {
                val raidData = JSONObject(remoteMessage.data.getValue("raid"))
                val eventType = raidData.optString("event")

                if (eventType.isNotEmpty()) {
                    when(eventType) {
                        "MatchingCompleted" -> {
                            if (UserSettingsModel.raidAlertMatched.value) {
                                sendRaidAlertNotification("You have matched to a raid!", "The raid will be starting soon.", 101)
                            }
                        }
                        "GameStarting" -> {
                            if (UserSettingsModel.raidAlertCountdown.value) {
                                sendRaidAlertNotification("The raid is starting!", "The countdown has started!", 102)
                            }
                        }
                        "GameStarted" -> {
                            if (UserSettingsModel.raidAlertStarted.value) {
                                sendRaidAlertNotification("The raid has started!", "You are currently in a raid!", 103)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun cancelRaidAlerts(id: Int) {
        when (id) {
            101 -> {
                notificationManager.cancel(101)
                notificationManager.cancel(102)
                notificationManager.cancel(103)
            }
            102 -> {
                notificationManager.cancel(102)
            }
            103 -> {
                notificationManager.cancel(103)
            }
        }
    }

    private fun sendRaidAlertNotification(title: String, content: String, id: Int) {
        cancelRaidAlerts(id)

        if (!UserSettingsModel.raidAlertGlobalNotifications.value) return

        val intent = Intent(this, NavActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        intent.putExtra("fromNoti", true)

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(this, "RAID_ALERTS").apply {
            setSmallIcon(R.drawable.hideout_shadow_1)
            setContentTitle(title)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentText(content)
            setStyle(NotificationCompat.BigTextStyle().bigText(content))
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        notificationManager.notify(id, builder.build())
    }

    private fun sendPriceAlertNotification(title: String, content: String, item: JSONObject) {
        val intent = Intent(this, FleaItemDetail::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        intent.putExtra("fromNoti", true)
        intent.putExtra("id", item.getString("id"))

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val url = item.getString("iconLink")
        val builder = NotificationCompat.Builder(this, "PRICE_ALERTS").apply {
            setSmallIcon(R.drawable.hideout_shadow_1)
            setContentTitle("Flea Market Price Alert \uD83D\uDCB8")
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentText(content)
            setContentIntent(pendingIntent)
            setStyle(NotificationCompat.BigTextStyle().bigText(""))
        }

        Glide.with(this).asBitmap().load(url).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                builder.setLargeIcon(resource)
                notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
            }
        })
    }

    private fun sendRestockNotification(title: String, message: String, trader: Traders) {
        //FUCK
        if (!UserSettingsModel.globalRestockAlert.value) return

        val intent = Intent(this, NavActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        intent.putExtra("fromNoti", true)
        intent.putExtra("trader", trader.id)

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(this, "TRADER_RESTOCK").apply {
            setSmallIcon(R.drawable.hideout_shadow_1)
            setContentTitle(title)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentText(message)
            setStyle(NotificationCompat.BigTextStyle().bigText(message))
            setContentIntent(pendingIntent)
            setAutoCancel(true)
            setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, trader.icon), 128, 128, false))
        }

        notificationManager.notify(trader.int, builder.build())

        /*Glide.with(this).asBitmap().load(trader.icon).into(object : SimpleTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                builder.setLargeIcon(resource)

            }
        })*/
    }

    private fun sendServerStatusNotification(title: String, message: String) {
        if (!UserSettingsModel.serverStatusNotifications.value) return

        val intent = Intent(this, ServerStatusActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        intent.putExtra("fromNoti", true)

        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val builder = NotificationCompat.Builder(this, "SERVER_STATUS").apply {
            setSmallIcon(R.drawable.hideout_shadow_1)
            setContentTitle(title)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setContentText(message)
            setStyle(NotificationCompat.BigTextStyle().bigText(message))
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onNewToken(token: String) {
        pushToken(token)
    }

    fun isAppInforegrounded() : Boolean {
        val appProcessInfo =  ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE)
    }
}