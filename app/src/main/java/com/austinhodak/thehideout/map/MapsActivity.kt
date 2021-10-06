package com.austinhodak.thehideout.map

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Pair
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.models.MapInteractive
import com.austinhodak.tarkovapi.models.QuestExtra
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.thehideout.GodActivity
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.map.viewmodels.MapViewModel
import com.austinhodak.thehideout.quests.QuestDetailActivity
import com.austinhodak.thehideout.utils.Map
import com.austinhodak.thehideout.utils.isDebug
import com.austinhodak.thehideout.utils.openActivity
import com.austinhodak.thehideout.utils.rememberMapViewWithLifecycle
import com.bumptech.glide.Glide
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.ktx.awaitMap
import com.stfalcon.imageviewer.StfalconImageViewer
import dagger.hilt.android.AndroidEntryPoint
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import timber.log.Timber
import java.net.MalformedURLException
import java.net.URL

@ExperimentalCoroutinesApi
@ExperimentalFoundationApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
@AndroidEntryPoint
class MapsActivity : GodActivity() {

    private val mapViewModel: MapViewModel by viewModels()
    private var markers: MutableList<Marker> = arrayListOf()

    lateinit var map: GoogleMap

    @ExperimentalAnimationApi
    @SuppressLint("CheckResult", "PotentialBehaviorOverride")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mapView = rememberMapViewWithLifecycle()
            val selectedMapText by mapViewModel.map.observeAsState()

            val selectedMap by mapViewModel.mapData.observeAsState()

            val backdropScaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
            val scope = rememberCoroutineScope()

            var isDistanceToolActive by remember {
                mutableStateOf(false)
            }

            val snackbarText: String? by remember {
                mutableStateOf("Select first point.")
            }

            val selectedCats by UserSettingsModel.mapMarkerCategories.flow.collectAsState(initial = emptySet())

            var selectedPoints: Pair<LatLng?, LatLng?>? = null

            scope.launch {
                selectedMap?.let {
                    UserSettingsModel.mapMarkerCategories.update(
                        it.groups?.flatMap { it?.categories!! }?.map { it?.id!! }?.toSet()!!
                    )
                    updateMarkers(selectedCats.toMutableList())
                }
            }

            var selectedMarker by remember { mutableStateOf<Marker?>(null) }

            if (isDistanceToolActive && snackbarText != null) {
                scope.launch {
                    bottomSheetScaffoldState.snackbarHostState.showSnackbar(snackbarText!!, "CANCEL", SnackbarDuration.Indefinite)
                }
            }

            val systemUiController = rememberSystemUiController()
            systemUiController.setNavigationBarColor(Color.Transparent)

            val questsExtra by mapViewModel.questsExtra.observeAsState()

