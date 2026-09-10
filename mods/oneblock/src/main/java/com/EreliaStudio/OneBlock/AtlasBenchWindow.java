package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.window.SimpleCraftingWindow;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.window.WindowAction;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.entity.entities.Player;
import org.joml.Vector3i;

/** Retains native bench lifecycle, nearby containers, distance validation and close handling. */
final class AtlasBenchWindow extends SimpleCraftingWindow {
    private final Ref<ChunkStore> block;
    AtlasBenchWindow(Vector3i pos, int rotation, BlockType type, BenchBlock bench, Ref<ChunkStore> block) {
        super(pos.x, pos.y, pos.z, rotation, type, bench);
        this.block = block;
    }
    String station() { return getBlockType().getBench().getId(); }
    boolean allowed(Ref<EntityStore> ref, Store<EntityStore> store) {
        if (!block.isValid() || block.getStore() != store.getExternalData().getWorld().getChunkStore().getStore()
                || !validate(ref, store)) return false;
        boolean[] allowed = {false};
        // OpenGate is a native CommandBuffer API. Use a store-owned buffer so gate side effects flush normally.
        store.forEachChunk(Player.getComponentType(), (chunk, buffer) -> {
            for (int i = 0; i < chunk.size(); i++) {
                if (ref.equals(chunk.getReferenceTo(i))) {
                    BenchBlock.refreshGrantedAugmentTags(block.getStore(), block, new Vector3i(x, y, z));
                    allowed[0] = BenchBlock.tryOpen(ref, buffer, block, new Vector3i(x, y, z));
                }
            }
        });
        return allowed[0];
    }
    @Override public void handleAction(Ref<EntityStore> ref, Store<EntityStore> store, WindowAction action) {
        // All Atlas crafting goes through its validated, revision-bound page events.
    }
}
