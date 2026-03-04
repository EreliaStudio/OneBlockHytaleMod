package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
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

    private static final Vector3i REWARD_OFFSET = new Vector3i(0, 1, 0);

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

        if (!isValidOneBlockBreak(player, event))
        {
            return;
        }

        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null)
        {
            return;
        }

        UUID playerId = playerRef.getUuid();

        String expeditionId = OneBlockExpeditionResolver.expeditionFromBlockType(event.getBlockType());
        List<String> unlockedDrops = stateProvider.getUnlockedDrops(playerId, expeditionId);
        String rewardId = dropRegistry.pickReward(expeditionId, unlockedDrops);
        if (rewardId == null || rewardId.isEmpty())
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

        replaceBlock(world, blockType, pos);
        spawnReward(store, world, rewardId, pos, REWARD_OFFSET);
    }

    private static boolean isValidOneBlockBreak(Player player, BreakBlockEvent event)
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

        return OneBlockBlockUtil.isOneBlock(event.getBlockType());
    }

    private static void spawnReward(Store<EntityStore> store,
                                    World world,
                                    String rewardId,
                                    Vector3i baseBlockPos,
                                    Vector3i offset)
    {
        if (store == null || world == null || rewardId == null || rewardId.isEmpty() || baseBlockPos == null || offset == null)
        {
            return;
        }

        OneBlockDropId dropId = OneBlockDropId.parse(rewardId);
        if (dropId.getId() == null || dropId.getId().isEmpty())
        {
            return;
        }

        int x = baseBlockPos.getX() + offset.getX();
        int y = baseBlockPos.getY() + offset.getY();
        int z = baseBlockPos.getZ() + offset.getZ();

        Vector3d dropPos = new Vector3d(x + 0.5, y + 0.1, z + 0.5);

        if (dropId.isEntity())
        {
            OneBlockEntitySpawner.spawnNpc(store, world, new Vector3i(x, y, z), dropId.getId());
            return;
        }

        world.execute(() ->
        {
            ItemStack stack = new ItemStack(dropId.getId(), 1);

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

    private static void replaceBlock(World world, BlockType blockType, Vector3i pos)
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
}
