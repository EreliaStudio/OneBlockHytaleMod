package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.universe.world.World;

import java.util.UUID;

/** Decides whether a player may operate a registered OneBlock. */
@FunctionalInterface
public interface OneBlockAccessResolver {
    boolean mayUse(World world, UUID playerId, OneBlockRootRegistry.RootEntry root);
}
