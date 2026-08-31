# Sideload builds

Debug APKs are **not** stored in git. Build one locally when you need to install on a device:

```bash
export ANDROID_HOME=/path/to/android-sdk
./gradlew :androidApp:assembleDebug
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk
```

Do not copy that file into `releases/` or commit it unless someone explicitly asks for a sideload binary in the repo.

- minSdk 26; not for Play Store
- Debug uses Google **test** AdMob unit IDs
- First launch unpacks `catalog.sqlite.gz` into `catalog.db`
