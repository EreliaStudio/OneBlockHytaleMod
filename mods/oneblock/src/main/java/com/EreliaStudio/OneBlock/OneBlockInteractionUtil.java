package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.logging.Level;

final class OneBlockInteractionUtil
{
    private OneBlockInteractionUtil()
    {
    }

    static void consumeHeldItem(InteractionContext interactionContext, ItemStack heldItem)
    {
        if (interactionContext == null || heldItem == null)
        {
            return;
        }

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

    static void notifyPlayer(CommandBuffer<EntityStore> commandBuffer,
                             InteractionContext interactionContext,
                             String message)
    {
        if (commandBuffer == null || interactionContext == null || message == null || message.isEmpty())
        {
            return;
        }

        Ref<EntityStore> ref = interactionContext.getEntity();
        if (ref == null)
        {
            return;
        }

        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        if (player != null)
        {
            player.sendMessage(Message.raw(message));
        }
    }

    static void finish(InteractionContext interactionContext)
    {
        setState(interactionContext, InteractionState.Finished);
    }

    static void skip(InteractionContext interactionContext)
    {
        setState(interactionContext, InteractionState.Skip);
    }

    static void fail(InteractionContext interactionContext, HytaleLogger logger, String reason)
    {
        setState(interactionContext, InteractionState.Failed);

        if (logger != null && reason != null && !reason.isEmpty())
        {
            logger.at(Level.WARNING).log(reason);
        }
    }

    private static void setState(InteractionContext interactionContext, InteractionState state)
    {
        if (interactionContext != null && interactionContext.getState() != null)
        {
            interactionContext.getState().state = state;
        }
    }
}
