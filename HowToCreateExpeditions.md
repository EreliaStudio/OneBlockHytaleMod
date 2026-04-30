# How to Create Expeditions — Editor Guide

This document explains how to design and add a new expedition to the OneBlock mod. All game content lives in a single module (`mods/oneblock/`). Adding an expedition requires editing one Java file and creating a handful of JSON files. No code outside of `OneBlockExpeditionDefaults.java` needs to change.

---

## What is an Expedition?

An expedition is a themed mode that the player activates by using an **Expedition Crystal** on the OneBlock. The block switches to that expedition's drop pool for a fixed number of breaks (ticks). When the ticks run out, the expedition completes, the block reverts to default, and any defined completion rewards are given.

Each expedition has:
- A **unique ID** (PascalCase with underscores allowed — e.g., `Cave_Entry`, `Forest`, `Deep_Cave`).
- A **tick count** — how many OneBlock breaks the expedition lasts.
- A **drop pool** — weighted list of items and/or entities that can drop.
- Optional **completion rewards** — items dropped and/or recipes unlocked when the expedition ends.
- A **block variant** JSON — visual appearance of the OneBlock during the expedition.
- A **crystal item** JSON — the consumable used to start the expedition.

---

## File Overview

| File | Location | Purpose |
|------|----------|---------|
| `OneBlockExpeditionDefaults.java` | `mods/oneblock/src/main/java/…/` | Register the expedition (drops, ticks, rewards) |
| `OneBlock_Block_<ExpeditionId>.json` | `mods/oneblock/src/main/resources/Server/Item/Items/OneBlock/` | OneBlock visual variant during the expedition |
| `OneBlock_Crystal_<ExpeditionId>.json` | `mods/oneblock/src/main/resources/Server/Item/Items/Crystal/Expedition/` | The crystal item that starts the expedition |
| `Bench_OneBlockEnchanter.json` | `mods/oneblock/src/main/resources/Server/Item/Items/OneBlockEnchanter/` | Add crystal recipe to the enchanter |
| `server.lang` | `mods/oneblock/src/main/resources/Server/Languages/en-US/` | Display names and descriptions |

---

## Step-by-Step: Adding a New Expedition

### Step 1 — Register the Expedition in Code

Open:
```
mods/oneblock/src/main/java/com/EreliaStudio/OneBlock/OneBlockExpeditionDefaults.java
```

Inside the `static { ... }` block, add a `register(...)` call for your expedition. The block ID is derived automatically as `"OneBlock_Block_" + expeditionId`.

**Expedition with no completion reward:**
```java
register(expeditions, "Swamp", 25, List.of(
    drop("Soil_Mud",         25),
    drop("Plant_Reed",       20),
    drop("Ingredient_Fibre", 15),
    drop(OneBlockDropId.entityDropId("Frog"), 3)
));
```

**Expedition that unlocks a follow-up expedition on completion:**
```java
register(expeditions, "Swamp_Entry", 25, List.of(
    drop("Soil_Mud",   25),
    drop("Plant_Reed", 20)
), List.of(
    crystalReward("Swamp", 1)   // drops Crystal_Swamp + teaches its recipe
));
```

**Expedition with a random reward bundle (one bundle chosen by weight):**
```java
register(expeditions, "Swamp", 25, List.of(
    drop("Soil_Mud",   25),
    drop("Plant_Reed", 20)
), List.of(
    reward("Ingredient_Fibre", 5)   // always given
), List.of(
    bundle(List.of(reward("Ore_Copper", 3)), 70),
    bundle(List.of(reward("Ore_Iron",   2)), 30)
));
```

#### Drop weight guidelines

| Rarity | Suggested weight |
|--------|-----------------|
| Very common (filler material) | 20–30 |
| Common | 10–15 |
| Uncommon | 5–8 |
| Rare | 2–4 |
| Very rare | 1–2 |

Use `drop("ItemId", weight)` for item drops.
Use `drop(OneBlockDropId.entityDropId("EntityId"), weight)` for entity spawns.

Refer to `ItemList.md` at the repo root for valid Hytale item and entity IDs.

---

### Step 2 — Create the Block Variant

Create the file:
```
mods/oneblock/src/main/resources/Server/Item/Items/OneBlock/OneBlock_Block_<ExpeditionId>.json
```

