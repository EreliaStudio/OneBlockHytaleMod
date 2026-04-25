package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public interface OneBlockPoolResolver
{
    String resolvePoolId(BlockType blockType);
}
