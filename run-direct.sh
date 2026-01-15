#!/bin/bash

echo "=== Starting Air Traffic Control System ==="
echo ""

# Unset snap library paths to avoid conflicts
unset LD_LIBRARY_PATH
unset LD_PRELOAD

cd "$(dirname "$0")"

# Use system Java, not snap
/usr/lib/jvm/java-25-openjdk-amd64/bin/java -cp target/classes com.atc.AirTrafficSystem
