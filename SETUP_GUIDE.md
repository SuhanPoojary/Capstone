# Setup Guide

## Prerequisites

- Android Studio
- Compatible JDK for the project’s Android Gradle Plugin version
- Android emulator or physical Android device
- Standard Android SDK components installed

## Installation Steps

1. Open the project in Android Studio.
2. Wait for Gradle sync to complete.
3. Let resource indexing finish.
4. Run the app on an emulator or device.

## Running the App

### Android Studio
- Select a target device.
- Click **Run**.

### Command Line

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

## Firebase / Backend Setup

### Current Status
No Firebase or backend setup is required for the current project state.

### If backend support is added later
- Add `google-services.json`
- Apply the Google Services plugin
- Configure authentication, Firestore, or storage
- Replace the current UI-only login/signup flow with real account handling

## Common Setup Issues

### 1. Gradle sync fails
**Possible causes**
- JDK mismatch
- stale caches
- incorrect Android Studio configuration

**Fix**
- Reopen the project
- Sync again
- Verify the JDK used by Android Studio

### 2. Video does not play
**Possible causes**
- missing file in `res/raw`
- incorrect file name
- wrong chapter/language mapping

**Fix**
- Verify the resource naming pattern
- Confirm the file exists in `app/src/main/res/raw/`

### 3. Language picker shows no options
**Possible causes**
- no matching raw media files for that chapter

**Fix**
- Add the expected video file
- Rebuild the project

### 4. Dashboard shows default user name
**Possible causes**
- name field left blank
- extra not passed correctly

**Fix**
- Enter a name during signup
- Verify `Intent` extra handling

### 5. Fullscreen player closes immediately
**Possible causes**
- missing URI extra
- missing layout IDs in the fullscreen layout

**Fix**
- Ensure `FullscreenPlayerActivity.EXTRA_URI` is passed
- Confirm the fullscreen layout contains the expected player view ID

## Developer Notes

- The app is offline-first and media-driven.
- Lesson content is controlled by files in `res/raw`.
- Crash logs can be inspected through `CrashViewerActivity`.
- `LocationHelper` is ready for future location-aware features.

## Recommended First Validation

After setup, verify the main flow:

```text
Splash → Onboarding → Signup/Login → Dashboard → Start Learning → Disaster Detail → Playback
```

If this flow works, the project is set up correctly.

