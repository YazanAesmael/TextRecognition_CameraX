package com.example

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.impl.utils.Exif
import com.example.cameravision.presentation.MainViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class ImageProcessor {
    @SuppressLint("RestrictedApi")
    internal fun processImage(context: Context, uri: Uri, viewModel: MainViewModel) {
        // define text recognizer
        val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // calculate rotation
        val inputStream = context.contentResolver.openInputStream(uri)
        val exif = inputStream?.let { Exif.createFromInputStream(it) }
        val rotation = exif?.rotation

        // translate Uri to Bitmap
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

        // process the image with ML Kit
        val inputImage = rotation?.let { InputImage.fromBitmap(bitmap, it) }
        inputImage?.let { image ->
            textRecognizer.process(image)
                // pass the recognized text to MainViewModel
                .addOnSuccessListener { visionText ->
                    viewModel.setRecognizedText(visionText.text)
                    viewModel.setIsCopy()
                    viewModel.setIsMinus()
                }
                // handle failure
                .addOnFailureListener { e ->
                    Log.e("CustomCameraRepoImpl", "Text recognition failed", e)
                }
        }
        inputStream?.close()
    }
}