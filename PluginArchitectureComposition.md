# OneBlock — Plugin Architecture & Composition

## Project Overview

The OneBlock mod is a **single Gradle project** (`oneblock`) compiled into one shaded JAR and deployed as a single Hytale plugin. All game systems — the drop engine, expedition progression, dungeon system, HUD, world setup, and fall-back protection — live in this one module.

---

## Repository Layout

```
OneBlockHytaleMod/
│
├── build.gradle.kts              # Root Gradle config
├── settings.gradle.kts           # Lists the oneblock sub-project
│
├── libs/
│   └── HytaleServer.jar          # Hytale server API (compile-only, not shipped)
│
├── mods/
│   └── oneblock/                 # The single plugin module
│
├── hytale-server/                # Local development server
│   ├── HytaleServer.jar          # The server executable
│   ├── config.json               # Server config (name, max players, default world, etc.)
│   ├── mods/                     # Deployed JARs land here (output of buildAndDeployAll)
│   ├── universe/                 # World data, player data, saves
│   └── logs/                     # Server log output
│
├── expeditionsTemplate.json      # Reference document for expedition design (not loaded at runtime)
├── ItemList.md                   # Reference list of Hytale item/entity IDs
├── GDD.md                        # Game Design Document
├── HowToCreateExpeditions.md     # Step-by-step guide for adding new expeditions
└── tools/                        # Build utilities
```

### Module internal layout

```
mods/oneblock/
├── build.gradle.kts
└── src/
    └── main/
        ├── java/
        │   └── com/EreliaStudio/OneBlock/
        │       └── *.java
        └── resources/
            ├── manifest.json
            ├── oneblock-world-config-template.json
            └── Server/
                └── Item/Items/
                    ├── OneBlock/              # Block variants (Default, Cave_Entry, …)
                    ├── Crystal/Expedition/    # Crystal items
                    ├── OneBlockEnchanter/     # Crystal Enchanter bench
                    ├── OneBlockDungeonEnchanter/ # Dungeon Enchanter bench
                    └── CustomItems/           # Misc items (Locket_GobelinDungeon, ExpeditionPoint)
```

---

## Java Classes

### Plugin entry point

| Class | Role |
|-------|------|
| `OneBlockPlugin` | Plugin entry point. Wires all services (HUD, drop registry, state providers), registers event handlers, registers the `oneblock_crystal_use` interaction codec, and sets up the void world. |

---

### Drop engine

| Class | Role |
|-------|------|
| `Dropable` | Interface: `getId()` + `execute(DropableContext)` |
| `DropableContext` | Data passed to a dropable: store, world, source block position, reward spawn position, player entity ref |
| `ItemDropable` | Implements `Dropable` — spawns an item entity on the ground with optional quantity |
| `EntitySpawnDropable` | Implements `Dropable` — spawns an NPC via reflection |
| `OneBlockEntitySpawner` | Reflection-based wrapper around `SpawnNPCInteraction` |
| `OneBlockDropId` | Parses drop ID prefixes (`entity:`, `npc:`, `item:`, bare) |
| `OneBlockDropRegistry` | Central registry: weighted selection, dropable handler map, `executeDropable(id, ctx, qty)` |

---

### Block & pool resolution

| Class | Role |
|-------|------|
| `OneBlockBlockIds` | Constants: `DEFAULT_BLOCK_ID = "OneBlock_Block_Default"`, `ONEBLOCK_POSITION = (0, 100, 0)` |
| `OneBlockBlockUtil` | Returns true if a broken block has the `Blocks.OneBlock` category |
| `OneBlockPools` | Holds the active `OneBlockPoolResolver`; defaults to returning "Meadow" if none set |
| `OneBlockPoolResolver` | Interface: `resolvePoolId(BlockType)` |
| `OneBlockExpeditionPoolResolver` | Implements `OneBlockPoolResolver` — delegates to `OneBlockExpeditionResolver.expeditionFromBlockType()` |
| `OneBlockExpeditionResolver` | Extracts expedition ID from block IDs (`OneBlock_Block_<X>` → `X`) and crystal item IDs (`OneBlock_Crystal_<X>` → `X`); resolves block ID for a given expedition ID via `OneBlockExpeditionDefaults` |

---

### Expedition system

| Class | Role |
|-------|------|
| `OneBlockExpeditionDefaults` | Hardcoded definitions for all expeditions. Each `ExpeditionDefinition` holds: `expeditionId`, `blockId`, `ticks`, `drops` (weighted), `mandatoryRewards` (always on completion), `randomBundles` (one picked by weight on completion). Provides `crystalReward()` helper which creates a `CompletionRewardDefinition` that also calls `CraftingPlugin.learnRecipe()`. |
| `OneBlockExpeditionStateProvider` | Expedition state: active expedition ID, ticks remaining, total ticks. `onBreak()` decrements ticks and returns the completed expedition ID when reaching zero. Persists to `oneblock-expedition.json`. |
| `OneBlockCrystalInteraction` | `SimpleInstantInteraction` — right-clicking the OneBlock with a crystal starts (or resets) the expedition/dungeon, shows the HUD, and consumes the crystal. Differentiates dungeon vs. expedition via `OneBlockDungeonDefaults.isDungeon()`. |
| `OneBlockInteractionUtil` | Shared helpers: `consumeHeldItem`, `finish`, `skip`, `fail` |

