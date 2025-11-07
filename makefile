.PHONY: help
help:
	@echo "Tasks:\n`cat makefile | grep '^[a-z\-]\+:' | sed 's/://;s/^/  /'`"

.PHONY: publish-all
publish-all:
	@gradle publish publishToMavenLocal

.PHONY: publish-remote
publish-remote:
	@gradle publish

.PHONY: publish-local
publish-local:
	@gradle publishToMavenLocal

.PHONY: docs
docs:
	@gradle docs

.PHONY: start-test-containers
start-test-containers:
	@docker compose -f test/docker-compose.test.yml up -d

.PHONY: stop-test-containers
stop-test-containers:
	@docker compose -f test/docker-compose.test.yml down

run-test:
	@./gradlew :test:shadowJar
	@java -jar test/build/libs/test.jar