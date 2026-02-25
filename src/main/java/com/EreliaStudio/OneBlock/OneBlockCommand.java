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
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public final class OneBlockCommand extends AbstractTargetPlayerCommand
{
    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> idArg;

    public OneBlockCommand()
    {
        super("oneblock", "Manage OneBlock unlocks/drops for a player.");

        // Usage:
        // /oneblock unlock Soil_Sand --player Bob
        // /oneblock lock Soil_Sand --player Bob
        // /oneblock enable Soil_Sand --player Bob
        // /oneblock disable Soil_Sand --player Bob
        // /oneblock status Soil_Sand --player Bob
        //
        // Also accepts: oneblock.unlock.Soil_Sand.name (will map to Soil_Sand)
        this.actionArg = this.withRequiredArg("action", "unlock|lock|enable|disable|status", ArgTypes.STRING);
        this.idArg = this.withRequiredArg("id", "Drop item id (ex: Soil_Sand) or unlock key (ex: oneblock.unlock.Soil_Sand.name)", ArgTypes.STRING);
    }

    @Override
    protected void execute(@Nonnull CommandContext ctx,
                           @Nonnull Ref<EntityStore> senderRef,
                           @Nonnull Ref<EntityStore> targetRef,
                           @Nonnull PlayerRef targetPlayerRef,
                           @Nonnull World world,
                           @Nonnull Store<EntityStore> store)
    {
        OneBlockDropsStateProvider provider = OneBlockPlugin.getInstance().getDropsStateProvider();

        String action = safeLower(actionArg.get(ctx));
        String rawId = idArg.get(ctx);

        String dropItemId = normalizeToDropItemId(rawId);
        if (dropItemId == null || dropItemId.isEmpty())
        {
            ctx.sendMessage(Message.raw("Invalid id: " + rawId));
            return;
        }

        UUID targetId = targetPlayerRef.getUuid();

        switch (action)
        {
            case "unlock":
                handleUnlock(ctx, provider, targetPlayerRef, targetId, dropItemId);
                return;

            case "lock":
                handleLock(ctx, provider, targetPlayerRef, targetId, dropItemId);
                return;

            case "enable":
                handleEnable(ctx, provider, targetPlayerRef, targetId, dropItemId);
                return;

            case "disable":
                handleDisable(ctx, provider, targetPlayerRef, targetId, dropItemId);
                return;

            case "status":
                handleStatus(ctx, provider, targetPlayerRef, targetId, dropItemId);
                return;

            default:
                ctx.sendMessage(Message.raw("Unknown action: " + action + " (expected unlock|lock|enable|disable|status)"));
        }
    }

    private static void handleUnlock(CommandContext ctx,
                                     OneBlockDropsStateProvider provider,
                                     PlayerRef targetPlayerRef,
                                     UUID targetId,
                                     String dropItemId)
    {
        if (OneBlockDropRegistry.DEFAULT_ITEM_ID.equals(dropItemId))
        {
            ctx.sendMessage(Message.raw("Already available by default: " + dropItemId));
            return;
        }

        boolean ok = provider.unlock(targetId, dropItemId);
        ctx.sendMessage(Message.raw(ok
                ? "Unlocked " + dropItemId + " for " + targetPlayerRef.getUsername()
                : "Already unlocked (or failed): " + dropItemId));
    }

    private static void handleLock(CommandContext ctx,
                                   OneBlockDropsStateProvider provider,
                                   PlayerRef targetPlayerRef,
                                   UUID targetId,
                                   String dropItemId)
    {
        if (OneBlockDropRegistry.DEFAULT_ITEM_ID.equals(dropItemId))
        {
            ctx.sendMessage(Message.raw("Cannot lock the default drop: " + dropItemId));
            return;
        }

        boolean ok = provider.lock(targetId, dropItemId);
        ctx.sendMessage(Message.raw(ok
                ? "Locked " + dropItemId + " for " + targetPlayerRef.getUsername()
                : "Was not unlocked (or failed): " + dropItemId));
    }

    private static void handleEnable(CommandContext ctx,
                                     OneBlockDropsStateProvider provider,
                                     PlayerRef targetPlayerRef,
                                     UUID targetId,
                                     String dropItemId)
    {
        if (!provider.isUnlocked(targetId, dropItemId) && !OneBlockDropRegistry.DEFAULT_ITEM_ID.equals(dropItemId))
        {
            ctx.sendMessage(Message.raw("Not unlocked: " + dropItemId));
            return;
        }

        boolean ok = provider.setEnabled(targetId, dropItemId, true);
        ctx.sendMessage(Message.raw(ok
                ? "Enabled " + dropItemId + " for " + targetPlayerRef.getUsername()
                : "Failed to enable: " + dropItemId));
    }

    private static void handleDisable(CommandContext ctx,
                                      OneBlockDropsStateProvider provider,
                                      PlayerRef targetPlayerRef,
                                      UUID targetId,
                                      String dropItemId)
    {
        if (OneBlockDropRegistry.DEFAULT_ITEM_ID.equals(dropItemId))
        {
            ctx.sendMessage(Message.raw("Cannot disable the default drop: " + dropItemId));
            return;
        }

        boolean ok = provider.setEnabled(targetId, dropItemId, false);
        ctx.sendMessage(Message.raw(ok
                ? "Disabled " + dropItemId + " for " + targetPlayerRef.getUsername()
                : "Failed to disable: " + dropItemId));
    }

    private static void handleStatus(CommandContext ctx,
                                     OneBlockDropsStateProvider provider,
                                     PlayerRef targetPlayerRef,
                                     UUID targetId,
                                     String dropItemId)
    {
        boolean unlocked = provider.isUnlocked(targetId, dropItemId) || OneBlockDropRegistry.DEFAULT_ITEM_ID.equals(dropItemId);
        List<String> enabled = provider.getEnabledDrops(targetId);
        boolean isEnabled = enabled.contains(dropItemId);

        ctx.sendMessage(Message.raw("Status for " + targetPlayerRef.getUsername() + " / " + dropItemId
                + " | unlocked=" + unlocked
                + " enabled=" + isEnabled));
    }

    private static String safeLower(String s)
    {
        if (s == null)
        {
            return "";
        }
        return s.toLowerCase(Locale.ROOT).trim();
    }

    /**
     * Accepts:
     * - Soil_Sand
     * - oneblock.unlock.Soil_Sand.name
     * - oneblock.unlock.Soil_Sand.description
     *
     * Converts the latter forms to: Soil_Sand
     */
    private static String normalizeToDropItemId(String raw)
    {
        if (raw == null)
        {
            return null;
        }

        String s = raw.trim();
        if (s.isEmpty())
        {
            return s;
        }

        String prefix = "oneblock.unlock.";
        if (!s.startsWith(prefix))
        {
            return s;
        }

        String remainder = s.substring(prefix.length()); // Soil_Sand.name
        int dot = remainder.indexOf('.');
        if (dot <= 0)
        {
            return remainder;
        }

        return remainder.substring(0, dot); // Soil_Sand
    }
}