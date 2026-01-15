#!/bin/bash

echo "=== Starting Air Traffic Control System ==="
echo ""
echo "Make sure MongoDB is running on localhost:27017"
echo ""

cd "$(dirname "$0")"

mvn exec:java -Dexec.mainClass="com.atc.AirTrafficSystem"
