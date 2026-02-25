package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.logger.HytaleLogger;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public class OneBlockPlugin extends JavaPlugin
{
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static OneBlockPlugin instance;

    private OneBlockDropRegistry dropRegistry;
    private OneBlockDropsStateProvider dropsStateProvider;
    private UnlockCatalog unlockCatalog;

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

        dropRegistry = new OneBlockDropRegistry();

        // Step 0: temporary in-memory provider (later replaced by persistent component provider)
        dropsStateProvider = new InMemoryOneBlockDropsStateProvider();

        getEntityStoreRegistry().registerSystem(new OneBlockBreakSystem(dropRegistry, dropsStateProvider));

        LOGGER.at(Level.INFO).log("Default drop is " + OneBlockDropRegistry.DEFAULT_ITEM_ID);

        unlockCatalog = UnlockCatalogLoader.loadFromResources(OneBlockPlugin.class, "/oneblock_unlocks.json");
        LOGGER.at(Level.INFO).log("Loaded unlock catalog. Categories: " + unlockCatalog.categories.size());

        getCommandRegistry().registerCommand(new OneBlockCommand());
        LOGGER.at(Level.INFO).log("Adding the OneBlock commands");

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
        instance = null;
    }

    public OneBlockDropsStateProvider getDropsStateProvider()
    {
        return dropsStateProvider;
    }
}