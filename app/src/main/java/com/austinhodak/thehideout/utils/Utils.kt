package com.austinhodak.thehideout.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.austinhodak.thehideout.R
import com.bumptech.glide.Glide

class Utils {
    companion object {
        @JvmStatic @BindingAdapter("keyImageUrl")
        fun loadImage(view: ImageView, url: String?) {
            if (url != null)
            Glide.with(view.context).load(url).into(view)
        }

        @JvmStatic @BindingAdapter("questTraderImage")
        fun loadTraderImage(view: ImageView, trader: String?) {
            when (trader) {
                "Prapor" -> view.setImageResource(R.drawable.prapor_portrait)
                "Therapist" -> view.setImageResource(R.drawable.therapist_portrait)
                "Fence" -> view.setImageResource(R.drawable.fence_portrait)
                "Skier" -> view.setImageResource(R.drawable.skier_portrait)
                "Peacekeeper" -> view.setImageResource(R.drawable.peacekeeper_portrait)
                "Mechanic" -> view.setImageResource(R.drawable.mechanic_portrait)
                "Ragman" -> view.setImageResource(R.drawable.ragman_portrait)
                "Jaeger" -> view.setImageResource(R.drawable.jaeger_portrait)
            }
        }
    }
}