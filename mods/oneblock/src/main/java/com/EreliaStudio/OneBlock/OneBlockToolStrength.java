package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTool;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemToolSpec;
import com.hypixel.hytale.server.core.inventory.ItemStack;

/** Resolves OneBlock damage from Hytale's native tool power and item level. */
final class OneBlockToolStrength
{
    private static final float CRUDE_PICKAXE_ROCK_POWER = 0.25F;
    private static final float CRUDE_AXE_WOOD_POWER = 0.15F;
    private static final float SOFT_BLOCK_POWER = 1.0F;
    private static final float ITEM_LEVELS_PER_MULTIPLIER = 10.0F;

    private OneBlockToolStrength() {}

    static float multiplier(ItemStack itemStack,
                            OneBlockSolidityDefaults.RequiredTool requiredTool)
    {
        if (ItemStack.isEmpty(itemStack)) return 1.0F;

        Item item = itemStack.getItem();
        if (item == null || item.getTool() == null) return 1.0F;

        String gatherType;
        float baselinePower;
        switch (requiredTool)
        {
            case PICKAXE ->
            {
                gatherType = "Rocks";
                baselinePower = CRUDE_PICKAXE_ROCK_POWER;
            }
            case AXE ->
            {
                gatherType = "Woods";
                baselinePower = CRUDE_AXE_WOOD_POWER;
            }
            case HAND ->
            {
                gatherType = "SoftBlocks";
                baselinePower = SOFT_BLOCK_POWER;
            }
            default -> throw new IllegalStateException("Unsupported required tool: " + requiredTool);
        }

        float nativePower = findPower(item.getTool(), gatherType);
        return calculateMultiplier(item.getItemLevel(), nativePower, baselinePower);
    }

    private static float findPower(ItemTool tool, String gatherType)
    {
        ItemToolSpec[] specs = tool.getSpecs();
        if (specs == null) return 0.0F;

        for (ItemToolSpec spec : specs)
        {
            if (spec != null && gatherType.equalsIgnoreCase(spec.getGatherType()))
                return Math.max(0.0F, spec.getPower());
        }
        return 0.0F;
    }

    /** Pure calculation kept separate for deterministic unit testing. */
    static float calculateMultiplier(int itemLevel, float nativePower, float baselinePower)
    {
        float nativeMultiplier = baselinePower > 0.0F
                ? Math.max(0.0F, nativePower) / baselinePower
                : 1.0F;
        float levelMultiplier = Math.max(0, itemLevel) / ITEM_LEVELS_PER_MULTIPLIER;

        // A valid tool is never worse than the configured base tick count.
        // Item level supplements native power for material specs which plateau
        // across several late-game tool tiers.
        return Math.max(1.0F, Math.max(nativeMultiplier, levelMultiplier));
    }
}
