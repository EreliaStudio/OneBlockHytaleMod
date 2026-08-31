package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.PrepareUniverseEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.WorldConfigProvider;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class OneBlockPlugin extends JavaPlugin
{
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static OneBlockPlugin instance;

    private OneBlockDropRegistry dropRegistry;
    private OneBlockWorldStateRegistry worldStateRegistry;
    private OneBlockRootRegistry rootRegistry;
    private OneBlockWorldService worldService;
    private OneBlockHudService hudService;
    private OneBlockSettingsProvider settingsProvider;

    public OneBlockPlugin(@Nonnull JavaPluginInit init)
    {
        super(init);
        instance = this;
    }

    public static OneBlockPlugin getInstance()
    {
        return instance;
    }

    @Override
    protected void setup()
    {
        LOGGER.at(Level.INFO).log("Setting up OneBlock...");

        // ── Services ─────────────────────────────────────────────────────────
        hudService = new OneBlockHudService();
        settingsProvider = new OneBlockSettingsProvider(
                getDataDirectory().resolve("oneblock-settings.json")
        );

        // ── Drop engine ──────────────────────────────────────────────────────
        dropRegistry = new OneBlockDropRegistry();

        worldStateRegistry = new OneBlockWorldStateRegistry(getDataDirectory());
        rootRegistry = new OneBlockRootRegistry(getDataDirectory());
        worldService = new OneBlockWorldService(worldStateRegistry, hudService);

        dropRegistry.registerDropable(new ItemDropable(OneBlockDropRegistry.DEFAULT_ITEM_ID));

        getEntityStoreRegistry().registerSystem(
                new OneBlockDamageSystem(worldStateRegistry, rootRegistry)
        );

        getEntityStoreRegistry().registerSystem(
                new OneBlockBreakSystem(
                        dropRegistry,
                        worldStateRegistry,
                        rootRegistry
                )
        );

        getEntityStoreRegistry().registerSystem(new OneBlockPlacementSystem(rootRegistry));

        getCommandRegistry().registerCommand(new OneBlockCommand());

        // ── Expedition progression ───────────────────────────────────────────
        OneBlockPools.setResolver(new OneBlockExpeditionPoolResolver());

        Map<String, Map<String, Integer>> defaultWeights =
                OneBlockExpeditionDefaults.getDefaultWeights();

        dropRegistry.registerDefaultWeights(defaultWeights);

        Map<String, Set<String>> dropsByExpedition =
                OneBlockExpeditionDefaults.getDefaultDropIdsByExpedition();

        for (Map.Entry<String, Set<String>> entry : dropsByExpedition.entrySet())
        {
            registerDropables(dropRegistry, entry.getValue());
        }

        registerDropables(dropRegistry, OneBlockExpeditionDefaults.getCompletionRewardDropIds());
        registerDropables(dropRegistry, OneBlockDungeonDefaults.getAllEntityIds());
        registerDropables(dropRegistry, OneBlockDungeonDefaults.getCompletionRewardDropIds());

        getCodecRegistry(Interaction.CODEC).register(
                OneBlockCrystalInteraction.INTERACTION_ID,
                OneBlockCrystalInteraction.class,
                OneBlockCrystalInteraction.CODEC
        );

        // ── World ────────────────────────────────────────────────────────────
        getEntityStoreRegistry().registerSystem(new OneBlockFallBackSystem(settingsProvider, worldStateRegistry));

        getEventRegistry().registerGlobal(PrepareUniverseEvent.class, event ->
        {
            WorldConfigProvider original = event.getWorldConfigProvider();

            event.setWorldConfigProvider(new WorldConfigProvider()
            {
                @Override
                public CompletableFuture<WorldConfig> load(Path path, String worldName)
                {
                    CompletableFuture<WorldConfig> future = original.load(path, worldName);

                    if (!worldStateRegistry.isManaged(worldName))
                    {
                        return future;
                    }

                    return future.thenApply(config ->
                    {
                        if (config != null)
                        {
                            config.setWorldGenProvider(OneBlockWorldInitializer.voidWorldGenProvider());
                            config.markChanged();
                        }

                        return config;
                    });
                }

                @Override
                public CompletableFuture<Void> save(Path path, WorldConfig config, World world)
                {
                    return original.save(path, config, world);
                }
            });
        });

        getEventRegistry().registerGlobal(AddWorldEvent.class, event ->
        {
            World world = event.getWorld();

            if (worldStateRegistry.isManaged(world))
            {
                OneBlockWorldBootstrap.ensureVoidWorldAtSavePath(world.getSavePath());
                OneBlockWorldInitializer.initializeWorld(world, resolveActiveBlockId(world));
            }
        });

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event ->
        {
            Ref<EntityStore> playerEntityRef = event.getPlayerRef();
            EntityStore entityStore = playerEntityRef == null || playerEntityRef.getStore() == null
                    ? null
                    : playerEntityRef.getStore().getExternalData();
            World world = entityStore == null ? null : entityStore.getWorld();
            Player player = event.getPlayer();
            if (!worldStateRegistry.isManaged(world))
            {
                PlayerRef playerRef = player == null ? null : player.getPlayerRef();
                if (playerRef != null && rootRegistry.hasRoots(world, playerRef.getUuid()))
                {
                    restoreRootHud(player, world, playerRef.getUuid());
                }
                return;
            }

            OneBlockDungeonStateProvider dungeonState = worldStateRegistry.dungeonState(world);
            if (dungeonState.isDungeonActive())
            {
                String dungeonId = dungeonState.getActiveDungeonId();
                int totalWaves = OneBlockDungeonDefaults.getWaveCount(dungeonId);
                hudService.showDungeonStarted(player, dungeonId, totalWaves);
                hudService.updateDungeonWave(player, dungeonId, dungeonState.getCurrentWaveIndex(), totalWaves);
                return;
            }

            OneBlockExpeditionStateProvider expeditionState = worldStateRegistry.expeditionState(world);
            if (!expeditionState.hasActiveExpedition())
            {
                hudService.clear(player);
                return;
            }

            hudService.restoreExpeditionHud(
                    player,
                    expeditionState.getActiveExpeditionId(),
                    expeditionState.getTicksRemaining(),
                    expeditionState.getTotalTicks()
            );
        });

        LOGGER.at(Level.INFO).log("Setup complete.");
    }

    @Override
    protected void start()
    {
        LOGGER.at(Level.INFO).log("Started.");
    }

    @Override
    protected void shutdown()
    {
        LOGGER.at(Level.INFO).log("Shutting down...");

        instance = null;
        hudService = null;
        settingsProvider = null;
        dropRegistry = null;
        worldService = null;
        worldStateRegistry = null;
        rootRegistry = null;
    }

    public OneBlockExpeditionStateProvider getExpeditionStateProvider()
    {
        return worldStateRegistry == null || !worldStateRegistry.isManaged(World.DEFAULT)
                ? null
                : worldStateRegistry.expeditionState(World.DEFAULT);
    }

    public OneBlockDungeonStateProvider getDungeonStateProvider()
    {
        return worldStateRegistry == null || !worldStateRegistry.isManaged(World.DEFAULT)
                ? null
                : worldStateRegistry.dungeonState(World.DEFAULT);
    }

    public OneBlockWorldStateRegistry getWorldStateRegistry()
    {
        return worldStateRegistry;
    }

    public OneBlockWorldService getWorldService()
    {
        return worldService;
    }

    public OneBlockRootRegistry getRootRegistry()
    {
        return rootRegistry;
    }

    public OneBlockExpeditionStateProvider getExpeditionStateProvider(World world)
    {
        return worldStateRegistry == null ? null : worldStateRegistry.expeditionState(world);
    }

    public OneBlockDungeonStateProvider getDungeonStateProvider(World world)
    {
        return worldStateRegistry == null ? null : worldStateRegistry.dungeonState(world);
    }

    public OneBlockDropRegistry getDropRegistry()
    {
        return dropRegistry;
    }

    public OneBlockHudService getHudService()
    {
        return hudService;
    }

    public OneBlockSettingsProvider getSettingsProvider()
    {
        return settingsProvider;
    }

    private String resolveActiveBlockId(World world)
    {
        OneBlockDungeonStateProvider dungeonStateProvider = worldStateRegistry.dungeonState(world);
        if (dungeonStateProvider.isDungeonActive())
        {
            String dungeonId = dungeonStateProvider.getActiveDungeonId();
            String blockId = OneBlockDungeonDefaults.getBlockId(dungeonId);
            return blockId != null ? blockId : OneBlockBlockIds.DEFAULT_BLOCK_ID;
        }

        OneBlockExpeditionStateProvider expeditionStateProvider = worldStateRegistry.expeditionState(world);
        if (expeditionStateProvider.hasActiveExpedition())
        {
            String expeditionId = expeditionStateProvider.getActiveExpeditionId();
            return OneBlockExpeditionResolver.blockIdForExpedition(expeditionId);
        }

        return OneBlockBlockIds.DEFAULT_BLOCK_ID;
    }

    private void restoreRootHud(Player player, World world, java.util.UUID ownerId)
    {
        OneBlockDungeonStateProvider dungeonState = rootRegistry.dungeonState(world, ownerId);
        if (dungeonState.isDungeonActive())
        {
            String dungeonId = dungeonState.getActiveDungeonId();
            int totalWaves = OneBlockDungeonDefaults.getWaveCount(dungeonId);
            hudService.showDungeonStarted(player, dungeonId, totalWaves);
            hudService.updateDungeonWave(
                    player,
                    dungeonId,
                    dungeonState.getCurrentWaveIndex(),
                    totalWaves
            );
            return;
        }

        OneBlockExpeditionStateProvider expeditionState = rootRegistry.expeditionState(world, ownerId);
        if (!expeditionState.hasActiveExpedition())
        {
            hudService.clear(player);
            return;
        }

        hudService.restoreExpeditionHud(
                player,
                expeditionState.getActiveExpeditionId(),
                expeditionState.getTicksRemaining(),
                expeditionState.getTotalTicks()
        );
    }

    private static void registerDropables(OneBlockDropRegistry registry, Iterable<String> dropableIds)
    {
        if (registry == null || dropableIds == null)
        {
            return;
        }

        for (String dropableId : dropableIds)
        {
            if (dropableId == null || dropableId.isEmpty())
            {
                continue;
            }

            OneBlockDropId parsed = OneBlockDropId.parse(dropableId);

            Dropable dropable = parsed.isEntity()
                    ? new EntitySpawnDropable(dropableId)
                    : new ItemDropable(dropableId);

            registry.registerDropable(dropable);
        }
    }
}
