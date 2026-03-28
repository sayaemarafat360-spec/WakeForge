#!/bin/bash
# =============================================================================
# WakeForge Setup Script
# =============================================================================
# Run this script to prepare the project for building.
# =============================================================================

set -e

echo "========================================"
echo "  WakeForge Setup Script"
echo "========================================"
echo ""

# ---- Check Java installation ----
if ! command -v java &> /dev/null; then
    echo "ERROR: Java 17 is required but was not found."
    echo "Please install JDK 17 and set JAVA_HOME."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "[OK] Java version: $(java -version 2>&1 | head -1)"

if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "WARNING: Java 17+ is recommended. Detected major version: $JAVA_VERSION"
fi

# ---- Check Android SDK ----
if [ -n "$ANDROID_HOME" ]; then
    echo "[OK] ANDROID_HOME: $ANDROID_HOME"
elif [ -n "$ANDROID_SDK_ROOT" ]; then
    echo "[OK] ANDROID_SDK_ROOT: $ANDROID_SDK_ROOT"
else
    echo "WARNING: ANDROID_HOME is not set."
    echo "  Set it to your Android SDK path, e.g.:"
    echo "  export ANDROID_HOME=\$HOME/Android/Sdk"
fi

# ---- Check / download Gradle wrapper JAR ----
WRAPPER_JAR="gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
    echo ""
    echo "Gradle wrapper JAR not found. Downloading..."
    mkdir -p gradle/wrapper
    curl -L -o "$WRAPPER_JAR" \
        "https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar" 2>/dev/null
    if [ $? -ne 0 ]; then
        echo "ERROR: Failed to download gradle-wrapper.jar"
        echo "  Try manually downloading it and placing it at: $WRAPPER_JAR"
        echo "  URL: https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar"
        exit 1
    fi
    echo "[OK] Downloaded gradle-wrapper.jar"
else
    echo "[OK] gradle-wrapper.jar found"
fi

# ---- Make gradlew executable ----
chmod +x gradlew 2>/dev/null || true
echo "[OK] gradlew is ready"

# ---- Check placeholder resources ----
RAW_DIR="app/src/main/res/raw"
if [ ! -d "$RAW_DIR" ]; then
    echo "Creating raw resource directory..."
    mkdir -p "$RAW_DIR"
fi

PLACEHOLDER_CREATED=0
for name in builtin_dawn builtin_rise builtin_forge builtin_crystal builtin_digital; do
    if [ ! -f "$RAW_DIR/${name}.ogg" ]; then
        printf 'OggS\x00' > "$RAW_DIR/${name}.ogg"
        PLACEHOLDER_CREATED=1
    fi
done
if [ $PLACEHOLDER_CREATED -eq 1 ]; then
    echo "[OK] Created placeholder OGG sound files (replace with real audio for production)"
fi

# ---- Done ----
echo ""
echo "========================================"
echo "  Setup complete!"
echo "========================================"
echo ""
echo "To build the project:"
echo "  ./gradlew assembleDebug"
echo ""
echo "To install on a connected device:"
echo "  ./gradlew installDebug"
echo ""
echo "To clean and rebuild:"
echo "  ./gradlew clean assembleDebug"
echo ""
