package com.example.snapitout

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
    private val capturedPhotoPaths = mutableListOf<String>() // Store image paths

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
        // Logic for applying filters to the camera preview
        // This is for changing filter options but we won't display captured images in this activity.
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
                    capturedPhotoPaths.add(photoFile.absolutePath) // Save path

                    capturedPhotosCount++
                    if (capturedPhotosCount < 4) {
                        startCountdown()
                    } else {
                        Toast.makeText(this@CameraActivity, "4 photos captured", Toast.LENGTH_SHORT).show()
                        goToEditingActivity() // Go to editing activity after capturing 4 photos
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@CameraActivity, "Capture failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
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
                        capturePhoto() // Capture the next photo
                    }
                }
            }

            countdownText.postDelayed(countdownRunnable, 1000)
        }
    }

    private fun goToEditingActivity() {
        val intent = Intent(this, EditingActivity::class.java)
        intent.putStringArrayListExtra("photoPaths", ArrayList(capturedPhotoPaths))
        startActivity(intent)
        finish() // Close CameraActivity so user can't go back to it
    }
}
