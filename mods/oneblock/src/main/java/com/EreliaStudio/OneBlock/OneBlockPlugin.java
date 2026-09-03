package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3i;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/** Provides position-scoped OneBlock gameplay without owning world lifecycle. */
public final class OneBlockPlugin extends JavaPlugin
{
    private static OneBlockPlugin instance;
    private OneBlockDropRegistry dropRegistry;
    private OneBlockRootRegistry rootRegistry;
    private OneBlockHudService hudService;
    private volatile OneBlockOwnerResolver ownerResolver = (world, playerId) -> playerId;
    private volatile OneBlockAccessResolver accessResolver = (world, playerId, root) -> true;
    private final CopyOnWriteArrayList<OneBlockProgressListener> progressListeners = new CopyOnWriteArrayList<>();

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
        hudService = new OneBlockHudService();
        rootRegistry = new OneBlockRootRegistry(getDataDirectory());
        dropRegistry = new OneBlockDropRegistry();

        dropRegistry.registerDropable(new ItemDropable(OneBlockDropRegistry.DEFAULT_ITEM_ID));
        getEntityStoreRegistry().registerSystem(new OneBlockDamageSystem(rootRegistry));
        getEntityStoreRegistry().registerSystem(new OneBlockBreakSystem(dropRegistry, rootRegistry));
        getEntityStoreRegistry().registerSystem(new OneBlockPlacementSystem(rootRegistry));
        getCommandRegistry().registerCommand(new OneBlockCommand());

        OneBlockPools.setResolver(new OneBlockExpeditionPoolResolver());
        dropRegistry.registerDefaultWeights(OneBlockExpeditionDefaults.getDefaultWeights());
        Map<String, Set<String>> dropsByExpedition = OneBlockExpeditionDefaults.getDefaultDropIdsByExpedition();
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

        getEventRegistry().registerGlobal(PlayerReadyEvent.class, event ->
        {
            Player player = event.getPlayer();
            Ref<EntityStore> playerEntityRef = event.getPlayerRef();
            EntityStore entityStore = playerEntityRef == null || playerEntityRef.getStore() == null
                    ? null
                    : playerEntityRef.getStore().getExternalData();
            World world = entityStore == null ? null : entityStore.getWorld();
            PlayerRef playerRef = playerEntityRef == null || playerEntityRef.getStore() == null
                    ? null
                    : playerEntityRef.getStore().getComponent(playerEntityRef, PlayerRef.getComponentType());
            updateHudForWorld(playerRef, player, world);
        });
        getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, event ->
        {
            PlayerRef playerRef = event.getHolder() == null
                    ? null
                    : event.getHolder().getComponent(PlayerRef.getComponentType());
            Player player = event.getHolder() == null
                    ? null
                    : event.getHolder().getComponent(Player.getComponentType());
            World world = event.getWorld();
            updateHudForWorld(playerRef, player, world);
        });
    }

    @Override
    protected void shutdown()
    {
        instance = null;
        hudService = null;
        dropRegistry = null;
        rootRegistry = null;
        ownerResolver = (world, playerId) -> playerId;
        accessResolver = (world, playerId, root) -> true;
    }

    public OneBlockRootRegistry getRootRegistry()
    {
        return rootRegistry;
    }

    /** Registers and places a OneBlock at an arbitrary position in an existing world. */
    public OneBlockRootRegistry.RootEntry initializeRoot(World world,
                                                         Vector3i position,
                                                         UUID ownerId,
                                                         String ownerName)
    {
        if (rootRegistry == null) throw new IllegalStateException("OneBlock root registry is unavailable");
        OneBlockRootRegistry.RootEntry root = rootRegistry.register(world, position, ownerId, ownerName);
        world.setBlock(position.x(), position.y(), position.z(), OneBlockBlockIds.DEFAULT_BLOCK_ID);
        return root;
    }

    public OneBlockDropRegistry getDropRegistry()
    {
        return dropRegistry;
    }

    public OneBlockHudService getHudService()
    {
        return hudService;
    }

    public void setOwnerResolver(OneBlockOwnerResolver resolver)
    {
        ownerResolver = resolver == null ? (world, playerId) -> playerId : resolver;
    }

    public UUID resolveOwner(World world, UUID playerId)
    {
        UUID resolved = ownerResolver.resolveOwner(world, playerId);
        return resolved == null ? playerId : resolved;
    }

    public void setAccessResolver(OneBlockAccessResolver resolver)
    {
        accessResolver = resolver == null ? (world, playerId, root) -> true : resolver;
    }

    public boolean mayUse(World world, UUID playerId, OneBlockRootRegistry.RootEntry root)
    {
        return playerId != null && root != null && accessResolver.mayUse(world, playerId, root);
    }

    public void addProgressListener(OneBlockProgressListener listener)
    {
        if (listener != null) progressListeners.addIfAbsent(listener);
    }

    public void removeProgressListener(OneBlockProgressListener listener)
    {
        progressListeners.remove(listener);
    }

    void expeditionUnlocked(UUID playerId, String expeditionId)
    {
        for (OneBlockProgressListener listener : progressListeners) listener.onExpeditionUnlocked(playerId, expeditionId);
    }

    void expeditionCompleted(UUID playerId, String expeditionId)
    {
        for (OneBlockProgressListener listener : progressListeners) listener.onExpeditionCompleted(playerId, expeditionId);
    }

    void dungeonCompleted(UUID playerId, String dungeonId)
    {
        for (OneBlockProgressListener listener : progressListeners) listener.onDungeonCompleted(playerId, dungeonId);
    }

    public void restoreHud(Player player, World world, UUID ownerId)
    {
        if (player == null || world == null || ownerId == null || !rootRegistry.hasRoots(world, ownerId))
        {
            if (player != null) hudService.clear(player);
            return;
        }

        OneBlockDungeonStateProvider dungeonState = rootRegistry.dungeonState(world, ownerId);
        if (dungeonState.isDungeonActive())
        {
            String dungeonId = dungeonState.getActiveDungeonId();
            int totalWaves = OneBlockDungeonDefaults.getWaveCount(dungeonId);
            hudService.showDungeonStarted(player, dungeonId, totalWaves);
            hudService.updateDungeonWave(player, dungeonId, dungeonState.getCurrentWaveIndex(), totalWaves);
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

    private void restoreRootHud(Player player, World world, UUID ownerId)
    {
        restoreHud(player, world, ownerId);
    }

    private void updateHudForWorld(PlayerRef playerRef, Player player, World world)
    {
        UUID ownerId = playerRef == null ? null : resolveOwner(world, playerRef.getUuid());
        if (ownerId != null && rootRegistry.hasRoots(world, ownerId))
        {
            restoreRootHud(player, world, ownerId);
        }
        else if (player != null)
        {
            hudService.clear(player);
        }
    }

    private static void registerDropables(OneBlockDropRegistry registry, Iterable<String> dropableIds)
    {
        if (registry == null || dropableIds == null) return;
        for (String dropableId : dropableIds)
        {
            if (dropableId == null || dropableId.isEmpty()) continue;
            OneBlockDropId parsed = OneBlockDropId.parse(dropableId);
            registry.registerDropable(parsed.isEntity()
                    ? new EntitySpawnDropable(dropableId)
                    : new ItemDropable(dropableId));
        }
    }
}
