# Achievement JSON Guide

This document explains how to write `achievements.authoring.json`, compile it with `tools/compile_achievements.py`, install the generated definitions, and build the OneBlock Achievement mod.

## Files and data flow

Achievement definitions use two separate JSON formats:

| File | Purpose |
|---|---|
| `achievements.authoring.json` | The compact, human-readable source file that you edit. |
| `achievements.json` | The normalized runtime file produced by the compiler and loaded by the mod. |
| `players.json` | Runtime player progress written by the mod. Never generate or edit this as an achievement definition. |

The normal flow is:

```text
achievements.authoring.json
        -> tools/compile_achievements.py
        -> achievements.json
        -> OneBlock Achievement mod
```

Keep `achievements.authoring.json` as the source of truth. Do not manually maintain both definition formats.

---

## Running the compiler

From the repository root, run:

```powershell
python tools/compile_achievements.py achievements.authoring.json mods/oneblock-achievement/src/main/resources/achievements.json
```

The command takes two positional arguments:

| Argument | Meaning |
|---|---|
| `input` | Path to the human-readable authoring JSON. |
| `output` | Path where the normalized runtime `achievements.json` will be written. Parent directories are created automatically. |

A successful run prints the number of compiled achievements:

```text
Compiled 2 achievements to mods\oneblock-achievement\src\main\resources\achievements.json
```

If validation fails, no usable output is produced. Correct the reported error and run the command again.

---

## Top-level structure

The authoring file is one JSON object containing an `achievements` array:

```json
{
  "achievements": [
    {
      "id": "beginner_miner",
      "name": "Beginner Miner",
      "title": "Beginner Miner"
    }
  ]
}
```

JSON does not support comments. Use descriptive IDs and names instead of adding `//` or `#` comment lines, which would make the file invalid.

Achievements are emitted in the same order in which they appear in this array. Put root achievements first and later stages after their prerequisites so that the configuration and UI remain easy to understand.

---

## Achievement fields

| Field | Type | Required | Default | Description |
|---|---|---:|---|---|
| `id` | string | Yes | — | Stable internal achievement identifier. |
| `name` | string | Yes | — | Player-facing achievement name shown in commands and the UI. |
| `title` | string | Yes | — | Text displayed in chat and the in-world nameplate when selected. |
| `requires` | array of strings | No | `[]` | Achievement IDs that must already be complete. |
| `money` | integer | No | `0` | Glymera currency that must be contributed. |
| `items` | object | No | `{}` | Map of exact Hytale item IDs to required quantities. |
| `expeditions` | array of strings | No | `[]` | OneBlock expedition IDs the player must know. |

All cost types are combined with **AND**. If an achievement specifies money, two items, and three expeditions, all of them must be satisfied before it completes.

### `id`

The ID is used in prerequisites, commands, saved progress, and compiler output.

```json
"id": "beginner_miner"
```

Rules:

- It must start with a lowercase letter or digit.
- It may contain lowercase letters, digits, `_`, and `-`.
- It cannot contain spaces or uppercase letters.
- It must be unique across the entire file.
- It should never be changed after players begin progressing unless you intentionally want the renamed achievement to behave like a new achievement.

Good IDs include `beginner_miner`, `miner-2`, and `all_expeditions`. Invalid examples include `BeginnerMiner`, `beginner miner`, and `_miner`.

### `name`

`name` is the readable achievement name:

```json
"name": "Beginner Miner"
```

It may differ from the unlocked title. For example, an achievement named `Master Every Mine` could unlock the title `Deep Delver`.

### `title`

`title` is the selectable prefix awarded by the achievement:

```json
"title": "Deep Delver"
```

Titles must be non-empty, must fit on one line, and may contain at most 64 characters. Brackets are added by the mod, so write `Beginner Miner`, not `[Beginner Miner]`.

With three unlocked achievements, that title is rendered as:

```text
[Deep Delver - Lv 3] PlayerName : Message
```

---

## Prerequisite achievements

Use `requires` to build an ordered achievement chain or tree:

