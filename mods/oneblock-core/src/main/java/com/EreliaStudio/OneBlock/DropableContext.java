package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

public final class DropableContext
{
    private final Store<EntityStore> store;
    private final World world;
    private final Vector3i sourceBlock;
    private final Vector3i spawnBlock;
    private final Ref<EntityStore> playerEntity;
    private final PlayerRef playerRef;

    public DropableContext(Store<EntityStore> store,
                           World world,
                           Vector3i sourceBlock,
                           Vector3i spawnBlock,
                           Ref<EntityStore> playerEntity,
                           PlayerRef playerRef)
    {
        this.store = store;
        this.world = world;
        this.sourceBlock = sourceBlock;
        this.spawnBlock = spawnBlock;
        this.playerEntity = playerEntity;
        this.playerRef = playerRef;
    }

    public Store<EntityStore> getStore() { return store; }
    public World getWorld() { return world; }
    public Vector3i getSourceBlock() { return sourceBlock; }
    public Vector3i getSpawnBlock() { return spawnBlock; }
    public Ref<EntityStore> getPlayerEntity() { return playerEntity; }
    public PlayerRef getPlayerRef() { return playerRef; }

    public UUID getPlayerId()
    {
        return playerRef == null ? null : playerRef.getUuid();
    }
}
