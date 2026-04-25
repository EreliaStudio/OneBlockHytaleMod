# OneBlock — Plugin Architecture & Composition

## Project Overview

The OneBlock mod is split into **4 independent Gradle sub-projects** (modules), each compiled into its own shaded JAR and deployed as a separate Hytale plugin. They are coordinated by a single root Gradle build.

The split follows one principle: **engine vs. content vs. independent systems**.

- `oneblock-core` is the pure drop engine — no game content, no expedition names, no item IDs hardcoded beyond the default fallback.
- `oneblock-progression` is all player-facing game content — expeditions, crystals, benches.
- `oneblock-salvager` and `oneblock-world` are completely standalone systems with no cross-dependencies.

---

## Repository Layout

```
OneBlockHytaleMod/
│
├── build.gradle.kts              # Root Gradle config (shared rules for all sub-projects)
├── settings.gradle.kts           # Lists the 4 sub-projects and their paths
│
├── libs/
│   └── HytaleServer.jar          # Hytale server API (compile-only, not shipped)
│
├── mods/                         # One folder per plugin module
│   ├── oneblock-core/
│   ├── oneblock-progression/
│   ├── oneblock-salvager/
│   └── oneblock-world/
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

### Module internal layout (same for all modules)

```
mods/<module-name>/
├── build.gradle.kts              # Module-specific overrides (archive name, optional dependencies)
└── src/
    └── main/
        ├── java/
        │   └── com/EreliaStudio/OneBlock/
        │       └── *.java
        └── resources/
            ├── manifest.json     # Plugin metadata (name, version, main class)
            ├── Server/           # Asset pack files (items, blocks, textures, lang)
            └── *.json            # Runtime config files loaded by the plugin
```

---

## The 4 Modules

### 1. `oneblock-core` — Drop Engine

**JAR name:** `OneBlock-Core.jar`
**Main class:** `OneBlockPlugin`
**Depends on:** nothing (only HytaleServer.jar)

The foundation. It owns everything needed to pick and execute a drop when the OneBlock is broken, manage the active expedition tick state, and nothing else. No game content is hardcoded here.

**Java classes:**

| Class | Role |
|-------|------|
| `Dropable` | Interface: `getId()` + `execute(DropableContext)` |
| `DropableContext` | Data passed to a dropable on execution (world, position, player) |
| `ItemDropable` | Implements `Dropable` — spawns an item entity on the ground |
| `EntitySpawnDropable` | Implements `Dropable` — spawns an NPC via reflection |
| `OneBlockEntitySpawner` | Reflection-based wrapper around `SpawnNPCInteraction` |
| `OneBlockDropId` | Parses drop ID prefixes (`entity:`, `npc:`, `item:`, bare) |
| `OneBlockDropRegistry` | Central registry: weighted selection + dropable handler map |
| `OneBlockExpeditionStateProvider` | Global expedition state: active expedition ID + ticks remaining (or end time). Persisted to `oneblock-expedition.json`. Returns completed expedition ID from `onBreak()`. |
| `OneBlockBreakSystem` | ECS event handler: on block break → pick reward → execute dropable → consume tick → on completion, reset block and call `OneBlockPlugin.fireExpeditionComplete` |
| `OneBlockPools` | Holds the active `OneBlockPoolResolver`; defaults to always returning "Meadow" |
| `OneBlockPoolResolver` | Interface for custom pool resolution logic |
| `OneBlockBlockUtil` | Detects whether a broken block is a OneBlock (checks `Blocks.OneBlock` category) |
| `OneBlockBlockIds` | Constant: `DEFAULT_BLOCK_ID = "OneBlock_Block_Meadow"` |
| `OneBlockCommand` | `/oneblock status|start|stop|list` admin command |
| `OneBlockPlugin` | Plugin entry point — wires everything together; exposes `setExpeditionCompleteCallback(BiConsumer<String, DropableContext>)` and `fireExpeditionComplete` |

**Resources:** 6 OneBlock variant block JSONs (one per expedition), language file.

---

### 2. `oneblock-progression` — Expedition Content & Crystal System

**JAR name:** `OneBlock-Progression.jar`
**Main class:** `OneBlockProgressionPlugin`
**Depends on:** `oneblock-core` (compile-only)

All player-facing game content lives here. This module tells core *what* drops exist and *what* their weights are, registers the expedition pool resolver, and wires the crystal use interaction that starts expeditions.

**Java classes:**

| Class | Role |
|-------|------|
| `OneBlockExpeditionResolver` | Parses expedition name from block IDs and crystal item IDs; returns tick counts for Small/Large crystals |
| `OneBlockExpeditionPoolResolver` | Implements `OneBlockPoolResolver` — reads expedition from block type via `OneBlockExpeditionResolver` |
| `OneBlockExpeditionDefaults` | Hardcoded base drop pools and weights for all 6 expeditions |
| `OneBlockCrystalInteraction` | SimpleInstantInteraction — right-clicking the OneBlock with an expedition crystal starts (or resets) the expedition |
| `OneBlockInteractionUtil` | Shared helpers (consumeHeldItem, notifyPlayer, finish/skip/fail) |
| `OneBlockProgressionPlugin` | Plugin entry point — installs resolver, loads defaults, registers crystal interaction, wires expedition-complete callback |

**Resources:**
- Base crystal items (`OneBlock_Crystal_Blue/Red/Yellow`)
- 10 expedition crystal items (Small + Large for FarmLand, Forest, Cave, Deep Cave, The Abyss)
- 5 bench recipe items (`OneBlock_Bench_Recipe_<ExpeditionId>`) — dropped at the OneBlock on expedition completion; consuming one teaches the matching bench recipe
- `Bench_OneBlockEnchanter.json` — Crystal Enchanter bench (crafts expedition crystals)
- `Bench_OneBlockWorkbench.json` — OneBlock Workbench (holds `KnowledgeRequired` expedition bench recipes)
- 5 expedition bench JSONs (`Bench_OneBlock_FarmLand/Forest/Cave/Deep Cave/The Abyss`)
- Language file

---

### 3. `oneblock-salvager` — Salvager Bench

**JAR name:** `OneBlock-Salvager.jar`
**Main class:** `OneBlockSalvagerPlugin`
**Depends on:** nothing

Completely standalone. Manages the Salvager Bench which converts Rubble into tier-appropriate base crystals (Blue/Red/Yellow) or ores.

**Java classes:**

| Class | Role |
|-------|------|
| `OneBlockSalvageChanceSystem` | ArchetypeTickingSystem — scans all benches, patches the crystal output slot with a tier-based random item from `oneblock-salvager-drops.json` |
| `OneBlockSalvagerPlugin` | Plugin entry point |

**Resources:** Salvager bench JSON, `oneblock-salvager-drops.json`, language file.

---

### 4. `oneblock-world` — Void World Setup

**JAR name:** `OneBlock-World.jar`
**Main class:** `OneBlockWorldPlugin`
**Depends on:** nothing

Completely standalone. Handles everything about the world environment.

**Java classes:**

| Class | Role |
|-------|------|
| `OneBlockWorldBootstrap` | On startup, creates/updates the void world config if missing or wrong |
| `OneBlockWorldInitializer` | On world load, places the starting OneBlock and sets the spawn point |
| `OneBlockFallBackSystem` | ArchetypeTickingSystem — teleports players below Y=85 back to spawn |
| `OneBlockWorldPlugin` | Plugin entry point — calls bootstrap, registers fall-back system, registers AddWorldEvent listener |

**Resources:** `oneblock-world-config-template.json`.

---

## Dependency Graph

```
                HytaleServer.jar  (compile-only, not shipped)
                       │
          ┌────────────┴────────────────────┐
          │                                 │
   oneblock-core                  (standalone modules)
   [Drop engine]                  oneblock-salvager
          │                       oneblock-world
          │ compileOnly
          │
   oneblock-progression
   [All game content]
