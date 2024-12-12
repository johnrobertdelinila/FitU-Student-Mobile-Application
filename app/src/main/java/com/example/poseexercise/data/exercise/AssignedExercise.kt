package com.example.poseexercise.data.exercise

import com.google.firebase.Timestamp

data class AssignedExercise(
    val id: String = "",
    val classRosterId: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val dueDate: String = "",
    val exerciseName: String = "",
    val instructorId: String = "",
    var instructorName: String = "",
    val repetitions: Int = 0,
    var isCompleted: Boolean = false,
    var attemptCount: Int = 0
) 