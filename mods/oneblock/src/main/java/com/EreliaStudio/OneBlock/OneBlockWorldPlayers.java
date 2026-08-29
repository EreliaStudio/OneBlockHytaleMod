package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.function.Consumer;

final class OneBlockWorldPlayers
{
    private OneBlockWorldPlayers()
    {
    }

    static void forEach(World world, Consumer<Player> action)
    {
        if (world == null || action == null)
        {
            return;
        }

        for (PlayerRef playerRef : world.getPlayerRefs())
        {
            Player player = playerRef.getComponent(Player.getComponentType());
            if (player != null)
            {
                action.accept(player);
            }
        }
    }
}
