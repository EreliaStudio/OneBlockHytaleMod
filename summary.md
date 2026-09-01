# OneBlock Hytale Mod — Current Implementation Summary

OneBlock is a server-side Hytale mechanic with an embedded asset pack. The core tracks regenerating OneBlocks by world, position, and owner without controlling world generation. The companion islands module creates isolated void islands and maps owners and members to a shared center OneBlock.

## Current scope

| Area | Current implementation |
| --- | --- |
| Mod version | `1.0.5` |
| Supported server | Hytale Server `>=0.5.2` |
| Gradle modules | `:oneblock` core and `:oneblock-islands` orchestration |
| Content definitions | 112 total: 88 expeditions and 24 dungeons |
| Difficulty tiers | Easy, Advanced, Difficult, Hard, and Expert |
| Starter expedition | Meadow (stable internal ID: `Default`) |
| OneBlock scope | Registered world + block position + owner |
| Island OneBlock position | `(0, 100, 0)` |
| Island spawn | `(0.5, 102, 0.5)` |
| Languages | English, Spanish, French, and Slovak |
| Generated visual assets | 112 OneBlock variants and 112 matching crystals |

This is no longer only a proof of concept: the multiplayer world lifecycle, data-driven content pipeline, persistence, progression, crafting, localization, and custom HUD are implemented.

## Core game loop

1. The player breaks the OneBlock and receives a weighted item or entity reward.
2. The block is replaced synchronously at the same position, without allowing the native break to leave an air frame.
3. Regular expedition progress decreases once per completed block break.
4. Expedition Points and crystal unlocks are awarded through configured completion rewards.
5. Players craft expedition or dungeon crystals at their corresponding enchanter.
6. Using a crystal changes the accessible registered roots and starts their configured expedition or dungeon.
7. Completion returns those roots to the default OneBlock.

## Multiplayer and multi-world support

The core root registry permits multiple independent owners and positions in any existing world. Each owner's state is persisted per world. The islands module creates a dedicated world per island and resolves authorized members to the island owner's state, so the group shares progression and HUD updates without introducing island concepts into the core.

## Island world initialization and void safety

Only the islands module creates persistent void worlds with environment `Env_Default_Void`. It:

- Places `OneBlock_Block_Default` at the fixed OneBlock position.
- Sets the player spawn above the block.
- Applies the configured sky/environment tint.
- Registers the center block through the core OneBlock API.

Island fall protection returns players below `Y=1` to the island spawn and removes fallen non-player entities. The core OneBlock mod contains no world generator or fall system. The server's `default` world is untouched.

## Deterministic block durability and tools

Every OneBlock definition can declare a `Solidity` object:

```json
"Solidity": {
  "Ticks": 10,
  "Tool": "Pickaxe"
}
```

- `Ticks` is the exact number of accepted damage events needed to finish the block.
- `Tool` supports `Hand`, `Pickaxe`, or `Axe`.
- `Hand` allows an empty hand or any held item.
- `Pickaxe` requires an item whose player animation identifies it as a pickaxe.
- `Axe` accepts axe/hatchet player animations.
- A wrong tool cancels the damage and applies no progress.
- Creative-mode block handling bypasses the mod's durability and reward logic.

`Ticks` is the base break time for an empty hand or crude valid tool. Held tools
scale each accepted hit using Hytale's material-specific native power and item
level, whichever gives the larger multiplier. Ten item levels equal 1x damage,
with a minimum of 1x, so a crude pickaxe remains at 1x, an Adamantite pickaxe is
4x, and a Mithril pickaxe is 5x. The same calculation applies to axes and to
durable tools used on Hand blocks. Non-tool items remain at 1x.

The generated native `GatherType` keeps the expected Hytale hit feedback: `SoftBlocks` for Hand, `Rocks` for Pickaxe, and `Woods` for Axe. `GatherType` can also be used as an input alias in `expeditions.json`.

All 112 current expedition definitions explicitly declare Solidity. The baseline uses three hits at item level 1, four at level 2, five at level 3, six at level 4, and eight at level 5. Cave, stone, ruin, and temple themes generally require a Pickaxe; forest and wood themes generally require an Axe; open terrain, water, creature, and magical themes use Hand. These values remain individually editable in `expeditions.json`. For backward compatibility, a missing `Solidity` value still means one tick with Hand.

