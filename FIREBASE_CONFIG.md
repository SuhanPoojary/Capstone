# Firebase Configuration Guide

## Current Status
✅ google-services.json is in place with your Firebase project credentials
✅ Firebase dependencies are added to build.gradle.kts
✅ Firebase initialization is in SafeReadyApp.kt
✅ Notification channels are set up for alerts and sync

## What You Need to Do in Firebase Console

### 1. Enable Authentication Methods
Go to Firebase Console → Authentication → Sign-in method
- ✅ Anonymous (already working for fallback)
- ✅ Email/Password (for user signup/login)

### 2. Create Firestore Database
Go to Firebase Console → Firestore Database
- Start in **Test mode** (for development)
- Choose a region close to your users
- Create these collections:

#### Collection: `users`
Document ID: `{uid}` (auto-generated from auth)
Fields:
```
{
  "name": "string",
  "email": "string",
  "institution": "string",
  "region": "string",
  "level": "number",
  "points": "number",
  "streak": "number",
  "createdAt": "timestamp",
  "lastSyncAt": "timestamp"
}
```

#### Collection: `progress`
Document ID: `{uid}`
Subcollection: `lessons`
Document ID: `{lessonKey}`
Fields:
```
{
  "completed": "boolean",
  "completedAt": "timestamp",
  "videoWatchedMinutes": "number",
  "quizScore": "number",
  "quizPassed": "boolean"
}
```

#### Collection: `analytics`
Document ID: `{uid}`
Fields:
```
{
  "totalLessonsCompleted": "number",
  "totalQuizzesAttempted": "number",
  "averageQuizScore": "number",
  "lastActiveAt": "timestamp"
}
```

### 3. Firestore Security Rules
Replace your current rules with these production-level rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow authenticated users to read/write their own user document
    match /users/{uid} {
      allow read, write: if request.auth.uid == uid;
    }
    
    // Allow authenticated users to read/write their own progress
    match /progress/{uid} {
      allow read, write: if request.auth.uid == uid;
      
      // Nested lessons subcollection
      match /lessons/{lesson} {
        allow read, write: if request.auth.uid == uid;
      }
    }
    
    // Allow authenticated users to read/write their own analytics
    match /analytics/{uid} {
      allow read, write: if request.auth.uid == uid;
    }
    
    // Leaderboards (read-only for all authenticated users)
    match /leaderboards/{document=**} {
      allow read: if request.auth != null;
      allow write: if false;
    }
    
    // Public app config (read-only)
    match /config/{document=**} {
      allow read: if true;
      allow write: if false;
    }
    
    // Default deny all other access
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

### 4. Enable Firebase Cloud Messaging (FCM)
For Phase 4A (push notifications):
1. Go to Firebase Console → Cloud Messaging
2. Generate a server key (if not auto-generated)
3. You'll use this for sending notifications from a backend service
4. For now, leave as-is; we'll integrate in Phase 4A

## Project Configuration Details
- **Project ID:** `pralaytrata`
- **Project Number:** `987511247898`
- **Package Name:** `com.example.capstone`
- **API Key:** `AIzaSyC9YU41ms-fwmtFA2MlqyrDcfuqxNEm95I`
- **App ID:** `1:987511247898:android:aa12a4de3b9fd13249ada3`

## Build Files Status
✅ app/build.gradle.kts has all required dependencies
✅ build.gradle.kts has google-gms plugin
✅ AndroidManifest.xml has all required permissions
✅ SafeReadyApp.kt initializes Firebase

## What's NOT Yet Done (Phase 4)
- FCM token management
- Real push notification handling
- Real-time database listeners for collaboration
- Analytics event tracking
- Offline write queue
- Leaderboards and achievement system

