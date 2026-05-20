# Indoor Navigation — Mobile Terminal

Android client for the indoor navigation system. Captures images/video, sends them to a recognition server, and displays directional guidance (forward/left/right) on the device.

## Features

- **Image recognition** — Select or capture a photo, get navigation direction
- **Video recognition** — Process video files frame-by-frame, real-time direction updates
- **Real-time camera** — Live camera feed with continuous scene recognition
- **Direction overlay** — Navigation arrows drawn directly on the image

## Architecture

```
┌─────────────────────────────────────────────────────┐
│              Android App (Java)                      │
│                                                      │
│  MainActivity  ──→  ChosePicture  ──→  HTTPAPI      │
│       │               ChoseVideo   ──→    POST       │
│       │               ShiBie (Camera)   image data   │
│       └──────────────────────────────────────┘       │
│                         │                            │
│                         ▼                            │
│              Recognition Server                      │
│              (REST API)                              │
└─────────────────────────────────────────────────────┘
```

## Project Structure

```
app/
├── build.gradle
├── src/main/
│   ├── AndroidManifest.xml
│   ├── java/com/example/myapplication/
│   │   ├── MainActivity.java        # Main entry, image/video processing
│   │   ├── ChosePicture.java        # Image selection activity
│   │   ├── ChoseVideo.java          # Video selection activity
│   │   ├── ShiBie.java              # Real-time camera recognition
│   │   ├── Login.java               # User login
│   │   ├── Feedback.java            # Feedback form
│   │   ├── Menu.java                # Menu helper
│   │   └── HTTPAPI.java             # HTTP client utilities
│   ├── res/
│   │   ├── layout/                  # Activity layouts (XML)
│   │   ├── drawable/                # Images and icons
│   │   ├── values/                  # Strings, colors, themes
│   │   └── xml/                     # Security config etc.
│   └── AndroidManifest.xml
build.gradle                          # Root build config
settings.gradle                       # Project settings
gradle.properties                     # Gradle properties
gradlew / gradlew.bat                 # Gradle wrapper
```

## Requirements

- Android SDK 24+
- Target SDK 31
- Recognition server running (default: `http://172.20.3.9:8086/`)
- Camera permission for real-time mode

## Build & Run

### Android Studio

1. Open this directory in Android Studio
2. Let Gradle sync complete
3. Run on device or emulator

### Command Line

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

## Configuration

The recognition server URL is configured in `MainActivity.java`:

```java
String baseUrl = "http://172.20.3.9:8086/";
```

Change this to match your server deployment before building.

## Dependencies

| Library | Purpose |
|---------|---------|
| OpenCV 4.5.3 | Image processing, camera frame capture |
| JavaCV 1.4.2 | Video frame extraction |
| FFmpeg 4.0.1 | Video codec support |
| AndroidX | Modern Android support libraries |

## Recognition Server

This app sends image data to a recognition server via REST API:

- **Endpoint:** `POST {baseUrl}/rest/api/navigation`
- **Body:** `image={base64_encoded_image}`
- **Response:** JSON with `data.resultdata.direct` field (e.g., `"forward"`, `"turn_left"`)

The server repo: [Indoor-navigation-algorithm](https://github.com/111wukong/Indoor-navigation-algorithm)

## License

MIT
