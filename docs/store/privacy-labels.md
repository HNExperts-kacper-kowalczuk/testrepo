# Privacy nutrition labels (Play / App Store)

Keep claims factual. Scan history and avoid-lists stay on device and are not used to personalise ads.

## Data collected

| Data | Purpose | Linked to identity | Used for ads | Notes |
| --- | --- | --- | --- | --- |
| Camera | Barcode and INCI-label scanning only | No | No | Frames are processed on device. Nothing is uploaded for OCR. |
| Advertising ID (Android) / tracking (iOS ATT) | Banner ads after consent | Via Google, if the user consents | Yes, if consent granted | Banners collapse if consent is denied, the SDK fails, or the device is offline. |
| Diagnostics (optional, none in v1) | — | — | — | v1 does not send crash or analytics data. |

## Data not collected

- Account email or name (no login)
- Precise location
- Scan history, ingredient avoid-list, pregnancy / fragrance flags (local SQLite only; optional report flush sends kind, GTIN, and payload hashes, never photos or INCI text)
- Photos of labels (not stored after OCR)

## Permissions

- **Camera** — barcodes and printed ingredient lists.
- **Internet / network state** — optional online GTIN lookup, optional hosted catalog delta, optional report flush, and ad banners. Core lookup works offline.
- **Ad ID (Android)** — Google Ads; declared in the manifest.

## User controls

- First-launch disclaimer acknowledgement (local).
- Ad privacy options (UMP on Android; ATT on iOS).
- Preferences: clear scan history, clear shelf, reset the avoid-list, or reset all personal data on this device (catalog and disclaimer stay).
- Optional “Send unsent reports”: `kind`, GTIN, and payload hashes only — no photos and no INCI text.
- In-app language: system / English / Polish.

## Backup

Android Auto Backup and standard iOS iCloud backup of the app container can include `user.db` (history, shelf, avoid-list, unsent reports, cached lookups). See [user-data-backup.md](../user-data-backup.md).
