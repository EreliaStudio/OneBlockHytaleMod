# Expedition JSON Guide

This document explains how to write and maintain `expeditions.json`, the input file consumed by `tools/generate_expeditions.py` to generate all OneBlock expedition assets.

## Running the generator

```bash
python tools/generate_expeditions.py expeditions.json --repo-root . [--dry-run] [--clean]
```

| Flag | Effect |
|------|--------|
| `--dry-run` | Print what would change without writing any file |
| `--clean` | Delete all previously generated files before regenerating (removes stale crystals, blocks, and lang entries) |
| `--render-names <file>` | JSON map of item/entity IDs to display names used in descriptions. Defaults to `item_render_names.json` |

---

## Top-level structure

The file is a single JSON object. Every key is an **expedition ID** (the name of the expedition). Keys starting with `_` are ignored and can be used for comments or metadata.

```json
{
  "_comment": "Optional free-text comment, ignored by the generator.",
  "My_Expedition": { ... },
  "Another_Expedition": { ... }
}
```

Spaces in expedition IDs are allowed but will be converted to `_` internally for file names and identifiers.

---

## Expedition entry fields

### Required for all expeditions

| Field | Type | Description |
|-------|------|-------------|
| `ItemLevel` | integer | Item level of the generated crystal and block (affects quality tier: 1 = Common, 2 = Uncommon, 3 = Rare, 4 = Epic) |
| `Category` | string | Enchanter tab group (e.g. `"Surface"`, `"Forest"`, `"Underground"`, `"Cold"`, `"Inferno"`, `"Dark"`). Use `"Dungeon"` to make this a dungeon instead of a regular expedition |
| `Crystal` | object | Crystal crafting recipe — see [Crystal section](#crystal) |

### Required for regular expeditions (not dungeons)

| Field | Type | Description |
|-------|------|-------------|
| `Ticks` | integer | Number of OneBlock ticks the expedition lasts |
| `BaseDropPool` | array | List of items/entities that can drop during the expedition — see [Drop entries](#drop-entries) |

### Required for dungeons (`Category: "Dungeon"`)

| Field | Type | Description |
|-------|------|-------------|
| `Waves` | array of arrays | Each element is a list of entity IDs (strings) that spawn in that wave. The number of waves determines the crystal's tick count |

### Optional for all expeditions

| Field | Type | Description |
|-------|------|-------------|
| `CompletionRewards` | object or array | Items/crystals awarded when the expedition completes — see [Completion Rewards](#completion-rewards) |

> **Legacy alias:** `Rewards` is accepted as an alias for `CompletionRewards` and `Group` as an alias for `Category`.

---

## Crystal

Defines the crafting recipe for the expedition crystal (crafted at the OneBlock Enchanter).

```json
"Crystal": {
  "Input": [
    { "ItemId": "Rock_Stone", "Quantity": 8 },
    { "ItemId": "Ore_Copper", "Quantity": 4 }
  ]
}
```

`Input` is a list of ingredient objects with `ItemId` (string) and `Quantity` (integer).

Crystals that are unlocked by completing another expedition get `KnowledgeRequired: true` automatically — you do not need to set this manually.

---

## Drop entries

Used in `BaseDropPool` and inside reward lists. Each entry is an object identifying the drop and its weight.

### Standard item drop

```json
{ "ID": "Rock_Stone", "Weight": 20 }
```

### Entity/mob drop

Prefix the entity name with `entity:`:

```json
{ "ID": "entity:Boar", "Weight": 10 }
```

### Custom item drop

Custom items are special items generated entirely by the script (icon, texture, JSON definition). Use `CustomID` instead of `ID`:

```json
{ "CustomID": "ExpeditionPoint", "Quantity": 2, "Weight": 5 }
```

The script creates the item JSON, placeholder icon, and texture if they don't already exist.

### Fields

| Field | Default | Description |
|-------|---------|-------------|
| `ID` | — | Standard item or entity ID (`entity:` prefix for entities) |
| `CustomID` | — | Custom generated item ID (mutually exclusive with `ID`) |
| `Weight` | `1` | Relative drop weight (higher = more common) |
| `Quantity` | `1` | Number of items in the drop |
| `RenderName` | *(derived from ID)* | Display name override used in the crystal description |

---

## Completion Rewards

Rewards given to the player when the expedition finishes. Supports two formats.

### Structured format (recommended)

```json
"CompletionRewards": {
  "Mandatory": [
    { "Crystal": "Cave", "Quantity": 1 }
  ],
  "Random": [
    {
      "Weight": 3,
      "Items": [
        { "ID": "Ore_Copper", "Quantity": 5 },
        { "ID": "Ore_Iron", "Quantity": 5 }
      ]
    },
    {
      "Weight": 1,
      "Items": [
        { "CustomID": "ExpeditionPoint", "Quantity": 1 }
      ]
    }
  ]
}
```

- **`Mandatory`** — always awarded on completion. Each entry is a [drop entry](#drop-entries) or a crystal unlock (use `Crystal` key with the expedition ID to award).
- **`Random`** — one bundle is selected by weighted random. Each bundle has a `Weight` and an `Items` list of [drop entries](#drop-entries).

### Crystal unlock reward

To unlock another expedition when this one completes, use `Crystal` instead of `ID`:

```json
{ "Crystal": "Forest", "Quantity": 1 }
```

This awards one Forest Crystal and marks the Forest expedition as unlocked. The Forest crystal will automatically gain `KnowledgeRequired: true` in its crafting recipe.

### Legacy list format

A plain array is treated as a mandatory-only reward list with no random bundles:

```json
"CompletionRewards": [
  { "Crystal": "Cave", "Quantity": 1 }
]
```

---

## Dungeon expeditions

Set `Category` to `"Dungeon"`. Replace `Ticks` and `BaseDropPool` with `Waves`.

```json
"Gobelin_Dungeon": {
  "ItemLevel": 3,
  "Category": "Dungeon",
  "Crystal": {
    "Input": [
      { "ItemId": "Ore_Copper", "Quantity": 10 }
    ]
  },
  "Waves": [
    ["Goblin_Warrior", "Goblin_Warrior"],
    ["Goblin_Shaman", "Goblin_Warrior", "Goblin_Warrior"],
    ["Goblin_Boss"]
  ],
  "CompletionRewards": {
    "Mandatory": [
      { "CustomID": "Locket_GobelinDungeon", "Quantity": 1 }
    ]
  }
}
```

Each element of `Waves` is a list of entity IDs (without the `entity:` prefix — the script adds it). The total number of waves becomes the crystal's tick count.

Dungeons are registered in `OneBlockDungeonDefaults.java` and use the Dungeon Enchanter bench instead of the regular Enchanter.

---

## What the generator produces

For each expedition the script creates or patches:

| Output | Path |
|--------|------|
| OneBlock block JSON | `mods/oneblock/.../Items/OneBlock/OneBlock_Block_<ID>.json` |
| Expedition crystal JSON | `mods/oneblock/.../Items/Crystal/Expedition/OneBlock_Crystal_<ID>.json` |
| Block texture (placeholder) | `Common/BlockTextures/OneBlock_Block_<ID>.png` |
| Block icon (placeholder) | `Common/Icons/ItemsGenerated/OneBlock_<ID>.png` |
| Lang entries | appended to `Server/Languages/en-US/server.lang` |
| Enchanter category | patched into `Bench_OneBlockEnchanter.json` or `Bench_OneBlockDungeonEnchanter.json` |
| Java static block | `OneBlockExpeditionDefaults.java` or `OneBlockDungeonDefaults.java` |
| Custom item JSON + assets | `Items/CustomItems/<CustomID>.json` + icon + texture (only for `CustomID` entries) |

Replace the placeholder PNGs with real artwork before shipping. The script will never overwrite an existing PNG.

---

## Full example

```json
{
  "_comment": "Example expedition file",

  "Default": {
    "ItemLevel": 1,
    "Category": "Surface",
    "Ticks": 25,
    "Crystal": {
      "Input": [
        { "ItemId": "Ingredient_Fibre", "Quantity": 4 }
      ]
    },
    "BaseDropPool": [
      { "ID": "Ingredient_Fibre", "Weight": 20 },
      { "ID": "Rubble_Stone",     "Weight": 20 },
      { "ID": "Soil_Dirt",        "Weight": 10 }
    ]
  },

  "Cave_Entry": {
    "ItemLevel": 1,
    "Category": "Surface",
    "Ticks": 25,
    "Crystal": {
      "Input": [
        { "ItemId": "Rubble_Stone", "Quantity": 4 }
      ]
    },
    "BaseDropPool": [
      { "ID": "Rubble_Stone", "Weight": 20 },
      { "ID": "Rock_Stone",   "Weight": 20 }
    ],
    "CompletionRewards": {
      "Mandatory": [
        { "Crystal": "Cave", "Quantity": 1 }
      ],
      "Random": []
    }
  },

  "Cave": {
    "ItemLevel": 2,
    "Category": "Surface",
    "Ticks": 40,
    "Crystal": {
      "Input": [
        { "ItemId": "Rock_Stone", "Quantity": 8 },
        { "ItemId": "Ore_Copper", "Quantity": 4 }
      ]
    },
    "BaseDropPool": [
      { "ID": "Rock_Stone", "Weight": 20 },
      { "ID": "Ore_Copper", "Weight": 20 }
    ],
    "CompletionRewards": {
      "Mandatory": [],
      "Random": [
        { "Weight": 3, "Items": [{ "ID": "Ore_Copper", "Quantity": 5 }] },
        { "Weight": 1, "Items": [{ "CustomID": "ExpeditionPoint", "Quantity": 1 }] }
      ]
    }
  }
}
```
