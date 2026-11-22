package com.kaixgen.assistant.security

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.kaixgen.assistant.service.KaiEventBus
import com.kaixgen.assistant.service.KaiHardwareEvent
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class IntruderSelfieCapture(private val context: Context) {

    fun captureIfPermitted(
        lifecycleOwner: LifecycleOwner,
        reason: String = "3+ failed unlock attempts",
        onCaptured: (String) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            onError("Camera permission not granted")
            return
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            runCatching {
                val cameraProvider = cameraProviderFuture.get()
                val imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    imageCapture
                )

                val dir = File(context.filesDir, "intruders").apply { mkdirs() }
                val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val outputFile = File(dir, "intruder_$stamp.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

                imageCapture.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            cameraProvider.unbindAll()
                            KaiEventBus.emitEvent(
                                KaiHardwareEvent.IntruderCaptured(
                                    imagePath = outputFile.absolutePath,
                                    reason = reason,
                                    timestampMs = System.currentTimeMillis()
                                )
                            )
                            onCaptured(outputFile.absolutePath)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            cameraProvider.unbindAll()
                            onError(exception.message ?: "Capture failed")
                        }
                    }
                )
            }.onFailure { err ->
                onError(err.localizedMessage ?: "Camera initialization failed")
            }
        }, ContextCompat.getMainExecutor(context))
    }
}
