package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

/** Registers crafted roots while preventing direct placement of expedition blocks. */
public final class OneBlockPlacementSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent>
{
    private final OneBlockRootRegistry rootRegistry;

    public OneBlockPlacementSystem(OneBlockRootRegistry rootRegistry)
    {
        super(PlaceBlockEvent.class);
        this.rootRegistry = rootRegistry;
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
        if (ItemStack.isEmpty(itemStack)) return;

        if (OneBlockBlockIds.ROOT_ITEM_ID.equals(itemStack.getItemId()))
        {
            registerRootAfterPlacement(entityIndex, chunk, store, event);
            return;
        }

        if (OneBlockBlockUtil.isOneBlock(itemStack.getItem()))
        {
            event.setCancelled(true);

            Ref<EntityStore> entityRef = chunk.getReferenceTo(entityIndex);
            PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
            if (playerRef != null)
            {
                playerRef.sendMessage(Message.raw(
                        "Expedition blocks cannot be placed directly. Craft a OneBlock Root instead."
                ));
            }
        }
    }

    private void registerRootAfterPlacement(int entityIndex,
                                            ArchetypeChunk<EntityStore> chunk,
                                            Store<EntityStore> store,
                                            PlaceBlockEvent event)
    {
        Ref<EntityStore> entityRef = chunk.getReferenceTo(entityIndex);
        PlayerRef playerRef = store.getComponent(entityRef, PlayerRef.getComponentType());
        EntityStore entityStore = store.getExternalData();
        World world = entityStore == null ? null : entityStore.getWorld();
        Vector3i target = event.getTargetBlock();

        if (playerRef == null || world == null || target == null)
        {
            event.setCancelled(true);
            return;
        }

        event.setConsumeItem(true);
        Vector3i position = new Vector3i(target);

        // Placement events run before the native block write. Queueing the
        // registration lets us verify that the placement actually succeeded.
        world.execute(() ->
        {
            BlockType placed = world.getBlockType(position);
            if (placed == null || !OneBlockBlockIds.ROOT_ITEM_ID.equals(placed.getId())) return;

            rootRegistry.register(
                    world,
                    position,
                    playerRef.getUuid(),
                    playerRef.getUsername()
            );

            String activeBlockId = activeBlockId(world, playerRef.getUuid());
            world.setBlock(position.x(), position.y(), position.z(), activeBlockId);
            playerRef.sendMessage(Message.raw(
                    "OneBlock Root placed. Its expedition belongs to " + playerRef.getUsername() + "."
            ));
        });
    }

    private String activeBlockId(World world, java.util.UUID ownerId)
    {
        OneBlockDungeonStateProvider dungeon = rootRegistry.dungeonState(world, ownerId);
        if (dungeon.isDungeonActive())
        {
            String blockId = OneBlockDungeonDefaults.getBlockId(dungeon.getActiveDungeonId());
            return blockId == null ? OneBlockBlockIds.DEFAULT_BLOCK_ID : blockId;
        }

        OneBlockExpeditionStateProvider expedition = rootRegistry.expeditionState(world, ownerId);
        return expedition.hasActiveExpedition()
                ? OneBlockExpeditionResolver.blockIdForExpedition(expedition.getActiveExpeditionId())
                : OneBlockBlockIds.DEFAULT_BLOCK_ID;
    }
}
