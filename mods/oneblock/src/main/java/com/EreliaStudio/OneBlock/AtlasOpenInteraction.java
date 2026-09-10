package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.builtin.crafting.component.BenchBlock;
import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import org.joml.Vector3i;

public final class AtlasOpenInteraction extends SimpleBlockInteraction {
    public static final BuilderCodec<AtlasOpenInteraction> CODEC = BuilderCodec.builder(
            AtlasOpenInteraction.class, AtlasOpenInteraction::new, SimpleBlockInteraction.CODEC).build();
    @Override protected void interactWithBlock(World world, CommandBuffer<EntityStore> buffer,
            InteractionType type, InteractionContext context, ItemStack held, Vector3i pos, CooldownHandler cooldown) {
        var ref = context.getEntity();
        var player = buffer.getComponent(ref, Player.getComponentType());
        var playerRef = buffer.getComponent(ref, PlayerRef.getComponentType());
        var manager = buffer.getComponent(ref, CraftingManager.getComponentType());
        if (player == null || playerRef == null || manager == null || manager.hasBenchSet()) return;
        var chunks = world.getChunkStore();
        var sectionRef = chunks.getChunkSectionReferenceAtBlock(pos.x, pos.y, pos.z);
        if (sectionRef == null || !sectionRef.isValid()) return;
        var store = chunks.getStore();
        var blockRef = BlockModule.getBlockEntity(store, sectionRef, pos.x, pos.y, pos.z);
        if (blockRef == null || !blockRef.isValid()) return;
        var bench = store.getComponent(blockRef, BenchBlock.getComponentType());
        var section = store.getComponent(sectionRef, BlockSection.getComponentType());
        if (bench == null || section == null) return;
        var blockType = BlockType.getAssetMap().getAsset(section.get(pos.x, pos.y, pos.z));
        if (blockType == null || blockType.getBench() == null || !AtlasCraftingService.isAtlasStation(blockType.getBench().getId())) return;
        BenchBlock.refreshGrantedAugmentTags(store, blockRef, pos);
        if (!BenchBlock.tryOpen(ref, buffer, blockRef, pos)) return;
        var window = new AtlasBenchWindow(pos, section.getRotationIndex(pos.x, pos.y, pos.z), blockType, bench, blockRef);
        if (bench.getWindows().putIfAbsent(playerRef.getUuid(), window) != null) return;
        window.registerCloseEvent(event -> bench.getWindows().remove(playerRef.getUuid(), window));
        var page = new ExpeditionAtlasPage(playerRef, OneBlockPlugin.getInstance().getExpeditionCatalog(), window);
        if (!player.getPageManager().openCustomPageWithWindows(ref, ref.getStore(), page, window))
            bench.getWindows().remove(playerRef.getUuid(), window);
    }
    @Override protected void simulateInteractWithBlock(InteractionType type, InteractionContext context,
            ItemStack held, World world, Vector3i pos) {}
}
