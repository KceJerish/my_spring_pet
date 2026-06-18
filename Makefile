.PHONY: install maven-install build run test clean mongo-install mongo-uninstall mongo-start mongo-stop mongo-status mongo-restart mongo-shell mongo-logs redis-start redis-stop redis-restart redis-status redis-shell redis-keys redis-type redis-get redis-ttl postgres-start postgres-stop postgres-restart postgres-status postgres-shell postgres-quartz-reset docker-build docker-run docker-stop docker-logs docker-shell docker-clean docker-compose-up docker-compose-down docker-compose-logs docker-compose-restart k8s-build-image k8s-deploy k8s-delete k8s-status k8s-logs k8s-restart k8s-shell k8s-port-forward k8s-describe k8s-update-ip help

## Show available commands
help:
	@echo "=== App Commands ==="
	@echo "  make install       - Install Java 25 and Maven"
	@echo "  make build         - Build the project (mvn package)"
	@echo "  make run           - Run the Spring Boot app (mvn spring-boot:run)"
	@echo "  make test          - Run tests (mvn test)"
	@echo "  make clean         - Clean build artifacts (mvn clean)"
	@echo ""
	@echo "=== MongoDB Commands ==="
	@echo "  make mongo-install - Install MongoDB via Homebrew"
	@echo "  make mongo-uninstall - Uninstall MongoDB"
	@echo "  make mongo-start   - Start MongoDB service"
	@echo "  make mongo-stop    - Stop MongoDB service"
	@echo "  make mongo-restart - Restart MongoDB service"
	@echo "  make mongo-status  - Check MongoDB status"
	@echo "  make mongo-shell   - Open MongoDB shell"
	@echo "  make mongo-logs    - Show MongoDB logs"
	@echo ""
	@echo "=== Redis Commands ==="
	@echo "  make redis-start   - Start Redis container (creates if not exists)"
	@echo "  make redis-stop    - Stop Redis container"
	@echo "  make redis-restart - Restart Redis container"
	@echo "  make redis-status  - Check Redis container status"
	@echo "  make redis-shell   - Open Redis CLI shell"
	@echo "  make redis-keys    - List all keys"
	@echo "  make redis-type    - Check key type:  make redis-type key=<key>"
	@echo "  make redis-get     - Get key value:   make redis-get key=<key>"
	@echo "  make redis-ttl     - Check key TTL:   make redis-ttl key=<key>"
	@echo ""
	@echo "=== PostgreSQL Commands ==="
	@echo "  make postgres-start   - Start PostgreSQL container (creates if not exists)"
	@echo "  make postgres-stop    - Stop PostgreSQL container"
	@echo "  make postgres-restart - Restart PostgreSQL container"
	@echo "  make postgres-status  - Check PostgreSQL container status"
	@echo "  make postgres-shell   - Open psql shell"
	@echo ""
	@echo "=== Docker Commands ==="
	@echo "  make docker-build          - Build Docker image"
	@echo "  make docker-run            - Run app in Docker container (standalone)"
	@echo "  make docker-stop           - Stop app Docker container"
	@echo "  make docker-logs           - View app Docker container logs"
	@echo "  make docker-shell          - Open shell in app container"
	@echo "  make docker-clean          - Remove app container and image"
	@echo ""
	@echo "=== Docker Compose Commands ==="
	@echo "  make docker-compose-up     - Start all services (app + databases)"
	@echo "  make docker-compose-down   - Stop all services"
	@echo "  make docker-compose-logs   - View all service logs"
	@echo "  make docker-compose-restart - Restart all services"
	@echo ""
	@echo "=== Kubernetes Commands ==="
	@echo "  make k8s-build-image   - Build Docker image for Rancher Desktop"
	@echo "  make k8s-deploy        - Deploy app to Kubernetes"
	@echo "  make k8s-delete        - Delete Kubernetes deployment"
	@echo "  make k8s-status        - Show pod status"
	@echo "  make k8s-logs          - View pod logs"
	@echo "  make k8s-restart       - Restart deployment"
	@echo "  make k8s-shell         - Open shell in pod"
	@echo "  make k8s-port-forward  - Port forward to access app locally"
	@echo "  make k8s-describe      - Describe pod details"
	@echo "  make k8s-update-ip     - Update host IP in K8s external services"
	@echo ""
	@echo "=== Full Setup ==="
	@echo "  make setup         - Install everything and start services"

