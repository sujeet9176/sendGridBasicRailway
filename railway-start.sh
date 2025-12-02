#!/bin/bash

# Railway startup script for SendGrid Basic Railway

WAR_FILE="target/sendGridBasicRailway.war"
EXTRACT_DIR="target/webapp-extracted"

if [ ! -f "$WAR_FILE" ]; then
    echo "ERROR: WAR file not found at $WAR_FILE"
    echo "Current directory: $(pwd)"
    echo "Listing target directory:"
    ls -la target/ 2>/dev/null || echo "target/ directory does not exist"
    exit 1
fi

# Extract WAR file to get access to classes
echo "Extracting WAR file to access EmbeddedTomcatServer class..."
rm -rf "$EXTRACT_DIR"
mkdir -p "$EXTRACT_DIR"
cd "$EXTRACT_DIR"

# Try jar command first, fallback to unzip
if command -v jar &> /dev/null; then
    jar xf "../$WAR_FILE"
else
    unzip -q "../$WAR_FILE"
fi

cd - > /dev/null

# Verify the class exists
if [ ! -f "$EXTRACT_DIR/WEB-INF/classes/com/sendgrid/EmbeddedTomcatServer.class" ]; then
    echo "ERROR: EmbeddedTomcatServer.class not found in extracted WAR"
    echo "Looking in: $EXTRACT_DIR/WEB-INF/classes/com/sendgrid/"
    ls -la "$EXTRACT_DIR/WEB-INF/classes/com/sendgrid/" 2>/dev/null || echo "Directory does not exist"
    exit 1
fi

# Build classpath: extracted classes + extracted libs + target libs (for embedded Tomcat)
CLASSPATH="$EXTRACT_DIR/WEB-INF/classes"
if [ -d "$EXTRACT_DIR/WEB-INF/lib" ]; then
    CLASSPATH="$CLASSPATH:$EXTRACT_DIR/WEB-INF/lib/*"
fi
if [ -d "target/lib" ]; then
    CLASSPATH="$CLASSPATH:target/lib/*"
fi

echo "Starting embedded Tomcat server..."
echo "WAR file: $WAR_FILE"
echo "Classpath includes: $EXTRACT_DIR/WEB-INF/classes"
java -cp "$CLASSPATH" com.sendgrid.EmbeddedTomcatServer

