package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.universe.world.World;

import java.util.UUID;

/** Maps a player to the owner of the OneBlock context they use in a world. */
@FunctionalInterface
public interface OneBlockOwnerResolver {
    UUID resolveOwner(World world, UUID playerId);
}