## Install Java 25 and Maven
install:
	@echo "==> Checking Homebrew..."
	@command -v brew >/dev/null 2>&1 || (echo "Homebrew not found. Install from https://brew.sh" && exit 1)
	@echo "==> Installing Java 25..."
	brew install --cask temurin@25 || true
	@echo "==> Installing Maven..."
	brew install maven || true
	@echo "==> Install complete!"

## Build the project
build:
	mvn package -DskipTests

## Run the Spring Boot application
run:
	mvn spring-boot:run

## Run tests
test:
	mvn test

## Clean build artifacts
clean:
	mvn clean

## Full setup: install everything, start MongoDB, build project
setup: install mongo-install mongo-start build
	@echo "==> Setup complete! Run 'make run' to start the app."

## Install MongoDB via Homebrew
mongo-install:
	brew tap mongodb/brew
	brew install mongodb-community

## Uninstall MongoDB
mongo-uninstall:
	brew services stop mongodb-community || true
	brew uninstall mongodb-community
	brew untap mongodb/brew

## Start MongoDB service
mongo-start:
	brew services start mongodb-community

## Stop MongoDB service
mongo-stop:
	brew services stop mongodb-community

## Restart MongoDB service
mongo-restart:
	brew services restart mongodb-community

## Check MongoDB service status
mongo-status:
	brew services info mongodb-community

## Open MongoDB shell
mongo-shell:
	mongosh

## Show MongoDB logs
mongo-logs:
	cat $$(brew --prefix)/var/log/mongodb/mongo.log | tail -50

## Start Redis container (creates if not exists)
redis-start:
	@docker ps -a --format '{{.Names}}' | grep -q '^redis$$' \
		&& docker start redis \
		|| docker run -d --name redis -p 6379:6379 redis:latest
	@echo "==> Redis running on port 6379"

## Stop Redis container
redis-stop:
	docker stop redis

## Restart Redis container
redis-restart:
	docker restart redis

## Check Redis container status
redis-status:
	docker ps -a --filter name=redis --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

## Open Redis CLI shell
redis-shell:
	docker exec -it redis redis-cli

## List all keys in Redis
redis-keys:
	docker exec -it redis redis-cli KEYS '*'

## Check key type: make redis-type key=<key>
redis-type:
	docker exec -it redis redis-cli TYPE $(key)

## Get string value: make redis-get key=<key>
redis-get:
	docker exec -it redis redis-cli GET $(key)

## Check TTL: make redis-ttl key=<key>
redis-ttl:
	docker exec -it redis redis-cli TTL $(key)

## Start PostgreSQL container (creates if not exists)
postgres-start:
	@docker ps -a --format '{{.Names}}' | grep -q '^postgres$$' \
		&& docker start postgres \
		|| docker run -d --name postgres \
			-e POSTGRES_DB=springweb \
			-e POSTGRES_USER=springweb \
			-e POSTGRES_PASSWORD=springweb123 \
			-p 5432:5432 \
			-v $(HOME)/postgres-data:/var/lib/postgresql \
			postgres:latest
	@echo "==> PostgreSQL running on port 5432 (db=springweb, user=springweb, data=$(HOME)/postgres-data)"

## Stop PostgreSQL container
postgres-stop:
	docker stop postgres

## Restart PostgreSQL container
postgres-restart:
	docker restart postgres

## Check PostgreSQL container status
postgres-status:
	docker ps -a --filter name=postgres --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

## Open psql shell
postgres-shell:
	docker exec -it postgres psql -U springweb -d springweb

## Clear all Quartz tables (fixes stale job/trigger errors after restart)
postgres-quartz-reset:
	docker exec -i postgres psql -U springweb -d springweb -c \
		"DELETE FROM QRTZ_FIRED_TRIGGERS; DELETE FROM QRTZ_SIMPLE_TRIGGERS; DELETE FROM QRTZ_SIMPROP_TRIGGERS; DELETE FROM QRTZ_CRON_TRIGGERS; DELETE FROM QRTZ_BLOB_TRIGGERS; DELETE FROM QRTZ_TRIGGERS; DELETE FROM QRTZ_JOB_DETAILS; DELETE FROM QRTZ_CALENDARS; DELETE FROM QRTZ_PAUSED_TRIGGER_GRPS; DELETE FROM QRTZ_LOCKS; DELETE FROM QRTZ_SCHEDULER_STATE;"
	@echo "==> Quartz tables cleared"

## Build Docker image
docker-build:
	@echo "==> Building Docker image..."
	docker build -t spring-web-app:latest .
	@echo "==> Docker image built successfully!"

