package com.austinhodak.thehideout.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.utils.getPrice
import com.austinhodak.thehideout.utils.pushToken
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject

class MessagingService : FirebaseMessagingService() {

    val notificationManager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        createNotificationChannel()

        if (remoteMessage.data.isNotEmpty()) {
            val fleaItem = JSONObject(remoteMessage.data["fleaItem"])
            val alertItem = JSONObject(remoteMessage.data["alertItem"])

            val whenText = when (alertItem["when"] as String) {
                "below" -> "dropped below"
                "above" -> "risen above"
                else -> ""
            }

            val notiText = "${fleaItem["name"]} has $whenText your alert price of ${(alertItem["price"] as Int).getPrice("₽")}.\n\n" +
                    "Current Price: ${(fleaItem["price"] as Int).getPrice("₽")}. \uD83D\uDE4C"

            val builder = NotificationCompat.Builder(this, "FLEA_ALERTS").apply {
                setSmallIcon(R.drawable.hideout_shadow_1)
                setContentTitle("Flea Market Price Alert \uD83D\uDCB8")
                priority = NotificationCompat.PRIORITY_DEFAULT
                setContentText(notiText)
                setStyle(NotificationCompat.BigTextStyle().bigText(notiText))
            }

            Glide.with(this).asBitmap().load(fleaItem["icon"]).into(object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    builder.setLargeIcon(resource)
                    notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
                }
            })

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

    override fun onNewToken(token: String) {
        pushToken(token)
    }
}