# OneBlock Hytale Mod — Proof of Concept

This is a proof of concept for a **OneBlock-style survival mode in Hytale**.

The player starts in a void world with a single magical block at the center. Breaking that block gives a random reward, then the block instantly regenerates so all progression flows from the same source.

---

## Core Features

* Void world setup with the OneBlock placed at `(0, 100, 0)` and a custom sky tint
* Player spawn directly above the block at `(0.5, 102, 0.5)`
* Fall protection that teleports players back if they drop below the void threshold
* Weighted random drop system, where every item in every pool has a configurable weight
* **111 custom OneBlock block variants**, each with unique textures and icons — one per expedition and dungeon
* State persistence: active expedition and dungeon state is saved to JSON and restored on server restart
* Real-time HUD integration showing:
  * Active expedition or dungeon name
  * Ticks remaining or wave progress
  * Completion notifications
  * Automatic restore on player reconnect
* Admin/debug commands for:
  * Checking status
  * Starting expeditions
  * Stopping expeditions
  * Listing expeditions and dungeons

---

## Expedition System

Expeditions transform the OneBlock into a themed drop pool for a set number of breaks.

Most expeditions last **25 breaks**, while the default **Meadow** expedition lasts **100 breaks**. Once complete, the block automatically reverts to the default Meadow expedition.

There are **87 expeditions** across five difficulty tiers:

| Tier      | Count | Examples                                                                            |
| --------- | ----- | ----------------------------------------------------------------------------------- |
| Easy      | 21    | Default / Meadow, Cave Entry, Forest Edge, Plain, River, Pond, Quarry, Cow Hallow   |
| Advanced  | 26    | Iron Cave, Desert, Sea, Enchanted Forest, Frozen Forest, Dry Jungle Pass, Fire Land |
| Difficult | 24    | Adamantite Cave, Burned Forest, Cursed Forest, Icy Necropolis, Jurassic Cave        |
| Hard      | 24    | Mithril Cave, Onyxium Cave, Infernal Gate, Void Portal, Burnt Battlefield           |
| Expert    | 16    | Prisma Cave, Yeti Cavern, Infernal Plain, Spirit Threshold, Elemental Confluence    |

Expeditions drop biome-appropriate resources such as:

* Stone
* Ores, from copper through adamantite and beyond
* Wood types
* Plants, seeds, and food
* Essences
* Special progression materials

Higher-tier expeditions can also spawn enemies directly from the OneBlock.

---

## Dungeon System

Dungeons offer a distinct, combat-focused challenge separate from expeditions.

There are **24 dungeons**, each built as a multi-wave encounter. Enemies spawn directly around the OneBlock in waves, and the player must survive all waves to earn the completion rewards. Wave counts range from **3 to 8 waves** per dungeon.

### Dungeon Types

#### Classic Encounters

* Rat Cave *(3 waves)*
* Goblin Gank *(3 waves)*
* Goblin Invasion *(4 waves)*
* Undead Temple *(4 waves)*
* Desert Temple *(4 waves)*

#### Maritime & Void

* Pirate Shipwreck *(4 waves)*
* Sea Monster *(4 waves)*
* Void Temple *(4 waves)*

#### Elemental Threats

* Volcano *(4 waves)*
* Ice Temple *(4 waves)*
* Frost Bone Crypt *(5 waves)*
* Burnt Skeleton Citadel *(5 waves)*

#### Faction Raids

* Outlander Gank *(4 waves)*
* Outlander City *(5 waves)*
* Trork Warband *(4 waves)*
* Trork Chieftain Camp *(5 waves)*

#### Prehistoric Threats

* Dino Crisis *(6 waves)*, featuring:
  * Raptors
  * Rex
  * Archaeopteryx
  * Crocodiles

#### Insect Hordes

* Insect Invasion *(3 waves)*
* Insect Nest *(4 waves)*
* Insect Core *(4 waves)*, featuring Scarak variants

#### Endgame Dungeons

* Jungle Crypt *(4 waves)*
* Ancient Undead Sanctum *(5 waves)*
* Shadow Knight Citadel *(5 waves)*
* Spirit Realm Trial *(8 waves — most complex dungeon, featuring elemental spirits and golems)*

The HUD tracks wave progression live, for example:

```
Wave 2/5
```

Rewards are distributed only after full dungeon completion.

---

## Crafting & Progression Economy

The progression system is built around crystals, unlocks, and Expedition Points.

### Crystal Benches

#### Crystal Enchanter

The **Crystal Enchanter** is crafted via Fieldcraft and is used to craft expedition crystals across all five difficulty tiers.

#### Dungeon Enchanter

The **Dungeon Enchanter** is crafted via Fieldcraft and is used to craft all 24 dungeon crystals.

### Expedition Points

**Expedition Points** are a custom currency earned by completing expeditions.

They are spent to craft crystals, usually costing **1–2 Expedition Points per crystal**.

### Recipe Unlock System

New crystal recipes are learned by receiving special unlock drops during expeditions.

Unlocked recipes:

* Persist across sessions (saved and restored on restart)
* Notify the player when learned
* Follow unlock chains — for example, completing a Cave expedition unlocks the Rat Cave dungeon crystal
* Gradually expand the available expedition and dungeon progression paths

---

## Core Loop

```
Break the OneBlock
→ Get a random resource
→ Earn Expedition Points and unlock crystal recipes from drops
→ Craft expedition or dungeon crystals at the appropriate bench
→ Use a crystal to start a new expedition or dungeon
→ Farm the themed pool, collect ores, enemies, and materials
→ Return to default Meadow mode when the expedition ends
→ Repeat with progressively harder tiers
```

In practice, the loop works like this:

1. Break the OneBlock to receive random rewards.
2. Earn Expedition Points and unlock new crystal recipes.
3. Craft expedition or dungeon crystals at the correct bench.
4. Use a crystal to start:
   * A new expedition lasting around 25 breaks
   * A dungeon with multi-wave combat
5. Farm themed resources, enemies, ores, and special materials.
6. Complete the expedition or dungeon.
7. Return to default Meadow mode.
8. Repeat with harder tiers and deeper progression paths.

---

## Entity Spawning

Enemies spawn directly from the OneBlock using a proximity-based spawn system.

The mod searches for valid spawn positions within a **5-block radius** around the OneBlock. A valid spawn space requires:

* A solid floor
* Two blocks of vertical clearance
* Enough room for the entity to spawn safely

This system is used for:

* Dungeon waves
* Higher-tier expedition enemy drops
* Future mid-expedition ambushes

---

## Localization

The mod currently ships with full support for:

* **English**
* **Spanish**

---

## What's Next

Planned improvements include:

* Expanded entity spawning for mid-expedition ambushes
* Custom craftable expedition items to add more tactile progression steps
* More lore-flavored progression unlocks

For example, an **Emerald Cavern Map** could be crafted and used to unlock or craft the **Emerald Cavern Crystal**.

---

## Feedback

Feedback is welcome.

Join me on Discord under the same username.

---

## Current Highlighted Additions

The main additions in this version are:

* Expanded expedition roster to **87 expeditions** across five tiers, with all tier counts updated
* Dungeon system with **24 multi-wave dungeons**, including new Maritime, Void, and Jungle encounters
* Spirit Realm Trial as the deepest dungeon at **8 waves**
* Dual-bench crafting system:
  * Crystal Enchanter
  * Dungeon Enchanter
* Expedition Points economy
* Persistent state system — expedition and dungeon progress survives server restarts
* Persistent recipe unlock system with unlock chains
* HUD support for expeditions, dungeon waves, completion notifications, and reconnect restore
* Spanish localization
