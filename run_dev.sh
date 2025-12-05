cd backend
docker compose -f docker-compose.dev.yml up
sh run_local.sh
cd ..

cd file-service
docker compose -f docker-compose.dev.yml up
sh start-n-build.sh
cd ..

cd frontend
bun run dev
cd ..

docker compose -f docker-compose.nginx.yml up
