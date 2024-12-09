# FitU Student Mobile Application

FitU is an Android application that helps students perform exercises correctly through real-time pose detection and AI-powered form correction. The app allows instructors to assign exercises to students and tracks their progress.

## Features

- Real-time pose detection and exercise form correction
- Exercise assignments from instructors
- Progress tracking and exercise history
- Multiple exercise support including:
  - Push-ups
  - Squats
  - Sit-ups
  - Deadlifts
  - Chest Press
  - Shoulder Press
  - Lunges
  - Warrior Yoga Pose
  - Tree Yoga Pose

## Technologies Used

- Kotlin
- Firebase (Authentication, Firestore)
- ML Kit Pose Detection
- CameraX
- Android Jetpack Components
  - Navigation
  - ViewBinding
  - ViewModel
  - LiveData

## Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK 21 or higher
- Google Play Services
- Firebase project setup with Authentication and Firestore enabled

## Setup

1. Clone the repository: 
git clone https://github.com/johnrobertdelinila/FitU-Student-Mobile-Application.git


2. Open the project in Android Studio

3. Create a Firebase project and add your `google-services.json` file to the app directory

4. Enable Authentication and Firestore in your Firebase project

5. Build and run the application

## Project Structure

- `app/src/main/java/com/example/poseexercise/`
  - `adapters/` - RecyclerView adapters
  - `data/` - Data models and repository
  - `posedetector/` - ML Kit pose detection implementation
  - `views/` - Activities and Fragments
  - `util/` - Utility classes

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- ML Kit for pose detection capabilities
- Firebase for backend services
- Android Jetpack libraries