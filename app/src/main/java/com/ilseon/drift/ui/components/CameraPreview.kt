package com.ilseon.drift.ui.components

import androidx.camera.core.Camera
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

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onPulseDetected: (Long) -> Unit,
    onCameraReady: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val currentOnPulseDetected = rememberUpdatedState(onPulseDetected)
    val currentOnCameraReady = rememberUpdatedState(onCameraReady)

    LaunchedEffect(Unit) {
        android.util.Log.d("CameraPreview", "Starting camera setup")
        val cameraProvider = ProcessCameraProvider.getInstance(context).await()
        var camera: Camera? = null
        try {
            android.util.Log.d("CameraPreview", "Got camera provider")

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    android.util.Log.d("CameraPreview", "Setting up analyzer")
                    it.setAnalyzer(cameraExecutor, PulseAnalyzer { timestamp ->
                        android.util.Log.d("CameraPreview", "Pulse callback: $timestamp")
                        currentOnPulseDetected.value(timestamp)
                    })
                }

            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis
            )
            camera.cameraControl.enableTorch(true)
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
