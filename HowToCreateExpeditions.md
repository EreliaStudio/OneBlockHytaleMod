# How to Create Expeditions — Editor Guide

This document explains how to design and add a new expedition to the OneBlock mod. It is written for a co-editor who may not be familiar with the codebase. You do not need to write any Java code to add a new expedition — all changes are in JSON files and one Java constants class.

---

## What is an Expedition?

An expedition is a themed tier that the player temporarily activates by using an **Expedition Crystal** on the OneBlock. The block switches to that expedition's drop pool for a fixed number of breaks (ticks). When the ticks run out, the expedition completes, the block reverts to default Meadow mode, and the player unlocks the expedition's bench recipe.

Each expedition has:
- A **unique ID** (CamelCase, no spaces — used in all file names and code).
- A **base drop pool** (items and creatures that can drop during the expedition).
- Two **Expedition Crystal** items (Small = 100 ticks, Large = 300 ticks).
- An **Expedition Bench** that is unlocked upon completing the expedition.

---

## File Overview

You will need to create or edit the following files:

| File | Where | What it does |
|------|-------|-------------|
| `expeditionsTemplate.json` | repo root | Master reference document. **Edit this first as your design source of truth.** |
| `OneBlock_Block_<ExpeditionId>.json` | `mods/oneblock-core/src/main/resources/Server/Item/Items/OneBlockBlocks/` | Defines the visual block variant |
| `OneBlock_Crystal_<ExpeditionId>_Small.json` | `mods/oneblock-progression/src/main/resources/Server/Item/Items/Crystal/Expedition/` | Small expedition crystal item (100 ticks) |
| `OneBlock_Crystal_<ExpeditionId>_Large.json` | `mods/oneblock-progression/src/main/resources/Server/Item/Items/Crystal/Expedition/` | Large expedition crystal item (300 ticks) |
| `Bench_OneBlockEnchanter.json` | `mods/oneblock-progression/src/main/resources/Server/Item/Items/OneBlockEnchanter/` | Add your two crystal recipes to the matching category |
| `Bench_OneBlockWorkbench.json` | `mods/oneblock-progression/src/main/resources/Server/Item/Items/OneBlockWorkbench/` | Add your expedition bench ID to the recipe list |
| `Bench_OneBlock_<ExpeditionId>.json` | `mods/oneblock-progression/src/main/resources/Server/Item/Items/ExpeditionBench/` | The expedition's dedicated craftable bench |
| `OneBlock_Bench_Recipe_<ExpeditionId>.json` | `mods/oneblock-progression/src/main/resources/Server/Item/Items/BenchRecipe/` | Recipe item dropped at expedition completion; consuming it teaches the bench recipe |
| `OneBlockExpeditionDefaults.java` | `mods/oneblock-progression/src/main/java/…/` | Register the base drop pool in code |
| `server.lang` | `mods/oneblock-progression/src/main/resources/Server/Languages/en-US/` | Display names and descriptions |

---

## Step-by-Step: Adding a New Expedition

### Step 1 — Design the Expedition in `expeditionsTemplate.json`

Open `expeditionsTemplate.json` at the repo root. Add a new entry following this structure:

```json
"<ExpeditionId>": {
  "CrystalCost": {
    "Small": [
      { "ID": "OneBlock_Crystal_Blue",   "Quantity": 6 },
      { "ID": "OneBlock_Crystal_Red",    "Quantity": 3 }
    ],
    "Large": [
      { "ID": "OneBlock_Crystal_Blue",   "Quantity": 12 },
      { "ID": "OneBlock_Crystal_Red",    "Quantity": 8 },
      { "ID": "OneBlock_Crystal_Yellow", "Quantity": 4 }
    ]
  },
  "BaseDropPool": [
    { "ID": "<ItemId>",          "Weight": <number> },
    { "ID": "entity:<EntityId>", "Weight": <number> }
  ],
  "BenchRecipe": [
    { "ID": "<ItemId>", "Quantity": <number> }
  ],
  "BenchUpgrades": {
    "Tier2": [ { "ID": "<ItemId>", "Quantity": <number> } ],
    "Tier3": [ { "ID": "<ItemId>", "Quantity": <number> } ]
  }
}
```

