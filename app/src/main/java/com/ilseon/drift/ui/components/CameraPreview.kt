package com.ilseon.drift.ui.components

import android.hardware.camera2.CameraCharacteristics
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.awaitCancellation
import java.util.concurrent.Executors

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onPulseDetected: (Long) -> Unit,
    onCameraReady: () -> Unit = {},
    onMeasurementFailed: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val currentOnPulseDetected = rememberUpdatedState(onPulseDetected)
    val currentOnCameraReady = rememberUpdatedState(onCameraReady)
    val currentOnMeasurementFailed = rememberUpdatedState(onMeasurementFailed)

    LaunchedEffect(Unit) {
        android.util.Log.d("CameraPreview", "Starting camera setup")
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()
        var camera: Camera? = null
        try {
            android.util.Log.d("CameraPreview", "Got camera provider")

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .addCameraFilter { cameraInfos ->
                    cameraInfos.maxByOrNull { cameraInfo ->
                        val characteristics = Camera2CameraInfo.from(cameraInfo)
                            .getCameraCharacteristic(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
                        (characteristics?.width ?: 0f) * (characteristics?.height ?: 0f)
                    }?.let { listOf(it) } ?: cameraInfos
                }
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    android.util.Log.d("CameraPreview", "Setting up analyzer")
                    it.setAnalyzer(
                        cameraExecutor,
                        PulseAnalyzer(
                            onPulseDetected = { timestamp ->
                                android.util.Log.d("CameraPreview", "Pulse callback: $timestamp")
                                currentOnPulseDetected.value(timestamp)
                            },
                            onMeasurementFailed = {
                                android.util.Log.d("CameraPreview", "Measurement failed")
                                currentOnMeasurementFailed.value()
                            }
                        )
                    )
                }

            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            camera.cameraControl.enableTorch(true)
            camera.cameraControl.setLinearZoom(0f)
            android.util.Log.d("CameraPreview", "Camera bound successfully")
            currentOnCameraReady.value()
            awaitCancellation()
        } catch (exc: Exception) {
            android.util.Log.e("CameraPreview", "Camera setup failed or cancelled", exc)
        } finally {
            android.util.Log.d("CameraPreview", "Cleaning up camera resources.")
            camera?.cameraControl?.enableTorch(false)
            cameraProvider.unbindAll()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}
