# Changelog

All notable changes to this project are documented here. The format is based on
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-07-08

### Added

- Global pipeline steps under `vars/`: `standardPipeline`, `buildDockerImage`,
  `notifySlack`, `semanticVersion`, and `runUnitTests`, each with a matching
  `.txt` documentation file.
- Library classes under `src/org/acme/jenkins/`: `Config`, `Docker`, `GitUtils`,
  and `Notifier`.
- Resource templates under `resources/org/acme/jenkins/`: `slack-message.json`,
  `email-template.html`, and `Dockerfile.tpl`.
- Example consumer `Jenkinsfile`s under `examples/`.
- Spock unit specs under `test/`.
- Documentation, CI pipeline, pre-commit hooks, and open-source project files.
