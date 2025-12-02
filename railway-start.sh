#!/bin/bash

# Railway startup script for SendGrid Basic Railway

WAR_FILE="target/sendGridBasicRailway.war"

echo "=== Railway Startup Script ==="
echo "Current directory: $(pwd)"
echo "Java version:"
java -version

# Check if WAR exists
if [ ! -f "$WAR_FILE" ]; then
    echo "ERROR: WAR file not found at $WAR_FILE"
    echo "Listing target directory:"
    ls -la target/ 2>/dev/null || echo "target/ directory does not exist"
    exit 1
fi

echo "WAR file found: $WAR_FILE"

# Check if compiled classes exist in target/classes
if [ -f "target/classes/com/sendgrid/EmbeddedTomcatServer.class" ]; then
    echo "Found EmbeddedTomcatServer.class in target/classes"
    CLASSES_DIR="target/classes"
elif [ -d "target/classes" ]; then
    echo "Checking target/classes directory..."
    find target/classes -name "*.class" | head -5
    CLASSES_DIR="target/classes"
else
    echo "target/classes not found, extracting from WAR..."
    EXTRACT_DIR="target/webapp-extracted"
    rm -rf "$EXTRACT_DIR"
    mkdir -p "$EXTRACT_DIR"
    cd "$EXTRACT_DIR"
    
    if command -v jar &> /dev/null; then
        jar xf "../$WAR_FILE"
    else
        unzip -q "../$WAR_FILE"
    fi
    
    cd - > /dev/null
    
    if [ -f "$EXTRACT_DIR/WEB-INF/classes/com/sendgrid/EmbeddedTomcatServer.class" ]; then
        echo "Found EmbeddedTomcatServer.class in extracted WAR"
        CLASSES_DIR="$EXTRACT_DIR/WEB-INF/classes"
    else
        echo "ERROR: EmbeddedTomcatServer.class not found anywhere!"
        echo "Searching for any EmbeddedTomcatServer files:"
        find . -name "*EmbeddedTomcatServer*" 2>/dev/null
        exit 1
    fi
fi

# Build classpath: classes + libs + WAR file
CLASSPATH="$CLASSES_DIR"

# Add target/lib dependencies (embedded Tomcat, etc.)
if [ -d "target/lib" ]; then
    CLASSPATH="$CLASSPATH:target/lib/*"
fi

# Add extracted libs if we extracted the WAR
if [ -d "target/webapp-extracted/WEB-INF/lib" ]; then
    CLASSPATH="$CLASSPATH:target/webapp-extracted/WEB-INF/lib/*"
fi

# Add the WAR file itself
CLASSPATH="$CLASSPATH:$WAR_FILE"

echo ""
echo "=== Starting Embedded Tomcat Server ==="
echo "Classpath: $CLASSPATH"
echo ""

java -cp "$CLASSPATH" com.sendgrid.EmbeddedTomcatServer

