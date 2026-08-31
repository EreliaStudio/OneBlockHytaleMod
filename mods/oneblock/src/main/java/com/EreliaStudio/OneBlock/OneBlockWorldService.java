package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/** Server-facing API for creating isolated OneBlock worlds and moving parties into them. */
public final class OneBlockWorldService
{
    private static final Pattern VALID_WORLD_NAME = Pattern.compile("[A-Za-z0-9_-]{1,48}");
    private static final Transform SPAWN = new Transform(
            new Vector3d(0.5, 102.0, 0.5),
            new Rotation3f(0.0F, 0.0F, 0.0F)
    );

    private final OneBlockWorldStateRegistry stateRegistry;
    private final OneBlockHudService hudService;
    private final ConcurrentHashMap<String, CompletableFuture<World>> creations = new ConcurrentHashMap<>();

    public OneBlockWorldService(OneBlockWorldStateRegistry stateRegistry,
                                OneBlockHudService hudService)
    {
        this.stateRegistry = stateRegistry;
        this.hudService = hudService;
    }

    /**
     * Creates a new persistent OneBlock world and transfers all supplied players.
     * Each call must use a new world name. Pass {@code null} or blank for an
     * automatically generated name.
     */
    public CompletableFuture<World> createExpeditionWorld(String requestedName,
                                                           Collection<PlayerRef> players)
    {
        String worldName;
        try
        {
            worldName = normalizeOrGenerateName(requestedName);
            validateWorldName(worldName);
        }
        catch (IllegalArgumentException exception)
        {
            return CompletableFuture.failedFuture(exception);
        }

        Universe universe = Universe.get();
        if (universe == null)
        {
            return CompletableFuture.failedFuture(new IllegalStateException("Universe is not ready"));
        }

        if (universe.getWorld(worldName) != null || universe.isWorldLoadable(worldName))
        {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("A world named '" + worldName + "' already exists")
            );
        }

        List<PlayerRef> party = players == null
                ? List.of()
                : players.stream().filter(player -> player != null).toList();

        CompletableFuture<World> pending = new CompletableFuture<>();
        CompletableFuture<World> existingCreation = creations.putIfAbsent(worldName, pending);
        if (existingCreation != null)
        {
            return existingCreation;
        }

