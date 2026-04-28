package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public final class OneBlockExpeditionResolver
{
    public static final String DEFAULT_EXPEDITION = "Meadow";
    public static final int DEFAULT_TICKS = 100;

    private static final String BLOCK_PREFIX   = "OneBlock_Block_";
    private static final String CRYSTAL_PREFIX = "OneBlock_Crystal_";

    private OneBlockExpeditionResolver() {}

    public static String expeditionFromBlockType(BlockType blockType)
    {
        if (blockType == null) return DEFAULT_EXPEDITION;
        return expeditionFromBlockId(blockType.getId());
    }

    public static String expeditionFromBlockId(String blockId)
    {
        if (blockId == null || !blockId.startsWith(BLOCK_PREFIX)) return DEFAULT_EXPEDITION;
        String expedition = blockId.substring(BLOCK_PREFIX.length());
        return expedition.isEmpty() ? DEFAULT_EXPEDITION : expedition;
    }

    /** Returns the expedition name from a crystal item ID, or null if not a crystal. */
    public static String expeditionFromCrystalItemId(String itemId)
    {
        if (itemId == null || !itemId.startsWith(CRYSTAL_PREFIX)) return null;
        String expedition = itemId.substring(CRYSTAL_PREFIX.length());
        return expedition.isEmpty() ? null : expedition;
    }

    public static String blockIdForExpedition(String expeditionId)
    {
        if (expeditionId == null || expeditionId.isEmpty()) return null;
        String blockId = OneBlockExpeditionDefaults.getBlockId(expeditionId);
        return blockId != null ? blockId : BLOCK_PREFIX + expeditionId;
    }
}