---

### Dungeon system

| Class | Role |
|-------|------|
| `OneBlockDungeonDefaults` | Hardcoded definitions for all dungeons. Each `DungeonDefinition` holds: `dungeonId`, `blockId`, `waves` (list of entity ID lists per wave), `completionRewards`. Currently empty — no dungeons are defined. |
| `OneBlockDungeonStateProvider` | Dungeon state: active dungeon ID and current wave index. `onWaveCompleted()` advances the wave and returns the completed dungeon ID when all waves are done. Persists to `oneblock-dungeon.json`. |

---

### Break handling

| Class | Role |
|-------|------|
| `OneBlockBreakSystem` | `EntityEventSystem<BreakBlockEvent>`. On each valid OneBlock break: if a dungeon is active → `handleDungeonBreak`; otherwise → `handleExpeditionBreak`. Updates the HUD and block state after each break. Ignores creative-mode players. |

`handleExpeditionBreak` flow:
1. Resolve pool ID from block type.
2. Pick a reward from the drop registry.
3. Decrement expedition ticks via `expeditionState.onBreak()`.
4. Replace the block (same block if expedition ongoing; default block if completed).
5. Execute the dropable.
6. If expedition completed: execute mandatory rewards + one random bundle; hide HUD.
7. If expedition ongoing: update HUD tick bar.

`handleDungeonBreak` flow:
1. Get the current wave entity list.
2. Find solid-ground spawn positions within 5 blocks.
3. Spawn each entity in the wave at a shuffled spawn position.
4. Advance wave via `dungeonState.onWaveCompleted()`.
5. If dungeon completed: set block to default; execute completion rewards; show dungeon-complete HUD.
6. If waves remain: restore dungeon block; update HUD wave bar.

---

### HUD system

| Class | Role |
|-------|------|
| `OneBlockHudService` | Per-player HUD manager (keyed by `PlayerRef`). Exposes `showExpeditionStarted`, `updateExpeditionTicks`, `showExpeditionCompleted`, `showExpeditionUnlocked`, `showDungeonStarted`, `updateDungeonWave`, `showDungeonCompleted`, `restoreExpeditionHud`. Hides HUD on completion. |
| `OneBlockProgressHud` | Custom HUD element: a named progress bar. Exposes `setTitle`, `setProgress`, `setTitleAndProgress`. |

---

### Notifications

| Class | Role |
|-------|------|
| `OneBlockNotifier` | Chat + HUD notification helpers: `notifyExpeditionUnlocked`, `notifyExpeditionStarted`, `notifyExpeditionCompleted`, `notifyDungeonStarted`, `notifyDungeonCompleted`. Used by `OneBlockBreakSystem` and `OneBlockCrystalInteraction`. |

---

### World setup

| Class | Role |
|-------|------|
| `OneBlockWorldBootstrap` | Ensures the default world config is set to void (no terrain). Runs on startup and on world load. |
| `OneBlockWorldInitializer` | On world load: installs the void world-gen provider, places the starting OneBlock (at the appropriate block ID based on active expedition/dungeon), and sets the spawn point to (0.5, 102, 0.5). |
| `OneBlockFallBackSystem` | `ArchetypeTickingSystem` — teleports any entity below Y=85 back to spawn. |

---

### Admin commands

| Class | Role |
|-------|------|
| `OneBlockCommand` | `/oneblock status|start <id>|stop|list` admin command for manually controlling expedition/dungeon state. |

---

## Dependency Graph

```
HytaleServer.jar  (compile-only, not shipped)
        │
   oneblock
   (all systems in one JAR)
```

---

## Key Runtime Data Flows

### Block Break → Drop → Tick (Expedition)

```
Player breaks block
  → BreakBlockEvent  (OneBlockBreakSystem)
  → Verify block category == "Blocks.OneBlock"  (OneBlockBlockUtil)
  → Verify player not in creative mode
  → dungeonState.isDungeonActive()?
      ├─ yes → handleDungeonBreak (see below)
      └─ no  → handleExpeditionBreak:
                  OneBlockPools.resolvePoolId(blockType)
                      → OneBlockExpeditionPoolResolver
                      → OneBlockExpeditionResolver.expeditionFromBlockType()
                  → OneBlockDropRegistry.pickReward(poolId, drops)
                  → expeditionState.onBreak()
                      ├─ ticks > 0  → replace block with same block type
                      └─ ticks == 0 → replace block with DEFAULT_BLOCK_ID
                                    → executeExpeditionCompletionRewards()
                                        → mandatory rewards (items + crystalReward → learnRecipe)
                                        → one random bundle (if any)
                                    → HudService.showExpeditionCompleted()
                  → dropRegistry.executeDropable(rewardId)  ← ItemDropable or EntitySpawnDropable
                  → HudService.updateExpeditionTicks()  (if expedition still ongoing)
```

### Crystal Use → Expedition Start

