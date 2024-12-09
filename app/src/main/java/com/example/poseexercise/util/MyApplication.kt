package com.example.poseexercise.util

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MyApplication : Application() {
    companion object {
        private lateinit var instance: MyApplication

        fun getInstance(): MyApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Enable Firestore logging for debugging
        FirebaseFirestore.setLoggingEnabled(true)
    }

    fun clearMemory() {
        // Clear any application-level caches or data
        // Add any app-wide cleanup code here
    }
}