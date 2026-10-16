# Slate Downloader — Android project

This is the Android adaptation of Slate. It is SOURCE CODE, **not a precompiled APK**.

## Build an installable APK
1. Install Android Studio (with Android SDK platform 35 and a JDK 17 compatible Gradle setup).
2. Open the `SlateAndroid` folder as a project, then allow Gradle to sync and download the declared libraries.
3. Select **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Find the APK at `app/build/outputs/apk/debug/app-debug.apk` after a successful build.
5. Install it on your own Android device for testing. Android may require you to approve installation from that source.

The build requires internet access. Android Studio can install an appropriate Gradle release automatically when importing the project; if it asks for a Gradle distribution, use Gradle 8.9 to match Android Gradle Plugin 8.7.3.

## Features
* YouTube HTTPS link input
* Best, 1440p, 1080p, 720p, 480p, MP3
* Progress, cancellation and error messages
* Dark Slate-style interface
* Android app-specific download storage (`Android/data/com.slate.downloader/files/Download/Slate`)

## Notes
* This has not been compiled or run in an Android SDK environment here; build compatibility and download behavior are **unverified**.
* Some YouTube videos or formats cannot be downloaded. 1440p requires the source to offer that quality.
* Files go into app-specific external storage, not the top-level public Downloads folder; Android file managers may restrict access to Android/data. Uninstalling the app may delete downloaded files unless moved elsewhere.
* No email address, account name, hardcoded credentials, analytics, telemetry, or personal data are included.
* This package relies on the third-party youtubedl-android library, which initializes bundled executables and contacts the download source.
* Use only for media you are authorized to download.