```
Player right-clicks OneBlock with expedition crystal
  → OneBlockCrystalInteraction.firstRun()
  → Resolve expeditionId from item ID (strip "OneBlock_Crystal_" prefix)
  → OneBlockDungeonDefaults.isDungeon(expeditionId)?
      ├─ yes → dungeonStateProvider.startDungeon(expeditionId)
             → HudService.showDungeonStarted(player, dungeonId, waveCount)
      └─ no  → OneBlockExpeditionResolver.blockIdForExpedition(expeditionId)
             → world.setBlock(ONEBLOCK_POSITION, newBlockId)
             → expeditionStateProvider.startExpedition(expeditionId, ticks)
             → HudService.showExpeditionStarted(player, expeditionId, ticks)
  → consumeHeldItem
```

### Dungeon Break → Wave Spawn

```
Player breaks OneBlock during a dungeon
  → dungeonState.getActiveDungeonId() + getCurrentWaveIndex()
  → OneBlockDungeonDefaults.getWave(dungeonId, waveIndex)  → entity ID list
  → findDungeonSpawnBlocks(world, pos)  → solid-ground positions within radius 5
  → for each entity: dropRegistry.executeDropable(entityId, spawnContext)
  → dungeonState.onWaveCompleted()
      ├─ waves remain → restore dungeon block; HudService.updateDungeonWave()
      └─ all done     → set block to DEFAULT_BLOCK_ID
                      → executeDungeonCompletionRewards()
                      → HudService.showDungeonCompleted()
```

### Player Reconnect → HUD Restore

```
PlayerReadyEvent fires
  → expeditionStateProvider.hasActiveExpedition()?
      └─ yes → HudService.restoreExpeditionHud(player, expeditionId, ticksRemaining, totalTicks)
```

---

## Persistent Data

```
hytale-server/mods/com.EreliaStudio_OneBlock/
├── oneblock-expedition.json
└── oneblock-dungeon.json
```

**`oneblock-expedition.json`**
```json
{
  "expeditionId": "Forest",
  "ticksRemaining": 12,
  "totalTicks": 25
}
```
- `expeditionId`: active expedition ID, or `null` if in default mode.
- `ticksRemaining`: breaks left before the expedition ends.
- `totalTicks`: the total ticks when the expedition was started (used for HUD fill calculation).

**`oneblock-dungeon.json`**
```json
{
  "dungeonId": "GoblinCave",
  "currentWaveIndex": 2
}
```
- `dungeonId`: active dungeon ID, or `null` if no dungeon is running.
- `currentWaveIndex`: which wave spawns on the next OneBlock break.

Both files are written immediately after every state change. Safe to hand-edit.

---

## Resources

All assets are in `mods/oneblock/src/main/resources/Server/Item/Items/`.

| Asset | Location | Notes |
|-------|----------|-------|
| Block variant JSONs | `OneBlock/` | One per expedition; must have `"Categories": ["Blocks.OneBlock"]` |
| Crystal item JSONs | `Crystal/Expedition/` | One per expedition; must use `oneblock_crystal_use` interaction |
| Crystal Enchanter | `OneBlockEnchanter/Bench_OneBlockEnchanter.json` | `OneBlock_Enchanter_Surface` category holds all expedition crystals |
| Dungeon Enchanter | `OneBlockDungeonEnchanter/Bench_OneBlockDungeonEnchanter.json` | No categories yet |
| Custom items | `CustomItems/` | `Locket_GobelinDungeon`, `ExpeditionPoint` |
| World config template | `oneblock-world-config-template.json` | Template for void world creation |

---

## Build & Deploy Reference

All commands run from the **repository root** (`OneBlockHytaleMod/`). Java 25 must be on your PATH. Gradle is included via the wrapper.

### Build and deploy (standard workflow)

```bash
./gradlew buildAndDeployAll
```

Compiles the module, produces a shaded JAR, copies it to `hytale-server/mods/`. Use after any code or JSON change.

### Other commands

```bash
./gradlew buildAll              # compile only, no deploy
./gradlew deployAll             # deploy only (assumes already built)
./gradlew :oneblock:build       # compile the oneblock module only
./gradlew :oneblock:buildAndDeploy  # build + deploy the module
./gradlew clean                 # delete all build/ folders
```

### Run the local development server

```bash
java -jar hytale-server/HytaleServer.jar --workdir hytale-server
```

Logs go to `hytale-server/logs/`. Stop with `Ctrl+C`.

### Typical development cycle

1. Edit Java or JSON files in `mods/oneblock/`.
2. `./gradlew buildAndDeployAll`.
3. Start the server.
4. Connect and test.
5. Repeat.

---

## Notes on the Build System

- **Gradle wrapper** (`gradlew` / `gradlew.bat`) is committed. No global Gradle install needed.
- The module produces a **shaded (fat) JAR** — GSON and Guava are bundled and relocated under `com.EreliaStudio.OneBlock.libs.gson` to avoid conflicts.
- The plain JAR is disabled; only the shaded JAR is produced.
- `HytaleServer.jar` in `libs/` is compile-only — never bundled.
- Resource expansion injects `${version}` and `${name}` into `manifest.json` at build time.
