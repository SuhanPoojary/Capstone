# SafeReady Progress Tracker

> Canonical progress log for the capstone. Update this file after each meaningful implementation change.

## Current focus
- Improve real UI interaction across the app
- Keep progress tracking in one place
- Avoid creating extra phase-specific status files

## Completed
- Single-activity fragment shell already in place with `MainActivity`
- Core learning tabs exist: Home, Training, Progress, Assistant, Profile
- Offline lesson playback uses ExoPlayer
- Local progress and quiz scoring are wired through repositories
- Rule-based assistant and gamification are present
- Mesh transport exists in `MeshService.kt`
- Mesh data cache exists in `MeshMessageCache.kt`
- `MeshRepository.kt` added to bridge mesh transport and local cache
- `MeshViewModel.kt` added for mesh state control
- `MeshDebugActivity.kt` and `MeshDebugFragment.kt` added for manual testing
- Profile screen now exposes a visible `Open Mesh Debug` action
- Assistant input now supports send-on-enter and empty input guard
- Reset actions now require confirmation dialogs
- Home recommendation card now opens the recommended disaster detail or training tab
- Mesh debug screen now shows live transport state and cached message count
- Mesh repository now drops expired mesh messages, filters duplicates, and relays eligible packets once
- Home and Mesh screens now have visible status badges for a more interactive feel
- Mesh send/relay now has bounded retry telemetry (sent, relayed, failed, retried, dropped duplicate/expired)
- Mesh debug screen now includes telemetry counters and a clear-telemetry action
- Mesh debug entry now requests required Bluetooth/location runtime permissions
- Room migration path scaffold added via `MeshRoomMigrationPlan.kt` and repository preview count
- Mesh debug screen now includes a manual resend action for the latest failed mesh message
- Emergency Mode toggle added to Profile UI and persisted locally
- Mesh debug access and mesh actions are now gated behind Emergency Mode
- Home screen now shows Emergency Mode status badge
- Profile now shows Firebase owner/auth status card to start Phase 4 UI work
- Profile now includes `Open Phase 4 Social UI` entry for a new collaboration UI hub screen
- Phase 4 hub now opens a real `Global Leaderboard` screen backed by Firebase leaderboard reads
 - Phase 4 hub now opens a real `Global Leaderboard` screen backed by Firebase leaderboard reads
 - Phase 4 Friends UI implemented (Phase4FriendsActivity) and wired from Phase4Hub
 - Phase 4 Activity Feed UI implemented (Phase4FeedActivity) and wired from Phase4Hub
 - Dedicated Emergency screen implemented (EmergencyActivity) with SOS button that queues SOS via `MeshViewModel` (gated by Emergency Mode)
- `progress.md` is now the single canonical progress tracker

## In progress
- Move from Room migration scaffold to full Room-backed persistence implementation
- Build Phase 4 UI flows (leaderboard/friends/activity feed) now that Firebase ownership is available
- Add more obvious success/error feedback in the main UI
- Reduce string-resource warnings across newly touched layouts and fragments

## Deferred / not currently implementing
- BLE fallback transport unless Nearby testing shows it is needed
- Large navigation overhaul beyond the current single-activity setup

## Validation status
- `assembleDebug` passes after fixing Firestore update calls
- Mesh-related UI files compile successfully
- `assembleDebug` passes after Phase 5 retry/telemetry and permissions updates
- `assembleDebug` passes after wiring Kotlin Android/kapt plugins and the mesh resend retry flow
- Remaining work is mostly product polish and deeper device testing

## Next implementation step
- Continue the Room-backed persistence implementation for mesh cache data
- Continue Phase 4B UI rollout: Friends list UI and Activity Feed UI after Global Leaderboard
 - Continue Phase 4B UI rollout: Friends list UI and Activity Feed UI after Global Leaderboard (friends/feed implemented; remaining: friend requests, accept/reject flows)
- Keep this file updated instead of creating new phase note files

## PLAN update
- `PLAN.md` appended with Phases 5, 6, 7, and 8 (mesh network, emergency mode, UI/UX redesign, ML placeholder).
- This change documents design, data models, components, and limitations for advanced phases.

## Phase 5 current status
- MeshService, MeshRepository, MeshViewModel, and debug UI files were created earlier but require:
  - Nearby Connections integration and permission flows (work remaining)
  - Room-backed persistence for message cache (in progress)
  - Battery-friendly discovery scheduling (planned)
  - UI actions for sending SOS from main UI (planned)
  - End-to-end testing of store-and-forward relaying (planned)
  - Emergency-mode-first gating for mesh usage (completed)

## Remaining Phase 5 tasks (actionable)
1. Integrate Nearby Connections API in `MeshService` (Primary)
   - Implement advertising and discovery
   - Implement reliable data send/receive
   - Add connection lifecycle callbacks
2. Add Room-backed `MessageCache` entity and DAO
   - MessageEntity and MessageDao
   - Integration tests for duplicate suppression and TTL pruning
3. Add permission prompts and rationale UI flows for Bluetooth, Location, Nearby
4. Expose SOS send action in `HomeFragment` and `EmergencyFragment` UI
5. Implement acknowledgement / relay telemetry logging in `MeshRepository`
6. Implement limited background operation using a foreground service or WorkManager when required
7. Add unit tests and instrumented tests for MessageCache behavior

## Next immediate step
- Start implementation of Nearby Connections setup in `MeshService` and add a small UI hook in `MeshDebugFragment` to trigger advertising/discovery.

## Notes
- Do not create new phase-specific progress files. Use this `progress.md` as the single canonical log.
- Firebase writes remain disabled due to project ownership concerns; mark as deferred where applicable.

## Last updated
- 2026-05-07 — Implemented Phase4FriendsActivity, Phase4FeedActivity, and EmergencyActivity (SOS UI). Phase4Hub wired to new screens. Build validated with assembleDebug.

## Manifest & build updates
- Added `ACCESS_BACKGROUND_LOCATION` and `FOREGROUND_SERVICE` permissions to `AndroidManifest.xml` to support long-running mesh discovery and foreground operation where required.
- Verify runtime permission flows for Android 12+ bluetooth permissions (BLUETOOTH_SCAN, BLUETOOTH_CONNECT, BLUETOOTH_ADVERTISE).

## Firestore / google-services note
- You replaced your friend's `google-services.json`. Since you do not own that Firebase project, cloud writes are intentionally disabled in the app; mark any Firestore-backed features as deferred until a valid project is available.
- Current Firestore rules are:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```
- These rules deny all reads and writes. To allow authenticated users to read/write their own documents, a minimal safe rule set (example) would be:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /progress/{docId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
    }
    // keep other collections locked until explicit design and security review
    match /{document=**} {
      allow read: if false;
      allow write: if false;
    }
  }
}
```
- Do NOT deploy relaxed rules to the friend's project. Only apply rules to a project you control and after testing.

## Next validation steps
- Run `./gradlew assembleDebug` and fix any manifest or Gradle sync errors.
- Verify Nearby Connections dependency resolves and device build targets support Google Play services.
- Add Nearby permission request flows in MeshDebugFragment and MainActivity as part of Phase 5 implementation.
