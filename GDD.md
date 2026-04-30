# OneBlock — Game Design Document

## Concept Overview

OneBlock is a survival game mode for Hytale. The player spawns in a completely void world — no terrain, no resources, nothing — with a single magical block floating at their feet. That block is the heart of everything: break it, and it drops a random resource or spawns a creature. It then regenerates, and the cycle repeats.

The entire progression is built around that one block. By default it drops basic resources — Fibre, Rubble Stone, Dirt. To access more rewarding content, the player crafts **Expedition Crystals** at the **Crystal Enchanter** and uses them on the block. The block then runs in that expedition's drop pool for a fixed number of breaks (ticks), then reverts to default. Completing certain expeditions drops a crystal that unlocks the recipe for the next expedition tier.

---

## Core Gameplay Loop

```
Break the OneBlock (default pool)
        ↓
Collect Fibre, Rubble Stone, Dirt
        ↓
Craft a Crystal Enchanter (at Fieldcraft bench)
        ↓
Craft an Expedition Crystal (e.g., Cave Entry or Forest Edge)
        ↓
Use the Expedition Crystal on the OneBlock
        ↓
The OneBlock switches to that expedition's drop pool
        ↓
Break the OneBlock — each break consumes one tick
        ↓
Ticks reach zero → expedition complete, block reverts to default
        ↓
Completion may drop a crystal that unlocks the next expedition
        ↓
Repeat with higher-tier expedition crystals
```

---

## The OneBlock

- Located at world coordinates **(0, 100, 0)**.
- Breaking it triggers the drop system and **immediately regenerates**.
- It never disappears — the player always has something to break.
- Its **visual appearance changes** when an expedition is active (e.g., cave stone for Cave Entry, oak forest for Forest).
- When no expedition is active, the block sits in **default mode** and drops basic resources.
- Creative-mode players cannot trigger drops.

---

## Expeditions

Expeditions are the themed worlds the player can temporarily activate. Each expedition has a unique drop pool, a visual block variant, and a fixed tick count (how many breaks it lasts). Some expeditions drop a completion crystal that teaches the recipe for the next expedition.

| # | Expedition ID | Theme | Key resources | Ticks | Completion reward |
|---|--------------|-------|---------------|-------|------------------|
| 1 | **Default** | Grassy surface | Fibre, Rubble Stone, Dirt | — | — |
| 2 | **Cave_Entry** | Underground entrance | Rubble Stone, Rock Stone | 25 | Crystal: Cave (unlocks Cave recipe) |
| 3 | **Forest_Edge** | Forest border | Sticks, Oak Wood | 25 | Crystal: Forest (unlocks Forest recipe) |
| 4 | **Cave** | Underground tunnels | Rock Stone, Copper Ore | 25 | — |
| 5 | **Forest** | Dense woods | Oak Wood, Boars | 25 | — |

**Default** is always active between expeditions. **Cave_Entry** and **Forest_Edge** crystals are craftable at the Crystal Enchanter from the start. Cave and Forest crystals are earned by completing the respective entry expeditions.

---

## Crystal System

### Expedition Crystals (from Crystal Enchanter)

Each expedition has one crystal item. Crystals are crafted at the **Crystal Enchanter** bench using basic materials.

| Crystal | Expedition activated | Ticks | How to obtain |
|---------|---------------------|-------|---------------|
| `OneBlock_Crystal_Default` | Default | 25 | Crafted at Crystal Enchanter |
| `OneBlock_Crystal_Cave_Entry` | Cave_Entry | 25 | Crafted at Crystal Enchanter (4× Rubble Stone) |
| `OneBlock_Crystal_Forest_Edge` | Forest_Edge | 25 | Crafted at Crystal Enchanter |
| `OneBlock_Crystal_Cave` | Cave | 25 | Dropped on completing Cave_Entry |
| `OneBlock_Crystal_Forest` | Forest | 25 | Dropped on completing Forest_Edge |

### Using a Crystal

The player right-clicks while holding an Expedition Crystal on the OneBlock. The block:
1. Switches to the expedition's visual variant.
2. Starts counting down ticks on each break.
3. Uses that expedition's drop pool until ticks reach zero.
4. Reverts to default mode automatically.

A new crystal can be used while an expedition is already in progress, replacing the current expedition.

---

## Expedition Completion Rewards

When the tick counter reaches zero, the expedition completes and the block reverts to default. Some expeditions give mandatory completion rewards:

