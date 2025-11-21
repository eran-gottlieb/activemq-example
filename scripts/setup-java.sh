#!/usr/bin/env zsh
# Helper to set JAVA_HOME to JDK 21 for this shell and run the build
set -euo pipefail

JAVA_HOME=$(/usr/libexec/java_home -v21)
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

echo "Using JAVA_HOME=$JAVA_HOME"

# Run Maven build
mvn -B clean verify
