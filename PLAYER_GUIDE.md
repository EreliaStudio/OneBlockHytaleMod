# OneBlock — Player Guide

## 1. Game loop

You begin at the server's normal spawn. An administrator can create a separate
OneBlock world with `/oneblock create <worldName>` or move you into an existing
one with `/oneblock join <worldName>`. In that void world, every time you finish
breaking the OneBlock, it immediately returns and gives you a
random reward. It may drop building materials, plants, ores, equipment, or
other useful resources. Some breaks summon animals or hostile creatures
instead, so expand and secure your platform as soon as possible.

Your adventure starts in **Meadow**. Complete its progress bar to receive
Expedition Points and begin unlocking new expedition crystals. You can craft
the Crystal Enchanter from the Fieldcraft menu with 4 Fibre and 3 Rubble Stone.
The Enchanter uses your collected resources and Expedition Points to create
crystals for future destinations.

Using a crystal changes the OneBlock and begins its expedition. The mod
contains 88 regular expeditions across five difficulty groups—Easy, Advanced,
Difficult, Hard, and Expert. Each has its own visual theme, possible drops,
duration, required tool, and completion rewards. A crystal's description shows
what it may provide and which expeditions it can unlock next.

OneBlocks may require:

- **Hand:** anything can damage the block.
- **Pickaxe:** only a pickaxe can damage the block.
- **Axe:** only an axe or hatchet can damage the block.

Higher-level tools break OneBlocks faster than crude tools, but they also use
durability. If a block is not taking damage, try changing to a tool appropriate
for its theme: pickaxes for caves and stone, or axes for forests and wood.

The mod also includes 24 dungeons. Dungeon crystals start combat encounters in
which each completed OneBlock summons the next enemy wave. Prepare a wide,
protected platform and suitable equipment before starting one. Clear each wave
before breaking the block again, because the next wave can be summoned even if
enemies from the previous wave are still alive.

In multiplayer, everyone in the same OneBlock world shares its active
expedition, dungeon, and progress. Using a crystal changes the activity for the
whole group. Servers can host several independent OneBlock worlds at the same
time, allowing multiple parties to progress separately.

Your expedition and dungeon progress is saved between server restarts. Fall
protection is enabled by default and returns players to the island if they fall
too far into the void.

## 2. Commands

The `/oneblock` commands are primarily intended for server administrators.
They are used to create separate party worlds, move players between them, and
manage an expedition when necessary.

| Command | Description |
| --- | --- |
| `/oneblock create <worldName>` | Creates a OneBlock world and moves the targeted player into it. Use `-` to generate a name. |
| `/oneblock join <worldName>` | Moves the targeted player into an existing OneBlock world. |
| `/oneblock list` | Lists all registered OneBlock worlds. |
| `/oneblock status` | Shows the current expedition or dungeon progress. |
| `/oneblock start <expeditionId>` | Starts an expedition in the current world. |
| `/oneblock stop` | Stops the current activity and restores Meadow. |
| `/oneblock fallProtection true` | Enables protection from falling into the void. |
| `/oneblock fallProtection false` | Disables protection from falling into the void. |

World names may contain letters, numbers, `_`, and `-`. If no custom name is
needed, `/oneblock create -` generates one automatically. The `status`, `start`,
and `stop` commands affect the targeted player's current OneBlock world, not
every world on the server. Each OneBlock world contains exactly one generated
OneBlock; OneBlock block items cannot be placed manually to add more.

After entering a OneBlock world, regular players do not need further commands
for normal progression: breaking the starter OneBlock begins Meadow, and using
crafted crystals starts later expeditions naturally.

## 3. Join the community

Join the Discord server to follow development, report problems, suggest new
expeditions, ask for help, or meet other OneBlock players:

[Join the OneBlock Discord](https://discord.gg/2hs2333ph)

Feedback from your adventures is welcome, especially balancing suggestions,
multiplayer experiences, and ideas for future expeditions or dungeon content.
