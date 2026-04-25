# OneBlock-Salvager

Salvager bench and tiered output logic.

## What It Does
- Adds a processing bench that consumes rubble recipes.
- Converts `Ingredient_Crystal_White` outputs into weighted results based on bench tier.

## Key Files
- `src/main/java/com/EreliaStudio/OneBlock/OneBlockSalvageChanceSystem.java`
- `src/main/resources/Server/Item/Items/OneBlockSalvager/Bench_OneBlockSalvager.json`
- `src/main/resources/oneblock-salvager-drops.json`

## Config
`oneblock-salvager-drops.json` defines outputs per tier:
- `TierLevel` is the bench tier.
- `Entries` is a weighted list of outputs.

Special output ids:
- `RandomCrystal` picks a random crystal (blue, red, yellow).
- `Empty` removes the output (failure case).

## Tier Upgrades
Bench tiers are defined in `Bench_OneBlockSalvager.json` under `Bench.TierLevels`.
Upgrade materials are part of the bench definition and use the native bench upgrade system.

## Dependency
Standalone. Can be used with OneBlock or any other system that outputs `Ingredient_Crystal_White`.