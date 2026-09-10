package com.EreliaStudio.OneBlock;

import org.junit.jupiter.api.Test;
import com.hypixel.hytale.server.core.asset.type.item.config.CraftingRecipe;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AtlasCraftingServiceTest {
    @Test void recipeKnowledgeUsesCrystalItemIdAndHonorsStarters() {
        assertFalse(AtlasCraftingService.known(true, "Crystal", Set.of("Crystal_Recipe_Generated_0")));
        assertTrue(AtlasCraftingService.known(true, "Crystal", Set.of("Crystal")));
        assertTrue(AtlasCraftingService.known(false, "Crystal", Set.of()));
    }
    @Test void enchantersAreNotInterchangeable() {
        assertTrue(AtlasCraftingService.stationMatches(false, "OneBlockEnchanter"));
        assertTrue(AtlasCraftingService.stationMatches(true, "OneBlockDungeonEnchanter"));
        assertFalse(AtlasCraftingService.stationMatches(true, "OneBlockEnchanter"));
        assertFalse(AtlasCraftingService.stationMatches(false, "OneBlockDungeonEnchanter"));
        assertFalse(AtlasCraftingService.isAtlasStation("Fieldcraft"));
    }
    @Test void staleCraftEventsCannotBeReplayedEvenAfterFailure() {
        var service = new AtlasCraftingService(null);
        String revision = service.revision();
        assertEquals(AtlasCraftingService.State.invalid, service.craft(null, null, null, null, 1, revision));
        assertEquals(AtlasCraftingService.State.stale, service.craft(null, null, null, null, 1, revision));
        assertEquals(AtlasCraftingService.State.stale, service.craft(null, null, null, null, 1, null));
    }
    @Test void quantityAndSelectionChangesInvalidatePreviousCraft() {
        var guard = new AtlasEventGuard(); var original = guard.token(); guard.advance();
        assertFalse(guard.consume(original)); assertFalse(guard.consume("forged"));
        var current = guard.token(); assertTrue(guard.consume(current)); assertFalse(guard.consume(current));
    }
    private static MaterialQuantity material(int count) {
        return new MaterialQuantity("Fibre", null, null, count, null) {
            @Override public MaterialQuantity clone(int quantity) { return material(quantity); }
            @Override public ItemStack toItemStack() { return stack("Fibre", getQuantity()); }
        };
    }
    private static CraftingRecipe recipe(int cost) {
        return new CraftingRecipe(new MaterialQuantity[]{material(cost)},
                new MaterialQuantity("Crystal", null, null, 1, null), new MaterialQuantity[0], 1, null, 0, false, 0);
    }
    // Native inventory algorithms with deterministic item metadata, without booting the asset server.
    private static final Item ITEM = new Item("AtlasTest") { @Override public int getMaxStack() { return 4; } };
    private static ItemStack stack(String id, int count) {
        return new ItemStack(id, count) {
            @Override public Item getItem() { return ITEM; }
            @Override public ItemStack withQuantity(int q) { return stack(id, q); }
        };
    }
    @Test void nativeAffordabilityScalesQuantityAndDoesNotMutateInventory() {
        var inventory = new SimpleItemContainer((short) 3);
        inventory.setItemStackForSlot((short) 0, stack("Fibre", 4));
        inventory.setItemStackForSlot((short) 1, stack("Fibre", 2));
        assertTrue(AtlasCraftingService.hasMaterials(recipe(2), 3, false, inventory));
        assertFalse(AtlasCraftingService.hasMaterials(recipe(2), 4, false, inventory));
        assertEquals(6, inventory.countRemovableMaterial(material(1)));
        assertFalse(AtlasCraftingService.hasMaterials(recipe(1), 0, true, inventory));
        assertFalse(AtlasCraftingService.hasMaterials(recipe(1), 65, true, inventory));
        assertFalse(AtlasCraftingService.hasMaterials(recipe(Integer.MAX_VALUE), 2, false, inventory));
        assertTrue(AtlasCraftingService.hasMaterials(recipe(100), 1, true, inventory));
    }
    @Test void nativeCapacityAccountsForStackLimitsAndEveryCrystal() {
        var inventory = new SimpleItemContainer((short) 2);
        inventory.setItemStackForSlot((short) 0, stack("Crystal", 3));
        assertTrue(inventory.canAddItemStacks(List.of(stack("Crystal", 5))));
        assertFalse(inventory.canAddItemStacks(List.of(stack("Crystal", 6))));
        inventory.setItemStackForSlot((short) 1, stack("Fibre", 4));
        assertTrue(inventory.canAddItemStacks(List.of(stack("Crystal", 1))));
        assertFalse(inventory.canAddItemStacks(List.of(stack("Crystal", 2))));
        assertEquals(3, inventory.getItemStack((short) 0).getQuantity());
    }
}
