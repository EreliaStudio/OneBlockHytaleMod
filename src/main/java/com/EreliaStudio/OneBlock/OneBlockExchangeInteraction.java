package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
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

public final class OneBlockExchangeInteraction extends SimpleInstantInteraction
{
    public static final String INTERACTION_ID = "oneblock_exchange";

    public static final BuilderCodec<OneBlockExchangeInteraction> CODEC = BuilderCodec.builder(
            OneBlockExchangeInteraction.class,
            OneBlockExchangeInteraction::new,
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

        OneBlockExchangeService exchangeService = plugin.getExchangeService();
        if (exchangeService == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Exchange service is not initialized");
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

        OneBlockExchangeService.ExchangeDefinition definition = exchangeService.getDefinition(itemId);
        if (definition == null)
        {
            OneBlockInteractionUtil.finish(interactionContext);
            return;
        }

        OneBlockExchangeService.ExchangeConsumeResult result = exchangeService.canConsume(playerId, itemId);
        switch (result)
        {
            case INVALID_ITEM:
                OneBlockInteractionUtil.finish(interactionContext);
                return;

            case LOCKED:
                player.sendMessage(Message.raw("Exchange not unlocked: " + itemId));
                OneBlockInteractionUtil.finish(interactionContext);
                return;

            case READY:
            default:
                if (definition.outputId == null || definition.outputId.isEmpty())
                {
                    OneBlockInteractionUtil.fail(interactionContext, LOGGER, "Exchange output id is empty");
                    return;
                }

                ItemStack output = new ItemStack(definition.outputId, definition.outputQuantity);
                ItemContainer container = interactionContext.getHeldItemContainer();
                short slot = (short) interactionContext.getHeldItemSlot();

                if (container != null && slot >= 0)
                {
                    container.setItemStackForSlot(slot, output);
                }

                interactionContext.setHeldItem(output);
                player.sendMessage(Message.raw("Exchanged for: " + definition.outputId + " x" + definition.outputQuantity));
                OneBlockInteractionUtil.finish(interactionContext);
        }
    }
}
