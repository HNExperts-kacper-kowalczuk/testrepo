# Sideload builds

`inci-scan-debug.apk` is a **debug-signed** Android build of INCI Scan (`com.hnexperts.cosmetics.scanner`, version 1.0).

This build includes crop-handle, layout, and **automatic** online GTIN lookup:

- Four 48 dp corner handles you can drag on the ingredient-list crop screen
- Nested Scaffolds no longer double status/navigation-bar padding
- If a barcode is missing from the offline catalog and the device is online, the INCI list is fetched and scored immediately (Open Beauty Facts, then Open Food Facts). No extra tap. Pack-scan is only shown when there is still no ingredient list.

Install:

```bash
adb install -r releases/inci-scan-debug.apk
```

- minSdk 26; not for Play Store
- Uses Google **test** AdMob unit IDs
