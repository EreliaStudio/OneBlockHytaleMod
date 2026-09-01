# OneBlock — Player Guide

## 1. Game loop

You begin at the server's normal spawn. Use `/island` to create or return to
your personal island, or `/island join` to visit an island you belong to. The
islands mod creates a separate void world with one shared OneBlock. Every time you finish
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

In multiplayer, an island owner and authorized members share its active
expedition, dungeon, and progress. Using a crystal changes the activity for the
whole group. Independent Roots in ordinary worlds retain their own owner-scoped
progress.

Your expedition and dungeon progress is saved between server restarts. Fall
protection is enabled by default and returns players to the island if they fall
too far into the void.

### Shared-world Roots

Players can also run several independent OneBlock games inside an ordinary
shared world. Craft a **OneBlock Root Workbench** from the Fieldcraft menu, then
use it to craft a **OneBlock Root** and a **Root Extractor**.

The player who places a Root owns its saved expedition. Any player may mine the
Root: if Alice mines Bob's Root, Bob's expedition advances and both Alice and
Bob see its current progress. A player may place several Roots in the same
world; every Root they own shares one expedition and changes phase together.
Different owners remain independent even when their Roots are close together.

Use an expedition crystal anywhere in the world to change the shared activity
of every Root you own; it does not matter which direction or block you are
looking at. Use the Root Extractor to remove a Root without producing a reward
or advancing the expedition. Removing the last Root does not erase its owner's
saved progress, so a replacement Root can continue the same game later.

## 2. Commands

The `/oneblock` commands are primarily intended for server administrators to
inspect or control a registered OneBlock. Island creation and travel use the
`/island` command tree.

| Command | Description |
| --- | --- |
| `/island` | Creates your Home island if needed, then enters it. |
| `/island create <name>` | Creates an additional island. |
| `/island join <owner> [island]` | Enters an island you belong to. |
| `/oneblock status` | Shows the targeted player's accessible OneBlock progress in the current world. |
| `/oneblock start <expeditionId>` | Starts an expedition on that OneBlock. |
| `/oneblock stop` | Stops its current activity and restores Meadow. |

The `status`, `start`, and `stop` commands affect only the OneBlock context
available to the targeted player in the current world. Expedition block items
cannot be placed directly; use a crafted OneBlock Root when adding a
player-owned game to an ordinary world.

The progress bar appears only while you are in a world containing a OneBlock
available to you. It is cleared when you return to a spawn world without one.

After entering a OneBlock world, regular players do not need further commands
for normal progression: breaking the starter OneBlock begins Meadow, and using
crafted crystals starts later expeditions naturally.

## 3. Join the community

Join the Discord server to follow development, report problems, suggest new
expeditions, ask for help, or meet other OneBlock players:

[Join the OneBlock Discord](https://discord.gg/2hs2333ph)

Feedback from your adventures is welcome, especially balancing suggestions,
multiplayer experiences, and ideas for future expeditions or dungeon content.
