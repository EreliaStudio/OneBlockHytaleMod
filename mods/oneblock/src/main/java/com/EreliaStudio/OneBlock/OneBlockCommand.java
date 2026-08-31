package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import org.joml.Vector3i;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Locale;

public final class OneBlockCommand extends AbstractTargetPlayerCommand
{
    private final RequiredArg<String> actionArg;
    private final OptionalArg<String> valueArg;

    public OneBlockCommand()
    {
        super("oneblock", "Create and join isolated OneBlock worlds.");
        this.actionArg = this.withRequiredArg("action", "create|join|status|start|stop|list|fallProtection=true|false|falloffHeight=X", ArgTypes.STRING);
        this.valueArg = this.withOptionalArg("value", "World name, expedition ID, fall protection, or falloff height", ArgTypes.STRING);
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

        ParsedAction parsedAction = parseAction(actionArg.get(ctx), valueArg.provided(ctx) ? valueArg.get(ctx) : null);
        String action = parsedAction.action();
        String value = parsedAction.value();

        if ("create".equals(action))
        {
            handleCreate(ctx, plugin, targetPlayerRef, value);
            return;
        }
        if ("join".equals(action))
        {
            handleJoin(ctx, plugin, targetPlayerRef, value);
            return;
        }

        OneBlockWorldStateRegistry stateRegistry = plugin.getWorldStateRegistry();
        if (stateRegistry == null)
        {
            return;
        }
        if ("list".equals(action))
        {
            ctx.sendMessage(Message.raw("OneBlock worlds: " + String.join(", ", stateRegistry.getManagedWorldNames())));
            return;
        }
        if ("fallprotection".equals(action))
        {
            handleFallProtection(plugin.getSettingsProvider(), value);
            return;
        }
        if ("falloffheight".equals(action))
        {
            handleFalloffHeight(ctx, plugin.getSettingsProvider(), world, value);
            return;
        }
        if (!stateRegistry.isManaged(world))
        {
            ctx.sendMessage(Message.raw("This command action requires a managed OneBlock world."));
            return;
        }

        OneBlockExpeditionStateProvider stateProvider = stateRegistry.expeditionState(world);
        switch (action)
        {
            case "start" -> handleStart(plugin, stateRegistry, stateProvider, world, value);
            case "stop" -> handleStop(plugin, stateRegistry, stateProvider, world);
            case "status" -> handleStatus(ctx, stateRegistry, world);
            default -> ctx.sendMessage(Message.raw("Unknown OneBlock action: " + action));
        }
    }

    private static void handleCreate(CommandContext ctx,
                                     OneBlockPlugin plugin,
                                     PlayerRef targetPlayerRef,
                                     String worldName)
    {
        CommandSender sender = ctx.sender();
        try
        {
            plugin.getWorldService()
                    .createExpeditionWorld(worldName, List.of(targetPlayerRef))
                    .whenComplete((createdWorld, error) ->
                    {
                        if (error != null)
                        {
                            sender.sendMessage(Message.raw("Could not create OneBlock world: " + rootMessage(error)));
                            return;
                        }
                        sender.sendMessage(Message.raw(
                                "Created isolated OneBlock world '" + createdWorld.getName() + "' and moved "
                                        + targetPlayerRef.getUsername() + "."
                        ));
                    });
        }
        catch (Exception exception)
        {
            sender.sendMessage(Message.raw("Could not create OneBlock world: " + rootMessage(exception)));
        }
    }

    private static void handleJoin(CommandContext ctx,
                                   OneBlockPlugin plugin,
                                   PlayerRef targetPlayerRef,
                                   String worldName)
    {
        if (worldName == null || worldName.isBlank())
        {
            ctx.sendMessage(Message.raw("A world name is required."));
            return;
        }

        if (!plugin.getWorldStateRegistry().isManaged(worldName.trim()))
        {
            ctx.sendMessage(Message.raw("No OneBlock world named '" + worldName.trim() + "'."));
            return;
        }

        CommandSender sender = ctx.sender();
        plugin.getWorldService().movePlayers(worldName.trim(), List.of(targetPlayerRef))
                .whenComplete((targetWorld, error) ->
                {
                    if (error != null)
                    {
                        sender.sendMessage(Message.raw("Could not move player: " + rootMessage(error)));
                    }
                    else
                    {
                        sender.sendMessage(Message.raw(
                                "Moved " + targetPlayerRef.getUsername() + " to '" + targetWorld.getName() + "'."
                        ));
                    }
                });
    }

