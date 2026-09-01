package com.EreliaStudio.OneBlockIslands;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

/** Returns fallen players to their island spawn and removes fallen entities. */
final class IslandFallProtectionSystem extends ArchetypeTickingSystem<EntityStore> {
    private static final double FALLOFF_HEIGHT = 1.0;
    private final IslandStore islands;

    IslandFallProtectionSystem(IslandStore islands) {
        this.islands = islands;
    }

    @Override public Query<EntityStore> getQuery() {
        return TransformComponent.getComponentType();
    }

    @Override public void tick(float delta,
                               ArchetypeChunk<EntityStore> chunk,
                               Store<EntityStore> store,
                               CommandBuffer<EntityStore> buffer) {
        EntityStore data = store == null ? null : store.getExternalData();
        World world = data == null ? null : data.getWorld();
        if (world == null || islands.findByWorld(world.getName()).isEmpty()) return;

        ComponentType<EntityStore, TransformComponent> transformType = TransformComponent.getComponentType();
        ComponentType<EntityStore, Player> playerType = Player.getComponentType();
        ComponentType<EntityStore, PlayerRef> playerRefType = PlayerRef.getComponentType();
        ComponentType<EntityStore, Teleport> teleportType = Teleport.getComponentType();

        for (int i = 0; i < chunk.size(); i++) {
            TransformComponent transform = chunk.getComponent(i, transformType);
            Vector3d position = transform == null ? null : transform.getPosition();
            if (position == null || position.y() >= FALLOFF_HEIGHT) continue;

            Ref<EntityStore> ref = chunk.getReferenceTo(i);
            Player player = chunk.getComponent(i, playerType);
            PlayerRef playerRef = chunk.getComponent(i, playerRefType);
            if (player == null && playerRef == null) {
                buffer.removeEntity(ref, RemoveReason.REMOVE);
                continue;
            }
            if (buffer.getComponent(ref, teleportType) != null) continue;

            Transform spawn = resolveSpawn(world, playerRef == null ? null : playerRef.getUuid());
            Teleport teleport = Teleport.createForPlayer(world, spawn);
            buffer.run(targetStore -> targetStore.addComponent(ref, teleportType, teleport));
        }
    }

    private static Transform resolveSpawn(World world, java.util.UUID playerId) {
        WorldConfig config = world.getWorldConfig();
        ISpawnProvider provider = config == null ? null : config.getSpawnProvider();
        Transform spawn = provider == null ? null : provider.getSpawnPoint(world, playerId);
        return spawn == null ? IslandWorldService.SPAWN : spawn;
    }
}
