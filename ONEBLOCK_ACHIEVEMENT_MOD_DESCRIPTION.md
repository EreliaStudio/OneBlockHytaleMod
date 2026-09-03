# OneBlock Achievement

OneBlock Achievement adds long-term, data-driven achievements to the server. Players contribute resources, pay currency, discover expeditions, and unlock prerequisite achievements to earn titles they can display in chat and above their character.

The core **OneBlock** mod is required. **GlymeraMerchant** is optional, but it is required for achievements that have a currency cost.

## What it adds

- Configurable achievements defined in a human-readable JSON file.
- Achievement chains, allowing one title to require earlier achievements.
- Item costs using exact Hytale item IDs and quantities.
- Currency costs paid through the GlymeraMerchant economy.
- OneBlock expedition-knowledge requirements.
- Persistent partial contributions: players can deposit part of a cost now and finish it later.
- A custom achievement page with resource cards, participation buttons, and title activation buttons.
- Selectable titles that players can change whenever they want.
- A player level equal to the total number of achievements they have unlocked.
- Custom chat formatting and native in-world nameplates.
- UUID-based progress that survives reconnects and server restarts.

## How it works

Every achievement has an internal ID, a player-facing name, an unlockable title, optional prerequisite achievements, and any combination of item, currency, and expedition-knowledge costs.

Every achievement has exactly one state:

- `INACCESSIBLE`: at least one prerequisite achievement is not unlocked. It is omitted from the player menu.
- `CURRENTLY_UNLOCKING`: every prerequisite is unlocked, so the player can see its costs and participate.
- `UNLOCKED`: every cost and condition is satisfied, so its title can be activated.

Prerequisites control the progression order. A player cannot contribute toward an inaccessible achievement. This makes it possible to build paths such as **Beginner Miner**, **Apprentice Miner**, and **Master Miner**.

Once an achievement is currently unlocking, `/achievements contribute <id>` takes only the resources that are still needed. Contributions are saved immediately. If Beginner Miner needs 64 stone and the player contributes 32, the achievement permanently remembers those 32; the player only needs to provide the remaining 32 later.

Currency is taken through GlymeraMerchant's public economy API. Item deposits and currency payments can both be partial. Expedition knowledge is a condition rather than a consumable cost: once OneBlock reports that a player has learned an expedition, that knowledge remains available for every achievement that requires it.

When every condition is met, the achievement becomes unlocked automatically and its title appears in the selector.

## Achievement page and titles

Use `/achievement` to open the custom achievement page. The first section contains all currently unlocking achievements in alphabetical order. Each card shows its item, Glymera, and expedition requirements with saved progress and a **Participate** button. The second section contains all unlocked achievements in alphabetical order, each with an **Activate** button. Inaccessible achievements are hidden.

Clicking **Activate** selects that achievement's title. The active title can also be removed from the page. Unlocking an achievement does not force the player to use its title, and any previously unlocked title can be selected again later.

The player's level is the number of achievements they have unlocked. A player with the Beginner Miner title and three unlocked achievements appears in chat as:

```text
[Beginner Miner - Lv 3] PlayerName : Message
```

The same prefix is displayed above the player's account name in the game world. If no title is selected, the level is still shown as `[Lv 3]`.

## Player commands

| Command | Purpose |
|---|---|
| `/achievement` | Open the clickable achievement and title selector. |
| `/achievements list` | List every achievement as inaccessible, currently unlocking, or unlocked. |
| `/achievements status <id>` | Show the exact progress and remaining conditions for an achievement. |
| `/achievements contribute <id>` | Contribute every currently held item that is still needed and as much required currency as possible. |
| `/achievements contribute <id> <maximum-money>` | Contribute items while limiting this payment to the specified currency amount. |
| `/achievements title <id>` | Activate the title from an unlocked achievement. |
| `/achievements title none` | Remove the active title while keeping the player's level. |
| `/achievements reload` | Reload the compiled achievement definitions from the plugin data directory. |

## Configuration and creation tools

Server owners write achievements in `achievements.authoring.json`. The provided `tools/compile_achievements.py` utility validates that file and converts it into the normalized `achievements.json` format consumed by the mod.

The compiler catches duplicate IDs, invalid quantities, missing prerequisites, malformed titles, and prerequisite cycles before the configuration reaches the server. See `tools/ACHIEVEMENTS_JSON_GUIDE.md` for the complete schema, examples, live-server workflow, and build instructions.

## Installation and requirements

Install the matching **OneBlock** jar and **OneBlock Achievement** jar in the server's mods directory. Add **GlymeraMerchant 8.0.0 or newer** when at least one configured achievement has a non-zero money cost.

The mod creates its editable `achievements.json` and persistent `players.json` files in its plugin data directory. Updating or reloading definitions does not erase unlocked achievements or partial contributions.

Because expedition knowledge is recorded through OneBlock progression events, knowledge acquired before this mod was installed is not automatically reconstructed. Players can satisfy those conditions by receiving the corresponding expedition unlock again.

## Compatibility

OneBlock Achievement depends on the OneBlock progression integration included in the matching core jar. It works with or without OneBlock Islands and tracks each player by account UUID, independently of island ownership.

GlymeraMerchant remains optional for servers that only use item, prerequisite, and expedition requirements. If GlymeraMerchant is absent, players can still contribute items, but they cannot complete an achievement whose remaining currency cost is greater than zero.

## Community

For help, bug reports, achievement ideas, and development news, join the community on [Discord](https://discord.gg/2hs2333ph).
