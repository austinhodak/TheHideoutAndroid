package com.austinhodak.thehideout.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.core.app.NotificationCompat
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.status.ServerStatusActivity
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

        if (remoteMessage.data.isNotEmpty()) {
            if (remoteMessage.data.containsKey("title") && remoteMessage.data.containsKey("content")) {
                val title = remoteMessage.data["title"]
                val content = remoteMessage.data["content"]
                sendNotification(title ?: "", content ?: "")
            } else if (remoteMessage.data.containsKey("data")) {
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
                    sendNotification(title, message)
                }

                if (data.has("message")) {
                    val message = data.getJSONObject("message")
                    val content = message.getString("content")
                    val time = message.getString("time")
                    val type = message.getInt("type")
                    val solveTime = message.getString("solveTime")

                    if (UserSettingsModel.serverStatusMessages.value)
                    sendNotification("New Status Update", content)
                }
            }

            /*val fleaItem = JSONObject(remoteMessage.data["fleaItem"])
            val alertItem = JSONObject(remoteMessage.data["alertItem"])

            val whenText = when (alertItem["when"] as String) {
                "below" -> "dropped below"
                "above" -> "risen above"
                else -> ""
            }

            *//*val notiText = "${fleaItem["name"]} has $whenText your alert price of ${(alertItem["price"] as Int).getPrice("₽")}.\n\n" +
                    "Current Price: ${(fleaItem["price"] as Int).getPrice("₽")}. \uD83D\uDE4C"*//*

            val builder = NotificationCompat.Builder(this, "FLEA_ALERTS").apply {
                setSmallIcon(R.drawable.hideout_shadow_1)
                setContentTitle("Flea Market Price Alert \uD83D\uDCB8")
                priority = NotificationCompat.PRIORITY_DEFAULT
                setContentText("")
                setStyle(NotificationCompat.BigTextStyle().bigText(""))
            }

            Glide.with(this).asBitmap().load(fleaItem["icon"]).into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    builder.setLargeIcon(resource)
                    notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
                }
            })*/
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_flea_alert)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("FLEA_ALERTS", name, importance)
            // Register the channel with the system
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String, message: String) {
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
}