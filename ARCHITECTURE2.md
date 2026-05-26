

# SafeReady Product Ecosystem Architecture (ARCHITECTURE2)

> This document is the master product-flow and feature-ownership blueprint for SafeReady.
>
> It is **not** a backend-only architecture document. It defines where features belong, how users move through the app, which tab owns which systems, and how Normal Mode and Emergency Mode work as two connected ecosystems.
>
> `ARCHITECTURE.md` remains historical context. `ARCHITECTURE2.md` is the editable blueprint for future UI restructuring, feature movement, and navigation changes.

---

## 1. Product Architecture Goal

SafeReady is evolving into an offline-first emergency preparedness ecosystem and emergency operating platform.

The architecture must make it easy to answer four questions at all times:
1. **Where does this feature belong?**
2. **Which tab owns this experience?**
3. **How does the user get there?**
4. **What happens when the app enters Emergency Mode?**

The app should feel intentional, operational, and emergency-first, not like a loose set of unrelated screens.

---

## 2. The Two Core App States

SafeReady has two major states.

### A. Normal Mode
Normal Mode is the preparedness and planning ecosystem.

It supports:
- learning
- readiness tracking
- personal setup
- health/supply preparation
- recommendations
- progress and growth
- account configuration

### B. Emergency Mode
Emergency Mode is the real-time survival operations ecosystem.

It supports:
- SOS broadcasting
- emergency communication
- offline maps
- rescue acknowledgment
- disaster alerts
- connectivity status
- emergency timeline and operational state

### Architectural rule
These are not just two screens.
They are two connected operational states with different priorities, different UI behavior, and different feature emphasis.

---

## 3. App Ecosystem Diagram

```text
MainActivity
│
├── Home
│   └── Situational Awareness Hub
│
├── Lab
│   └── Preparedness & Simulation Ecosystem
│
├── Emergency
│   └── Real-Time Survival Operations System
│
├── MedReady
│   └── Preparedness Supply Intelligence System
│
└── Profile
    └── System Configuration & Personalization
```

### Mode relationship
```text
Normal Mode
├── Home
├── Lab
├── MedReady
└── Profile

Emergency Mode
├── Emergency
├── Home (light status access only if needed)
└── Profile (limited safety/config actions only if needed)
```

Emergency Mode should take priority over normal browsing when activated.

---

## 4. Top-Level Tab Ownership

SafeReady has 5 primary tabs. Each tab is a subsystem, not just a screen.

### 4.1 Home — Situational Awareness Hub
Home is the lightweight command center for overview and immediate readiness.

#### Home owns
- readiness summary
- local risk overview
- quick actions
- recommendations
- emergency shortcuts
- map preview
- lightweight status information
- high-level progress snapshots

#### Home should NOT own
- deep learning modules
- full social systems
- mesh diagnostics
- advanced analytics
- long settings flows
- detailed account management
- heavy feature configuration screens

#### Home purpose
Home should answer: **“What should I know right now?”**

---

### 4.2 Lab — Preparedness & Simulation Ecosystem
Lab is the engagement and training environment.

#### Lab owns
- simulations
- drills
- challenges
- lessons
- quizzes
- XP
- streaks
- badges
- achievements
- leaderboards
- progression systems
- retention loops

#### Recommended internal structure
```text
Lab
├── Daily Challenges
├── Simulations
├── Quick Drills
├── Interactive Learning
├── Achievements
├── Leaderboards
└── Progression Systems
```

#### Lab purpose
Lab should answer: **“How do I become more prepared?”**

#### Lab design rules
- Keep learning fast and structured.
- Keep gamification motivating but not noisy.
- Keep progression visible without burying important safety tasks.
- Keep long-form content readable and navigable.

---

### 4.3 Emergency — Real-Time Survival Operations System
Emergency is the most important operational subsystem.

#### Emergency owns
- SOS broadcasting
- emergency communication
- offline emergency relay
- nearby rescue network
- offline maps
- rescue acknowledgments
- disaster alerts
- connectivity status
- emergency timeline
- quick survival tools
- live operational state