Copy an existing file (e.g., `OneBlock_Block_Cave.json`) as a base. Change:
- The `"Id"` field to `OneBlock_Block_<ExpeditionId>`
- Textures to match your expedition's visual theme

**These two fields MUST stay exactly as-is** — the break system depends on them:
```json
"Categories": ["Blocks.OneBlock"]
```
```json
"Tags": { "Type": ["OneBlock_Block"] }
```

The pool resolver extracts the expedition name by stripping the `OneBlock_Block_` prefix from the block type ID, so the ID must match exactly.

---

### Step 3 — Create the Crystal Item

Create the file:
```
mods/oneblock/src/main/resources/Server/Item/Items/Crystal/Expedition/OneBlock_Crystal_<ExpeditionId>.json
```

Copy an existing crystal file as a base (e.g., `OneBlock_Crystal_Cave_Entry.json`). Change:
- `"Id"` to `OneBlock_Crystal_<ExpeditionId>`
- `"ItemLevel"` to reflect the expedition tier
- `"Quality"` appropriately
- The `"Recipe"` inputs (what materials are needed to craft it)
- The bench requirement category to `OneBlock_Enchanter_Surface` (all expedition crystals use the same category)

**The `"Interactions"` block must stay exactly as-is:**
```json
"Interactions": {
  "Primary":   { "Interactions": [ { "Type": "oneblock_crystal_use" } ] },
  "Secondary": { "Interactions": [ { "Type": "oneblock_crystal_use" } ] }
}
```

**The `"Tags"` block must include the expedition ID and tick count:**
```json
"Tags": {
  "Type": ["OneBlock_ExpeditionCrystal"],
  "OneBlockCrystalExpedition": ["<ExpeditionId>"],
  "OneBlockCrystalTicks": ["25"]
}
```

**The item ID naming convention is critical.** The crystal interaction system extracts the expedition name from the item ID by stripping the `OneBlock_Crystal_` prefix. `OneBlock_Crystal_Swamp` → expedition ID = `Swamp`. Do not deviate from this pattern.

---

### Step 4 — Add the Crystal to the Crystal Enchanter

Open:
```
mods/oneblock/src/main/resources/Server/Item/Items/OneBlockEnchanter/Bench_OneBlockEnchanter.json
```

Find the `"Recipes"` array inside the `OneBlock_Enchanter_Surface` category and add your crystal ID:

```json
{
  "Id": "OneBlock_Enchanter_Surface",
  "Recipes": [
    "OneBlock_Crystal_Default",
    "OneBlock_Crystal_Cave_Entry",
    "OneBlock_Crystal_Forest_Edge",
    "OneBlock_Crystal_Cave",
    "OneBlock_Crystal_Forest",
    "OneBlock_Crystal_<ExpeditionId>"
  ]
}
```

Only add the crystal here if you want it to be directly craftable. If the crystal is **only** obtainable as a completion reward from another expedition (and not craftable), skip this step.

---

### Step 5 — Add Lang Entries

Open:
```
mods/oneblock/src/main/resources/Server/Languages/en-US/server.lang
```

Add entries for the crystal item:
```
server.items.OneBlock_Crystal_<ExpeditionId>.name=<Expedition Name> Crystal
server.items.OneBlock_Crystal_<ExpeditionId>.description=Use on the OneBlock to begin a 25-tick <Expedition Name> expedition.
```

---

### Step 6 — Build and Deploy

From the repo root:

```bash
./gradlew buildAndDeployAll
```

Then restart the server.

---

## Full Example — New Expedition "Swamp"

Expedition ID: `Swamp`
Preceded by `Swamp_Entry` (which gives the `Swamp` crystal on completion).

### `OneBlockExpeditionDefaults.java` additions

```java
// Entry expedition — craftable, gives Swamp crystal on completion
register(expeditions, "Swamp_Entry", 25, List.of(
    drop("Soil_Mud",   25),
    drop("Plant_Reed", 20)
), List.of(
    crystalReward("Swamp", 1)
));

// Main expedition — crystal only obtained from Swamp_Entry completion
register(expeditions, "Swamp", 25, List.of(
    drop("Soil_Mud",          25),
    drop("Plant_Reed",        20),
    drop("Ingredient_Fibre",  15),
    drop(OneBlockDropId.entityDropId("Frog"), 3)
));
```

### `OneBlock_Crystal_Swamp_Entry.json` (key fields)

