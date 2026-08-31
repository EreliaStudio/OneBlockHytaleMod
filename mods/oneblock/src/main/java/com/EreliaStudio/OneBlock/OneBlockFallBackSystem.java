package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Rotation3f;
import org.joml.Vector3d;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.VoidWorldGenProvider;

import java.util.UUID;

public final class OneBlockFallBackSystem extends ArchetypeTickingSystem<EntityStore>
{
    private static final Vector3d DEFAULT_SPAWN_POS = new Vector3d(0.5, 102.0, 0.5);
    private static final float VOID_DAMAGE_AMOUNT = Float.MAX_VALUE;

    private final OneBlockSettingsProvider settingsProvider;
    private final OneBlockWorldStateRegistry stateRegistry;

    public OneBlockFallBackSystem(OneBlockSettingsProvider settingsProvider,
                                  OneBlockWorldStateRegistry stateRegistry)
    {
        this.settingsProvider = settingsProvider;
        this.stateRegistry = stateRegistry;
    }

    @Override
    public Query<EntityStore> getQuery()
    {
        return TransformComponent.getComponentType();
    }

    @Override
    public void tick(float delta,
                     ArchetypeChunk<EntityStore> chunk,
                     Store<EntityStore> store,
                     CommandBuffer<EntityStore> buffer)
    {
        if (chunk == null || store == null)
        {
            return;
        }

        EntityStore entityStore = store.getExternalData();
        if (entityStore == null)
        {
            return;
        }

        World world = entityStore.getWorld();
        if (!isOneBlockVoidWorld(world))
        {
            return;
        }
        double falloffHeight = settingsProvider == null
                ? OneBlockSettingsProvider.DEFAULT_FALLOFF_HEIGHT
                : settingsProvider.getFalloffHeight(world.getName());

        ComponentType<EntityStore, Player> playerType = Player.getComponentType();
        ComponentType<EntityStore, TransformComponent> transformType = TransformComponent.getComponentType();
        ComponentType<EntityStore, PlayerRef> playerRefType = PlayerRef.getComponentType();
        ComponentType<EntityStore, Teleport> teleportType = Teleport.getComponentType();
        ComponentType<EntityStore, DeathComponent> deathType = DeathComponent.getComponentType();

        int size = chunk.size();
        for (int i = 0; i < size; i++)
        {
            TransformComponent transform = chunk.getComponent(i, transformType);
            if (transform == null)
            {
                continue;
            }

            Vector3d position = transform.getPosition();
            if (position == null || position.y() >= falloffHeight)
            {
                continue;
            }

            Ref<EntityStore> ref = chunk.getReferenceTo(i);
            if (ref == null)
            {
                continue;
            }

            Player player = chunk.getComponent(i, playerType);
            PlayerRef playerRef = chunk.getComponent(i, playerRefType);
            if (player == null && playerRef == null)
            {
                buffer.removeEntity(ref, RemoveReason.REMOVE);
                continue;
            }

            if (buffer.getComponent(ref, teleportType) != null)
            {
                continue;
            }

            if (!isFallProtectionEnabled())
            {
                killPlayer(buffer, ref, deathType);
                continue;
            }

            UUID playerId = playerRef == null ? null : playerRef.getUuid();
            Transform spawn = resolveSpawn(world, playerId);
            if (spawn == null)
            {
                continue;
            }

            Teleport teleport = Teleport.createForPlayer(world, spawn);
            buffer.run(targetStore -> targetStore.addComponent(ref, teleportType, teleport));
        }
    }

    private boolean isFallProtectionEnabled()
    {
        return settingsProvider == null || settingsProvider.isFallProtectionEnabled();
    }

    /**
     * Legacy OneBlock installations may still have a void-generated default
     * world even though the default world is no longer kept in the managed
     * world registry. Detect the actual generator so void cleanup continues
     * to work for those saves without affecting ordinary terrain worlds.
     */
    private boolean isOneBlockVoidWorld(World world)
    {
        if (world == null)
        {
            return false;
        }
        if (stateRegistry != null && stateRegistry.isManaged(world))
        {
            return true;
        }

        WorldConfig config = world.getWorldConfig();
        return config != null && config.getWorldGenProvider() instanceof VoidWorldGenProvider;
    }

    private static void killPlayer(CommandBuffer<EntityStore> buffer,
                                   Ref<EntityStore> ref,
                                   ComponentType<EntityStore, DeathComponent> deathType)
    {
        if (buffer.getComponent(ref, deathType) != null)
        {
            return;
        }

        Damage damage = new Damage(new Damage.EnvironmentSource("oneblock_void"), DamageCause.OUT_OF_WORLD, VOID_DAMAGE_AMOUNT);
        DeathComponent.tryAddComponent(buffer, ref, damage);
    }

    private static Transform resolveSpawn(World world, UUID playerId)
    {
        if (world == null || playerId == null)
        {
            return new Transform(DEFAULT_SPAWN_POS, new Rotation3f());
        }

        WorldConfig config = world.getWorldConfig();
        if (config != null)
        {
            ISpawnProvider provider = config.getSpawnProvider();
            if (provider != null)
            {
                Transform spawn = provider.getSpawnPoint(world, playerId);
                if (spawn != null)
                {
                    return spawn;
                }
            }
        }

        return new Transform(DEFAULT_SPAWN_POS, new Rotation3f());
    }
}
