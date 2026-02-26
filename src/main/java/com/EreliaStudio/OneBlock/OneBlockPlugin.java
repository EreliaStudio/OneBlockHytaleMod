package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class OneBlockPlugin extends JavaPlugin
{
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static OneBlockPlugin instance;

    private OneBlockDropRegistry dropRegistry;
    private OneBlockDropsStateProvider dropsStateProvider;
    private OneBlockUnlockService unlockService;

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
        LOGGER.at(Level.INFO).log("Setting up...");

        OneBlockWorldBootstrap.ensureVoidDefaultWorldConfig(getDataDirectory());

        dropRegistry = new OneBlockDropRegistry();

        // File-backed provider so unlocks persist across restarts
        dropsStateProvider = new FileBackedOneBlockDropsStateProvider(
                getDataDirectory().resolve("oneblock-drops.json")
        );

        getEntityStoreRegistry().registerSystem(new OneBlockBreakSystem(dropRegistry, dropsStateProvider));

        LOGGER.at(Level.INFO).log("Default drop is " + OneBlockDropRegistry.DEFAULT_ITEM_ID);

        var consumableToDropMap = OneBlockUnlockRecipeLoader.loadConsumableToDropMap(
                OneBlockPlugin.class,
                "/Server/Item/Items/OneBlockUpgrader/Bench_OneBlockUpgrader.json",
                "/Server/Item/Items/UnlockRecipe"
        );

        dropRegistry.registerDefaultWeights(OneBlockExpeditionDefaults.getDefaultWeights());
        dropRegistry.registerWeights(consumableToDropMap.values());
        unlockService = new OneBlockUnlockService(dropsStateProvider, consumableToDropMap);
        LOGGER.at(Level.INFO).log("Loaded unlock recipes: " + consumableToDropMap.size());

        getCommandRegistry().registerCommand(new OneBlockCommand());
        LOGGER.at(Level.INFO).log("Adding the OneBlock commands");

        getCodecRegistry(Interaction.CODEC).register(
                OneBlockUnlockInteraction.INTERACTION_ID,
                OneBlockUnlockInteraction.class,
                OneBlockUnlockInteraction.CODEC
        );
        LOGGER.at(Level.INFO).log("Registered interaction: " + OneBlockUnlockInteraction.INTERACTION_ID);

        getCodecRegistry(Interaction.CODEC).register(
                OneBlockExpeditionInteraction.INTERACTION_ID,
                OneBlockExpeditionInteraction.class,
                OneBlockExpeditionInteraction.CODEC
        );
        LOGGER.at(Level.INFO).log("Registered interaction: " + OneBlockExpeditionInteraction.INTERACTION_ID);

        getEventRegistry().registerGlobal(AddWorldEvent.class, event ->
        {
            var world = event.getWorld();
            if (!OneBlockWorldInitializer.isDefaultWorld(world))
            {
                return;
            }
            OneBlockWorldInitializer.initializeWorld(world);
        });
        LOGGER.at(Level.INFO).log("Registered OneBlock world initializer");

        LOGGER.at(Level.INFO).log("Setup complete!");
    }

    @Override
    protected void start()
    {
        LOGGER.at(Level.INFO).log("Started!");
    }

    @Override
    protected void shutdown()
    {
        LOGGER.at(Level.INFO).log("Shutting down...");
        if (dropsStateProvider instanceof FileBackedOneBlockDropsStateProvider fileProvider)
        {
            fileProvider.saveIfDirty();
        }
        instance = null;
    }

    public OneBlockDropsStateProvider getDropsStateProvider()
    {
        return dropsStateProvider;
    }

    public OneBlockUnlockService getUnlockService()
    {
        return unlockService;
    }
}
