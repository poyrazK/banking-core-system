.PHONY: build up down restart ps logs help

# Variables
MVN = mvn
DOCKER_COMPOSE = docker compose

# Default target
help:
	@echo "Core Banking System (CBS) Management Commands:"
	@echo "  make build    - Build all modules locally using Maven (skipping tests)"
	@echo "  make up       - Start all services using Docker Compose"
	@echo "  make down     - Stop and remove all containers"
	@echo "  make restart  - Build and then start all services"
	@echo "  make ps       - List all running services"
	@echo "  make logs     - Follow all service logs"

build:
	$(MVN) clean package -DskipTests -T 1C

up:
	$(DOCKER_COMPOSE) up -d --build

down:
	$(DOCKER_COMPOSE) down

restart: build up

ps:
	$(DOCKER_COMPOSE) ps

logs:
	$(DOCKER_COMPOSE) logs -f
