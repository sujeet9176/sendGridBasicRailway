#!/bin/bash

# Railway startup script for SendGrid Basic Railway

WAR_FILE="target/sendGridBasicRailway.war"
EMBEDDED_JAR="target/sendGridBasicRailway-embedded-server.jar"

echo "=== Railway Startup Script ==="
echo "Current directory: $(pwd)"
echo "Java version:"
java -version 2>&1

# Method 1: Try using the embedded server JAR if it exists
if [ -f "$EMBEDDED_JAR" ]; then
    echo ""
    echo "=== Method 1: Using embedded server JAR ==="
    echo "Found: $EMBEDDED_JAR"
    CLASSPATH="$EMBEDDED_JAR:target/lib/*:$WAR_FILE"
    java -cp "$CLASSPATH" com.sendgrid.EmbeddedTomcatServer
    exit $?
fi

# Method 2: Use compiled classes from target/classes
if [ -f "target/classes/com/sendgrid/EmbeddedTomcatServer.class" ]; then
    echo ""
    echo "=== Method 2: Using target/classes ==="
    echo "Found EmbeddedTomcatServer.class in target/classes"
    CLASSPATH="target/classes:target/lib/*:$WAR_FILE"
    java -cp "$CLASSPATH" com.sendgrid.EmbeddedTomcatServer
    exit $?
fi

# Method 3: Extract from WAR
if [ -f "$WAR_FILE" ]; then
    echo ""
    echo "=== Method 3: Extracting from WAR ==="
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
        CLASSPATH="$EXTRACT_DIR/WEB-INF/classes"
        if [ -d "$EXTRACT_DIR/WEB-INF/lib" ]; then
            CLASSPATH="$CLASSPATH:$EXTRACT_DIR/WEB-INF/lib/*"
        fi
        if [ -d "target/lib" ]; then
            CLASSPATH="$CLASSPATH:target/lib/*"
        fi
        CLASSPATH="$CLASSPATH:$WAR_FILE"
        java -cp "$CLASSPATH" com.sendgrid.EmbeddedTomcatServer
        exit $?
    fi
fi

# If we get here, class was not found
echo ""
echo "ERROR: EmbeddedTomcatServer.class not found!"
echo ""
echo "Debugging information:"
echo "Current directory: $(pwd)"
echo ""
echo "Looking for WAR file:"
ls -la "$WAR_FILE" 2>/dev/null || echo "  Not found: $WAR_FILE"
echo ""
echo "Looking for embedded JAR:"
ls -la "$EMBEDDED_JAR" 2>/dev/null || echo "  Not found: $EMBEDDED_JAR"
echo ""
echo "Looking in target/classes:"
find target/classes -name "EmbeddedTomcatServer.class" 2>/dev/null || echo "  Not found in target/classes"
echo ""
echo "All .class files in target/classes:"
find target/classes -name "*.class" 2>/dev/null | head -10 || echo "  No .class files found"
echo ""
echo "Contents of target directory:"
ls -la target/ 2>/dev/null || echo "  target/ does not exist"
exit 1

