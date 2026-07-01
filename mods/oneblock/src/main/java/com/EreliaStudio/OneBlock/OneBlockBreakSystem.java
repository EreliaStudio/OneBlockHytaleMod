package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthChunk;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.math.util.ChunkUtil;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joml.Vector3i;

public final class OneBlockBreakSystem extends EntityEventSystem<EntityStore, BreakBlockEvent>
{
    private static final Vector3i REWARD_OFFSET = new Vector3i(0, 1, 0);
    private static final int DUNGEON_SPAWN_RADIUS = 5;

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

        // BreakBlockEvent is emitted after native block health reaches zero.
        // Keep the supporting block physically present, then reset its native
        // health entry so the next break starts with full rock durability.
        event.setCancelled(true);
        resetBlockHealth(world, pos);

        DropableContext context = new DropableContext(
                store, world, pos,
                new Vector3i(pos.x() + REWARD_OFFSET.x(),
                             pos.y() + REWARD_OFFSET.y(),
                             pos.z() + REWARD_OFFSET.z()),
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
        List<Vector3i> spawnBlocks = findDungeonSpawnBlocks(world, pos);
        if (!spawnBlocks.isEmpty()) Collections.shuffle(spawnBlocks);

        int spawnIndex = 0;
        for (String entityId : wave)
        {
            DropableContext spawnContext = context;
            if (!spawnBlocks.isEmpty())
            {
                spawnContext = withSpawnBlock(context, spawnBlocks.get(spawnIndex % spawnBlocks.size()));
                spawnIndex++;
            }

            dropRegistry.executeDropable(entityId, spawnContext);
        }

        String completedDungeon = dungeonState.onWaveCompleted();
        OneBlockPlugin plugin = OneBlockPlugin.getInstance();

        if (completedDungeon != null)
        {
            world.execute(() -> world.setBlock(pos.x(), pos.y(), pos.z(), OneBlockBlockIds.DEFAULT_BLOCK_ID));
            executeDungeonCompletionRewards(completedDungeon, context);

            if (player != null)
            {
                if (plugin != null)
                {
                    plugin.getHudService().showDungeonCompleted(player, completedDungeon);
                }

            }
        }
        else
        {
            String dungeonBlockId = OneBlockDungeonDefaults.getBlockId(dungeonId);
            if (dungeonBlockId == null) dungeonBlockId = OneBlockBlockIds.DEFAULT_BLOCK_ID;

            String finalBlockId = dungeonBlockId;
            world.execute(() -> world.setBlock(pos.x(), pos.y(), pos.z(), finalBlockId));

            int completedWaves = dungeonState.getCurrentWaveIndex();
            int totalWaves = OneBlockDungeonDefaults.getWaveCount(dungeonId);

            if (player != null)
            {
                if (plugin != null)
                {
                    plugin.getHudService().updateDungeonWave(
                            player,
                            dungeonId,
                            completedWaves,
                            totalWaves
                    );
                }

            }
        }
    }

    private static List<Vector3i> findDungeonSpawnBlocks(World world, Vector3i sourceBlock)
    {
        if (world == null || sourceBlock == null) return List.of();

        List<Vector3i> spawnBlocks = new ArrayList<>();
        int sourceX = sourceBlock.x();
        int sourceY = sourceBlock.y();
        int sourceZ = sourceBlock.z();

        for (int y = sourceY; y >= sourceY - DUNGEON_SPAWN_RADIUS; y--)
        {
            for (int x = sourceX - DUNGEON_SPAWN_RADIUS; x <= sourceX + DUNGEON_SPAWN_RADIUS; x++)
            {
                for (int z = sourceZ - DUNGEON_SPAWN_RADIUS; z <= sourceZ + DUNGEON_SPAWN_RADIUS; z++)
                {
                    if (x == sourceX && y == sourceY && z == sourceZ) continue;
                    if (isDungeonSpawnBlock(world, x, y, z))
                        spawnBlocks.add(new Vector3i(x, y, z));
                }
            }
        }

        return spawnBlocks;
    }

    private static boolean isDungeonSpawnBlock(World world, int x, int y, int z)
    {
        BlockType floor = world.getBlockType(x, y, z);
        BlockType feet = world.getBlockType(x, y + 1, z);
        BlockType head = world.getBlockType(x, y + 2, z);

        return isSolidBlock(floor) && isClearBlock(feet) && isClearBlock(head);
    }

    private static boolean isSolidBlock(BlockType blockType)
    {
        return blockType != null
                && blockType != BlockType.EMPTY
                && blockType.getMaterial() != BlockMaterial.Empty;
    }

    private static boolean isClearBlock(BlockType blockType)
    {
        return blockType != null
                && (blockType == BlockType.EMPTY || blockType.getMaterial() == BlockMaterial.Empty);
    }

    private static DropableContext withSpawnBlock(DropableContext context, Vector3i spawnBlock)
    {
        return new DropableContext(
                context.getStore(),
                context.getWorld(),
                context.getSourceBlock(),
                spawnBlock,
                context.getPlayerEntity(),
                context.getPlayerRef()
        );
    }

