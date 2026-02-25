package com.EreliaStudio.OneBlock;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public final class OneBlockDropRegistry
{
    public static final String DEFAULT_ITEM_ID = "Ingredient_Fibre";

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

    public void registerDefaultWeights(Map<String, Map<String, Integer>> defaultsByChapter)
    {
        if (defaultsByChapter == null || defaultsByChapter.isEmpty())
        {
            return;
        }

        for (Map.Entry<String, Map<String, Integer>> entry : defaultsByChapter.entrySet())
        {
            String chapterId = entry.getKey();
            Map<String, Integer> weights = entry.getValue();
            if (weights == null || weights.isEmpty())
            {
                continue;
            }

            weightByChapter
                    .computeIfAbsent(chapterId, key -> new HashMap<>())
                    .putAll(weights);
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
        return weight == null ? 1 : weight;
    }
}
