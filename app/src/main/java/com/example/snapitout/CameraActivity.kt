package com.example.snapitout

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
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

    private val cameraPermissions = arrayOf(Manifest.permission.CAMERA)
    private var imageCapture: ImageCapture? = null
    private var currentFilter: ColorMatrixColorFilter? = null
    private var capturedPhotosCount = 0

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

        // Request permissions
        if (isCameraPermissionGranted()) {
            startCamera()
        } else {
            permissionRequest.launch(cameraPermissions)
        }

        // Set filter buttons
        normalButton.setOnClickListener { applyFilter("normal") }
        bwButton.setOnClickListener { applyFilter("bw") }
        vintageButton.setOnClickListener { applyFilter("vintage") }
        oldPhotoButton.setOnClickListener { applyFilter("oldPhoto") }

        // Capture photo
        shutterButton.setOnClickListener {
            val imageCapture = imageCapture ?: return@setOnClickListener
            val photoFile = File.createTempFile("snap_", ".jpg", cacheDir)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                        runOnUiThread {
                            // Show the captured photo
                            filterOverlay.setImageBitmap(bitmap)
                            filterOverlay.visibility = View.VISIBLE
                            cameraPreview.visibility = View.INVISIBLE
                            chooseFilterText.text = "Apply filter to captured image"
                        }

                        if (capturedPhotosCount < 4) {
                            startCountdown()
                        } else {
                            Toast.makeText(this@CameraActivity, "4 photos captured", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(this@CameraActivity, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
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

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

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

    private fun applyFilter(filterType: String) {
        val matrix = ColorMatrix()

        when (filterType) {
            "normal" -> matrix.setSaturation(1f)
            "bw" -> matrix.setSaturation(0f)
            "vintage" -> {
                matrix.set(
                    floatArrayOf(
                        0.9f, 0.3f, 0.1f, 0f, 0f,
                        0.2f, 0.7f, 0.2f, 0f, 0f,
                        0.1f, 0.2f, 0.7f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            "oldPhoto" -> {
                matrix.set(
                    floatArrayOf(
                        1.3f, 0.3f, 0.2f, 0f, -50f,
                        0.2f, 1.2f, 0.2f, 0f, -30f,
                        0.1f, 0.1f, 1.1f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
        }

        // Apply the selected filter to the overlay (ImageView)
        currentFilter = ColorMatrixColorFilter(matrix)
        filterOverlay.colorFilter = currentFilter // Apply to the ImageView (filterOverlay)

        // Set the visibility of the filter overlay to make it visible
        filterOverlay.visibility = View.VISIBLE
    }

    private fun startCountdown() {
        runOnUiThread {
            filterOverlay.visibility = View.GONE
            cameraPreview.visibility = View.VISIBLE
            chooseFilterText.text = "Choose a filter"
        }

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
                    captureNextPhoto()
                }
            }
        }

        countdownText.postDelayed(countdownRunnable, 1000)
    }

    private fun captureNextPhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File.createTempFile("snap_", ".jpg", cacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    runOnUiThread {
                        capturedPhotosCount++
                        filterOverlay.setImageBitmap(bitmap)
                        filterOverlay.visibility = View.VISIBLE
                        cameraPreview.visibility = View.INVISIBLE
                        chooseFilterText.text = "Apply filter to captured image"
                    }

                    if (capturedPhotosCount < 4) {
                        startCountdown()
                    } else {
                        Toast.makeText(this@CameraActivity, "4 photos captured", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}
