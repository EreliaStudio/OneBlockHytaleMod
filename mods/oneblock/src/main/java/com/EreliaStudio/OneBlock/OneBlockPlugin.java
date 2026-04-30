package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.event.events.PrepareUniverseEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.WorldConfigProvider;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;

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
    private OneBlockExpeditionStateProvider expeditionStateProvider;
    private OneBlockDungeonStateProvider dungeonStateProvider;
    private OneBlockHudService hudService;

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

        // ── Drop engine ──────────────────────────────────────────────────────
        dropRegistry = new OneBlockDropRegistry();

        expeditionStateProvider = new OneBlockExpeditionStateProvider(
                getDataDirectory().resolve("oneblock-expedition.json")
        );

        dungeonStateProvider = new OneBlockDungeonStateProvider(
                getDataDirectory().resolve("oneblock-dungeon.json")
        );

        dropRegistry.registerDropable(new ItemDropable(OneBlockDropRegistry.DEFAULT_ITEM_ID));

        getEntityStoreRegistry().registerSystem(
                new OneBlockBreakSystem(
                        dropRegistry,
                        expeditionStateProvider,
                        dungeonStateProvider
                )
        );

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
        OneBlockWorldBootstrap.ensureVoidDefaultWorldConfig(getDataDirectory());

        getEntityStoreRegistry().registerSystem(new OneBlockFallBackSystem());

        getEventRegistry().registerGlobal(PrepareUniverseEvent.class, event ->
        {
            WorldConfigProvider original = event.getWorldConfigProvider();

            event.setWorldConfigProvider(new WorldConfigProvider()
            {
                @Override
                public CompletableFuture<WorldConfig> load(Path path, String worldName)
                {
                    CompletableFuture<WorldConfig> future = original.load(path, worldName);

                    if (!World.DEFAULT.equals(worldName))
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

            if (OneBlockWorldInitializer.isDefaultWorld(world))
            {
                OneBlockWorldBootstrap.ensureVoidWorldAtSavePath(world.getSavePath());
                OneBlockWorldInitializer.initializeWorld(world, resolveActiveBlockId());
            }
        });

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event ->
        {
            if (!expeditionStateProvider.hasActiveExpedition())
            {
                return;
            }

            Player player = event.getPlayer();
            String expeditionId = expeditionStateProvider.getActiveExpeditionId();
            int ticksRemaining = expeditionStateProvider.getTicksRemaining();
            int totalTicks = expeditionStateProvider.getTotalTicks();

            hudService.restoreExpeditionHud(player, expeditionId, ticksRemaining, totalTicks);
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
        dropRegistry = null;
        expeditionStateProvider = null;
        dungeonStateProvider = null;
    }

    public OneBlockExpeditionStateProvider getExpeditionStateProvider()
    {
        return expeditionStateProvider;
    }

    public OneBlockDungeonStateProvider getDungeonStateProvider()
    {
        return dungeonStateProvider;
    }

    public OneBlockDropRegistry getDropRegistry()
    {
        return dropRegistry;
    }

    public OneBlockHudService getHudService()
    {
        return hudService;
    }

    private String resolveActiveBlockId()
    {
        if (dungeonStateProvider != null && dungeonStateProvider.isDungeonActive())
        {
            String dungeonId = dungeonStateProvider.getActiveDungeonId();
            String blockId = OneBlockDungeonDefaults.getBlockId(dungeonId);
            return blockId != null ? blockId : OneBlockBlockIds.DEFAULT_BLOCK_ID;
        }

        if (expeditionStateProvider != null && expeditionStateProvider.hasActiveExpedition())
        {
            String expeditionId = expeditionStateProvider.getActiveExpeditionId();
            return OneBlockExpeditionResolver.blockIdForExpedition(expeditionId);
        }

        return OneBlockBlockIds.DEFAULT_BLOCK_ID;
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