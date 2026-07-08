.DEFAULT_GOAL := help
.PHONY: help lint lint-groovy lint-yaml test clean

help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-14s\033[0m %s\n", $$1, $$2}'

lint: lint-groovy lint-yaml ## Run all linters

lint-groovy: ## Lint Groovy sources (requires npm-groovy-lint on PATH)
	@if command -v npm-groovy-lint >/dev/null 2>&1; then \
		npm-groovy-lint --failon error "vars/**/*.groovy" "src/**/*.groovy"; \
	else \
		echo "npm-groovy-lint not found; install with 'npm i -g npm-groovy-lint'"; \
		echo "Falling back to a structural check that vars/ files exist"; \
		ls vars/*.groovy >/dev/null; \
	fi

lint-yaml: ## Lint YAML files (requires yamllint on PATH)
	@if command -v yamllint >/dev/null 2>&1; then \
		yamllint .github .pre-commit-config.yaml; \
	else \
		echo "yamllint not found; install with 'pip install yamllint'"; \
	fi

test: ## Run the Spock specs (requires Gradle in the consuming project)
	@echo "Run the Spock specs with your Groovy/Gradle toolchain, e.g.:"
	@echo "  gradle test"

clean: ## Remove build and report artifacts
	rm -rf build target test-results reports .gradle