#### Emergency internal structure
```text
Emergency
├── SOS System
├── Emergency Communication
├── Offline Maps
├── Rescue Status
├── Disaster Alerts
├── Connectivity Status
├── Emergency Timeline
└── Quick Survival Tools
```

#### Important naming rule
Mesh is not a user-facing standalone feature.

User-facing concepts should be framed as:
- Emergency Communication
- Offline Emergency Relay
- Nearby Rescue Network

Avoid user-facing language like:
- Mesh Debug
- Packet Relay
- Transport Layer

Those are implementation details, not product concepts.

#### Emergency purpose
Emergency should answer: **“What do I do now, and how do I get help?”**

#### Emergency design rules
- Minimize steps.
- Show clear status.
- Prioritize oversized, readable actions.
- Reduce nonessential content.
- Keep operational information visible and calm.
- Never bury SOS behind deep navigation.

---

### 4.4 MedReady — Preparedness Supply Intelligence System
MedReady supports readiness around medical and supply preparedness.

#### MedReady owns
- OCR scanning
- emergency kit analysis
- medicine tracking
- expiry tracking
- preparedness scoring
- supply recommendations
- AI kit analysis
- inventory readiness
- offline preparedness packs

#### Recommended internal structure
```text
MedReady
├── Kit Scanner
├── Inventory Analysis
├── Expiry Tracking
├── Readiness Scoring
├── Recommendations
└── Offline Preparedness Packs
```

#### MedReady purpose
MedReady should answer: **“What supplies do I have, what is missing, and what expires soon?”**

---

### 4.5 Profile — System Configuration & Personalization
Profile configures systems. Emergency Mode uses systems.

This separation is critical.

#### Profile owns
- user identity
- emergency contacts
- Firebase sync
- offline downloads
- accessibility settings
- connectivity preferences
- emergency behavior configuration
- notification settings
- map download settings
- mesh communication settings
- account and profile management

#### Profile purpose
Profile should answer: **“How do I configure SafeReady for my needs?”**

#### Profile design rule
Profile should configure behavior, not become a dumping ground for feature controls.

Examples:
- Offline maps are configured in Profile.
- Mesh behavior is configured in Profile.
- SOS preferences are configured in Profile.
- Emergency Mode consumes those settings.

---

## 5. Feature Ownership Map

### Home
Owns:
- summaries
- awareness cards
- emergency shortcut entry points
- quick insights
- lightweight map preview

### Lab
Owns:
- learning flows
- simulations
- drills
- quizzes
- achievements
- leaderboards
- progression

### Emergency
Owns:
- SOS
- emergency communication
- offline maps
- alerts
- acknowledgment
- live operational state

### MedReady
Owns:
- supplies
- medicine readiness
- kit scanning
- inventory intelligence
- expiry tracking

### Profile
Owns:
- identity
- settings
- sync controls
- emergency preferences
- accessibility
- offline and connectivity configuration

---

## 6. Cross-Module Relationships

SafeReady systems should share data intentionally, not accidentally.

### Shared relationships
- Lab updates preparedness and progression metrics shown on Home.
- MedReady contributes readiness scores and supply status to Home.
- Profile configures emergency behavior used by Emergency Mode.
- Firebase sync can affect all major systems.
- Achievements from Lab can appear on Home and Profile.
- Emergency Mode reads offline map and communication settings from Profile.

### Isolation rules
- Home should not own deep learning or communication internals.
- Lab should not own emergency operational controls.
- Emergency should not become cluttered with training content.
- MedReady should not become a generic settings page.
- Profile should not become a feature graveyard.

---

## 7. User-State Architecture

### Primary state sources
- authentication state
- onboarding state
- sync state
- offline availability
- emergency mode state
- readiness/progress state
- notification/alert state
- configuration state

### State flow diagram
```text
User Identity + Settings
        ↓
Local State + Cloud Sync
        ↓
Tab-Specific UI State
        ↓
Normal Mode or Emergency Mode behavior
```

