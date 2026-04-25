package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.logger.HytaleLogger;

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
    protected void firstRun(@Nonnull InteractionType interactionType,
                            @Nonnull InteractionContext interactionContext,
                            @Nonnull CooldownHandler cooldownHandler)
    {
        if (interactionContext == null) return;

        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) { OneBlockInteractionUtil.fail(interactionContext, LOGGER, "CommandBuffer is null"); return; }

        EntityStore entityStore = commandBuffer.getExternalData();
        if (entityStore == null) { OneBlockInteractionUtil.fail(interactionContext, LOGGER, "EntityStore is null"); return; }

        World world = entityStore.getWorld();
        if (world == null) { OneBlockInteractionUtil.fail(interactionContext, LOGGER, "World is null"); return; }

        ItemStack heldItem = interactionContext.getHeldItem();
        if (heldItem == null || heldItem.isEmpty()) { OneBlockInteractionUtil.skip(interactionContext); return; }

        BlockPosition target = interactionContext.getTargetBlock();
        if (target == null) { OneBlockInteractionUtil.skip(interactionContext); return; }

        BlockType existing = world.getBlockType(target.x, target.y, target.z);
        if (!OneBlockBlockUtil.isOneBlock(existing)) { OneBlockInteractionUtil.skip(interactionContext); return; }

        String itemId = heldItem.getItemId();
        String expeditionId = OneBlockExpeditionResolver.expeditionFromCrystalItemId(itemId);
        if (expeditionId == null)
        {
            OneBlockInteractionUtil.skip(interactionContext);
            return;
        }

        int ticks = OneBlockExpeditionResolver.ticksFromCrystalItemId(itemId);
        if (ticks <= 0)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Invalid crystal item: " + itemId);
            return;
        }

        OneBlockPlugin base = OneBlockPlugin.getInstance();
        if (base == null) { OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Core plugin not available"); return; }

        String targetBlockId = OneBlockExpeditionResolver.blockIdForExpedition(expeditionId);
        world.execute(() -> world.setBlock(target.x, target.y, target.z, targetBlockId));
        base.getExpeditionStateProvider().startExpedition(expeditionId, ticks);

        OneBlockInteractionUtil.consumeHeldItem(interactionContext, heldItem);
        OneBlockInteractionUtil.notifyPlayer(commandBuffer, interactionContext,
                "Expedition " + expeditionId + " started — " + ticks + " ticks remaining.");
        OneBlockInteractionUtil.finish(interactionContext);
    }
}
