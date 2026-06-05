#!/bin/sh
# Maven Wrapper script
# Automatically downloads and runs Maven if not installed

set -e

# Determine the project base directory
MAVEN_PROJECTBASEDIR="${MAVEN_BASEDIR:-$(cd "$(dirname "$0")" && pwd)}"
MAVEN_WRAPPER_PROPERTIES="${MAVEN_PROJECTBASEDIR}/.mvn/wrapper/maven-wrapper.properties"

# Read distribution URL from properties
if [ -f "${MAVEN_WRAPPER_PROPERTIES}" ]; then
    distributionUrl=$(grep "distributionUrl" "${MAVEN_WRAPPER_PROPERTIES}" | cut -d'=' -f2-)
fi

distributionUrl="${distributionUrl:-https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip}"

MAVEN_HOME="${MAVEN_PROJECTBASEDIR}/.mvn/wrapper/maven"

if [ ! -d "${MAVEN_HOME}" ]; then
    echo "Downloading Maven from ${distributionUrl}..."
    mkdir -p "${MAVEN_HOME}"
    
    if command -v wget > /dev/null 2>&1; then
        wget -q -O /tmp/maven.zip "${distributionUrl}"
    elif command -v curl > /dev/null 2>&1; then
        curl -sSL -o /tmp/maven.zip "${distributionUrl}"
    else
        echo "Error: neither wget nor curl found. Cannot download Maven."
        exit 1
    fi
    
    unzip -q -o /tmp/maven.zip -d "${MAVEN_HOME}"
    rm -f /tmp/maven.zip
    
    # Move contents up one level (the zip contains a directory)
    mv "${MAVEN_HOME}"/apache-maven-*/* "${MAVEN_HOME}/"
    rmdir "${MAVEN_HOME}"/apache-maven-* 2>/dev/null || true
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"
