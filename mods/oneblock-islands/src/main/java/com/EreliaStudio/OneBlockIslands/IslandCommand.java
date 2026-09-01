package com.EreliaStudio.OneBlockIslands;

import com.EreliaStudio.OneBlock.OneBlockPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** The complete /island command tree. */
final class IslandCommand extends IslandPlayerCommand {
    private final IslandStore islands;

    IslandCommand(IslandStore islands) {
        super("island", "Create, enter, or manage your OneBlock island.");
        this.islands = islands;
        addSubCommand(new HomeCommand());
        addSubCommand(new JoinCommand());
        addSubCommand(new CreateCommand());
        addSubCommand(new MembersCommand());
        addSubCommand(new InviteCommand());
        addSubCommand(new AcceptCommand());
        addSubCommand(new KickCommand());
        addSubCommand(new LeaveCommand());
        addSubCommand(new AdminCommand());
    }

    /** /island is the no-argument variant of /island home. */
    @Override protected void run(CommandContext ctx, PlayerRef player) throws Exception {
        enterOrCreate(player, player);
    }

    private void enterOrCreate(PlayerRef subject, PlayerRef feedback) throws IOException {
        IslandRecord existing = islands.findHome(subject.getUuid()).orElse(null);
        if (existing != null) {
            move(subject, existing, feedback);
            return;
        }

        // create() synchronizes the membership check, reservation, and durable write.
        createIsland(subject, feedback, IslandStore.HOME_NAME);
    }

    private void createIsland(PlayerRef subject, PlayerRef feedback, String name) throws IOException {
        IslandRecord reserved = islands.create(subject.getUuid(), name);
        feedback.sendMessage(Message.raw("Creating an island for " + subject.getUsername() + "..."));
        try {
            OneBlockPlugin.getInstance().getWorldService().createExpeditionWorld(reserved.worldName(), List.of(subject))
                    .whenComplete((createdWorld, error) -> {
                        if (error == null) {
                            feedback.sendMessage(Message.raw("Island created: " + reserved.name() + "."));
                            if (feedback != subject) subject.sendMessage(Message.raw("Your island was created by staff."));
                            return;
                        }
                        Universe universe = Universe.get();
                        if (universe != null && (universe.getWorld(reserved.worldName()) != null
                                || universe.isWorldLoadable(reserved.worldName()))) {
                            feedback.sendMessage(Message.raw("Island reserved, but it could not be entered: "
                                    + rootMessage(error) + ". Use " + retryCommand(subject, reserved) + " to try again."));
                            return;
                        }
                        try {
                            islands.rollbackCreate(reserved);
                        } catch (IOException rollbackError) {
                            error.addSuppressed(rollbackError);
                        }
                        feedback.sendMessage(Message.raw("Island creation failed: " + rootMessage(error)));
                    });
        } catch (RuntimeException error) {
            islands.rollbackCreate(reserved);
            throw error;
        }
    }

    private static void move(PlayerRef subject, IslandRecord island, PlayerRef feedback) {
        OneBlockPlugin.getInstance().getWorldService().movePlayers(island.worldName(), List.of(subject))
                .whenComplete((targetWorld, error) -> feedback.sendMessage(Message.raw(error == null
                        ? "Moved " + subject.getUsername() + " to " + island.name() + "."
                        : "Could not enter island: " + rootMessage(error))));
    }

    private static String retryCommand(PlayerRef owner, IslandRecord island) {
        return IslandStore.HOME_NAME.equalsIgnoreCase(island.name())
                ? "/island home"
                : "/island join " + owner.getUsername() + " " + island.name();
    }

    private final class HomeCommand extends IslandPlayerCommand {
        HomeCommand() { super("home", "Enter your island, creating it if necessary."); }
        @Override protected void run(CommandContext ctx, PlayerRef player) throws Exception { enterOrCreate(player, player); }
    }

    private final class JoinCommand extends IslandPlayerCommand {
        private final RequiredArg<PlayerRef> ownerArg;
        private final OptionalArg<String> islandNameArg;

