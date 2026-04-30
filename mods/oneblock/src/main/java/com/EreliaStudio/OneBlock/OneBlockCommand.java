package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Locale;

public final class OneBlockCommand extends AbstractTargetPlayerCommand
{
    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> valueArg;

    public OneBlockCommand()
    {
        super("oneblock", "Admin commands for the OneBlock expedition system.");
        this.actionArg = this.withRequiredArg("action", "status|start|stop|list", ArgTypes.STRING);
        this.valueArg = this.withRequiredArg("value", "Expedition ID (for start), or '-' (for status/stop/list)", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Ref<EntityStore> senderRef,
                           @Nonnull Ref<EntityStore> targetRef,
                           @Nonnull PlayerRef targetPlayerRef,
                           @Nonnull World world,
                           @Nonnull Store<EntityStore> store)
    {
        OneBlockPlugin plugin = OneBlockPlugin.getInstance();
        if (plugin == null)
        {
            ctx.sendMessage(Message.raw("OneBlock plugin is not available."));
            return;
        }

        OneBlockExpeditionStateProvider stateProvider = plugin.getExpeditionStateProvider();
        OneBlockDropRegistry dropRegistry = plugin.getDropRegistry();
        Player targetPlayer = getPlayer(store, targetRef);

        String action = safeLower(actionArg.get(ctx));
        String value = valueArg.get(ctx);

        switch (action)
        {
            case "status" -> handleStatus(ctx, stateProvider);
            case "start" -> handleStart(ctx, plugin, stateProvider, targetPlayer, value);
            case "stop" -> handleStop(ctx, plugin, stateProvider, targetPlayer, world);
            case "list" -> handleList(ctx, dropRegistry, value);
            default -> ctx.sendMessage(Message.raw("Unknown action: " + action + " (expected status|start|stop|list)"));
        }
    }

    private static void handleStatus(CommandContext ctx, OneBlockExpeditionStateProvider stateProvider)
    {
        if (!stateProvider.hasActiveExpedition())
        {
            ctx.sendMessage(Message.raw("No expedition is currently active. OneBlock is in default mode."));
            return;
        }

        String expeditionId = stateProvider.getActiveExpeditionId();
        int ticks = stateProvider.getTicksRemaining();

        ctx.sendMessage(Message.raw("Active expedition: " + expeditionId + " | Ticks remaining: " + ticks));
    }

    private static void handleStart(CommandContext ctx,
                                    OneBlockPlugin plugin,
                                    OneBlockExpeditionStateProvider stateProvider,
                                    Player targetPlayer,
                                    String expeditionId)
    {
        if (expeditionId == null || expeditionId.isBlank() || "-".equals(expeditionId.trim()))
        {
            ctx.sendMessage(Message.raw("Usage: /oneblock - start <expeditionId>"));
            return;
        }

        String normalizedExpeditionId = expeditionId.trim();
        int ticks = OneBlockExpeditionDefaults.getTicks(normalizedExpeditionId);

        stateProvider.startExpedition(normalizedExpeditionId, ticks);

        if (targetPlayer != null)
        {
            plugin.getHudService().showExpeditionStarted(
                    targetPlayer,
                    normalizedExpeditionId,
                    ticks
            );
        }

        ctx.sendMessage(Message.raw("Started expedition '" + normalizedExpeditionId + "' with " + ticks + " ticks."));
    }

    private static void handleStop(CommandContext ctx,
                                   OneBlockPlugin plugin,
                                   OneBlockExpeditionStateProvider stateProvider,
                                   Player targetPlayer,
                                   World world)
    {
        if (!stateProvider.hasActiveExpedition())
        {
            ctx.sendMessage(Message.raw("No expedition is active."));
            return;
        }

        String expeditionId = stateProvider.getActiveExpeditionId();

        stateProvider.endExpedition();

        if (world != null)
        {
            Vector3i pos = OneBlockBlockIds.ONEBLOCK_POSITION;
            world.execute(() -> world.setBlock(pos.getX(), pos.getY(), pos.getZ(), OneBlockBlockIds.DEFAULT_BLOCK_ID));
        }

        if (targetPlayer != null)
        {
            plugin.getHudService().clear(targetPlayer);
        }

        ctx.sendMessage(Message.raw("Stopped expedition '" + expeditionId + "'. OneBlock reset to default."));
    }

    private static void handleList(CommandContext ctx, OneBlockDropRegistry dropRegistry, String expeditionId)
    {
        String poolId = (expeditionId == null || expeditionId.isBlank() || "-".equals(expeditionId.trim()))
                ? OneBlockPools.DEFAULT_POOL_ID
                : OneBlockPools.normalizePoolId(expeditionId.trim());

        var drops = dropRegistry.getKnownDrops(poolId);
        if (drops.isEmpty())
        {
            ctx.sendMessage(Message.raw("No drops registered for expedition: " + poolId));
            return;
        }

        ctx.sendMessage(Message.raw("Drops for expedition '" + poolId + "' (" + drops.size() + "): " + String.join(", ", drops)));
    }

    private static Player getPlayer(Store<EntityStore> store, Ref<EntityStore> playerRef)
    {
        if (store == null || playerRef == null)
        {
            return null;
        }

        return store.getComponent(playerRef, Player.getComponentType());
    }

    private static String safeLower(String value)
    {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}