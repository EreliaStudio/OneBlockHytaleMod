package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3d;
import org.joml.Vector3i;
import java.util.logging.Level;

public final class OneBlockEntitySpawner
{
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private OneBlockEntitySpawner() {}

    public static boolean spawnNpc(Store<EntityStore> store, World world, Vector3i basePos, String entityId)
    {
        if (store == null || world == null || basePos == null || entityId == null || entityId.isEmpty()) return false;
        Vector3d spawnPosition = new Vector3d(basePos.x() + 0.5, basePos.y() + 1.5, basePos.z() + 0.5);

        world.execute(() ->
        {
            SpawnTestResult result = NPCPlugin.get().spawnNPCWithSpaceValidation(
                    store,
                    entityId,
                    null,
                    spawnPosition,
                    Rotation3f.IDENTITY
            );
            if (result != SpawnTestResult.TEST_OK)
            {
                LOGGER.at(Level.WARNING).log("Failed to spawn NPC '" + entityId + "': " + result);
            }
        });
        return true;
    }
}
