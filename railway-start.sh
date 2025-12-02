#!/bin/bash

# Railway startup script for SendGrid Basic Railway

set -e  # Exit on error

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
cd "$EXTRACT_DIR"

# Use absolute path for WAR file
WAR_ABS_PATH="$(cd "$(dirname "../$WAR_FILE")" && pwd)/$(basename "$WAR_FILE")"

if command -v jar &> /dev/null; then
    jar xf "$WAR_ABS_PATH"
else
    unzip -q "$WAR_ABS_PATH"
fi

cd - > /dev/null

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

# Build classpath
CLASSPATH="$EXTRACT_DIR/WEB-INF/classes"

# Add libraries from extracted WAR
if [ -d "$EXTRACT_DIR/WEB-INF/lib" ]; then
    CLASSPATH="$CLASSPATH:$EXTRACT_DIR/WEB-INF/lib/*"
fi

# Add target/lib for embedded Tomcat dependencies
if [ -d "target/lib" ]; then
    CLASSPATH="$CLASSPATH:target/lib/*"
fi

# Add the WAR file itself (for EmbeddedTomcatServer to find it)
CLASSPATH="$CLASSPATH:$WAR_FILE"

echo ""
echo "=== Starting Embedded Tomcat Server ==="
echo "Classpath: $CLASSPATH"
echo ""

# Run with explicit classpath
java -cp "$CLASSPATH" com.sendgrid.EmbeddedTomcatServer

