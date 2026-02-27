package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class OneBlockCommand extends AbstractTargetPlayerCommand
{
    private final RequiredArg<String> actionArg;
    private final RequiredArg<String> idArg;

    public OneBlockCommand()
    {
        super("oneblock", "Manage OneBlock expedition unlocks/drops for a player.");

        // Usage:
        // /oneblock unlock Soil_Sand --player Bob
        // /oneblock lock Soil_Sand --player Bob
        // /oneblock status Soil_Sand --player Bob
        // /oneblock list --player Bob
        //
        // Also accepts: oneblock.unlock.Soil_Sand.name (will map to Soil_Sand)
        this.actionArg = this.withRequiredArg("action", "unlock|lock|status|consume|list", ArgTypes.STRING);
        this.idArg = this.withRequiredArg(
                "id",
                "Drop id (ex: Soil_Grass or entity:Sheep) or consumable item id (ex: OneBlock_Unlock_Soil_Grass). "
                        + "Ignored for list.",
                ArgTypes.STRING
        );
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
        OneBlockDropsStateProvider provider = plugin.getDropsStateProvider();
        OneBlockDropRegistry dropRegistry = plugin.getDropRegistry();

        String action = safeLower(actionArg.get(ctx));
        String rawId = idArg.get(ctx);

        UUID targetId = targetPlayerRef.getUuid();

        if ("consume".equals(action))
        {
            handleConsume(ctx, plugin.getUnlockService(), targetPlayerRef, targetId, rawId);
            return;
        }
        if ("list".equals(action))
        {
            handleList(ctx, provider, dropRegistry, targetPlayerRef, world);
            return;
        }

        String dropItemId = normalizeToDropItemId(rawId);
        if (dropItemId == null || dropItemId.isEmpty())
        {
            ctx.sendMessage(Message.raw("Invalid id: " + rawId));
            return;
        }

        switch (action)
        {
            case "unlock":
            {
                handleUnlock(ctx, provider, targetPlayerRef, targetId, dropItemId);
                return;
            }

            case "lock":
            {
                handleLock(ctx, provider, targetPlayerRef, targetId, dropItemId);
                return;
            }

            case "status":
            {
                handleStatus(ctx, provider, targetPlayerRef, targetId, dropItemId);
                return;
            }

            default:
                ctx.sendMessage(Message.raw("Unknown action: " + action + " (expected unlock|lock|status|consume)"));
        }
    }

    private static void handleUnlock(CommandContext ctx,
                                     OneBlockDropsStateProvider provider,
                                     PlayerRef targetPlayerRef,
                                     UUID targetId,
                                     String dropItemId)
    {
        if (OneBlockExpeditionDefaults.isDefaultDrop(OneBlockExpeditionResolver.DEFAULT_EXPEDITION, dropItemId))
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
        if (OneBlockExpeditionDefaults.isDefaultDrop(OneBlockExpeditionResolver.DEFAULT_EXPEDITION, dropItemId))
        {
            ctx.sendMessage(Message.raw("Cannot lock the default drop: " + dropItemId));
            return;
        }

        boolean ok = provider.lock(targetId, dropItemId);
        ctx.sendMessage(Message.raw(ok
                ? "Locked " + dropItemId + " for " + targetPlayerRef.getUsername()
                : "Was not unlocked (or failed): " + dropItemId));
    }

    private static void handleStatus(CommandContext ctx,
                                     OneBlockDropsStateProvider provider,
                                     PlayerRef targetPlayerRef,
                                     UUID targetId,
                                     String dropItemId)
    {
        boolean unlocked = provider.isUnlocked(targetId, dropItemId)
                || OneBlockExpeditionDefaults.isDefaultDrop(OneBlockExpeditionResolver.DEFAULT_EXPEDITION, dropItemId);

        ctx.sendMessage(Message.raw("Status for " + targetPlayerRef.getUsername() + " / " + dropItemId
                + " | unlocked=" + unlocked));
    }

    private static void handleConsume(CommandContext ctx,
                                      OneBlockUnlockService unlockService,
                                      PlayerRef targetPlayerRef,
                                      UUID targetId,
                                      String consumableItemId)
    {
        if (unlockService == null)
        {
            ctx.sendMessage(Message.raw("Unlock service is not available."));
            return;
        }

        OneBlockUnlockService.UnlockConsumeResult result = unlockService.consume(targetId, consumableItemId);
        switch (result)
        {
            case UNLOCKED:
            {
                String unlockedDrop = unlockService.getDropItemIdForConsumable(consumableItemId);
                ctx.sendMessage(Message.raw("Consumed " + consumableItemId + " for " + targetPlayerRef.getUsername()
                        + ". Unlocked drop: " + unlockedDrop));
                return;
            }

            case ALREADY_UNLOCKED:
                ctx.sendMessage(Message.raw("Cannot consume " + consumableItemId + ": this unlock is already known by "
                        + targetPlayerRef.getUsername()));
                return;

            case INVALID_ITEM:
                ctx.sendMessage(Message.raw("Not a OneBlock unlock consumable: " + consumableItemId));
                return;

            case UNLOCK_FAILED:
            default:
                ctx.sendMessage(Message.raw("Failed to consume unlock item: " + consumableItemId));
        }
    }

    private static void handleList(CommandContext ctx,
                                   OneBlockDropsStateProvider provider,
                                   OneBlockDropRegistry dropRegistry,
                                   PlayerRef targetPlayerRef,
                                   World world)
    {
        if (provider == null || dropRegistry == null || targetPlayerRef == null)
        {
            ctx.sendMessage(Message.raw("List failed: missing services."));
            return;
        }

        String expeditionId = resolveActiveExpedition(world);

        Set<String> unlockedSet = new HashSet<>(provider.getUnlockedDrops(targetPlayerRef.getUuid(), expeditionId));
        List<String> knownDrops = dropRegistry.getKnownDrops(expeditionId);
        if (knownDrops.isEmpty())
        {
            knownDrops = new ArrayList<>(unlockedSet);
            knownDrops.sort(String::compareToIgnoreCase);
        }

        List<String> unlockedDrops = new ArrayList<>();
        List<String> lockedDrops = new ArrayList<>();
        List<String> unlockedExchanges = new ArrayList<>();
        List<String> lockedExchanges = new ArrayList<>();

        for (String dropId : knownDrops)
        {
            if (dropId == null || dropId.isEmpty())
            {
                continue;
            }

            boolean isExchange = OneBlockExchangeService.isExchangeUnlockId(dropId);
            boolean unlocked = unlockedSet.contains(dropId)
                    || OneBlockExpeditionDefaults.isDefaultDrop(expeditionId, dropId);

            if (isExchange)
            {
                if (unlocked)
                {
                    unlockedExchanges.add(dropId);
                }
                else
                {
                    lockedExchanges.add(dropId);
                }
                continue;
            }

            if (unlocked)
            {
                unlockedDrops.add(dropId);
            }
            else
            {
                lockedDrops.add(dropId);
            }
        }

        ctx.sendMessage(Message.raw("OneBlock expedition for " + targetPlayerRef.getUsername() + ": " + expeditionId));
        ctx.sendMessage(Message.raw("Unlocked drops (" + unlockedDrops.size() + "): " + joinOrDash(unlockedDrops)));
        ctx.sendMessage(Message.raw("Locked drops (" + lockedDrops.size() + "): " + joinOrDash(lockedDrops)));
        if (!unlockedExchanges.isEmpty() || !lockedExchanges.isEmpty())
        {
            ctx.sendMessage(Message.raw("Unlocked exchanges (" + unlockedExchanges.size() + "): " + joinOrDash(unlockedExchanges)));
            ctx.sendMessage(Message.raw("Locked exchanges (" + lockedExchanges.size() + "): " + joinOrDash(lockedExchanges)));
        }
    }

    private static String resolveActiveExpedition(World world)
    {
        if (world == null)
        {
            return OneBlockExpeditionResolver.DEFAULT_EXPEDITION;
        }

        BlockType blockType = world.getBlockType(
                OneBlockWorldInitializer.ORIGIN_BLOCK.getX(),
                OneBlockWorldInitializer.ORIGIN_BLOCK.getY(),
                OneBlockWorldInitializer.ORIGIN_BLOCK.getZ()
        );

        if (!OneBlockBlockUtil.isOneBlock(blockType))
        {
            return OneBlockExpeditionResolver.DEFAULT_EXPEDITION;
        }

        return OneBlockExpeditionResolver.expeditionFromBlockType(blockType);
    }

    private static String joinOrDash(List<String> values)
    {
        if (values == null || values.isEmpty())
        {
            return "-";
        }
        return String.join(", ", values);
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
