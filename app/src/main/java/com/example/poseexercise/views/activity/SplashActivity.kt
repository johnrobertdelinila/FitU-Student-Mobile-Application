package com.example.poseexercise.views.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.poseexercise.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private val prefManager by lazy { PrefManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Using Handler to delay the transition
        Handler(Looper.getMainLooper()).postDelayed({
            checkFirstLaunch()
        }, 2000) // 2 seconds delay
    }

    private fun checkFirstLaunch() {
        if (prefManager.isFirstTimeLaunch()) {
            // First time launch - show onboarding
            prefManager.setFirstTimeLaunch(false)
            startActivity(Intent(this, OnboardingActivity::class.java))
            finish()
            return
        }

        // Check authentication status
        val currentUser = auth.currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Check if user has completed account setup
        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // User exists in Firestore, go to MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    // User exists in Auth but not in Firestore
                    startActivity(Intent(this, AccountSetupActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener { e ->
                // On Firestore error, log out user and go to login
                auth.signOut()
                Toast.makeText(
                    this,
                    "Session expired. Please login again.",
                    Toast.LENGTH_SHORT
                ).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
    }
}
