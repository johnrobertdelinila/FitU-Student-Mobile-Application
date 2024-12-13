package com.example.poseexercise.views.activity

import com.example.poseexercise.views.activity.AccountSetupActivity
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.poseexercise.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseUser
import com.google.android.material.card.MaterialCardView

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                // Check if email domain is valid
                if (/*account.email?.endsWith("@student.dmmmsu.edu.ph") == true*/ true) {
                    firebaseAuthWithGoogle(account.idToken!!)
                } else {
                    // Sign out from Google
                    googleSignInClient.signOut()
                    showInvalidEmailDialog()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showInvalidEmailDialog() {
        AlertDialog.Builder(this)
            .setTitle("Invalid Email")
            .setMessage("Please use your @student.dmmmsu.edu.ph email to log in.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .create()
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.LoginTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set up sign-in button
        findViewById<MaterialCardView>(R.id.googleSignInButton).setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        signInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    if (user != null) {
                        checkUserAccount(user)
                    } else {
                        showError("Authentication failed")
                    }
                } else {
                    // Sign in failed
                    showError("Authentication failed")
                }
            }
    }

    private fun checkUserAccount(user: FirebaseUser) {
        // Check if user document exists in Firestore
        db.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { document ->
                // Save login state
                PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putBoolean("is_logged_in", true)
                    .putString("user_email", user.email)
                    .apply()

                if (document.exists()) {
                    // User account exists, go to MainActivity
                    startMainActivity()
                } else {
                    // User account doesn't exist, go to AccountSetupActivity
                    startAccountSetup()
                }
                finish()
            }
            .addOnFailureListener { e ->
                showError("Failed to check account: ${e.message}")
            }
    }

    private fun startAccountSetup() {
        val intent = Intent(this, AccountSetupActivity::class.java)
        startActivity(intent)
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
} 