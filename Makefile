.PHONY: install maven-install build run test clean mongo-install mongo-uninstall mongo-start mongo-stop mongo-status mongo-restart mongo-shell mongo-logs redis-start redis-stop redis-restart redis-status redis-shell redis-keys redis-type redis-get redis-ttl help

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

