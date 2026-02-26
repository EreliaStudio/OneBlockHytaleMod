package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;

final class OneBlockBlockUtil
{
    static final String ONEBLOCK_CATEGORY = "Blocks.OneBlock";

    private OneBlockBlockUtil()
    {
    }

    static boolean isOneBlock(BlockType blockType)
    {
        if (blockType == null)
        {
            return false;
        }

        Item item = blockType.getItem();
        if (item == null)
        {
            return false;
        }

        String[] categories = item.getCategories();
        if (categories == null)
        {
            return false;
        }

        for (String category : categories)
        {
            if (ONEBLOCK_CATEGORY.equals(category))
            {
                return true;
            }
        }

        return false;
    }
}