```

Arrow direction = "depends on". `oneblock-salvager` and `oneblock-world` have zero dependencies on the other OneBlock modules.

---

## Key Runtime Data Flow

### Block Break → Drop → Tick

```
Player breaks block
  → BreakBlockEvent  (oneblock-core: OneBlockBreakSystem)
  → Verify block category == "Blocks.OneBlock"
  → OneBlockPools.resolvePoolId(blockType)         ← uses OneBlockExpeditionPoolResolver (set by progression)
  → OneBlockDropRegistry.getKnownDrops(poolId)
  → OneBlockDropRegistry.pickReward(poolId, drops)
  → Dropable.execute(context)                      ← ItemDropable or EntitySpawnDropable
  → OneBlockExpeditionStateProvider.onBreak()
      ├─ Ticks remaining > 0 → replaceBlock with same block type
      └─ Ticks reached zero  → replaceBlock with DEFAULT_BLOCK_ID
                             → fire completeCallback(expeditionId)  ← wired by progression
```

### Crystal Use → Expedition Start (or Reset)

```
Player right-clicks OneBlock with expedition crystal
  → OneBlockCrystalInteraction.firstRun()  (oneblock-progression)
  → Verify target block is a OneBlock
  → OneBlockExpeditionResolver.expeditionFromCrystalItemId(itemId)  →  expeditionId
  → OneBlockExpeditionResolver.ticksFromCrystalItemId(itemId)       →  ticks (100 or 300)
  → OneBlockExpeditionResolver.blockIdForExpedition(expeditionId)   →  new block type ID
  → world.setBlock(...)                             ← block switches visually (resets if already active)
  → OneBlockExpeditionStateProvider.startExpedition(expeditionId, ticks)
  → consumeHeldItem + notify player