        JoinCommand() {
            super("join", "Join an island you own or belong to.");
            ownerArg = withRequiredArg("owner", "Online island owner", ArgTypes.PLAYER_REF);
            islandNameArg = withOptionalArg("island", "Island name (defaults to Home)", ArgTypes.STRING);
        }

        @Override protected void run(CommandContext ctx, PlayerRef player) {
            PlayerRef owner = ownerArg.get(ctx);
            String name = islandNameArg.provided(ctx) ? islandNameArg.get(ctx) : IslandStore.HOME_NAME;
            IslandRecord island = islands.findOwned(owner.getUuid(), name)
                    .orElseThrow(() -> new IllegalStateException(owner.getUsername() + " has no island named " + name));
            if (!island.canEdit(player.getUuid())) {
                throw new IllegalStateException("You do not belong to " + owner.getUsername() + "'s " + island.name() + " island");
            }
            move(player, island, player);
        }
    }

    private final class CreateCommand extends IslandPlayerCommand {
        private final RequiredArg<String> nameArg;

        CreateCommand() {
            super("create", "Create and enter an additional named island.");
            nameArg = withRequiredArg("name", "Island name", ArgTypes.STRING);
        }

        @Override protected void run(CommandContext ctx, PlayerRef player) throws Exception {
            String name = nameArg.get(ctx);
            if (IslandStore.HOME_NAME.equalsIgnoreCase(name)) {
                throw new IllegalStateException("Home is reserved for your primary island; use /island home");
            }
            createIsland(player, player, name);
        }
    }

    private final class MembersCommand extends IslandPlayerCommand {
        MembersCommand() { super("members", "List your island owner and members."); }
        @Override protected void run(CommandContext ctx, PlayerRef player) {
            IslandRecord island = islandForContext(player);
            player.sendMessage(Message.raw("Island: " + island.name() + " | Owner: " + displayName(island.ownerUuid())
                    + " | Members: " + displayNames(island.members())));
        }
    }

    private final class InviteCommand extends OnePlayerArgumentCommand {
        InviteCommand() { super("invite", "Invite an online player to your island."); }
        @Override protected void run(CommandContext ctx, PlayerRef owner) throws Exception {
            PlayerRef target = target(ctx);
            IslandRecord island = ownedIslandForContext(owner);
            boolean added = islands.invite(island.worldName(), owner.getUuid(), target.getUuid());
            if (!added) throw new IllegalStateException(target.getUsername() + " already has a pending invitation to your island");
            target.sendMessage(Message.raw(owner.getUsername() + " invited you to " + island.name()
                    + ". Use /island accept."));
            owner.sendMessage(Message.raw("Invited " + target.getUsername() + " to " + island.name() + "."));
        }
    }

    private final class AcceptCommand extends IslandPlayerCommand {
        AcceptCommand() { super("accept", "Accept your pending island invitation."); }
        @Override protected void run(CommandContext ctx, PlayerRef player) throws Exception {
            IslandRecord island = islands.accept(player.getUuid());
            player.sendMessage(Message.raw("Island invitation accepted."));
            move(player, island, player);
        }
    }

    private final class KickCommand extends OnePlayerArgumentCommand {
        KickCommand() { super("kick", "Remove an online member from your island."); }
        @Override protected void run(CommandContext ctx, PlayerRef owner) throws Exception {
            PlayerRef target = target(ctx);
            IslandRecord island = ownedIslandForContext(owner);
            islands.kick(island.worldName(), owner.getUuid(), target.getUuid());
            owner.sendMessage(Message.raw("Removed " + target.getUsername() + " from " + island.name() + "."));
            target.sendMessage(Message.raw("You were removed from " + owner.getUsername() + "'s " + island.name() + " island."));
            redirect(target);
        }
    }

    private final class LeaveCommand extends IslandPlayerCommand {
        LeaveCommand() { super("leave", "Leave your current island."); }
        @Override protected void run(CommandContext ctx, PlayerRef player) throws Exception {
            IslandRecord island = islandForContext(player);
            islands.leave(island.worldName(), player.getUuid());
            player.sendMessage(Message.raw("You left " + island.name() + "."));
            redirect(player);
        }
    }

