# OneBlock Paper Plugin

Target: Paper 1.21.1
Java: 21

## Features
- Generates a dedicated void world called `oneblock_world`
- Creates a private OneBlock island for each player on first join
- Places islands on a spaced grid (1000 blocks apart)
- Uses the same block as the regenerating OneBlock
- Regenerates the OneBlock 1 tick after it is broken
- Tracks blocks broken and levels
- Shows a purple boss bar with the player's current level
- Saves player island data to `plugins/OneBlock/players.yml`

## Commands
- `/ob home`
- `/ob tp <player>`
- `/ob info [player]`
- `/ob reset <player>`
- `/ob reload`

## Build
Run:

```bash
mvn clean package
```

Then take the jar from:

`target/oneblock-1.0.0.jar`

## Notes
- This project is set up for Paper `1.21.1-R0.1-SNAPSHOT`
- If you actually meant a different Paper version, update the dependency in `pom.xml`
