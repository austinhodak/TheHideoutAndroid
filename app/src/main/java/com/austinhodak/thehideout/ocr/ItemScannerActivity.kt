package com.austinhodak.thehideout.ocr

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Rational
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.core.*
import androidx.camera.core.AspectRatio.RATIO_16_9
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.austinhodak.tarkovapi.UserSettingsModel
import com.austinhodak.tarkovapi.repository.TarkovRepo
import com.austinhodak.tarkovapi.room.models.Ammo
import com.austinhodak.tarkovapi.room.models.Item
import com.austinhodak.thehideout.R
import com.austinhodak.thehideout.ammunition.AmmoCard
import com.austinhodak.thehideout.compose.components.AmmoDetailToolbar
import com.austinhodak.thehideout.compose.components.EmptyText
import com.austinhodak.thehideout.compose.components.FleaItem
import com.austinhodak.thehideout.compose.theme.*
import com.austinhodak.thehideout.ocr.viewmodels.ItemScannerViewModel
import com.austinhodak.thehideout.utils.openAmmunitionDetail
import com.austinhodak.thehideout.utils.openFleaDetail
import com.fondesa.kpermissions.coroutines.sendSuspend
import com.fondesa.kpermissions.extension.permissionsBuilder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.google.protobuf.Empty
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

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

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

                val executor = remember(context) { ContextCompat.getMainExecutor(context) }
                val priceDisplay = UserSettingsModel.fleaVisiblePrice.value

                val isScanning by viewModel.isScanning.observeAsState(true)

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
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            ) {
                                items(items = scannedItems?.values?.toList() ?: emptyList()) {
                                    if (it is Item) {
                                        FleaItem(item = it, priceDisplay = priceDisplay) {
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
                                                    delay(1000)
                                                    it.close()
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                e.printStackTrace()
                                            }

                                    }
                                }
                            }

                        //val viewPort =  ViewPort.Builder(Rational(previewCameraView.width, previewCameraView.height), prev.targetRotation).build()

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