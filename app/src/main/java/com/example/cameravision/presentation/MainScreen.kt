package com.example.cameravision.presentation

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.ImageProcessor
import com.example.cameravision.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel()
) {

    // Permission Handling
    val permissionList = listOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val permissions = if (Build.VERSION.SDK_INT <= 28) permissionList else listOf(Manifest.permission.CAMERA)
    val permissionState = rememberMultiplePermissionsState(permissions)
    if (!permissionState.allPermissionsGranted){
        SideEffect {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    // Build
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configuration = LocalConfiguration.current
    val clipboardManager = LocalClipboardManager.current
    val screeHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    var previewView: PreviewView
    val scrollState = rememberScrollState()

    // Image Picker
    var pickedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    val activityResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        val pickedPhoto = uri ?: return@rememberLauncherForActivityResult
        pickedPhotoUri = pickedPhoto
    }

    // Share Intent
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }

    // Shared State Flow
    val isMinusVisible = viewModel.isMinus.collectAsState() // check again?
    val isCopyVisible = viewModel.isCopy.collectAsState()
    val recognizedText = viewModel.recognizedText.collectAsState()

    // border brush #1
    val borderColors = listOf(
        Color.Yellow,
        Color.Red,
        Color.Magenta.copy(0.6f)
    )
    val borderBrush = Brush.linearGradient(colors = borderColors)

    // background brush #1
    val bgColors = listOf(
        Color.Yellow.copy(0.6f),
        Color.Red.copy(0.6f),
        Color.Magenta.copy(0.6f)

        )
    val bgBrush = Brush.horizontalGradient (colors = bgColors)

    val isVisible = pickedPhotoUri != null

    var blurValue by remember {
        mutableStateOf(
            if (!isCopyVisible.value.value) 4.dp else 0.dp
        )
    }

    val blurIconValue = if (blurValue > 0.dp) R.drawable.view else R.drawable.hide

    val blur by animateDpAsState(targetValue = blurValue)

    Column(
        modifier = Modifier
            .zIndex(1f)
            .fillMaxSize()
            .background(Color(0xFF181616)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (permissionState.allPermissionsGranted) {
            Box(
                modifier = Modifier
                    .padding(top = 32.dp, bottom = 16.dp)
                    .height(screeHeight * 0.80f)
                    .width(screenWidth * 0.85f)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, borderBrush, RoundedCornerShape(20.dp))
            ) {

                // Utilities Column
                Column(
                    modifier = Modifier
                        .padding(top = 8.dp, end = 8.dp)
                        .background(bgBrush, RoundedCornerShape(14.dp), alpha = 0.4f)
                        .zIndex(1f)
                        .align(Alignment.TopEnd)
                        .drawBehind {
                            drawRoundRect(
                                color = Color.Black.copy(0.2f),
                                cornerRadius = CornerRadius(40f, 40f)
                            )
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility (isMinusVisible.value.value) {
                        IconButton(
                            onClick = {
                                viewModel.setRecognizedText(null)
                                pickedPhotoUri = null
                                viewModel.setIsCopyNot()
                                viewModel.setIsMinusNot()
                            },
                            modifier = Modifier
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_cancel_24),
                                contentDescription = "",
                                modifier = Modifier,
                                tint = Color.White.copy(0.8f)
                            )
                        }
                    }
                    AnimatedVisibility (isCopyVisible.value.value) {
                        if (!recognizedText.value.value.isNullOrBlank()){
                            Column {
                                IconButton(
                                    onClick = {
                                        val text = AnnotatedString(recognizedText.value.value!!)
                                        clipboardManager.setText(text).apply {
                                            Toast.makeText(context, "copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_content_copy_24),
                                        contentDescription = "copy to clipboard",
                                        tint = Color.White.copy(0.8f)
                                    )
                                }

                                IconButton(
                                    onClick = { shareText(recognizedText.value.value!!, launcher) },
                                    modifier = Modifier
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.outline_share_24),
                                        contentDescription = "share text",
                                        tint = Color.White.copy(0.8f)
                                    )
                                }

                                if (pickedPhotoUri != null) {
                                    IconButton(
                                        onClick = {
                                            blurValue = if (blurValue > 0.dp) 0.dp else 4.dp
                                        },
                                        modifier = Modifier
                                    ) {
                                        Icon(
                                            painter = painterResource(blurIconValue),
                                            contentDescription = "un-blur image",
                                            tint = Color.White.copy(0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Camera View
                if (!isVisible) {
                    AndroidView(
                        factory = {
                            previewView = PreviewView(it)
                            viewModel.showCameraPreview(previewView, lifecycleOwner)
                            previewView
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .blur(blur)
                    )
                }

                // Gallery Image View
                pickedPhotoUri?.let { uri ->

                    ImageProcessor().processImage(context, uri, viewModel)

                    val painter = rememberAsyncImagePainter(uri)

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)
                            .blur(blur),
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.Center),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // Display Text
                if (recognizedText.value.value != null) {
                    Box(
                        modifier = Modifier
                            .height(screeHeight * 0.80f)
                            .width(screenWidth * 0.85f)
                            .align(Alignment.Center)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .zIndex(2f)
                                .widthIn(max = screenWidth / 2 + 32.dp)
                                .background(bgBrush, RoundedCornerShape(14.dp), alpha = 0.4f)
                                .drawBehind {
                                    drawRoundRect(
                                        color = Color.Black.copy(0.3f),
                                        cornerRadius = CornerRadius(40f, 40f)
                                    )
                                }
                                .verticalScroll(scrollState)
                        ) {
                            SelectionContainer {
                                Text(
                                    text = recognizedText.value.value!!.ifBlank { "Couldn't find text!" },
                                    modifier = Modifier
                                        .padding(8.dp),
                                    fontWeight = FontWeight.Normal,
                                )
                            }
                        }
                    }
                }

            }
        }

        // Bottom App Bar
        Column(
            modifier = Modifier
                .height(screeHeight * 0.20f)
                .width(screenWidth),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pick or Scan Image",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Thin,
                )
            )

            Row(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(
                        bgBrush,
                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    ),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (permissionState.allPermissionsGranted) {
                            activityResultLauncher.launch("image/*")
                        }
                    },
                    modifier = Modifier
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.gallery),
                        contentDescription = "Gallery Button",
                        modifier = Modifier
                            .size(45.dp)
                    )
                }

                IconButton(
                    onClick = {
                        if (permissionState.allPermissionsGranted) {
                            viewModel.captureAndSave(context)
                        }
                    },
                    modifier = Modifier
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Camera Button",
                        modifier = Modifier
                            .size(45.dp)
                    )
                }
            }
        }
    }
}

private fun shareText(text: String, launcher: ActivityResultLauncher<Intent>) {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, text)
    val chooserIntent = Intent.createChooser(intent, "Share via")
    launcher.launch(chooserIntent)
}