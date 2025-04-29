package com.msa.core.ui.camera




import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * مدیریت دوربین و گرفتن عکس.
 */
class CameraHelper(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: androidx.camera.core.ImageCapture? = null

    /**
     * راه‌اندازی دوربین و پیش‌نمایش.
     */
    fun setupCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        isFrontCamera: Boolean = false,
        onCameraReady: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                val preview = androidx.camera.core.Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                imageCapture = androidx.camera.core.ImageCapture.Builder()
                    .setCaptureMode(androidx.camera.core.ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                val cameraSelector = if (isFrontCamera)
                    CameraSelector.DEFAULT_FRONT_CAMERA
                else
                    CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )

                onCameraReady()
            } catch (e: Exception) {
                onError(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * گرفتن عکس و ذخیره به فایل.
     */
    fun takePicture(onImageCaptured: (Uri) -> Unit, onError: (Exception) -> Unit) {
        val outputDirectory = context.getExternalFilesDir(null) ?: return
        val fileName = SimpleDateFormat(CameraConstants.IMAGE_FORMAT, Locale.US)
            .format(System.currentTimeMillis()) + CameraConstants.IMAGE_EXTENSION
        val file = File(outputDirectory, fileName)

        val outputOptions = androidx.camera.core.ImageCapture.OutputFileOptions.Builder(file).build()

        imageCapture?.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : androidx.camera.core.ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: androidx.camera.core.ImageCapture.OutputFileResults) {
                    onImageCaptured(Uri.fromFile(file))
                }

                override fun onError(exception: androidx.camera.core.ImageCaptureException) {
                    onError(exception)
                }
            }
        )
    }

    /**
     * ذخیره‌ی مستقیم یک Bitmap به عنوان فایل.
     */
    fun saveBitmapToFile(bitmap: Bitmap): File? {
        return try {
            val outputDirectory = context.getExternalFilesDir(null) ?: return null
            val fileName = SimpleDateFormat(CameraConstants.IMAGE_FORMAT, Locale.US)
                .format(System.currentTimeMillis()) + CameraConstants.IMAGE_EXTENSION
            val file = File(outputDirectory, fileName)

            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            }
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}



//private val cameraHelper: CameraHelper by inject()
//
//fun initCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
//    cameraHelper.setupCamera(
//        lifecycleOwner = lifecycleOwner,
//        previewView = previewView,
//        isFrontCamera = false,
//        onCameraReady = {
//            println("دوربین راه‌اندازی شد")
//        },
//        onError = {
//            it.printStackTrace()
//        }
//    )
//}
//
//fun captureImage() {
//    cameraHelper.takePicture(
//        onImageCaptured = { uri ->
//            println("تصویر ذخیره شد: $uri")
//        },
//        onError = {
//            it.printStackTrace()
//        }
//    )
//}
//
//
//@Composable
//fun CameraScreen() {
//    val cameraHelper: CameraHelper by inject()
//    val context = LocalContext.current
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val previewView = remember { PreviewView(context) }
//    var capturedUri by remember { mutableStateOf<Uri?>(null) }
//
//    LaunchedEffect(Unit) {
//        cameraHelper.setupCamera(
//            lifecycleOwner,
//            previewView,
//            isFrontCamera = false,
//            onCameraReady = { println("دوربین آماده است") },
//            onError = { it.printStackTrace() }
//        )
//    }
//
//    Column(modifier = Modifier.fillMaxSize()) {
//        AndroidView(factory = { previewView }, modifier = Modifier.weight(1f))
//
//        Button(
//            onClick = {
//                cameraHelper.takePicture(
//                    onImageCaptured = { uri ->
//                        capturedUri = uri
//                    },
//                    onError = { it.printStackTrace() }
//                )
//            },
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Text("گرفتن عکس")
//        }
//
//        capturedUri?.let { uri ->
//            AsyncImage(
//                model = uri,
//                contentDescription = "عکس گرفته‌شده",
//                modifier = Modifier
//                    .size(200.dp)
//                    .align(Alignment.CenterHorizontally)
//            )
//        }
//    }
//}