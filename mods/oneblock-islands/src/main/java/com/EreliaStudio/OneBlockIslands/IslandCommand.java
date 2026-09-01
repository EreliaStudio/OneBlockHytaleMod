package com.EreliaStudio.OneBlockIslands;

import com.EreliaStudio.OneBlock.OneBlockPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

final class IslandCommand extends AbstractPlayerCommand {
    private final OptionalArg<String> action;
    private final OptionalArg<String> first;
    private final OptionalArg<String> second;
    private final OptionalArg<String> third;
    private final IslandStore islands;

    IslandCommand(IslandStore islands) {
        super("island", "Create, enter, or manage your OneBlock island.");
        requireNoPermission();
        setAllowsExtraArguments(false);
        action = withOptionalArg("action", "home|members|invite|accept|kick|leave|admin", ArgTypes.STRING);
        first = withOptionalArg("first", "Player or admin action", ArgTypes.STRING);
        second = withOptionalArg("second", "Player", ArgTypes.STRING);
        third = withOptionalArg("third", "Confirmation", ArgTypes.STRING);
        this.islands = islands;
    }

    @Override protected void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store,
                                     @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef player, @Nonnull World world) {
        String verb = value(action, ctx, "home").toLowerCase(Locale.ROOT);
        try {
            switch (verb) {
                case "home" -> home(player);
                case "members" -> members(player);
                case "invite" -> invite(player, required(first, ctx, "player"));
                case "accept" -> accept(player);
                case "kick" -> kick(player, required(first, ctx, "player"));
                case "leave" -> leave(player);
                case "admin" -> admin(player, value(first, ctx, ""), value(second, ctx, ""), value(third, ctx, ""));
                default -> player.sendMessage(Message.raw("Usage: /island [home|members|invite <player>|accept|kick <player>|leave|admin ...]"));
            }
        } catch (Exception e) {
            player.sendMessage(Message.raw("Island action failed: " + rootMessage(e)));
        }
    }

    private void home(PlayerRef player) throws IOException {
        IslandRecord existing = islands.findByPlayer(player.getUuid()).orElse(null);
        if (existing != null) { move(player, existing); return; }
        IslandRecord created = islands.create(player.getUuid());
        player.sendMessage(Message.raw("Creating your OneBlock island..."));
        OneBlockPlugin.getInstance().getWorldService().createExpeditionWorld(created.worldName(), List.of(player))
                .whenComplete((world, error) -> {
                    if (error == null) player.sendMessage(Message.raw("Island created: " + created.worldName()));
                    else {
                        try { islands.rollbackCreate(created); } catch (IOException ignored) {}
                        player.sendMessage(Message.raw("Island creation failed: " + rootMessage(error)));
                    }
                });
    }

    private void move(PlayerRef player, IslandRecord island) {
        OneBlockPlugin.getInstance().getWorldService().movePlayers(island.worldName(), List.of(player))
                .whenComplete((world, error) -> player.sendMessage(Message.raw(error == null
                        ? "Welcome to " + island.worldName() : "Could not enter island: " + rootMessage(error))));
    }

    private void members(PlayerRef player) {
        IslandRecord island = islands.findByPlayer(player.getUuid()).orElseThrow(() -> new IllegalStateException("You do not belong to an island"));
        player.sendMessage(Message.raw("Owner: " + island.ownerUuid() + " | Members: " + island.members()));
    }

    private void invite(PlayerRef owner, String name) throws IOException {
        PlayerRef target = online(name);
        islands.invite(owner.getUuid(), target.getUuid());
        target.sendMessage(Message.raw(owner.getUsername() + " invited you to an island. Use /island accept."));
        owner.sendMessage(Message.raw("Invited " + target.getUsername() + "."));
    }

    private void accept(PlayerRef player) throws IOException {
        IslandRecord island = islands.accept(player.getUuid());
        player.sendMessage(Message.raw("Island invite accepted."));
        move(player, island);
    }

    private void kick(PlayerRef owner, String name) throws IOException {
        PlayerRef target = online(name);
        islands.kick(owner.getUuid(), target.getUuid());
        owner.sendMessage(Message.raw("Removed " + target.getUsername() + " from your island."));
        OneBlockIslandsPlugin.getInstance().redirectIfUnauthorized(target);
    }

    private void leave(PlayerRef player) throws IOException {
        islands.leave(player.getUuid());
        player.sendMessage(Message.raw("You left the island."));
        OneBlockIslandsPlugin.getInstance().redirectIfUnauthorized(player);
    }

    private void admin(PlayerRef sender, String operation, String ownerName, String playerOrConfirm) throws IOException {
        if (!sender.hasPermission(IslandAccess.ADMIN_PERMISSION)) throw new IllegalStateException("Missing permission " + IslandAccess.ADMIN_PERMISSION);
        PlayerRef owner = online(ownerName);
        switch (operation.toLowerCase(Locale.ROOT)) {
            case "info" -> {
                IslandRecord island = islands.findByPlayer(owner.getUuid()).orElseThrow(() -> new IllegalStateException("Player has no island"));
                sender.sendMessage(Message.raw(island.worldName() + " owner=" + island.ownerUuid() + " members=" + island.members()));
            }
            case "create" -> { if (islands.findByPlayer(owner.getUuid()).isPresent()) throw new IllegalStateException("Player already has an island"); home(owner); }
            case "add" -> { PlayerRef member = online(playerOrConfirm); islands.adminAdd(owner.getUuid(), member.getUuid()); sender.sendMessage(Message.raw("Member added.")); }
            case "remove" -> { PlayerRef member = online(playerOrConfirm); islands.adminRemove(owner.getUuid(), member.getUuid()); redirect(member); sender.sendMessage(Message.raw("Member removed.")); }
            case "delete" -> {
                if (!"confirm".equalsIgnoreCase(playerOrConfirm)) throw new IllegalStateException("Repeat as /island admin delete <owner> confirm; world files are retained");
                islands.deleteMetadata(owner.getUuid());
                sender.sendMessage(Message.raw("Ownership metadata deleted; world files were retained."));
            }
            default -> throw new IllegalStateException("Usage: /island admin info|create <player>, add|remove <owner> <player>, delete <owner> confirm");
        }
    }

    private static void redirect(PlayerRef player) { OneBlockIslandsPlugin.getInstance().redirectIfUnauthorized(player); }
    private static PlayerRef online(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Player name is required");
        PlayerRef player = Universe.get().getPlayerByUsername(name, NameMatching.EXACT_IGNORE_CASE);
        if (player == null) throw new IllegalArgumentException("Player must be online: " + name);
        return player;
    }
    private static String value(OptionalArg<String> arg, CommandContext ctx, String fallback) { return arg.provided(ctx) ? arg.get(ctx) : fallback; }
    private static String required(OptionalArg<String> arg, CommandContext ctx, String label) { String value=value(arg,ctx,""); if(value.isBlank()) throw new IllegalArgumentException(label+" is required"); return value; }
    private static String rootMessage(Throwable error) { Throwable root=error; while(root.getCause()!=null) root=root.getCause(); return root.getMessage()==null?root.getClass().getSimpleName():root.getMessage(); }
}
