#!/bin/bash

INPUT_CLASS=$1

if [ -z "$INPUT_CLASS" ]; then
    echo "Usage: ./run.sh [LoadTest | LoadTest* | WebLogicJmsXmlSender]"
    exit 1
fi

# Determine the Full Class Path
if [ "$INPUT_CLASS" == "WebLogicJmsXmlSender" ]; then
    FULL_CLASS="com.rogers.jms.util.WebLogicJmsXmlSender"
else
    FULL_CLASS="com.rogers.jms.loadtest.$INPUT_CLASS"
fi

# Change this to correct Java location
APP_HOME="$(cd "$(dirname "$0")/.." && pwd)"
JAVA_HOME="$APP_HOME/jdk-21.0.10"

CLASSPATH="$APP_HOME/lib/*:$APP_HOME/lib/JMSMessageSender-2.0.jar"
CONFIG_FILE="$APP_HOME/config/loadtest.properties"

# Get the level, default to INFO if grep finds nothing
EXTRACTED_LOG_LEVEL=$(grep '^log.level.root[[:space:]]*=' "$CONFIG_FILE" | cut -d= -f2 | tr -d '\r')
LOG_LEVEL=$(echo "$EXTRACTED_LOG_LEVEL" | xargs)
LOG_LEVEL=${LOG_LEVEL:-INFO}

printf "\n\nLaunching: $FULL_CLASS"
printf "\nLog Level = $LOG_LEVEL\n"

"$JAVA_HOME/bin/java" \
  -Dlog.level.root="$LOG_LEVEL" \
  -Dconfig.file="$CONFIG_FILE" \
  -cp "$CLASSPATH" \
  "$FULL_CLASS"
