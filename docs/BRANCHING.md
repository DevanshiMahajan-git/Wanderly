# Git Branching Convention

The main branch contains the stable version of the Wanderly project.

Use the following naming conventions for development:

- `feature/<short-description>` — new functionality
- `fix/<short-description>` — bug fixes
- `test/<short-description>` — testing-related changes
- `docs/<short-description>` — documentation changes

Examples:
- `feature/trip-search`
- `fix/status-validation`
- `test/selenium-trip-creation`
- `docs/setup-guide`

Changes should be developed on a separate branch, committed with descriptive messages, and merged into `main` after review and verification.
