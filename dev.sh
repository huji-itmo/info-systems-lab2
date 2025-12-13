
./gradlew :file-service:jibDockerBuild :backend:jibDockerBuild

docker compose -f docker-compose.dev.yml up --build
