package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class EntitySpawnDropable implements Dropable
{
    private final String id;
    private final String entityId;

    public EntitySpawnDropable(String dropableId)
    {
        this.id = dropableId;
        OneBlockDropId parsed = OneBlockDropId.parse(dropableId);
        this.entityId = parsed.isEntity() ? parsed.getId() : null;
    }

    @Override
    public String getId() { return id; }

    @Override
    public void execute(DropableContext context)
    {
        if (context == null || entityId == null || entityId.isEmpty()) return;

        Store<EntityStore> store = context.getStore();
        World world = context.getWorld();
        Vector3i spawnBlock = context.getSpawnBlock();
        if (store == null || world == null || spawnBlock == null) return;

        OneBlockEntitySpawner.spawnNpc(store, world, spawnBlock, entityId);
    }
}
