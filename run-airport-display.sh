#!/bin/bash

# Run Airport Display Board
cd "$(dirname "$0")"

echo "🛫 Starting Airport Display Board..."
mvn clean compile javafx:run
