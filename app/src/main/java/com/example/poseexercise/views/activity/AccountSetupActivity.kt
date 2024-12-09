package com.example.poseexercise.views.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.poseexercise.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AccountSetupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var fitnessLevelSpinner: Spinner
    private lateinit var loadingDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_setup)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Initialize loading dialog
        initLoadingDialog()

        // Initialize Spinner
        fitnessLevelSpinner = findViewById(R.id.fitnessLevelSpinner)
        setupFitnessLevelSpinner()

        // Setup UI elements for collecting user information
        setupViews()
    }

    private fun initLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_loading, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        loadingDialog = builder.create()
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun setupFitnessLevelSpinner() {
        val fitnessLevels = arrayOf("Beginner", "Intermediate", "Advanced")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, fitnessLevels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        fitnessLevelSpinner.adapter = adapter
    }

    private fun setupViews() {
        // Get current user
        val user = auth.currentUser
        if (user == null) {
            // Handle error - user should be logged in at this point
            finish()
            return
        }

        // Handle save button click
        findViewById<Button>(R.id.saveButton).setOnClickListener {
            saveUserData(user)
        }
    }

    private fun saveUserData(user: FirebaseUser) {
        // Get user input from form
        val name = findViewById<EditText>(R.id.nameInput).text.toString()
        val age = findViewById<EditText>(R.id.ageInput).text.toString().toIntOrNull() ?: 0
        val weight = findViewById<EditText>(R.id.weightInput).text.toString().toFloatOrNull() ?: 0f
        val height = findViewById<EditText>(R.id.heightInput).text.toString().toFloatOrNull() ?: 0f
        val course = findViewById<EditText>(R.id.courseInput).text.toString()
        val yearLevel = findViewById<EditText>(R.id.yearLevelInput).text.toString().toIntOrNull() ?: 1
        val fitnessLevel = fitnessLevelSpinner.selectedItem.toString()

        // Validate inputs
        if (name.isEmpty() || course.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading dialog
        loadingDialog.show()

        // Create a flag to track if the operation timed out
        var isTimedOut = false

        // Add timeout handling
        Handler(Looper.getMainLooper()).postDelayed({
            if (loadingDialog.isShowing && !isTimedOut) {
                isTimedOut = true
                loadingDialog.dismiss()
                Toast.makeText(
                    this,
                    "Connection timeout. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }, 30000) // 30 seconds timeout

        // Create user document
        val userData = hashMapOf(
            "uid" to user.uid,
            "email" to user.email,
            "name" to name,
            "age" to age,
            "weight" to weight,
            "height" to height,
            "course" to course,
            "yearLevel" to yearLevel,
            "fitnessLevel" to fitnessLevel,
            "createdAt" to FieldValue.serverTimestamp()
        )

        // Save to Firestore
        db.collection("users")
            .document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                if (!isTimedOut) {  // Only proceed if we haven't timed out
                    loadingDialog.dismiss()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                if (!isTimedOut) {  // Only show error if we haven't timed out
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this,
                        "Failed to save account: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    override fun onDestroy() {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
        super.onDestroy()
    }
} 