```json
"requires": ["beginner_miner"]
```

Every listed ID must exist in the same authoring file. All listed achievements must be complete before the player may contribute items or money to this achievement.

### Linear chain

```text
beginner_miner -> apprentice_miner -> master_miner
```

```json
{
  "id": "apprentice_miner",
  "name": "Apprentice Miner",
  "title": "Apprentice Miner",
  "requires": ["beginner_miner"]
}
```

### Merging branches

An achievement can require several previous achievements:

```json
"requires": ["master_miner", "master_lumberjack", "master_farmer"]
```

This achievement remains `INACCESSIBLE` until all three branches are unlocked.

Circular requirements are invalid. For example, `a` cannot require `b` if `b` eventually requires `a`. The compiler detects and rejects these cycles.

---

## Money costs

Use `money` for GlymeraMerchant currency:

```json
"money": 250
```

- The value must be a whole, non-negative integer.
- `0` means that the achievement has no currency cost.
- The configured Glymera currency name and symbol do not affect the JSON; only the numeric amount belongs here.
- Currency is withdrawn through GlymeraMerchant when the player contributes.
- Currency contributions are persistent and can be partial.

By default, `/achievements contribute apprentice_miner` contributes as much of the remaining currency as the player can afford. A player can cap one payment:

```text
/achievements contribute apprentice_miner 50
```

This contributes at most 50 currency during that command while still contributing any needed items in the player's inventory.

GlymeraMerchant is optional for the mod as a whole. It becomes functionally required for any achievement with `money` greater than zero.

---

## Item costs

`items` is a JSON object mapping exact Hytale item IDs to positive integer quantities:

```json
"items": {
  "Rock_Stone": 64,
  "Ore_Copper": 32,
  "Ore_Iron": 16
}
```

Rules:

- The key must be the exact item ID, including capitalization and underscores.
- The quantity must be a positive whole number.
- Each item ID can appear only once in an object.
- Use `{}` or omit `items` when no items are required.

The repository's `ItemList.md` is the convenient item-ID reference. IDs used by OneBlock can also be found in `expeditions.json` and `OneBlockExpeditionDefaults.java`.

When a player uses `/achievements contribute <id>`, the mod checks the backpack, storage, and hotbar. It removes at most the still-required quantity of every configured item. Extra items remain in the inventory.

### Partial contribution example

Suppose the achievement requires:

```json
"items": {
  "Rock_Stone": 64
}
```

If the player has 32 stone, the first contribution produces progress of `32/64`. That progress is written to `players.json`. A later contribution of 20 changes it to `52/64`, and a final contribution of 12 completes the item cost.

Use `/achievements status beginner_miner` at any time to see the recorded amount and total requirement.

---

## Expedition-knowledge requirements

Use `expeditions` when the player must have learned one or more OneBlock expeditions:

```json
"expeditions": ["CopperCave", "IronCave", "GoldCave"]
```

These values are exact OneBlock expedition IDs, not achievement IDs and not crystal item IDs. For example:

- Use `CopperCave` for the expedition.
- Do not use `apprentice_miner`, which is an achievement ID.
- Do not use `OneBlock_Crystal_CopperCave`, which is the generated item ID.

The compiler verifies that the field is a duplicate-free string array, but it cannot verify expedition IDs against another repository file. Check `expeditions.json` carefully when authoring them.

Knowledge is not consumed. Once the mod records an expedition unlock for a player, any number of achievements can use that knowledge. All listed expeditions are required.

Knowledge is recorded when OneBlock grants the expedition recipe/crystal unlock. Unlocks obtained before the OneBlock Achievement mod was installed cannot currently be reconstructed automatically.

---

## Minimal examples

### Free root achievement

An achievement with no requirements or costs completes the next time completion is evaluated. In practice, add at least one meaningful condition unless it is intended as a free title.

```json
{
  "id": "founder",
  "name": "Server Founder",
  "title": "Founder"
}
```

### Item-only achievement