#### Weight guidelines

| Rarity | Suggested weight |
|--------|-----------------|
| Very common (filler material) | 20–30 |
| Common | 10–15 |
| Uncommon | 5–8 |
| Rare | 2–4 |
| Very rare | 1–2 |

Keep total base pool weight in the 50–100 range so probabilities are easy to reason about.

#### Item IDs

Refer to `ItemList.md` at the repo root for a full list of valid Hytale item and entity IDs.

---

### Step 2 — Create the OneBlock Variant Block

Create the file:
```
mods/oneblock-core/src/main/resources/Server/Item/Items/OneBlockBlocks/OneBlock_Block_<ExpeditionId>.json
```

Copy any existing block file (e.g., `OneBlock_Block_Cave.json`) as a base. Change:
- The `"Id"` field to `OneBlock_Block_<ExpeditionId>`
- Textures to match your expedition's visual theme

**These two fields MUST stay exactly as-is** (the break system depends on them):
```json
"Categories": ["Blocks.OneBlock"]
```
```json
"Tags": { "Type": ["OneBlock_Block"] }
```

The pool resolver extracts the expedition name by stripping the `OneBlock_Block_` prefix from the block type ID.

---

### Step 3 — Create the Two Expedition Crystal Items

Create these two files:
```
mods/oneblock-progression/src/main/resources/Server/Item/Items/Crystal/Expedition/OneBlock_Crystal_<ExpeditionId>_Small.json
mods/oneblock-progression/src/main/resources/Server/Item/Items/Crystal/Expedition/OneBlock_Crystal_<ExpeditionId>_Large.json
```

Copy any existing crystal file as a base. For each file, change:
- `"Id"` to `OneBlock_Crystal_<ExpeditionId>_Small` (or `_Large`)
- `"ItemLevel"` to reflect the expedition tier
- `"Quality"` appropriately (Small: Uncommon/Rare, Large: Rare/Epic)
- The `"Recipe"` inputs (Blue/Red/Yellow crystal quantities from your design in Step 1)
- The bench requirement category ID to `OneBlock_Enchanter_<ExpeditionId>`

**The `"Interactions"` block must stay exactly as-is** — it references the crystal interaction system:
```json
"Interactions": {
  "Use": {
    "Interactions": [ { "Type": "oneblock_crystal_use" } ]
  }
}
```

**The item ID naming convention is critical.** The crystal interaction system extracts the expedition name and tick count purely from the item ID:
- `OneBlock_Crystal_<ExpeditionId>_Small` → expedition = `<ExpeditionId>`, ticks = 100
- `OneBlock_Crystal_<ExpeditionId>_Large` → expedition = `<ExpeditionId>`, ticks = 300

Do not deviate from this pattern.

---

### Step 4 — Add Crystal Recipes to the Crystal Enchanter

Open:
```
mods/oneblock-progression/src/main/resources/Server/Item/Items/OneBlockEnchanter/Bench_OneBlockEnchanter.json
```

Add a new category entry inside `"Bench" → "Categories"`:

```json
{
  "Id": "OneBlock_Enchanter_<ExpeditionId>",
  "Icon": "Icons/CraftingCategories/ExpeditionKey.png",
  "Name": "server.benchCategories.OneBlockEnchanter_<ExpeditionId>",
  "Recipes": [
    "OneBlock_Crystal_<ExpeditionId>_Small",
    "OneBlock_Crystal_<ExpeditionId>_Large"
  ]
}
```

---

### Step 5 — Create the Expedition Bench