```json
{
  "Id": "OneBlock_Crystal_Swamp_Entry",
  "ItemLevel": 2,
  "Interactions": {
    "Primary":   { "Interactions": [ { "Type": "oneblock_crystal_use" } ] },
    "Secondary": { "Interactions": [ { "Type": "oneblock_crystal_use" } ] }
  },
  "Recipe": {
    "Input": [ { "ItemId": "Soil_Mud", "Quantity": 4 } ],
    "OutputQuantity": 1,
    "BenchRequirement": [ { "Type": "Crafting", "Categories": ["OneBlock_Enchanter_Surface"], "Id": "OneBlockEnchanter" } ]
  },
  "Consumable": true,
  "Tags": {
    "Type": ["OneBlock_ExpeditionCrystal"],
    "OneBlockCrystalExpedition": ["Swamp_Entry"],
    "OneBlockCrystalTicks": ["25"]
  },
  "MaxStack": 4,
  "Quality": "Uncommon"
}
```

### `OneBlock_Crystal_Swamp.json` (key fields)

```json
{
  "Id": "OneBlock_Crystal_Swamp",
  "ItemLevel": 3,
  "Interactions": {
    "Primary":   { "Interactions": [ { "Type": "oneblock_crystal_use" } ] },
    "Secondary": { "Interactions": [ { "Type": "oneblock_crystal_use" } ] }
  },
  "Consumable": true,
  "Tags": {
    "Type": ["OneBlock_ExpeditionCrystal"],
    "OneBlockCrystalExpedition": ["Swamp"],
    "OneBlockCrystalTicks": ["25"]
  },
  "MaxStack": 4,
  "Quality": "Rare"
}
```

*(No `Recipe` block — this crystal is not craftable, only earned.)*

### Files to create

| File | Notes |
|------|-------|
| `OneBlock_Block_Swamp_Entry.json` | Keep `Blocks.OneBlock` category and `OneBlock_Block` tag |
| `OneBlock_Block_Swamp.json` | Keep `Blocks.OneBlock` category and `OneBlock_Block` tag |
| `OneBlock_Crystal_Swamp_Entry.json` | ID must be `OneBlock_Crystal_Swamp_Entry` |
| `OneBlock_Crystal_Swamp.json` | ID must be `OneBlock_Crystal_Swamp`; no `Recipe` block |

---

## Quick Checklist

- [ ] Added expedition entry (or entries) in `OneBlockExpeditionDefaults.java`
- [ ] Created `OneBlock_Block_<ExpeditionId>.json` (with `"Categories": ["Blocks.OneBlock"]` and `"Tags": {"Type": ["OneBlock_Block"]}`)
- [ ] Created `OneBlock_Crystal_<ExpeditionId>.json` (ID must be `OneBlock_Crystal_` + exactly the expedition ID)
- [ ] Crystal `"Tags"` contains `"OneBlockCrystalExpedition": ["<ExpeditionId>"]`
- [ ] Crystal `"Interactions"` uses `oneblock_crystal_use` for both Primary and Secondary
- [ ] Added crystal to `Bench_OneBlockEnchanter.json` (if craftable)
- [ ] Added lang entries in `server.lang`
- [ ] Ran `./gradlew buildAndDeployAll`
- [ ] Restarted the server

---

## Common Mistakes

| Mistake | Symptom | Fix |
|---------|---------|-----|
| Crystal item ID doesn't match expedition ID | Crystal consumed, wrong expedition starts (or none) | Crystal ID must be `OneBlock_Crystal_` + expedition ID exactly |
| `"Categories": ["Blocks.OneBlock"]` missing from block JSON | Block break does not trigger the drop system | Add the category |
| `"Tags": {"Type": ["OneBlock_Block"]}` missing from block JSON | Block not recognized as a OneBlock variant | Add the tag |
| Expedition not registered in `OneBlockExpeditionDefaults.java` | Crystal starts the expedition but block drops nothing | Add the `register(...)` call |
| Crystal not added to enchanter category | Crystal can't be crafted (if intended to be craftable) | Add its ID to `OneBlock_Enchanter_Surface` recipes |
| `crystalReward("X", 1)` used but `OneBlock_Crystal_X` item JSON doesn't exist | Completion spawns an invisible item; recipe never learned | Create the crystal item JSON |
| Forgot to rebuild after changes | Old behavior persists | Run `./gradlew buildAndDeployAll` |
