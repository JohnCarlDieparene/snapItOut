package com.example.snapitout

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.io.FileOutputStream

class CameraActivity : AppCompatActivity() {

    private lateinit var cameraPreview: PreviewView
    private lateinit var filterOverlay: ImageView
    private lateinit var shutterButton: ImageView
    private lateinit var normalButton: Button
    private lateinit var bwButton: Button
    private lateinit var vintageButton: Button
    private lateinit var oldPhotoButton: Button
    private lateinit var chooseFilterText: TextView
    private lateinit var countdownText: TextView
    private lateinit var homeButton: ImageView

    private val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
    private var imageCapture: ImageCapture? = null
    private var capturedPhotosCount = 0
    private val capturedPhotoPaths = mutableListOf<String>()
    private var selectedFilter: String = "normal"
    private var lensFacing = CameraSelector.LENS_FACING_BACK

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.CAMERA, false)) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        // Initialize Views
        cameraPreview = findViewById(R.id.cameraPreview)
        filterOverlay = findViewById(R.id.filterOverlay)
        shutterButton = findViewById(R.id.shutterButton)
        normalButton = findViewById(R.id.normalButton)
        bwButton = findViewById(R.id.bwButton)
        vintageButton = findViewById(R.id.vintageButton)
        oldPhotoButton = findViewById(R.id.oldPhotoButton)
        chooseFilterText = findViewById(R.id.chooseFilterText)
        countdownText = findViewById(R.id.countdownText)
        homeButton = findViewById(R.id.homeButton)

        homeButton.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        if (isCameraPermissionGranted()) {
            startCamera()
        } else {
            permissionRequest.launch(cameraPermissions)
        }

        // Double tap to toggle front/back camera
        cameraPreview.setOnTouchListener(object : View.OnTouchListener {
            private var lastTapTime = 0L
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastTapTime < 300) {
                        toggleCamera()
                    }
                    lastTapTime = currentTime
                }
                return true
            }
        })

        // Apply filter button logic
        normalButton.setOnClickListener { applyFilter("normal") }
        bwButton.setOnClickListener { applyFilter("bw") }
        vintageButton.setOnClickListener { applyFilter("vintage") }
        oldPhotoButton.setOnClickListener { applyFilter("oldPhoto") }

        shutterButton.setOnClickListener {
            capturePhoto()
        }
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(cameraPreview.surfaceProvider)

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun toggleCamera() {
        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
        startCamera()
    }

    private fun applyFilter(filterType: String) {
        selectedFilter = filterType

        val colorMatrix = ColorMatrix()
        when (filterType) {
            "normal" -> {
                filterOverlay.colorFilter = null
                filterOverlay.setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
            "bw" -> {
                colorMatrix.setSaturation(0f)
                filterOverlay.colorFilter = ColorMatrixColorFilter(colorMatrix)
            }
            "vintage" -> {
                colorMatrix.setScale(1f, 0.9f, 0.7f, 1f)
                filterOverlay.colorFilter = ColorMatrixColorFilter(colorMatrix)
            }
            "oldPhoto" -> {
                colorMatrix.set(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                filterOverlay.colorFilter = ColorMatrixColorFilter(colorMatrix)
            }
        }
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File.createTempFile("snap_", ".jpg", cacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // Apply mirror effect to the captured photo
                    mirrorImage(photoFile)
                    capturedPhotoPaths.add(photoFile.absolutePath)
                    capturedPhotosCount++
                    if (capturedPhotosCount < 4) {
                        startCountdown()
                    } else {
                        Toast.makeText(this@CameraActivity, "4 photos captured", Toast.LENGTH_SHORT).show()
                        goToEditingActivity()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun mirrorImage(photoFile: File) {
        try {
            // Open the image file
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)

            // Create a matrix for the mirror effect
            val matrix = android.graphics.Matrix()
            matrix.preScale(-1f, 1f)  // Flip horizontally

            // Create a new mirrored bitmap using the matrix
            val mirroredBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            // Save the mirrored image to the file
            val outputStream = FileOutputStream(photoFile)
            mirroredBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            // Recycle the original and mirrored bitmaps to free memory
            bitmap.recycle()
            mirroredBitmap.recycle()

        } catch (e: Exception) {
            Toast.makeText(this@CameraActivity, "Error applying mirror effect: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCountdown() {
        runOnUiThread {
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
    }

    private fun goToEditingActivity() {
        val intent = Intent(this, EditingActivity::class.java)
        intent.putStringArrayListExtra("photoPaths", ArrayList(capturedPhotoPaths))
        intent.putExtra("selectedFilter", selectedFilter)
        startActivity(intent)
        finish()
    }
}
