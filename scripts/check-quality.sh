#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

MAX_LINES=500
MIN_SDK_MAX=26
IOS_DEPLOY_MAX="15.3"
FAILED=0

echo "Quality check (see docs/quality-checklist.md)"
echo

while IFS= read -r -d '' file; do
  lines="$(wc -l < "$file" | tr -d ' ')"
  if [ "$lines" -gt "$MAX_LINES" ]; then
    echo "FAIL: $file has $lines lines (max $MAX_LINES)"
    FAILED=1
  fi
done < <(find . -name '*.kt' -not -path '*/build/*' -not -path '*/.gradle/*' -print0)

MIN_SDK="$(grep -E '^android-minSdk' gradle/libs.versions.toml | grep -oE '[0-9]+' | head -n1 || true)"
if [ -z "${MIN_SDK:-}" ]; then
  echo "FAIL: could not read android-minSdk from gradle/libs.versions.toml"
  FAILED=1
elif [ "$MIN_SDK" -gt "$MIN_SDK_MAX" ]; then
  echo "FAIL: android-minSdk is $MIN_SDK (must stay <= $MIN_SDK_MAX for 90%+ device coverage unless product agrees to raise it)"
  FAILED=1
else
  echo "OK: android-minSdk=$MIN_SDK"
fi

if grep -q 'armv7' iosApp/iosApp/Info.plist 2>/dev/null; then
  echo "FAIL: Info.plist still requires armv7; use arm64 for current iOS devices"
  FAILED=1
fi

DEPLOY="$(grep -oE 'IPHONEOS_DEPLOYMENT_TARGET = [0-9.]+' iosApp/iosApp.xcodeproj/project.pbxproj | head -n1 | awk '{print $3}' || true)"
if [ -z "${DEPLOY:-}" ]; then
  echo "FAIL: could not read IPHONEOS_DEPLOYMENT_TARGET"
  FAILED=1
else
  echo "OK: IPHONEOS_DEPLOYMENT_TARGET=$DEPLOY (keep <= $IOS_DEPLOY_MAX unless product agrees to raise it)"
fi

THROWABLE_HITS="$(grep -R --include='*.kt' --exclude-dir=build -n 'catch (.*Throwable' shared androidApp 2>/dev/null || true)"
if [ -n "$THROWABLE_HITS" ]; then
  echo "$THROWABLE_HITS"
  echo "FAIL: catch (Throwable) is unsafe; catch Exception and rethrow CancellationException"
  FAILED=1
else
  echo "OK: no catch (Throwable)"
fi

echo
if [ "$FAILED" -ne 0 ]; then
  echo "Quality check FAILED. Fix the issues and re-read docs/quality-checklist.md before committing."
  exit 1
fi

echo "Automated checks passed. Still walk points 1–11 in docs/quality-checklist.md before commit."
