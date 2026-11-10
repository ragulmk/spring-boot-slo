PROM_IMAGE  := prom/prometheus:v3.5.0
MAVEN_IMAGE := maven:3.9-eclipse-temurin-21

.PHONY: help up down logs test test-app test-rules chaos-errors chaos-latency chaos-off reset

help:            ## Show available commands
	@grep -E '^[a-z-]+:.*##' $(MAKEFILE_LIST) | awk -F':.*## ' '{printf "  make %-14s %s\n", $$1, $$2}'

up:              ## Build the app and start everything
	docker compose up -d --build
	@echo "App: http://localhost:8080/api/orders   Grafana: http://localhost:3000"

down:            ## Stop everything (keeps data)
	docker compose down

logs:            ## Follow app logs
	docker compose logs -f orders-service

test: test-app test-rules  ## Run all tests

test-app:        ## Run Spring Boot tests (uses Docker, no local Java needed)
	docker run --rm -v "$(PWD)/app:/src" -v maven-cache:/root/.m2 -w /src $(MAVEN_IMAGE) mvn -B verify

test-rules:      ## Validate Prometheus config and run SLO rule unit tests
	docker run --rm -v "$(PWD)/prometheus:/etc/prometheus:ro" --entrypoint promtool $(PROM_IMAGE) \
		check config /etc/prometheus/prometheus.yml
	docker run --rm -v "$(PWD)/prometheus:/etc/prometheus:ro" --entrypoint promtool $(PROM_IMAGE) \
		test rules /etc/prometheus/tests/slo_test.yml

chaos-errors:    ## Inject 20% errors (watch the fast burn alert fire)
	./scripts/chaos.sh errors 0.2

chaos-latency:   ## Inject 400ms latency (breaks the latency SLO)
	./scripts/chaos.sh latency 400

chaos-off:       ## Stop injecting failures
	./scripts/chaos.sh off

reset:           ## DANGER: stop and delete all stored metrics
	docker compose down -v
