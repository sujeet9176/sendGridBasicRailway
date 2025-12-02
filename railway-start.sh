#!/bin/bash

# Railway startup script for SendGrid Basic Railway

# Don't use set -e, we want to handle errors gracefully

WAR_FILE="target/sendGridBasicRailway.war"

echo "=== Railway Startup Script ==="
echo "Current directory: $(pwd)"
echo "Java version:"
java -version 2>&1

# Ensure we have the WAR file
if [ ! -f "$WAR_FILE" ]; then
    echo "ERROR: WAR file not found at $WAR_FILE"
    echo "Contents of target directory:"
    ls -la target/ 2>/dev/null || echo "target/ does not exist"
    exit 1
fi

echo "WAR file found: $WAR_FILE"

# Extract WAR to get classes
EXTRACT_DIR="target/webapp-extracted"
echo "Extracting WAR file..."
rm -rf "$EXTRACT_DIR"
mkdir -p "$EXTRACT_DIR"

# Get absolute path to WAR file before changing directory
BASE_DIR="$(pwd)"
WAR_ABS_PATH="$BASE_DIR/$WAR_FILE"

cd "$EXTRACT_DIR"

if command -v jar &> /dev/null; then
    jar xf "$WAR_ABS_PATH"
else
    unzip -q "$WAR_ABS_PATH"
fi

cd "$BASE_DIR"

# Verify the class exists
CLASS_FILE="$EXTRACT_DIR/WEB-INF/classes/com/sendgrid/EmbeddedTomcatServer.class"
if [ ! -f "$CLASS_FILE" ]; then
    echo ""
    echo "ERROR: EmbeddedTomcatServer.class not found in extracted WAR!"
    echo "Expected location: $CLASS_FILE"
    echo ""
    echo "Checking what's actually in the WAR:"
    echo "WEB-INF/classes structure:"
    find "$EXTRACT_DIR/WEB-INF/classes" -type f -name "*.class" 2>/dev/null | head -20 || echo "No .class files found"
    echo ""
    echo "com/sendgrid directory:"
    ls -la "$EXTRACT_DIR/WEB-INF/classes/com/sendgrid/" 2>/dev/null || echo "Directory does not exist"
    echo ""
    echo "All com/sendgrid files:"
    find "$EXTRACT_DIR/WEB-INF/classes/com" -type f 2>/dev/null || echo "No files found"
    exit 1
fi

echo "Found EmbeddedTomcatServer.class: $CLASS_FILE"

# Verify we can actually see the class file
if [ ! -f "$CLASS_FILE" ]; then
    echo "ERROR: Class file verification failed!"
    exit 1
fi

# Try to verify the class can be found (basic check)
echo "Verifying class file is readable..."
ls -lh "$CLASS_FILE"

# Build classpath by expanding wildcards (Java doesn't always handle * in classpath)
build_classpath() {
    local classpath=""
    
    # Add extracted classes
    classpath="$EXTRACT_DIR/WEB-INF/classes"
    
    # Add libraries from extracted WAR (expand wildcards)
    if [ -d "$EXTRACT_DIR/WEB-INF/lib" ]; then
        for jar in "$EXTRACT_DIR/WEB-INF/lib"/*.jar; do
            if [ -f "$jar" ]; then
                classpath="$classpath:$jar"
            fi
        done
    fi
    
    # Add target/lib for embedded Tomcat dependencies (expand wildcards)
    if [ -d "target/lib" ]; then
        for jar in target/lib/*.jar; do
            if [ -f "$jar" ]; then
                classpath="$classpath:$jar"
            fi
        done
    fi
    
    # Add the WAR file itself
    classpath="$classpath:$WAR_FILE"
    
    echo "$classpath"
}

# Build classpath
CLASSPATH=$(build_classpath)

# Get absolute path to WAR file
WAR_ABS_PATH="$(cd "$(dirname "$WAR_FILE")" && pwd)/$(basename "$WAR_FILE")"

echo ""
echo "=== Starting Embedded Tomcat Server ==="
echo "WAR file path: $WAR_ABS_PATH"
echo "Classpath length: ${#CLASSPATH} characters"
echo ""

# Run with explicit classpath and WAR file path as system property
echo "Executing: java -cp [classpath] -Dwar.file.path=$WAR_ABS_PATH com.sendgrid.EmbeddedTomcatServer"
java -cp "$CLASSPATH" -Dwar.file.path="$WAR_ABS_PATH" com.sendgrid.EmbeddedTomcatServer || {
    echo ""
    echo "ERROR: Failed to start EmbeddedTomcatServer"
    echo "Exit code: $?"
    echo ""
    echo "Debugging info:"
    echo "  Classpath first 500 chars: ${CLASSPATH:0:500}"
    echo "  Number of JARs in WEB-INF/lib: $(ls -1 "$EXTRACT_DIR/WEB-INF/lib"/*.jar 2>/dev/null | wc -l)"
    echo "  Number of JARs in target/lib: $(ls -1 target/lib/*.jar 2>/dev/null | wc -l)"
    exit 1
}

