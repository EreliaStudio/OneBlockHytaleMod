package com.EreliaStudio.OneBlockIslands;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class IslandStore {
    private static final int FORMAT_VERSION = 2;
    public static final String HOME_NAME = "Home";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path path;
    private final Map<String, IslandRecord> byWorld = new LinkedHashMap<>();

    public IslandStore(Path path) { this.path = path; }

    public synchronized void load() throws IOException {
        byWorld.clear();
        if (!Files.exists(path)) return;
        try {
            Database data = gson.fromJson(Files.readString(path, StandardCharsets.UTF_8), Database.class);
            if (data == null || data.islands == null) throw new IOException("Island database has no islands array");
            for (IslandRecord island : data.islands) {
                validate(island);
                if (byWorld.putIfAbsent(island.worldName, island) != null) {
                    throw new IOException("Duplicate island world: " + island.worldName);
                }
            }
            validateUniqueIslandNames();
        } catch (JsonParseException | IllegalStateException e) {
            throw new IOException("Malformed island database " + path + ": " + e.getMessage(), e);
        }
    }

    public synchronized Optional<IslandRecord> findByPlayer(UUID player) {
        return byWorld.values().stream().filter(i -> i.canEdit(player)).findFirst();
    }

    public synchronized List<IslandRecord> findAllByPlayer(UUID player) {
        return byWorld.values().stream().filter(i -> i.canEdit(player)).toList();
    }

    public synchronized Optional<IslandRecord> findByOwner(UUID owner) {
        return findHome(owner);
    }

    public synchronized Optional<IslandRecord> findHome(UUID owner) {
        return byWorld.values().stream()
                .filter(i -> owner.equals(i.ownerUuid) && HOME_NAME.equalsIgnoreCase(i.name))
                .findFirst();
    }

    public synchronized Optional<IslandRecord> findOwned(UUID owner, String name) {
        if (name == null) return Optional.empty();
        return byWorld.values().stream()
                .filter(i -> owner.equals(i.ownerUuid) && name.equalsIgnoreCase(i.name)).findFirst();
    }

    public synchronized List<IslandRecord> findOwned(UUID owner) {
        return byWorld.values().stream().filter(i -> owner.equals(i.ownerUuid)).toList();
    }

    public synchronized Optional<IslandRecord> findByWorld(String world) {
        return Optional.ofNullable(byWorld.get(world));
    }

    public synchronized IslandRecord create(UUID owner) throws IOException {
        Optional<IslandRecord> existing = findHome(owner);
        if (existing.isPresent()) return existing.get();
        return create(owner, HOME_NAME);
    }

    public synchronized IslandRecord create(UUID owner, String requestedName) throws IOException {
        String name = normalizeName(requestedName);
        Optional<IslandRecord> existing = findOwned(owner, name);
        if (existing.isPresent()) {
            if (HOME_NAME.equalsIgnoreCase(name)) return existing.get();
            throw new IllegalStateException("You already own an island named " + name);
        }
        String worldName = HOME_NAME.equalsIgnoreCase(name)
                ? "ob_" + owner.toString().replace("-", "")
                : "ob_" + UUID.randomUUID().toString().replace("-", "");
        if (byWorld.containsKey(worldName)) throw new IllegalStateException("Island world collision: " + worldName);
        IslandRecord island = new IslandRecord(owner, worldName, name);
        byWorld.put(worldName, island);
        try {
            save();
        } catch (IOException error) {
            byWorld.remove(worldName);
            throw error;
        }
        return island;
    }

    public synchronized void rollbackCreate(IslandRecord island) throws IOException {
        if (island != null && byWorld.get(island.worldName) == island) {
            byWorld.remove(island.worldName);
            try {
                save();
            } catch (IOException error) {
                byWorld.put(island.worldName, island);
                throw error;
            }
        }
    }

    public synchronized boolean invite(UUID owner, UUID invitee) throws IOException {
        IslandRecord island = findHome(owner).orElseThrow(() -> new IllegalStateException("You do not own a Home island"));
        return invite(island.worldName, owner, invitee);
    }

    public synchronized boolean invite(String worldName, UUID owner, UUID invitee) throws IOException {
        IslandRecord island = ownedWorld(worldName, owner);
        if (island.canEdit(invitee)) throw new IllegalStateException("That player already belongs to this island");
        boolean changed = island.pendingInvites.add(invitee);
        if (changed) {
            try {
                save();
            } catch (IOException error) {
                island.pendingInvites.remove(invitee);
                throw error;
            }
        }
        return changed;
    }

    public synchronized IslandRecord accept(UUID player) throws IOException {
        IslandRecord island = byWorld.values().stream().filter(i -> i.pendingInvites.contains(player)).findFirst()
                .orElseThrow(() -> new IllegalStateException("You have no pending island invite"));
        island.pendingInvites.remove(player);
        island.members.add(player);
        try {
            save();
        } catch (IOException error) {
            island.members.remove(player);
            island.pendingInvites.add(player);
            throw error;
        }
        return island;
    }

    public synchronized void kick(UUID owner, UUID member) throws IOException {
        IslandRecord island = findHome(owner).orElseThrow(() -> new IllegalStateException("You do not own a Home island"));
        kick(island.worldName, owner, member);
    }

    public synchronized void kick(String worldName, UUID owner, UUID member) throws IOException {
        IslandRecord island = ownedWorld(worldName, owner);
        if (!island.members.remove(member)) throw new IllegalStateException("That player is not a member");
        try {
            save();
        } catch (IOException error) {
            island.members.add(member);
            throw error;
        }
    }

    public synchronized void leave(UUID player) throws IOException {
        IslandRecord island = findByPlayer(player).orElseThrow(() -> new IllegalStateException("You do not belong to an island"));
        leave(island.worldName, player);
    }

    public synchronized void leave(String worldName, UUID player) throws IOException {
        IslandRecord island = findByWorld(worldName).filter(i -> i.canEdit(player))
                .orElseThrow(() -> new IllegalStateException("You do not belong to that island"));
        if (player.equals(island.ownerUuid)) throw new IllegalStateException("The owner cannot leave; use an admin repair command");
        island.members.remove(player);
        try {
            save();
        } catch (IOException error) {
            island.members.add(player);
            throw error;
        }
    }

    public synchronized void adminAdd(UUID owner, UUID player) throws IOException {
        IslandRecord island = findHome(owner).orElseThrow(() -> new IllegalStateException("Owner has no Home island"));
        if (island.canEdit(player)) throw new IllegalStateException("Player already belongs to that island");
        boolean wasInvited = island.pendingInvites.remove(player);
        island.members.add(player);
        try {
            save();
        } catch (IOException error) {
            island.members.remove(player);
            if (wasInvited) island.pendingInvites.add(player);
            throw error;
        }
    }

    public synchronized void adminRemove(UUID owner, UUID player) throws IOException { kick(owner, player); }

    public synchronized IslandRecord deleteMetadata(UUID owner) throws IOException {
        IslandRecord island = findHome(owner).orElseThrow(() -> new IllegalStateException("Owner has no Home island"));
        byWorld.remove(island.worldName);
        try {
            save();
        } catch (IOException error) {
            byWorld.put(island.worldName, island);
            throw error;
        }
        return island;
    }

    private void save() throws IOException {
        Files.createDirectories(path.getParent());
        Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
        Files.writeString(temporary, gson.toJson(new Database(FORMAT_VERSION, new ArrayList<>(byWorld.values()))), StandardCharsets.UTF_8);
        try {
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void validate(IslandRecord island) throws IOException {
        if (island == null || island.ownerUuid == null || island.worldName == null || island.worldName.isBlank())
            throw new IOException("Island entry is missing ownerUuid or worldName");
        if (island.name == null || island.name.isBlank()) island.name = HOME_NAME;
        if (island.members == null) island.members = new java.util.LinkedHashSet<>();
        if (island.pendingInvites == null) island.pendingInvites = new java.util.LinkedHashSet<>();
        if (island.bannedVisitors == null) island.bannedVisitors = new java.util.LinkedHashSet<>();
        island.members.remove(island.ownerUuid);
    }

    private void validateUniqueIslandNames() throws IOException {
        Map<String, String> names = new LinkedHashMap<>();
        for (IslandRecord island : byWorld.values()) {
            String key = island.ownerUuid + "\u0000" + island.name.toLowerCase(java.util.Locale.ROOT);
            String prior = names.putIfAbsent(key, island.worldName);
            if (prior != null) throw new IOException("Owner " + island.ownerUuid + " has duplicate island name " + island.name);
        }
    }

    private IslandRecord ownedWorld(String worldName, UUID owner) {
        IslandRecord island = byWorld.get(worldName);
        if (island == null || !owner.equals(island.ownerUuid)) {
            throw new IllegalStateException("You do not own that island");
        }
        return island;
    }

    private static String normalizeName(String requestedName) {
        String name = requestedName == null ? "" : requestedName.trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Island name is required");
        if (name.length() > 32) throw new IllegalArgumentException("Island names may contain at most 32 characters");
        if (!name.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("Island names may only contain letters, numbers, underscores, and hyphens");
        }
        return HOME_NAME.equalsIgnoreCase(name) ? HOME_NAME : name;
    }

    private static final class Database {
        int version;
        List<IslandRecord> islands;
        Database(int version, List<IslandRecord> islands) { this.version = version; this.islands = islands; }
    }
}
