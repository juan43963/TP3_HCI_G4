package com.grupo4.smarthome.ui.screens.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.grupo4.smarthome.R
import com.grupo4.smarthome.data.model.Device
import com.grupo4.smarthome.ui.components.ModalCloseButton
import com.grupo4.smarthome.ui.theme.BrandIndigoDark
import com.grupo4.smarthome.ui.theme.ErrorBg
import com.grupo4.smarthome.ui.theme.ErrorBorder
import com.grupo4.smarthome.ui.theme.ErrorRed
import com.grupo4.smarthome.ui.theme.SmartHomeIcons
import java.util.concurrent.Executors

// Pantalla de escaneo. Stateless respecto del ScanViewModel: recibe el uiState, la lista de devices
// del hogar para el match y los callbacks de detección/navegación. El permiso de cámara se maneja
// acá (concierne a Android/Compose) con el contrato RequestPermission.
@Composable
fun ScanScreen(
    uiState: ScanUiState,
    homeDevices: List<Device>,
    onCodeDetected: (String, List<Device>) -> Unit,
    onClose: () -> Unit,
    onRetry: () -> Unit,
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    var cameraError by rememberSaveable { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        permissionDenied = !granted
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        when {
            !hasPermission -> ScanInfoState(
                icon = SmartHomeIcons.Devices,
                title = stringResource(R.string.scan_permission_title),
                description = stringResource(R.string.scan_permission_description),
                primaryLabel = stringResource(R.string.scan_permission_grant),
                onPrimary = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                secondaryLabel = if (permissionDenied) {
                    stringResource(R.string.scan_permission_settings)
                } else {
                    null
                },
                onSecondary = { context.openAppSettings() },
                onClose = onClose,
            )

            cameraError -> ScanInfoState(
                icon = SmartHomeIcons.Info,
                title = stringResource(R.string.scan_camera_unavailable_title),
                description = stringResource(R.string.scan_camera_unavailable_description),
                primaryLabel = null,
                onPrimary = {},
                secondaryLabel = null,
                onSecondary = {},
                onClose = onClose,
            )

            else -> ScanCameraContent(
                uiState = uiState,
                homeDevices = homeDevices,
                onCodeDetected = onCodeDetected,
                onCameraError = { cameraError = true },
                onClose = onClose,
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun ScanCameraContent(
    uiState: ScanUiState,
    homeDevices: List<Device>,
    onCodeDetected: (String, List<Device>) -> Unit,
    onCameraError: () -> Unit,
    onClose: () -> Unit,
    onRetry: () -> Unit,
) {
    // rememberUpdatedState para que el analizador (creado una vez) lea siempre la última lista/callback.
    val latestDevices by rememberUpdatedState(homeDevices)
    val latestOnDetected by rememberUpdatedState(onCodeDetected)

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            onQrCode = { raw -> latestOnDetected(raw, latestDevices) },
            onCameraError = onCameraError,
            modifier = Modifier.fillMaxSize(),
        )

        ScanOverlay()

        // Scrim superior para legibilidad del header sobre el preview.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color.Black.copy(alpha = 0.32f)),
        )

        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp),
        ) {
            ModalCloseButton(onDismiss = onClose, tint = Color.White)
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.scan_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
        }

        InstructionPill(
            text = stringResource(R.string.scan_instruction),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
        )

        when (uiState) {
            ScanUiState.Processing -> ProcessingOverlay()
            ScanUiState.Invalid -> InvalidBanner(
                onRetry = onRetry,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 20.dp, vertical = 28.dp),
            )
            ScanUiState.Scanning -> Unit
        }
    }
}

@Composable
private fun ScanOverlay() {
    // Marco de escaneo centrado: cuadrado acotado (en tablet no ocupa toda la pantalla).
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val frameSize = minOf(maxWidth, maxHeight) * 0.66f
        // Borde del visor centrado, sobre el preview vivo (el centro queda limpio para enfocar el QR).
        Surface(
            modifier = Modifier.size(frameSize),
            shape = RoundedCornerShape(28.dp),
            color = Color.Transparent,
            border = BorderStroke(3.dp, Color.White.copy(alpha = 0.92f)),
            content = {},
        )
    }
}

@Composable
private fun InstructionPill(
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = Color.Black.copy(alpha = 0.55f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProcessingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CircularProgressIndicator(color = Color.White)
            Text(
                text = stringResource(R.string.scan_processing),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun InvalidBanner(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = ErrorBg,
        border = BorderStroke(1.dp, ErrorBorder),
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = SmartHomeIcons.Close,
                    contentDescription = null,
                    tint = ErrorRed,
                )
            }
            Text(
                text = stringResource(R.string.scan_invalid_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.scan_invalid_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandIndigoDark,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            ) {
                Text(text = stringResource(R.string.scan_retry), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ScanInfoState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    primaryLabel: String?,
    onPrimary: () -> Unit,
    secondaryLabel: String?,
    onSecondary: () -> Unit,
    onClose: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp),
        ) {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = SmartHomeIcons.Back,
                    contentDescription = stringResource(R.string.topbar_back_content_description),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 420.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(28.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp),
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (primaryLabel != null) {
                Button(
                    onClick = onPrimary,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandIndigoDark,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Text(text = primaryLabel, fontWeight = FontWeight.Bold)
                }
            }
            if (secondaryLabel != null) {
                OutlinedButton(
                    onClick = onSecondary,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(text = secondaryLabel, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    onQrCode: (String) -> Unit,
    onCameraError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val frameDescription = stringResource(R.string.scan_frame_content_description)
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            contentDescription = frameDescription
        }
    }
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build(),
        )
    }
    val latestOnQrCode by rememberUpdatedState(onQrCode)
    val latestOnError by rememberUpdatedState(onCameraError)

    androidx.compose.runtime.DisposableEffect(Unit) {
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            val provider = runCatching { future.get() }.getOrNull()
            if (provider == null) {
                latestOnError()
                return@addListener
            }
            val bound = runCatching {
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(
                    analysisExecutor,
                    QrCodeAnalyzer(barcodeScanner) { raw -> latestOnQrCode(raw) },
                )
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    analysis,
                )
            }.isSuccess
            if (!bound) latestOnError()
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
            analysisExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    AndroidView(factory = { previewView }, modifier = modifier)
}

private class QrCodeAnalyzer(
    private val scanner: BarcodeScanner,
    private val onQrCode: (String) -> Unit,
) : ImageAnalysis.Analyzer {
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                barcodes.firstNotNullOfOrNull { it.rawValue }?.let(onQrCode)
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}

private fun android.content.Context.openAppSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}
