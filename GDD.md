# OneBlock — Game Design Document

## Concept Overview

OneBlock is a survival game mode for Hytale. The player spawns in a completely void world — no terrain, no resources, nothing — with a single magical block floating at their feet. That block is the heart of everything: break it, and it drops a random resource or spawns a creature. It then regenerates, and the cycle repeats.

The entire progression is built around that one block. By default it drops basic  resources — Fibre, Stone, Rubble. To access more dangerous and rewarding content, the player crafts **Expedition Crystals** and uses them on the block. The block then runs in that expedition's drop pool for a limited number of breaks (or a fixed real-time duration), then reverts to default. Completing an expedition unlocks the matching expedition bench, which the player can craft, place, and upgrade to access more powerful recipes.

---

## Core Gameplay Loop

```
Break the OneBlock (default  pool)
        ↓
Collect Fibre, Stone, Rubble
        ↓
Put Rubble in the Salvager → receive Blue / Red / Yellow Crystals
        ↓
Craft a Crystal Enchanter (at Fieldcraft bench)
        ↓
Craft crystals into an Expedition Crystal (Small = 100 ticks, Large = 300 ticks)
        ↓
Use the Expedition Crystal
        ↓
The OneBlock switches to that expedition's drop pool
        ↓
Break the OneBlock — each break consumes one tick
        ↓
Ticks reach zero → expedition complete, block reverts to default
        ↓
Expedition completion unlocks the expedition's bench recipe in the OneBlock Workbench
        ↓
Craft and place the expedition bench → upgrade it with crystals for more powerful recipes
        ↓
Repeat with higher-tier expedition crystals
```

---

## The OneBlock

- Located at world coordinates **(0, 100, 0)**.
- Breaking it triggers the drop system and **immediately regenerates**.
- It never disappears — the player always has something to break.
- Its **visual appearance changes** when an expedition is active (e.g., stone texture for Cave, basalt for Deep Cave).
- When no expedition is active, the block sits in **default mode** and drops basic resources.

---

## Expeditions

Expeditions are the six themed worlds the player can temporarily activate. Each expedition has a unique drop pool, a visual block variant, and a matching expedition bench that is unlocked upon completion.

| # | Expedition | Theme | Key resources |
|---|-----------|-------|---------------|
| 1 | **Default** | Grassy surface (default) | Fibre, Stone, Rubble |
| 2 | **FarmLand** | Agriculture, livestock | Life Essence, Pigs, Cows |
| 3 | **Forest** | Dense woods | Ash Wood, Leather, Apples, Boars |
| 4 | **Cave** | Underground tunnels | Stone, Rubble, Charcoal, Copper, Iron, Goblins |
| 5 | **Deep Cave** | Deep darkness | Basalt, Iron, Thorium, Cobalt, Spiders, Void Crawlers |
| 6 | **The Abyss** | Endgame void depths | Slate, Cobalt, Adamantite, Mithril, Zombies, Earth Elementals |

Default is the **permanent default** — the player farms it between expeditions to gather basic materials and crystals. The other expeditions require crafting their matching crystals at the Crystal Enchanter.

---

## Crystal System

### Base Crystals (from Salvager)

The Salvager Bench processes Rubble and outputs tier-dependent base crystals:

| Crystal | Tier availability | Use |
|---------|-------------------|-----|
| **Blue Crystal** | Tier 1+ | Most abundant, used in early expedition crystals |
| **Red Crystal** | Tier 2+ | Mid-grade, required for stronger crystals |
| **Yellow Crystal** | Tier 3+ | High-grade, required for deep-tier crystals |

### Expedition Crystals (from Crystal Enchanter)

Expedition Crystals are crafted by fusing base crystals at the **Crystal Enchanter** bench. Each expedition has two sizes:

| Size | Tick count | Cost |
|------|-----------|------|
| **Small** | 100 ticks (OneBlock breaks) | Fewer Blue + Red crystals |
| **Large** | 300 ticks (OneBlock breaks) | More Blue + Red + Yellow crystals |

### Using a Crystal

The player right-clicks while holding an Expedition Crystal. The block:
1. Switches to the expedition's visual variant.
2. Starts counting down ticks on each break.
3. Uses that expedition's drop pool until ticks reach zero.
4. Reverts to default  mode automatically.

A new crystal can be used while an expedition is already in progress, resetting the tick count.

---

## Drop System

### Item Drops
Common drops are items that appear on the ground near the OneBlock. The player walks over them to collect.

