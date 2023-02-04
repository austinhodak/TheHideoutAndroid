package com.austinhodak.thehideout.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.widget.TextView
import coil.imageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ImageGetter(
    private val res: Resources,
    private val htmlTextView: TextView,
    private val context: Context
) : Html.ImageGetter {

    // Function needs to overridden when extending [Html.ImageGetter] ,
    // which will download the image
    override fun getDrawable(url: String): Drawable {
        val holder = BitmapDrawablePlaceHolder(res, null)

        // Coroutine Scope to download image in Background
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {

                // downloading image in bitmap format using [Picasso] Library

                val request = ImageRequest.Builder(context)
                    .data(url)
                    .target(
                        onSuccess = { result ->
                            val drawable = result

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
                        }
                    )

                context.imageLoader.enqueue(request.build())
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