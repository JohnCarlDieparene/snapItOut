package com.example.snapitout

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailSignUp: EditText
    private lateinit var passwordSignUp: EditText
    private lateinit var signUpBtn: Button
    private lateinit var goToLogin: TextView
    private lateinit var togglePasswordIcon: ImageView
    private lateinit var mAuth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        emailSignUp = findViewById(R.id.emailSignUp)
        passwordSignUp = findViewById(R.id.passwordSignUp)
        signUpBtn = findViewById(R.id.signUpBtn)
        goToLogin = findViewById(R.id.goToLogin)
        togglePasswordIcon = findViewById(R.id.togglePasswordVisibility) // Your eye icon ImageView
        mAuth = FirebaseAuth.getInstance()

        // Toggle password visibility
        togglePasswordIcon.setOnClickListener {
            if (isPasswordVisible) {
                passwordSignUp.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordIcon.setImageResource(R.drawable.ic_eye_closed)
            } else {
                passwordSignUp.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordIcon.setImageResource(R.drawable.ic_eye_open)
            }
            isPasswordVisible = !isPasswordVisible
            passwordSignUp.setSelection(passwordSignUp.text.length)
        }

        signUpBtn.setOnClickListener {
            val email = emailSignUp.text.toString()
            val password = passwordSignUp.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your email and password.", Toast.LENGTH_SHORT).show()
            } else {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

        goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
