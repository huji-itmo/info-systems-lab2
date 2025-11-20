#!/bin/bash

# sets env variables
set -a
. ../.env
set +a

# Construct expected JAR filename from env or default
JAR_NAME="payara-micro-${PAYARA_VERSION:-6.2025.10}.jar"

export DB_HOST=localhost

if [ ! -f "$JAR_NAME" ]; then
    echo "Payara Micro JAR not found: $JAR_NAME"
    echo "Running download_payara.sh..."
    sh ./download_payara.sh
else
    echo "Payara Micro JAR found: $JAR_NAME"
fi

./gradlew war

java -Xmx1024m -Xms512m \
    -Djava.net.preferIPv4Stack=true \
    -Dorg.example.level=FINE \
    -jar ${JAR_NAME} \
    --deploy app/build/libs/app.war \
    --port 8080 \
    --contextroot "/"
