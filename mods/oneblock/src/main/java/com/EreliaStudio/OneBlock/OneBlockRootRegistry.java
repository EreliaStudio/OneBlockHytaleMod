package com.EreliaStudio.OneBlock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hypixel.hytale.server.core.universe.world.World;
import org.joml.Vector3i;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Persists player-owned OneBlock Roots in ordinary shared worlds.
 *
 * <p>Every owner has one expedition/dungeon state per world. Any number of
 * physical roots can point at that state, so a break at any of them advances
 * the same game.</p>
 */
public final class OneBlockRootRegistry
{
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path pluginDataDirectory;
    private final Path dataDirectory;
    private final ConcurrentHashMap<String, RootWorldState> worlds = new ConcurrentHashMap<>();

    public OneBlockRootRegistry(Path dataDirectory)
    {
        this.pluginDataDirectory = dataDirectory;
        this.dataDirectory = dataDirectory.resolve("root-worlds");
    }

    public RootEntry find(World world, Vector3i position)
    {
        if (world == null || position == null) return null;
        return find(world.getName(), position);
    }

    RootEntry find(String worldName, Vector3i position)
    {
        if (worldName == null || position == null) return null;
        return stateFor(worldName).find(PositionKey.of(position));
    }

    /** Resolves only roots actually owned by this player; never an island owner's fallback root. */
    RootEntry findOwnedRoot(String worldName, UUID playerId, String playerName)
    {
        if (worldName == null || playerId == null || positions(worldName, playerId).isEmpty())
            return null;
        return new RootEntry(playerId, playerName == null ? "" : playerName);
    }

    public RootEntry register(World world,
                              Vector3i position,
                              UUID ownerId,
                              String ownerName)
    {
        if (world == null) throw new IllegalArgumentException("World cannot be null");
        return register(world.getName(), position, ownerId, ownerName);
    }

    RootEntry register(String worldName,
                       Vector3i position,
                       UUID ownerId,
                       String ownerName)
    {
        if (worldName == null || worldName.isBlank())
            throw new IllegalArgumentException("World name cannot be blank");
        if (position == null) throw new IllegalArgumentException("Position cannot be null");
        if (ownerId == null) throw new IllegalArgumentException("Owner UUID cannot be null");

        return stateFor(worldName).register(PositionKey.of(position), ownerId, ownerName);
    }

    public boolean remove(World world, Vector3i position)
    {
        return world != null && position != null && remove(world.getName(), position);
    }

    boolean remove(String worldName, Vector3i position)
    {
        return worldName != null && position != null
                && stateFor(worldName).remove(PositionKey.of(position));
    }

    public List<Vector3i> positions(World world, UUID ownerId)
    {
        if (world == null || ownerId == null) return List.of();
        return positions(world.getName(), ownerId);
    }

    List<Vector3i> positions(String worldName, UUID ownerId)
    {
        if (worldName == null || ownerId == null) return List.of();
        return stateFor(worldName).positions(ownerId);
    }

    /** Lists the physical OneBlock nodes owned by a player in this world. */
    public List<OneBlockNode> nodes(World world, UUID ownerId)
    {
        if (world == null || ownerId == null) return List.of();
        return nodes(world.getName(), ownerId);
    }

    List<OneBlockNode> nodes(String worldName, UUID ownerId)
    {
        if (worldName == null || ownerId == null) return List.of();
        return stateFor(worldName).nodes(worldName, ownerId);
    }

    public boolean hasRoots(World world, UUID ownerId)
    {
        return !positions(world, ownerId).isEmpty();
    }

    public OneBlockExpeditionStateProvider expeditionState(World world, UUID ownerId)
    {
        return ownerState(world, ownerId).expeditionState();
    }

    OneBlockExpeditionStateProvider expeditionState(String worldName, UUID ownerId)
    {
        return ownerState(worldName, ownerId).expeditionState();
    }

