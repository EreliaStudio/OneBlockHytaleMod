package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

/** Prevents player-placed duplicates; OneBlocks are owned by their world. */
public final class OneBlockPlacementSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent>
{
    public OneBlockPlacementSystem()
    {
        super(PlaceBlockEvent.class);
    }

    @Override
    public Query<EntityStore> getQuery()
    {
        return Query.any();
    }

    @Override
    public void handle(int entityIndex,
                       ArchetypeChunk<EntityStore> chunk,
                       Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull PlaceBlockEvent event)
    {
        ItemStack itemStack = event.getItemInHand();
        if (!ItemStack.isEmpty(itemStack) && OneBlockBlockUtil.isOneBlock(itemStack.getItem()))
        {
            event.setCancelled(true);

            Ref<EntityStore> entityRef = chunk.getReferenceTo(entityIndex);
            PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
            if (playerRef != null)
            {
                playerRef.sendMessage(Message.raw(
                        "Each world has one generated OneBlock. Use /oneblock create <worldName> for another."
                ));
            }
        }
    }
}
