# OneBlock Achievement mod

This third mod adds data-driven achievements, persistent partial contributions, selectable titles, chat levels, and overhead nameplates.

## Player commands

- `/achievement` opens the achievement card list. Currently unlocking achievements appear first with their requirements and a **Participate** button. Unlocked achievements follow with an **Activate** button. Both sections are alphabetical; inaccessible achievements are hidden.
- `/achievements list` lists all configured achievements as `INACCESSIBLE`, `CURRENTLY_UNLOCKING`, or `UNLOCKED`.
- `/achievements status <id>` shows exact progress for money, every item, prerequisites, and expedition knowledge.
- `/achievements contribute <id>` contributes as many still-needed items and as much still-needed Glymera currency as the player currently has.
- `/achievements contribute <id> <maximum-money>` does the same but caps this contribution's currency amount. This supports partial payments as well as partial item deposits.
- `/achievements title <id>` selects an unlocked title without opening the UI. `/achievements title none` clears it.
- `/achievements reload` reloads the server-side compiled configuration.

The unlocked achievement count is the player's level. Chat is formatted as `[Selected Title - Lv X] AccountName : Message`. With no selected title it is `[Lv X] AccountName : Message`. The same prefix appears on a separate line above the account name in the in-world nameplate.

## Authoring achievements

Edit `achievements.authoring.json`. Each entry supports:

```json
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
}
```

- `requires` contains achievement IDs that must already be complete before contributions are accepted.
- `money` is an integer amount taken through GlymeraMerchant 8's public economy API. It may be zero.
- `items` maps exact Hytale item IDs to positive quantities. Deposits are consumed and saved immediately, so a player can give 32 of a required 64 now and the remainder later.
- `expeditions` contains exact OneBlock expedition IDs. Knowledge is recorded when OneBlock grants the expedition recipe/crystal unlock after this mod is installed; knowledge is checked but not consumed.

### Translating achievements

Achievement names and titles are localized by ID. For every achievement, add these entries to each locale's
`mods/oneblock-achievement/src/main/resources/Server/Languages/<locale>/server.lang` file:

```properties
achievement.definition.<id>.name=Localized achievement name
achievement.definition.<id>.title=Localized unlockable title
```

For example, `beginner_miner` uses `achievement.definition.beginner_miner.name` and
`achievement.definition.beginner_miner.title`. The JSON `name` and `title` remain server-side fallbacks used for
validation, sorting, logs, and nameplates. Achievement cards and chat resolve the language keys on each player's client.
References sent to the client use the `server.` namespace (for example,
`server.achievement.definition.beginner_miner.name`). Hytale merges the selected locale over `en-US`, so a missing
locale or missing translated entry automatically falls back to the English value.

Compile and validate the friendly file with:

```powershell
python tools/compile_achievements.py achievements.authoring.json mods/oneblock-achievement/src/main/resources/achievements.json
```

The compiler removes and regenerates every `achievement.definition.<id>.name` and `.title` entry in
`Server/Languages/en-US/server.lang` from the authoring file. In other locale files, it preserves translations for
current IDs and removes entries for deleted IDs; missing localized entries fall back to `en-US`.

The compiled catalogue is embedded in the mod JAR and replaces the runtime catalogue whenever the plugin starts.
After changing definitions, rebuild, deploy, and restart the server; no `/achievements reload` step is required. The
compiler rejects duplicate IDs, missing prerequisites, invalid costs, and prerequisite cycles.

## Build and installation

Build the third mod with `./gradlew :oneblock-achievement:build`. Install the resulting `mods/oneblock-achievement/build/libs/OneBlockAchievement-<version>.jar` together with a compatible OneBlock jar. GlymeraMerchant is optional globally, but achievements with a non-zero money cost cannot be funded unless it is installed.

Player progress remains stored separately by UUID in the mod data directory's `players.json`.