When the final accepted hit occurs, the native `BreakBlockEvent` is cancelled, block health is reset, the reward is processed exactly once, and the correct OneBlock variant is placed synchronously at the same coordinate. This prevents visible air gaps and overlapping replacement attempts.

A held tool loses durability once when a OneBlock break completes. Pickaxe and Axe blocks use Hytale's native material-aware durability handling. Hand blocks add one durability use for a held durable tool because Hytale's `SoftBlocks` gather type normally consumes none. Creative mode and empty-hand breaks do not consume durability.

## Weighted drop engine

Regular pools support weighted entries for:

- Hytale item IDs.
- Custom mod items.
- Configurable item quantities.
- Entity/NPC spawns through `entity:` definitions.

An entry's probability is relative to the sum of weights in its pool. The selected item is spawned as an item stack, while entity entries use the safe entity-spawn system.

Two utility drops currently verified in the expedition data are:

- `Lake`: a filled water bucket (`*Container_Bucket_State_Filled_Water`).
- `FireLand`: `Fluid_Lava`, the functional lava-source item available in the current Hytale assets. There is currently no equivalent filled lava-bucket state in those assets.

## Expeditions

There are **88 regular expeditions**. Their configured duration ranges from **15 to 50 completed OneBlock breaks**.

| Tier | Expeditions |
| --- | ---: |
| Easy | 19 |
| Advanced | 24 |
| Difficult | 19 |
| Hard | 17 |
| Expert | 9 |
| **Total** | **88** |

Each expedition can define:

- A themed weighted drop pool.
- A dedicated OneBlock visual and crystal.
- Duration in completed block breaks.
- Solidity and required tool.
- Crystal crafting inputs.
- Mandatory completion rewards.
- Weighted random completion-reward bundles.
- Items, Expedition Points, generated custom IDs, and crystal-recipe unlocks.

Only one regular expedition or dungeon can be active in a given world. Starting one ends the other. A regular expedition completes when its remaining-break count reaches zero, awards its completion rewards, clears the HUD, and restores the default block.

## Dungeons

There are **24 dungeons**, ranging from **3 to 8 configured waves**:

| Dungeon | Waves | Dungeon | Waves |
| --- | ---: | --- | ---: |
| Rat Cave | 3 | Goblin Gank | 3 |
| Goblin Invasion | 4 | Pirate Shipwreck | 4 |
| Sea Monster | 4 | Undead Temple | 4 |
| Void Temple | 4 | Outlander Gank | 4 |
| Outlander City | 5 | Ice Temple | 4 |
| Volcano | 4 | Desert Temple | 4 |
| Insect Invasion | 3 | Insect Nest | 4 |
| Insect Core | 4 | Dino Crisis | 6 |
| Trork Warband | 4 | Trork Chieftain Camp | 5 |
| Frostbone Crypt | 5 | Burnt Skeleton Citadel | 5 |
| Jungle Crypt | 4 | Ancient Undead Sanctum | 5 |
| Shadow Knight Citadel | 5 | Spirit Realm Trial | 8 |

Breaking the dungeon OneBlock spawns the entities configured for the current wave and advances the saved wave index. After the final wave is triggered, the dungeon completes, distributes its configured rewards, restores the default OneBlock, and clears the world HUD.

Entity placement searches shuffled positions within a five-block radius. It prefers a solid floor with two clear blocks above it so groups do not all appear on the OneBlock itself.

Important current behavior: dungeon progression is **break-driven**. The mod does not yet wait for every spawned enemy to die before allowing the next block break to trigger another wave.

## Crystals, crafting, and progression

Every expedition and dungeon has a corresponding consumable crystal. A crystal can be used when the player has an accessible registered OneBlock context. Using it:

- Replaces the current OneBlock with the corresponding visual variant.
- Ends the other active mode for that root owner.
- Starts the selected expedition or dungeon state.
- Consumes the held crystal.
- Updates the HUD for players mapped to that same root owner.

The **Crystal Enchanter** contains regular expedition crystal recipes organized by the five difficulty tiers. The **Dungeon Enchanter** contains the dungeon crystal recipes. Both benches are registered as craftable content.

`ExpeditionPoint` is the custom progression currency. Crystal inputs and costs are configured per definition in `expeditions.json`; they are not hard-coded to a single price.

Crystal rewards can unlock gated recipes. When an unlock is awarded, the mod teaches the recipe through Hytale's crafting knowledge system and notifies the player. The generator adds `KnowledgeRequired` to crystals that are gated by an unlock reward.

