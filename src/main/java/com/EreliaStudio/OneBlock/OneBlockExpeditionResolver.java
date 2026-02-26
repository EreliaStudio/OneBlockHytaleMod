package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public final class OneBlockExpeditionResolver
{
    public static final String DEFAULT_EXPEDITION = "Meadow";

    private static final String BLOCK_PREFIX = "OneBlock_Block_";
    private static final String EXPEDITION_PREFIX = "OneBlock_Expedition_";
    private static final String KEY_SUFFIX = "_Key";

    private OneBlockExpeditionResolver()
    {
    }

    public static String expeditionFromBlockType(BlockType blockType)
    {
        if (blockType == null)
        {
            return DEFAULT_EXPEDITION;
        }

        return expeditionFromBlockId(blockType.getId());
    }

    public static String expeditionFromBlockId(String blockId)
    {
        if (blockId == null || !blockId.startsWith(BLOCK_PREFIX))
        {
            return DEFAULT_EXPEDITION;
        }

        String expedition = blockId.substring(BLOCK_PREFIX.length());
        if (expedition.isEmpty())
        {
            return DEFAULT_EXPEDITION;
        }

        return normalizeExpedition(expedition);
    }

    public static String expeditionFromKeyItemId(String itemId)
    {
        if (itemId == null)
        {
            return null;
        }

        String expedition = stripPrefix(itemId, EXPEDITION_PREFIX);
        if (expedition == null)
        {
            return null;
        }

        if (expedition.endsWith(KEY_SUFFIX))
        {
            expedition = expedition.substring(0, expedition.length() - KEY_SUFFIX.length());
        }

        expedition = normalizeExpedition(expedition);
        return expedition.isEmpty() ? null : expedition;
    }

    public static String normalizeExpedition(String expeditionId)
    {
        if (expeditionId == null || expeditionId.isEmpty())
        {
            return DEFAULT_EXPEDITION;
        }

        return expeditionId;
    }

    public static String blockIdForExpedition(String expeditionId)
    {
        if (expeditionId == null || expeditionId.isEmpty())
        {
            return null;
        }

        return BLOCK_PREFIX + expeditionId;
    }

    private static String stripPrefix(String value, String prefix)
    {
        if (value.startsWith(prefix))
        {
            return value.substring(prefix.length());
        }
        return null;
    }

}
