package com.austinhodak.thehideout.utils

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageGetter(
    private val res: Resources,
    private val htmlTextView: TextView
) : Html.ImageGetter {

    // Function needs to overridden when extending [Html.ImageGetter] ,
    // which will download the image
    override fun getDrawable(url: String): Drawable {
        val holder = BitmapDrawablePlaceHolder(res, null)

        // Coroutine Scope to download image in Background
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {

                // downloading image in bitmap format using [Picasso] Library

                Glide.with(htmlTextView).asBitmap().load(url).into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        val drawable = BitmapDrawable(res, resource)

                        val width = getScreenWidth() - 150

                        // Images may stretch out if you will only resize width,
                        // hence resize height to according to aspect ratio
                        val aspectRatio: Float =
                            (drawable.intrinsicWidth.toFloat()) / (drawable.intrinsicHeight.toFloat())
                        val height = width / aspectRatio
                        drawable.setBounds(0, 0, width, height.toInt())
                        holder.setDrawable(drawable)
                        holder.setBounds(0, 0, width, height.toInt())

                        htmlTextView.text = htmlTextView.text
                       /* withContext(Dispatchers.Main) {

                        }*/
                    }
                })
            }
        }
        return holder
    }

    // Actually Putting images
    internal class BitmapDrawablePlaceHolder(res: Resources, bitmap: Bitmap?) :
        BitmapDrawable(res, bitmap) {
        private var drawable: Drawable? = null

        override fun draw(canvas: Canvas) {
            drawable?.run { draw(canvas) }
        }

        fun setDrawable(drawable: Drawable) {
            this.drawable = drawable
        }
    }

    // Function to get screenWidth used above
    fun getScreenWidth() =
        Resources.getSystem().displayMetrics.widthPixels
}