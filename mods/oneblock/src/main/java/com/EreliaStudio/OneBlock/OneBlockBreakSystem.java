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

    public OneBlockBreakSystem(OneBlockDropRegistry dropRegistry, OneBlockExpeditionStateProvider expeditionState)
    {
        super(BreakBlockEvent.class);
        this.dropRegistry = dropRegistry;
        this.expeditionState = expeditionState;
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

        String poolId = OneBlockPools.resolvePoolId(event.getBlockType());
        List<String> drops = dropRegistry.getKnownDrops(poolId);
        String rewardId = dropRegistry.pickReward(poolId, drops);
        if (rewardId == null || rewardId.isEmpty()) return;

        Vector3i pos = event.getTargetBlock();

        String completedExpedition = expeditionState.onBreak();
        String nextBlockId = (completedExpedition != null)
                ? OneBlockBlockIds.DEFAULT_BLOCK_ID
                : event.getBlockType().getId();

        Vector3i finalPos = pos;
        String finalBlockId = nextBlockId;
        world.execute(() -> world.setBlock(finalPos.getX(), finalPos.getY(), finalPos.getZ(), finalBlockId));

        DropableContext context = new DropableContext(
                store, world, pos,
                new Vector3i(pos.getX() + REWARD_OFFSET.getX(),
                             pos.getY() + REWARD_OFFSET.getY(),
                             pos.getZ() + REWARD_OFFSET.getZ()),
                ref,
                store.getComponent(ref, PlayerRef.getComponentType())
        );
        dropRegistry.executeDropable(rewardId, context);

        if (completedExpedition != null && player != null)
        {
            executeCompletionRewards(completedExpedition, context);
            player.sendMessage(Message.raw("Expedition complete: " + completedExpedition + ". The OneBlock has returned to default."));
        }
    }

    private void executeCompletionRewards(String expeditionId, DropableContext context)
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
