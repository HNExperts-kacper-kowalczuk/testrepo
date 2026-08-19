# Sideload builds

`inci-scan-debug.apk` is a **debug-signed** Android build of INCI Scan (`com.hnexperts.cosmetics.scanner`, version 1.0).

This build includes the matcher, OCR crop, and scan-first UI work (PRs #5–#7):

- Compound slash INCI names (`Caprylic/Capric Triglyceride`), `Alcohol Denat.` abbreviations, `(nano)` suffix
- Four-corner crop after capturing an ingredient-list still, then extract only the Ingredients / Skład block
- Scan-first home and colour-coded result header

Install:

```bash
adb install -r releases/inci-scan-debug.apk
```

- minSdk 26; not for Play Store
- Uses Google **test** AdMob unit IDs
