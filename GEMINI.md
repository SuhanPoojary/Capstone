# AGENT.md — SafeReady AI Development Instructions

**Project:** SafeReady · **Platform:** Android (Kotlin) · **Architecture:** MVVM + Repository
**Package root:** `com.example.capstone` · **Status:** Active development, mid-migration

This file is not a project description — it's an operating manual for any AI (Gemini, Claude, Copilot, etc.) working on this codebase. `ARCHITECTURE.md`, `PLAN.md`, and `UI_DESIGN_SYSTEM.md` describe *what the product is*. This file governs *how the AI is allowed to act on that information*.

---

## 1. What SafeReady Is

SafeReady is an offline-first emergency preparedness and disaster-response platform. It is **not** a generic learning app, not a chatbot app, not a social app.

It operates in two states:

| State | Purpose | Owns |
|---|---|---|
| **Normal Mode** | Preparedness, learning, planning | Home, Lab, MedReady, Profile |
| **Emergency Mode** | Real-time survival operations | SOS, emergency communication, offline maps, survivor status |

Emergency Mode always takes priority. Every decision should be weighed against: *does this help someone stay prepared, or stay alive?*

---

## 2. Read Order — Do This Before Touching Code

1. `AGENT.md` (this file)
2. `UI_DESIGN_SYSTEM.md`
3. `ARCHITECTURE.md`
4. `PLAN.md`
5. `progress.md` (if present)
6. The actual codebase — search for existing implementations before writing anything new

Then: search for existing screens, ViewModels, and repositories that already cover the request. Extend them. Do not recreate them.

---

## 3. Documentation Priority When Sources Disagree

```
1. UI_DESIGN_SYSTEM.md   (visual + interaction truth)
2. ARCHITECTURE.md       (system + ownership truth)
3. PLAN.md                (roadmap + historical context)
4. Current codebase
5. Your own assumptions  (lowest priority — flag, don't invent)
```

All three source docs explicitly acknowledge they were merged from older, conflicting files and may still disagree with each other or with the live code. When you hit a contradiction not listed in Section 4 below, **stop and ask** rather than picking a side silently.

---

## 4. Known Conflicts — Resolve Before Acting, Don't Paper Over Them

These are real, current contradictions between the docs. Do not "helpfully" resolve them on your own by guessing — confirm against the actual codebase first.

**4.1 — Fragment naming mismatch (high risk of duplicate screens).**
PLAN.md's Phase 1 ("Completed") shipped `HomeFragment`, `TrainingFragment`, `ProgressFragment`, `AssistantFragment`, `ProfileFragment`. But the target shell in ARCHITECTURE.md / UI_DESIGN_SYSTEM.md is `HomeFragment`, `LabFragment`, `MedReadyFragment`, `ProfileFragment` — with no `TrainingFragment`, no `ProgressFragment`, and no `AssistantFragment` as a tab at all (the AI Assistant becomes a floating orb, see §10). Before creating `LabFragment` or `MedReadyFragment`, check whether `TrainingFragment` or `ProgressFragment` already exist under those old names and need renaming/merging rather than duplicating.

**4.2 — Emergency: tab or not a tab?**
ARCHITECTURE.md's ecosystem diagram lists Emergency as one of five top-level systems and even shows an `EmergencyFragment` in the `NavHostFragment` graph. UI_DESIGN_SYSTEM.md is explicit that the bottom nav bar has only **4 visible tab icons** (Home, Lab, MedReady, Profile) plus an **empty center slot** holding a floating SOS button — Emergency is **never a bottom-nav icon**. Resolution: Emergency is a real `NavHostFragment` destination (`navigation_emergency`) reached only by tapping the floating SOS button, never rendered as a 5th tab item in the bottom navigation menu. If you see Emergency added as a `BottomNavigationView` menu item, that's wrong — fix it, don't extend it.

**4.3 — Activity shell vs. Fragment shell.**
The original flow (`SplashActivity → OnboardingActivity → LoginActivity/SignupActivity → DashboardActivity → StartLearningActivity → DisasterDetailActivity → FullscreenPlayerActivity`) is legacy and Activity-based. `MainActivity` is becoming the host for the newer Fragment + Navigation Component shell. Before modifying either, check the actual codebase to see how much of the Fragment shell is really built vs. still a target — don't assume.

