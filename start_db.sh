#!/bin/bash

DB_DIR="db"
JAR_NAME="h2-1.4.197.jar"
H2_JAR="$DB_DIR/$JAR_NAME"
H2_DOWNLOAD_URL="https://repo1.maven.org/maven2/com/h2database/h2/1.4.197/h2-1.4.197.jar"

if [ ! -d "$DB_DIR" ]; then
  mkdir -p "$DB_DIR"
fi

if [ ! -f "$H2_JAR" ]; then
  wget "$H2_DOWNLOAD_URL" -P "$DB_DIR"
fi

cd "$DB_DIR"
java -cp "$JAR_NAME" org.h2.tools.Server
