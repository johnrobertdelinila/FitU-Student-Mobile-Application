package com.example.poseexercise.data.plan

import com.example.poseexercise.R

/**
Object containing constants related to exercise data
 **/
object Constants {

    // Function to get a list of predefined exercises with their details
    fun getExerciseList(): List<ExerciseType> {
        return listOf(
            ExerciseType(
                name = "Push-up",
                category = "Strength",
                caloriesPerRep = 2.5,
                image = R.drawable.push_up
            ),
            ExerciseType(
                name = "Squat",
                category = "Strength",
                caloriesPerRep = 3.0,
                image = R.drawable.squat
            ),
            ExerciseType(
                name = "Sit-up",
                category = "Strength",
                caloriesPerRep = 2.0,
                image = R.drawable.sit_ups
            ),
            ExerciseType(
                name = "Deadlift",
                category = "Strength",
                caloriesPerRep = 4.0,
                image = R.drawable.dead_lift
            ),
            ExerciseType(
                name = "Chest Press",
                category = "Strength",
                caloriesPerRep = 3.5,
                image = R.drawable.chest_press
            ),
            ExerciseType(
                name = "Shoulder Press",
                category = "Strength",
                caloriesPerRep = 3.0,
                image = R.drawable.shoulder_press
            ),
            ExerciseType(
                name = "Lunges",
                category = "Strength",
                caloriesPerRep = 2.5,
                image = R.drawable.reverse_lunges
            ),
            ExerciseType(
                name = "Warrior Yoga",
                category = "Yoga",
                caloriesPerRep = 1.5,
                image = R.drawable.warrior_yoga_pose
            ),
            ExerciseType(
                name = "Tree Yoga",
                category = "Yoga",
                caloriesPerRep = 1.0,
                image = R.drawable.tree_yoga_pose
            )
        )
    }
}

data class ExerciseType(
    val name: String,
    val category: String,
    val caloriesPerRep: Double,
    val image: Int? = null,
    val level: String = category,
    val calorie: Double = caloriesPerRep
)