    private final class AdminCommand extends AbstractCommandCollection {
        AdminCommand() {
            super("admin", "Staff island administration commands.");
            requirePermission(IslandAccess.ADMIN_PERMISSION);
            addSubCommand(new AdminInfoCommand());
            addSubCommand(new AdminCreateCommand());
            addSubCommand(new AdminAddCommand());
            addSubCommand(new AdminRemoveCommand());
            addSubCommand(new AdminDeleteCommand());
        }
    }

    private abstract class AdminOnePlayerCommand extends OnePlayerArgumentCommand {
        AdminOnePlayerCommand(String name, String description) {
            super(name, description);
            requirePermission(IslandAccess.ADMIN_PERMISSION);
        }
    }

    private final class AdminInfoCommand extends AdminOnePlayerCommand {
        AdminInfoCommand() { super("info", "Show island information for an online player."); }
        @Override protected void run(CommandContext ctx, PlayerRef staff) {
            PlayerRef queried = target(ctx);
            List<IslandRecord> accessible = islands.findAllByPlayer(queried.getUuid());
            if (accessible.isEmpty()) throw new IllegalStateException(queried.getUsername() + " belongs to no islands");
            staff.sendMessage(Message.raw("Islands available to " + queried.getUsername() + ": "
                    + String.join(", ", accessible.stream().map(i -> displayName(i.ownerUuid()) + "/"
                            + i.name() + " (" + i.worldName() + ")").toList())));
        }
    }

    private final class AdminCreateCommand extends AdminOnePlayerCommand {
        AdminCreateCommand() { super("create", "Create an island for an online player."); }
        @Override protected void run(CommandContext ctx, PlayerRef staff) throws Exception {
            PlayerRef owner = target(ctx);
            if (islands.findHome(owner.getUuid()).isPresent()) {
                throw new IllegalStateException(owner.getUsername() + " already owns a Home island");
            }
            enterOrCreate(owner, staff);
        }
    }

    private abstract class AdminTwoPlayerCommand extends IslandPlayerCommand {
        private final RequiredArg<PlayerRef> ownerArg;
        private final RequiredArg<PlayerRef> memberArg;

        AdminTwoPlayerCommand(String name, String description) {
            super(name, description);
            requirePermission(IslandAccess.ADMIN_PERMISSION);
            ownerArg = withRequiredArg("owner", "Online island owner", ArgTypes.PLAYER_REF);
            memberArg = withRequiredArg("player", "Online player", ArgTypes.PLAYER_REF);
        }

        final PlayerRef owner(CommandContext ctx) { return ownerArg.get(ctx); }
        final PlayerRef member(CommandContext ctx) { return memberArg.get(ctx); }
    }

    private final class AdminAddCommand extends AdminTwoPlayerCommand {
        AdminAddCommand() { super("add", "Add an online player to an owner's island."); }
        @Override protected void run(CommandContext ctx, PlayerRef staff) throws Exception {
            PlayerRef owner = owner(ctx);
            PlayerRef member = member(ctx);
            islands.adminAdd(owner.getUuid(), member.getUuid());
            staff.sendMessage(Message.raw("Added " + member.getUsername() + " to " + owner.getUsername() + "'s island."));
            member.sendMessage(Message.raw("Staff added you to " + owner.getUsername() + "'s island."));
        }
    }

    private final class AdminRemoveCommand extends AdminTwoPlayerCommand {
        AdminRemoveCommand() { super("remove", "Remove an online player from an owner's island."); }
        @Override protected void run(CommandContext ctx, PlayerRef staff) throws Exception {
            PlayerRef owner = owner(ctx);
            PlayerRef member = member(ctx);
            islands.adminRemove(owner.getUuid(), member.getUuid());
            staff.sendMessage(Message.raw("Removed " + member.getUsername() + " from " + owner.getUsername() + "'s island."));
            member.sendMessage(Message.raw("Staff removed you from " + owner.getUsername() + "'s island."));
            redirect(member);
        }
    }

    private final class AdminDeleteCommand extends IslandPlayerCommand {
        private final RequiredArg<PlayerRef> ownerArg;
        private final RequiredArg<String> confirmationArg;

