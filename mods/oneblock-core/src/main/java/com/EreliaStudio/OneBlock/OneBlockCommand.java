package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
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
        this.valueArg  = this.withRequiredArg("value",  "Expedition ID (for start), or '-' (for status/stop/list)", ArgTypes.STRING);
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
        OneBlockExpeditionStateProvider stateProvider = plugin.getExpeditionStateProvider();
        OneBlockDropRegistry dropRegistry = plugin.getDropRegistry();

        String action = safeLower(actionArg.get(ctx));
        String value  = valueArg.get(ctx);

        switch (action)
        {
            case "status" -> handleStatus(ctx, stateProvider);
            case "start"  -> handleStart(ctx, stateProvider, value);
            case "stop"   -> handleStop(ctx, stateProvider, world);
            case "list"   -> handleList(ctx, dropRegistry, value);
            default       -> ctx.sendMessage(Message.raw("Unknown action: " + action + " (expected status|start|stop|list)"));
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

    private static void handleStart(CommandContext ctx, OneBlockExpeditionStateProvider stateProvider, String expeditionId)
    {
        if (expeditionId == null || expeditionId.isBlank() || "-".equals(expeditionId.trim()))
        {
            ctx.sendMessage(Message.raw("Usage: /oneblock - start <expeditionId>"));
            return;
        }
        stateProvider.startExpedition(expeditionId.trim(), 100);
        ctx.sendMessage(Message.raw("Started expedition '" + expeditionId.trim() + "' with 100 ticks."));
    }

    private static void handleStop(CommandContext ctx, OneBlockExpeditionStateProvider stateProvider, World world)
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
            world.execute(() -> world.setBlock(0, 100, 0, OneBlockBlockIds.DEFAULT_BLOCK_ID));
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

    private static String safeLower(String s)
    {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
    }
}
