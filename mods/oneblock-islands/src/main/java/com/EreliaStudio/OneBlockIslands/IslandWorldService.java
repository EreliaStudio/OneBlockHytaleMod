package com.EreliaStudio.OneBlockIslands;

import com.EreliaStudio.OneBlock.OneBlockBlockIds;
import com.EreliaStudio.OneBlock.OneBlockPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.WorldConfigProvider;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.VoidWorldGenProvider;
import org.joml.Vector3d;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/** Owns creation, loading, and player transfer for island worlds. */
final class IslandWorldService {
    static final Transform SPAWN = new Transform(new Vector3d(0.5, 102.0, 0.5), new Rotation3f());
    private static final Color VOID_TINT = new Color((byte) 0x5a, (byte) 0x99, (byte) 0x2b);
    private static final String VOID_ENVIRONMENT = "Env_Default_Void";

    private final IslandStore islands;
    private final ConcurrentHashMap<String, CompletableFuture<World>> creations = new ConcurrentHashMap<>();

    IslandWorldService(IslandStore islands) {
        this.islands = islands;
    }

    WorldConfigProvider worldConfigProvider(WorldConfigProvider original) {
        return new WorldConfigProvider() {
            @Override public CompletableFuture<WorldConfig> load(Path path, String worldName) {
                return original.load(path, worldName).thenApply(config -> {
                    if (config != null && islands.findByWorld(worldName).isPresent()) configure(config);
                    return config;
                });
            }

            @Override public CompletableFuture<Void> save(Path path, WorldConfig config, World world) {
                return original.save(path, config, world);
            }
        };
    }

    CompletableFuture<World> create(IslandRecord island, Collection<PlayerRef> players) {
        if (island == null) return CompletableFuture.failedFuture(new IllegalArgumentException("Island is required"));
        Universe universe = Universe.get();
        if (universe == null) return CompletableFuture.failedFuture(new IllegalStateException("Universe is not ready"));
        if (universe.getWorld(island.worldName()) != null || universe.isWorldLoadable(island.worldName())) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Island world already exists: " + island.worldName()));
        }

        CompletableFuture<World> pending = new CompletableFuture<>();
        CompletableFuture<World> existing = creations.putIfAbsent(island.worldName(), pending);
        if (existing != null) return existing;

        try {
            if (!IslandWorldBootstrap.ensureConfig(universe.getWorldsPath(), island.worldName())) {
                throw new IllegalStateException("Could not create island world configuration");
            }
            universe.loadWorld(island.worldName())
                    .thenCompose(world -> initialize(world).thenCompose(ignored -> movePlayers(world, players).thenApply(v -> world)))
                    .whenComplete((world, error) -> {
                        if (error == null) pending.complete(world); else pending.completeExceptionally(error);
                        creations.remove(island.worldName(), pending);
                    });
        } catch (Exception error) {
            pending.completeExceptionally(error);
            creations.remove(island.worldName(), pending);
        }
        return pending;
    }

    CompletableFuture<World> movePlayers(String worldName, Collection<PlayerRef> players) {
        IslandRecord island = islands.findByWorld(worldName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown island world: " + worldName));
        Universe universe = Universe.get();
        if (universe == null) return CompletableFuture.failedFuture(new IllegalStateException("Universe is not ready"));
        World loaded = universe.getWorld(island.worldName());
        CompletableFuture<World> worldFuture = loaded == null ? universe.loadWorld(island.worldName()) : CompletableFuture.completedFuture(loaded);
        return worldFuture.thenCompose(world -> initialize(world)
                .thenCompose(ignored -> movePlayers(world, players))
                .thenApply(ignored -> world));
    }

    CompletableFuture<Void> initialize(World world) {
        IslandRecord island = world == null ? null : islands.findByWorld(world.getName()).orElse(null);
        if (island == null) return CompletableFuture.completedFuture(null);
        CompletableFuture<Void> initialized = new CompletableFuture<>();
        world.execute(() -> {
            try {
                WorldConfig config = world.getWorldConfig();
                if (config != null) {
                    configure(config);
                    config.markChanged();
                }
                OneBlockPlugin plugin = OneBlockPlugin.getInstance();
                if (plugin == null) throw new IllegalStateException("OneBlock plugin is unavailable");
                if (plugin.getRootRegistry().find(world, OneBlockBlockIds.ONEBLOCK_POSITION) == null) {
                    plugin.initializeRoot(world, OneBlockBlockIds.ONEBLOCK_POSITION, island.ownerUuid(), island.ownerUuid().toString());
                }
                initialized.complete(null);
            } catch (Exception error) {
                initialized.completeExceptionally(error);
            }
        });
        return initialized;
    }

    private CompletableFuture<Void> movePlayers(World target, Collection<PlayerRef> players) {
        List<PlayerRef> party = players == null ? List.of() : players.stream().filter(p -> p != null).toList();
        return CompletableFuture.allOf(party.stream().map(player -> transfer(target, player)).toArray(CompletableFuture[]::new));
    }

    private static CompletableFuture<Void> transfer(World target, PlayerRef player) {
        Ref<EntityStore> ref = player.getReference();
        Store<EntityStore> store = ref == null ? null : ref.getStore();
        EntityStore data = store == null ? null : store.getExternalData();
        World source = data == null ? null : data.getWorld();
        if (source == null) return CompletableFuture.failedFuture(new IllegalStateException("Player has no current world"));
        if (target.equals(source)) return CompletableFuture.completedFuture(null);
        return Universe.transferPlayerAsync(player, source, CompletableFuture.completedFuture(target), ignored -> SPAWN)
                .thenApply(ignored -> null);
    }

    private static void configure(WorldConfig config) {
        config.setWorldGenProvider(new VoidWorldGenProvider(VOID_TINT, VOID_ENVIRONMENT));
        config.setSpawnProvider(new GlobalSpawnProvider(SPAWN));
    }
}
