package com.example.cameravision.domin.repository

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.example.cameravision.presentation.MainViewModel

interface CustomCameraRepository {

    suspend fun captureAndSaveImage(context: Context, viewModel: MainViewModel)

    suspend fun showCameraPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    )
}