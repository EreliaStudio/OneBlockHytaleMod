package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
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
    private final OptionalArg<String> valueArg;

    public OneBlockCommand()
    {
        super("oneblock", "Admin commands for the OneBlock expedition system.");
        this.actionArg = this.withRequiredArg("action", "status|start|stop|list|fallProtection=true|false", ArgTypes.STRING);
        this.valueArg = this.withOptionalArg("value", "Expedition ID (for start/list), or fallProtection true|false", ArgTypes.STRING);
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
        OneBlockSettingsProvider settingsProvider = plugin.getSettingsProvider();
        OneBlockDropRegistry dropRegistry = plugin.getDropRegistry();
        Player targetPlayer = getPlayer(store, targetRef);

        ParsedAction parsedAction = parseAction(actionArg.get(ctx), valueArg.provided(ctx) ? valueArg.get(ctx) : null);
        String action = parsedAction.action();
        String value = parsedAction.value();

        switch (action)
        {
            case "status" -> handleStatus(ctx, stateProvider, settingsProvider);
            case "start" -> handleStart(ctx, plugin, stateProvider, targetPlayer, value);
            case "stop" -> handleStop(ctx, plugin, stateProvider, targetPlayer, world);
            case "list" -> handleList(ctx, dropRegistry, value);
            case "fallprotection" -> handleFallProtection(ctx, settingsProvider, value);
            default -> ctx.sendMessage(Message.raw("Unknown action: " + action + " (expected status|start|stop|list|fallProtection=true|false)"));
        }
    }

    private static void handleStatus(CommandContext ctx,
                                     OneBlockExpeditionStateProvider stateProvider,
                                     OneBlockSettingsProvider settingsProvider)
    {
        if (!stateProvider.hasActiveExpedition())
        {
            ctx.sendMessage(Message.raw("No expedition is currently active. OneBlock is in default mode. Fall protection: " + formatEnabled(settingsProvider.isFallProtectionEnabled())));
            return;
        }

        String expeditionId = stateProvider.getActiveExpeditionId();
        int ticks = stateProvider.getTicksRemaining();

        ctx.sendMessage(Message.raw("Active expedition: " + expeditionId + " | Ticks remaining: " + ticks + " | Fall protection: " + formatEnabled(settingsProvider.isFallProtectionEnabled())));
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
            world.execute(() -> world.setBlock(pos.x(), pos.y(), pos.z(), OneBlockBlockIds.DEFAULT_BLOCK_ID));
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

    private static void handleFallProtection(CommandContext ctx,
                                             OneBlockSettingsProvider settingsProvider,
                                             String value)
    {
        if (settingsProvider == null)
        {
            ctx.sendMessage(Message.raw("OneBlock settings are not available."));
            return;
        }

        if (value == null || value.isBlank())
        {
            ctx.sendMessage(Message.raw("Fall protection is currently " + formatEnabled(settingsProvider.isFallProtectionEnabled()) + ". Usage: /oneblock fallProtection=true|false"));
            return;
        }

        Boolean enabled = parseBoolean(value);
        if (enabled == null)
        {
            ctx.sendMessage(Message.raw("Invalid fallProtection value: " + value + " (expected true or false)"));
            return;
        }

        settingsProvider.setFallProtectionEnabled(enabled);
        ctx.sendMessage(Message.raw("Fall protection is now " + formatEnabled(enabled) + "."));
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

    private static ParsedAction parseAction(String rawAction, String rawValue)
    {
        String action = rawAction == null ? "" : rawAction.trim();
        String value = rawValue;

        int separator = action.indexOf('=');
        if (separator >= 0)
        {
            value = action.substring(separator + 1);
            action = action.substring(0, separator);
        }

        return new ParsedAction(safeLower(action), value == null ? null : value.trim());
    }

    private static Boolean parseBoolean(String value)
    {
        String normalized = safeLower(value);
        if ("true".equals(normalized)) return Boolean.TRUE;
        if ("false".equals(normalized)) return Boolean.FALSE;
        return null;
    }

    private static String formatEnabled(boolean enabled)
    {
        return enabled ? "enabled" : "disabled";
    }

    private record ParsedAction(String action, String value) {}
}
