package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public final class OneBlockExpeditionPoolResolver implements OneBlockPoolResolver
{
    @Override
    public String resolvePoolId(BlockType blockType)
    {
        return OneBlockExpeditionResolver.expeditionFromBlockType(blockType);
    }
}