            HideoutTheme {
                Box {
                    BackdropScaffold(
                        scaffoldState = backdropScaffoldState,
                        gesturesEnabled = false,
                        appBar = {
                            TopAppBar(
                                title = { Text(selectedMap?.map?.title ?: "Map") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        finish()
                                    }) {
                                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                    }
                                },
                                backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                                elevation = 5.dp,
                                actions = {

                                }
                            )
                        },
                        backLayerContent = {
                            var isCategoriesVisible by remember { mutableStateOf(true) }
                            var isSettingsVisible by remember { mutableStateOf(false) }

                            Column(
                                Modifier
                                    .height(IntrinsicSize.Min)
                                    .padding(start = 8.dp, end = 8.dp, bottom = 16.dp, top = 0.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF282828))
                                        .clickable {
                                            isCategoriesVisible = !isCategoriesVisible
                                        }
                                        .padding(16.dp)
                                ) {
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier,
                                    ) {
                                        Text("Categories", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium)
                                        Icon(
                                            painter = if (isCategoriesVisible) painterResource(id = R.drawable.ic_baseline_keyboard_arrow_up_24) else painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
                                            contentDescription = ""
                                        )
                                    }
                                    AnimatedVisibility(visible = isCategoriesVisible) {
                                        Column {
                                            selectedMap?.groups?.forEach { group ->
                                                Row(
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                                                            val list = selectedCats.toMutableList()

                                                            group?.categories?.forEach { category ->
                                                                if (selectedCats.contains(category?.id)) {
                                                                    category?.id?.let {
                                                                        list.remove(it)
                                                                    }
                                                                } else {
                                                                    category?.id?.let {
                                                                        list.add(it)
                                                                    }
                                                                }
                                                            }

                                                            scope.launch {
                                                                UserSettingsModel.mapMarkerCategories.update(list.toSet())
                                                            }
                                                            updateMarkers(list)
                                                        }
                                                        .padding(top = 16.dp, bottom = 12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = group?.title ?: "Group",
                                                        style = MaterialTheme.typography.subtitle2,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }

                                                val groupCategories = group?.categories

                                                FlowRow(crossAxisSpacing = 8.dp, mainAxisSpacing = 0.dp) {
                                                    groupCategories?.forEach { category ->
                                                        Chip(
                                                            string = category?.title ?: "",
                                                            selected = selectedCats.contains(category?.id)
                                                        ) {
                                                            val list = selectedCats.toMutableList()
                                                            if (selectedCats.contains(category?.id)) {
                                                                category?.id?.let {
                                                                    list.remove(it)
                                                                }
                                                            } else {
                                                                category?.id?.let {
                                                                    list.add(it)
                                                                }
                                                            }

                                                            scope.launch {
                                                                UserSettingsModel.mapMarkerCategories.update(list.toSet())
                                                            }
                                                            updateMarkers(list)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                /*Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFF282828))
                                        .clickable {
                                            isSettingsVisible = !isSettingsVisible
                                        }
                                        .padding(16.dp)
                                ) {
                                    Row (
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier,
                                    ) {
                                        Text("Settings", modifier = Modifier.weight(1f), style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.Medium)
                                        Icon(
                                            painter = if (isSettingsVisible) painterResource(id = R.drawable.ic_baseline_keyboard_arrow_up_24) else painterResource(id = R.drawable.ic_baseline_keyboard_arrow_down_24),
                                            contentDescription = ""
                                        )
                                    }
                                    AnimatedVisibility(visible = isSettingsVisible) {
                                        Column {

                                        }
                                    }
                                }*/
                            }
                        },
                        frontLayerContent = {
                            BottomSheetScaffold(
                                scaffoldState = bottomSheetScaffoldState,
                                sheetPeekHeight = 0.dp,
                                sheetElevation = 5.dp,
                                sheetContent = {
                                    selectedMarker?.let {
                                        if (it.tag is MapInteractive.Location) {
                                            val location = it.tag as MapInteractive.Location
                                            var quest by remember {
                                                mutableStateOf<QuestExtra.QuestExtraItem?>(null)
                                            }
                                            if (location.category_id == 955) {
                                                try {
                                                    val questTitle = location.description?.split("**Quest:** ")?.get(1)?.substringBefore("\n")?.trim()
                                                    questTitle?.let { title ->
                                                        quest = questsExtra?.find { it.title.equals(title, true) }
                                                    }
                                                } catch (e: Exception) {
                                                    quest = null
                                                }

                                            } else {
                                                quest = null
                                            }
                                            Column(
                                                Modifier.padding(16.dp)
                                            ) {
                                                Text(text = location.title ?: "", style = MaterialTheme.typography.h6)
                                                MarkdownText(markdown = location.getFormattedDescription(), style = MaterialTheme.typography.body2)
                                                if (quest != null) {
                                                    Row(
                                                        Modifier
                                                            .padding(start = 0.dp, top = 8.dp, bottom = 0.dp, end = 0.dp)
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(itemDefault)
                                                            .clickable {
                                                                openActivity(QuestDetailActivity::class.java) {
                                                                    putString("questID", quest!!.id.toString())
                                                                }
                                                            }
                                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                                            .height(IntrinsicSize.Min),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(painter = painterResource(id = R.drawable.ic_baseline_assignment_24), contentDescription = "", modifier = Modifier.size(20.dp))
                                                        Column(
                                                            Modifier
                                                                .padding(start = 16.dp)
                                                                .weight(1f),
                                                            verticalArrangement = Arrangement.Center
                                                        ) {
                                                            Text(
                                                                text = quest!!.title ?: "",
                                                                style = MaterialTheme.typography.h6,
                                                                fontSize = 15.sp
                                                            )
                                                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                                                Text(
                                                                    text = "Unlocks at Level ${quest!!.require?.level}",
                                                                    style = MaterialTheme.typography.caption,
                                                                    fontSize = 10.sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                                if (!location.media.isNullOrEmpty()) {
                                                    Row(
                                                        Modifier
                                                            .horizontalScroll(rememberScrollState())
                                                            .padding(top = 16.dp)
                                                            .fillMaxWidth()
                                                    ) {
                                                        location.media?.forEach { media ->
                                                            Image(
                                                                painter = rememberImagePainter(media?.url),
                                                                contentDescription = "",
                                                                modifier = Modifier
                                                                    .height(100.dp)
                                                                    .width(150.dp)
                                                                    .padding(end = 8.dp)
                                                                    .border(1.dp, BorderColor)
                                                                    .clickable {
                                                                        StfalconImageViewer
                                                                            .Builder(
                                                                                this@MapsActivity,
                                                                                listOf(media?.url)
                                                                            ) { view, image ->
                                                                                Glide
                                                                                    .with(view)
                                                                                    .load(image)
                                                                                    .into(view)
                                                                            }
                                                                            .withHiddenStatusBar(false)
                                                                            .show()
                                                                    },
                                                                contentScale = ContentScale.FillHeight
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                sheetBackgroundColor = Color(0xFE1F1F1F)
                            ) { paddingValues ->
                                Box {
                                    selectedMap?.let {
                                        AndroidView(factory = {
                                            mapView
                                        }, modifier = Modifier.padding(paddingValues)) {
                                            CoroutineScope(Dispatchers.Main).launch {
                                                val map = mapView.awaitMap()
                                                setupMap(map, selectedMap!!)

                                                map.setOnMarkerClickListener { marker ->
                                                    selectedMarker = marker
                                                    scope.launch {
                                                        bottomSheetScaffoldState.bottomSheetState.expand()
                                                    }
                                                    false
                                                }

                                                map.setOnMapClickListener {
                                                    scope.launch {
                                                        try {
                                                            bottomSheetScaffoldState.bottomSheetState.collapse()
                                                        } catch (e: Exception) {

                                                        }
                                                    }

                                                    Timber.d(it.toString())

                                                    //TODO ADD MAP MEASURING

                                                    /* val first = selectedPoints?.first
                                                     val second = selectedPoints?.second

                                                     if (first == null) {
                                                         selectedPoints = Pair(it, null)
                                                     }

                                                     if (first != null && second == null) {
                                                         selectedPoints = Pair(first, it)
                                                         val distance =
                                                             SphericalUtil.computeDistanceBetween(selectedPoints!!.first, selectedPoints!!.second)
                                                         Toast.makeText(
                                                             this@MapsActivity,
                                                             "${distance.div(selectedMap?.distanceToolConfig?.scale ?: 1.0).roundToInt()} Meters",
                                                             Toast.LENGTH_SHORT
                                                         ).show()
                                                     }

                                                     if (first != null && second != null) {
                                                         selectedPoints = null
                                                         selectedPoints = Pair(it, null)
                                                     }*/
                                                }

                                                setupMarkers(selectedMap, map, selectedCats.toMutableList())
                                            }
                                        }
                                    }
                                    Column(
                                        Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(horizontal = 16.dp, vertical = 8.dp)
                                    ) {
                                        FloatingActionButton(
                                            onClick = {
                                                val title = Map.values().map { it.mapName }

                                                MaterialDialog(this@MapsActivity).show {
                                                    title(text = "Choose Map")
                                                    listItemsSingleChoice(
                                                        items = title,
                                                        initialSelection = title.map { it.lowercase() }.indexOf(selectedMapText)
                                                    ) { _, _, text ->
                                                        mapViewModel.setMap(text.toString().lowercase(), this@MapsActivity)
                                                    }
                                                }
                                            },
                                            backgroundColor = DarkPrimary,
                                            modifier = Modifier
                                                .padding(vertical = 8.dp)
                                                .size(40.dp),
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_baseline_map_24),
                                                contentDescription = "Choose Map",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        if (isDebug())
                                            FloatingActionButton(
                                                onClick = {
                                                    isDistanceToolActive = !isDistanceToolActive
                                                },
                                                backgroundColor = DarkPrimary,
                                                modifier = Modifier
                                                    .padding(vertical = 8.dp)
                                                    .size(40.dp),
                                            ) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.icons8_ruler_96),
                                                    contentDescription = "Distance",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                    }
                                }
                            }
                        },
                        snackbarHost = {
                            SnackbarHost(it) { data ->
                                Snackbar(
                                    backgroundColor = Green500,
                                    snackbarData = data
                                )
                            }
                        }
                    )

                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        onClick = {
                            scope.launch {
                                if (backdropScaffoldState.isConcealed) backdropScaffoldState.reveal() else backdropScaffoldState.conceal()
                            }
                        },
                        elevation = FloatingActionButtonDefaults.elevation(4.dp)
                    ) {
                        AnimatedContent(targetState = backdropScaffoldState) {
                            when {
                                it.isRevealed -> {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_close_24),
                                        contentDescription = "Filter",
                                        tint = Color.Black
                                    )
                                }
                                it.isConcealed -> {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_baseline_filter_alt_24),
                                        contentDescription = "Filter",
                                        tint = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    private fun setupMap(map: GoogleMap, selectedMap: MapInteractive) {
        this.map = map
        map.clear()
        map.mapType = GoogleMap.MAP_TYPE_NONE

        val tileProvider: TileProvider = object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                val normalizedCords = getNormalizedCord(x, y, zoom, selectedMap) ?: return null
                val url = "${selectedMap.map?.url}${selectedMap.getFirstMap()?.path}/${zoom}/${normalizedCords.first}/${normalizedCords.second}.png"
                //Timber.d(url)

                return try {
                    URL(url)
                } catch (e: MalformedURLException) {
                    throw AssertionError(e)
                }
            }
        }

        map.addTileOverlay(TileOverlayOptions().tileProvider(tileProvider).zIndex(2f))

        map.setMaxZoomPreference(selectedMap.getFirstMap()?.max_zoom?.toFloat() ?: 15f)
        map.setMinZoomPreference(selectedMap.getFirstMap()?.min_zoom?.toFloat() ?: 8f)

        map.setInfoWindowAdapter(null)

        map.moveCamera(CameraUpdateFactory.newLatLng(LatLng(0.600058195510644, -0.6989045441150665)))

        val one = LatLng(1.5, -1.5)
        val two = LatLng(0.0, 0.0)

        val builder = LatLngBounds.Builder()
        builder.include(one)
        builder.include(two)

        val bounds = builder.build()
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels

        map.setLatLngBoundsForCameraTarget(bounds)
        //map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, 0))
        map.moveCamera(CameraUpdateFactory.zoomTo(selectedMap.mapConfig?.initial_zoom?.toFloat() ?: 9f))

        val polygonOptions = PolygonOptions()
            .add(
                LatLng(5.0, -5.0),
                LatLng(5.0, 5.0),
                LatLng(0.0, 5.0),
                LatLng(-5.0, -5.0),
            ).zIndex(1f).fillColor(android.graphics.Color.BLACK)

        map.addPolygon(polygonOptions)

        map.uiSettings.isMapToolbarEnabled = false
        map.uiSettings.isZoomControlsEnabled = false
    }

    private fun setupMarkers(selectedMap: MapInteractive?, map: GoogleMap, selectedCategories: List<Int>?) {
        if (selectedMap == null) return
        Timber.d(selectedCategories.toString())
        selectedMap.locations?.forEach {
            addMarkerToMap(it, map)
        }

        updateMarkers(selectedCategories)
    }

    private fun addMarkerToMap(it: MapInteractive.Location?, map: GoogleMap) {
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
                949,
                946 -> R.drawable.icon_loose_loot
                943 -> R.drawable.icon_meds
                945 -> R.drawable.icon_money
                968 -> R.drawable.icon_pc
                1016 -> R.drawable.icon_food
                944 -> R.drawable.icon_safe
                970 -> R.drawable.icon_toolbox
                958 -> R.drawable.icon_weapons
                1015 -> R.drawable.icon_mods_2
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
                1012 -> R.drawable.icon_scav_spawn
                else -> R.drawable.icon_unknown
            }

            val bitmapDrawable = (ResourcesCompat.getDrawable(resources, icon, null) as BitmapDrawable).bitmap
            val markerIcon = Bitmap.createScaledBitmap(bitmapDrawable, 100, 100, false)

            val marker: Marker? = when {
                it.category_id != 952 -> {
                    map.addMarker(
                        MarkerOptions().position(LatLng(it.latitude!!.toDouble(), it.longitude!!.toDouble())).icon(
                            BitmapDescriptorFactory.fromBitmap(markerIcon)
                        ).zIndex(if (it.category_id == 972) 1f else 2f)
                    )
                }
                it.category_id == 952 -> {
                    map.addMarker(
                        MarkerOptions().position(LatLng(it.latitude!!.toDouble() - 0.0003, it.longitude!!.toDouble())).icon(
                            BitmapDescriptorFactory.fromBitmap(
                                textAsBitmap(
                                    it.title ?: "",
                                    26f,
                                    getColor(R.color.white),
                                    Color.Transparent.toArgb()
                                )
                            )
                        )
                    )
                }
                it.category_id == 954 -> {
                    map.addMarker(
                        MarkerOptions().position(LatLng(it.latitude!!.toDouble() - 0.0003, it.longitude!!.toDouble())).icon(
                            BitmapDescriptorFactory.fromBitmap(
                                textAsBitmap(
                                    it.title ?: "",
                                    36f,
                                    getColor(R.color.white),
                                    Green500.toArgb()
                                )
                            )
                        )
                    )
                }
                else -> {
                    map.addMarker(
                        MarkerOptions().position(LatLng(it.latitude!!.toDouble(), it.longitude!!.toDouble())).icon(
                            BitmapDescriptorFactory.fromBitmap(bitmapDrawable)
                        ).zIndex(if (it.category_id == 972) 1f else 2f)
                    )
                }
            }

            marker?.apply {
                tag = it
            }

            marker?.let {
                markers.add(it)
            }
        }
    }

    private fun updateMarkers(selectedCategories: List<Int>?) {
        markers.forEach {
            if (it.tag is MapInteractive.Location) {
                val location = it.tag as MapInteractive.Location
                it.isVisible = selectedCategories?.contains(location.category_id) != false
            }
        }

        val isMarkerOnMap = markers.find {
            if (it.tag is MapInteractive.Location) {
                val location = it.tag as MapInteractive.Location
                val catID = location.category_id
                selectedCategories?.contains(catID ?: 0)
            }
            false
        }
    }

    fun getNormalizedCord(x: Int, y: Int, zoom: Int, map: MapInteractive?): Pair<Int, Int>? {
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

    private fun textAsBitmap(text: String, textSize: Float, textColor: Int, outlineColor: Int): Bitmap {
        val benderFont = ResourcesCompat.getFont(this, R.font.bender_bold)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.typeface = benderFont
        paint.textSize = textSize
        paint.color = textColor
        paint.textAlign = Paint.Align.LEFT

        val stkPaint = Paint()
        stkPaint.style = Paint.Style.STROKE
        stkPaint.textSize = textSize
        stkPaint.typeface = benderFont
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

    @Composable
    private fun Chip(
        selected: Boolean = false,
        string: String,
        onClick: () -> Unit
    ) {
        Surface(
            color = when {
                selected -> Red400
                else -> Color(0xFF2F2F2F)
            },
            contentColor = when {
                selected -> Color.Black
                else -> Color.White
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(end = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    onClick()
                }
        ) {
            Text(
                text = string,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2,
                modifier = Modifier.padding(8.dp),
                fontWeight = FontWeight.Medium
            )
        }
    }
}