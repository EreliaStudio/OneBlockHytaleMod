package com.EreliaStudio.OneBlockIslands;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.ICancellableEcsEvent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.DamageBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseEntityEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

final class IslandMutationSystems {
    private IslandMutationSystems() {}

    private abstract static class ProtectionSystem<E extends com.hypixel.hytale.component.system.EcsEvent>
            extends EntityEventSystem<EntityStore, E> {
        final IslandStore islands;
        ProtectionSystem(Class<E> type, IslandStore islands) { super(type); this.islands = islands; }
        @Override public Query<EntityStore> getQuery() { return Query.any(); }
        final boolean deny(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store) {
            Ref<EntityStore> ref = chunk.getReferenceTo(index);
            PlayerRef player = store.getComponent(ref, PlayerRef.getComponentType());
            EntityStore external = store.getExternalData();
            World world = external == null ? null : external.getWorld();
            if (world == null || islands.findByWorld(world.getName()).isEmpty()) return false;
            if (IslandAccess.mayEdit(islands, world, player)) return false;
            if (player != null) player.sendMessage(Message.raw("You cannot modify this island."));
            return true;
        }
    }

    static final class Damage extends ProtectionSystem<DamageBlockEvent> {
        Damage(IslandStore islands) { super(DamageBlockEvent.class, islands); }
        @Override public void handle(int i, ArchetypeChunk<EntityStore> c, Store<EntityStore> s, @Nonnull CommandBuffer<EntityStore> b, @Nonnull DamageBlockEvent e) { if (deny(i,c,s)) e.setCancelled(true); }
    }
    static final class Break extends ProtectionSystem<BreakBlockEvent> {
        Break(IslandStore islands) { super(BreakBlockEvent.class, islands); }
        @Override public void handle(int i, ArchetypeChunk<EntityStore> c, Store<EntityStore> s, @Nonnull CommandBuffer<EntityStore> b, @Nonnull BreakBlockEvent e) { if (deny(i,c,s)) e.setCancelled(true); }
    }
    static final class Place extends ProtectionSystem<PlaceBlockEvent> {
        Place(IslandStore islands) { super(PlaceBlockEvent.class, islands); }
        @Override public void handle(int i, ArchetypeChunk<EntityStore> c, Store<EntityStore> s, @Nonnull CommandBuffer<EntityStore> b, @Nonnull PlaceBlockEvent e) { if (deny(i,c,s)) e.setCancelled(true); }
    }
    static final class UseBlock extends ProtectionSystem<UseBlockEvent.Pre> {
        UseBlock(IslandStore islands) { super(UseBlockEvent.Pre.class, islands); }
        @Override public void handle(int i, ArchetypeChunk<EntityStore> c, Store<EntityStore> s, @Nonnull CommandBuffer<EntityStore> b, @Nonnull UseBlockEvent.Pre e) { if (deny(i,c,s)) e.setCancelled(true); }
    }
    static final class UseEntity extends ProtectionSystem<UseEntityEvent.Pre> {
        UseEntity(IslandStore islands) { super(UseEntityEvent.Pre.class, islands); }
        @Override public void handle(int i, ArchetypeChunk<EntityStore> c, Store<EntityStore> s, @Nonnull CommandBuffer<EntityStore> b, @Nonnull UseEntityEvent.Pre e) { if (deny(i,c,s)) e.setCancelled(true); }
    }
}
