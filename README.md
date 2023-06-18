# TextRecognition_CameraX

The AppModule is a Dagger module annotated with @Module and @InstallIn(SingletonComponent::class). It provides various dependencies used in the camera vision feature.

The module provides the following dependencies:

providesCameraSelector(): Provides a CameraSelector instance configured to use the back-facing camera.
provideCameraProvider(application: Application): Provides a ProcessCameraProvider instance using the application context.
provideCameraPreview(): Provides a Preview instance for camera preview configuration.
provideImageCapture(): Provides an ImageCapture instance with auto flash mode and a target aspect ratio of 16:9.
provideImageAnalysis(): Provides an ImageAnalysis instance with a backpressure strategy of keeping only the latest frame.
provideCustomCameraRepo(...): Provides a CustomCameraRepository instance using the CustomCameraRepoImpl implementation. It requires the previously provided dependencies along with a cameraProvider, selector, imageCapture, imageAnalysis, and preview.
The provideTextRecognizer() function provides a TextRecognizer instance from ML Kit. It uses TextRecognizerOptions with default settings.

The CustomCameraRepoImpl class implements the CustomCameraRepository interface and is injected with the dependencies provided by the AppModule. It includes two main functions:

captureAndSaveImage(...): This function is responsible for capturing an image using the imageCapture and saving it to the device's storage. It also uses an ImageProcessor to process the saved image using ML Kit's text recognition.
showCameraPreview(...): This function sets up the camera preview using the previewView and binds the camera use case to the lifecycle of the lifecycleOwner. It unbinds any existing use cases and binds the preview, image capture, and image analysis use cases.

