package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;

public final class OneBlockFallBackSystem extends ArchetypeTickingSystem<EntityStore>
{
    private static final double FALLBACK_Y = 85.0;

    @Override
    public Query<EntityStore> getQuery()
    {
        return Query.and(Player.getComponentType(), TransformComponent.getComponentType(), PlayerRef.getComponentType());
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
        if (!OneBlockWorldInitializer.isDefaultWorld(world))
        {
            return;
        }

        ComponentType<EntityStore, Player> playerType = Player.getComponentType();
        ComponentType<EntityStore, TransformComponent> transformType = TransformComponent.getComponentType();
        ComponentType<EntityStore, PlayerRef> playerRefType = PlayerRef.getComponentType();
        ComponentType<EntityStore, Teleport> teleportType = Teleport.getComponentType();

        int size = chunk.size();
        for (int i = 0; i < size; i++)
        {
            Player player = chunk.getComponent(i, playerType);
            if (player == null)
            {
                continue;
            }

            TransformComponent transform = chunk.getComponent(i, transformType);
            if (transform == null)
            {
                continue;
            }

            Vector3d position = transform.getPosition();
            if (position == null || position.getY() >= FALLBACK_Y)
            {
                continue;
            }

            Ref<EntityStore> ref = chunk.getReferenceTo(i);
            if (ref == null)
            {
                continue;
            }

            if (buffer.getComponent(ref, teleportType) != null)
            {
                continue;
            }

            PlayerRef playerRef = chunk.getComponent(i, playerRefType);
            if (playerRef == null)
            {
                continue;
            }

            Transform spawn = resolveSpawn(world, playerRef.getUuid());
            if (spawn == null)
            {
                continue;
            }

            Teleport teleport = Teleport.createForPlayer(world, spawn);
            buffer.run(targetStore -> targetStore.addComponent(ref, teleportType, teleport));
        }
    }

    private static Transform resolveSpawn(World world, UUID playerId)
    {
        if (world == null || playerId == null)
        {
            return new Transform(OneBlockWorldInitializer.SPAWN_POS, Vector3f.ZERO);
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

        return new Transform(OneBlockWorldInitializer.SPAWN_POS, Vector3f.ZERO);
    }
}
