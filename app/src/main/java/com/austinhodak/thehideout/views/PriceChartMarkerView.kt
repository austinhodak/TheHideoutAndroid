package com.austinhodak.thehideout.views

import android.content.Context
import android.view.View
import android.widget.TextView
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.google.firebase.database.DataSnapshot
import timber.log.Timber
import java.text.SimpleDateFormat


class PriceChartMarkerView(context: Context?, layoutResource: Int) :
    MarkerView(context, layoutResource) {
    private val tvPrice: TextView = findViewById<View>(R.id.marker_price) as TextView
    private val tvDate: TextView = findViewById<View>(R.id.marker_date) as TextView

    override fun refreshContent(e: Entry, highlight: Highlight?) {
        val dataSnapshot = e.data as DataSnapshot

        val simpleDateFormat = SimpleDateFormat("MM/dd hh:mm")
        val dateString = simpleDateFormat.format(dataSnapshot.key?.removeSurrounding("\"")?.toFloat())

        tvPrice.text = (dataSnapshot.value as Long).toInt().asCurrency()

        tvDate.text = dateString
        super.refreshContent(e, highlight)
    }

    private var mOffset: MPPointF? = null
    override fun getOffset(): MPPointF? {
        if (mOffset == null) {
            // center the marker horizontally and vertically
            mOffset = MPPointF((-(width / 2)).toFloat(), (-height).toFloat() - 20f)
        }
        return mOffset
    }

    init {
        // this markerview only displays a textview
    }
}