package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;

public class OneBlockBreakSystem extends EntityEventSystem<EntityStore, BreakBlockEvent>
{
    private final OneBlockDropRegistry dropRegistry;
    private final OneBlockDropsStateProvider stateProvider;

    private static final String ONEBLOCK_CATEGORY = "Blocks.OneBlock";

    public OneBlockBreakSystem(OneBlockDropRegistry dropRegistry, OneBlockDropsStateProvider stateProvider)
    {
        super(BreakBlockEvent.class);
        this.dropRegistry = dropRegistry;
        this.stateProvider = stateProvider;
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
                       @Nonnull BreakBlockEvent event)
    {
        Ref<EntityStore> ref = chunk.getReferenceTo(entityIndex);
        Player player = store.getComponent(ref, Player.getComponentType());

        if (!IsPlayerReferenceValid(player, event))
        {
            return;
        }

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null)
        {
            return;
        }

        UUID playerId = playerRef.getUuid();

        String chapterId = OneBlockChapterResolver.chapterFromBlockType(event.getBlockType());
        List<String> enabledDrops = stateProvider.getEnabledDrops(playerId, chapterId);
        String rewardItemId = dropRegistry.pickReward(enabledDrops);
        if (rewardItemId == null || rewardItemId.isEmpty())
        {
            return;
        }

        EntityStore entityStore = store.getExternalData();
        if (entityStore == null)
        {
            return;
        }

        World world = entityStore.getWorld();
        if (world == null)
        {
            return;
        }

        BlockType blockType = event.getBlockType();
        Vector3i pos = event.getTargetBlock();

        SpawnBlockByID(world, blockType, pos);
        SpawnItemByID(store, world, rewardItemId, pos, new Vector3i(0, 1, 0));
    }

    private static boolean IsPlayerReferenceValid(Player player, BreakBlockEvent event)
    {
        if (player == null || event == null)
        {
            return false;
        }

        Object gameMode = player.getGameMode();
        if (gameMode != null)
        {
            String gm = gameMode.toString();
            if ("Creative".equalsIgnoreCase(gm))
            {
                return false;
            }
        }

        BlockType blockType = event.getBlockType();
        return isOneBlock(blockType);
    }

    private static void SpawnItemByID(Store<EntityStore> store,
                                      World world,
                                      String itemId,
                                      Vector3i baseBlockPos,
                                      Vector3i offset)
    {
        if (store == null || world == null || itemId == null || itemId.isEmpty() || baseBlockPos == null || offset == null)
        {
            return;
        }

        int x = baseBlockPos.getX() + offset.getX();
        int y = baseBlockPos.getY() + offset.getY();
        int z = baseBlockPos.getZ() + offset.getZ();

        Vector3d dropPos = new Vector3d(x + 0.5, y + 0.1, z + 0.5);

        world.execute(() ->
        {
            ItemStack stack = new ItemStack(itemId, 1);

            var drop = ItemComponent.generateItemDrop(
                    store,
                    stack,
                    dropPos,
                    Vector3f.ZERO,
                    0.0F,
                    3.25F,
                    0.0F
            );

            if (drop != null)
            {
                store.addEntity(drop, AddReason.SPAWN);
            }
        });
    }

    private static void SpawnBlockByID(World world, BlockType blockType, Vector3i pos)
    {
        if (world == null || blockType == null || pos == null)
        {
            return;
        }

        final String blockTypeId = blockType.getId();
        if (blockTypeId == null || blockTypeId.isEmpty())
        {
            return;
        }

        world.execute(() -> world.setBlock(pos.getX(), pos.getY(), pos.getZ(), blockTypeId));
    }

    private static boolean isOneBlock(BlockType blockType)
    {
        if (blockType == null)
        {
            return false;
        }

        Item item = blockType.getItem();
        if (item == null)
        {
            return false;
        }

        String[] categories = item.getCategories();
        if (categories == null)
        {
            return false;
        }

        for (String category : categories)
        {
            if (ONEBLOCK_CATEGORY.equals(category))
            {
                return true;
            }
        }

        return false;
    }
}
