package com.EreliaStudio.OneBlock;

import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;

public final class OneBlockPools
{
    public static final String DEFAULT_POOL_ID = OneBlockExpeditionResolver.DEFAULT_EXPEDITION;

    private static final OneBlockPoolResolver DEFAULT_RESOLVER = blockType -> DEFAULT_POOL_ID;
    private static volatile OneBlockPoolResolver resolver = DEFAULT_RESOLVER;

    private OneBlockPools() {}

    public static void setResolver(OneBlockPoolResolver newResolver)
    {
        resolver = (newResolver == null) ? DEFAULT_RESOLVER : newResolver;
    }

    public static OneBlockPoolResolver getResolver()
    {
        return resolver;
    }

    public static String resolvePoolId(BlockType blockType)
    {
        return normalizePoolId(resolver.resolvePoolId(blockType));
    }

    public static String normalizePoolId(String poolId)
    {
        if (poolId == null || poolId.isEmpty()) return DEFAULT_POOL_ID;
        return poolId;
    }
}