**4.4 — `DashboardActivity` vs. `HomeFragment`.**
Open question, unresolved in the docs themselves: are these the same screen mid-replacement, two coexisting screens, or a planned future merge? Don't assume either supersedes the other — check the codebase and ask if unclear.

**4.5 — Firebase is wired but not provisioned.**
The SDK, `google-services.json`, dependencies, and `SafeReadyApp.kt` initialization are done. The Firestore collections (`users`, `progress`, `analytics`, `leaderboards`, `config`) and security rules are **specified but not yet created in the Firebase Console**, and Firestore is still planned to start in Test mode. Treat any Firestore-backed feature as **not yet backed by real persisted cloud data** unless you've verified otherwise in the console or codebase. Don't write code that silently assumes cloud sync already works end-to-end.

**4.6 — No Room database exists yet**, despite mesh-stabilization plans (Phase 8) calling for Room-backed message caching. Current local persistence is `SharedPreferences` via repository abstractions, plus `Intent` extras for screen-to-screen state, plus internal file storage for crash logs only. Don't assume Room is integrated — check before writing DAO code.

---

## 5. Architecture Rules

Mandatory layering:

```
UI (Fragment/Activity)
   ↓
ViewModel
   ↓
Repository
   ↓
Data Source (local prefs / Firebase / mesh transport / media resources)
```

**Never:**
- Firebase or Firestore calls inside Activities or Fragments
- Business logic inside XML-bound UI code
- Direct `SharedPreferences` access from a Fragment (go through a repository)
- Networking calls directly from an Activity

**Always:**
- MVVM with `ViewModel` owning screen state
- Repository pattern as the single source of truth per domain
- Coroutines for async work
- Lifecycle-aware observation (`viewLifecycleOwner`, not `this`, inside Fragments)

### Target package structure
```
com.example.capstone
├── data
│   ├── local
│   │   ├── preferences
│   │   ├── database
│   │   └── model
│   ├── remote
│   └── repository
├── domain
│   ├── model
│   ├── usecase
│   └── interface
├── presentation
│   ├── activity
│   ├── fragment
│   ├── viewmodel
│   └── uiState
├── service
├── util
└── app
```
Follow this whenever you touch a file, even if the surrounding code hasn't fully migrated yet. Don't invent a parallel structure.

---

## 6. Navigation Rules

```
[ Home ] [ Lab ] [ EMPTY ] [ MedReady ] [ Profile ]
                    ↑
        Floating SOS button lives here, elevated above the bar
```

- 5 slots, but only **4 are tab items**. The center slot is reserved space for the SOS FAB — it is structurally empty in the nav menu, never a clickable tab.
- SOS button: 64dp circle, `#D91F32`, continuous pulse (scale 1.0→1.08→1.0, 1200ms loop), tapping it navigates to the Emergency destination via the `NavController` — it is not wired through `NavigationUI.setupWithNavController`.
- Bottom nav bar: white, 64dp height, 24dp top corner radius, 16dp side margins, 16dp bottom margin, 8dp elevation.
- Every scrollable fragment must set `android:paddingBottom="@dimen/fragment_bottom_padding"` (88dp) and `android:clipToPadding="false"` so content clears the floating nav.
- The AI Assistant is never a navigation destination reached via bottom nav (see §10).
- Do not add a 6th tab, ever, for any feature.

---

## 7. Screen / Tab Ownership

| Tab | Owns | Must never own |
|---|---|---|
| **Home** | Readiness summary, local risk overview, quick actions, recommendations, map preview, weather, emergency shortcut entry points | Deep learning modules, full social systems, mesh diagnostics, long settings flows, quizzes |
| **Lab** | Lessons, simulations, drills, quizzes, XP, streaks, badges, achievements, leaderboards, progression | Emergency broadcast UI, map configuration, account configuration |
| **MedReady** | OCR scanning, kit/medicine analysis, expiry tracking, readiness scoring, gap analysis, supply recommendations | Anything Home, Lab, or Profile owns |
| **Profile** | Identity, emergency contacts, notification/accessibility/language settings, offline map downloads, mesh & emergency *preferences* (configuration only) | Executing emergency systems — Profile configures, it does not run them |
| **Emergency** (destination, not a tab) | SOS broadcasting, emergency communication, offline maps, rescue acknowledgment, survivor status, emergency timeline | Training content, decorative UI, anything that adds friction |

