#!/bin/sh

# Launch the checked-in Gradle Wrapper on Unix systems.
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd) || exit 1

if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
    if [ ! -x "$JAVACMD" ]; then
        echo "ERROR: JAVA_HOME does not point to a valid Java installation: $JAVA_HOME" >&2
        exit 1
    fi
else
    JAVACMD=java
    if ! command -v "$JAVACMD" >/dev/null 2>&1; then
        echo "ERROR: JAVA_HOME is not set and java was not found in PATH." >&2
        exit 1
    fi
fi

exec "$JAVACMD" -Xms64m -Xmx64m \
    -Dorg.gradle.appname=gradlew \
    -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \
    org.gradle.wrapper.GradleWrapperMain "$@"
