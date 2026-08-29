package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.VoidWorldGenProvider;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.joml.Vector3d;
import org.joml.Vector3i;

final class OneBlockWorldInitializer
{
    private static final String START_BLOCK_ID = "OneBlock_Block_Default";
    private static final Vector3i ORIGIN_BLOCK = new Vector3i(0, 100, 0);
    private static final Vector3d SPAWN_POS = new Vector3d(0.5, 102.0, 0.5);
    private static final Color VOID_TINT = new Color((byte) 0x5a, (byte) 0x99, (byte) 0x2b);
    private static final String VOID_ENVIRONMENT = "Env_Default_Void";

    static VoidWorldGenProvider voidWorldGenProvider()
    {
        return new VoidWorldGenProvider(VOID_TINT, VOID_ENVIRONMENT);
    }

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Set<String> INITIALIZED_WORLDS = ConcurrentHashMap.newKeySet();

    private OneBlockWorldInitializer() {}

    static void initializeWorld(World world, String blockId)
    {
        if (world == null) return;

        String resolvedBlockId = (blockId != null && !blockId.isBlank()) ? blockId : START_BLOCK_ID;

        world.execute(() ->
        {
            WorldConfig config = world.getWorldConfig();
            if (config != null)
            {
                Transform spawn = new Transform(SPAWN_POS, new Rotation3f(0.0F, 0.0F, 0.0F));
                config.setSpawnProvider(new GlobalSpawnProvider(spawn));
                config.setWorldGenProvider(new VoidWorldGenProvider(VOID_TINT, VOID_ENVIRONMENT));
                config.markChanged();
            }

            if (INITIALIZED_WORLDS.add(world.getName()))
            {
                world.setBlock(ORIGIN_BLOCK.x(), ORIGIN_BLOCK.y(), ORIGIN_BLOCK.z(), resolvedBlockId);
                LOGGER.at(Level.INFO).log(
                        "Placed OneBlock in '" + world.getName() + "' at " + ORIGIN_BLOCK + " with block " + resolvedBlockId
                );
            }
        });
    }
}
