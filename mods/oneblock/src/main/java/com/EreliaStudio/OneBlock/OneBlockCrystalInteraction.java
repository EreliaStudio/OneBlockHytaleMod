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
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

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

        String newBlockId = OneBlockExpeditionResolver.blockIdForExpedition(expeditionId);
        Vector3i pos = OneBlockBlockIds.ONEBLOCK_POSITION;
        world.execute(() -> world.setBlock(pos.getX(), pos.getY(), pos.getZ(), newBlockId));

        OneBlockInteractionUtil.consumeHeldItem(interactionContext, heldItem);

        Player player = resolvePlayer(commandBuffer, interactionContext);

        if (OneBlockDungeonDefaults.isDungeon(expeditionId))
        {
            plugin.getDungeonStateProvider().startDungeon(expeditionId);

            int waveCount = OneBlockDungeonDefaults.getWaveCount(expeditionId);

            if (player != null)
            {
                plugin.getHudService().showDungeonStarted(player, expeditionId, waveCount);
            }
        }
        else
        {
            int ticks = OneBlockExpeditionDefaults.getTicks(expeditionId);

            plugin.getExpeditionStateProvider().startExpedition(expeditionId, ticks);

            if (player != null)
            {
                plugin.getHudService().showExpeditionStarted(player, expeditionId, ticks);
            }
        }

        OneBlockInteractionUtil.finish(interactionContext);
    }

    private static Player resolvePlayer(CommandBuffer<EntityStore> commandBuffer,
                                        InteractionContext interactionContext)
    {
        Player directPlayer = resolveDirectPlayer(interactionContext);
        if (directPlayer != null)
        {
            return directPlayer;
        }

        Ref<EntityStore> playerRef = resolvePlayerRef(interactionContext);
        if (playerRef == null)
        {
            return null;
        }

        return resolvePlayerFromRef(commandBuffer, playerRef);
    }

    private static Player resolveDirectPlayer(InteractionContext interactionContext)
    {
        Object value = callNoArg(interactionContext, "getPlayer");
        if (value instanceof Player player)
        {
            return player;
        }

        value = callNoArg(interactionContext, "getSourcePlayer");
        if (value instanceof Player player)
        {
            return player;
        }

        value = callNoArg(interactionContext, "getActor");
        if (value instanceof Player player)
        {
            return player;
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static Ref<EntityStore> resolvePlayerRef(InteractionContext interactionContext)
    {
        Object value = callNoArg(interactionContext, "getPlayer");
        if (value instanceof Ref<?> ref)
        {
            return (Ref<EntityStore>) ref;
        }

        value = callNoArg(interactionContext, "getPlayerRef");
        if (value instanceof Ref<?> ref)
        {
            return (Ref<EntityStore>) ref;
        }

        value = callNoArg(interactionContext, "getEntity");
        if (value instanceof Ref<?> ref)
        {
            return (Ref<EntityStore>) ref;
        }

        value = callNoArg(interactionContext, "getEntityRef");
        if (value instanceof Ref<?> ref)
        {
            return (Ref<EntityStore>) ref;
        }

        value = callNoArg(interactionContext, "getSourceEntity");
        if (value instanceof Ref<?> ref)
        {
            return (Ref<EntityStore>) ref;
        }

        return null;
    }

    private static Player resolvePlayerFromRef(CommandBuffer<EntityStore> commandBuffer,
                                               Ref<EntityStore> playerRef)
    {
        if (commandBuffer == null || playerRef == null)
        {
            return null;
        }

        for (Method method : commandBuffer.getClass().getMethods())
        {
            if (!"getComponent".equals(method.getName()))
            {
                continue;
            }

            if (method.getParameterCount() != 2)
            {
                continue;
            }

            try
            {
                Object value = method.invoke(commandBuffer, playerRef, Player.getComponentType());
                if (value instanceof Player player)
                {
                    return player;
                }
            }
            catch (Exception ignored)
            {
            }
        }

        return null;
    }

    private static Object callNoArg(Object target, String methodName)
    {
        if (target == null || methodName == null || methodName.isBlank())
        {
            return null;
        }

        try
        {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        }
        catch (Exception ignored)
        {
            return null;
        }
    }
}