package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public final class OneBlockActInteraction extends SimpleInstantInteraction
{
    public static final String INTERACTION_ID = "oneblock_act_change";

    private static final String ONEBLOCK_CATEGORY = "Blocks.OneBlock";
    private static final boolean RESET_ENABLED_ON_CHAPTER_CHANGE = false;

    public static final BuilderCodec<OneBlockActInteraction> CODEC = BuilderCodec.builder(
            OneBlockActInteraction.class,
            OneBlockActInteraction::new,
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
            fail(interactionContext, "CommandBuffer is null");
            return;
        }

        EntityStore entityStore = commandBuffer.getExternalData();
        if (entityStore == null)
        {
            fail(interactionContext, "EntityStore is null");
            return;
        }

        World world = entityStore.getWorld();
        if (world == null)
        {
            fail(interactionContext, "World is null");
            return;
        }

        ItemStack heldItem = interactionContext.getHeldItem();
        if (heldItem == null || heldItem.isEmpty())
        {
            fail(interactionContext, "Held item is empty");
            return;
        }

        BlockPosition target = interactionContext.getTargetBlock();
        if (target == null)
        {
            notifyPlayer(commandBuffer, interactionContext, "Aim at a OneBlock block.");
            fail(interactionContext, "Target block is null");
            return;
        }

        String chapterId = OneBlockChapterResolver.chapterFromActItemId(heldItem.getItemId());
        String targetBlockId = OneBlockChapterResolver.blockIdForChapter(chapterId);
        if (targetBlockId == null || targetBlockId.isEmpty())
        {
            fail(interactionContext, "Invalid Act item id: " + heldItem.getItemId());
            return;
        }

        BlockType existing = world.getBlockType(target.x, target.y, target.z);
        if (!isOneBlock(existing))
        {
            notifyPlayer(commandBuffer, interactionContext, "That is not a OneBlock block.");
            fail(interactionContext, "Target block is not OneBlock");
            return;
        }

        if (existing != null && targetBlockId.equals(existing.getId()))
        {
            notifyPlayer(commandBuffer, interactionContext, "OneBlock is already in that chapter.");
            finish(interactionContext);
            return;
        }

        world.execute(() -> world.setBlock(target.x, target.y, target.z, targetBlockId));
        consumeHeldItem(interactionContext, heldItem);

        if (RESET_ENABLED_ON_CHAPTER_CHANGE)
        {
            OneBlockPlugin plugin = OneBlockPlugin.getInstance();
            if (plugin != null)
            {
                OneBlockDropsStateProvider provider = plugin.getDropsStateProvider();
                if (provider != null)
                {
                    resetEnabledForChapter(provider, commandBuffer, interactionContext, chapterId);
                }
            }
        }

        notifyPlayer(commandBuffer, interactionContext, "OneBlock chapter set to: " + targetBlockId);
        finish(interactionContext);
    }

    private static boolean isOneBlock(BlockType blockType)
    {
        if (blockType == null)
        {
            return false;
        }

        Item item = blockType.getItem();
        if (item == null)
        {
            return false;
        }

        String[] categories = item.getCategories();
        if (categories == null)
        {
            return false;
        }

        for (String category : categories)
        {
            if (ONEBLOCK_CATEGORY.equals(category))
            {
                return true;
            }
        }

        return false;
    }

    private static void notifyPlayer(CommandBuffer<EntityStore> commandBuffer,
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

    private static void resetEnabledForChapter(OneBlockDropsStateProvider provider,
                                               CommandBuffer<EntityStore> commandBuffer,
                                               InteractionContext interactionContext,
                                               String chapterId)
    {
        Ref<EntityStore> ref = interactionContext.getEntity();
        if (ref == null)
        {
            return;
        }

        PlayerRef playerRef = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null)
        {
            return;
        }

        // Reset by disabling everything then enabling unlocked drops.
        // This is a conservative reset that keeps unlocks but clears custom enable/disable.
        if (provider instanceof FileBackedOneBlockDropsStateProvider fileProvider)
        {
            fileProvider.resetEnabledToUnlocked(playerRef.getUuid(), chapterId);
        }
        else if (provider instanceof InMemoryOneBlockDropsStateProvider memoryProvider)
        {
            memoryProvider.resetEnabledToUnlocked(playerRef.getUuid(), chapterId);
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
