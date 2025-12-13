#!/bin/bash

# sets env variables
set -a
. ../.env
set +a

echo https://repo1.maven.org/maven2/fish/payara/extras/payara-micro/${PAYARA_VERSION}/payara-micro-${PAYARA_VERSION}.jar

curl -L -o payara-micro-${PAYARA_VERSION}.jar https://repo1.maven.org/maven2/fish/payara/extras/payara-micro/${PAYARA_VERSION}/payara-micro-${PAYARA_VERSION}.jar
