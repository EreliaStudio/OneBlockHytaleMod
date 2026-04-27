package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.builtin.crafting.CraftingPlugin;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Ref;

import javax.annotation.Nonnull;
import java.util.List;
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

        // Install expedition-aware pool resolver
        OneBlockPools.setResolver(new OneBlockExpeditionPoolResolver());

        // Register all expedition drop pools with the core registry
        Map<String, Map<String, Integer>> defaultWeights = OneBlockExpeditionDefaults.getDefaultWeights();
        base.getDropRegistry().registerDefaultWeights(defaultWeights);

        Map<String, Set<String>> defaultDropIdsByExpedition = OneBlockExpeditionDefaults.getDefaultDropIdsByExpedition();
        for (Map.Entry<String, Set<String>> entry : defaultDropIdsByExpedition.entrySet())
        {
            registerDropables(base.getDropRegistry(), entry.getValue());
        }

        // Register completion callback — grants knowledge for each expedition's unlock IDs
        base.setExpeditionCompleteCallback((expeditionId, ctx) -> onExpeditionComplete(expeditionId, ctx));

        // Register crystal interaction
        getCodecRegistry(Interaction.CODEC).register(
			OneBlockCrystalInteraction.INTERACTION_ID,
			OneBlockCrystalInteraction.class,
			OneBlockCrystalInteraction.CODEC
		);

		getCodecRegistry(Interaction.CODEC).register(
			OneBlockUnlockInteraction.INTERACTION_ID,
			OneBlockUnlockInteraction.class,
			OneBlockUnlockInteraction.CODEC
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

    private void onExpeditionComplete(String expeditionId, DropableContext ctx)
    {
        if (ctx == null) return;

        List<String> knowledgeIds = OneBlockExpeditionDefaults.getUnlockKnowledgeIds(expeditionId);
        if (knowledgeIds.isEmpty())
        {
            LOGGER.at(Level.WARNING).log("Expedition complete: " + expeditionId + " — no knowledge IDs defined, nothing to unlock.");
            return;
        }

        LOGGER.at(Level.INFO).log("Expedition complete: " + expeditionId + ". Granting " + knowledgeIds.size() + " knowledge ID(s): " + knowledgeIds);
        for (String knowledgeId : knowledgeIds)
        {
            grantKnowledge(ctx, knowledgeId);
        }
    }

    private void grantKnowledge(DropableContext ctx, String itemId)
{
    PlayerRef playerRef = ctx.getPlayerRef();

    Ref<EntityStore> playerEntityRef = playerRef.getReference();

    if (playerEntityRef == null)
    {
        LOGGER.at(Level.WARNING).log("Cannot grant recipe '" + itemId + "': player has no valid entity reference.");
        return;
    }

    boolean learned = CraftingPlugin.learnRecipe(
        playerEntityRef,
        itemId,
        playerEntityRef.getStore()
    );

    if (learned)
    {
        LOGGER.at(Level.INFO).log("Granted recipe '" + itemId + "' to player " + playerRef.getUuid());
    }
    else
    {
        LOGGER.at(Level.INFO).log("Recipe '" + itemId + "' was already known by player " + playerRef.getUuid());
    }
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
