Plus Jakarta Sans font setup

This project uses Plus Jakarta Sans as the primary UI font for the Profile redesign.

Two recommended approaches:

1) Downloadable fonts (used by the added resource)
   - The repository contains a downloadable font-family XML at res/font/plus_jakarta_sans.xml that uses the Google Fonts provider.
   - No font binary files needed; Google Play services will fetch the font at runtime.
   - Ensure your app's minSdk and dependencies support downloadable fonts and that the device has Google Play services.

2) Bundled font files (offline-safe)
   - Download the Plus Jakarta Sans family .ttf files (regular, medium, bold) from Google Fonts.
   - Place them in app/src/main/res/font/ as:
        - plus_jakarta_sans_regular.ttf
        - plus_jakarta_sans_medium.ttf
        - plus_jakarta_sans_bold.ttf
   - Replace the content of res/font/plus_jakarta_sans.xml with a font-family XML pointing to the local files.

Example local font-family XML (if bundling):

<?xml version="1.0" encoding="utf-8"?>
<font-family xmlns:android="http://schemas.android.com/apk/res/android">
    <font android:fontStyle="normal" android:fontWeight="400" android:font="@font/plus_jakarta_sans_regular" />
    <font android:fontStyle="normal" android:fontWeight="600" android:font="@font/plus_jakarta_sans_medium" />
    <font android:fontStyle="normal" android:fontWeight="700" android:font="@font/plus_jakarta_sans_bold" />
</font-family>

Notes:
- If you use the downloadable font approach, ensure you have the font cert arrays (already present in res/values/font_certs.xml).
- After adding bundled font files, run a clean rebuild.


