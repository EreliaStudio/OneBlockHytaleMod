package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.interactions.SpawnNPCInteraction;
import com.hypixel.hytale.logger.HytaleLogger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

public final class OneBlockEntitySpawner
{
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final Vector3d DEFAULT_SPAWN_OFFSET = new Vector3d(0, 0.5, 0);

    private static volatile boolean initialized = false;
    private static Field fieldEntityId;
    private static Field fieldSpawnOffset;
    private static Field fieldSpawnChance;
    private static Field fieldSpawnYawOffset;
    private static Field fieldWeightedSpawns;
    private static Field fieldWeightedSpawnMap;
    private static Method methodSpawnNpc;

    private OneBlockEntitySpawner()
    {
    }

    public static boolean spawnNpc(Store<EntityStore> store, World world, Vector3i basePos, String entityId)
    {
        if (store == null || world == null || basePos == null || entityId == null || entityId.isEmpty())
        {
            return false;
        }

        if (!ensureInitialized())
        {
            return false;
        }

        SpawnNPCInteraction interaction = new SpawnNPCInteraction();
        if (!configureInteraction(interaction, entityId))
        {
            return false;
        }

        world.execute(() ->
        {
            try
            {
                methodSpawnNpc.invoke(interaction, store, basePos);
            }
            catch (Exception e)
            {
                LOGGER.at(Level.SEVERE).log("Failed to spawn NPC '" + entityId + "': " + e.getMessage());
            }
        });
        return true;
    }

    private static boolean ensureInitialized()
    {
        if (initialized)
        {
            return fieldEntityId != null && methodSpawnNpc != null;
        }

        synchronized (OneBlockEntitySpawner.class)
        {
            if (initialized)
            {
                return fieldEntityId != null && methodSpawnNpc != null;
            }

            try
            {
                fieldEntityId = SpawnNPCInteraction.class.getDeclaredField("entityId");
                fieldSpawnOffset = SpawnNPCInteraction.class.getDeclaredField("spawnOffset");
                fieldSpawnChance = SpawnNPCInteraction.class.getDeclaredField("spawnChance");
                fieldSpawnYawOffset = SpawnNPCInteraction.class.getDeclaredField("spawnYawOffset");
                fieldWeightedSpawns = SpawnNPCInteraction.class.getDeclaredField("weightedSpawns");
                fieldWeightedSpawnMap = SpawnNPCInteraction.class.getDeclaredField("weightedSpawnMap");
                methodSpawnNpc = SpawnNPCInteraction.class.getDeclaredMethod("spawnNPC", Store.class, Vector3i.class);

                fieldEntityId.setAccessible(true);
                fieldSpawnOffset.setAccessible(true);
                fieldSpawnChance.setAccessible(true);
                fieldSpawnYawOffset.setAccessible(true);
                fieldWeightedSpawns.setAccessible(true);
                fieldWeightedSpawnMap.setAccessible(true);
                methodSpawnNpc.setAccessible(true);
            }
            catch (Exception e)
            {
                LOGGER.at(Level.SEVERE).log("Failed to init SpawnNPCInteraction reflection: " + e.getMessage());
            }
            finally
            {
                initialized = true;
            }
        }

        return fieldEntityId != null && methodSpawnNpc != null;
    }

    private static boolean configureInteraction(SpawnNPCInteraction interaction, String entityId)
    {
        try
        {
            fieldEntityId.set(interaction, entityId);
            fieldSpawnOffset.set(interaction, DEFAULT_SPAWN_OFFSET);
            fieldSpawnChance.set(interaction, 1.0f);
            fieldSpawnYawOffset.set(interaction, 0.0f);
            fieldWeightedSpawns.set(interaction, null);
            fieldWeightedSpawnMap.set(interaction, null);
            return true;
        }
        catch (Exception e)
        {
            LOGGER.at(Level.SEVERE).log("Failed to configure SpawnNPCInteraction: " + e.getMessage());
            return false;
        }
    }
}
