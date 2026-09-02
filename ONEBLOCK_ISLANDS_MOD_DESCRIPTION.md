# OneBlock Islands

OneBlock Islands is the server companion for the **OneBlock** mod. It provides the classic sky-island experience: every island lives in its own persistent void world, begins with a shared OneBlock, and is protected by ownership and membership rules.

The core **OneBlock** mod is required.

## What it adds

- Automatic creation of a personal **Home** island.
- Additional named islands for players who want more than one adventure.
- One persistent void world per island, without altering the server's normal spawn world.
- A OneBlock placed at the center of each new island, ready for its owner and members to use.
- Invitations, island membership, shared building access, and protection against unauthorized changes.
- Island-specific fall protection that returns players who fall into the void.
- Persistent ownership and membership based on player UUIDs.
- Staff tools for inspecting and managing islands.

## How it works

Use `/island` when you are ready to play. The first use creates your Home island; later uses return you to it. The island's owner and accepted members can build, mine, and use its shared OneBlock. Visitors who are not members cannot edit the island.

Everyone on an island shares the owner's current expedition, dungeon, and progress. If one member activates a crystal, the activity changes for the group. If another member breaks the OneBlock, the same shared progress advances. Island data and OneBlock progress survive restarts.

Each island is isolated in its own void world. The companion manages world creation and entry while leaving the server's configured default world untouched, making it suitable for a lobby or ordinary spawn area.

## Player commands

| Command | Purpose |
|---|---|
| `/island` | Create your Home island if necessary, or enter it. |
| `/island home` | Enter your Home island, creating it if necessary. |
| `/island create <name>` | Create and enter an additional named island. |
| `/island join <owner> [island]` | Enter an island you own or belong to. The owner must be online. |
| `/island members` | List the owner and members of your current island. |
| `/island invite <player>` | Invite an online player to your current island. |
| `/island accept` | Accept your pending island invitation and enter the island. |
| `/island kick <player>` | Remove an online member from an island you own. |
| `/island leave` | Leave the island you are currently visiting. Owners cannot leave their own island. |

Invitations and membership apply to the island being managed. Only the owner can invite or remove members.

## Installation and requirements

Install **OneBlock** first, then add **OneBlock Islands** to the server's mods directory and restart the server. No manual void-world setup is required. The server's existing default world remains the entry and fallback world.

This companion focuses on island lifecycle, travel, permissions, and safety. Expedition content, crystals, loot, combat dungeons, Roots, and progression all come from the required core **OneBlock** mod.

## Community

For help, bug reports, suggestions, and development news, join the community on [Discord](https://discord.gg/2hs2333ph).
