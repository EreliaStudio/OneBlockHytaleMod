package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public final class OneBlockSalvagerPlugin extends JavaPlugin
{
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public OneBlockSalvagerPlugin(@Nonnull JavaPluginInit init)
    {
        super(init);
    }

    @Override
    protected void setup()
    {
        LOGGER.at(Level.INFO).log("Setting up OneBlock Salvager...");
        getChunkStoreRegistry().registerSystem(new OneBlockSalvageChanceSystem());
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
    }
}
