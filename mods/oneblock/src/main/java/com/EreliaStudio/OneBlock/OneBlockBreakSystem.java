package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;

import javax.annotation.Nonnull;
import java.util.List;

public final class OneBlockBreakSystem extends EntityEventSystem<EntityStore, BreakBlockEvent>
{
    private static final Vector3i REWARD_OFFSET = new Vector3i(0, 1, 0);

    private final OneBlockDropRegistry dropRegistry;
    private final OneBlockExpeditionStateProvider expeditionState;
    private final OneBlockDungeonStateProvider dungeonState;

    public OneBlockBreakSystem(OneBlockDropRegistry dropRegistry,
                               OneBlockExpeditionStateProvider expeditionState,
                               OneBlockDungeonStateProvider dungeonState)
    {
        super(BreakBlockEvent.class);
        this.dropRegistry = dropRegistry;
        this.expeditionState = expeditionState;
        this.dungeonState = dungeonState;
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

        if (!isValidOneBlockBreak(player, event)) return;

        EntityStore entityStore = store.getExternalData();
        if (entityStore == null) return;

        World world = entityStore.getWorld();
        if (world == null) return;

        Vector3i pos = event.getTargetBlock();

        DropableContext context = new DropableContext(
                store, world, pos,
                new Vector3i(pos.getX() + REWARD_OFFSET.getX(),
                             pos.getY() + REWARD_OFFSET.getY(),
                             pos.getZ() + REWARD_OFFSET.getZ()),
                ref,
                store.getComponent(ref, PlayerRef.getComponentType())
        );

        if (dungeonState.isDungeonActive())
        {
            handleDungeonBreak(world, pos, player, context);
        }
        else
        {
            handleExpeditionBreak(world, pos, player, event, context);
        }
    }

    private void handleDungeonBreak(World world, Vector3i pos, Player player, DropableContext context)
    {
        String dungeonId = dungeonState.getActiveDungeonId();
        int waveIndex = dungeonState.getCurrentWaveIndex();
        List<String> wave = OneBlockDungeonDefaults.getWave(dungeonId, waveIndex);

        for (String entityId : wave)
            dropRegistry.executeDropable(entityId, context);

        String completedDungeon = dungeonState.onWaveCompleted();

        if (completedDungeon != null)
        {
            world.execute(() -> world.setBlock(pos.getX(), pos.getY(), pos.getZ(), OneBlockBlockIds.DEFAULT_BLOCK_ID));
            executeDungeonCompletionRewards(completedDungeon, context);
            if (player != null)
                player.sendMessage(Message.raw("Dungeon complete: " + completedDungeon + ". The OneBlock has returned to default."));
        }
        else
        {
            String dungeonBlockId = OneBlockDungeonDefaults.getBlockId(dungeonId);
            if (dungeonBlockId == null) dungeonBlockId = OneBlockBlockIds.DEFAULT_BLOCK_ID;
            String finalBlockId = dungeonBlockId;
            world.execute(() -> world.setBlock(pos.getX(), pos.getY(), pos.getZ(), finalBlockId));
            int nextWave = dungeonState.getCurrentWaveIndex() + 1;
            int totalWaves = OneBlockDungeonDefaults.getWaveCount(dungeonId);
            if (player != null)
                player.sendMessage(Message.raw("Wave " + waveIndex + " spawned. " + nextWave + "/" + totalWaves + " waves completed."));
        }
    }

    private void handleExpeditionBreak(World world, Vector3i pos, Player player,
                                       BreakBlockEvent event, DropableContext context)
    {
        String poolId = OneBlockPools.resolvePoolId(event.getBlockType());
        List<String> drops = dropRegistry.getKnownDrops(poolId);
        String rewardId = dropRegistry.pickReward(poolId, drops);
        if (rewardId == null || rewardId.isEmpty()) return;

        String completedExpedition = expeditionState.onBreak();
        String nextBlockId = (completedExpedition != null)
                ? OneBlockBlockIds.DEFAULT_BLOCK_ID
                : event.getBlockType().getId();

        String finalBlockId = nextBlockId;
        world.execute(() -> world.setBlock(pos.getX(), pos.getY(), pos.getZ(), finalBlockId));

        dropRegistry.executeDropable(rewardId, context);

        if (completedExpedition != null && player != null)
        {
            executeExpeditionCompletionRewards(completedExpedition, context);
            player.sendMessage(Message.raw("Expedition complete: " + completedExpedition + ". The OneBlock has returned to default."));
        }
    }

    private void executeDungeonCompletionRewards(String dungeonId, DropableContext context)
    {
        List<OneBlockDungeonDefaults.CompletionRewardDefinition> rewards =
                OneBlockDungeonDefaults.getCompletionRewards(dungeonId);
        for (OneBlockDungeonDefaults.CompletionRewardDefinition reward : rewards)
        {
            if (reward == null || reward.dropId == null || reward.dropId.isEmpty()) continue;
            dropRegistry.executeDropable(reward.dropId, context, reward.quantity);
        }
    }

    private void executeExpeditionCompletionRewards(String expeditionId, DropableContext context)
    {
        List<OneBlockExpeditionDefaults.CompletionRewardDefinition> rewards =
                OneBlockExpeditionDefaults.getCompletionRewards(expeditionId);
        if (rewards.isEmpty()) return;

        for (OneBlockExpeditionDefaults.CompletionRewardDefinition reward : rewards)
        {
            if (reward == null || reward.dropId == null || reward.dropId.isEmpty()) continue;
            dropRegistry.executeDropable(reward.dropId, context, reward.quantity);
        }
    }

    private static boolean isValidOneBlockBreak(Player player, BreakBlockEvent event)
    {
        if (player == null || event == null) return false;
        Object gameMode = player.getGameMode();
        if (gameMode != null && "Creative".equalsIgnoreCase(gameMode.toString())) return false;
        return OneBlockBlockUtil.isOneBlock(event.getBlockType());
    }
}
