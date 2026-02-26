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
    private final Map<String, Map<String, Integer>> weightByExpedition = new HashMap<>();

    public String pickReward(List<String> availableDrops)
    {
        return pickReward(OneBlockExpeditionResolver.DEFAULT_EXPEDITION, availableDrops);
    }

    public String pickReward(String expeditionId, List<String> availableDrops)
    {
        if (availableDrops == null || availableDrops.isEmpty())
        {
            return DEFAULT_ITEM_ID;
        }

        int totalWeight = 0;
        for (String dropId : availableDrops)
        {
            int weight = getWeight(expeditionId, dropId);
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
        for (String dropId : availableDrops)
        {
            int weight = getWeight(expeditionId, dropId);
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

            String expeditionId = OneBlockExpeditionResolver.normalizeExpedition(definition.expeditionId);

            int weight = definition.weight;
            if (weight < 1)
            {
                weight = 1;
            }

            weightByExpedition
                    .computeIfAbsent(expeditionId, key -> new HashMap<>())
                    .put(dropId, weight);
        }
    }

    public void registerDefaultWeights(Map<String, Map<String, Integer>> defaultsByExpedition)
    {
        if (defaultsByExpedition == null || defaultsByExpedition.isEmpty())
        {
            return;
        }

        for (Map.Entry<String, Map<String, Integer>> entry : defaultsByExpedition.entrySet())
        {
            String expeditionId = OneBlockExpeditionResolver.normalizeExpedition(entry.getKey());
            Map<String, Integer> weights = entry.getValue();
            if (weights == null || weights.isEmpty())
            {
                continue;
            }

            weightByExpedition
                    .computeIfAbsent(expeditionId, key -> new HashMap<>())
                    .putAll(weights);
        }
    }

    private int getWeight(String expeditionId, String dropId)
    {
        if (dropId == null || dropId.isEmpty())
        {
            return 0;
        }

        String expeditionKey = OneBlockExpeditionResolver.normalizeExpedition(expeditionId);

        Map<String, Integer> weights = weightByExpedition.get(expeditionKey);
        if (weights == null)
        {
            return 1;
        }

        Integer weight = weights.get(dropId);
        return weight == null ? 1 : weight;
    }
}