- **Cave_Entry**: Drops 1× `OneBlock_Crystal_Cave` and teaches the recipe for it.
- **Forest_Edge**: Drops 1× `OneBlock_Crystal_Forest` and teaches the recipe for it.

The crystal item lands at the OneBlock position. Picking it up places the recipe in the player's crafting knowledge, making that expedition available at the Crystal Enchanter.

---

## Drop System

### Item Drops
Common drops are items that appear on the ground near the OneBlock. The player walks over them to collect.

### Entity Spawns
Some drops spawn a creature instead of an item. The creature appears next to the block — it might be passive (e.g., Boar) or hostile. The player must deal with it. Entity drops use the `entity:<EntityId>` format in code.

### Weighted Randomness
Every entry in a drop pool has a **weight** value. Higher weight = more frequent. The system performs a single weighted draw per break.

---

## Dungeon System

Dungeons are a wave-based variant of expeditions. Breaking the OneBlock during a dungeon spawns a wave of enemies around the block (within a 5-block radius on solid ground), rather than dropping items. Each break advances the dungeon by one wave.

- Dungeons are activated by a **Dungeon Crystal** used at the **Dungeon Enchanter** bench.
- The dungeon tracks `currentWaveIndex` — which wave to spawn next.
- When all waves are done, the dungeon completes, completion rewards are given, and the block reverts to default.
- Dungeons and expeditions are mutually exclusive: if a dungeon is active, dungeon logic runs instead of expedition logic.

The dungeon infrastructure is in place. No dungeon content is defined yet.

---

## Crystal Enchanter

The **Crystal Enchanter** is the primary crafting station for expedition crystals.

- Crafted at the **Fieldcraft** bench (4× Fibre + 3× Rubble Stone — starter recipe).
- Has one crafting category (`OneBlock_Enchanter_Surface`) containing all available expedition crystals.
- All currently available crystals (Default, Cave_Entry, Forest_Edge, Cave, Forest) appear here once their recipes are known.

---

## Dungeon Enchanter

The **Dungeon Enchanter** is the crafting station for dungeon crystals.

- Crafted at the **Fieldcraft** bench (4× Fibre + 3× Rubble Stone — same cost as Crystal Enchanter).
- No dungeon crystals are defined yet; the bench and infrastructure are ready.

---

## World Setup

- The world is a **complete void** — no terrain, no natural generation.
- Players spawn at **(0.5, 102, 0.5)**, directly above the OneBlock.
- **Fall protection** is active: if a player falls below Y=85, they are teleported back to spawn. Players never die from falling into the void.
- The world has no PvP by default.
- Players build their base entirely from materials dropped by the OneBlock.

---

## HUD

While an expedition or dungeon is active, players see a **progress bar HUD** showing:
- The expedition or dungeon name as the title.
- A fill bar representing remaining ticks (expeditions) or completed waves (dungeons).

The HUD is restored if the player disconnects and reconnects while an expedition is active. It hides automatically on completion.

---

## Player Goals

There is no explicit win condition — OneBlock is an **open-ended progression game**. Suggested milestones:

1. Survive the early game with default drops alone.
2. Build a Crystal Enchanter.
3. Complete Cave_Entry and Forest_Edge to unlock Cave and Forest crystals.
4. Complete Cave and Forest expeditions.
5. Clear dungeon content (when added).
6. Build an impressive base in the void using accumulated materials.

---

## Summary of Implemented Systems

| System | Status | Description |
|--------|--------|-------------|
| OneBlock | Done | Central magical block at (0,100,0), always regenerates |
| Default pool | Done | Fibre, Rubble Stone, Dirt — always active between expeditions |
| Expeditions | Done | 5 tiers (Default, Cave_Entry, Forest_Edge, Cave, Forest), 25 ticks each |
| Expedition crystals | Done | One crystal per expedition; crafted at Crystal Enchanter or earned via completion |
| Crystal Enchanter | Done | Crafted at Fieldcraft; holds all expedition crystal recipes |
| Completion reward chain | Done | Cave_Entry → Cave crystal; Forest_Edge → Forest crystal |
| Dungeon system | Framework | Wave-based combat; infrastructure done, no content defined yet |
| Dungeon Enchanter | Framework | Bench exists; no dungeon crystals defined yet |
| Expedition HUD | Done | Per-player progress bar with name and tick/wave fill |
| Void World | Done | Empty world with fall-back protection (teleport below Y=85) |
| Save/load | Done | Expedition and dungeon state persisted to JSON; restored on server restart |