## Custom HUD

The mod includes a custom top-centered expedition panel built from:

- `OneBlockHud.ui`
- `OneBlockHudFrame.png`
- `OneBlockHudProgressFill.png`

The HUD displays an uppercase expedition or dungeon title and a graphical progress bar. Regular expeditions use remaining versus total breaks; dungeons use completed versus total waves.

HUD state is restored only when the current world contains an accessible registered OneBlock. Entering spawn or any world without one clears the HUD. Island members resolve to their island owner's shared state.

Because the UI resources are embedded in the plugin JAR and `IncludesAssetPack` is enabled, no separate standalone asset-pack folder is required after deployment.

## Persistence

The mod writes state after progression changes so active sessions can survive a server restart:

- Expedition: active ID, remaining breaks, and total breaks.
- Dungeon: active ID and current wave index.
- Root registry: world, position, owner UUID, and owner name.

Each owner uses independent expedition and dungeon files under the encoded `root-worlds/<world>/owners/<uuid>/` data directory.

## Server commands

The root command is `/oneblock`. It is implemented as an administrative target-player command.

| Command | Result |
| --- | --- |
| `/oneblock status` | Shows the accessible OneBlock state for the target player in the current world. |
| `/oneblock start <expeditionId>` | Starts the requested expedition for that OneBlock owner. |
| `/oneblock stop` | Ends the current expedition/dungeon, restores the default block, and clears the HUD. |

Island creation, joining, membership, and travel are handled by `/island` commands.

## Localization

Complete server language catalogs currently ship for:

- English: `en-US`
- Spanish: `es-ES`
- French: `fr-FR`
- Slovak: `sk-SK`

The content generator updates generated English expedition and crystal entries. After adding or renaming content, the translated catalogs should be synchronized manually.

## Data-driven content generation

`expeditions.json` is the source of truth for regular expeditions, dungeons, drops, waves, recipes, completion rewards, and Solidity values. Run:

```powershell
python tools/generate_expeditions.py expeditions.json --repo-root .
```

Useful options include:

- `--dry-run` to validate and preview changes without writing.
- `--clean` to remove obsolete generated content.
- `--render-names` to provide the display-name mapping used for rendered item art.

The generator validates Solidity values before writing and produces:

- All OneBlock item/block JSON definitions.
- All crystal item definitions.
- Crystal and dungeon-enchanter recipe registrations.
- English language entries.
- Java defaults for expedition pools, dungeon waves, and Solidity.

It does not overwrite existing PNG artwork. The full schema and examples are documented in `tools/EXPEDITIONS_JSON_GUIDE.md`, while item display-name overrides live in `item_render_names.json`.

## Project layout

| Path | Purpose |
| --- | --- |
| `expeditions.json` | Main content definition file |
| `PLAYER_GUIDE.md` | Player-facing introduction, progression guide, and gameplay reference |
| `mods/oneblock/src/main/java/com/EreliaStudio/OneBlock/` | Server-side runtime implementation |
| `mods/oneblock/src/main/resources/` | Manifest, item/block assets, UI, recipes, textures, and languages |
| `tools/generate_expeditions.py` | Content generator and validation pipeline |
| `tools/EXPEDITIONS_JSON_GUIDE.md` | Generator schema and authoring guide |
| `hytale-server/` | Local server working directory, deployed mods, assets, and backups |

## Build, deploy, and local test

The project requires Java 25 for compilation and `libs/HytaleServer.jar` as its compile-only server API.

Build the shaded plugin JAR:

```powershell
.\gradlew.bat buildAll
```

Build and copy it into `hytale-server/mods`:

```powershell
.\gradlew.bat buildAndDeployAll
```

Start or restart the local server with the installed Hytale runtime and `hytale-server/Assets.zip`:

```powershell
.\reload-server.ps1
```

Once the server is ready, connect from Hytale to `localhost`. The produced plugin is `OneBlock-1.0.5.jar`; its server resources and CustomUI documents are embedded in that JAR.

## Known implementation limits

- Dungeon waves advance when the OneBlock is broken, not when the preceding wave has been defeated.
- The current Hytale asset set exposes `Fluid_Lava` rather than a filled lava-bucket state.
- Generated localization targets English; Spanish, French, and Slovak need manual synchronization after content changes.
- Automated coverage includes the deterministic OneBlock damage calculation and display-name behavior; native in-game tool durability and interaction feedback still require local server testing.
