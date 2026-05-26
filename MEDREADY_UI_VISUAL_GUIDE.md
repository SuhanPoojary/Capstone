# MedReady UI - Visual Layout Guide

## Screen Structure Overview

```
┌─────────────────────────────────────┐
│  NestedScrollView (fillViewport)    │
│  Background: #F6F1E8 (sr_bg_main)   │
│                                     │
│  ┌─────────────────────────────────┐│
│  │ LinearLayout (vertical)         ││
│  │ Padding: 16dp all sides         ││
│  │                                 ││
│  │ ┌──────────────────────────────┐││
│  │ │ "MedReady"                   │││  ← Title (20sp Bold, Navy)
│  │ │ "AI-powered emergency kit..." │││  ← Subtitle (14sp, Secondary Gray)
│  │ └──────────────────────────────┘││
│  │                                 ││
│  │ ┌──────────────────────────────┐││
│  │ │ HERO CARD (CardView)         │││
│  │ │ Corner: 28dp                 │││
│  │ │ Color: #015185 → #5990C0     │││
│  │ │                              │││
│  │ │  ┌────────────────────────┐  ││
│  │ │  │   [SCAN ICON]          │  │││  ← 64dp circle
│  │ │  │   64dp container       │  │││
│  │ │  └────────────────────────┘  ││
│  │ │                              │││
│  │ │  "Scan Your Emergency Kit"   │││  ← White Bold 20sp
│  │ │  "Upload or capture an..."   │││  ← White 14sp
│  │ │                              │││
│  │ │  ┌──────────┐  ┌──────────┐  ││
│  │ │  │ Take     │  │ Upload   │  │││  ← Pill buttons (48dp)
│  │ │  │ Photo    │  │          │  │││
│  │ │  └──────────┘  └──────────┘  ││
│  │ └──────────────────────────────┘││
│  │                                 ││
│  │ ┌──────────────────────────────┐││
│  │ │ "What We Analyze"            │││  ← Section Title
│  │ │                              │││
│  │ │ ┌────────────┐ ┌────────────┐││
│  │ │ │ [ICON]     │ │ [ICON]     │││
│  │ │ │ Detection  │ │ Expiry     │││
│  │ │ │ OCR scans  │ │ Alerts     │││
│  │ │ └────────────┘ └────────────┘││
│  │ │ ┌────────────┐ ┌────────────┐││
│  │ │ │ [ICON]     │ │ [ICON]     │││
│  │ │ │ Missing    │ │ Readiness  │││
│  │ │ │ Gap        │ │ Assessment │││
│  │ │ └────────────┘ └────────────┘││
│  │ └──────────────────────────────┘││
│  │                                 ││
│  │ ┌──────────────────────────────┐││
│  │ │ "Previous Scans"     View All │││  ← Header Row
│  │ │                              │││
│  │ │ ┌────────────────────────────┐││
│  │ │ │ May 5, 2026       78%      │││
│  │ │ │ 12 items scanned  Readiness│││
│  │ │ ├────────────────────────────┤││
│  │ │ │ ⚠ 2 warnings found        │││  ← Orange strip
│  │ │ └────────────────────────────┘││  │
│  │ │ ┌────────────────────────────┐││  │
│  │ │ │ April 28, 2026    65%      │││  │
│  │ │ │ 10 items scanned  Readiness│││  │
│  │ │ ├────────────────────────────┤││  │
│  │ │ │ ⚠ 4 warnings found        │││  ← Orange strip
│  │ │ └────────────────────────────┘││
│  │ └──────────────────────────────┘││
│  │                                 ││
│  │ [80dp bottom padding]           ││  ← Navbar spacing
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

## Component Breakdown

### Header Section
```
Title: "MedReady"
- Font: Plus Jakarta Sans Bold
- Size: 20sp
- Color: #1B1B1B (sr_text_primary)
- Weight: Bold 700

Subtitle: "AI-powered emergency kit analyzer"
- Font: Plus Jakarta Sans Regular
- Size: 14sp
- Color: #5C5C5C (sr_text_secondary)
- Margin Bottom: 24dp
```

### Hero Card
```
Outer: CardView
- Width: match_parent
- Height: wrap_content
- Background: #015185 → #5990C0 (135° gradient)
- Corners: 28dp
- Elevation: 2dp