### Operational rule
The UI should react to state, not own state.

### Example state ownership
- Repository: data source and persistence
- ViewModel: screen or feature state
- Fragment: display and interaction
- Profile: configuration source
- Emergency: runtime operational state

---

## 8. Navigation Architecture

### Bottom navigation ownership
The bottom navigation should represent the five major subsystems.

### Navigation hierarchy
```text
MainActivity
│
├── NavHostFragment
│   ├── HomeFragment
│   ├── LabFragment
│   ├── EmergencyFragment
│   ├── MedReadyFragment
│   └── ProfileFragment
```

### Navigation principles
- Keep the shell stable.
- Keep tab switching predictable.
- Keep deep flows inside their owning tab.
- Use modal/full-screen flows only when they improve clarity.
- Preserve backstack behavior that feels simple and safe.

### Floating Emergency FAB behavior
A floating Emergency FAB may exist across Normal Mode.

#### Rules
- It should always provide a direct path into Emergency Mode.
- It should never be hidden behind deep navigation.
- It should feel like a safety action, not a general shortcut.
- It should prioritize SOS and emergency assistance entry points.

### Backstack philosophy
- Normal Mode back navigation should feel shallow and understandable.
- Emergency Mode should prioritize operational continuity over decorative navigation.
- Modal screens should be used for confirmation, not for important system ownership.

---

## 9. Emergency Mode Transition Architecture

### Transition flow
```text
Trigger detected
↓
Emergency Mode activated
↓
Emergency tab becomes primary
↓
SOS / communication / map / acknowledgment flows become available
↓
Operational status remains visible until user exits or de-escalates
```

### Emergency triggers
- manual SOS button press
- fall detection
- predicted disaster event
- external alert ingestion
- user escalation from Home/Profile

### Emergency-first behavior
- reduce UI clutter
- surface critical actions first
- keep status visible
- prioritize acknowledgment and communication
- preserve offline functionality

---

## 10. App Flow Architecture

### Normal Mode flow
```text
Launch → Home → Lab / MedReady / Profile → return to Home
```

### Emergency Mode flow
```text
Launch or trigger → Emergency Mode → SOS / communication / maps / acknowledgment → remain in operational state or exit safely
```

### Hybrid flow
```text
Normal Mode
→ user configures emergency preferences in Profile
→ Home shows readiness and quick actions
→ Emergency Mode can be entered instantly when needed
```

---

## 11. What Belongs Where

### Home belongs to
- readiness summary
- local risk overview
- quick actions
- simple recommendations
- map preview
- emergency shortcut tiles

### Home should never contain
- full social system
- mesh diagnostics
- long settings flows
- deep analytics
- full learning catalogs

### Lab belongs to
- drills
- training
- quizzes
- progression
- gamification
- leaderboards
- achievements

### Lab should never contain
- emergency broadcast UI
- map configuration
- account configuration
- deep profile settings

### Emergency belongs to
- SOS
- emergency communication
- offline map access
- acknowledgment
- live status
- emergency timeline

### Emergency should never contain
- experimental labels
- technical jargon visible to users
- unrelated training content
- profile setup tasks unless strictly required for safety

### MedReady belongs to
- kit analysis
- medicine readiness
- expiry checks
- preparedness scoring
- supply recommendations

### Profile belongs to
- identity
- configuration
- sync
- accessibility
- notification settings
- emergency communication preferences

---

## 12. Firebase Ownership Areas

Firebase is part of the active product architecture.

### Firebase should own
- authentication
- cloud profile storage
- sync state
- social collaboration data
- leaderboards
- feed events
- notification delivery support
- Firestore-backed emergency/account metadata where appropriate

### Firebase should not own alone
- emergency-only critical runtime decisions
- offline survival logic
- immediate SOS action availability

### Architectural rule
Cloud is supportive. Local and offline systems remain authoritative for emergency-critical behavior.

---

## 13. Offline Data Flow

