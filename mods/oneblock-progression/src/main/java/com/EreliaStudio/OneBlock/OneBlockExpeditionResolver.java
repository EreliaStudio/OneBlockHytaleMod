package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public final class OneBlockExpeditionResolver
{
    public static final String DEFAULT_EXPEDITION = "Meadow";

    private static final String BLOCK_PREFIX     = "OneBlock_Block_";
    private static final String CRYSTAL_PREFIX   = "OneBlock_Crystal_";
    private static final String SUFFIX_SMALL     = "_Small";
    private static final String SUFFIX_LARGE     = "_Large";

    public static final int TICKS_SMALL = 100;
    public static final int TICKS_LARGE = 300;

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
        String remainder = itemId.substring(CRYSTAL_PREFIX.length());
        if (remainder.endsWith(SUFFIX_LARGE))
            remainder = remainder.substring(0, remainder.length() - SUFFIX_LARGE.length());
        else if (remainder.endsWith(SUFFIX_SMALL))
            remainder = remainder.substring(0, remainder.length() - SUFFIX_SMALL.length());
        else return null;
        return remainder.isEmpty() ? null : remainder;
    }

    /** Returns the tick count for a crystal item ID (100 for Small, 300 for Large), or -1 if not a crystal. */
    public static int ticksFromCrystalItemId(String itemId)
    {
        if (itemId == null || !itemId.startsWith(CRYSTAL_PREFIX)) return -1;
        if (itemId.endsWith(SUFFIX_LARGE)) return TICKS_LARGE;
        if (itemId.endsWith(SUFFIX_SMALL)) return TICKS_SMALL;
        return -1;
    }

    public static String blockIdForExpedition(String expeditionId)
    {
        if (expeditionId == null || expeditionId.isEmpty()) return null;
        String blockId = OneBlockExpeditionDefaults.getBlockId(expeditionId);
        return blockId != null ? blockId : BLOCK_PREFIX + expeditionId;
    }
}
