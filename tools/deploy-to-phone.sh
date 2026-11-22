#!/usr/bin/env bash
# Build a signed release APK of the current checkout and install it over
# the TSWA app on a phone already connected by wireless debugging.
#
# Prerequisites (one-time; see docs/BUILDING.md):
#   - The phone is paired and connected: `adb devices` lists it.
#       adb pair <host>:<pairing-port> <code>   # once per phone
#       adb connect <host>:<connect-port>       # mDNS usually does this
#   - JDK 17, the Android SDK and Gradle 8.7+ are installed.
#   - The signing key sits outside git, in ../TSWA-secrets/:
#       tswa-release.jks   the same key CI signs with
#       password.txt       its password, on one line
#
# Usage: tools/deploy-to-phone.sh
#
# Env overrides:
#   ANDROID_HOME   default ~/Android/Sdk
#   TSWA_SECRETS   default <repo>/../TSWA-secrets
#   GRADLE         default `gradle` on PATH, else the newest one in ~/.gradle
#   ANDROID_SERIAL pick a device when more than one is connected (adb reads it)
set -euo pipefail

REPO=$(cd "$(dirname "$0")/.." && pwd)
cd "$REPO"

export ANDROID_HOME=${ANDROID_HOME:-$HOME/Android/Sdk}
SECRETS=${TSWA_SECRETS:-$REPO/../TSWA-secrets}
# The SDK's adb: older system adb builds lack wireless pairing.
ADB=$ANDROID_HOME/platform-tools/adb
PACKAGE=com.robnugen.tswa
INDEX=app/src/main/assets/www/index.html

die() { echo "deploy-to-phone: $*" >&2; exit 1; }

# --- find the tools ----------------------------------------------------
[ -x "$ADB" ] || die "no adb at $ADB (set ANDROID_HOME)"
if [ -z "${GRADLE:-}" ]; then
  GRADLE=$(command -v gradle || true)
  [ -n "$GRADLE" ] || GRADLE=$(ls -d "$HOME"/.gradle/wrapper/dists/gradle-*-bin/*/gradle-*/bin/gradle 2>/dev/null | sort -V | tail -1)
fi
[ -n "$GRADLE" ] && [ -x "$GRADLE" ] || die "no gradle found (set GRADLE)"

[ -f "$SECRETS/tswa-release.jks" ] || die "missing $SECRETS/tswa-release.jks"
[ -f "$SECRETS/password.txt" ] || die "missing $SECRETS/password.txt"

# --- check the phone ---------------------------------------------------
"$ADB" get-state >/dev/null 2>&1 \
  || die "no phone connected; pair and connect it first (see the top of this script)"
echo "Phone: $("$ADB" shell getprop ro.product.model | tr -d '\r')"

# --- choose the version ------------------------------------------------
# Android refuses to install a lower versionCode over a higher one, so
# go one past whatever the phone has. CI uses its run number instead.
installed=$("$ADB" shell dumpsys package "$PACKAGE" | grep -o 'versionCode=[0-9]*' | head -1 | cut -d= -f2 || true)
code=$(( ${installed:-0} + 1 ))
base=$(sed -n 's/^val tswaBaseVersionName = "\(.*\)"/\1/p' app/build.gradle.kts)
name="$base.$code"
echo "Installed versionCode: ${installed:-none}; building $name (versionCode $code)"

# --- build -------------------------------------------------------------
# CI writes the version into the web UI with sed before building; do the
# same, and put the file back afterwards however the script exits.
backup=$(mktemp)
cp "$INDEX" "$backup"
trap 'cp "$backup" "$INDEX"; rm -f "$backup"' EXIT
sed -i "s/__VERSION__/$name/" "$INDEX"

TSWA_KEYSTORE_PATH="$SECRETS/tswa-release.jks" \
TSWA_KEY_ALIAS=tswa \
TSWA_KEYSTORE_PASSWORD=$(tr -d '\r\n' < "$SECRETS/password.txt") \
  "$GRADLE" -q :app:assembleRelease \
    -PtswaVersionCode="$code" -PtswaVersionName="$name"

# --- install and launch ------------------------------------------------
APK=app/build/outputs/apk/release/app-release.apk
# -r replaces the app in place, keeping its data (settings, saved places).
"$ADB" install -r "$APK"
"$ADB" shell monkey -p "$PACKAGE" -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
echo "Installed and launched $name"
