# OneBlock

Paper plugin for a per-player OneBlock void world.

## Features

- Automatic void world creation
- One bedrock + one regenerating block per player
- Islands placed on a 1000-block grid
- Saved level, blocks broken, and island coordinates
- Purple boss bar level display
- `/ob home`, `/ob tp`, `/ob info`, `/ob reset`, `/ob reload`
- GitHub Actions build workflow

## Build locally

Install Java 21 and Gradle, then run:

```bash
gradle clean build
```

The jar will be in:

```text
build/libs/
```

## GitHub Actions

Push this repo to GitHub. The workflow in `.github/workflows/build.yml` will build the jar and upload it as an artifact.
