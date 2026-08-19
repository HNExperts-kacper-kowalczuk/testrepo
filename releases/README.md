# Sideload builds

`inci-scan-debug.apk` is a **debug-signed** Android build of INCI Scan (`com.hnexperts.cosmetics.scanner`, version 1.0).

This build includes crop-handle, layout, and online GTIN lookup fixes:

- Four 48 dp corner handles you can drag on the ingredient-list crop screen
- Nested Scaffolds no longer double status/navigation-bar padding
- Unknown barcodes can be looked up on Open Beauty Facts or searched on the web

Install:

```bash
adb install -r releases/inci-scan-debug.apk
```

- minSdk 26; not for Play Store
- Uses Google **test** AdMob unit IDs