        AdminDeleteCommand() {
            super("delete", "Delete an owner's island metadata.");
            requirePermission(IslandAccess.ADMIN_PERMISSION);
            ownerArg = withRequiredArg("owner", "Online island owner", ArgTypes.PLAYER_REF);
            confirmationArg = withRequiredArg("confirmation", "Type confirm", ArgTypes.STRING);
        }

        @Override protected void run(CommandContext ctx, PlayerRef staff) throws Exception {
            PlayerRef owner = ownerArg.get(ctx);
            if (!"confirm".equalsIgnoreCase(confirmationArg.get(ctx))) {
                throw new IllegalStateException("Use /island admin delete " + owner.getUsername() + " confirm");
            }
            IslandRecord deleted = islands.deleteMetadata(owner.getUuid());
            redirectOnlinePlayers(deleted);
            staff.sendMessage(Message.raw("Deleted island metadata for " + owner.getUsername()
                    + "; world files were retained."));
        }
    }

    private abstract static class OnePlayerArgumentCommand extends IslandPlayerCommand {
        private final RequiredArg<PlayerRef> targetArg;

        OnePlayerArgumentCommand(String name, String description) {
            super(name, description);
            targetArg = withRequiredArg("player", "Online player", ArgTypes.PLAYER_REF);
        }

        final PlayerRef target(CommandContext ctx) { return targetArg.get(ctx); }
    }

    private IslandRecord islandForContext(PlayerRef player) {
        IslandRecord current = currentIsland(player);
        if (current != null && current.canEdit(player.getUuid())) return current;
        IslandRecord home = islands.findHome(player.getUuid()).orElse(null);
        if (home != null) return home;
        return islands.findByPlayer(player.getUuid())
                .orElseThrow(() -> new IllegalStateException("You do not belong to an island"));
    }

    private IslandRecord ownedIslandForContext(PlayerRef owner) {
        IslandRecord current = currentIsland(owner);
        if (current != null && owner.getUuid().equals(current.ownerUuid())) return current;
        return islands.findHome(owner.getUuid())
                .orElseThrow(() -> new IllegalStateException("You do not own a Home island"));
    }

    private IslandRecord currentIsland(PlayerRef player) {
        Ref<EntityStore> reference = player.getReference();
        if (reference == null || !reference.isValid()) return null;
        EntityStore entityStore = reference.getStore().getExternalData();
        World world = entityStore == null ? null : entityStore.getWorld();
        return world == null ? null : islands.findByWorld(world.getName()).orElse(null);
    }

    private static String displayNames(Set<UUID> players) {
        List<String> names = new ArrayList<>();
        for (UUID player : players) names.add(displayName(player));
        return names.isEmpty() ? "none" : String.join(", ", names);
    }

    private static String displayName(UUID playerId) {
        PlayerRef online = Universe.get().getPlayer(playerId);
        return online == null ? playerId.toString() : online.getUsername();
    }

    private static void redirectOnlinePlayers(IslandRecord island) {
        redirectIfOnline(island.ownerUuid());
        for (UUID member : island.members()) redirectIfOnline(member);
    }

    private static void redirectIfOnline(UUID playerId) {
        PlayerRef player = Universe.get().getPlayer(playerId);
        if (player != null) redirect(player);
    }

    private static void redirect(PlayerRef player) {
        OneBlockIslandsPlugin plugin = OneBlockIslandsPlugin.getInstance();
        if (plugin != null) plugin.redirectIfUnauthorized(player);
    }

    private static String rootMessage(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) root = root.getCause();
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }
}

abstract class IslandPlayerCommand extends AbstractPlayerCommand {
    IslandPlayerCommand(String name, String description) {
        super(name, description);
        requireNoPermission();
        setAllowsExtraArguments(false);
    }

    @Override protected final void execute(@Nonnull CommandContext ctx, @Nonnull Store<EntityStore> store,
                                           @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef player,
                                           @Nonnull World world) {
        try {
            run(ctx, player);
        } catch (Exception error) {
            player.sendMessage(Message.raw("Island action failed: " + rootMessage(error)));
        }
    }

    protected abstract void run(CommandContext ctx, PlayerRef player) throws Exception;

    private static String rootMessage(Throwable error) {
        Throwable root = error;
        while (root.getCause() != null) root = root.getCause();
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }
}
