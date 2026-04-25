package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class ItemDropable implements Dropable
{
    private static final String ITEM_PREFIX = "item:";

    private final String id;
    private final String itemId;

    public ItemDropable(String dropableId)
    {
        this.id = dropableId;
        this.itemId = normalize(dropableId);
    }

    @Override
    public String getId() { return id; }

    @Override
    public void execute(DropableContext context)
    {
        if (context == null || itemId == null || itemId.isEmpty()) return;

        Store<EntityStore> store = context.getStore();
        World world = context.getWorld();
        Vector3i spawnBlock = context.getSpawnBlock();
        if (store == null || world == null || spawnBlock == null) return;

        Vector3d dropPos = new Vector3d(spawnBlock.getX() + 0.5, spawnBlock.getY() + 0.1, spawnBlock.getZ() + 0.5);

        world.execute(() ->
        {
            var drop = ItemComponent.generateItemDrop(store, new ItemStack(itemId, 1), dropPos, Vector3f.ZERO, 0.0F, 3.25F, 0.0F);
            if (drop != null) store.addEntity(drop, AddReason.SPAWN);
        });
    }

    private static String normalize(String dropableId)
    {
        if (dropableId == null) return null;
        String trimmed = dropableId.trim();
        if (trimmed.toLowerCase().startsWith(ITEM_PREFIX)) return trimmed.substring(ITEM_PREFIX.length()).trim();
        return trimmed;
    }
}
