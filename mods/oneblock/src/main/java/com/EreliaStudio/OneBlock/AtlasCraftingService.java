package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.builtin.crafting.component.CraftingManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryUtils;
import com.hypixel.hytale.server.core.modules.entity.player.PlayerSettings;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.*;

/** World-thread only. Never removes inputs or grants outputs itself. */
final class AtlasCraftingService {
    static final int MAX_QUANTITY = 64;
    enum State { ready, locked, materials, capacity, station, invalid, quantity, stale, failed, crafted, timed }
    private final AtlasBenchWindow window;
    private final AtlasEventGuard guard = new AtlasEventGuard();
    AtlasCraftingService(AtlasBenchWindow window) { this.window = window; }
    String revision() { return guard.token(); }
    void invalidate() { guard.advance(); }
    static boolean isAtlasStation(String station) {
        return "OneBlockEnchanter".equals(station) || "OneBlockDungeonEnchanter".equals(station);
    }
    static boolean stationMatches(boolean dungeon, String station) {
        return (dungeon ? "OneBlockDungeonEnchanter" : "OneBlockEnchanter").equals(station);
    }
    static boolean known(boolean required, String crystal, Set<String> recipes) {
        return !required || recipes.contains(crystal);
    }
    static CraftingRecipe recipe(ExpeditionCatalog.Expedition e) {
        if (e == null || Item.getAssetMap().getAsset(e.crystal()) == null) return null;
        var item = Item.getAssetMap().getAsset(e.crystal());
        return CraftingRecipe.getAssetMap().getAsset(CraftingRecipe.generateIdFromItemRecipe(item, 0));
    }
    boolean unlocked(Player player, ExpeditionCatalog.Expedition e) {
        var recipe = recipe(e);
        return recipe != null && known(recipe.isKnowledgeRequired(), e.crystal(), player.getPlayerConfigData().getKnownRecipes());
    }
    ItemContainer inventory(Ref<EntityStore> ref, Store<EntityStore> store) {
        return InventoryComponent.getCombined(store, ref, InventoryComponent.BACKPACK_STORAGE_HOTBAR);
    }
    ItemContainer materials(Ref<EntityStore> ref, Store<EntityStore> store) {
        return new CombinedItemContainer(inventory(ref, store), window.getExtraResourcesSection().getItemContainer());
    }
    boolean affordable(Player player, ExpeditionCatalog.Expedition e, ItemContainer materials, int quantity) {
        var recipe = recipe(e);
        return hasMaterials(recipe, quantity, player.getGameMode() == GameMode.Creative, materials);
    }
    static boolean hasMaterials(CraftingRecipe recipe, int quantity, boolean creative, ItemContainer materials) {
        if (recipe == null || quantity < 1 || quantity > MAX_QUANTITY) return false;
        if (recipe.getInput() != null && Arrays.stream(recipe.getInput()).anyMatch(m ->
                m.getQuantity() < 1 || (long) m.getQuantity() * quantity > Integer.MAX_VALUE)) return false;
        return creative || materials.canRemoveMaterials(CraftingManager.getInputMaterials(recipe, quantity));
    }
    State state(Ref<EntityStore> ref, Store<EntityStore> store, Player player,
                ExpeditionCatalog.Expedition e, int quantity) {
        if (e == null || recipe(e) == null) return State.invalid;
        if (quantity < 1 || quantity > MAX_QUANTITY) return State.quantity;
        if (!stationMatches(e.dungeon(), window.station())) return State.station;
        var recipe = recipe(e);
        if (!unlocked(player, e)) return State.locked;
        if (recipe.getBenchRequirement() == null || Arrays.stream(recipe.getBenchRequirement()).noneMatch(
                b -> b.type == BenchType.Crafting && window.station().equals(b.id))) return State.station;
        // Current crystals are instant. Refuse altered timed recipes rather than bypassing their queue semantics.
        if (recipe.getTimeSeconds() > 0) return State.timed;
        if (!affordable(player, e, materials(ref, store), quantity)) return State.materials;
        // Native output destination; deliberately require space before removal, including in creative.
        var outputs = CraftingManager.getOutputItemStacks(recipe, quantity);
        if (outputs.isEmpty() || outputs.stream().anyMatch(s -> !e.crystal().equals(s.getItemId()))) return State.invalid;
        var settings = store.getComponent(ref, PlayerSettings.getComponentType());
        var destination = InventoryUtils.getContainerForItemPickup(ref, Item.getAssetMap().getAsset(e.crystal()),
                settings == null ? PlayerSettings.defaults() : settings, store);
        if (!destination.canAddItemStacks(outputs)) return State.capacity;
        return State.ready;
    }
    State craft(Ref<EntityStore> ref, Store<EntityStore> store, ExpeditionAtlasPage page,
                ExpeditionCatalog.Expedition e, int quantity, String revision) {
        if (!guard.consume(revision)) return State.stale;
        if (ref == null || !ref.isValid()) return State.invalid;
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null || player.getPageManager().getCustomPage() != page
                || player.getWindowManager().getWindow(window.getId()) != window || !window.allowed(ref, store)) return State.station;
        window.invalidateExtraResources();
        var status = state(ref, store, player, e, quantity);
        if (status != State.ready) return status;
        var manager = store.getComponent(ref, CraftingManager.getComponentType());
        if (manager == null || !manager.hasBenchSet()) return State.station;
        // Native method revalidates knowledge, memories, bench tier/tags and materials; dispatches
        // cancellable Pre/Post + PlayerCraftEvent; performs all-or-nothing input removal and sync.
        boolean crafted = manager.craftItem(ref, store, recipe(e), quantity, materials(ref, store));
        window.invalidateExtraResources();
        return crafted ? State.crafted : State.failed;
    }
}