        try
        {
            if (!OneBlockWorldBootstrap.ensureVoidWorldConfig(universe.getWorldsPath(), worldName))
            {
                pending.completeExceptionally(
                        new IllegalStateException("Could not create the world configuration for '" + worldName + "'")
                );
                creations.remove(worldName, pending);
                return pending;
            }

            stateRegistry.registerWorld(worldName);

            universe.loadWorld(worldName)
                    .thenCompose(world -> movePlayers(world, party).thenApply(nothing -> world))
                    .whenComplete((world, error) ->
                    {
                        if (error == null)
                        {
                            pending.complete(world);
                        }
                        else
                        {
                            pending.completeExceptionally(error);
                        }
                        creations.remove(worldName, pending);
                    });
        }
        catch (Exception exception)
        {
            pending.completeExceptionally(exception);
            creations.remove(worldName, pending);
        }
        return pending;
    }

    /** Transfers a party into an already loaded managed OneBlock world. */
    public CompletableFuture<Void> movePlayers(World targetWorld, Collection<PlayerRef> players)
    {
        if (targetWorld == null || !stateRegistry.isManaged(targetWorld))
        {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("Target is not a managed OneBlock world")
            );
        }

        if (players == null || players.isEmpty())
        {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<?>[] transfers = players.stream()
                .filter(player -> player != null)
                .map(player -> movePlayer(targetWorld, player))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(transfers);
    }

    /** Loads a persisted managed world if necessary. */
    public CompletableFuture<World> loadExpeditionWorld(String worldName)
    {
        if (worldName == null || worldName.isBlank())
        {
            return CompletableFuture.failedFuture(new IllegalArgumentException("World name cannot be blank"));
        }

        String normalizedName = worldName.trim();
        if (!stateRegistry.isManaged(normalizedName))
        {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("World is not managed by OneBlock: " + normalizedName)
            );
        }

        Universe universe = Universe.get();
        if (universe == null)
        {
            return CompletableFuture.failedFuture(new IllegalStateException("Universe is not ready"));
        }
        World loaded = universe.getWorld(normalizedName);
        if (loaded != null)
        {
            return CompletableFuture.completedFuture(loaded);
        }
        if (!universe.isWorldLoadable(normalizedName))
        {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException("OneBlock world does not exist on disk: " + normalizedName)
            );
        }
        return universe.loadWorld(normalizedName);
    }

    /** Loads a managed world on demand and transfers a party into it. */
    public CompletableFuture<World> movePlayers(String worldName, Collection<PlayerRef> players)
    {
        return loadExpeditionWorld(worldName)
                .thenCompose(world -> movePlayers(world, players).thenApply(nothing -> world));
    }

    private CompletableFuture<Void> movePlayer(World targetWorld, PlayerRef playerRef)
    {
        Ref<EntityStore> entityRef = playerRef.getReference();
        if (entityRef == null || !entityRef.isValid())
        {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Player '" + playerRef.getUsername() + "' is not in a world")
            );
        }

        Store<EntityStore> sourceStore = entityRef.getStore();
        EntityStore entityStore = sourceStore == null ? null : sourceStore.getExternalData();
        World sourceWorld = entityStore == null ? null : entityStore.getWorld();
        if (sourceWorld == null)
        {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Could not resolve the current world for '" + playerRef.getUsername() + "'")
            );
        }

        return Universe.transferPlayerAsync(
                playerRef,
                sourceWorld,
                CompletableFuture.completedFuture(targetWorld),
                world -> SPAWN
        ).thenCompose(transferredPlayer ->
        {
            CompletableFuture<Void> hudRestored = new CompletableFuture<>();
            targetWorld.execute(() ->
            {
                try
                {
                    restoreHud(targetWorld, transferredPlayer);
                    hudRestored.complete(null);
                }
                catch (Exception exception)
                {
                    hudRestored.completeExceptionally(exception);
                }
            });
            return hudRestored;
        });
    }

    private void restoreHud(World world, PlayerRef playerRef)
    {
        if (hudService == null)
        {
            return;
        }

        Player player = playerRef.getComponent(Player.getComponentType());
        if (player == null)
        {
            return;
        }

        OneBlockDungeonStateProvider dungeon = stateRegistry.dungeonState(world);
        if (dungeon.isDungeonActive())
        {
            String dungeonId = dungeon.getActiveDungeonId();
            int totalWaves = OneBlockDungeonDefaults.getWaveCount(dungeonId);
            hudService.showDungeonStarted(player, dungeonId, totalWaves);
            hudService.updateDungeonWave(player, dungeonId, dungeon.getCurrentWaveIndex(), totalWaves);
            return;
        }

        OneBlockExpeditionStateProvider expedition = stateRegistry.expeditionState(world);
        if (expedition.hasActiveExpedition())
        {
            hudService.restoreExpeditionHud(
                    player,
                    expedition.getActiveExpeditionId(),
                    expedition.getTicksRemaining(),
                    expedition.getTotalTicks()
            );
        }
        else
        {
            hudService.clear(player);
        }
    }

    private static String normalizeOrGenerateName(String requestedName)
    {
        if (requestedName == null || requestedName.isBlank() || "-".equals(requestedName.trim()))
        {
            return "oneblock-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return requestedName.trim();
    }

    private static void validateWorldName(String worldName)
    {
        if (!VALID_WORLD_NAME.matcher(worldName).matches())
        {
            throw new IllegalArgumentException(
                    "World names may only contain letters, numbers, '-' and '_' (maximum 48 characters)"
            );
        }

        if (World.DEFAULT.equals(worldName))
        {
            throw new IllegalArgumentException("The default world already exists");
        }
    }
}
