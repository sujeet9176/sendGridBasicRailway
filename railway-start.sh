#!/bin/bash

# Railway startup script for SendGrid Basic Railway

WAR_FILE="target/sendGridBasicRailway.war"

if [ ! -f "$WAR_FILE" ]; then
    echo "ERROR: WAR file not found at $WAR_FILE"
    exit 1
fi

# Build classpath: compiled classes (EmbeddedTomcatServer) + libs + WAR file
# Note: EmbeddedTomcatServer is excluded from WAR, so it's in target/classes
CLASSPATH="target/classes:target/lib/*:$WAR_FILE"

echo "Starting embedded Tomcat server..."
echo "Classpath: $CLASSPATH"
java -cp "$CLASSPATH" com.sendgrid.EmbeddedTomcatServer

