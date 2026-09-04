package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.UUID;

/** Administrative actions for a player's position-scoped OneBlock state. */
public final class OneBlockCommand extends AbstractTargetPlayerCommand {
    private final RequiredArg<String> actionArg;
    private final OptionalArg<String> valueArg;

    public OneBlockCommand() {
        super("oneblock", "Inspect or control a player's OneBlock in the current world.");
        actionArg = withRequiredArg("action", "status|start|stop", ArgTypes.STRING);
        valueArg = withOptionalArg("value", "Expedition ID", ArgTypes.STRING);
    }

    @Override protected void execute(@Nonnull CommandContext ctx,
                                     @Nonnull Ref<EntityStore> senderRef,
                                     @Nonnull Ref<EntityStore> targetRef,
                                     @Nonnull PlayerRef target,
                                     @Nonnull World world,
                                     @Nonnull Store<EntityStore> store) {
        OneBlockPlugin plugin = OneBlockPlugin.getInstance();
        OneBlockRootRegistry roots = plugin == null ? null : plugin.getRootRegistry();
        // Commands target the selected player's own roots, including when
        // that player is a guest inside another owner's island world.
        UUID ownerId = target.getUuid();
        if (roots == null || !roots.hasRoots(world, ownerId)) {
            ctx.sendMessage(Message.raw(target.getUsername() + " has no OneBlock in this world."));
            return;
        }

        String action = actionArg.get(ctx).trim().toLowerCase(Locale.ROOT);
        OneBlockExpeditionStateProvider expedition = roots.expeditionState(world, ownerId);
        OneBlockDungeonStateProvider dungeon = roots.dungeonState(world, ownerId);
        switch (action) {
            case "start" -> start(ctx, plugin, roots, world, target, ownerId, expedition, dungeon);
            case "stop" -> stop(plugin, roots, world, ownerId, expedition, dungeon);
            case "status" -> status(ctx, target, expedition, dungeon);
            default -> ctx.sendMessage(Message.raw("Unknown OneBlock action: " + action));
        }
    }

    private void start(CommandContext ctx,
                       OneBlockPlugin plugin,
                       OneBlockRootRegistry roots,
                       World world,
                       PlayerRef target,
                       UUID ownerId,
                       OneBlockExpeditionStateProvider expedition,
                       OneBlockDungeonStateProvider dungeon) {
        if (!valueArg.provided(ctx) || valueArg.get(ctx).isBlank()) {
            ctx.sendMessage(Message.raw("An expedition ID is required."));
            return;
        }
        String id = valueArg.get(ctx).trim();
        dungeon.endDungeon();
        int ticks = OneBlockExpeditionDefaults.getTicks(id);
        expedition.startExpedition(id, ticks);
        setBlocks(roots, world, ownerId, OneBlockExpeditionResolver.blockIdForExpedition(id));
        Player actor = target.getComponent(Player.getComponentType());
        plugin.triggerOwnerNodes(world, ownerId, actor, position -> OneBlockTrigger.expedition(
                world.getName(), position, ownerId, id, ticks, ticks, true));
    }

    private static void stop(OneBlockPlugin plugin,
                             OneBlockRootRegistry roots,
                             World world,
                             UUID ownerId,
                             OneBlockExpeditionStateProvider expedition,
                             OneBlockDungeonStateProvider dungeon) {
        String expeditionId = expedition.getActiveExpeditionId();
        int totalTicks = expedition.getTotalTicks();
        String dungeonId = dungeon.getActiveDungeonId();
        int totalWaves = OneBlockDungeonDefaults.getWaveCount(dungeonId);
        expedition.endExpedition();
        dungeon.endDungeon();
        setBlocks(roots, world, ownerId, OneBlockBlockIds.DEFAULT_BLOCK_ID);
        if (dungeonId != null && !dungeonId.isBlank()) {
            plugin.triggerOwnerNodes(world, ownerId, null, position -> OneBlockTrigger.dungeon(
                    world.getName(), position, ownerId, dungeonId,
                    totalWaves, totalWaves, false));
        } else if (expeditionId != null && !expeditionId.isBlank()) {
            plugin.triggerOwnerNodes(world, ownerId, null, position -> OneBlockTrigger.expedition(
                    world.getName(), position, ownerId, expeditionId,
                    0, totalTicks, false));
        }
    }

    private static void status(CommandContext ctx,
                               PlayerRef target,
                               OneBlockExpeditionStateProvider expedition,
                               OneBlockDungeonStateProvider dungeon) {
        if (dungeon.isDungeonActive()) {
            ctx.sendMessage(Message.raw(target.getUsername() + ": dungeon " + dungeon.getActiveDungeonId()
                    + ", wave " + (dungeon.getCurrentWaveIndex() + 1)));
        } else if (expedition.hasActiveExpedition()) {
            ctx.sendMessage(Message.raw(target.getUsername() + ": expedition " + expedition.getActiveExpeditionId()
                    + ", " + expedition.getTicksRemaining() + "/" + expedition.getTotalTicks() + " blocks remaining"));
        } else {
            ctx.sendMessage(Message.raw(target.getUsername() + ": no active expedition."));
        }
    }

    private static void setBlocks(OneBlockRootRegistry roots, World world, UUID ownerId, String blockId) {
        for (Vector3i position : roots.positions(world, ownerId)) {
            world.setBlock(position.x(), position.y(), position.z(), blockId);
        }
    }
}