```

### Expedition Complete → Bench Recipe Drop

```
OneBlockExpeditionStateProvider.onBreak() → ticks = 0 → returns completedExpeditionId
  → OneBlockBreakSystem calls OneBlockPlugin.fireExpeditionComplete(expeditionId, context)
  → OneBlockProgressionPlugin.onExpeditionComplete(expeditionId, ctx)
  → dropRegistry.executeDropable("OneBlock_Bench_Recipe_<expeditionId>", ctx)
        ← spawns recipe item at the OneBlock position
  → Player picks up item → consumes it → game engine unlocks KnowledgeRequired recipe
        for Bench_OneBlock_<expeditionId> in the OneBlock Workbench
```

---

## Persistent Data

```
hytale-server/mods/com.EreliaStudio_OneBlock-Core/
└── oneblock-expedition.json
```

Format:
```json
{
  "expeditionId": "Forest",
  "ticksRemaining": 47,
  "timeBased": false,
  "endTimeMs": 0
}
```

- `expeditionId`: the currently active expedition, or `null` if in default Meadow mode.
- `ticksRemaining`: breaks left before the expedition ends (tick-based mode).
- `timeBased`: if `true`, `endTimeMs` is used instead of `ticksRemaining`.
- `endTimeMs`: Unix timestamp (ms) at which the expedition ends (time-based mode only).

Written immediately after every OneBlock break. Safe to hand-edit.

---

## Configuration Files

| File | Module | Purpose |
|------|--------|---------|
| `oneblock-salvager-drops.json` | oneblock-salvager | Salvager bench outputs per tier level (now outputs Blue/Red/Yellow crystals) |
| `oneblock-world-config-template.json` | oneblock-world | Template for void world creation |
| `manifest.json` (per module) | all | Plugin metadata, main class, version |
| `hytale-server/config.json` | server | Server name, player limit, default world |

---

## Build & Deploy Reference

All commands run from the **repository root** (`OneBlockHytaleMod/`). Java 25 must be on your PATH. Gradle is included via the wrapper — no separate Gradle install needed.

### Build and deploy everything (standard workflow)

```bash
./gradlew buildAndDeployAll
```

Compiles all 4 modules, produces shaded JARs, and copies them to `hytale-server/mods/`. **Use this after any code or JSON change.**

### Build only (no deploy)

```bash
./gradlew buildAll
```

Produces JARs in each module's `build/libs/` folder. Does not copy them to the server.

### Deploy only (assumes already built)

```bash
./gradlew deployAll
```

### Single-module commands

Replace `<moduleName>` with `oneblock-core`, `oneblock-progression`, `oneblock-salvager`, or `oneblock-world`.

```bash
./gradlew :<moduleName>:build             # compile only
./gradlew :<moduleName>:deploy            # deploy (triggers build if stale)
./gradlew :<moduleName>:buildAndDeploy    # build + deploy
```

Example — rebuild only progression after editing `OneBlockExpeditionDefaults.java`:

```bash
./gradlew :oneblock-progression:buildAndDeploy
```

### Clean all build artifacts

```bash
./gradlew clean
```

Deletes all `build/` folders. Use this if you see stale class files or unexplained compilation errors.

### Run the local development server

```bash
java -jar hytale-server/HytaleServer.jar --workdir hytale-server
```

The server reads `hytale-server/config.json`, loads all JARs from `hytale-server/mods/`, and starts. Logs go to `hytale-server/logs/`. Stop with `Ctrl+C`.

### Typical development cycle

1. Edit Java or JSON files in the relevant `mods/<module>/` folder.
2. `./gradlew buildAndDeployAll` (or single-module variant for faster iteration).
3. Start the server: `java -jar hytale-server/HytaleServer.jar --workdir hytale-server`.
4. Connect and test.
5. Repeat.

### Adding a new module

1. Create `mods/my-new-mod/` with `build.gradle.kts` and source under `src/main/java/` + `manifest.json` under `src/main/resources/`.
2. Register in `settings.gradle.kts`:
   ```kotlin
   include(":my-new-mod")
   project(":my-new-mod").projectDir = file("mods/my-new-mod")
   ```
3. If it depends on core, add to `build.gradle.kts`:
   ```kotlin
   dependencies {
       compileOnly(project(":oneblock-core"))
   }
   ```
4. `./gradlew buildAndDeployAll`.

---

## Notes on the Build System

- **Gradle wrapper** (`gradlew` / `gradlew.bat`) is committed to the repo. No global Gradle installation needed.
- Every module produces a **shaded (fat) JAR** — GSON and Guava are bundled and relocated under `com.EreliaStudio.OneBlock.libs.gson` to avoid conflicts with server dependencies.
- The plain JAR is disabled; only the shaded JAR is produced.
- `HytaleServer.jar` in `libs/` is a **compile-only** dependency — it is never bundled into output JARs.
- Resource expansion injects `${version}` and `${name}` into `manifest.json` at build time.