## Run app in Docker container (standalone - requires databases already running)
docker-run: docker-build
	@docker ps -a --format '{{.Names}}' | grep -q '^spring-web-app$$' \
		&& (echo "==> Container already exists, starting..." && docker start spring-web-app) \
		|| docker run -d --name spring-web-app \
			-p 8080:8080 \
			-e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/springweb \
			-e SPRING_DATASOURCE_USERNAME=springweb \
			-e SPRING_DATASOURCE_PASSWORD=springweb123 \
			-e SPRING_MONGODB_HOST=host.docker.internal \
			-e SPRING_MONGODB_PORT=27017 \
			-e SPRING_MONGODB_DATABASE=pets \
			-e SPRING_DATA_REDIS_HOST=host.docker.internal \
			-e SPRING_DATA_REDIS_PORT=6379 \
			spring-web-app:latest
	@echo "==> Spring Boot app running on http://localhost:8080"

## Stop app Docker container
docker-stop:
	docker stop spring-web-app || true

## View app Docker container logs
docker-logs:
	docker logs -f spring-web-app

## Open shell in app container
docker-shell:
	docker exec -it spring-web-app sh

## Remove app container and image
docker-clean: docker-stop
	docker rm spring-web-app || true
	docker rmi spring-web-app:latest || true
	@echo "==> Docker container and image removed"

## Start all services with docker-compose (app + databases)
docker-compose-up:
	@echo "==> Starting all services with docker-compose..."
	docker-compose up -d
	@echo "==> All services started! App available at http://localhost:8080"

## Stop all services
docker-compose-down:
	docker-compose down

## View all service logs
docker-compose-logs:
	docker-compose logs -f

## Restart all services
docker-compose-restart:
	docker-compose restart
	@echo "==> All services restarted!"

## Build Docker image for Kubernetes (Rancher Desktop)
k8s-build-image:
	@echo "==> Building Docker image for Rancher Desktop..."
	docker build -t spring-web-app:latest .
	@echo "==> Docker image built successfully!"
	@echo "==> Image is available to Kubernetes (using imagePullPolicy: Never)"

## Deploy app to Kubernetes
k8s-deploy: k8s-build-image
	@echo "==> Deploying to Kubernetes..."
	kubectl apply -f kube/configmap.yml
	kubectl apply -f kube/deployment.yml
	@echo "==> Deployment complete!"
	@echo "==> Run 'make k8s-status' to check status"
	@echo "==> Run 'make k8s-port-forward' to access the app"

## Delete Kubernetes deployment
k8s-delete:
	@echo "==> Deleting Kubernetes resources..."
	kubectl delete -f kube/deployment.yml --ignore-not-found=true
	kubectl delete -f kube/configmap.yml --ignore-not-found=true
	kubectl delete -f kube/external_postgres_service.yaml --ignore-not-found=true
	kubectl delete -f kube/external_mongodb_service.yaml --ignore-not-found=true
	kubectl delete -f kube/external_redis_service.yaml --ignore-not-found=true
	kubectl delete -f kube/mypod.yml --ignore-not-found=true
	@echo "==> Resources deleted!"

## Show pod status
k8s-status:
	@echo "==> Pod Status:"
	kubectl get pods -l app=spring-web
	@echo ""
	@echo "==> Service Status:"
	kubectl get svc spring-web

## View pod logs
k8s-logs:
	@echo "==> Tailing logs for spring-web pods..."
	kubectl logs -l app=spring-web -f --tail=100

## Restart deployment (rollout restart)
k8s-restart:
	kubectl rollout restart deployment/spring-web
	@echo "==> Deployment restarted!"

## Open shell in pod
k8s-shell:
	@echo "==> Opening shell in first available pod..."
	kubectl exec -it $$(kubectl get pods -l app=spring-web -o jsonpath='{.items[0].metadata.name}') -- sh

## Port forward to access app locally
k8s-port-forward:
	@echo "==> Port forwarding to http://localhost:8080"
	@echo "==> Press Ctrl+C to stop"
	kubectl port-forward svc/spring-web 8080:8080

## Describe pod details (useful for debugging)
k8s-describe:
	@echo "==> Describing pods..."
	kubectl describe pods -l app=spring-web

## Update host IP in Kubernetes external service files
k8s-update-ip:
	@./kube/update-host-ip.sh

## spotless apply
spotless-apply:
	mvn spotless:apply

## set java 25
java25:
 	export JAVA_HOME=`/usr/libexec/java_home -v 25.0.2`
