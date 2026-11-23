#!/bin/sh
# =====================================================================
#  Maven Wrapper (Linux / macOS) - Descarga Apache Maven si no esta
#  presente y ejecuta Maven sobre el proyecto actual.
# =====================================================================
set -e

MAVEN_VERSION=3.9.6
MVNW_DIR="$(cd "$(dirname "$0")" && pwd)"
MAVEN_HOME="$MVNW_DIR/.mvn/apache-maven-$MAVEN_VERSION"

if [ ! -x "$MAVEN_HOME/bin/mvn" ]; then
  echo "[mvnw] Apache Maven $MAVEN_VERSION no encontrado. Descargando..."
  mkdir -p "$MVNW_DIR/.mvn"
  URL="https://archive.apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$URL" -o "$MVNW_DIR/.mvn/maven.tar.gz"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$URL" -O "$MVNW_DIR/.mvn/maven.tar.gz"
  else
    echo "[mvnw] ERROR: se requiere curl o wget para descargar Maven." >&2
    exit 1
  fi
  tar -xzf "$MVNW_DIR/.mvn/maven.tar.gz" -C "$MVNW_DIR/.mvn"
  rm -f "$MVNW_DIR/.mvn/maven.tar.gz"
fi

exec "$MAVEN_HOME/bin/mvn" "$@"
