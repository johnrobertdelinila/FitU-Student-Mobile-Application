package com.example.poseexercise.data.model

import com.google.firebase.Timestamp

data class PerformedExercise(
    val uid: String = "",
    val exercise_name: String = "",
    val duration: Long = 0,
    val time_and_date: Timestamp = Timestamp.now(),
    val repetition: Int = 0,
    val confidence: Float = 0f
) 