### Entity Spawns
Some drops spawn a creature instead of an item. The creature appears next to the block — it might be passive (Cow, Boar Piglet) or hostile (Goblin, Spider, Zombie). The player must deal with it.

### Weighted Randomness
Every entry in a drop pool has a **weight** value. Higher weight = more frequent. Common bulk materials have high weights (20–30), rare drops have low weights (1–2).

### Recipe Drops
Some rare drops in the pool are **recipe unlock items**. Consuming one teaches the player a crafting recipe (e.g., Mossy Stone, Cracked Basalt).

---

## Expedition Completion

When the tick counter (or timer) reaches zero:

1. The OneBlock **automatically reverts** to default  mode.
2. The **expedition bench recipe** for that expedition is unlocked in the OneBlock Workbench (`KnowledgeRequired` recipe becomes available).
3. The player can now craft the expedition bench.

---

## Crystal Enchanter

The **Crystal Enchanter** is the primary crafting station for expedition progression.

- Crafted at the **Fieldcraft** bench (4× Fibre + 3× Rubble Stone — starter recipe).
- Has one crafting category per expedition, containing Small and Large crystals.
- Requires base crystals (Blue / Red / Yellow) as input.
- Crystal cost scales with expedition tier — early expeditions need few Blue crystals; endgame expeditions need many Yellow crystals.

---

## OneBlock Workbench

A craftable bench (8× Fibre + 6× Stone, at Fieldcraft) that holds all expedition bench recipes.

- Expedition bench recipes are **locked by default** (`KnowledgeRequired`).
- Each recipe unlocks automatically when the matching expedition is completed for the first time.
- Once unlocked, the player crafts the bench and places it in the world.

---

## Expedition Benches

Each of the non-default expeditions has a dedicated bench. These benches use Hytale's native **tier upgrade** system:

- **Tier 1** (default): Basic recipes for that expedition.
- **Tier 2**: Unlocked by consuming a set of crystals at the bench.
- **Tier 3**: Unlocked by consuming higher-tier crystals and ores.

Expedition bench recipes (what they produce) are defined per-bench and filled in as the game design evolves. The infrastructure (leveling, unlock gate) is in place.

---

## Salvager Bench

The Salvager converts **Rubble** into base crystals and ores. It is the primary crystal source.

| Tier | Primary outputs |
|------|----------------|
| 1 | Blue Crystal (100%) |
| 2 | Blue Crystal (65%), Red Crystal (25%), Copper (7%), Iron (3%) |
| 3 | Blue Crystal (45%), Red Crystal (30%), Yellow Crystal (15%), Iron (5%), Thorium (5%) |

The bench upgrades via Hytale's native tier system. Higher tiers produce more valuable outputs.

---

## World Setup

- The world is a **complete void** — no terrain, no natural generation.
- Players spawn at **(0.5, 102, 0.5)**, directly above the OneBlock.
- **Fall protection** is active: if a player falls below Y=85, they are teleported back to spawn. Players never die from falling into the void.
- The world has no PvP by default.
- Players build their base entirely from materials dropped by the OneBlock.

---

## Player Goals

There is no explicit win condition — OneBlock is an **open-ended progression game**. Suggested milestones:

1. Survive the early game with  drops alone.
2. Build a Salvager and Crystal Enchanter.
3. Complete all five non-default expeditions at least once.
4. Unlock and max-upgrade all expedition benches.
5. Use a Large Abyss Crystal and clear 300 breaks in the endgame pool.
6. Build an impressive base in the void using accumulated materials.

---

## Summary of Added Gameplay Elements

| Element | Description |
|---------|-------------|
| OneBlock | The central magical block, always regenerates after being broken |
| Default Pool |  drops (Fibre, Stone, Rubble) — always active between expeditions |
| Expeditions | 5 timed themed tiers (FarmLand → The Abyss), each with unique drops |
| Crystal System | Salvager → base crystals → Enchanter → expedition crystals → temporary expeditions |
| Crystal Enchanter | Bench for fusing base crystals into expedition crystals |
| OneBlock Workbench | Holds expedition bench recipes; gates them behind expedition completion |
| Expedition Benches | One per expedition, upgradable via Hytale's native tier system |
| Salvager Bench | Converts Rubble to crystals/ores; output improves with tier |
| Void World | Empty world — everything the player has comes from the OneBlock |
| Fall Protection | Players who fall into the void are teleported back to spawn |
