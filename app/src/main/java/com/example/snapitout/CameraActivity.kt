package com.example.snapitout

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.MotionEvent
import android.view.Surface
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraPreview: PreviewView
    private lateinit var shutterButton: ImageView
    private lateinit var countdownText: TextView
    private lateinit var homeButton: ImageView

    private val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
    private var imageCapture: ImageCapture? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private val capturedPhotoFiles = mutableListOf<File>()
    private lateinit var snapItOutFolder: File

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.CAMERA, false)) startCamera()
        else Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraPreview = findViewById(R.id.cameraPreview)
        shutterButton = findViewById(R.id.shutterButton)
        countdownText = findViewById(R.id.countdownText)
        homeButton = findViewById(R.id.homeButton)

        val picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        snapItOutFolder = File(picturesDir, "SnapItOut")
        if (!snapItOutFolder.exists()) snapItOutFolder.mkdirs()

        homeButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        if (isCameraPermissionGranted()) startCamera() else permissionRequest.launch(cameraPermissions)

        // Double-tap to toggle camera
        cameraPreview.setOnTouchListener(object : View.OnTouchListener {
            private var lastTapTime = 0L
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < 300) toggleCamera()
                    lastTapTime = currentTime
                }
                return true
            }
        })

        shutterButton.setOnClickListener { startCountdown() }
    }

    private fun isCameraPermissionGranted() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(cameraPreview.surfaceProvider) }

            // SAFELY get rotation
            val rotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                cameraPreview.display?.rotation ?: Surface.ROTATION_0
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay?.rotation ?: Surface.ROTATION_0
            }

            imageCapture = ImageCapture.Builder().setTargetRotation(rotation).build()
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
            CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
        startCamera()
    }

    private fun startCountdown() {
        countdownText.visibility = View.VISIBLE
        var countdown = 3
        countdownText.text = "$countdown"

        val countdownRunnable = object : Runnable {
            override fun run() {
                countdown--
                countdownText.text = "$countdown"
                if (countdown > 0) {
                    countdownText.postDelayed(this, 1000)
                } else {
                    countdownText.visibility = View.GONE
                    capturePhoto()
                }
            }
        }

        countdownText.postDelayed(countdownRunnable, 1000)
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(snapItOutFolder, "IMG_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val rotatedBitmap = rotateImageIfRequired(
                        photoFile.absolutePath,
                        lensFacing == CameraSelector.LENS_FACING_FRONT
                    )
                    FileOutputStream(photoFile).use { rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

                    capturedPhotoFiles.add(photoFile)

                    // Automatically go to EditingActivity after 4 photos
                    if (capturedPhotoFiles.size >= 4) {
                        goToEditingActivity()
                    } else {
                        // Reset rotation in case camera preview changed
                        startCamera()
                        startCountdown()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun goToEditingActivity() {
        val intent = Intent(this, EditingActivity::class.java)
        intent.putStringArrayListExtra(
            "photoPaths",
            ArrayList(capturedPhotoFiles.map { it.absolutePath })
        )
        intent.putExtra("isFrontCamera", lensFacing == CameraSelector.LENS_FACING_FRONT)
        startActivity(intent)
        finish()
    }

    private fun rotateImageIfRequired(filePath: String, mirror: Boolean = false): Bitmap {
        val bitmap = BitmapFactory.decodeFile(filePath)
        val exif = ExifInterface(filePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        if (mirror) {
            matrix.postScale(-1f, 1f)
            matrix.postTranslate(bitmap.width.toFloat(), 0f)
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}