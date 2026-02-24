package com.ereliastudio.oneblock;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

public final class OneBlockBreakSystem extends EntityEventSystem {
    private static final String ONEBLOCK_CATEGORY = "Blocks.OneBlock";

    public OneBlockBreakSystem() {
        super(BreakBlockEvent.class);
    }

    @Override
    public Query getQuery() {
        return Query.any();
    }

    @Override
    public void handle(int entityIndex,
                       ArchetypeChunk chunk,
                       Store store,
                       CommandBuffer commandBuffer,
                       BreakBlockEvent event) {
        BlockType blockType = event.getBlockType();
        if (!isOneBlock(blockType)) {
            player.sendMessage(Message.raw("No OneBlock broken!"));
            return;
        }

        Ref ref = chunk.getReferenceTo(entityIndex);
        Player player = (Player) store.getComponent(ref, Player.getComponentType());
        if (player != null) {
            player.sendMessage(Message.raw("OneBlock broken!"));
        }

        Object external = store.getExternalData();
        if (external instanceof EntityStore entityStore) {
            World world = entityStore.getWorld();
            Vector3i pos = event.getTargetBlock();
            if (world != null && pos != null) {
                String blockId = blockType.getId();
                if (blockId != null && !blockId.isEmpty()) {
                    world.setBlock(pos.getX(), pos.getY(), pos.getZ(), blockId);
                }
            }
        }
    }

    private static boolean isOneBlock(BlockType blockType) {
        if (blockType == null) {
            return false;
        }
        Item item = blockType.getItem();
        if (item == null) {
            return false;
        }
        String[] categories = item.getCategories();
        if (categories == null) {
            return false;
        }
        for (String category : categories) {
            if (ONEBLOCK_CATEGORY.equals(category)) {
                return true;
            }
        }
        return false;
    }
}
