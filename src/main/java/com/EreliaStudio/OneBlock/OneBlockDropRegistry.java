package com.EreliaStudio.OneBlock;

import java.util.List;
import java.util.Random;

public final class OneBlockDropRegistry
{
    public static final String DEFAULT_ITEM_ID = "Soil_Grass";

    private final Random rng = new Random();

    public String pickReward(List<String> enabledDrops)
    {
        if (enabledDrops == null || enabledDrops.isEmpty())
        {
            return DEFAULT_ITEM_ID;
        }

        return enabledDrops.get(rng.nextInt(enabledDrops.size()));
    }
}