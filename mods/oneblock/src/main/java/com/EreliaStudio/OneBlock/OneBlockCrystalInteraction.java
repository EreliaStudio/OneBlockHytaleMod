package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.logger.HytaleLogger;
import org.joml.Vector3i;

import javax.annotation.Nonnull;

public final class OneBlockCrystalInteraction extends SimpleInstantInteraction
{
    public static final String INTERACTION_ID = "oneblock_crystal_use";

    public static final BuilderCodec<OneBlockCrystalInteraction> CODEC = BuilderCodec.builder(
            OneBlockCrystalInteraction.class,
            OneBlockCrystalInteraction::new,
            SimpleInstantInteraction.CODEC
    ).build();

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    protected void firstRun(
            @Nonnull InteractionType interactionType,
            @Nonnull InteractionContext interactionContext,
            @Nonnull CooldownHandler cooldownHandler)
    {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "CommandBuffer is null");
            return;
        }

        EntityStore entityStore = commandBuffer.getExternalData();
        if (entityStore == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "EntityStore is null");
            return;
        }

        World world = entityStore.getWorld();
        if (world == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "World is null");
            return;
        }

        ItemStack heldItem = interactionContext.getHeldItem();
        if (heldItem == null || heldItem.isEmpty())
        {
            OneBlockInteractionUtil.skip(interactionContext);
            return;
        }

        String itemId = heldItem.getItemId();
        String expeditionId = OneBlockExpeditionResolver.expeditionFromCrystalItemId(itemId);
        if (expeditionId == null)
        {
            OneBlockInteractionUtil.skip(interactionContext);
            return;
        }

        OneBlockPlugin plugin = OneBlockPlugin.getInstance();
        if (plugin == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Core plugin not available");
            return;
        }

        OneBlockWorldStateRegistry stateRegistry = plugin.getWorldStateRegistry();
        OneBlockRootRegistry rootRegistry = plugin.getRootRegistry();
        if (stateRegistry == null || rootRegistry == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "OneBlock state is unavailable");
            return;
        }

        Ref<EntityStore> actorEntityRef = interactionContext.getEntity();
        Player actor = actorEntityRef == null
                ? null
                : commandBuffer.getComponent(actorEntityRef, Player.getComponentType());
        PlayerRef actorPlayerRef = actorEntityRef == null
                ? null
                : commandBuffer.getComponent(actorEntityRef, PlayerRef.getComponentType());

        boolean hasPersonalRoots = actorPlayerRef != null
                && rootRegistry.hasRoots(world, actorPlayerRef.getUuid());
        OneBlockRootRegistry.RootEntry root = hasPersonalRoots
                ? new OneBlockRootRegistry.RootEntry(
                        actorPlayerRef.getUuid(),
                        actorPlayerRef.getUsername()
                )
                : null;

        if (root == null && !stateRegistry.isManaged(world))
        {
            OneBlockInteractionUtil.fail(
                    interactionContext,
                    LOGGER,
                    "Place a OneBlock Root before using an expedition crystal"
            );
            return;
        }

        OneBlockExpeditionStateProvider expeditionState = root == null
                ? stateRegistry.expeditionState(world)
                : rootRegistry.expeditionState(world, root.ownerId());
        OneBlockDungeonStateProvider dungeonState = root == null
                ? stateRegistry.dungeonState(world)
                : rootRegistry.dungeonState(world, root.ownerId());

        String newBlockId = OneBlockExpeditionResolver.blockIdForExpedition(expeditionId);
        world.execute(() -> setTargetBlocks(world, rootRegistry, root, newBlockId));

        OneBlockInteractionUtil.consumeHeldItem(interactionContext, heldItem);

        if (OneBlockDungeonDefaults.isDungeon(expeditionId))
        {
            expeditionState.endExpedition();
            dungeonState.startDungeon(expeditionId);

            int waveCount = OneBlockDungeonDefaults.getWaveCount(expeditionId);

            OneBlockAudience.forTarget(
                    world,
                    actor,
                    root,
                    worldPlayer -> plugin.getHudService().showDungeonStarted(worldPlayer, expeditionId, waveCount)
            );
        }
        else
        {
            int ticks = OneBlockExpeditionDefaults.getTicks(expeditionId);

            dungeonState.endDungeon();
            expeditionState.startExpedition(expeditionId, ticks);

            OneBlockAudience.forTarget(
                    world,
                    actor,
                    root,
                    worldPlayer -> plugin.getHudService().showExpeditionStarted(worldPlayer, expeditionId, ticks)
            );
        }

        OneBlockInteractionUtil.finish(interactionContext);
    }

    private static void setTargetBlocks(World world,
                                        OneBlockRootRegistry rootRegistry,
                                        OneBlockRootRegistry.RootEntry root,
                                        String blockId)
    {
        if (root == null)
        {
            Vector3i position = OneBlockBlockIds.ONEBLOCK_POSITION;
            world.setBlock(position.x(), position.y(), position.z(), blockId);
            return;
        }

        for (Vector3i position : rootRegistry.positions(world, root.ownerId()))
        {
            world.setBlock(position.x(), position.y(), position.z(), blockId);
        }
    }
}
