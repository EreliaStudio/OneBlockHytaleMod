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
    private static final int FORMAT_VERSION = 1;
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
            validateUniqueMembership();
        } catch (JsonParseException | IllegalStateException e) {
            throw new IOException("Malformed island database " + path + ": " + e.getMessage(), e);
        }
    }

    public synchronized Optional<IslandRecord> findByPlayer(UUID player) {
        return byWorld.values().stream().filter(i -> i.canEdit(player)).findFirst();
    }

    public synchronized Optional<IslandRecord> findByOwner(UUID owner) {
        return byWorld.values().stream().filter(i -> owner.equals(i.ownerUuid)).findFirst();
    }

    public synchronized Optional<IslandRecord> findByWorld(String world) {
        return Optional.ofNullable(byWorld.get(world));
    }

    public synchronized IslandRecord create(UUID owner) throws IOException {
        Optional<IslandRecord> existing = findByPlayer(owner);
        if (existing.isPresent()) return existing.get();
        String worldName = "ob_" + owner.toString().replace("-", "");
        if (byWorld.containsKey(worldName)) throw new IllegalStateException("Island world collision: " + worldName);
        IslandRecord island = new IslandRecord(owner, worldName);
        byWorld.put(worldName, island);
        save();
        return island;
    }

    public synchronized void rollbackCreate(IslandRecord island) throws IOException {
        if (island != null && byWorld.get(island.worldName) == island) {
            byWorld.remove(island.worldName);
            save();
        }
    }

    public synchronized boolean invite(UUID owner, UUID invitee) throws IOException {
        IslandRecord island = findByOwner(owner).orElseThrow(() -> new IllegalStateException("You do not own an island"));
        if (findByPlayer(invitee).isPresent()) throw new IllegalStateException("That player already belongs to an island");
        boolean changed = island.pendingInvites.add(invitee);
        if (changed) save();
        return changed;
    }

    public synchronized IslandRecord accept(UUID player) throws IOException {
        if (findByPlayer(player).isPresent()) throw new IllegalStateException("You already belong to an island");
        IslandRecord island = byWorld.values().stream().filter(i -> i.pendingInvites.contains(player)).findFirst()
                .orElseThrow(() -> new IllegalStateException("You have no pending island invite"));
        island.pendingInvites.remove(player);
        island.members.add(player);
        save();
        return island;
    }

    public synchronized void kick(UUID owner, UUID member) throws IOException {
        IslandRecord island = findByOwner(owner).orElseThrow(() -> new IllegalStateException("You do not own an island"));
        if (!island.members.remove(member)) throw new IllegalStateException("That player is not a member");
        save();
    }

    public synchronized void leave(UUID player) throws IOException {
        IslandRecord island = findByPlayer(player).orElseThrow(() -> new IllegalStateException("You do not belong to an island"));
        if (player.equals(island.ownerUuid)) throw new IllegalStateException("The owner cannot leave; use an admin repair command");
        island.members.remove(player);
        save();
    }

    public synchronized void adminAdd(UUID owner, UUID player) throws IOException {
        IslandRecord island = findByOwner(owner).orElseThrow(() -> new IllegalStateException("Owner has no island"));
        if (findByPlayer(player).isPresent()) throw new IllegalStateException("Player already belongs to an island");
        island.pendingInvites.remove(player);
        island.members.add(player);
        save();
    }

    public synchronized void adminRemove(UUID owner, UUID player) throws IOException { kick(owner, player); }

    public synchronized void deleteMetadata(UUID owner) throws IOException {
        IslandRecord island = findByOwner(owner).orElseThrow(() -> new IllegalStateException("Owner has no island"));
        byWorld.remove(island.worldName);
        save();
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
        if (island.members == null) island.members = new java.util.LinkedHashSet<>();
        if (island.pendingInvites == null) island.pendingInvites = new java.util.LinkedHashSet<>();
        if (island.bannedVisitors == null) island.bannedVisitors = new java.util.LinkedHashSet<>();
        island.members.remove(island.ownerUuid);
    }

    private void validateUniqueMembership() throws IOException {
        Map<UUID, String> membership = new LinkedHashMap<>();
        for (IslandRecord island : byWorld.values()) {
            for (UUID player : concat(island.ownerUuid, island.members)) {
                String prior = membership.putIfAbsent(player, island.worldName);
                if (prior != null) throw new IOException("Player " + player + " belongs to both " + prior + " and " + island.worldName);
            }
        }
    }

    private static List<UUID> concat(UUID owner, java.util.Set<UUID> members) {
        List<UUID> result = new ArrayList<>(); result.add(owner); result.addAll(members); return result;
    }

    private static final class Database {
        int version;
        List<IslandRecord> islands;
        Database(int version, List<IslandRecord> islands) { this.version = version; this.islands = islands; }
    }
}
