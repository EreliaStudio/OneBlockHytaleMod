package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public final class OneBlockPlugin extends JavaPlugin
{
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static OneBlockPlugin instance;

    private OneBlockDropRegistry dropRegistry;
    private OneBlockExpeditionStateProvider expeditionStateProvider;

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
        LOGGER.at(Level.INFO).log("Setting up OneBlock Core...");

        dropRegistry = new OneBlockDropRegistry();
        expeditionStateProvider = new OneBlockExpeditionStateProvider(
                getDataDirectory().resolve("oneblock-expedition.json"));

        dropRegistry.registerDropable(new ItemDropable(OneBlockDropRegistry.DEFAULT_ITEM_ID));

        getEntityStoreRegistry().registerSystem(new OneBlockBreakSystem(dropRegistry, expeditionStateProvider));
        getCommandRegistry().registerCommand(new OneBlockCommand());

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
    }

    public OneBlockExpeditionStateProvider getExpeditionStateProvider()
    {
        return expeditionStateProvider;
    }

    public OneBlockDropRegistry getDropRegistry()
    {
        return dropRegistry;
    }

    private BiConsumer<String, DropableContext> expeditionCompleteCallback;

    public void setExpeditionCompleteCallback(BiConsumer<String, DropableContext> callback)
    {
        this.expeditionCompleteCallback = callback;
    }

    public void fireExpeditionComplete(String expeditionId, DropableContext context)
    {
        if (expeditionCompleteCallback != null && expeditionId != null)
            expeditionCompleteCallback.accept(expeditionId, context);
    }
}
