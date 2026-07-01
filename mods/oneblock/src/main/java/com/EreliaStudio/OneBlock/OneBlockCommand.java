package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3i;
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
            return;
        }

        OneBlockExpeditionStateProvider stateProvider = plugin.getExpeditionStateProvider();
        OneBlockSettingsProvider settingsProvider = plugin.getSettingsProvider();
        Player targetPlayer = getPlayer(store, targetRef);

        ParsedAction parsedAction = parseAction(actionArg.get(ctx), valueArg.provided(ctx) ? valueArg.get(ctx) : null);
        String action = parsedAction.action();
        String value = parsedAction.value();

        switch (action)
        {
            case "start" -> handleStart(plugin, stateProvider, targetPlayer, value);
            case "stop" -> handleStop(plugin, stateProvider, targetPlayer, world);
            case "fallprotection" -> handleFallProtection(settingsProvider, value);
            case "status", "list" -> { }
            default -> { }
        }
    }

    private static void handleStart(OneBlockPlugin plugin,
                                    OneBlockExpeditionStateProvider stateProvider,
                                    Player targetPlayer,
                                    String expeditionId)
    {
        if (expeditionId == null || expeditionId.isBlank() || "-".equals(expeditionId.trim()))
        {
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

    }

    private static void handleStop(OneBlockPlugin plugin,
                                   OneBlockExpeditionStateProvider stateProvider,
                                   Player targetPlayer,
                                   World world)
    {
        if (!stateProvider.hasActiveExpedition())
        {
            return;
        }

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

    }

    private static void handleFallProtection(OneBlockSettingsProvider settingsProvider,
                                             String value)
    {
        if (settingsProvider == null)
        {
            return;
        }

        if (value == null || value.isBlank())
        {
            return;
        }

        Boolean enabled = parseBoolean(value);
        if (enabled == null)
        {
            return;
        }

        settingsProvider.setFallProtectionEnabled(enabled);
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

    private record ParsedAction(String action, String value) {}
}
