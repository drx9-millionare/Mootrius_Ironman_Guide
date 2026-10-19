# Mootrius Ironman Helper

Unofficial RuneLite plugin based on [Mootrius Ironman Guide](https://oldschool.runescape.wiki/w/Guide:Mootrius_Ironman_Guide).

## Features
- Searchable guide sidebar with section navigation
- Manual task completion and per-account progress via RuneLite configuration
- Optional next-task overlay
- Experimental automatic skill target and quest completion checks
- Guide text extracted from the supplied 56-page PDF

## Run locally
Requires Java 11 and an internet connection to resolve RuneLite dependencies. Run `./gradlew run` on macOS/Linux or `gradlew.bat run` on Windows. `./gradlew test` runs unit tests.

## Development status
**Not compiled or tested in RuneLite in this environment.** PDF extraction can contain erroneous, duplicated, or incomplete steps; review against the original guide before public release. Automatic quest detection is heuristic and should be validated against the current RuneLite API. In-game map/NPC markers are **not yet implemented**.

## Attribution
Original guide authored by Mootrius on the OSRS Wiki. Unofficial adaptation; confirm attribution and licensing before distributing guide content publicly.
