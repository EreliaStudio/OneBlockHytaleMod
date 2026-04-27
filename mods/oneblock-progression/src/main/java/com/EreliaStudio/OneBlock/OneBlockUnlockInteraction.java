package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

public final class OneBlockUnlockInteraction extends SimpleInstantInteraction
{
    public static final String INTERACTION_ID = "oneblock_unlock_use";

    public static final BuilderCodec<OneBlockUnlockInteraction> CODEC = BuilderCodec.builder(
            OneBlockUnlockInteraction.class,
            OneBlockUnlockInteraction::new,
            SimpleInstantInteraction.CODEC
    ).build();

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final String PREFIX = "OneBlock_Unlock_";

    @Override
    protected void firstRun(
            @Nonnull InteractionType interactionType,
            @Nonnull InteractionContext interactionContext,
            @Nonnull CooldownHandler cooldownHandler)
    {
        ItemStack heldItem = interactionContext.getHeldItem();

        if (heldItem == null || heldItem.isEmpty())
        {
            OneBlockInteractionUtil.skip(interactionContext);
            return;
        }

        UnlockData unlockData = parseUnlockItemId(heldItem.getItemId());

        if (unlockData == null)
        {
            OneBlockInteractionUtil.skip(interactionContext);
            return;
        }

        OneBlockPlugin base = OneBlockPlugin.getInstance();

        if (base == null)
        {
            OneBlockInteractionUtil.fail(interactionContext, LOGGER, "OneBlock Core plugin not available");
            return;
        }

        OneBlockDropRegistry dropRegistry = base.getDropRegistry();

        Dropable dropable = OneBlockDropId.parse(unlockData.dropId).isEntity()
                ? new EntitySpawnDropable(unlockData.dropId)
                : new ItemDropable(unlockData.dropId);

        dropRegistry.registerDropable(dropable);
        dropRegistry.registerWeight(
                unlockData.expeditionId,
                unlockData.dropId,
                unlockData.weight
        );

        OneBlockInteractionUtil.consumeHeldItem(interactionContext, heldItem);

        OneBlockInteractionUtil.notifyPlayer(
                interactionContext.getCommandBuffer(),
                interactionContext,
                "Unlocked drop '" + unlockData.dropId + "' for expedition '" + unlockData.expeditionId + "'."
        );

        LOGGER.at(Level.INFO).log(
                "Unlocked drop '" + unlockData.dropId + "' for expedition '" + unlockData.expeditionId + "' with weight " + unlockData.weight
        );

        OneBlockInteractionUtil.finish(interactionContext);
    }

    private static UnlockData parseUnlockItemId(String itemId)
    {
        if (itemId == null || !itemId.startsWith(PREFIX))
        {
            return null;
        }

        String payload = itemId.substring(PREFIX.length());

        List<String> expeditionIds = OneBlockExpeditionDefaults.getExpeditionIds()
                .stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .toList();

        for (String expeditionId : expeditionIds)
        {
            String expectedPrefix = expeditionId + "_";

            if (!payload.startsWith(expectedPrefix))
            {
                continue;
            }

            String dropId = payload.substring(expectedPrefix.length());

            if (dropId.isEmpty())
            {
                return null;
            }

            return new UnlockData(expeditionId, dropId, 10);
        }

        return null;
    }

    private static final class UnlockData
    {
        private final String expeditionId;
        private final String dropId;
        private final int weight;

        private UnlockData(String expeditionId, String dropId, int weight)
        {
            this.expeditionId = expeditionId;
            this.dropId = dropId;
            this.weight = Math.max(1, weight);
        }
    }
}