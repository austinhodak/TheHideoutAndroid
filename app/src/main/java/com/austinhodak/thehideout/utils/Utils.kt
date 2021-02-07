package com.austinhodak.thehideout.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

class Utils {
    companion object {
        @JvmStatic @BindingAdapter("keyImageUrl")
        fun loadImage(view: ImageView, url: String?) {
            if (url != null)
            Glide.with(view.context).load(url).into(view)
        }
    }
}