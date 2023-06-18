package com.example.cameravision.presentation

import android.content.Context
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cameravision.domin.repository.CustomCameraRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: CustomCameraRepository
): ViewModel() {

    private val _isCameraPermissionGranted = mutableStateOf(false)
    val isCameraPermissionGranted = MutableStateFlow(_isCameraPermissionGranted)
    private val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _isCopy = mutableStateOf(false)
    val isCopy = MutableStateFlow(_isCopy)

    private val _isMinus = mutableStateOf(false)
    val isMinus = MutableStateFlow(_isMinus)

    private val _recognizedText = mutableStateOf<String?>(null)
    val recognizedText = MutableStateFlow(_recognizedText)

    fun setRecognizedText(text: String?) {
        _recognizedText.value = text
    }

    fun setIsCopy() {
        _isCopy.value = true
    }

    fun setIsCopyNot() {
        _isCopy.value = false
    }

    fun setIsMinus() {
        _isMinus.value = true
    }

    fun setIsMinusNot() {
        _isMinus.value = false
    }

    fun showCameraPreview(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner
    ) {
        viewModelScope.launch {
            repo.showCameraPreview(
                previewView,
                lifecycleOwner
            )
        }
    }

    fun captureAndSave(context: Context) {
        viewModelScope.launch {
            repo.captureAndSaveImage(context, this@MainViewModel)
        }
    }

    private fun showMainScreen() {
        _isCameraPermissionGranted.value = true
    }

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirst()
    }

    fun onPermissionResult(
        permission: String,
        isGranted: Boolean
    ) {
        if (!isGranted) {
            visiblePermissionDialogQueue.add(permission)
        }
        if (isGranted) {
            showMainScreen()
        }
    }


}