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

public final class OneBlockExpeditionInteraction extends SimpleInstantInteraction
{
    public static final String INTERACTION_ID = "oneblock_expedition_change";

    public static final BuilderCodec<OneBlockExpeditionInteraction> CODEC = BuilderCodec.builder(
            OneBlockExpeditionInteraction.class,
            OneBlockExpeditionInteraction::new,
            SimpleInstantInteraction.CODEC
    ).build();

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    protected void firstRun(@Nonnull InteractionType interactionType,
                            @Nonnull InteractionContext interactionContext,
                            @Nonnull CooldownHandler cooldownHandler)
    {
        if (interactionContext == null)
        {
            return;
        }

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
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Held item is empty");
            return;
        }

        BlockPosition target = interactionContext.getTargetBlock();
        if (target == null)
        {
            OneBlockInteractionUtil.skip(interactionContext);
            return;
        }

        BlockType existing = world.getBlockType(target.x, target.y, target.z);
        if (!OneBlockBlockUtil.isOneBlock(existing))
        {
            OneBlockInteractionUtil.skip(interactionContext);
            return;
        }

        String expeditionId = OneBlockExpeditionResolver.expeditionFromKeyItemId(heldItem.getItemId());
        String targetBlockId = OneBlockExpeditionResolver.blockIdForExpedition(expeditionId);
        if (targetBlockId == null || targetBlockId.isEmpty())
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Invalid expedition item id: " + heldItem.getItemId());
            return;
        }

        if (existing != null && targetBlockId.equals(existing.getId()))
        {
            OneBlockInteractionUtil.notifyPlayer(commandBuffer, interactionContext, "OneBlock is already in that expedition.");
            OneBlockInteractionUtil.finish(interactionContext);
            return;
        }

        world.execute(() -> world.setBlock(target.x, target.y, target.z, targetBlockId));
        OneBlockInteractionUtil.consumeHeldItem(interactionContext, heldItem);

        OneBlockInteractionUtil.notifyPlayer(commandBuffer, interactionContext, "OneBlock expedition set to: " + expeditionId);
        OneBlockInteractionUtil.finish(interactionContext);
    }
}
