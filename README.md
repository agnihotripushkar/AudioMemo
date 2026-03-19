# AudioMemo

AudioMemo is a native Android application that records audio, transcribes it using the OpenAI Whisper API, and provides AI-powered summaries. Built with modern Android development practices, it features a sleek intuitive UI and robust background processing capabilities.

## Features

- **Audio Recording:** High-quality voice recording with a real-time audio wavelength visualization UI.
- **Smart Chunking:** Automatically splits long audio recordings into manageable 30-second chunks and saves them to local storage.
- **AI Transcription:** Integrates with OpenAI's `whisper-1` model to provide accurate transcriptions of recorded audio.
- **Intelligent Summarization:** Uses OpenAI's `gpt-4o-mini` model to generate concise summaries from the transcriptions.
- **Local Storage:** Securely stores audio chunks and transcriptions locally using Room Database.
- **Background Processing:** Ensures seamless transcription and summarization tasks run efficiently in the background using WorkManager.

## Tech Stack

The app is built using the latest Android development technologies and architecture patterns:

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel)
- **Concurrency:** Coroutines & Flow
- **Dependency Injection:** Dagger Hilt
- **Networking:** Retrofit2 & OkHttp
- **JSON Serialization:** Kotlinx Serialization
- **Local Database:** Room
- **Background Work:** WorkManager

## Prerequisites

- **Android Studio:** Latest version recommended (Giraffe or newer).
- **Minimum SDK:** API 24 (Android 7.0)
- **Target SDK:** API 34 (Android 14)
- **OpenAI API Key:** Required for transcription and summarization features.

## Getting Started

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   ```

2. **Set up the OpenAI API Key:**
   To securely provide your API key to the application without checking it into version control, add it to your `local.properties` file:
   
   Open the `local.properties` file in the project's root directory (create it if it doesn't exist) and add the following line:
   ```properties
   OPENAI_API_KEY=your_openai_api_key_here
   ```

3. **Build the project:**
   Sync the project with Gradle files in Android Studio to download all required dependencies.

4. **Run the app:**
   Select an emulator or a physical device and click the Run button in Android Studio.

## Permissions

The app requires the following permissions to function correctly:
- `RECORD_AUDIO`: To capture voice notes.
- `INTERNET`: To communicate with the OpenAI API.

## Architecture Highlights

- **Dependency Injection:** Centralized using Dagger Hilt for components like Networking, Database, and Repositories.
- **Modular Data Sources:** Clean separation between local storage (Room) and remote APIs (Retrofit).
- **Declarative UI:** Completely built using Jetpack Compose with a reactive state management approach using StateFlow.

## License

This project is licensed under the MIT License.
