package com.example.cameravision.di

import android.app.Application
import androidx.camera.core.*
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.lifecycle.ProcessCameraProvider
import com.example.cameravision.data.repository.CustomCameraRepoImpl
import com.example.cameravision.domin.repository.CustomCameraRepository
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun providesCameraSelector(): CameraSelector {
        return CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
    }


    @Provides
    @Singleton
    fun provideCameraProvider(application: Application): ProcessCameraProvider {
        return ProcessCameraProvider.getInstance(application).get()
    }


    @Provides
    @Singleton
    fun provideCameraPreview(): Preview {
        return Preview.Builder().build()
    }


    @Provides
    @Singleton
    fun provideImageCapture(): ImageCapture {
        return ImageCapture.Builder()
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()
    }

    @Provides
    @Singleton
    fun provideImageAnalysis(): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
            .build()
    }

    @Provides
    @Singleton
    @ExperimentalGetImage
    fun provideCustomCameraRepo(
        cameraProvider: ProcessCameraProvider,
        selector: CameraSelector,
        imageCapture: ImageCapture,
        imageAnalysis: ImageAnalysis,
        preview: Preview
    ): CustomCameraRepository {
        return CustomCameraRepoImpl(
            cameraProvider = cameraProvider,
            selector = selector,
            imageAnalysis = imageAnalysis,
            imageCapture = imageCapture,
            preview = preview
        )
    }



    @Provides
    @Singleton
    fun provideTextRecognizer(): TextRecognizer {
        val options = TextRecognizerOptions.Builder().build()
        return TextRecognition.getClient(options)
    }

}