package com.EreliaStudio.OneBlockIslands;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.events.StartWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.logging.Level;

public final class OneBlockIslandsPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static OneBlockIslandsPlugin instance;
    private IslandStore islands;
    private HubService hub;

    public OneBlockIslandsPlugin(@Nonnull JavaPluginInit init) { super(init); instance = this; }
    static OneBlockIslandsPlugin getInstance() { return instance; }

    @Override protected void setup() {
        islands = new IslandStore(getDataDirectory().resolve("islands.json"));
        try { islands.load(); }
        catch (IOException e) { throw new IllegalStateException("Refusing to start with an invalid island database: " + e.getMessage(), e); }
        hub = new HubService();
        getCommandRegistry().registerCommand(new IslandCommand(islands));
        getEntityStoreRegistry().registerSystem(new IslandMutationSystems.Damage(islands));
        getEntityStoreRegistry().registerSystem(new IslandMutationSystems.Break(islands));
        getEntityStoreRegistry().registerSystem(new IslandMutationSystems.Place(islands));
        getEntityStoreRegistry().registerSystem(new IslandMutationSystems.UseBlock(islands));
        getEntityStoreRegistry().registerSystem(new IslandMutationSystems.UseEntity(islands));
        getEventRegistry().registerGlobal(AddWorldEvent.class, event -> hub.worldAdded(event.getWorld()));
        getEventRegistry().registerGlobal(StartWorldEvent.class, event -> hub.worldStarted(event.getWorld()));
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event -> {
            PlayerRef player = event.getPlayer() == null ? null : event.getPlayer().getPlayerRef();
            if (player != null) redirectIfUnauthorized(player);
        });
        getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, event -> {
            PlayerRef player = event.getHolder() == null ? null : event.getHolder().getComponent(PlayerRef.getComponentType());
            if (player != null) redirectIfUnauthorized(player);
        });
        LOGGER.at(Level.INFO).log("OneBlockIslands setup complete; UUID ownership database loaded.");
    }

    @Override protected void start() {
        hub.ensureLoaded().whenComplete((world, error) -> {
            if (error != null) LOGGER.at(Level.SEVERE).withCause(error).log("Spawn world initialization failed");
            else LOGGER.at(Level.INFO).log("Spawn world ready: " + world.getName());
        });
    }

    @Override protected void shutdown() { instance = null; islands = null; hub = null; }

    void redirectIfUnauthorized(PlayerRef player) {
        if (player == null || player.getReference() == null || !player.getReference().isValid()) return;
        EntityStore entityStore = player.getReference().getStore().getExternalData();
        World world = entityStore == null ? null : entityStore.getWorld();
        if (world == null || (islands.findByWorld(world.getName()).isEmpty() && !IslandAccess.isManagedOneBlockWorld(world))
                || IslandAccess.mayEnter(islands, world, player)) return;
        LOGGER.at(Level.WARNING).log("Redirecting unauthorized player " + player.getUuid() + " from island world " + world.getName());
        hub.transfer(player).exceptionally(error -> { LOGGER.at(Level.SEVERE).withCause(error).log("Failed to redirect unauthorized player"); return null; });
    }
}
