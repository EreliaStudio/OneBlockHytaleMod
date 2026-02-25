package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public final class OneBlockChapterResolver
{
    public static final String DEFAULT_CHAPTER = "A1";

    private static final String BLOCK_PREFIX = "OneBlock_Block_";
    private static final String ACT_PREFIX = "OneBlock_Act_";

    private OneBlockChapterResolver()
    {
    }

    public static String chapterFromBlockType(BlockType blockType)
    {
        if (blockType == null)
        {
            return DEFAULT_CHAPTER;
        }

        return chapterFromBlockId(blockType.getId());
    }

    public static String chapterFromBlockId(String blockId)
    {
        if (blockId == null || !blockId.startsWith(BLOCK_PREFIX))
        {
            return DEFAULT_CHAPTER;
        }

        String chapter = blockId.substring(BLOCK_PREFIX.length());
        return chapter.isEmpty() ? DEFAULT_CHAPTER : chapter;
    }

    public static String chapterFromActItemId(String itemId)
    {
        if (itemId == null || !itemId.startsWith(ACT_PREFIX))
        {
            return null;
        }

        String chapter = itemId.substring(ACT_PREFIX.length());
        return chapter.isEmpty() ? null : chapter;
    }

    public static String blockIdForChapter(String chapterId)
    {
        if (chapterId == null || chapterId.isEmpty())
        {
            return null;
        }

        return BLOCK_PREFIX + chapterId;
    }
}
