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

| Permission | Purpose |
|---|---|
| `RECORD_AUDIO` | Capture voice recordings (runtime — requested before recording starts) |
| `INTERNET` | Communicate with the OpenAI API |
| `FOREGROUND_SERVICE` | Keep the recording service alive in the background |
| `FOREGROUND_SERVICE_MICROPHONE` | Android 14 foreground service type for microphone access |
| `POST_NOTIFICATIONS` | Show the persistent recording notification (API 33+) |
| `READ_PHONE_STATE` | Detect incoming / active calls to pause recording |
| `MODIFY_AUDIO_SETTINGS` | Start Bluetooth SCO for headset microphone support |
| `BLUETOOTH` | Bluetooth headset detection (API ≤ 30) |

## Interruption Handling

AudioMemo is built to survive all common audio interruptions during a recording session. The table below describes every scenario, where it is handled, and what happens.

| Scenario | Status | Mechanism | Behaviour |
|---|---|---|---|
| **Process death** | ✅ Handled | `ChunkFinalizationWorker` (WorkManager) | A 15-second delayed WorkManager job is enqueued when recording starts. If the process dies before the user stops cleanly, the worker fires, marks all in-flight chunks `FAILED`, and re-queues `TranscriptRetryWorker` to retry uploads. |
| **Network failure during upload** | ✅ Handled | `WhisperUploadWorker` + `TranscriptRetryWorker` | `WhisperUploadWorker` uses WorkManager's built-in retry with exponential back-off. `TranscriptRetryWorker` can also re-enqueue failed chunks on demand. |
| **Long recordings** | ✅ Handled | 30-second chunking + Room | `AudioRecorderManager` splits audio into 30-second `.m4a` files. Each chunk is saved to Room and uploaded independently, so arbitrarily long sessions are supported without memory pressure. |
| **Audio focus loss** | ✅ Handled | `AudioInterruptionManager` | Requests `AUDIOFOCUS_GAIN` at start. On `AUDIOFOCUS_LOSS` / `AUDIOFOCUS_LOSS_TRANSIENT` / `AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK` → pauses recording and shows "Paused – Audio focus lost" notification. Resumes automatically on `AUDIOFOCUS_GAIN`. |
| **Phone calls** | ✅ Handled | `AudioInterruptionManager` | Listens via `TelephonyCallback` (API 31+) or `PhoneStateListener` (< API 31). `CALL_STATE_RINGING` / `CALL_STATE_OFFHOOK` → pauses and shows "Paused – Phone call" notification. `CALL_STATE_IDLE` → resumes automatically. Requires `READ_PHONE_STATE` permission. |
| **Android 14 foreground service type** | ✅ Handled | `AndroidManifest.xml` | `AudioRecordingService` is declared with `android:foregroundServiceType="microphone"` and the `FOREGROUND_SERVICE_MICROPHONE` permission, satisfying the Android 14 (API 34) requirement for microphone FGS. |
| **Mic hardware toggle (mute button)** | ✅ Handled | `AudioInterruptionManager` | Registers `AudioManager.ACTION_MICROPHONE_MUTE_CHANGED` (API 27+). When the hardware mic is muted → pauses recording and shows "Paused – Microphone muted" notification. Unmuting → resumes automatically. Initial mute state is checked at start. |
| **One-time permission** | ✅ Handled | `TranscriptScreen` (Compose) | Before starting the foreground service, `RECORD_AUDIO` permission is checked via `ActivityResultContracts.RequestPermission`. The service is only started after the permission is confirmed. The check runs every time the screen is composed, so Android 11+ one-time grants (revoked when the user leaves the app) are correctly re-requested on the next session. |
| **Bluetooth SCO connect/disconnect** | ✅ Handled | `AudioInterruptionManager` | Registers `AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED`. On start, if a `TYPE_BLUETOOTH_SCO` input device is already connected, `startBluetoothSco()` is called to route audio through it. `SCO_AUDIO_STATE_CONNECTED` → shows "Microphone switched to Bluetooth headset" notification. `SCO_AUDIO_STATE_DISCONNECTED` / `SCO_AUDIO_STATE_ERROR` → stops SCO and shows "Microphone switched to device microphone" notification. SCO is stopped cleanly when recording ends. |

### Dual-flag pause logic

Multiple interruptions can overlap (e.g., a phone call arrives while audio focus is also lost). `AudioInterruptionManager` tracks three independent boolean flags — `pausedForCall`, `pausedForFocus`, and `pausedForMicMute` — and only calls `onResumeRequested()` when **all three** are clear. This prevents premature resumption when one condition clears but another is still active.

## Architecture Highlights

- **Dependency Injection:** Centralized using Dagger Hilt for components like Networking, Database, and Repositories.
- **Modular Data Sources:** Clean separation between local storage (Room) and remote APIs (Retrofit).
- **Declarative UI:** Completely built using Jetpack Compose with a reactive state management approach using StateFlow.

## License

This project is licensed under the MIT License.
