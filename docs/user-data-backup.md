# User data backup

Scan history, the personal avoid-list, shelf stars, unsent reports, and cached online GTIN hits live in the **user database** (`user.db` on Android, `user.db` on iOS). Replacing the bundled catalog does not wipe this file.

## Android

- `android:allowBackup="true"` is set on the application so Google Auto Backup can include `user.db` with the rest of app data.
- Store privacy labels should describe this as on-device scan history and preferences, not an account.

## iOS

- `user.db` is in the app sandbox (SQLDelight default location) and is included in **standard iCloud / computer backup of the app container**. This is not an iCloud Documents entitlement and not a custom CloudKit container.
- Spotlight / Siri can offer “Scan barcode” via `NSUserActivity` type `com.hnexperts.cosmetics.scanner.scan` (iOS 15.3, no App Intents). That opens the in-app barcode camera through the same launch-intent path as the Android home-screen shortcut.
- Privacy nutrition: `PrivacyInfo.xcprivacy` records scan history / avoid-list as on-device User Content for app functionality, not linked and not used for tracking.

## What is not backed up

The offline **catalog** (`catalog.db`) is replaced from the bundled `catalog.sqlite.gz` when the checksum changes. Do not treat it as user data.
