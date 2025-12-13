
./gradlew :file-service:jibDockerBuild :payara-service:jibDockerBuild

docker compose -f docker-compose.dev.yml up --build
