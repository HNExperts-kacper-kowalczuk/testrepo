#!/usr/bin/env bash
set -euo pipefail

# First-option iOS workflow: sync TEAM_ID from the login keychain, then open Xcode.

ROOT="$(cd "$(dirname "$0")/.." && pwd)"

if [ "$(uname -s)" != "Darwin" ]; then
  echo "Opening the iOS app requires macOS and Xcode."
  exit 1
fi
if ! command -v open >/dev/null 2>&1; then
  echo "The open command is missing; install Xcode."
  exit 1
fi

"$ROOT/scripts/sync-ios-signing.sh"
open "$ROOT/iosApp/iosApp.xcodeproj"
echo "Opened iosApp/iosApp.xcodeproj. Select a simulator or device and Run."
