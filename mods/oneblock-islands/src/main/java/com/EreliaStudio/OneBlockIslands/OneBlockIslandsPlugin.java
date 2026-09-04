package com.EreliaStudio.OneBlockIslands;

import com.EreliaStudio.OneBlock.OneBlockPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.PrepareUniverseEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.math.vector.Transform;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class OneBlockIslandsPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static OneBlockIslandsPlugin instance;
    private IslandStore islands;
    private IslandWorldService islandWorlds;

    public OneBlockIslandsPlugin(@Nonnull JavaPluginInit init) { super(init); instance = this; }
    static OneBlockIslandsPlugin getInstance() { return instance; }

    @Override
    @SuppressWarnings("deprecation") // PrepareUniverseEvent is the only world-config provider hook in this API.
    protected void setup() {
        islands = new IslandStore(getDataDirectory().resolve("islands.json"));
        try { islands.load(); }
        catch (IOException e) { throw new IllegalStateException("Refusing to start with an invalid island database: " + e.getMessage(), e); }
        islandWorlds = new IslandWorldService(islands);
        OneBlockPlugin oneBlock = OneBlockPlugin.getInstance();
        if (oneBlock == null) throw new IllegalStateException("OneBlock plugin dependency is unavailable");
        oneBlock.setOwnerResolver((world, playerId) -> world == null
                ? playerId
                : islands.findByWorld(world.getName())
                        .filter(island -> island.canEnter(playerId))
                        .map(IslandRecord::ownerUuid)
                        .orElse(playerId));
        oneBlock.setAccessResolver((world, playerId, root) -> world == null
                || islands.findByWorld(world.getName())
                        .map(island -> island.canEdit(playerId))
                        .orElse(true));
        getCommandRegistry().registerCommand(new IslandCommand(islands, islandWorlds));
        getEntityStoreRegistry().registerSystem(new IslandMutationSystems.Damage(islands));
        getEntityStoreRegistry().registerSystem(new IslandMutationSystems.Break(islands));
        getEntityStoreRegistry().registerSystem(new IslandMutationSystems.Place(islands));
        getEntityStoreRegistry().registerSystem(new IslandMutationSystems.UseBlock(islands));
        getEntityStoreRegistry().registerSystem(new IslandMutationSystems.UseEntity(islands));
        getEntityStoreRegistry().registerSystem(new IslandFallProtectionSystem(islands));
        getEventRegistry().registerGlobal(PrepareUniverseEvent.class,
                event -> event.setWorldConfigProvider(islandWorlds.worldConfigProvider(event.getWorldConfigProvider())));
        getEventRegistry().registerGlobal(AddWorldEvent.class, event -> islandWorlds.initialize(event.getWorld())
                .exceptionally(error -> { LOGGER.at(Level.SEVERE).withCause(error).log("Island initialization failed"); return null; }));
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
            Ref<EntityStore> playerEntityRef = event.getPlayerRef();
            PlayerRef player = playerEntityRef == null || playerEntityRef.getStore() == null
                    ? null
                    : playerEntityRef.getStore().getComponent(playerEntityRef, PlayerRef.getComponentType());
            if (player != null) redirectIfUnauthorized(player);
        });
        getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, event -> {
            PlayerRef player = event.getHolder() == null ? null : event.getHolder().getComponent(PlayerRef.getComponentType());
            if (player != null) redirectIfUnauthorized(player);
        });
        LOGGER.at(Level.INFO).log("OneBlockIslands setup complete; UUID ownership database loaded.");
    }

    @Override protected void shutdown() {
        OneBlockPlugin oneBlock = OneBlockPlugin.getInstance();
        if (oneBlock != null) {
            oneBlock.setOwnerResolver(null);
            oneBlock.setAccessResolver(null);
        }
        instance = null;
        islands = null;
        islandWorlds = null;
    }

    void redirectIfUnauthorized(PlayerRef player) {
        if (player == null || player.getReference() == null || !player.getReference().isValid()) return;
        EntityStore entityStore = player.getReference().getStore().getExternalData();
        World world = entityStore == null ? null : entityStore.getWorld();
        IslandRecord island = world == null ? null : islands.findByWorld(world.getName()).orElse(null);
        if (island == null) return;
        if (IslandAccess.mayEnter(islands, world, player)) {
            OneBlockPlugin oneBlock = OneBlockPlugin.getInstance();
            Ref<EntityStore> playerEntityRef = player.getReference();
            Player entity = playerEntityRef.getStore().getComponent(playerEntityRef, Player.getComponentType());
            if (oneBlock != null && entity != null) oneBlock.restoreHud(entity, world, island.ownerUuid());
            return;
        }
        LOGGER.at(Level.WARNING).log("Redirecting unauthorized player " + player.getUuid() + " from island world " + world.getName());
        transferToDefault(player).exceptionally(error -> { LOGGER.at(Level.SEVERE).withCause(error).log("Failed to redirect unauthorized player"); return null; });
    }

    private static CompletableFuture<Void> transferToDefault(PlayerRef player) {
        Universe universe = Universe.get();
        if (universe == null) return CompletableFuture.failedFuture(new IllegalStateException("Universe is not ready"));

        World target = universe.getDefaultWorld();
        if (target == null) return CompletableFuture.failedFuture(new IllegalStateException("The default world is not loaded"));

        EntityStore entityStore = player.getReference().getStore().getExternalData();
        World source = entityStore == null ? null : entityStore.getWorld();
        if (source == null) return CompletableFuture.failedFuture(new IllegalStateException("The player's current world is unavailable"));
        if (target.equals(source)) return CompletableFuture.completedFuture(null);

        return Universe.transferPlayerAsync(
                player,
                source,
                CompletableFuture.completedFuture(target),
                world -> configuredSpawn(world, player)
        ).thenApply(ignored -> null);
    }

    private static Transform configuredSpawn(World world, PlayerRef player) {
        WorldConfig config = world.getWorldConfig();
        ISpawnProvider provider = config == null ? null : config.getSpawnProvider();
        Transform spawn = provider == null ? null : provider.getSpawnPoint(world, player.getUuid());
        if (spawn == null) throw new IllegalStateException("The default world has no configured spawn point");
        return spawn;
    }
}
