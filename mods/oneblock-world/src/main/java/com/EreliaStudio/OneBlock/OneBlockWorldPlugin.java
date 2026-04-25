package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.events.AddWorldEvent;

import javax.annotation.Nonnull;
import java.util.logging.Level;

public final class OneBlockWorldPlugin extends JavaPlugin
{
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public OneBlockWorldPlugin(@Nonnull JavaPluginInit init)
    {
        super(init);
    }

    @Override
    protected void setup()
    {
        LOGGER.at(Level.INFO).log("Setting up OneBlock World...");

        OneBlockWorldBootstrap.ensureVoidDefaultWorldConfig(getDataDirectory());
        getEntityStoreRegistry().registerSystem(new OneBlockFallBackSystem());

        getEventRegistry().registerGlobal(AddWorldEvent.class, event ->
        {
            var world = event.getWorld();
            if (OneBlockWorldInitializer.isDefaultWorld(world))
                OneBlockWorldInitializer.initializeWorld(world);
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
    }
}
