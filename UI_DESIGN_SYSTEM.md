# SafeReady — UI Design System

## Version 2.0 | May 2026

> **This is the single source of truth** for all SafeReady UI decisions.
> Based on the latest Figma screens. All previous docs (`DESIGN_SYSTEM.md`, `FONT_SETUP.md`, `UI_IMPLEMENTATION_GUIDE.md`, `UI_REDESIGN_PROGRESS.md`) are superseded by this file.
> The implemented colors and tokens in this document are **already live in the app** — do not change them without a deliberate design decision.

---

## TABLE OF CONTENTS

1. [Core Principles](#1-core-principles)
2. [Color Palette](#2-color-palette)
3. [Typography System](#3-typography-system)
4. [Font Setup](#4-font-setup)
5. [Spacing System](#5-spacing-system)
6. [Corner Radius System](#6-corner-radius-system)
7. [Elevation & Shadow System](#7-elevation--shadow-system)
8. [Component Styles](#8-component-styles)
9. [Navigation System](#9-navigation-system)
10. [Screen Layouts — Home](#10-screen-layout--home)
11. [Screen Layouts — Lab](#11-screen-layout--lab-survival-lab)
12. [Screen Layouts — MedReady](#12-screen-layout--medready)
13. [Screen Layouts — Profile](#13-screen-layout--profile)
14. [Screen Layouts — Emergency Mode](#14-screen-layout--emergency-mode)
15. [Future: AI Assistant](#15-future-ai-assistant)
16. [Future: Mesh Network UI](#16-future-mesh-network-ui)
17. [Animation Guidelines](#17-animation-guidelines)
18. [Implementation Guide](#18-implementation-guide)
19. [Theming & Style Application](#19-theming--style-application)
20. [ViewModel Binding Examples](#20-viewmodel-binding-examples)
21. [Accessibility Requirements](#21-accessibility-requirements)
22. [File Structure](#22-file-structure)
23. [Implementation Progress](#23-implementation-progress)
24. [Remaining Tasks](#24-remaining-tasks)
25. [Troubleshooting](#25-troubleshooting)
26. [Do's and Don'ts](#26-dos-and-donts)
27. [Quick Reference Card](#27-quick-reference-card)

---

## 1. Core Principles

SafeReady's UI is built for **calm clarity under stress**. Every decision supports a user who may be anxious, in a hurry, or in an actual emergency.

| Principle | What It Means |
|-----------|---------------|
| Calm Interface | Soft backgrounds, gentle transitions, no aggressive CTA patterns |
| Modern & Clean | Rounded cards, ample white space, navy + cream palette |
| Minimal Clutter | Content-first, no unnecessary decoration or emoji icons |
| Highly Readable | Strong type hierarchy, WCAG AA contrast on all text |
| Stress-Friendly | Large tap targets (48dp+), clear feedback, works in panic |
| Gamified & Motivating | XP, streaks, ranks keep users engaged in preparedness |

---

## 2. Color Palette

> ⚠️ These values are **already implemented** in `colors.xml`. Do not modify without a design decision. Always reference via `@color/` tokens — never hardcode hex.

### 2.1 App Background

```
bg_app:   #F7F2EA   ← Warm cream. Used on Home, Lab, MedReady, Profile, Training, Progress screens.
```

### 2.2 Navy Gradient (Hero Sections)

Used on: Profile hero, Preparedness/Emergency Readiness card, Lab hero, any dark header card.

```
Gradient Start:  #0E4B93
Gradient Center: #123F78
Gradient End:    #1A2D5C
Direction: top → bottom (angle 270°)
```

XML drawable:
```xml
<gradient
    android:startColor="#0E4B93"
    android:centerColor="#123F78"
    android:endColor="#1A2D5C"
    android:angle="270"/>
```

### 2.3 Navy Scale (Cards, Borders, Tints)

```
color_navy_900: #1A2847   ← Deep emergency bg (legacy, prefer bg_emergency_dark)
color_navy_800: #22355B   ← Dark navy accents
color_navy_700: #2A3E6E
color_navy_600: #3A4F7F
color_navy_300: #A5C3E7
color_navy_200: #C3D7EF
color_navy_100: #E1EBF7   ← Chip/badge backgrounds
color_navy_50:  #F0F5FB   ← Very light navy tint
```

### 2.4 Gray Scale (Text, Borders, Dividers)

```
color_gray_900: #1F2937   ← (legacy; prefer text_primary)
color_gray_800: #374151
color_gray_700: #4B5563
color_gray_600: #6B7280   ← (legacy; prefer text_secondary)
color_gray_500: #9CA3AF
color_gray_400: #D1D5DB   ← Borders
color_gray_300: #E5E7EB   ← Light borders
color_gray_200: #F3F4F6   ← Input backgrounds
color_gray_100: #F9FAFB   ← Dividers
```

### 2.5 Accent Colors

```
Emergency Red (SOS button, alerts):
  color_emergency:   #D91F32
  color_red_600:     #DC2626
  color_red_700:     #B91C1C
  color_red_50:      #FEF2F2  ← alert card background
  color_red_100:     #FEE2E2  ← alert card border tint

Success / Safe Green:
  status_success:    #2EA58D
  color_green_600:   #059669
  color_green_50:    #F0FDF4
  color_green_100:   #DCFCE7

Warning / Streak Orange:
  status_warning:    #F2A65A
  color_orange_500:  #F59E0B
  color_orange_200:  #FCD8B5
  color_orange_50:   #FEF8F0

Info Blue:
  status_info:       #3B82F6
```

### 2.6 Semantic Tokens (Always Use These in Code)

```
─── Backgrounds ────────────────────────────────────────
bg_app:              #F7F2EA   ← All standard screens
bg_card:             #FFFFFF   ← Card surfaces
bg_card_elevated:    #F9FAFB   ← Slightly elevated cards
bg_hero:             #123F78   ← Hero/gradient sections (mid-point)
bg_emergency_dark:   #041631   ← Emergency Mode full background

─── Text ───────────────────────────────────────────────
text_primary:        #0B2856   ← All main body/heading text
text_secondary:      #6B7280   ← Subtitles, captions, metadata
text_tertiary:       #9CA3AF   ← Placeholder, hint text
text_inverse:        #FFFFFF   ← On dark/navy backgrounds

─── Status ─────────────────────────────────────────────
status_success:      #2EA58D
status_warning:      #F2A65A
status_danger:       #D91F32
status_info:         #3B82F6

─── Navigation ─────────────────────────────────────────
nav_active:          #0B2856   ← Active tab icon + label
nav_inactive:        #9CA3AF   ← Inactive tab icon + label
nav_background:      #FFFFFF   ← Bottom nav bar surface
nav_sos:             #D91F32   ← Center SOS button
```

---

## 3. Typography System

### 3.1 Font Family

**Plus Jakarta Sans** — used across the **entire app**, every screen, every component.

| Weight | Value | Use |
|--------|-------|-----|
| Regular | 400 | Body text, captions |
| Medium | 500 | Labels, nav items |
| SemiBold | 600 | Card titles, section headers |
| Bold | 700 | Screen titles, key numbers |
| ExtraBold | 800 | Hero stats, large display numbers (XP, %, level) |

> Poppins is **removed**. Do not reference it in any new layout or style.

### 3.2 Type Scale

#### Display (Hero Stats, Big Numbers)
```
text_display_xl: 40sp ExtraBold  ← XP count, hero percentage
text_display_lg: 32sp Bold       ← Page title, hero header
text_display_md: 28sp Bold       ← Large section headers
text_display_sm: 24sp Bold       ← Section headers
```

#### Heading
```
text_heading_lg: 22sp Bold       ← Major section headers ("Survival Lab")
text_heading_md: 20sp SemiBold   ← Section headers
text_heading_sm: 18sp SemiBold   ← Card titles ("Earthquake Response Drill")
```

#### Body
```
text_body_lg: 16sp Regular       ← Primary content
text_body_md: 14sp Regular       ← Secondary content, descriptions
text_body_sm: 12sp Regular       ← Helper text, captions, issued dates
```

#### Label / Badge
```
text_label_lg: 13sp Medium       ← Small labels, metadata rows
text_label_md: 12sp Medium       ← Badges, chip text, nav labels
text_label_sm: 11sp Medium       ← Tiny labels (e.g. "DAILY CHALLENGE" pill)
```

### 3.3 TextStyle References (XML)
```
TextStyle.Display.XLarge
TextStyle.Display.Large
TextStyle.Display.Medium
TextStyle.Heading.Large
TextStyle.Heading.Medium
TextStyle.Heading.Small
TextStyle.Body.Large
TextStyle.Body.Medium
TextStyle.Body.Small
TextStyle.Label.Large
TextStyle.Label.Medium
TextStyle.Label.Small
```

---

## 4. Font Setup

### Option 1: Downloadable Font (Recommended for Development)

Place in `res/font/plus_jakarta_sans.xml` using the Google Fonts provider. No `.ttf` files needed.

Requires:
- `minSdk` compatible with downloadable fonts
- Google Play services on device

### Option 2: Bundled Font Files (Required for Offline / Production Safety)

Download Plus Jakarta Sans from Google Fonts. Place files at:
```
res/font/plus_jakarta_sans_regular.ttf
res/font/plus_jakarta_sans_medium.ttf
res/font/plus_jakarta_sans_semibold.ttf
res/font/plus_jakarta_sans_bold.ttf
res/font/plus_jakarta_sans_extrabold.ttf
```

Font family XML (`res/font/plus_jakarta_sans.xml`):
```xml
<?xml version="1.0" encoding="utf-8"?>
<font-family xmlns:android="http://schemas.android.com/apk/res/android">
    <font android:fontStyle="normal" android:fontWeight="400"
          android:font="@font/plus_jakarta_sans_regular" />
    <font android:fontStyle="normal" android:fontWeight="500"
          android:font="@font/plus_jakarta_sans_medium" />
    <font android:fontStyle="normal" android:fontWeight="600"
          android:font="@font/plus_jakarta_sans_semibold" />
    <font android:fontStyle="normal" android:fontWeight="700"
          android:font="@font/plus_jakarta_sans_bold" />
    <font android:fontStyle="normal" android:fontWeight="800"
          android:font="@font/plus_jakarta_sans_extrabold" />
</font-family>
```

> Run a **clean rebuild** after adding bundled font files.

---

## 5. Spacing System

Base unit: **4dp**

```
spacing_xxs: 2dp
spacing_xs:  4dp
spacing_sm:  8dp
spacing_md:  12dp
spacing_lg:  16dp   ← default screen and card padding
spacing_xl:  20dp
spacing_2xl: 24dp
spacing_3xl: 32dp
spacing_4xl: 40dp
spacing_5xl: 48dp
```

### Component-Specific Spacing
```
screen_padding_horizontal:  16dp
screen_padding_vertical:    16dp
card_padding_horizontal:    16dp
card_padding_vertical:      20dp
card_inner_spacing:         12dp
section_spacing:            24dp
fragment_bottom_padding:    88dp   ← must clear the floating nav bar
item_spacing:               12dp
stat_card_padding:          16dp
```

---

## 6. Corner Radius System

```
corner_none:  0dp
corner_xs:    4dp
corner_sm:    8dp
corner_md:    12dp
corner_lg:    16dp   ← default for all cards
corner_xl:    20dp
corner_2xl:   24dp   ← bottom nav bar top corners
corner_full:  9999dp ← pill buttons, chips, badges
```

---

## 7. Elevation & Shadow System

```
elevation_none:   0dp
elevation_light:  2dp   ← standard cards
elevation_medium: 4dp   ← hero cards, prominent sections
elevation_high:   8dp   ← floating SOS button, nav bar
elevation_top:    12dp  ← modal overlays
```

Shadow spec: `#000000` at 15–20% alpha, standard Material shadow rendering. Do not implement custom shadow drawables.

---

## 8. Component Styles

### 8.1 Cards

| Style | Background | Border | Radius | Elevation | Use |
|-------|-----------|--------|--------|-----------|-----|
| `Card.Elevated` | #FFFFFF | none | 16dp | 2dp | Standard content cards |
| `Card.Hero` | Navy gradient | none | 16dp | 4dp | Preparedness card, Lab hero |
| `Card.Alert` | #FEF8F0 | 1dp #FCD8B5 | 16dp | 0dp | Risk/warning cards |
| `Card.Emergency` | #FEF2F2 | 2dp #D91F32 | 16dp | 0dp | Emergency alerts |
| `Card.Dark` | #041631 | none | 16dp | 4dp | Emergency Mode cards |
| `Card.Stat` | #FFFFFF | none | 12dp | 2dp | Stat grid items (Level, XP, Streak) |

### 8.2 Buttons

| Style | Background | Text | Height | Radius | Use |
|-------|-----------|------|--------|--------|-----|
| `Button.Primary` | #0B2856 (navy) | #FFFFFF SemiBold | 48dp | 16dp | Primary actions |
| `Button.Secondary` | transparent | #0B2856 SemiBold, border | 48dp | 16dp | Secondary actions |
| `Button.Alert` | #D91F32 | #FFFFFF SemiBold | 48dp | 16dp | SOS, emergency confirm |
| `Button.Success` | #2EA58D | #FFFFFF SemiBold | 48dp | 16dp | Safe status confirm |
| `Button.Ghost` | transparent | #FFFFFF Medium | 48dp | 16dp | On dark backgrounds |
| `Button.Pill` | #2A2A3A | #FFFFFF SemiBold | 48dp | full | Exit Emergency Mode pill |
| `Button.Challenge` | #FFFFFF | #0B2856 SemiBold | 48dp | full | "Start Challenge" in red card |

### 8.3 Progress Bars

| Style | Height | Track | Fill | Use |
|-------|--------|-------|------|-----|
| `ProgressBar.Standard` | 8dp | #E5E7EB | #2EA58D or navy gradient | Preparedness journey, readiness |
| `ProgressBar.Large` | 12dp | navy_600 | #F2A65A | Hero card readiness bar |
| `ProgressBar.XP` | 6dp | #FFFFFF 30% alpha | #FFFFFF | Lab XP progress on dark bg |
| `ProgressBar.Streak` | 8dp | #E5E7EB | #F2A65A | Weekly streak bar |

### 8.4 Status / Survivor Chips

```
Chip.Status.Safe:        bg #2EA58D,  text white, checkmark icon
Chip.Status.Injured:     bg #1E2A3A,  text white, plus icon
Chip.Status.Trapped:     bg #1E2A3A,  text white, warning icon
Chip.Status.NeedWater:   bg #1E2A3A,  text white, water icon
Chip.Status.NeedFood:    bg #1E2A3A,  text white, food icon
Chip.Status.NeedMedical: bg #1E2A3A,  text white, medical icon
```

All status chips: height 56dp, corner 12dp, icon left-aligned, label below icon, full-width tap area.

### 8.5 Input Fields

```
style: InputField.Primary
Height:        48dp
Background:    #F3F4F6
Border:        1dp #E5E7EB (focus: 1dp #0B2856)
Corner radius: 12dp
Text:          text_primary, body_lg
Hint:          text_tertiary
```

### 8.6 Badges & Difficulty Pills

```
Badge.Easy:     bg #DCFCE7, text #059669
Badge.Medium:   bg #FEF8F0, text #D97706
Badge.Hard:     bg #FEE2E2, text #DC2626
Badge.Daily:    bg rgba(255,255,255,0.2), text white  ← "DAILY CHALLENGE" pill on red card
Badge.Live:     bg #2EA58D, text white                ← "Live" chip on map
Badge.Active:   bg #D91F32, text white + pulse dot     ← "ACTIVE" on Emergency Mode
```

---

## 9. Navigation System

### 9.1 Structure

The nav bar has **5 slots**. The center slot is reserved for the floating SOS button — it is **not** a navigation tab.

```
[ Home ] [ Lab ] [ EMPTY ] [ MedReady ] [ Profile ]
                    ↑
              SOS Button floats here, elevated above the bar
```

### 9.2 Bottom Nav Bar

```
Background:      #FFFFFF (white card surface)
Height:          64dp
Top corners:     24dp radius (rounded, floating card look)
Side margins:    16dp left and right
Bottom margin:   16dp from screen bottom
Elevation:       8dp
```

Tab item specs:
```
Active icon + label color:   nav_active (#0B2856)
Inactive icon + label color: nav_inactive (#9CA3AF)
Active indicator:            3dp underline, nav_active color
Label style:                 TextStyle.Label.Medium
Transition:                  150ms fade
```

### 9.3 Floating SOS Button

```
Shape:        Circle
Size:         64dp diameter
Background:   #D91F32 (emergency red)
Icon:         ic_sos.xml — white, 28dp
Elevation:    8dp + drop shadow
Position:     Centered horizontally, sitting above the nav bar
              (vertically overlaps nav bar top edge by ~20dp)
Animation:    Continuous pulse — scale 1.0 → 1.08 → 1.0, 1200ms loop
```

> The SOS button is **always visible** on all standard screens. It is **not** a nav tab. Tapping it enters Emergency Mode.

### 9.4 Fragment Bottom Padding

Every scrollable fragment **must** set:
```xml
android:paddingBottom="@dimen/fragment_bottom_padding"   <!-- 88dp -->
android:clipToPadding="false"
```

---

## 10. Screen Layout — Home

### Visual Reference
Screen shows: warm cream background, navy gradient hero card, white content cards, floating red SOS button.

### Sections (top to bottom)

**1. Header**
```
Top padding:    16dp
Layout:         Horizontal row
Left:           Greeting label ("Good Morning,") — TextStyle.Body.Medium, text_secondary
                User name ("Alex") — TextStyle.Display.Medium, text_primary (bold, large)
                Location row: pin icon + "Mumbai, Maharashtra" — TextStyle.Body.Small, text_secondary
Right:          Weather chip — navy_100 background, cloud icon, "28°C" — TextStyle.Label.Medium
```

**2. Emergency Readiness Hero Card**
```
Style:          Card.Hero (navy gradient)
Margin top:     20dp
Content:
  - Label row:    "Emergency Readiness" — TextStyle.Label.Small, ExtraBold, orange/amber color
                  Achievement icon (badge icon) — 40dp, navy_600 tinted rounded square
  - Percentage:   "78%" — TextStyle.Display.XLarge, ExtraBold, white
  - Progress bar: ProgressBar.Large, orange fill, navy_600 track
  - Subtext:      "Complete 3 more drills to reach Expert level"
                  TextStyle.Body.Small, white 70% alpha
```

**3. Risk Alert Card**
```
Style:          Card.Alert (orange tint)
Margin top:     16dp
Content:
  - Left:   Warning triangle icon (orange, 24dp) + vertical divider
  - Middle: "Moderate Risk Level" — TextStyle.Heading.Small, text_primary
            "Heavy rainfall expected this week" — TextStyle.Body.Small, text_secondary
  - Right:  "View" text button — TextStyle.Label.Large, navy, tappable
```

**4. Disaster Map Preview**
```
Style:          Card.Elevated
Margin top:     16dp
Height:         200dp
Content:
  - Section header: "Disaster Map" — TextStyle.Heading.Medium, text_primary
                    "Live" badge (green) and "24h" button row — top right
  - Map area:   Gray placeholder / embedded map view
                "2 Active Alerts" label — TextStyle.Heading.Small, text_primary, centered
                "Flood risk in coastal areas" — TextStyle.Body.Small, text_secondary, centered
  - Controls:   Trend icon button + GPS/locate icon button (top right of map)
```

**5. Quick Actions Grid**
```
Section header: "Quick Actions" — TextStyle.Heading.Medium, text_primary
Margin top:     20dp
Layout:         2×2 grid, 12dp gap

Card 1 — Continue Mission (navy gradient bg):
  Icon: target/mission icon, white
  Title: "Continue Mission" — white, SemiBold
  Subtitle: "Earthquake Drill" — white 70% alpha, small

Card 2 — AI Assistant (white):
  Icon: sparkle/AI icon, navy tint
  Title: "AI Assistant" — text_primary, SemiBold
  Subtitle: "Ask anything" — text_secondary

Card 3 — Emergency SOS (white):
  Icon: warning triangle, red
  Title: "Emergency SOS" — text_primary, SemiBold
  Subtitle: "Instant help" — text_secondary

Card 4 — MedReady (white):
  Icon: lightning/med icon, teal
  Title: "MedReady" — text_primary, SemiBold
  Subtitle: "Kit scanner" — text_secondary
```

**6. Your Progress Section**
```
Section header: "Your Progress" — TextStyle.Heading.Medium, text_primary
Margin top:     24dp

Row 1 — Weekly Streak:
  Label: "Weekly Streak" — TextStyle.Body.Large, text_primary
  Right: fire emoji placeholder icon + "12 days" — TextStyle.Body.Large, Bold, orange
  Progress bar: ProgressBar.Streak, full width, orange fill

Row 2 — Badge Progress:
  Left:   Badge icon (navy tint, 40dp)
  Text:   "Complete 2 more lessons" — TextStyle.Body.Medium, text_primary
          "Unlock 'Disaster Expert' badge" — TextStyle.Body.Small, text_secondary
```

---

## 11. Screen Layout — Lab (Survival Lab)

### Visual Reference
Screen shows: dark navy gradient hero with stats, red daily challenge card, simulation cards, standard cream background below hero.

### Sections (top to bottom)

**1. Hero Section (Dark Navy Gradient)**
```
Background:   Navy gradient (#0E4B93 → #1A2D5C)
Padding:      20dp all sides

Stat row (4 items):
  [ 28 / Level ] [ 3.2K / XP ] [ 🔥 12 / Streak ] [ #45 / Rank ]
  Each stat:
    Value — TextStyle.Display.Medium or Heading.Large, white, ExtraBold
    Label — TextStyle.Label.Medium, white 60% alpha
  XP and Streak use accent colors (amber, orange)

XP Progress bar:
  Label row: "Next level in 800 XP" left, "80%" right — white 70% alpha, body_small
  Bar: ProgressBar.XP (thin, white, on dark bg)
```

**2. Daily Challenge Card (Red)**
```
Background:   #D91F32 gradient (slightly darker edges)
Margin:       16dp horizontal, 16dp top
Corner:       16dp
Content:
  - Pill badge: "⚡ DAILY CHALLENGE" — TextStyle.Label.Small, ExtraBold, white, semi-transparent bg
  - Title: "Earthquake Response Drill" — TextStyle.Heading.Large, white, Bold
  - Subtitle: "Complete the challenge within 24 hours to earn 500 bonus XP"
              TextStyle.Body.Medium, white 80% alpha
  - Metadata row: clock icon "15 min" | lightning icon "500 XP" | target icon "Hard"
                  TextStyle.Label.Large, white 70% alpha
  - Button: "Start Challenge" — Button.Challenge (white pill button, navy text)
```

**3. Disaster Simulations**
```
Section header row: "Disaster Simulations" left + "View All" right (navy link)
                    TextStyle.Heading.Medium

Simulation cards (horizontal scroll or vertical list):
  Each card:
    Background:   Navy gradient or image overlay
    Corner:       16dp
    Tag row:      Difficulty badge + duration ("Medium • 12 min")
    Title:        Simulation name ("Tsunami Escape") — Heading.Small, white
    Progress row: "60% Complete" — Body.Small, white 70%
    XP badge:     "+350 XP" — Label.Large, accent amber, right-aligned
```

**4. Quick Drills Section**
```
Section header: "Quick Drills" + "View All"
Cards: Similar to Simulations but shorter format (single row items with icon, title, duration, XP)
```

**5. Interactive Learning**
```
Section header: "Interactive Learning" + "View All"
Cards: Course-style cards with progress bars
```

**6. Leaderboard Snippet**
```
Section header: "Leaderboard"
Shows top 3 users: rank, avatar, name, XP
Current user row highlighted
"View Full Leaderboard" link
```

**7. Achievement Badges**
```
Section header: "Achievements"
Horizontal scroll row of badge icons
Earned: full color | Unearned: gray/locked
```

---

## 12. Screen Layout — MedReady

### Visual Reference
Screen shows: cream background, large navy scan hero card, white analysis feature cards in a grid.

### Sections (top to bottom)

**1. Header**
```
Title: "MedReady" — TextStyle.Display.Large, text_primary
Subtitle: "AI-powered emergency kit analyzer" — TextStyle.Body.Medium, text_secondary
```

**2. Scan Hero Card**
```
Style:        Card.Hero (navy gradient)
Padding:      32dp
Content:
  - Scan frame icon: rounded square with corner markers, white, 64dp
  - Title: "Scan Your Emergency Kit" — TextStyle.Heading.Large, white, centered
  - Subtitle: "Upload or capture an image of your medicines and supplies for AI analysis"
              TextStyle.Body.Medium, white 80% alpha, centered
  - Buttons row:
      "Take Photo" — Button.Ghost (white outline) with camera icon
      "Upload" — Button.Ghost (white outline, slightly gray) with upload icon
```

**3. What We Analyze Grid**
```
Section header: "What We Analyze" — TextStyle.Heading.Medium, text_primary

2×2 card grid:
  Card 1 — Medicine Detection:
    Icon: box/pill icon, navy tint
    Title: "Medicine Detection" — Heading.Small
    Subtitle: "OCR scanning of labels" — Body.Small, text_secondary

  Card 2 — Expiry Tracking:
    Icon: calendar icon, navy tint
    Title: "Expiry Tracking" — Heading.Small
    Subtitle: "Automated alerts" — Body.Small, text_secondary

  Card 3 — Gap Analysis:
    Icon: checklist icon, navy tint
    Title: "Gap Analysis" — Heading.Small
    Subtitle: "Missing items detection" — Body.Small, text_secondary

  Card 4 — Overall Assessment:
    Icon: shield/score icon, navy tint
    Title: "Readiness Score" — Heading.Small
    Subtitle: "Overall assessment" — Body.Small, text_secondary
```

**4. Previous Scans**
```
Section header: "Previous Scans"
List of past scan results:
  Each row: thumbnail | date | item count | score chip
Empty state: "No scans yet. Scan your kit to get started."
```

**5. Future AI States (do not fake)**
```
Loading:   Skeleton shimmer cards + "Analyzing your kit..." label
Error:     Error card with retry button
Coming Soon sections: Gray "Coming Soon" badge overlay on future features
```

---

## 13. Screen Layout — Profile

### Visual Reference
Screen shows: full-width navy gradient hero with avatar and stats, then white cards on cream background.

### Sections (top to bottom)

**1. Profile Hero (Navy Gradient)**
```
Background:   Navy gradient full width, no side margins
Padding:      20dp sides, 24dp top, 32dp bottom
Content:
  Row 1: Avatar circle (gray, initials "A", 56dp) | Name "Alex Chen" Heading.Large white Bold
                                                   | Email body_small white 70%
                                                   | "Edit" button (ghost, small, white outline)
  Row 2 (stat cards, 3 columns):
    [Badge icon | 28 | Level]
    [Flame icon | 12 | Day Streak]
    [Badge icon | 45 | Badges]
    Each: white rounded card (corner_md), navy_100 bg tint, stat value ExtraBold white, label white 60%
```

**2. Preparedness Journey Card**
```
Style:      Card.Elevated
Title:      "Preparedness Journey" — TextStyle.Heading.Medium, text_primary
Content:    Three progress rows:
  "Simulations Completed"  32/50  — navy progress bar, full width
  "Lessons Mastered"       24/40  — navy progress bar
  "Drills Practiced"       18/25  — orange/amber progress bar
  Each row: label left | count right | bar below
```

**3. Certifications**
```
Section header: "Certifications"
List items (Card.Elevated each):
  Left:   Icon (emoji-free — SVG illustration or colored icon)
  Center: Title "First Responder Certified" — Heading.Small
          "Issued on May 1, 2026" — Body.Small, text_secondary
  Right:  "Verified" chip — green bg, green text
```

**4. Badges**
```
Section header: "Badges"
Grid or horizontal scroll of earned badges
Locked badges shown as grayed silhouettes
```

**5. Emergency Contacts**
```
Section header: "Emergency Contacts"
List of added contacts with name, relationship, phone
"Add Contact" button at bottom
```

**6. Offline Maps**
```
Section header: "Offline Maps"
Downloaded region cards: region name, storage size, download date
"Download New Region" button
```

**7. Settings**
```
Section header: "Settings"
Rows: Notifications toggle | Dark Mode toggle | Region selector | Language selector
```

**8. Version Card**
```
Bottom card: App version, build number
Light gray text, centered
```

---

## 14. Screen Layout — Emergency Mode

> Emergency Mode uses an **entirely separate dark theme**. No cream backgrounds. No standard nav bar. High contrast only.

### Visual Reference
Screen shows: deep dark navy background, ACTIVE badge, status grid row, large red SOS broadcast button, survivor status chips, Exit pill button.

### Theme Overrides
```
Background:       #041631  (deep dark navy, almost black)
Text primary:     #FFFFFF
Text secondary:   #A0AEC0  (light gray)
Card background:  #0D2137  (slightly lighter than bg)
Card border:      #1A3A5C  (subtle border for definition)
```

### Sections (top to bottom)

**1. Header**
```
"Emergency" — TextStyle.Display.Large, white, Bold
"Mode" — same line or next line
Right: "● ACTIVE" badge — red dot + "ACTIVE" text, red pill background
```

**2. Connection Status Row**
```
Style: Card.Dark, single row, 4 columns
Items (icon + value + label):
  Battery icon | "78%" | "Battery"
  Signal icon  | "4G"  | "Network"
  Bluetooth icon | "3" | "Mesh"
  GPS icon     | "On"  | "GPS"
Icon colors: green = active/good, amber = degraded, red = offline
Text: white values, white 60% alpha labels
```

**3. SOS Broadcast Button**
```
Shape:        Circle
Size:         160dp diameter
Background:   #D91F32 with radial gradient (slightly lighter center)
Border:       4dp white, 20% alpha (subtle glow ring)
Content:
  - Broadcast/wifi icon (white, 48dp)
  - "SOS" — TextStyle.Display.Medium, white, ExtraBold
  - "Broadcasting" — TextStyle.Label.Large, white 80% alpha
Animation:    Pulse scale 1.0 → 1.06 → 1.0, 1000ms loop, infinite
              Outer ring glow pulses opacity 100% → 40% in sync
Status text below button:
  "Your location is being broadcast to nearby devices and emergency services"
  TextStyle.Body.Small, white 60% alpha, centered
```

**4. Your Status Section**
```
Header: "Your Status" — TextStyle.Heading.Medium, white

2×3 grid of status chips:
  I'm Safe     | Injured
  Trapped      | Need Water
  Need Food    | Need Medical

Selected chip:   #2EA58D background, white text, checkmark icon
Unselected chip: #0D2137 background, white 70% text, relevant icon
Height per chip: 56dp, corner_md (12dp)
```

**5. Quick Utility Actions (if scrolled)**
```
2×2 grid:
  Flashlight | Shelters
  Contacts   | Checklist
Style: Card.Dark with icon + label
```

**6. Nearby Devices (if scrolled)**
```
Section: "Nearby Devices"
List: device name, distance, mesh status indicator
```

**7. Offline Maps Card (if scrolled)**
```
Shows downloaded region name + storage used
"Open Map" button
```

**8. Exit Emergency Mode**
```
Position: Bottom, always visible (fixed or sticky footer)
Style:     Button.Pill — dark pill (#2A2A3A), white text "Exit Emergency Mode"
Width:     Match parent minus 32dp margin
```

---

## 15. Future: AI Assistant

The AI Assistant is **not a navigation tab**. Future implementation:

```
Trigger:     Floating orb button, bottom-right corner of standard screens
Size:        52dp circle
Background:  Navy gradient
Icon:        Sparkle/AI icon, white
Behavior:    Tap → expands into a chat overlay (bottom sheet)
             Overlay: 70% screen height, rounded top corners 24dp
             Chat history + input field inside
State:       Always available on Home, Lab, MedReady, Profile
             Hidden in Emergency Mode
```

Do not implement a fake AI. Show "Coming Soon" state until backend is ready.

---

## 16. Future: Mesh Network UI

Full mesh network screens are a future phase. Use Emergency Mode dark theme for all mesh screens.

Required screens (future):
```
1. Mesh Discovery      — scanning for nearby nodes
2. Nearby Survivors    — list with distance, status, name
3. Priority List       — sorted by urgency (medical > water > food)
4. Rescuer View        — different role, different UI
5. Survivor Detail     — individual survivor card
6. Offline Map         — downloaded region with survivor pins
```

---

## 17. Animation Guidelines

### Timing Scale
```
anim_instant:   0ms    ← state changes with no animation
anim_fast:      100ms  ← button ripple, toggle flick
anim_normal:    200ms  ← tab switch, badge update, fade
anim_medium:    300ms  ← card reveal, progress fill
anim_slow:      500ms  ← screen entrance, hero reveal
anim_pulse:     1200ms ← SOS button loop (infinite)
```

### Specific Animations

| Element | Animation | Duration |
|---------|-----------|----------|
| Screen transition | Fade + 20dp slide up | 300ms |
| Tab switch | Fade | 150ms |
| Card entrance | Fade + 40dp translate up | 300ms, staggered 50ms per card |
| Progress bar fill | Smooth interpolated fill | 400ms, decelerate |
| SOS button | Scale pulse 1.0→1.08→1.0 | 1200ms, infinite |
| Emergency button (nav) | Scale pulse 1.0→1.06→1.0 | 1200ms, infinite |
| Status badge update | Fade transition | 200ms |
| Challenge card entrance | Scale 0.95→1.0 + fade | 250ms |
| Achievement unlock | Scale pop 0.0→1.1→1.0 + fade | 400ms, overshoot |

### Performance Rules
- All animations must be GPU-accelerated (use `transform`, avoid layout-affecting props)
- Disable all non-essential animations in battery-saver mode
- Avoid complex path animations on low-end devices (< 3GB RAM)
- SOS pulse must never drop frames — keep it simple (scale only)

---

## 18. Implementation Guide

### Prerequisites
```
Architecture:     MVVM (already in place)
UI Framework:     Kotlin + XML layouts
Material:         Material Components 1.9.0+
Min SDK:          API 26 (Android 8.0)
Target SDK:       API 34+
```

### Estimated Effort
| Task | Time |
|------|------|
| Font migration (Poppins → Plus Jakarta Sans) | 1–2 hrs |
| Color token update (semantic colors) | 1 hr |
| Home screen (already done, validation only) | 1 hr |
| Lab screen | 2–3 hrs |
| MedReady screen | 1.5–2 hrs |
| Profile screen | 1.5–2 hrs |
| Emergency Mode screen | 2–3 hrs |
| Reusable component includes | 2 hrs |
| Navigation (5-slot + SOS button) | 1.5 hrs |
| Animation system | 2 hrs |
| ViewModel wiring + testing | 3–4 hrs |
| **Total** | **~18–22 hrs** |

### Step 1: Dependencies

`build.gradle.kts`:
```kotlin
dependencies {
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.0")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.0")
}
```

### Step 2: Navigation Graph

```xml
<?xml version="1.0" encoding="utf-8"?>
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/mobile_navigation"
    app:startDestination="@id/navigation_home">

    <fragment android:id="@+id/navigation_home"
        android:name="...HomeFragment"
        android:label="@string/nav_home" />

    <fragment android:id="@+id/navigation_lab"
        android:name="...LabFragment"
        android:label="@string/nav_lab" />

    <fragment android:id="@+id/navigation_medready"
        android:name="...MedReadyFragment"
        android:label="@string/nav_medready" />

    <fragment android:id="@+id/navigation_profile"
        android:name="...ProfileFragment"
        android:label="@string/nav_profile" />

    <fragment android:id="@+id/navigation_emergency"
        android:name="...EmergencyModeFragment"
        android:label="Emergency Mode" />
</navigation>
```

### Step 3: MainActivity Setup

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = (supportFragmentManager
            .findFragmentById(R.id.mainFragmentContainer) as NavHostFragment)
            .navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        NavigationUI.setupWithNavController(bottomNav, navController)

        // SOS button — center slot, NOT a nav item
        val sosButton = findViewById<FloatingActionButton>(R.id.sosButton)
        val pulseAnimator = AnimatorInflater.loadAnimator(this, R.animator.emergency_button_pulse)
        pulseAnimator.setTarget(sosButton)
        pulseAnimator.start()

        sosButton.setOnClickListener {
            navController.navigate(R.id.navigation_emergency)
        }
    }
}
```

### Step 4: Style Migration Pattern

**Before (old Poppins-based):**
```xml
<TextView
    android:textColor="@color/dashboard_greeting"
    android:textSize="14sp"
    android:fontFamily="@font/poppins_regular"
    android:text="Good morning" />
```

**After (current design system):**
```xml
<TextView
    style="@style/TextStyle.Body.Medium"
    android:textColor="@color/text_secondary"
    android:text="@string/home_greeting" />
```

---

## 19. Theming & Style Application

**Text — always use predefined styles:**
```xml
<TextView style="@style/TextStyle.Display.Large" />
<TextView style="@style/TextStyle.Heading.Medium" />
<TextView style="@style/TextStyle.Body.Small" />
```

**Cards — always use CardView with style:**
```xml
<androidx.cardview.widget.CardView
    style="@style/Card.Elevated"
    app:cardBackgroundColor="@color/bg_card"
    app:cardCornerRadius="@dimen/corner_lg" />
```

**Buttons — always use MaterialButton with style:**
```xml
<com.google.android.material.button.MaterialButton
    style="@style/Button.Primary"
    android:text="@string/action_start" />
```

**Gradient backgrounds — use drawable:**
```xml
<View
    android:background="@drawable/card_hero_background" />
```

---

## 20. ViewModel Binding Examples

### HomeFragment
```kotlin
class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_home_modern, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userName.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.homeUserName).text = it
        }
        viewModel.preparednessLevel.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.prepPercentage).text = "$it%"
            view.findViewById<ProgressBar>(R.id.prepProgress).progress = it
        }
        viewModel.riskAlert.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.riskAlertTitle).text = it.title
            view.findViewById<TextView>(R.id.riskAlertBody).text = it.description
        }
    }
}
```

### EmergencyModeFragment
```kotlin
class EmergencyModeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_emergency_modern, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start SOS pulse
        val sosButton = view.findViewById<FloatingActionButton>(R.id.sosButton)
        AnimatorInflater.loadAnimator(requireContext(), R.animator.emergency_button_pulse)
            .also { it.setTarget(sosButton); it.start() }

        sosButton.setOnClickListener { viewModel.broadcastSOS() }

        view.findViewById<Button>(R.id.exitEmergencyButton).setOnClickListener {
            findNavController().navigateUp()
        }

        // Status chip selection
        listOf(R.id.chipSafe, R.id.chipInjured, R.id.chipTrapped).forEach { id ->
            view.findViewById<View>(id).setOnClickListener { viewModel.setStatus(id) }
        }
    }
}
```

---

## 21. Accessibility Requirements

| Requirement | Target |
|-------------|--------|
| Minimum touch target | 48dp × 48dp (SOS button: 64dp) |
| Normal text contrast | ≥ 4.5:1 (WCAG AA) |
| Large text contrast (18sp+ bold) | ≥ 3:1 |
| Color as sole indicator | Never — always pair with icon or text label |
| System text scaling | Support up to 200% |
| TalkBack | Full support, logical reading order |
| Focus indicators | Visible on all interactive elements |
| Content descriptions | Required on all icons and images |
| Heading hierarchy | No skipped levels (H1 → H2 → H3) |

Emergency Mode has stricter requirements:
- Minimum touch target: 56dp (panic use scenario)
- All critical actions (SOS, status) must be reachable with one thumb
- No text below 14sp in Emergency Mode

---

## 22. File Structure

```
res/
├── values/
│   ├── colors.xml              ✅ Implemented (semantic + scale tokens)
│   ├── dimens.xml              ✅ Implemented (full spacing + sizing scale)
│   ├── styles.xml              ✅ Implemented (all component styles)
│   └── strings.xml             ✅ Implemented (300+ strings)
├── font/
│   ├── plus_jakarta_sans.xml   ⚠️  UPDATE — replace Poppins references
│   ├── plus_jakarta_sans_regular.ttf    ⏳ Add
│   ├── plus_jakarta_sans_medium.ttf     ⏳ Add
│   ├── plus_jakarta_sans_semibold.ttf   ⏳ Add
│   ├── plus_jakarta_sans_bold.ttf       ⏳ Add
│   └── plus_jakarta_sans_extrabold.ttf  ⏳ Add
├── drawable/
│   ├── card_background.xml             ✅
│   ├── card_hero_background.xml        ✅ (gradient: #0E4B93 → #1A2D5C)
│   ├── emergency_background.xml        ✅ (bg: #041631)
│   ├── emergency_button_background.xml ✅
│   ├── nav_bar_background.xml          ✅ (white, 24dp top corners)
│   ├── ic_sos.xml                      ✅
│   ├── ic_nav_home.xml                 ✅
│   ├── ic_nav_lab.xml                  ✅
│   ├── ic_nav_medready.xml             ✅
│   ├── ic_nav_profile.xml              ✅
│   ├── ic_location.xml                 ✅
│   ├── ic_alert_circle.xml             ✅
│   ├── ic_weather_sunny.xml            ✅
│   ├── ic_play_circle.xml              ✅
│   └── ic_expand.xml                   ✅
├── animator/
│   └── emergency_button_pulse.xml      ✅
├── layout/
│   ├── activity_main.xml               ✅ (5-slot nav + SOS FAB)
│   ├── fragment_home_modern.xml        ✅ (complete)
│   ├── floating_bottom_nav.xml         ✅ (reference layout)
│   ├── fragment_lab_modern.xml         ⏳ TODO
│   ├── fragment_medready_modern.xml    ⏳ TODO
│   ├── fragment_emergency_modern.xml   ⏳ TODO
│   └── fragment_profile_modern.xml     ⏳ TODO
├── menu/
│   └── menu_bottom_navigation.xml      ⚠️  UPDATE to 5-slot (add empty center slot)
└── navigation/
    └── mobile_navigation.xml           ⚠️  UPDATE (add emergency destination)
```

---

## 23. Implementation Progress

### Build Status
- **Last Build**: SUCCESS ✅
- **Resource Compilation**: Clean
- **Kotlin Compilation**: Clean
- **APK Generated**: Yes

### Completed
- [x] `colors.xml` — 175+ definitions, semantic tokens, implemented in app
- [x] `dimens.xml` — full spacing, sizing, radius scale
- [x] `styles.xml` — 200+ component and typography styles
- [x] `strings.xml` — 300+ localization-ready strings
- [x] `poppins.xml` — (to be replaced by Plus Jakarta Sans)
- [x] Nav menu IDs standardized
- [x] `MainActivity.kt` updated
- [x] `MedReadyFragment` stub created
- [x] `fragment_home_modern.xml` — complete, all sections
- [x] `activity_main.xml` — floating nav + SOS FAB
- [x] All icon drawables
- [x] `emergency_button_pulse.xml` animator
- [x] Build fixes (ic_play_circle, nav_bar_background)

### In Progress
- [ ] Font migration: Poppins → Plus Jakarta Sans (partially started on Profile)
- [ ] Navigation: update to 5-slot with proper empty center slot

---

## 24. Remaining Tasks

### High Priority

**Font Migration — 1–2 hrs**
- [ ] Add Plus Jakarta Sans `.ttf` files to `res/font/`
- [ ] Update `plus_jakarta_sans.xml` font-family
- [ ] Update `styles.xml` to reference new font family everywhere
- [ ] Remove all Poppins references

**Navigation Update — 1.5 hrs**
- [ ] Update `menu_bottom_navigation.xml` to 5-slot structure (empty center)
- [ ] Position SOS FAB correctly above center slot
- [ ] Add 88dp bottom padding to all fragments

**Screen Layouts — 8–10 hrs**
- [ ] `fragment_lab_modern.xml` — hero, daily challenge, simulations grid, drills, leaderboard, badges
- [ ] `fragment_medready_modern.xml` — scan hero, analysis grid, previous scans
- [ ] `fragment_profile_modern.xml` — gradient hero, journey, certifications, settings
- [ ] `fragment_emergency_modern.xml` — dark theme, status row, SOS button, status chips, exit pill

### Medium Priority

**Reusable Component Includes — 2 hrs**
- [ ] `include_preparedness_hero_card.xml`
- [ ] `include_risk_alert_card.xml`
- [ ] `include_stat_card.xml`
- [ ] `include_progress_bar_row.xml`
- [ ] `include_challenge_card.xml`
- [ ] `include_simulation_card.xml`
- [ ] `include_certification_row.xml`

**Animation System — 2 hrs**
- [ ] SOS button pulse (nav bar button)
- [ ] Tab active state transition
- [ ] Progress bar fill animation (400ms)
- [ ] Card staggered entrance (50ms delay per card)
- [ ] Screen slide-up transition

**Emergency Mode Integration — 2–3 hrs**
- [ ] Dark theme toggle when entering Emergency Mode
- [ ] SOS broadcast state management
- [ ] Haptic feedback on SOS press
- [ ] Status chip selection logic
- [ ] Exit Emergency Mode confirmation dialog

### Lower Priority (Polish)

**Testing & QA — 3–4 hrs**
- [ ] Wire all ViewModels to modern layouts
- [ ] Test on 5", 6", 7" devices
- [ ] Test landscape orientation
- [ ] Validate contrast ratios
- [ ] TalkBack audit

**Future Screens**
- [ ] AI Assistant floating orb + chat overlay
- [ ] Mesh Network UI (full phase, separate spec)
- [ ] Dark mode support (separate phase)

---

## 25. Troubleshooting

**Text not using correct style**
Use `style="@style/TextStyle.Body.Medium"` on the TextView. Do not set `textSize`, `fontFamily`, or `textColor` individually unless overriding for a specific reason.

**Font showing as system default**
Ensure the font XML file is valid, the `.ttf` files are in `res/font/`, and you've done a clean rebuild (`Build > Clean Project`, then rebuild).

**Colors looking wrong or washed out**
All color values must use `@color/` references. Check that `colors.xml` has the semantic token you're referencing. Never use hardcoded hex in layout XML.

**Layout not scrolling or content cut off at bottom**
All scrollable fragments need `android:paddingBottom="@dimen/fragment_bottom_padding"` (88dp) and `android:clipToPadding="false"` on the root scroll view.

**SOS button behind other views / not tappable**
Check layout hierarchy in Layout Inspector. The FAB must be the last (topmost z-order) child in the root FrameLayout of `activity_main.xml`. Increase elevation if needed.

**Emergency Mode still showing cream background**
The Emergency Mode fragment or activity must explicitly set the window background and root view background to `@color/bg_emergency_dark` (#041631). It does not inherit from the app theme automatically.

**Stat numbers showing wrong font weight**
Ensure ExtraBold (800) weight is included in your font family XML. If you only have up to 700, Android will synthesize bold which looks different.

**Bottom nav center slot gap inconsistent across screen sizes**
Use `ConstraintLayout` with a fixed-width invisible placeholder view in the center slot, then position the SOS FAB relative to that placeholder.

---

## 26. Do's and Don'ts

### DO
- Use `@color/` tokens from `colors.xml` for every color reference
- Use `@dimen/` from `dimens.xml` for every spacing and size value
- Use `@style/TextStyle.*` for all text — never set font attributes individually
- Use `@style/Card.*`, `@style/Button.*` for all components
- Use SVG vector drawables for all icons — no PNGs, no emojis
- Set `fragment_bottom_padding` (88dp) on every scrollable fragment
- Ensure all tap targets are minimum 48dp (56dp in Emergency Mode)
- Test contrast ratios — all text must pass WCAG AA (4.5:1)
- Keep Emergency Mode entirely separate from the standard theme
- Use Plus Jakarta Sans for every text element

### DON'T
- Hardcode any hex color value in layout XML or Kotlin
- Hardcode any dimension in layout XML or Kotlin
- Use Poppins anywhere — it is removed
- Use emoji characters as icons or labels in the UI
- Use Material Design's default blue color scheme
- Use sharp corners (corner_none) on cards or buttons
- Use pure black (`#000000`) as text color — use `text_primary` (#0B2856)
- Use more than 2 font weights in a single card or section
- Place the SOS button inside the nav bar as a regular tab item
- Show AI analysis results before actual backend integration (use skeleton/coming soon states)
- Apply the cream background to Emergency Mode screens

---

## 27. Quick Reference Card

```
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
SAFEREADY DESIGN SYSTEM — QUICK REFERENCE v2.0
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

FONT
  Plus Jakarta Sans (400 / 500 / 600 / 700 / 800)

BACKGROUNDS
  App screens:       bg_app           #F7F2EA
  Cards:             bg_card          #FFFFFF
  Hero sections:     Navy gradient    #0E4B93 → #1A2D5C
  Emergency Mode:    bg_emergency     #041631

TEXT
  Primary:           text_primary     #0B2856
  Secondary:         text_secondary   #6B7280
  On dark:           text_inverse     #FFFFFF

ACCENT
  Emergency / SOS:   #D91F32
  Success / Safe:    #2EA58D
  Warning / Streak:  #F2A65A
  Info:              #3B82F6

SPACING (dp)
  xs=4  sm=8  md=12  lg=16  xl=20  2xl=24  3xl=32  4xl=40

RADIUS (dp)
  xs=4  sm=8  md=12  lg=16  xl=20  2xl=24  full=pill

ELEVATION (dp)
  light=2  medium=4  high=8  overlay=12

TYPOGRAPHY (sp)
  Display XL: 40 ExtraBold   Display L: 32 Bold
  Heading L:  22 Bold        Heading M: 20 SemiBold
  Body L:     16 Regular     Body M: 14 Regular
  Label M:    12 Medium      Label S: 11 Medium

NAV
  5 slots: [Home][Lab][EMPTY][MedReady][Profile]
  SOS FAB: 64dp circle, #D91F32, floats above center slot
  Bar:     64dp height, 24dp top radius, 16dp margins, 8dp elevation
  Fragment bottom padding: 88dp (always)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

**Document Version**: 2.0
**Last Updated**: May 2026
**Source of Truth**: Figma screenshots (May 23–24, 2026)
**Status**: Foundation complete — screens in progress
**Replaces**: DESIGN_SYSTEM.md v1.0, FONT_SETUP.md, UI_IMPLEMENTATION_GUIDE.md v1.0, UI_REDESIGN_PROGRESS.md