package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.logging.Level;

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
            fail(interactionContext, "OneBlockPlugin instance is null");
            return;
        }

        OneBlockUnlockService unlockService = plugin.getUnlockService();
        if (unlockService == null)
        {
            fail(interactionContext, "Unlock service is not initialized");
            return;
        }

        ItemStack heldItem = interactionContext.getHeldItem();
        if (heldItem == null || heldItem.isEmpty())
        {
            fail(interactionContext, "Held item is empty");
            return;
        }

        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null)
        {
            fail(interactionContext, "CommandBuffer is null");
            return;
        }

        Ref<EntityStore> ref = interactionContext.getEntity();
        if (ref == null)
        {
            fail(interactionContext, "Entity reference is null");
            return;
        }

        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        if (playerRef == null || player == null)
        {
            fail(interactionContext, "Player component is missing");
            return;
        }

        UUID playerId = playerRef.getUuid();
        String itemId = heldItem.getItemId();
        OneBlockUnlockService.UnlockConsumeResult result = unlockService.consume(playerId, itemId);

        switch (result)
        {
            case INVALID_ITEM:
                // Not an unlock item; treat as a no-op so the interaction doesn't hard-fail.
                finish(interactionContext);
                return;

            case ALREADY_UNLOCKED:
                player.sendMessage(Message.raw("OneBlock unlock already known: " + itemId));
                finish(interactionContext);
                return;

            case UNLOCKED:
            {
                String dropId = unlockService.getDropItemIdForConsumable(itemId);
                consumeHeldItem(interactionContext, heldItem);
                player.sendMessage(Message.raw("Unlocked OneBlock drop: " + dropId));
                finish(interactionContext);
                return;
            }

            case UNLOCK_FAILED:
            default:
                player.sendMessage(Message.raw("Failed to unlock OneBlock drop from: " + itemId));
                fail(interactionContext, "Unlock failed for item: " + itemId);
        }
    }

    private static void consumeHeldItem(InteractionContext interactionContext, ItemStack heldItem)
    {
        int quantity = heldItem.getQuantity();
        if (quantity <= 0)
        {
            return;
        }

        ItemContainer container = interactionContext.getHeldItemContainer();
        short slot = (short) interactionContext.getHeldItemSlot();

        ItemStack updatedStack = (quantity <= 1)
                ? ItemStack.EMPTY
                : heldItem.withQuantity(quantity - 1);

        if (container != null && slot >= 0)
        {
            if (updatedStack.isEmpty())
            {
                container.removeItemStackFromSlot(slot);
            }
            else
            {
                container.setItemStackForSlot(slot, updatedStack);
            }
        }

        interactionContext.setHeldItem(updatedStack);
    }

    private static void finish(InteractionContext interactionContext)
    {
        if (interactionContext.getState() != null)
        {
            interactionContext.getState().state = InteractionState.Finished;
        }
    }

    private static void fail(InteractionContext interactionContext, String reason)
    {
        if (interactionContext.getState() != null)
        {
            interactionContext.getState().state = InteractionState.Failed;
        }

        if (reason != null && !reason.isEmpty())
        {
            LOGGER.at(Level.WARNING).log(reason);
        }
    }
}
