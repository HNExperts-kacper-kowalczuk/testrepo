# User data backup

Scan history, the personal avoid-list, shelf stars, unsent reports, and cached online GTIN hits live in the **user database** (`user.db` on Android, `user.db` on iOS). Replacing the bundled catalog does not wipe this file.

## Android

- `android:allowBackup="true"` is set on the application so Google Auto Backup can include `user.db` with the rest of app data.
- Store privacy labels should describe this as on-device scan history and preferences, not an account.

## iOS

- The user database is in the app sandbox. If you later enable iCloud Documents or iCloud Backup of the container, include `user.db` in the privacy nutrition labels as “Scan History” / “User Content”.
- This release does not add a custom iCloud entitlement.

## What is not backed up

The offline **catalog** (`catalog.db`) is replaced from the bundled `catalog.sqlite.gz` when the checksum changes. Do not treat it as user data.