    private static void handleStart(OneBlockPlugin plugin,
                                    OneBlockWorldStateRegistry stateRegistry,
                                    OneBlockExpeditionStateProvider stateProvider,
                                    World world,
                                    String expeditionId)
    {
        if (expeditionId == null || expeditionId.isBlank() || "-".equals(expeditionId.trim()))
        {
            return;
        }

        String normalizedExpeditionId = expeditionId.trim();
        int ticks = OneBlockExpeditionDefaults.getTicks(normalizedExpeditionId);

        stateRegistry.dungeonState(world).endDungeon();
        stateProvider.startExpedition(normalizedExpeditionId, ticks);

        OneBlockWorldPlayers.forEach(
                world,
                worldPlayer -> plugin.getHudService().showExpeditionStarted(
                    worldPlayer,
                    normalizedExpeditionId,
                    ticks
                )
        );

    }

    private static void handleStop(OneBlockPlugin plugin,
                                   OneBlockWorldStateRegistry stateRegistry,
                                   OneBlockExpeditionStateProvider stateProvider,
                                   World world)
    {
        OneBlockDungeonStateProvider dungeonState = stateRegistry.dungeonState(world);
        if (!stateProvider.hasActiveExpedition() && !dungeonState.isDungeonActive())
        {
            return;
        }

        stateProvider.endExpedition();
        dungeonState.endDungeon();

        if (world != null)
        {
            Vector3i pos = OneBlockBlockIds.ONEBLOCK_POSITION;
            world.execute(() -> world.setBlock(pos.x(), pos.y(), pos.z(), OneBlockBlockIds.DEFAULT_BLOCK_ID));
        }

        OneBlockWorldPlayers.forEach(world, plugin.getHudService()::clear);

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

    private static void handleFalloffHeight(CommandContext ctx,
                                            OneBlockSettingsProvider settingsProvider,
                                            World world,
                                            String value)
    {
        if (settingsProvider == null || world == null)
        {
            return;
        }
        if (value == null || value.isBlank())
        {
            ctx.sendMessage(Message.raw("A numeric falloff height is required."));
            return;
        }

        final double height;
        try
        {
            height = Double.parseDouble(value.trim());
        }
        catch (NumberFormatException exception)
        {
            ctx.sendMessage(Message.raw("Invalid falloff height: " + value));
            return;
        }

        if (!Double.isFinite(height))
        {
            ctx.sendMessage(Message.raw("Falloff height must be a finite number."));
            return;
        }

        settingsProvider.setFalloffHeight(world.getName(), height);
        ctx.sendMessage(Message.raw(
                "Falloff height for world '" + world.getName() + "' set to " + formatHeight(height) + "."
        ));
    }

    private static String formatHeight(double height)
    {
        return height == Math.rint(height) ? Long.toString((long) height) : Double.toString(height);
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

    private static void handleStatus(CommandContext ctx,
                                     OneBlockWorldStateRegistry stateRegistry,
                                     World world)
    {
        OneBlockDungeonStateProvider dungeon = stateRegistry.dungeonState(world);
        if (dungeon.isDungeonActive())
        {
            ctx.sendMessage(Message.raw(
                    "World '" + world.getName() + "': dungeon " + dungeon.getActiveDungeonId()
                            + ", wave " + (dungeon.getCurrentWaveIndex() + 1)
            ));
            return;
        }

        OneBlockExpeditionStateProvider expedition = stateRegistry.expeditionState(world);
        if (expedition.hasActiveExpedition())
        {
            ctx.sendMessage(Message.raw(
                    "World '" + world.getName() + "': expedition " + expedition.getActiveExpeditionId()
                            + ", " + expedition.getTicksRemaining() + "/" + expedition.getTotalTicks() + " blocks remaining"
            ));
            return;
        }

        ctx.sendMessage(Message.raw("World '" + world.getName() + "': no active expedition."));
    }

    private static String rootMessage(Throwable error)
    {
        Throwable current = error;
        while (current.getCause() != null)
        {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
    }

    private record ParsedAction(String action, String value) {}
}
