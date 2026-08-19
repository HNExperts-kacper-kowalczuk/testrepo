# Sideload builds

`inci-scan-debug.apk` is a **debug-signed** Android build of INCI Scan (`com.hnexperts.cosmetics.scanner`, version 1.0).

This build includes the **shipped CosIng/OBF catalog** (~36k ingredients, ~17k products) and caches online GTIN hits on device.

Install:

```bash
adb install -r releases/inci-scan-debug.apk
```

- minSdk 26; not for Play Store
- Uses Google **test** AdMob unit IDs
- First launch unpacks `catalog.sqlite.gz` into `catalog.db`
