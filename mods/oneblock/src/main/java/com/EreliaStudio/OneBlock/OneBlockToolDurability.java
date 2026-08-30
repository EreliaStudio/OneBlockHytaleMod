package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/** Restores tool durability usage that native soft-block gathering omits. */
final class OneBlockToolDurability
{
    private OneBlockToolDurability() {}

    static void applyForCompletedBreak(Ref<EntityStore> playerRef,
                                       Store<EntityStore> store,
                                       String blockId)
    {
        if (playerRef == null || store == null || blockId == null) return;

        OneBlockSolidityDefaults.SolidityDefinition solidity =
                OneBlockSolidityDefaults.get(blockId);

        // Pickaxe/Axe blocks use Rocks/Woods gathering, for which Hytale
        // applies its native, material-aware durability loss after this event.
        // Hand blocks use SoftBlocks, where Hytale deliberately applies none.
        if (solidity.requiredTool() != OneBlockSolidityDefaults.RequiredTool.HAND) return;
        if (!ItemUtils.canDecreaseItemStackDurability(playerRef, store)) return;

        InventoryComponent.Hotbar hotbar = store.getComponent(
                playerRef,
                InventoryComponent.Hotbar.getComponentType()
        );
        if (hotbar == null || hotbar.getActiveSlot() < 0) return;

        int activeSlot = hotbar.getActiveSlot();
        ItemContainer inventory = hotbar.getInventory();
        ItemStack stack = inventory.getItemStack((short) activeSlot);
        if (ItemStack.isEmpty(stack) || stack.isUnbreakable() || stack.isBroken()) return;

        Item item = stack.getItem();
        if (item == null || item.getTool() == null || stack.getMaxDurability() <= 0.0) return;

        // Some tools express durability by material-specific tables. Soft
        // OneBlocks do not match those tables, so guarantee one durability
        // unit when the general hit loss is absent.
        double durabilityLoss = Math.max(1.0, item.getDurabilityLossOnHit());
        ItemUtils.updateItemStackDurability(
                playerRef,
                stack,
                inventory,
                activeSlot,
                -durabilityLoss,
                store
        );
    }
}