Cross-module rule: Lab's progression feeds Home's summary cards; MedReady's readiness score feeds Home; Profile's settings are read by Emergency Mode at runtime. The UI should react to state owned elsewhere — it should not duplicate that ownership.

---

## 8. Design Tokens — Use These, Invent Nothing

> Source of truth: `UI_DESIGN_SYSTEM.md` v2.0. These are already implemented in `colors.xml` / `dimens.xml` / `styles.xml`. Reference via `@color/`, `@dimen/`, `@style/` — never hardcode a hex value, a raw dp number, or a font name in layout XML or Kotlin.

### Colors (semantic tokens)
```
Backgrounds
  bg_app             #F7F2EA   ← Home / Lab / MedReady / Profile background
  bg_card            #FFFFFF
  bg_hero            #123F78   ← navy gradient hero sections
  bg_emergency_dark  #041631   ← Emergency Mode background ONLY

Text
  text_primary       #0B2856
  text_secondary      #6B7280
  text_tertiary       #9CA3AF
  text_inverse        #FFFFFF

Status
  status_success      #2EA58D
  status_warning       #F2A65A
  status_danger        #D91F32
  status_info          #3B82F6

Navigation
  nav_active           #0B2856
  nav_inactive          #9CA3AF
  nav_sos               #D91F32
```
Navy hero gradient: `#0E4B93 → #123F78 → #1A2D5C`, 270° (top to bottom).

### Typography
Font: **Plus Jakarta Sans only** — weights 400 / 500 / 600 / 700 / 800. Poppins and Roboto are removed; never reference them in new layouts.

Use the predefined style tokens, never set `textSize`/`fontFamily`/`textColor` ad hoc:
```
TextStyle.Display.XLarge / .Large / .Medium / .Small
TextStyle.Heading.Large / .Medium / .Small
TextStyle.Body.Large / .Medium / .Small
TextStyle.Label.Large / .Medium / .Small
```

### Spacing (base unit 4dp)
```
xs=4  sm=8  md=12  lg=16(default)  xl=20  2xl=24  3xl=32  4xl=40
fragment_bottom_padding = 88dp (mandatory on every scrollable screen)
```

### Corner radius
```
sm=8  md=12  lg=16(default for cards)  xl=20  2xl=24(nav bar top corners)  full=pill
```

### Elevation
```
light=2(standard cards)  medium=4(hero cards)  high=8(SOS button, nav bar)  top=12(modals)
```

---

## 9. Component Rules

Reuse named styles — don't redefine a card or button with raw attributes:

```
Cards:        Card.Elevated · Card.Hero · Card.Alert · Card.Emergency · Card.Dark · Card.Stat
Buttons:      Button.Primary · Button.Secondary · Button.Alert · Button.Success · Button.Ghost · Button.Pill · Button.Challenge
Progress:     ProgressBar.Standard · ProgressBar.Large · ProgressBar.XP · ProgressBar.Streak
Status chips: Chip.Status.Safe / .Injured / .Trapped / .NeedWater / .NeedFood / .NeedMedical (56dp height, 12dp corner)
Badges:       Badge.Easy · Badge.Medium · Badge.Hard · Badge.Daily · Badge.Live · Badge.Active
```

If a screen needs something none of these cover, propose a new named style rather than inlining attributes — and flag it as a design-system addition, not a one-off hack.

Icons: SVG vector drawables only. No PNG icons, no emoji as UI elements anywhere.

---

## 10. AI Assistant Rules

The AI Assistant is **not a bottom-nav tab and not a fragment destination reached via tab bar**.

```
Trigger:    Floating orb, bottom-right corner, 52dp circle, navy gradient, sparkle icon
Behavior:   Tap → bottom-sheet chat overlay, 70% screen height, 24dp rounded top corners
Visible on: Home, Lab, MedReady, Profile
Hidden on:  Emergency Mode
```

Do not fake AI output. If the backend isn't ready, show a "Coming Soon" state — never fabricate OCR results, kit analysis, or emergency recommendations to make a screen look finished.

---

## 11. Emergency Mode Rules

Emergency Mode is the highest-priority subsystem and uses a **fully separate dark theme** — never the cream `bg_app` background.

```
Background:      #041631
Card background: #0D2137
Text primary:    #FFFFFF
Text secondary:  #A0AEC0
```