Inner: LinearLayout (vertical, centered)
- Padding: 24dp all sides
- Gravity: center_horizontal

Children:
1. FrameLayout (icon container)
   - Size: 64x64dp
   - Background: white @0.15 alpha
   - Margin bottom: 24dp
   - Child: ImageView (QR icon, 56dp)

2. TextView (title)
   - "Scan Your Emergency Kit"
   - 20sp Bold White
   - Margin bottom: 8dp

3. TextView (description)
   - "Upload or capture an image..."
   - 14sp White @0.9 alpha
   - Padding H: 12dp
   - Margin bottom: 24dp

4. LinearLayout (buttons, horizontal)
   - Children with 16dp spacing:
     a) Button "Take Photo"
        - Height: 48dp
        - Padding H: 24dp
        - Corners: 24dp
        - Background: white
     b) Button "Upload"
        - Same as (a) but @0.8 alpha
```

### Analysis Grid
```
GridLayout
- Columns: 2
- Rows: 2
- Spacing: 12dp between items

Each Card:
- Width: 0dp + columnWeight=1 (50% - 6dp gap)
- Height: wrap_content
- Background: #FFFFFF
- Corners: 22dp
- Elevation: 2dp

Card Contents:
- Icon: 32dp, tint #102A6B
- Title: 16sp Bold, #1B1B1B
- Desc: 12sp Regular, #5C5C5C
- Padding: 16dp
- All centered
```

### Previous Scans Section
```
Header Row:
- Left: Title (20sp Bold) - flex_grow=1
- Right: "View All" (14sp Bold, Navy)

Scan Item Card:
- CardView: 22dp corners, white, elevation 2dp

Top Section (LinearLayout horizontal):
- Left (flex_grow=1):
  - Date: 14sp Bold, #1B1B1B
  - Items: 12sp Regular, #5C5C5C
  - Margin top between: 4dp

- Right:
  - Percentage: 24sp Bold, #102A6B
  - Label: 12sp Regular, #5C5C5C
  - Margin left: 8dp

Bottom: Warning Strip
- Height: 40dp
- Background: #FEF8F0 (orange_50)
- Content (horizontal):
  - Icon: 20x20dp QR warning, tint #F59E0B
  - Text: "N warnings found"
  - Color: #D97706 (orange_600)
  - Font: 12sp Bold
```

## Color Palette Reference

### Primary Colors
- **Navy (Headers)**: #102A6B
- **Deep Navy (Hero)**: #015185
- **Accent Blue**: #5990C0
- **Background**: #F6F1E8

### Text Colors
- **Primary**: #1B1B1B
- **Secondary**: #5C5C5C
- **Inverse (White)**: #FFFFFF (on dark backgrounds)

### Semantic
- **Card**: #FFFFFF
- **Warning BG**: #FEF8F0
- **Warning Icon**: #F59E0B
- **Warning Text**: #D97706

## Spacing Reference

```
4dp  spacing_xs     Used for: minimal gaps
8dp  spacing_sm     Used for: text line spacing
12dp spacing_md     Used for: component padding top
16dp spacing_lg     Used for: standard padding
20dp spacing_xl     Used for: section spacing
24dp spacing_2xl    Used for: hero card padding
32dp spacing_3xl    Used for: major sections
```

## Typography Reference

**Font Family**: Plus Jakarta Sans

```
Heading Large:    20sp Bold (#1B1B1B)
Body Large:       16sp Regular (#1B1B1B)
Body Medium:      14sp Regular (#1B1B1B)
Body Small:       12sp Regular (#5C5C5C)
Label Medium:     12sp Medium (#5C5C5C)
Display Small:    24sp Bold (#102A6B)
```

## Responsive Behavior

- **Phones (< 600dp)**: Single column grid (may need adjustment)
- **Tablets (≥ 600dp)**: 2-column grid as designed
- **All devices**: GridLayout with columnWeight="1" ensures equal width

## Elevation/Shadow System

```
Light (2dp):    Used for most cards and buttons
Medium (4dp):   Used for elevated surfaces
High (8dp):     Reserved for future use
```

## Animation Considerations

Future implementations may add:
- Hero card fade-in on screen load
- Grid cards stagger animation
- Button press ripple effect
- Warning badge pulse animation

---

**Design Specifications Version**: 1.0
**Last Updated**: May 24, 2026