    public OneBlockDungeonStateProvider dungeonState(World world, UUID ownerId)
    {
        return ownerState(world, ownerId).dungeonState();
    }

    OneBlockDungeonStateProvider dungeonState(String worldName, UUID ownerId)
    {
        return ownerState(worldName, ownerId).dungeonState();
    }

    private OwnerState ownerState(World world, UUID ownerId)
    {
        if (world == null) throw new IllegalArgumentException("World cannot be null");
        if (ownerId == null) throw new IllegalArgumentException("Owner UUID cannot be null");
        return stateFor(world.getName()).ownerState(ownerId);
    }

    private OwnerState ownerState(String worldName, UUID ownerId)
    {
        if (worldName == null || worldName.isBlank())
            throw new IllegalArgumentException("World name cannot be blank");
        if (ownerId == null) throw new IllegalArgumentException("Owner UUID cannot be null");
        return stateFor(worldName).ownerState(ownerId);
    }

    private RootWorldState stateFor(String worldName)
    {
        return worlds.computeIfAbsent(worldName, this::loadWorld);
    }

    private RootWorldState loadWorld(String worldName)
    {
        String directoryName = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(worldName.getBytes(StandardCharsets.UTF_8));
        return new RootWorldState(dataDirectory.resolve(directoryName));
    }

    public record RootEntry(UUID ownerId, String ownerName)
    {
    }

    /** A stable description of one registered physical node. */
    public record OneBlockNode(String worldName, Vector3i position, RootEntry root)
    {
        public OneBlockNode
        {
            if (worldName == null || worldName.isBlank())
                throw new IllegalArgumentException("World name cannot be blank");
            if (position == null) throw new IllegalArgumentException("Position cannot be null");
            if (root == null) throw new IllegalArgumentException("Root cannot be null");
            position = new Vector3i(position);
        }

        @Override
        public Vector3i position()
        {
            return new Vector3i(position);
        }
    }

    private record PositionKey(int x, int y, int z)
    {
        private static PositionKey of(Vector3i position)
        {
            return new PositionKey(position.x(), position.y(), position.z());
        }

        private Vector3i toVector()
        {
            return new Vector3i(x, y, z);
        }
    }

    private record OwnerState(OneBlockExpeditionStateProvider expeditionState,
                              OneBlockDungeonStateProvider dungeonState)
    {
    }

    private final class RootWorldState
    {
        private final Path worldDirectory;
        private final Path rootsPath;
        private final ConcurrentHashMap<PositionKey, RootEntry> roots = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<UUID, OwnerState> owners = new ConcurrentHashMap<>();

        private RootWorldState(Path worldDirectory)
        {
            this.worldDirectory = worldDirectory;
            this.rootsPath = worldDirectory.resolve("roots.json");
            loadRoots();
        }

        private RootEntry find(PositionKey position)
        {
            return roots.get(position);
        }

        private synchronized RootEntry register(PositionKey position,
                                                UUID ownerId,
                                                String ownerName)
        {
            RootEntry root = new RootEntry(ownerId, ownerName == null ? "" : ownerName);
            roots.put(position, root);
            ownerState(ownerId);
            saveRoots();
            return root;
        }

        private synchronized boolean remove(PositionKey position)
        {
            if (roots.remove(position) == null) return false;
            saveRoots();
            return true;
        }

        private List<Vector3i> positions(UUID ownerId)
        {
            return roots.entrySet().stream()
                    .filter(entry -> ownerId.equals(entry.getValue().ownerId()))
                    .map(entry -> entry.getKey().toVector())
                    .sorted(Comparator.comparingInt(Vector3i::x)
                            .thenComparingInt(Vector3i::y)
                            .thenComparingInt(Vector3i::z))
                    .toList();
        }

