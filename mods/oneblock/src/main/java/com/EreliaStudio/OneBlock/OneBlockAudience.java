package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/** Selects the players who should see progress for a particular OneBlock game. */
final class OneBlockAudience
{
    private OneBlockAudience()
    {
    }

    static void forTarget(World world,
                          Player actor,
                          OneBlockRootRegistry.RootEntry root,
                          Consumer<Player> action)
    {
        if (world == null || action == null) return;

        if (root == null)
        {
            OneBlockWorldPlayers.forEach(world, action);
            return;
        }

        Set<UUID> notified = new HashSet<>();
        notifyPlayer(actor, action, notified);

        for (PlayerRef playerRef : world.getPlayerRefs())
        {
            if (!root.ownerId().equals(playerRef.getUuid())) continue;
            notifyPlayer(playerRef.getComponent(Player.getComponentType()), action, notified);
        }
    }

    private static void notifyPlayer(Player player,
                                     Consumer<Player> action,
                                     Set<UUID> notified)
    {
        if (player == null || player.getPlayerRef() == null) return;
        if (notified.add(player.getPlayerRef().getUuid())) action.accept(player);
    }
}
