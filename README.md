# OneBlock Mods

This repository splits the OneBlock gameplay into small, focused mods. Each mod builds to its own jar and can be deployed together to the server.

Players should start with the [OneBlock Player Guide](PLAYER_GUIDE.md), which
explains the gameplay loop, expeditions, crystals, tools, dungeons, multiplayer,
and progression without requiring development knowledge.

## Modules

- `mods/oneblock` provides position-scoped OneBlock gameplay, progression, drops, dungeons, roots, and HUDs. It does not create or configure worlds.
- `mods/oneblock-islands` provides island creation, void generation, fall protection, ownership, membership, access control, and `/island` commands.

## Build And Deploy
From the repo root:
```bash
./gradlew buildAll
./gradlew deployAll
./gradlew buildAndDeployAll
```
The deploy tasks copy each shaded jar to `hytale-server/mods`.

## Dedicated OneBlock Server

The `:oneblock-islands` companion module adds UUID-based island ownership,
membership, `/island`, world-entry enforcement, and island edit protection. It
creates a separate void world for each island, places its OneBlock at the
center, and provides island-only fall protection. It never modifies the
server's `default` spawn world, which you configure manually.

## How Drops Work
- The OneBlock block chooses a pool id based on its block type. The default resolver uses the expedition name derived from the block id.
- The drop registry picks a dropable id by weighted random from that pool.
- A `Dropable` is an interface executed by the registry. Implementations live in other mods.
- Default drops and weights are provided by the recipes mod.

Key files:
- `mods/oneblock/src/main/java/com/EreliaStudio/OneBlock/Dropable.java`
- `mods/oneblock/src/main/java/com/EreliaStudio/OneBlock/OneBlockDropRegistry.java`
- `mods/oneblock/src/main/java/com/EreliaStudio/OneBlock/OneBlockExpeditionDefaults.java`

## Add A New Item Drop
Use this when you want OneBlock to drop an item.

1. Decide the pool id. Use an existing expedition name like `Meadow`, `Forest`, `Cave`, `Deep Cave`, `The Abyss`.
2. Pick the dropable id. For items, the dropable id is the item id. You can also use `item:ItemId`.
3. Add the drop as default or unlockable.

Default drop (always available):
- Edit `mods/oneblock/src/main/java/com/EreliaStudio/OneBlock/OneBlockExpeditionDefaults.java`.
- Add a `drop("ItemId", weight)` entry to the pool list.

Unlockable drop (crafted in the workbench):
1. Create an unlock item JSON in `mods/oneblock/src/main/resources/Server/Item/Items/UnlockRecipe/...`.
2. Add an entry to `mods/oneblock/src/main/resources/oneblock-recipes.json`.
   - Use `DropableId` for item drops.
3. Add a translation in `mods/oneblock/src/main/resources/Server/Languages/en-US/server.lang`.
4. Add the unlock item id to the workbench category in `mods/oneblock-workbench/src/main/resources/Server/Item/Items/OneBlockUpgrader/Bench_OneBlockUpgrader.json`.

Note: the unlock items still contain tags, but the current loader ignores tags. `oneblock-recipes.json` is the source of truth.

## Add A New Entity Drop
Use this when you want OneBlock to spawn an entity.

- Use `entity:EntityId` as the dropable id, or use `EntityId` in `oneblock-recipes.json`.
- Default drops go in `OneBlockExpeditionDefaults.java`.
- Unlockables go in `oneblock-recipes.json` and an unlock item JSON.

## Add A Recipe Drop Item
Recipe drop items are consumables that teach a crafting recipe.

1. Add a JSON file under `mods/oneblock/src/main/resources/Server/Item/Items/RecipeDrop/...`.
2. Add translations in `mods/oneblock/src/main/resources/Server/Languages/en-US/server.lang`.
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

The core OneBlock mod never changes a world's generator. A OneBlock is a
registered position and owner inside any existing world. The islands companion
owns the specialized world lifecycle: `/island` creates a persistent void
world, places the owner's registered OneBlock at `(0, 100, 0)`, sets the spawn
above it, and enables fall recovery for that island only. The server's
`default` world is untouched.

## Multiplayer Expeditions

The core registry keys OneBlocks by world, position, and owner. Multiple owners
can therefore run independent OneBlocks in one ordinary world. The islands mod
maps an island's owner and authorized members to the same registered root, so
the group shares expedition and dungeon progress.

The progress HUD is restored only when the player's current world contains the
OneBlock context available to that player. Entering `default` or another world
without an accessible OneBlock clears it.

Admin actions:

- `/oneblock status`, `start`, and `stop` operate on the target player's accessible OneBlock in the current world.
- `/island`, `/island create`, and `/island join` own island-world creation and travel.

Server code can place a registered OneBlock in any already loaded world without
changing that world's generator:

```java
OneBlockPlugin.getInstance().initializeRoot(world, position, ownerUuid, ownerName);
```
