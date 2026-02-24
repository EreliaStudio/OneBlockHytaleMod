# ModPlan.md
# OneBlock Mod – Development Plan (Hytale)

---

## 1. Project Overview

This mod implements a **OneBlock game mode** with:

- A single regenerating central block (OneBlock)
- Player-based loot progression
- A ranked crafting station (`OneBlockRankWorkbench`)
- A loot configuration station (`OneBlockInterface`)
- 4 progression ranks
- Unlockable loot pools
- Player-controlled loot filtering (enable/disable pools)

The system must be modular, scalable, and persistence-safe.

---

# 2. Core Gameplay Design

## 2.1 OneBlock

- Located at world center (0,0,0 or spawn).
- When broken:
  - Cancel default drop.
  - Immediately regenerate the OneBlock.
  - Roll a random item from player’s **enabled + unlocked loot pools**.
  - Drop the selected reward.

Default unlocked pool:
- Cobble
- Dirt
- Fibre
- Stone rubble
- Stick

---

## 2.2 Progression System

Progression is **per-player**.

Each player has:

- `workbenchRank` (1 → 4)
- `unlockedPools`
- `disabledPools` (from Interface)

---

# 3. Custom Blocks (Pack Assets)

The Pack must define:

### Blocks
- `oneblock:block_oneblock`
- `oneblock:block_rank_workbench`
- `oneblock:block_interface`

### Optional Items
- Unlock items (if unlocks are item-based instead of instant)
- Workbench rank upgrade item (optional)

Workbench rank upgrades:
- Cost: **1 cobble**

Unlock crafts:
- Cost: **1 fibre**

---

# 4. Plugin Architecture

## 4.1 Required Systems

### A) World Initialization
- Generate void world (or minimal platform).
- Place OneBlock at spawn.

---

### B) Player Data System

Persist per-player:

```
PlayerState:
    int workbenchRank
    Set<String> unlockedPools
    Set<String> disabledPools
```

Default values on first join:

```
workbenchRank = 1
unlockedPools = [\default_pool\]
disabledPools = []
```

Requirements:

- Load player state on join.
- Save player state on:
  - rank upgrade
  - pool unlock
  - pool toggle
- If no save exists, initialize with defaults.
- If unknown pools are found during load (removed in newer version), ignore them safely.

---

### C) Loot Pool System

Define core structures:

```
LootPool:
    id
    List<LootEntry>

LootEntry:
    itemId
    weight
```

Example initial pool:

```
default_pool:
    cobble
    dirt
    fibre
    stone_rubble
    stick
```

Rules:

- Pools are identified by stable string IDs.
- New pools are unlocked via workbench upgrades.
- Pools are modular; entries should never be hardcoded directly inside drop logic.
- Weighted random selection should be supported (even if initial weights are equal).

---

### D) OneBlock Break Handler

On block break event:

1. Check if block == `oneblock:block_oneblock`.
2. Cancel normal drop.
3. Immediately replace the block.
4. Compute active pools:

```
activePools = unlockedPools - disabledPools
```

5. If activePools is empty:
   - Fallback to \default_pool\
   - OR notify player and drop nothing (configurable)

6. Gather all LootEntries from activePools.
7. Perform weighted random selection.
8. Drop the selected item at block position.

Design constraints:

- OneBlock must never permanently disappear.
- Drop logic must be player-dependent.
- No world mutation beyond restoring the block.

---

# 5. OneBlockRankWorkbench

## 5.1 Responsibilities

- Upgrade player workbench rank (max 4).
- Craft unlocks (new loot pools).

## 5.2 Rank Upgrade Logic

Rules:

- Max rank = 4.
- Upgrade cost = 1 cobble.
- Rank is per-player.

Upgrade process:

1. Player interacts with `block_rank_workbench`.
2. If `workbenchRank < 4`:
   - Check inventory for 1 cobble.
   - Remove 1 cobble.
   - Increment `workbenchRank`.
   - Save player state.
   - Notify player.

If rank == 4:
- Disable upgrade option.

---

## 5.3 Unlock Crafting

Each unlock:

- Requires specific minimum rank.
- Costs 1 fibre.
- Unlocks a specific loot pool.

Example unlock table:

Rank 1:
- unlock_building_basic

Rank 2:
- unlock_ores_1

Rank 3:
- unlock_rare_materials

Rank 4:
- unlock_exotic

Unlock flow:

1. Player selects unlock option.
2. Verify:
   - Required rank met.
   - Player has 1 fibre.
   - Pool not already unlocked.
3. Remove 1 fibre.
4. Add pool ID to `unlockedPools`.
5. Save state.
6. Notify player.

---

# 6. OneBlockInterface

## 6.1 Purpose

Allow player to enable/disable unlocked loot pools.

This does NOT unlock new pools.
It only filters existing unlocked pools.

---

## 6.2 Interaction Behavior

On interaction with `oneblock:block_interface`:

Open a menu displaying:

- List of all unlockedPools.
- For each:
  - Status: ENABLED / DISABLED
  - Toggle option.

Additional action:
- \Reset All\ → clears disabledPools.

---

## 6.3 Toggle Logic

When toggling a pool:

If pool is in disabledPools:
- Remove from disabledPools (ENABLE)

Else:
- Add to disabledPools (DISABLE)

Save state immediately.

---

## 6.4 Drop Filtering Rule

When rolling loot:

```
activePools = unlockedPools - disabledPools
```

If activePools is empty:

Option A (recommended):
- Fallback to default_pool.

Option B:
- Warn player and drop nothing.

---

# 7. Development Milestones

## Milestone 1 – Core Loop

- Create Pack assets:
  - block_oneblock
  - block_rank_workbench
- Implement break handler.
- Restore OneBlock.
- Drop from default_pool only.

Goal: Infinite OneBlock working.

---

## Milestone 2 – Player Persistence

- Implement PlayerState.
- Load on join.
- Save on change.
- Ensure default_pool unlocked by default.

Goal: Progress survives server restart.

---

## Milestone 3 – Rank Workbench

- Add interaction handler.
- Implement:
  - Rank upgrade (1 cobble).
  - One unlock option (1 fibre).

Goal: First unlock changes drop behavior.

---

## Milestone 4 – Multiple Pools

- Add 2–3 additional pools.
- Gate by rank.
- Validate weighted drop logic.

Goal: Visible progression.

---

## Milestone 5 – Interface Block

- Add `block_interface` asset.
- Add interaction handler.
- Implement pool toggle.
- Update drop logic to respect disabledPools.

Goal: Player-configurable loot filtering.

---

# 8. Data Safety Requirements

- Never hard-delete unknown pools.
- Ignore missing pools gracefully.
- Use stable string IDs for pools.
- Always fallback safely if no active pools.
- Never corrupt player save if structure changes.

---

# 9. Scalability Considerations

This architecture allows:

- Entry-level toggling (future extension):
  - Add `disabledEntries` set.
- Shared server progression mode:
  - Move workbenchRank to world state.
- Dynamic weighted scaling:
  - Increase rare item weight by rank.
- Break-count unlocks:
  - Track block break count in PlayerState.
- Multiplayer-compatible progression:
  - Independent player states.

---

# 10. Design Principles

- Keep Pack purely visual (no gameplay logic).
- Keep all mechanics in Plugin.
- Use data-driven pools.
- Avoid hardcoding item behavior in break handler.
- Keep player progression modular.
- Never mutate OneBlock asset directly.

---

# End of ModPlan.md;