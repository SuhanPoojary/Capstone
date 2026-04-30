# Database Schema

## Current Data Layer

SafeReady does **not** currently use Room, Firebase, or a remote database.

The current data layer is made of:
- `Intent` extras for screen state,
- packaged raw media resources for lessons,
- and internal file storage for crash logs.

## Tables / Collections

There are no database tables or collections in the current implementation.

## Logical Data Entities

### 1. User Display Data
**Storage type:** `Intent` extras and in-memory UI state

| Field | Type | Purpose |
|---|---|---|
| `name` | `String` | Display name shown on dashboard |
| `email` | `String` | Used to derive fallback display name |
| `password` | `String` | Collected in UI only |
| `institution` | `String` | Collected in UI only |

**Related screens**
- `LoginActivity`
- `SignupActivity`
- `DashboardActivity`

**Example**
```text
Name: Arjun Sharma
Email: arjun@iitb.ac.in
```

---

### 2. Disaster Lesson Metadata
**Storage type:** Derived from resource naming

| Field | Type | Purpose |
|---|---|---|
| `disasterKey` | `String` | Identifies the disaster topic |
| `chapterIndex` | `Int` | Selects the lesson chapter |
| `phase` | `String` | `general`, `during`, or `after` |
| `languageCode` | `String` | Language variant such as `en`, `hi`, `mr` |
| `resourceName` | `String` | Packaged raw media file name |
| `uri` | `String` | Android resource URI used for playback |

**Naming convention**
```text
{prefix}_ch{n}_{phase}_{lang}.mp4
```

**Examples**
```text
eq_ch1_general_en.mp4
flood_ch2_during_hi.mp4
cyclone_ch1_general_tel.mp4
```

**Related code**
- `DemoVideoRepository.getVideoUri(...)`
- `DemoVideoRepository.getAvailableLanguages(...)`
- `LanguageOption`

---

### 3. Language Option
**Storage type:** UI model

| Field | Type | Purpose |
|---|---|---|
| `code` | `String` | Language code |
| `label` | `String` | User-facing label |

**Example**
```kotlin
LanguageOption(code = "hi", label = "Hindi")
```

---

### 4. Crash Log Entry
**Storage type:** Internal file storage

| Field | Type | Purpose |
|---|---|---|
| `time` | `String` | Timestamp of the crash |
| `thread` | `String` | Thread name |
| `stackTrace` | `String` | Exception trace |
| `filePath` | `String` | Saved under `files/crash/` |

**Example file**
```text
files/crash/crash_2026-04-30_14-22-09.txt
```

**Related code**
- `SafeReadyApp.writeCrashToFile(...)`
- `CrashViewerActivity.loadLatestCrash()`

## Relationships

### User → Dashboard
The user name is passed from login/signup into the dashboard using an `Intent` extra.

### Disaster → Chapters
Each disaster maps to three chapters:
- 1: General / preparation
- 2: During the event
- 3: After the event

### Chapter → Languages
Available languages depend on which raw media files exist for that chapter.

### Crash → Viewer
The crash viewer loads the latest crash log file from internal storage.

## Notes on Room / Firebase Structure

### Room
Not used yet.

### Firebase
Not used yet.

### Backend API
Not used yet.

### Internal storage
Used only for crash logs. This keeps the app self-contained and offline-friendly.

## Example Raw Media Structure

```text
app/src/main/res/raw/
├── eq_ch1_general_en.mp4
├── eq_ch1_general_hi.mp4
├── flood_ch2_during_hi.mp4
└── ...
```

## Example Crash Log Content

```text
SafeReady crash
time=2026-04-30_14-22-09
thread=main

java.lang.IllegalStateException: ...
```

