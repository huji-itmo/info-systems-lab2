
./gradlew \
    file-service:jibDockerBuild --image="info-systems-lab2/file-service:jib-dev" \
    backend:jibDockerBuild --image="info-systems-lab2/backend:jib-dev"

docker compose -f docker-compose.dev.yml up --build