```json
{
  "id": "beginner_miner",
  "name": "Beginner Miner",
  "title": "Beginner Miner",
  "items": {
    "Rock_Stone": 64
  }
}
```

### Currency-only achievement

```json
{
  "id": "wealthy_trader",
  "name": "A Small Fortune",
  "title": "Wealthy Trader",
  "money": 10000
}
```

### Expedition-only achievement

```json
{
  "id": "copper_explorer",
  "name": "Discover Copper Cave",
  "title": "Copper Explorer",
  "expeditions": ["CopperCave"]
}
```

---

## Full progression example

```json
{
  "achievements": [
    {
      "id": "beginner_miner",
      "name": "Beginner Miner",
      "title": "Beginner Miner",
      "requires": [],
      "money": 0,
      "items": {
        "Rock_Stone": 64
      },
      "expeditions": []
    },
    {
      "id": "apprentice_miner",
      "name": "Apprentice Miner",
      "title": "Apprentice Miner",
      "requires": ["beginner_miner"],
      "money": 250,
      "items": {
        "Ore_Copper": 32,
        "Ore_Iron": 16
      },
      "expeditions": ["CopperCave"]
    },
    {
      "id": "master_miner",
      "name": "Master of the Deep",
      "title": "Master Miner",
      "requires": ["apprentice_miner"],
      "money": 2500,
      "items": {
        "Ore_Gold": 64,
        "Ore_Mithril": 16
      },
      "expeditions": ["IronCave", "GoldCave", "MithrilCave"]
    },
    {
      "id": "underground_cartographer",
      "name": "Underground Cartographer",
      "title": "Cave Cartographer",
      "requires": ["master_miner"],
      "money": 0,
      "items": {},
      "expeditions": [
        "Cave",
        "LowerCave",
        "CopperCave",
        "IronCave",
        "GoldCave",
        "MithrilCave"
      ]
    }
  ]
}
```

This creates one linear branch. The player must:

1. Deposit 64 stone to complete Beginner Miner.
2. Learn Copper Cave, deposit copper and iron, and pay 250 currency for Apprentice Miner.
3. Complete Apprentice Miner, learn the listed mining expeditions, deposit the ores, and pay 2,500 currency for Master Miner.
4. Complete Master Miner and learn every listed cave to unlock Cave Cartographer.

---

## What the compiler produces

The compact item object is expanded into a normalized list, and authoring names are mapped to explicit runtime fields.

Authoring input:

```json
{
  "id": "beginner_miner",
  "name": "Beginner Miner",
  "title": "Beginner Miner",
  "requires": [],
  "money": 0,
  "items": {
    "Rock_Stone": 64
  },
  "expeditions": []
}
```

Compiled runtime output:

```json
{
  "id": "beginner_miner",
  "name": "Beginner Miner",
  "title": "Beginner Miner",
  "prerequisites": [],
  "cost": {
    "currency": 0,
    "items": [
      {
        "id": "Rock_Stone",
        "quantity": 64
      }
    ],
    "expeditions": []
  }
}
```

The complete output wraps these entries in:

```json
{
  "schemaVersion": 1,
  "achievements": []
}
```

The runtime format is deliberately explicit and versioned. Authors should normally work only with the compact source format.

---

## Compiler validation

The compiler rejects:

- Invalid JSON syntax.
- A missing or non-array `achievements` field.
- Missing or invalid IDs.
- Duplicate achievement IDs.
- Empty names or titles.
- Multiline titles or titles longer than 64 characters.
- Negative or non-integer money costs.
- Invalid item objects or non-positive quantities.
- Non-string or duplicate prerequisite and expedition entries.
- Prerequisites that reference unknown achievement IDs.
- Direct or indirect prerequisite cycles.

The mod validates the runtime file again during startup and `/achievements reload`. If the data-directory file is invalid during startup, the mod refuses to start instead of silently discarding progress or loading a partial catalog.

---

## Development build workflow

Use this workflow when shipping new definitions inside a new mod jar:

1. Edit `achievements.authoring.json`.
2. Compile it into the module resources:

   ```powershell
   python tools/compile_achievements.py achievements.authoring.json mods/oneblock-achievement/src/main/resources/achievements.json
   ```

3. Build and test the module:

   ```powershell
   .\gradlew.bat :oneblock-achievement:test :oneblock-achievement:build
   ```

4. The resulting jar is:

   ```text
   mods/oneblock-achievement/build/libs/oneblock-achievement-1.0.0.jar
   ```

5. Install the new achievement jar and the matching updated OneBlock jar in the server's `mods` directory, then restart the server.

The bundled `achievements.json` is copied into the plugin data directory only when no data-directory definition exists. This protects server-specific edits. Consequently, updating the jar does not overwrite an existing live `achievements.json`.

---

## Live-server update workflow

Use this workflow to change definitions without rebuilding the jar:

1. Back up the achievement plugin's existing data-directory `achievements.json` and `players.json`.
2. Edit the authoring file on the server or an administration machine.
3. Compile directly to the plugin data directory:

   ```powershell
   python tools/compile_achievements.py achievements.authoring.json "<achievement-data-directory>\achievements.json"
   ```

4. In game, run:

   ```text
   /achievements reload
   ```

The server log prints the exact path from which definitions were loaded. Use that path instead of guessing it.

Reloading definitions does not delete `players.json`. Existing completion IDs and contributions remain stored. Keep the following consequences in mind:

- Removing an achievement definition hides its title card, but its historical unlock ID remains in player progress.
- Renaming an achievement ID creates a new logical achievement; progress under the old ID is not migrated.
- Reducing a cost can cause an already-funded achievement to complete when it is next evaluated.
- Increasing a cost preserves the amount already contributed, but the player must supply the new remainder.
- Changing a title updates how that achievement appears when selected because the active selection stores the achievement ID, not a copy of its title text.

---

## Player testing checklist

After installing or reloading a definition set, test at least one complete branch:

1. Run `/achievements list` and confirm the `INACCESSIBLE`, `CURRENTLY_UNLOCKING`, and `UNLOCKED` states.
2. Run `/achievements status <id>` and confirm every configured cost.
3. Contribute fewer items than required and confirm that the partial count survives reconnecting.
4. Contribute a capped money amount and confirm the Glymera balance and saved achievement amount both change correctly.
5. Unlock a required expedition and confirm its knowledge condition becomes met.
6. Complete the final cost and confirm the achievement level increases.
7. Open `/achievement`, click **Participate**, then click **Activate** after unlocking it.
8. Send a chat message and confirm `[Title - Lv X] AccountName : Message`.
9. Ask another player to confirm that the same prefix appears above the character's nameplate.
10. Restart the server and confirm that progress, completion, and the active title remain intact.

---

## Troubleshooting

### `Unknown achievement id`

The command uses the achievement's `id`, not its `name` or `title`. Recompile the source, reload the runtime file, and use the exact lowercase ID.

### `Complete its prerequisites first`

At least one ID in `requires` is not complete for that player. Use `/achievements status <id>` and inspect the prerequisite list.

### Currency does not contribute

Confirm that GlymeraMerchant is installed and enabled, that the player has a positive balance, and that the achievement has remaining `money` cost. Item-only achievements do not require GlymeraMerchant.

### Expedition knowledge remains missing

Check that the JSON uses the exact expedition ID from `expeditions.json`. Knowledge is recorded on a new OneBlock expedition unlock event; older unlocks from before this mod was installed are not backfilled.

### A changed bundled file does not appear on the server

The mod intentionally preserves the existing data-directory `achievements.json`. Compile to that live file and run `/achievements reload`, or remove it while the server is stopped if you deliberately want the jar's bundled default to be installed again. Back it up first.

### The achievement UI is empty

The `/achievement` page displays currently unlocking and unlocked achievements. If it is empty, every definition is inaccessible because its prerequisites are not yet unlocked; use `/achievements list` and `/achievements status <id>` to inspect the chain.