        private List<OneBlockNode> nodes(String worldName, UUID ownerId)
        {
            return roots.entrySet().stream()
                    .filter(entry -> ownerId.equals(entry.getValue().ownerId()))
                    .sorted(Comparator.comparingInt((java.util.Map.Entry<PositionKey, RootEntry> e) -> e.getKey().x())
                            .thenComparingInt(e -> e.getKey().y())
                            .thenComparingInt(e -> e.getKey().z()))
                    .map(entry -> new OneBlockNode(worldName, entry.getKey().toVector(), entry.getValue()))
                    .toList();
        }

        private OwnerState ownerState(UUID ownerId)
        {
            return owners.computeIfAbsent(ownerId, id ->
            {
                Path ownerDirectory = worldDirectory.resolve("owners").resolve(id.toString());
                migrateLegacyWorldState(ownerDirectory);
                return new OwnerState(
                        new OneBlockExpeditionStateProvider(ownerDirectory.resolve("expedition.json")),
                        new OneBlockDungeonStateProvider(ownerDirectory.resolve("dungeon.json"))
                );
            });
        }

        private void migrateLegacyWorldState(Path ownerDirectory)
        {
            Path legacyDirectory = pluginDataDirectory.resolve("worlds").resolve(worldDirectory.getFileName());
            copyLegacyFile(legacyDirectory.resolve("expedition.json"), ownerDirectory.resolve("expedition.json"));
            copyLegacyFile(legacyDirectory.resolve("dungeon.json"), ownerDirectory.resolve("dungeon.json"));
        }

        private void copyLegacyFile(Path source, Path target)
        {
            if (!Files.exists(source) || Files.exists(target)) return;
            try
            {
                Files.createDirectories(target.getParent());
                Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);
            }
            catch (IOException ignored)
            {
            }
        }

        private void loadRoots()
        {
            if (!Files.exists(rootsPath)) return;

            try (Reader reader = Files.newBufferedReader(rootsPath, StandardCharsets.UTF_8))
            {
                RootSaveData data = GSON.fromJson(reader, RootSaveData.class);
                if (data == null || data.roots == null) return;

                for (SavedRoot saved : data.roots)
                {
                    if (saved == null || saved.ownerId == null) continue;
                    try
                    {
                        UUID ownerId = UUID.fromString(saved.ownerId);
                        roots.put(
                                new PositionKey(saved.x, saved.y, saved.z),
                                new RootEntry(ownerId, saved.ownerName == null ? "" : saved.ownerName)
                        );
                    }
                    catch (IllegalArgumentException ignored)
                    {
                    }
                }
            }
            catch (Exception ignored)
            {
            }
        }

        private void saveRoots()
        {
            try
            {
                Files.createDirectories(rootsPath.getParent());
                RootSaveData data = new RootSaveData();
                data.roots = new ArrayList<>();

                roots.entrySet().stream()
                        .sorted(Comparator.comparingInt((java.util.Map.Entry<PositionKey, RootEntry> e) -> e.getKey().x())
                                .thenComparingInt(e -> e.getKey().y())
                                .thenComparingInt(e -> e.getKey().z()))
                        .forEach(entry -> data.roots.add(new SavedRoot(entry.getKey(), entry.getValue())));

                try (Writer writer = Files.newBufferedWriter(rootsPath, StandardCharsets.UTF_8))
                {
                    GSON.toJson(data, writer);
                }
            }
            catch (IOException ignored)
            {
            }
        }
    }

    private static final class RootSaveData
    {
        private List<SavedRoot> roots;
    }

    private static final class SavedRoot
    {
        private int x;
        private int y;
        private int z;
        private String ownerId;
        private String ownerName;

        @SuppressWarnings("unused")
        private SavedRoot()
        {
        }

        private SavedRoot(PositionKey position, RootEntry root)
        {
            this.x = position.x();
            this.y = position.y();
            this.z = position.z();
            this.ownerId = root.ownerId().toString();
            this.ownerName = root.ownerName();
        }
    }
}