**Never:**
- Hide or bury SOS actions behind navigation
- Require internet connectivity for core SOS/status functions
- Require Firebase or authentication to broadcast SOS or set survivor status
- Drop touch targets below 56dp or text below 14sp inside Emergency Mode

**Always:** offline-first, minimal steps to act, calm visual hierarchy, status always visible, an always-reachable "Exit Emergency Mode" pill at the bottom.

### Mesh networking — naming discipline
Mesh transport, packet relay, routing, and queue/retry internals are implementation detail and must never reach user-facing copy. Use: "Emergency Communication," "Offline Emergency Relay," "Nearby Rescue Network," "Nearby Survivors." Never expose "Mesh Debug," "Packet Relay," "Transport Layer," or similar terms to end users. Developer-only surfaces (`MeshDebugActivity`, `MeshDebugFragment`) stay developer-only and must not leak into production user flows.

---

## 12. Firebase & Data Rules

**Firebase owns:** authentication, user profiles, cloud progress sync, presence, leaderboards, social systems, analytics.

**Firebase must never:**
- Be the sole gate for emergency-critical actions (SOS must work without it)
- Store passwords locally as a substitute for real auth
- Be duplicated by a second, parallel auth system

**Local-first principle:** local repository state updates immediately on user action; cloud sync is supportive, applied opportunistically, and never blocks the UI. During an emergency, local state is authoritative — cloud is secondary, full stop.

Remember §4.5: Firestore collections and rules are specified, not yet provisioned. Don't write code that assumes a `users` or `progress` collection already exists in production until verified.

---

## 13. Accessibility Rules

| Requirement | Target |
|---|---|
| Minimum touch target | 48dp (56dp inside Emergency Mode) |
| Normal text contrast | ≥ 4.5:1 (WCAG AA) |
| Large text contrast (18sp+ bold) | ≥ 3:1 |
| Color as sole indicator | Never — always pair with icon or text |
| Text scaling | Support up to 200% |
| TalkBack | Full support, logical reading order |
| Content descriptions | Required on every icon/image |
| Heading hierarchy | No skipped levels |

---

## 14. Error Handling, Logging, Performance

- Always log failures and state changes; always give the user feedback and a retry path; never silently swallow an exception or crash intentionally.
- Never log passwords, tokens, or secrets.
- Avoid nested `RecyclerView`s, redundant observers, duplicate network calls, and main-thread blocking work. Use coroutines, caching, and lazy loading where it matters.
- Don't fake unfinished backend features — use skeleton/loading/"coming soon" states instead (applies to AI Assistant, MedReady analysis, and anything else not yet wired to real data).

---

## 15. Before You Change Any File — Checklist

1. Have I read AGENT.md, UI_DESIGN_SYSTEM.md, ARCHITECTURE.md, PLAN.md (and progress.md if present)?
2. Does this conflict with anything in Section 4? If yes, stop and confirm against the codebase before proceeding.
3. Does an existing Fragment, ViewModel, or Repository already cover this — possibly under an old name (§4.1)?
4. Am I using existing design tokens and component styles instead of inventing new ones?
5. Does this respect tab ownership (§7) and Emergency Mode's separate theme (§11)?
6. If this touches Firebase, have I checked whether the relevant collection actually exists yet (§4.5, §12)?

---

## 16. Development Commandments

1. Preserve Emergency Mode above all else.
2. Preserve offline-first behavior — nothing emergency-critical depends on a network.
3. Preserve MVVM + Repository architecture.
4. Follow UI_DESIGN_SYSTEM.md exactly — no new colors, fonts, spacing, or navigation patterns.
5. Search for and extend existing implementations before creating new screens, ViewModels, or repositories.
6. Never duplicate a repository, ViewModel, or fragment that already exists under a different (possibly legacy) name.
7. Never add a 6th nav tab, and never turn Emergency into a bottom-nav icon.
8. Never move emergency functionality outside Emergency Mode, or vice versa.
9. Keep mesh networking terminology human-facing — never expose transport-layer language to users.
10. Never fake AI results, OCR output, or analysis — use honest loading/coming-soon states.
11. Accessibility (48dp/56dp targets, WCAG AA contrast, TalkBack) is not optional.
12. When documentation conflicts and it isn't already covered in Section 4, stop and ask rather than guessing.

---
