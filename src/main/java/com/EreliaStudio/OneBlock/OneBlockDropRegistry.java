package com.EreliaStudio.OneBlock;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class OneBlockDropRegistry
{
    public static final String DEFAULT_ITEM_ID = "Soil_Grass";
    private static final int DEFAULT_DROP_WEIGHT = 50;

    private final Random rng = new Random();
    private final Map<String, Map<String, Integer>> weightByChapter = new HashMap<>();

    public String pickReward(List<String> enabledDrops)
    {
        return pickReward(OneBlockChapterResolver.DEFAULT_CHAPTER, enabledDrops);
    }

    public String pickReward(String chapterId, List<String> enabledDrops)
    {
        if (enabledDrops == null || enabledDrops.isEmpty())
        {
            return DEFAULT_ITEM_ID;
        }

        int totalWeight = 0;
        for (String dropId : enabledDrops)
        {
            int weight = getWeight(chapterId, dropId);
            if (weight > 0)
            {
                totalWeight += weight;
            }
        }

        if (totalWeight <= 0)
        {
            return DEFAULT_ITEM_ID;
        }

        int roll = rng.nextInt(totalWeight);
        int cursor = 0;
        for (String dropId : enabledDrops)
        {
            int weight = getWeight(chapterId, dropId);
            if (weight <= 0)
            {
                continue;
            }
            cursor += weight;
            if (roll < cursor)
            {
                return dropId;
            }
        }

        return DEFAULT_ITEM_ID;
    }

    public void registerWeights(Collection<OneBlockUnlockService.UnlockDefinition> definitions)
    {
        if (definitions == null)
        {
            return;
        }

        for (OneBlockUnlockService.UnlockDefinition definition : definitions)
        {
            if (definition == null)
            {
                continue;
            }

            String dropId = definition.dropItemId;
            if (dropId == null || dropId.isEmpty())
            {
                continue;
            }

            String chapterId = definition.chapterId;
            if (chapterId == null || chapterId.isEmpty())
            {
                chapterId = OneBlockChapterResolver.DEFAULT_CHAPTER;
            }

            int weight = definition.weight;
            if (weight < 1)
            {
                weight = 1;
            }

            weightByChapter
                    .computeIfAbsent(chapterId, key -> new HashMap<>())
                    .put(dropId, weight);
        }
    }

    private int getWeight(String chapterId, String dropId)
    {
        if (dropId == null || dropId.isEmpty())
        {
            return 0;
        }

        if (chapterId == null || chapterId.isEmpty())
        {
            chapterId = OneBlockChapterResolver.DEFAULT_CHAPTER;
        }

        Map<String, Integer> weights = weightByChapter.get(chapterId);
        if (weights == null)
        {
            return 1;
        }

        Integer weight = weights.get(dropId);
        if (weight != null)
        {
            return weight;
        }

        if (DEFAULT_ITEM_ID.equals(dropId))
        {
            return DEFAULT_DROP_WEIGHT;
        }

        return 1;
    }
}
