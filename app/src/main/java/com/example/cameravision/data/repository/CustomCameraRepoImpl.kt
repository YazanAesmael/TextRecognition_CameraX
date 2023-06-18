package com.example.cameravision.data.repository

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.ImageProcessor
import com.example.cameravision.domin.repository.CustomCameraRepository
import com.example.cameravision.presentation.MainViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@ExperimentalGetImage
@SuppressLint("RestrictedApi")
class CustomCameraRepoImpl @Inject constructor(
    private val cameraProvider: ProcessCameraProvider,
    private val selector: CameraSelector,
    private val preview :Preview,
    private val imageAnalysis: ImageAnalysis,
    private val imageCapture: ImageCapture
): CustomCameraRepository {

    // take & save picture, process with ML Kit
    override suspend fun captureAndSaveImage(
        context: Context,
        viewModel: MainViewModel
    ) {
        val name = SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss-SSS",
            Locale.ENGLISH
        ).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Doc-Scanner")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                context.contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                @RequiresApi(Build.VERSION_CODES.P)
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    outputFileResults.savedUri?.let {
                        ImageProcessor().processImage(context, it, viewModel)
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(context, "error message: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // camera view
    override suspend fun showCameraPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    ) {
        preview.setSurfaceProvider(previewView.surfaceProvider)
        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                selector,
                preview,
                imageCapture,
                imageAnalysis
            )
        }catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
}

