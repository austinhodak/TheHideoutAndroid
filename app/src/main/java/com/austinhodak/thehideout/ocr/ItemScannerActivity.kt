package com.austinhodak.thehideout.ocr

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.austinhodak.tarkovapi.FleaVisiblePrice
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.tarkovapi.room.models.Quest
import com.austinhodak.tarkovapi.utils.asCurrency
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.AmmoCard
import com.austinhodak.thehideout.compose.components.*
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.firebase.User
import com.austinhodak.thehideout.ocr.viewmodels.ItemScannerViewModel
import com.austinhodak.thehideout.utils.*
import com.fondesa.kpermissions.coroutines.sendSuspend
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.michaelflisar.materialpreferences.core.initialisation.SettingSetup.context
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.Executor
import javax.inject.Inject

@AndroidEntryPoint
class ItemScannerActivity : AppCompatActivity() {

    @Inject
    lateinit var tarkovRepo: TarkovRepo

    private val viewModel: ItemScannerViewModel by viewModels()

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val result = permissionsBuilder(Manifest.permission.CAMERA).build().sendSuspend()
            // Handle the result.
        }

        setContent {
            HideoutTheme {
                val scannedItems by viewModel.scannedItems.observeAsState()
                val quests by tarkovRepo.getAllQuests().collectAsState(initial = emptyList())

                val executor = remember(context) { ContextCompat.getMainExecutor(context) }
                val priceDisplay = UserSettingsModel.fleaVisiblePrice.value

                val isScanning by viewModel.isScanning.observeAsState(true)
                val userData by viewModel.userData.observeAsState()

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = "Item Scanner")
                                    Badge(
                                        Modifier.padding(start = 8.dp),
                                        backgroundColor = Red400
                                    ) {
                                        Text(text = "ALPHA")
                                    }
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    onBackPressed()
                                }) {
                                    Icon(Icons.Filled.ArrowBack, contentDescription = null)
                                }
                            },
                            elevation = 5.dp,
                            backgroundColor = if (isSystemInDarkTheme()) Color(0xFE1F1F1F) else MaterialTheme.colors.primary,
                            actions = {
                                IconButton(onClick = {
                                    viewModel.clearAll()
                                }) {
                                    Icon(painterResource(id = R.drawable.ic_baseline_clear_all_24), contentDescription = null, tint = White)
                                }
                            },
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            viewModel.setScan(!isScanning)
                        }) {
                            if (isScanning) {
                                Icon(painterResource(id = R.drawable.ic_baseline_pause_24), contentDescription = "", tint = Color.Black)
                            } else {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "", tint = Color.Black)
                            }
                        }
                    }
                ) {
                    Column(
                        Modifier.fillMaxSize()
                    ) {
                        Box {
                            MLCameraView(
                                modifier = Modifier
                                    .height(300.dp)
                                    .fillMaxWidth()
                                    .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                context = context,
                                executor = executor
                            )
                            if (!isScanning) {
                                Surface(
                                    color = DarkGrey,
                                    modifier = Modifier
                                        .height(300.dp)
                                        .fillMaxWidth()
                                        .padding(start = 8.dp, end = 8.dp, top = 8.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                ) {
                                    EmptyText(text = "Paused")
                                }
                            }
                        }

                        if (scannedItems.isNullOrEmpty() && isScanning) {
                            EmptyText(text = "Scanning...")
                        } else if (isScanning) {
                            LazyColumn(
                                contentPadding = PaddingValues(vertical = 4.dp),
                                horizontalAlignment = CenterHorizontally,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            ) {
                                items(items = scannedItems?.values?.toList() ?: emptyList()) {
                                    if (it is Item) {
                                        FleaItemScanner(item = it, priceDisplay = priceDisplay, quests, userData) {
                                            openFleaDetail(it)
                                        }
                                    }
                                    if (it is Ammo) {
                                        AmmoCard(ammo = it, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                            openAmmunitionDetail(it.id)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("CheckResult")
    @OptIn(ExperimentalFoundationApi::class)
    @ExperimentalCoilApi
    @ExperimentalMaterialApi
    @Composable
    fun FleaItemScanner(
        item: Item,
        priceDisplay: FleaVisiblePrice,
        quests: List<Quest>,
        userData: User?,
        onClick: (String) -> Unit
    ) {
        val questsItemNeeded = quests.filterNot {
            userData?.isQuestCompleted(it) == true
        }.sumOf { quest ->
            quest.objective?.sumOf { obj ->
                if (obj.targetItem?.id == item.id || obj.target?.contains(item.id) == true) {
                    obj.number ?: 0
                } else 0
            } ?: 0
        }

        val context = LocalContext.current

        val color = when (item.BackgroundColor) {
            "blue" -> itemBlue
            "grey" -> itemGrey
            "red" -> itemRed
            "orange" -> itemOrange
            "default" -> itemDefault
            "violet" -> itemViolet
            "yellow" -> itemYellow
            "green" -> itemGreen
            "black" -> itemBlack
            else -> itemDefault
        }

        Card(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .combinedClickable(
                    onClick = {
                        onClick(item.id)
                    },
                    onLongClick = {
                        context.showDialog(
                            Pair("Wiki Page") {
                                item.pricing?.wikiLink?.openWithCustomTab(context)
                            },
                            Pair("Add to Needed Items") {
                                item.pricing?.addToNeededItemsDialog(context)
                            },
                            Pair("Add Price Alert") {
                                item.pricing?.addPriceAlertDialog(context)
                            },
                            Pair("Add to Cart") {
                                item.pricing?.addToCartDialog(context)
                            },
                        )
                    }
                ),
            backgroundColor = Color(0xFE1F1F1F)
        ) {
            Column {
                Row(
                    Modifier
                        .padding(end = 16.dp)
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Rectangle(color = color, modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 16.dp))
                    Image(
                        rememberImagePainter(item.pricing?.getCleanIcon()),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .width(48.dp)
                            .height(48.dp)
                            .border((0.25).dp, color = BorderColor)

                    )
                    Column(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.pricing?.name ?: "",
                            style = MaterialTheme.typography.h6,
                            fontSize = 15.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (questsItemNeeded > 0) {
                                Icon(painter = painterResource(id = R.drawable.ic_baseline_assignment_24), contentDescription = "", Modifier.size(24.dp).padding(end = 8.dp))
                            }
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                val text = if (item.pricing?.noFlea == false) {
                                    item.getUpdatedTime()
                                } else {
                                    "${item.getUpdatedTime()} • Not on Flea"
                                }
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.caption,
                                    fontSize = 10.sp,
                                )
                            }
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        val price = when (priceDisplay) {
                            FleaVisiblePrice.DEFAULT -> item.getPrice()
                            FleaVisiblePrice.AVG -> item.pricing?.avg24hPrice
                            FleaVisiblePrice.HIGH -> item.pricing?.high24hPrice
                            FleaVisiblePrice.LOW -> item.pricing?.low24hPrice
                            FleaVisiblePrice.LAST -> item.pricing?.lastLowPrice
                        }

                        Text(
                            text = price?.asCurrency() ?: "",
                            style = MaterialTheme.typography.h6,
                            fontSize = 15.sp
                        )
                        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = "${item.getPricePerSlot(price ?: 0).asCurrency()}/slot",
                                style = MaterialTheme.typography.caption,
                                fontSize = 10.sp
                            )
                        }
                        /*CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                            Text(
                                text = "${item.pricing?.changeLast48h}%",
                                style = MaterialTheme.typography.caption,
                                color = if (item.pricing?.changeLast48h ?: 0.0 > 0.0) Green500 else if (item.pricing?.changeLast48h ?: 0.0 < 0.0) Red500 else Color.Unspecified,
                                fontSize = 10.sp
                            )
                        }*/
                        TraderSmall(item = item.pricing)
                    }
                }
            }
        }
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Composable
    fun MLCameraView(
        modifier: Modifier,
        executor: Executor,
        context: Context
    ) {
        val previewCameraView = remember { PreviewView(context) }
        val cameraProviderFuture =
            remember(context) { ProcessCameraProvider.getInstance(context) }
        val cameraProvider = remember(cameraProviderFuture) { cameraProviderFuture.get() }
        var cameraSelector: CameraSelector? by remember { mutableStateOf(null) }
        val lifecycleOwner = LocalLifecycleOwner.current

        val isScanning by viewModel.isScanning.observeAsState(true)

        var useCaseGroup: UseCaseGroup? = null

        AndroidView(
            modifier = modifier,
            factory = {
                cameraProviderFuture.addListener(
                    {
                        cameraSelector = CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()

                        val prev = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewCameraView.surfaceProvider)
                        }

                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .setTargetAspectRatio(RATIO_4_3)
                            .build()
                            .also {
                                it.setAnalyzer(
                                    executor
                                ) {
                                    val mediaImage = it.image
                                    if (mediaImage != null) {
                                        val image = InputImage.fromMediaImage(mediaImage, it.imageInfo.rotationDegrees)
                                        recognizer.process(image)
                                            .addOnSuccessListener { visionText ->
                                                viewModel.processText(visionText)
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    delay(500)
                                                    it.close()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                e.printStackTrace()
                                            }

                                    }
                                }
                            }

                        useCaseGroup = UseCaseGroup.Builder()
                            .setViewPort(previewCameraView.viewPort!!)
                            .addUseCase(prev)
                            .addUseCase(imageAnalyzer)
                            .build()

                        cameraProvider.unbindAll()

                        Timber.d(isScanning.toString())

                        //if (isScanning)
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector as CameraSelector,
                            useCaseGroup!!
                        )
                    }, executor
                )
                previewCameraView
            }
        )
    }
}