Create the file:
```
mods/oneblock-progression/src/main/resources/Server/Item/Items/ExpeditionBench/Bench_OneBlock_<ExpeditionId>.json
```

Copy any existing expedition bench file as a base. Change:
- `"Id"` to `Bench_OneBlock_<ExpeditionId>`
- `"ItemLevel"` to reflect the expedition tier
- The `"Recipe"` inputs (what the player needs to craft it at the OneBlock Workbench)
- The bench `"Id"` field inside `"BlockType" → "Bench"` to `OneBlock_Bench_<ExpeditionId>`
- The `TierLevels` upgrade costs for Tier 2 and Tier 3
- The category `"Id"` inside the bench to `OneBlock_Bench_<ExpeditionId>_Tier1`
- The category `"Name"` to `server.benchCategories.OneBlock_Bench_<ExpeditionId>_Tier1`

**The `"KnowledgeRequired": true` field in the recipe MUST be present.** This is what gates the recipe behind expedition completion. Without it, the bench is craftable immediately.

---

### Step 6 — Create the Bench Recipe Item

Create the file:
```
mods/oneblock-progression/src/main/resources/Server/Item/Items/BenchRecipe/OneBlock_Bench_Recipe_<ExpeditionId>.json
```

Copy any existing bench recipe item file as a base. Change:
- `"Id"` to `OneBlock_Bench_Recipe_<ExpeditionId>`
- `"ItemLevel"` to reflect the expedition tier
- `"Quality"` appropriately
- The `"RecipeDropTarget"` tag value to `Bench_OneBlock_<ExpeditionId>`

This item is **automatically spawned at the OneBlock position** when the expedition completes. The player picks it up, consumes it, and the game engine teaches them the matching bench recipe (which is gated by `KnowledgeRequired: true`).

The item ID must be exactly `OneBlock_Bench_Recipe_<ExpeditionId>` — the completion handler constructs this name at runtime from the expedition ID.

---

### Step 8 — Add the Expedition Bench Recipe to the OneBlock Workbench

Open:
```
mods/oneblock-progression/src/main/resources/Server/Item/Items/OneBlockWorkbench/Bench_OneBlockWorkbench.json
```

Find the `"Recipes"` array inside the `OneBlock_Workbench_ExpeditionBenches` category and add your bench ID:

```json
"Recipes": [
  "Bench_OneBlock_FarmLand",
  "Bench_OneBlock_Forest",
  "Bench_OneBlock_Cave",
  "Bench_OneBlock_Deep Cave",
  "Bench_OneBlock_The Abyss",
  "Bench_OneBlock_<ExpeditionId>"
]
```

---

### Step 9 — Register the Base Drop Pool in Code

Open:
```
mods/oneblock-progression/src/main/java/com/EreliaStudio/OneBlock/OneBlockExpeditionDefaults.java
```

Find the `static { ... }` block at the top. Add your expedition's base drops following the same pattern as the existing ones:

```java
defaults.put("<ExpeditionId>", List.of(
    drop("ItemId_One", 25),
    drop("ItemId_Two", 15),
    drop(OneBlockDropId.entityDropId("EntityId"), 3)
));
```

Use `drop("itemId", weight)` for item drops and `OneBlockDropId.entityDropId("EntityId")` for creature spawns.

The IDs and weights must match what you wrote in `BaseDropPool` in `expeditionsTemplate.json`.

---

### Step 10 — Add Lang Entries

Open:
```
mods/oneblock-progression/src/main/resources/Server/Languages/en-US/server.lang
```

Add entries for:
- The two crystal items (Small + Large)
- The expedition bench item
- The bench category in the Crystal Enchanter
- The bench category in the OneBlock Workbench
- The tier 1 category in the expedition bench

