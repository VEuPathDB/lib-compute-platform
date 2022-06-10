.PHONY: what
what:
	@echo what are you doing?

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
