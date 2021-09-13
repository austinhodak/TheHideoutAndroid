package com.austinhodak.thehideout

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.austinhodak.tarkovapi.models.MapInteractive
import com.austinhodak.thehideout.compose.theme.DarkPrimary
import com.austinhodak.thehideout.compose.theme.Green500
import com.austinhodak.thehideout.compose.theme.HideoutTheme
import com.austinhodak.thehideout.databinding.ActivityMapsBinding
import com.austinhodak.thehideout.utils.rememberMapViewWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import com.google.maps.android.ktx.awaitMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL


class MapsActivity : AppCompatActivity() {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mapView = rememberMapViewWithLifecycle()
            val selectedMapText = remember {
                mutableStateOf("interchange")
            }

            val customs = resources.openRawResource(R.raw.map_interchange).bufferedReader().use { it.readText() }
            val selectedMap = Gson().fromJson(customs, MapInteractive::class.java)

            HideoutTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Map") },
                            navigationIcon = {
                                IconButton(onClick = {

                                }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            elevation = 5.dp
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { }) {
                            Icon(painter = painterResource(id = R.drawable.ic_baseline_map_24), contentDescription = "Choose Map", tint = Color.Black)
                        }
                    }
                ) {
                    Box() {
                        AndroidView(factory = {
                            mapView
                        }, modifier = Modifier.padding(it)) {
                            CoroutineScope(Dispatchers.Main).launch {
                                val map = mapView.awaitMap()
                                setupMap(map, selectedMap)
                            }
                        }
                        FloatingActionButton(
                            onClick = { },
                            backgroundColor = DarkPrimary,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .size(40.dp),
                        ) {
                            Icon(painter = painterResource(id = R.drawable.ic_baseline_map_24), contentDescription = "Choose Map", tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    private fun setupMap(map: GoogleMap, selectedMap: MapInteractive) {
        mMap = map
        val googleMap = map

        val tileProvider: TileProvider = object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                if (selectedMap == null) return null

                val normalizedCoords = getNormalizedCoord(x, y, zoom, selectedMap) ?: return null
                val url = "${selectedMap.map?.url}${selectedMap.getFirstMap()?.path}/${zoom}/${normalizedCoords.first}/${normalizedCoords.second}.png"
                Timber.d(url)

                return if (!checkTileExists(x, y, zoom)) {
                    null
                } else try {
                    URL(url)
                } catch (e: MalformedURLException) {
                    throw AssertionError(e)
                }
            }

            private fun checkTileExists(x: Int, y: Int, zoom: Int): Boolean {
                val minZoom = 8
                val maxZoom = 15
                return zoom in minZoom..maxZoom
            }
        }

        googleMap.addTileOverlay(TileOverlayOptions().tileProvider(tileProvider).zIndex(2f))

        googleMap.setMaxZoomPreference(selectedMap.getFirstMap()?.max_zoom?.toFloat() ?: 15f)
        googleMap.setMinZoomPreference(selectedMap.getFirstMap()?.min_zoom?.toFloat() ?: 8f)

        googleMap.mapType = GoogleMap.MAP_TYPE_NONE

        googleMap.setInfoWindowAdapter(CustomInfoWindowAdapter())

        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(0.0, 0.0)))

        val one = LatLng(1.2, 0.0)
        val two = LatLng(0.0, -1.2)

        val builder = LatLngBounds.Builder()
        builder.include(one)
        builder.include(two)

        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels

        mMap.setLatLngBoundsForCameraTarget(bounds)
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0))
        mMap.moveCamera(CameraUpdateFactory.zoomTo(selectedMap.mapConfig?.initial_zoom?.toFloat() ?: 9f))

        val polygonOptions = PolygonOptions()
            .add(
                LatLng(5.0, -5.0),
                LatLng(5.0, 5.0),
                LatLng(0.0, 5.0),
                LatLng(-5.0, -5.0),
            ).zIndex(1f).fillColor(android.graphics.Color.BLACK)

        mMap.addPolygon(polygonOptions)

        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = false

        selectedMap.locations?.forEach {
            if (it?.latitude != null && it.longitude != null) {
                val icon = when (it.category_id) {
                    960 -> R.drawable.icon_ammo
                    1013 -> R.drawable.icon_valuable
                    948 -> R.drawable.icon_cache
                    941 -> R.drawable.icon_crate
                    959 -> R.drawable.icon_dead_scav
                    969 -> R.drawable.icon_duffle
                    1017 -> R.drawable.icon_filing_cabinet
                    942 -> R.drawable.icon_greande
                    961 -> R.drawable.icon_jacket
                    947 -> R.drawable.icon_key
                    946 -> R.drawable.icon_loose_loot
                    943 -> R.drawable.icon_meds
                    945 -> R.drawable.icon_money
                    949 -> R.drawable.icon_unknown
                    968 -> R.drawable.icon_pc
                    1016 -> R.drawable.icon_food
                    944 -> R.drawable.icon_safe
                    970 -> R.drawable.icon_toolbox
                    958 -> R.drawable.icon_weapons
                    1015 -> R.drawable.icon_mods
                    951 -> R.drawable.icon_boss
                    950 -> R.drawable.icon_scav
                    1011 -> R.drawable.icon_sniper
                    1014 -> R.drawable.icon_easter_eggs
                    954 -> R.drawable.icon_extract
                    952 -> R.drawable.icon_location
                    957 -> R.drawable.icon_lock
                    956 -> R.drawable.icon_unknown
                    955 -> R.drawable.icon_quest
                    953 -> R.drawable.icon_spawn
                    972 -> R.drawable.icon_transition
                    else -> R.drawable.icon_unknown
                }

                val bitmapDrawable = (resources.getDrawable(icon) as BitmapDrawable).bitmap
                val markerIcon = Bitmap.createScaledBitmap(bitmapDrawable, 72, 72, false)

                val marker = mMap.addMarker(
                    MarkerOptions().position(LatLng(it.latitude!!.toDouble(), it.longitude!!.toDouble())).title(it.title).icon(
                        BitmapDescriptorFactory.fromBitmap(bitmapDrawable)
                    ).snippet(it.description).zIndex(if (it.category_id == 972) 1f else 2f)
                )

                if (it.category_id == 952) {
                    mMap.addMarker(
                        MarkerOptions().position(LatLng(it.latitude!!.toDouble()-0.0003, it.longitude!!.toDouble())).icon(
                            BitmapDescriptorFactory.fromBitmap(textAsBitmap(it.title ?: "", 26f, resources.getColor(R.color.white), Color.Transparent.toArgb()))
                        )
                    )
                }

                if (it.category_id == 954) {
                    mMap.addMarker(
                        MarkerOptions().position(LatLng(it.latitude!!.toDouble()-0.0003, it.longitude!!.toDouble())).icon(
                            BitmapDescriptorFactory.fromBitmap(textAsBitmap(it.title ?: "", 36f, resources.getColor(R.color.white), Green500.toArgb()))
                        )
                    )
                }

                marker?.tag = it
            }
        }
    }

    fun getNormalizedCoord(x: Int, y: Int, zoom: Int, map: MapInteractive?): Pair<Int, Int>? {
        if (map == null) return null
        val tileSet = map.getFirstMap()!!.bounds!!

        val bounds = when (zoom) {
            8 -> tileSet.`8`
            9 -> tileSet.`9`
            10 -> tileSet.`10`
            11 -> tileSet.`11`
            12 -> tileSet.`12`
            13 -> tileSet.`13`
            14 -> tileSet.`14`
            15 -> tileSet.`15`
            16 -> tileSet.`16`
            17 -> tileSet.`17`
            else -> tileSet.`8`
        }

        val tileRange = 1 shl zoom

        if (y < 0 || y >= tileRange) return null

        if (x < 0 || x >= tileRange) return null

        if (x < bounds?.x?.min!! || x > bounds.x?.max!!) {
            return null
        }

        if (y < bounds.y?.min!! || y > bounds.y?.max!!) {
            return null
        }

        return Pair(x, y)
    }


    internal inner class CustomInfoWindowAdapter : GoogleMap.InfoWindowAdapter {

        private val window: View = layoutInflater.inflate(R.layout.map_compose, null)

        override fun getInfoWindow(marker: Marker): View {
            render(marker, window)
            return window
        }

        override fun getInfoContents(marker: Marker): View {
            render(marker, window)
            return window
        }

        private fun render(marker: Marker, window: View) {
            val location = marker.tag as MapInteractive.Location

            val title = window.findViewById<TextView>(R.id.marker_title)
            val subtitle1 = window.findViewById<TextView>(R.id.marker_subtitle_1)
            //val subtitle2 = window.findViewById<TextView>(R.id.marker_subtitle_2)

            title.text = location.title
            subtitle1.text = location.description
        }
    }

    fun textAsBitmap(text: String, textSize: Float, textColor: Int, outlineColor: Int): Bitmap? {
        val benderFont = ResourcesCompat.getFont(this, R.font.bender_bold)
        val bender = ResourcesCompat.getFont(this, R.font.bender_bold)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setTypeface(benderFont)
        paint.textSize = textSize
        paint.color = textColor
        paint.textAlign = Paint.Align.LEFT

        val stkPaint = Paint()
        stkPaint.style = Paint.Style.STROKE
        stkPaint.textSize = textSize
        stkPaint.setTypeface(benderFont)
        stkPaint.strokeWidth = 3f
        stkPaint.color = outlineColor

        val baseline: Float = -paint.ascent() // ascent() is negative
        val width = (paint.measureText(text) + 0.5f).toInt() // round
        val height = (baseline + paint.descent() + 0.5f).toInt()
        val image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(image)

        canvas.drawText(text, 0f, baseline, stkPaint)
        canvas.drawText(text, 0f, baseline, paint)
        return image
    }
}