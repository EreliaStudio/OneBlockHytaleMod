package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;



public final class OneBlockProgressionPlugin extends JavaPlugin
{
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static OneBlockProgressionPlugin instance;

    public OneBlockProgressionPlugin(@Nonnull JavaPluginInit init)
    {
        super(init);
        instance = this;
    }

    public static OneBlockProgressionPlugin getInstance()
    {
        return instance;
    }

    @Override
    protected void setup()
    {
        LOGGER.at(Level.INFO).log("Setting up OneBlock Progression...");

        OneBlockPlugin base = OneBlockPlugin.getInstance();
        if (base == null)
        {
            LOGGER.at(Level.WARNING).log("OneBlock Core plugin not available; progression features disabled.");
            return;
        }

        OneBlockPools.setResolver(new OneBlockExpeditionPoolResolver());

        Map<String, Map<String, Integer>> defaultWeights = OneBlockExpeditionDefaults.getDefaultWeights();
        base.getDropRegistry().registerDefaultWeights(defaultWeights);

        Map<String, Set<String>> defaultDropIdsByExpedition = OneBlockExpeditionDefaults.getDefaultDropIdsByExpedition();
        for (Map.Entry<String, Set<String>> entry : defaultDropIdsByExpedition.entrySet())
        {
            registerDropables(base.getDropRegistry(), entry.getValue());
        }

        getCodecRegistry(Interaction.CODEC).register(
            OneBlockCrystalInteraction.INTERACTION_ID,
            OneBlockCrystalInteraction.class,
            OneBlockCrystalInteraction.CODEC
        );

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

    private static void registerDropables(OneBlockDropRegistry registry, Iterable<String> dropableIds)
    {
        if (registry == null || dropableIds == null) return;
        for (String dropableId : dropableIds)
        {
            if (dropableId == null || dropableId.isEmpty()) continue;
            OneBlockDropId parsed = OneBlockDropId.parse(dropableId);
            Dropable dropable = parsed.isEntity()
                    ? new EntitySpawnDropable(dropableId)
                    : new ItemDropable(dropableId);
            registry.registerDropable(dropable);
        }
    }
}
