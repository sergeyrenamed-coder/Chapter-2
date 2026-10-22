#!/usr/bin/env sh
# Bootstrap launcher. For a canonical wrapper JAR-free project, this downloads Gradle 8.8.
set -eu
GRADLE_VERSION="8.8"
if command -v gradle >/dev/null 2>&1; then exec gradle "$@"; fi
CACHE_ROOT="${GRADLE_USER_HOME:-$HOME/.gradle}/cursedquest-gradle"
GRADLE_HOME="$CACHE_ROOT/gradle-$GRADLE_VERSION"
if [ ! -x "$GRADLE_HOME/bin/gradle" ]; then
  mkdir -p "$CACHE_ROOT"
  ARCHIVE="$CACHE_ROOT/gradle-$GRADLE_VERSION-bin.zip"
  URL="https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
  echo "Gradle $GRADLE_VERSION not found; downloading it now..."
  if command -v curl >/dev/null 2>&1; then curl -fL "$URL" -o "$ARCHIVE"
  elif command -v wget >/dev/null 2>&1; then wget -O "$ARCHIVE" "$URL"
  else echo 'ERROR: curl or wget is required.' >&2; exit 1; fi
  if ! command -v unzip >/dev/null 2>&1; then echo 'ERROR: unzip is required.' >&2; exit 1; fi
  unzip -q -o "$ARCHIVE" -d "$CACHE_ROOT"
  rm -f "$ARCHIVE"
fi
exec "$GRADLE_HOME/bin/gradle" "$@"
