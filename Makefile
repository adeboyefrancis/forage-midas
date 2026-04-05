# Fix: $(shell echo $$HOME) or using the built-in $(HOME) variable is safer
HOME_DIR := $(HOME)

# Use a single shell for the entire recipe to allow 'source' to work
.ONESHELL:
SHELL := /bin/bash

.PHONY: all update-package install-java-tools verify-tools

all: update-package install-java-tools verify-tools

update-package:
	@echo "==> Updating and Upgrading Package Manager..."
	sudo apt update && sudo apt upgrade -y

install-java-tools:
	@echo "==> Installing Java 17..."
	sudo apt install -y openjdk-17-jdk

	@echo "==> Installing SDKMAN dependencies..."
	sudo apt install -y curl zip unzip

	@echo "==> Installing SDKMAN..."
	@if [ ! -d "$(HOME_DIR)/.sdkman" ]; then \
		curl -s "https://get.sdkman.io" | bash; \
	else \
		echo "SDKMAN already installed. Skipping."; \
	fi

	@echo "==> Installing Maven..."
	sudo apt install maven -y

verify-tools:
	@echo "==> Verifying Installations..."
	@java -version
	@source "$(HOME_DIR)/.sdkman/bin/sdkman-init.sh" && sdk version
