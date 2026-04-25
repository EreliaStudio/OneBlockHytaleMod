# OneBlock Mods

This repository splits the OneBlock gameplay into small, focused mods. Each mod builds to its own jar and can be deployed together to the server.

## Modules
- `mods/oneblock-block` Core OneBlock block, drop registry, player drop state, and the `/oneblock` command.
- `mods/oneblock-itemdropable` Dropable implementation for items.
- `mods/oneblock-entityspawndropable` Dropable implementation for entity spawns.
- `mods/oneblock-recipes` Unlock logic, expedition pool defaults, and recipe data loading.
- `mods/oneblock-workbench` Workbench assets and bench categories for unlock recipes.
- `mods/oneblock-salvager` Salvager bench assets and tiered salvaging outputs.
- `mods/oneblock-worldgeneration` Void world generation, spawn placement, and fall protection.

## Build And Deploy
From the repo root:
```bash
./gradlew buildAll
./gradlew deployAll
./gradlew buildAndDeployAll
```
The deploy tasks copy each shaded jar to `hytale-server/mods`.

## How Drops Work
- The OneBlock block chooses a pool id based on its block type. The default resolver uses the expedition name derived from the block id.
- The drop registry picks a dropable id by weighted random from that pool.
- A `Dropable` is an interface executed by the registry. Implementations live in other mods.
- Default drops and weights are provided by the recipes mod.

Key files:
- `mods/oneblock-block/src/main/java/com/EreliaStudio/OneBlock/Dropable.java`
- `mods/oneblock-block/src/main/java/com/EreliaStudio/OneBlock/OneBlockDropRegistry.java`
- `mods/oneblock-recipes/src/main/java/com/EreliaStudio/OneBlock/OneBlockExpeditionDefaults.java`

## Add A New Item Drop
Use this when you want OneBlock to drop an item.

1. Decide the pool id. Use an existing expedition name like `Meadow`, `Forest`, `Cave`, `Deep Cave`, `The Abyss`.
2. Pick the dropable id. For items, the dropable id is the item id. You can also use `item:ItemId`.
3. Add the drop as default or unlockable.

Default drop (always available):
- Edit `mods/oneblock-recipes/src/main/java/com/EreliaStudio/OneBlock/OneBlockExpeditionDefaults.java`.
- Add a `drop("ItemId", weight)` entry to the pool list.

Unlockable drop (crafted in the workbench):
1. Create an unlock item JSON in `mods/oneblock-recipes/src/main/resources/Server/Item/Items/UnlockRecipe/...`.
2. Add an entry to `mods/oneblock-recipes/src/main/resources/oneblock-recipes.json`.
   - Use `DropableId` for item drops.
3. Add a translation in `mods/oneblock-recipes/src/main/resources/Server/Languages/en-US/server.lang`.
4. Add the unlock item id to the workbench category in `mods/oneblock-workbench/src/main/resources/Server/Item/Items/OneBlockUpgrader/Bench_OneBlockUpgrader.json`.

Note: the unlock items still contain tags, but the current loader ignores tags. `oneblock-recipes.json` is the source of truth.

## Add A New Entity Drop
Use this when you want OneBlock to spawn an entity.

- Use `entity:EntityId` as the dropable id, or use `EntityId` in `oneblock-recipes.json`.
- Default drops go in `OneBlockExpeditionDefaults.java`.
- Unlockables go in `oneblock-recipes.json` and an unlock item JSON.

## Add A Recipe Drop Item
Recipe drop items are consumables that teach a crafting recipe.

1. Add a JSON file under `mods/oneblock-recipes/src/main/resources/Server/Item/Items/RecipeDrop/...`.
2. Add translations in `mods/oneblock-recipes/src/main/resources/Server/Languages/en-US/server.lang`.
3. If you want OneBlock to drop the recipe item, add it to `OneBlockExpeditionDefaults.java` or create an unlockable entry.

## Add A New Dropable Type
If you need a custom behavior:
1. Create a new mod that implements `Dropable`.
2. Register the dropable in the base registry during plugin setup.
   - Use `OneBlockPlugin.getInstance().getDropRegistry().registerDropable(...)`.
3. Use a distinct dropable id format so your mod can recognize it.

## Workbench Categories
The OneBlock Upgrader bench defines categories and their recipe lists in:
- `mods/oneblock-workbench/src/main/resources/Server/Item/Items/OneBlockUpgrader/Bench_OneBlockUpgrader.json`

To expose a new unlock item in the UI, add its item id to the relevant `Recipes` array.

## Salvager Tiers
The salvager output table is data-driven:
- Config: `mods/oneblock-salvager/src/main/resources/oneblock-salvager-drops.json`
- Bench tier upgrades: `mods/oneblock-salvager/src/main/resources/Server/Item/Items/OneBlockSalvager/Bench_OneBlockSalvager.json`

Special output ids:
- `RandomCrystal` picks a random crystal (blue/red/yellow).
- `Empty` removes the output (failure).

## World Generation
The worldgen mod keeps the existing behavior:
- Void world.
- OneBlock placed at `x=0, y=100, z=0`.
- Spawn at `x=0.5, y=102, z=0.5`.
