package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

final class OneBlockWorldInitializer
{
    static final String START_BLOCK_ID = "OneBlock_Block_A1";
    static final Vector3i ORIGIN_BLOCK = new Vector3i(0, 100, 0);
    static final Vector3d SPAWN_POS = new Vector3d(0.0, 102.0, 0.0);

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final AtomicBoolean ORIGIN_PLACED = new AtomicBoolean(false);

    private OneBlockWorldInitializer()
    {
    }

    static boolean isDefaultWorld(World world)
    {
        return world != null && World.DEFAULT.equals(world.getName());
    }

    static void initializeWorld(World world)
    {
        if (world == null)
        {
            return;
        }

        world.execute(() ->
        {
            WorldConfig config = world.getWorldConfig();
            if (config != null)
            {
                Transform spawn = new Transform(SPAWN_POS, new Vector3f(0.0F, 0.0F, 0.0F));
                config.setSpawnProvider(new GlobalSpawnProvider(spawn));
                config.markChanged();
            }

            if (ORIGIN_PLACED.compareAndSet(false, true))
            {
                world.setBlock(
                        ORIGIN_BLOCK.getX(),
                        ORIGIN_BLOCK.getY(),
                        ORIGIN_BLOCK.getZ(),
                        START_BLOCK_ID
                );
                LOGGER.at(Level.INFO).log("Placed OneBlock at " + ORIGIN_BLOCK);
            }
        });
    }
}