    private void handleExpeditionBreak(World world,
                                       Vector3i pos,
                                       Player player,
                                       BreakBlockEvent event,
                                       DropableContext context)
    {
        String poolId = OneBlockPools.resolvePoolId(event.getBlockType());
        ensureExpeditionActiveForBreak(player, poolId);

        List<String> drops = dropRegistry.getKnownDrops(poolId);
        String rewardId = dropRegistry.pickReward(poolId, drops);
        if (rewardId == null || rewardId.isEmpty())
        {
            String currentBlockId = event.getBlockType().getId();
            world.execute(() -> world.setBlock(pos.x(), pos.y(), pos.z(), currentBlockId));
            return;
        }

        String activeExpeditionBeforeBreak = expeditionState.getActiveExpeditionId();
        int totalTicks = expeditionState.getTotalTicks();
        if (totalTicks <= 0)
        {
            totalTicks = OneBlockExpeditionDefaults.getTicks(activeExpeditionBeforeBreak);
        }

        String completedExpedition = expeditionState.onBreak();

        String nextBlockId = (completedExpedition != null)
                ? OneBlockBlockIds.DEFAULT_BLOCK_ID
                : event.getBlockType().getId();

        String finalBlockId = nextBlockId;
        world.execute(() -> world.setBlock(pos.x(), pos.y(), pos.z(), finalBlockId));

        dropRegistry.executeDropable(rewardId, context);

        OneBlockPlugin plugin = OneBlockPlugin.getInstance();

        if (completedExpedition != null && player != null)
        {
            executeExpeditionCompletionRewards(completedExpedition, context);

            if (plugin != null)
            {
                plugin.getHudService().showExpeditionCompleted(player, completedExpedition);
            }
        }
        else if (player != null && plugin != null && activeExpeditionBeforeBreak != null && !activeExpeditionBeforeBreak.isBlank())
        {
            plugin.getHudService().updateExpeditionTicks(
                    player,
                    activeExpeditionBeforeBreak,
                    expeditionState.getTicksRemaining(),
                    totalTicks
            );
        }
    }

    private void ensureExpeditionActiveForBreak(Player player, String expeditionId)
    {
        if (expeditionState.hasActiveExpedition()) return;
        if (expeditionId == null || expeditionId.isBlank()) return;
        if (!OneBlockExpeditionDefaults.getExpeditionIds().contains(expeditionId)) return;

        int ticks = OneBlockExpeditionDefaults.getTicks(expeditionId);
        expeditionState.startExpedition(expeditionId, ticks);

        OneBlockPlugin plugin = OneBlockPlugin.getInstance();
        if (player != null && plugin != null)
        {
            plugin.getHudService().showExpeditionStarted(player, expeditionId, ticks);
        }
    }

    private void executeDungeonCompletionRewards(String dungeonId, DropableContext context)
    {
        for (OneBlockDungeonDefaults.CompletionRewardDefinition reward :
                OneBlockDungeonDefaults.getCompletionRewards(dungeonId))
        {
            giveDungeonReward(reward, context);
        }

        OneBlockDungeonDefaults.RandomRewardBundle bundle =
                OneBlockDungeonDefaults.pickRandomBundle(dungeonId);

        if (bundle != null)
        {
            for (OneBlockDungeonDefaults.CompletionRewardDefinition reward : bundle.items)
            {
                giveDungeonReward(reward, context);
            }
        }
    }

    private void giveDungeonReward(OneBlockDungeonDefaults.CompletionRewardDefinition reward, DropableContext context)
    {
        if (reward == null || reward.dropId == null || reward.dropId.isEmpty()) return;

        dropRegistry.executeDropable(reward.dropId, context, reward.quantity);

        if (reward.isCrystalReward())
        {
            CraftingPlugin.learnRecipe(context.getPlayerEntity(), reward.dropId, context.getStore());

            OneBlockNotifier.notifyExpeditionUnlocked(
                    context.getStore(),
                    context.getPlayerEntity(),
                    reward.unlockExpeditionId
            );
        }
    }

    private void executeExpeditionCompletionRewards(String expeditionId, DropableContext context)
    {
        for (OneBlockExpeditionDefaults.CompletionRewardDefinition reward :
                OneBlockExpeditionDefaults.getMandatoryRewards(expeditionId))
        {
            giveReward(reward, context);
        }

        OneBlockExpeditionDefaults.RandomRewardBundle bundle =
                OneBlockExpeditionDefaults.pickRandomBundle(expeditionId);

        if (bundle != null)
        {
            for (OneBlockExpeditionDefaults.CompletionRewardDefinition reward : bundle.items)
            {
                giveReward(reward, context);
            }
        }
    }

    private void giveReward(OneBlockExpeditionDefaults.CompletionRewardDefinition reward, DropableContext context)
    {
        if (reward == null || reward.dropId == null || reward.dropId.isEmpty()) return;

        dropRegistry.executeDropable(reward.dropId, context, reward.quantity);

        if (reward.isCrystalReward())
        {
            CraftingPlugin.learnRecipe(context.getPlayerEntity(), reward.dropId, context.getStore());

            OneBlockNotifier.notifyExpeditionUnlocked(
                    context.getStore(),
                    context.getPlayerEntity(),
                    reward.unlockExpeditionId
            );
        }
    }

    private static void resetBlockHealth(World world, Vector3i pos)
    {
        if (world == null || pos == null) return;

        ChunkStore chunkStore = world.getChunkStore();
        if (chunkStore == null) return;

        long chunkIndex = ChunkUtil.indexChunkFromBlock(pos.x(), pos.z());
        Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
        if (chunkRef == null || !chunkRef.isValid()) return;

        Store<ChunkStore> chunkComponentStore = chunkStore.getStore();
        BlockHealthChunk blockHealth = chunkComponentStore.getComponent(
                chunkRef,
                BlockHealthModule.get().getBlockHealthChunkComponentType()
        );
        if (blockHealth != null)
        {
            blockHealth.removeBlock(world, pos);
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
