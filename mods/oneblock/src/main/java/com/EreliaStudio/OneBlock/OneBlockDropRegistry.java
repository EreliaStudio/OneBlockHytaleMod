package com.EreliaStudio.OneBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public final class OneBlockDropRegistry
{
    public static final String DEFAULT_ITEM_ID = "Ingredient_Fibre";

    private final Random rng = new Random();
    private final Map<String, Map<String, Integer>> weightByPool = new HashMap<>();
    private final Map<String, Dropable> dropableById = new ConcurrentHashMap<>();

    public String pickReward(String poolId, List<String> availableDrops)
    {
        if (availableDrops == null || availableDrops.isEmpty()) return DEFAULT_ITEM_ID;

        int totalWeight = 0;
        for (String dropId : availableDrops)
        {
            int w = getWeight(poolId, dropId);
            if (w > 0) totalWeight += w;
        }

        if (totalWeight <= 0) return DEFAULT_ITEM_ID;

        int roll = rng.nextInt(totalWeight);
        int cursor = 0;
        for (String dropId : availableDrops)
        {
            int w = getWeight(poolId, dropId);
            if (w <= 0) continue;
            cursor += w;
            if (roll < cursor) return dropId;
        }

        return DEFAULT_ITEM_ID;
    }

    public void registerWeight(String poolId, String dropableId, int weight)
    {
        if (dropableId == null || dropableId.isEmpty()) return;
        String poolKey = OneBlockPools.normalizePoolId(poolId);
        int safeWeight = Math.max(1, weight);
        weightByPool.computeIfAbsent(poolKey, k -> new HashMap<>()).put(dropableId, safeWeight);
    }

    public void registerDefaultWeights(Map<String, Map<String, Integer>> defaultsByExpedition)
    {
        if (defaultsByExpedition == null || defaultsByExpedition.isEmpty()) return;

        for (Map.Entry<String, Map<String, Integer>> entry : defaultsByExpedition.entrySet())
        {
            String expeditionId = OneBlockPools.normalizePoolId(entry.getKey());
            Map<String, Integer> weights = entry.getValue();
            if (weights == null || weights.isEmpty()) continue;
            weightByPool.computeIfAbsent(expeditionId, k -> new HashMap<>()).putAll(weights);
        }
    }

    public List<String> getKnownDrops(String expeditionId)
    {
        String expeditionKey = OneBlockPools.normalizePoolId(expeditionId);
        Map<String, Integer> weights = weightByPool.get(expeditionKey);
        if (weights == null || weights.isEmpty()) return List.of();

        List<String> out = new ArrayList<>(weights.keySet());
        out.sort(String::compareToIgnoreCase);
        return out;
    }

    public void registerDropable(Dropable dropable)
    {
        if (dropable == null) return;
        String id = dropable.getId();
        if (id != null && !id.isEmpty()) dropableById.put(id, dropable);
    }

    public Dropable getDropable(String dropableId)
    {
        if (dropableId == null || dropableId.isEmpty()) return null;
        return dropableById.get(dropableId);
    }

    public boolean executeDropable(String dropableId, DropableContext context)
    {
        Dropable dropable = getDropable(dropableId);
        if (dropable == null || context == null) return false;
        dropable.execute(context);
        return true;
    }

    private int getWeight(String expeditionId, String dropId)
    {
        if (dropId == null || dropId.isEmpty()) return 0;
        String expeditionKey = OneBlockPools.normalizePoolId(expeditionId);
        Map<String, Integer> weights = weightByPool.get(expeditionKey);
        if (weights == null) return 1;
        Integer weight = weights.get(dropId);
        return weight == null ? 1 : weight;
    }
}
