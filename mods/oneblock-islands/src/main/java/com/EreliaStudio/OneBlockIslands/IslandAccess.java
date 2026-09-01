package com.EreliaStudio.OneBlockIslands;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

final class IslandAccess {
    static final String ADMIN_PERMISSION = "oneblockislands.admin";
    static final String BYPASS_PERMISSION = "oneblockislands.protection.bypass";

    private IslandAccess() {}

    static boolean mayEdit(IslandStore store, World world, PlayerRef player) {
        if (world == null || player == null) return false;
        if (player.hasPermission(BYPASS_PERMISSION) || player.hasPermission(ADMIN_PERMISSION)) return true;
        return store.findByWorld(world.getName()).map(i -> i.canEdit(player.getUuid()))
                .orElse(true);
    }

    static boolean mayEnter(IslandStore store, World world, PlayerRef player) {
        if (world == null || player == null) return false;
        if (player.hasPermission(BYPASS_PERMISSION) || player.hasPermission(ADMIN_PERMISSION)) return true;
        return store.findByWorld(world.getName()).map(i -> i.canEnter(player.getUuid()))
                .orElse(true);
    }
}
