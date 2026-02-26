package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.UUID;

public final class OneBlockUnlockInteraction extends SimpleInstantInteraction
{
    public static final String INTERACTION_ID = "oneblock_unlock_pool_insert";

    public static final BuilderCodec<OneBlockUnlockInteraction> CODEC = BuilderCodec.builder(
            OneBlockUnlockInteraction.class,
            OneBlockUnlockInteraction::new,
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

        OneBlockPlugin plugin = OneBlockPlugin.getInstance();
        if (plugin == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "OneBlockPlugin instance is null");
            return;
        }

        OneBlockUnlockService unlockService = plugin.getUnlockService();
        if (unlockService == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Unlock service is not initialized");
            return;
        }

        ItemStack heldItem = interactionContext.getHeldItem();
        if (heldItem == null || heldItem.isEmpty())
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Held item is empty");
            return;
        }

        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "CommandBuffer is null");
            return;
        }

        Ref<EntityStore> ref = interactionContext.getEntity();
        if (ref == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Entity reference is null");
            return;
        }

        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        if (playerRef == null || player == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Player component is missing");
            return;
        }

        UUID playerId = playerRef.getUuid();
        String itemId = heldItem.getItemId();
        OneBlockUnlockService.UnlockConsumeResult result = unlockService.consume(playerId, itemId);

        switch (result)
        {
            case INVALID_ITEM:
                // Not an unlock item; treat as a no-op so the interaction doesn't hard-fail.
                OneBlockInteractionUtil.finish(interactionContext);
                return;

            case ALREADY_UNLOCKED:
                player.sendMessage(Message.raw("OneBlock unlock already known: " + itemId));
                OneBlockInteractionUtil.finish(interactionContext);
                return;

            case UNLOCKED:
            {
                String dropId = unlockService.getDropItemIdForConsumable(itemId);
                OneBlockInteractionUtil.consumeHeldItem(interactionContext, heldItem);
                player.sendMessage(Message.raw("Unlocked OneBlock drop: " + dropId));
                OneBlockInteractionUtil.finish(interactionContext);
                return;
            }

            case UNLOCK_FAILED:
            default:
                player.sendMessage(Message.raw("Failed to unlock OneBlock drop from: " + itemId));
                OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Unlock failed for item: " + itemId);
        }
    }
}
