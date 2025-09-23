package com.example.snapitout

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth

class UserActivity : AppCompatActivity() {

    // Declare all views
    private lateinit var profileImage: ImageView
    private lateinit var username: TextView
    private lateinit var email: TextView
    private lateinit var btnMyProfile: Button
    private lateinit var switchEventMode: Switch
    private lateinit var toolbar: MaterialToolbar
    private lateinit var homeIcon: ImageView
    private lateinit var albumIcon: ImageView
    private lateinit var moreText: TextView
    private lateinit var exclusiveFeaturesLayout: LinearLayout
    private lateinit var logOutBtn: Button  // Added LogOut button

    // SharedPreferences for saving switch state and user data
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userprofile)

        // Initialize views
        profileImage = findViewById(R.id.profileImage)
        username = findViewById(R.id.username)
        email = findViewById(R.id.email)
        btnMyProfile = findViewById(R.id.btnMyProfile)
        switchEventMode = findViewById(R.id.switch3)
        toolbar = findViewById(R.id.materialToolbar8)
        homeIcon = findViewById(R.id.home2btn)
        albumIcon = findViewById(R.id.imageView10)
        moreText = findViewById(R.id.moretxt)
        exclusiveFeaturesLayout = findViewById(R.id.exclusiveFeaturesLayout)
        logOutBtn = findViewById(R.id.logOutBtn) // Initialize LogOut button

        // Initialize SharedPreferences and FirebaseAuth
        sharedPreferences = getSharedPreferences("SnapItOutPrefs", MODE_PRIVATE)
        mAuth = FirebaseAuth.getInstance()

        // Load saved user data
        val usernameText = sharedPreferences.getString("username", "User")
        val emailText = sharedPreferences.getString("email", "noemail@snapitout.com")

        // Set username and email
        username.text = usernameText
        email.text = emailText

        // Load and apply Event Mode state
        val isEventModeOn = sharedPreferences.getBoolean("eventMode", false)
        applyEventModeState(isEventModeOn)

        // Toggle Event Mode logic
        switchEventMode.setOnCheckedChangeListener { _, isChecked ->
            applyEventModeState(isChecked)
            sharedPreferences.edit().putBoolean("eventMode", isChecked).apply()
        }

        // Exclusive Features Click
        exclusiveFeaturesLayout.setOnClickListener {
            if (switchEventMode.isChecked) {
                Toast.makeText(this, "Accessing Exclusive Features...", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ExclusiveActivity::class.java))
            } else {
                Toast.makeText(this, "Please turn on Event Mode to access Exclusive Features.", Toast.LENGTH_SHORT).show()
            }
        }

        // "My Profile" button (to be implemented)
        btnMyProfile.setOnClickListener {
            // TODO: Handle "My Profile" click
        }

        // Home icon click logic
        homeIcon.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        // Album icon click logic
        albumIcon.setOnClickListener {
            // TODO: Navigate to album/gallery
        }

        // LogOut button click logic
        logOutBtn.setOnClickListener {
            logOutUser()
        }
    }

    private fun applyEventModeState(isEnabled: Boolean) {
        switchEventMode.isChecked = isEnabled
        exclusiveFeaturesLayout.alpha = if (isEnabled) 1.0f else 0.4f
        exclusiveFeaturesLayout.isClickable = isEnabled
    }

    private fun logOutUser() {
        // Log out from Firebase
        mAuth.signOut()

        // Turn off Event Mode and update SharedPreferences
        sharedPreferences.edit()
            .putBoolean("eventMode", false)
            .apply()

        // Navigate to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Close the UserActivity
    }
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                )
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }
}