```
server.items.OneBlock_Crystal_<ExpeditionId>_Small.name=<ExpeditionId> Crystal (Small)
server.items.OneBlock_Crystal_<ExpeditionId>_Small.description=Use on the OneBlock to begin a 100-tick <ExpeditionId> expedition.

server.items.OneBlock_Crystal_<ExpeditionId>_Large.name=<ExpeditionId> Crystal (Large)
server.items.OneBlock_Crystal_<ExpeditionId>_Large.description=Use on the OneBlock to begin a 300-tick <ExpeditionId> expedition.

server.items.Bench_OneBlock_<ExpeditionId>.name=<ExpeditionId> Bench
server.items.Bench_OneBlock_<ExpeditionId>.description=An expedition bench for <ExpeditionId>. Upgrade it to unlock more powerful recipes.

server.benchCategories.OneBlockEnchanter_<ExpeditionId>=<ExpeditionId> Crystals
server.benchCategories.OneBlock_Bench_<ExpeditionId>_Tier1=<ExpeditionId> Recipes

server.items.OneBlock_Bench_Recipe_<ExpeditionId>.name=Recipe: <ExpeditionId> Bench
server.items.OneBlock_Bench_Recipe_<ExpeditionId>.description=Consume to unlock the <ExpeditionId> Bench crafting recipe at the OneBlock Workbench.
```

---

### Step 11 — Build and Deploy

From the repo root:

```bash
./gradlew buildAndDeployAll
```

Then restart the server.

---

## Full Example — New Expedition "Swamp"

Assume expedition ID: `Swamp`

### `expeditionsTemplate.json` entry

```json
"Swamp": {
  "CrystalCost": {
    "Small": [
      { "ID": "OneBlock_Crystal_Blue", "Quantity": 5 },
      { "ID": "OneBlock_Crystal_Red",  "Quantity": 2 }
    ],
    "Large": [
      { "ID": "OneBlock_Crystal_Blue",   "Quantity": 10 },
      { "ID": "OneBlock_Crystal_Red",    "Quantity": 6 },
      { "ID": "OneBlock_Crystal_Yellow", "Quantity": 2 }
    ]
  },
  "BaseDropPool": [
    { "ID": "Soil_Mud",          "Weight": 25 },
    { "ID": "Plant_Reed",        "Weight": 20 },
    { "ID": "Ingredient_Fibre",  "Weight": 15 },
    { "ID": "entity:Frog",       "Weight": 3  }
  ],
  "BenchRecipe": [
    { "ID": "Soil_Mud",    "Quantity": 32 },
    { "ID": "Plant_Reed",  "Quantity": 16 }
  ],
  "BenchUpgrades": {
    "Tier2": [ { "ID": "OneBlock_Crystal_Blue", "Quantity": 32 } ],
    "Tier3": [ { "ID": "OneBlock_Crystal_Red",  "Quantity": 24 } ]
  }
}
```

### `OneBlock_Crystal_Swamp_Small.json` (key fields only)

```json
{
  "Id": "OneBlock_Crystal_Swamp_Small",
  "ItemLevel": 2,
  "Interactions": { "Use": { "Interactions": [ { "Type": "oneblock_crystal_use" } ] } },
  "Recipe": {
    "Input": [
      { "ItemId": "OneBlock_Crystal_Blue", "Quantity": 5 },
      { "ItemId": "OneBlock_Crystal_Red",  "Quantity": 2 }
    ],
    "OutputQuantity": 1,
    "BenchRequirement": [ { "Type": "Crafting", "Categories": ["OneBlock_Enchanter_Swamp"], "Id": "OneBlockEnchanter" } ]
  },
  "Consumable": true,
  "Tags": {
    "OneBlockCrystalExpedition": ["Swamp"],
    "OneBlockCrystalSize": ["Small"]
  },
  "MaxStack": 4,
  "Quality": "Uncommon"
}
```

### `OneBlockExpeditionDefaults.java` addition

