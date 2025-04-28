package com.example.snapitout

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity : AppCompatActivity() {

    private lateinit var emailSignUp: EditText
    private lateinit var passwordSignUp: EditText
    private lateinit var signUpBtn: Button
    private lateinit var goToLogin: TextView
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        emailSignUp = findViewById(R.id.emailSignUp)
        passwordSignUp = findViewById(R.id.passwordSignUp)
        signUpBtn = findViewById(R.id.signUpBtn)
        goToLogin = findViewById(R.id.goToLogin)
        mAuth = FirebaseAuth.getInstance()

        signUpBtn.setOnClickListener {
            val email = emailSignUp.text.toString()
            val password = passwordSignUp.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your name, email, and password.", Toast.LENGTH_SHORT).show()
            } else {
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Account Created!", Toast.LENGTH_SHORT).show()
                            finish() // Go back to LoginActivity
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