### Offline-first flow
```text
User action
↓
Local repository/state update
↓
UI reflects change immediately
↓
Cloud sync occurs when available
```

### Offline-first principles
- The app should remain useful without network access.
- Emergency features should not depend on cloud availability.
- Sync should enhance state, not block it.
- Local state should always be able to power the core experience.

---

## 14. Mesh / Emergency Communication Ownership

Mesh-related functionality belongs inside Emergency Mode.

### Product framing
Use human-centered language:
- Emergency Communication
- Offline Emergency Relay
- Nearby Rescue Network

### Hidden technical layer
Internal implementation details may still include transport, nearby discovery, relay logic, queueing, retry/backoff, and diagnostics.

### Ownership rule
The user should experience a survival communication tool, not a debugging or networking subsystem.

---

## 15. UI Ownership Structure

### Recommended ownership pattern
- **Tab owner:** defines what the area is for.
- **ViewModel owner:** manages the feature state.
- **Repository owner:** handles data and sync.
- **Fragment owner:** presents the UI and interactions.
- **Shared components:** supply consistent cards, chips, progress indicators, headers, and states.

### UI system rule
Reusable UI should live in shared component spaces, not scattered across tabs.

---

## 16. Architecture Diagrams for Future Editing

### Current editable shell blueprint
```text
MainActivity
│
├── Home
│   ├── readiness summary
│   ├── risk overview
│   ├── quick actions
│   └── map preview
│
├── Lab
│   ├── daily challenges
│   ├── simulations
│   ├── quick drills
│   ├── interactive learning
│   ├── achievements
│   ├── leaderboards
│   └── progression systems
│
├── Emergency
│   ├── SOS
│   ├── emergency communication
│   ├── offline maps
│   ├── rescue status
│   ├── disaster alerts
│   ├── connectivity status
│   ├── emergency timeline
│   └── quick survival tools
│
├── MedReady
│   ├── kit scanner
│   ├── inventory analysis
│   ├── expiry tracking
│   ├── readiness scoring
│   └── recommendations
│
└── Profile
    ├── identity
    ├── sync
    ├── accessibility
    ├── notification settings
    ├── connectivity preferences
    └── emergency behavior configuration
```

### User-state flow diagram
```text
Settings + Identity
→ Normal Mode features
→ Preparedness results
→ Home summaries
→ Emergency Mode readiness
→ Emergency operational response
```

### Feature ownership tree
```text
SafeReady
├── Home: awareness
├── Lab: preparedness growth
├── Emergency: survival operations
├── MedReady: supply intelligence
└── Profile: configuration and personalization
```

---

## 17. Future Editability Rules

This architecture should remain easy to change later.

### Future changes that should be easy
- move a feature between tabs
- split a tab into sub-flows
- add a new tab
- remove an old experimental screen
- expand Emergency Mode
- add wearable support
- add smarter offline tools
- merge or split feature modules

### To preserve editability
- keep ownership boundaries explicit
- keep shared UI components centralized
- avoid naming features after implementation details
- keep mode logic separate from content logic
- document ownership before moving code

---

## 18. Architectural Guardrails

### Do
- keep the app emergency-first
- keep offline-first behavior intact
- keep tabs purposeful
- keep settings in Profile
- keep survival tools in Emergency
- keep growth tools in Lab
- keep awareness in Home
- keep supply intelligence in MedReady

### Do not
- scatter emergency tools across unrelated tabs
- put technical infrastructure names in the UI
- turn Profile into a miscellaneous dumping ground
- make Home too heavy
- make Lab replace Emergency
- hide critical actions behind deep navigation

---

## 19. Summary

`ARCHITECTURE2.md` is the master editable blueprint for SafeReady’s product ecosystem.

It defines:
- where every feature belongs
- which tab owns which systems
- how the user flows through the app
- how Normal Mode and Emergency Mode relate
- how cloud, offline, and emergency systems interact
- how to restructure the app later without architectural confusion

The end goal is a modular, premium, emergency-first product that feels like an operating system for preparedness and response.