```java
defaults.put("Swamp", List.of(
    drop("Soil_Mud",         25),
    drop("Plant_Reed",       20),
    drop("Ingredient_Fibre", 15),
    drop(OneBlockDropId.entityDropId("Frog"), 3)
));
```

### Crystal Enchanter category addition

```json
{
  "Id": "OneBlock_Enchanter_Swamp",
  "Icon": "Icons/CraftingCategories/ExpeditionKey.png",
  "Name": "server.benchCategories.OneBlockEnchanter_Swamp",
  "Recipes": [
    "OneBlock_Crystal_Swamp_Small",
    "OneBlock_Crystal_Swamp_Large"
  ]
}
```

### Files to create

| File | Notes |
|------|-------|
| `OneBlock_Block_Swamp.json` | Keep `Blocks.OneBlock` category |
| `OneBlock_Crystal_Swamp_Small.json` | ID must be `OneBlock_Crystal_Swamp_Small` |
| `OneBlock_Crystal_Swamp_Large.json` | ID must be `OneBlock_Crystal_Swamp_Large` |
| `Bench_OneBlock_Swamp.json` | Must have `"KnowledgeRequired": true` in recipe |
| `OneBlock_Bench_Recipe_Swamp.json` | ID must be exactly `OneBlock_Bench_Recipe_Swamp`; `RecipeDropTarget` = `Bench_OneBlock_Swamp` |

---

## Quick Checklist

- [ ] Added expedition entry to `expeditionsTemplate.json`
- [ ] Created `OneBlock_Block_<ExpeditionId>.json` (with `"Categories": ["Blocks.OneBlock"]` intact)
- [ ] Created `OneBlock_Crystal_<ExpeditionId>_Small.json` (ID must end in `_Small`)
- [ ] Created `OneBlock_Crystal_<ExpeditionId>_Large.json` (ID must end in `_Large`)
- [ ] Added a category for the expedition in `Bench_OneBlockEnchanter.json`
- [ ] Created `Bench_OneBlock_<ExpeditionId>.json` (must have `"KnowledgeRequired": true` in recipe)
- [ ] Added the bench ID to `Bench_OneBlockWorkbench.json` recipe list
- [ ] Created `OneBlock_Bench_Recipe_<ExpeditionId>.json` (ID must match exactly — used by completion handler)
- [ ] Added base drop pool entries to `OneBlockExpeditionDefaults.java`
- [ ] Added lang entries in `server.lang`
- [ ] Ran `./gradlew buildAndDeployAll`
- [ ] Restarted the server

---

## Common Mistakes

| Mistake | Symptom | Fix |
|---------|---------|-----|
| Crystal item ID does not end in `_Small` or `_Large` | Crystal consumed, expedition never starts | Fix the ID — the system extracts tick count from the suffix |
| `<ExpeditionId>` in crystal ID doesn't match block `OneBlock_Block_<ExpeditionId>` | Wrong expedition pool is used | Make the middle segment match exactly |
| Missing `"Categories": ["Blocks.OneBlock"]` in block JSON | Block break does not trigger the drop system | Add the category |
| Missing `"KnowledgeRequired": true` in expedition bench recipe | Bench is craftable immediately without completing the expedition | Add the field |
| `OneBlock_Bench_Recipe_<ExpeditionId>` item ID doesn't match expedition ID exactly | Completion spawns nothing (registry miss) | ID must be `OneBlock_Bench_Recipe_` + exactly the expedition ID string |
| Expedition not added to `Bench_OneBlockWorkbench.json` recipe list | Recipe never appears in the workbench | Add the bench ID to the list |
| Base pool not added to `OneBlockExpeditionDefaults.java` | Crystal starts the expedition but block drops nothing | Add the `defaults.put(...)` entry |
| Forgot to add a Crystal Enchanter category | Crystals can't be crafted | Add the category to `Bench_OneBlockEnchanter.json` |
| Forgot to rebuild after changes | Old behavior persists | Run `./gradlew buildAndDeployAll` |
