package com.EreliaStudio.OneBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OneBlockExpeditionDefaults
{
    public static final class DropDefinition
    {
        public final String dropId;
        public final int weight;

        public DropDefinition(String dropId, int weight)
        {
            this.dropId = dropId;
            this.weight = weight;
        }
    }

    private static final Map<String, List<DropDefinition>> DEFAULTS;
    private static final Map<String, List<String>> DEFAULT_IDS;
    private static final Map<String, Map<String, Integer>> DEFAULT_WEIGHTS;

    static
    {
        Map<String, List<DropDefinition>> defaults = new HashMap<>();

        defaults.put("Meadow", List.of(
                drop("Wood_Ash_Trunk", 10),
                drop("Rock_Stone_Cobble", 10),
                drop("Ingredient_Fibre", 3),
                drop("Rubble_Stone", 3),
                drop("Soil_Grass", 5),
                drop("Ingredient_Stick", 3)
        ));

        defaults.put("Forest", List.of(
                drop("Wood_Ash_Trunk", 15),
                drop("Rock_Stone_Cobble", 15),
                drop("Ingredient_Fibre", 3),
                drop("Rubble_Stone", 3),
                drop("Soil_Grass", 7),
                drop("Ingredient_Stick", 3)
        ));

        defaults.put("Cave", List.of(
                drop("Rock_Stone_Cobble", 15),
                drop("Rubble_Stone", 10),
                drop(OneBlockDropId.entityDropId("Zombie"), 6),
                drop(OneBlockDropId.entityDropId("Skeleton"), 6),
                drop(OneBlockDropId.entityDropId("Crawler_Void"), 2)
        ));

        DEFAULTS = Collections.unmodifiableMap(defaults);
        DEFAULT_IDS = buildDefaultIds(DEFAULTS);
        DEFAULT_WEIGHTS = buildDefaultWeights(DEFAULTS);
    }

    private OneBlockExpeditionDefaults()
    {
    }

    public static List<DropDefinition> getDefaults(String expeditionId)
    {
        String key = OneBlockExpeditionResolver.normalizeExpedition(expeditionId);
        List<DropDefinition> defaults = DEFAULTS.get(key);
        if (defaults == null)
        {
            return List.of();
        }
        return defaults;
    }

    public static List<String> getDefaultDropIds(String expeditionId)
    {
        String key = OneBlockExpeditionResolver.normalizeExpedition(expeditionId);
        List<String> ids = DEFAULT_IDS.get(key);
        if (ids == null || ids.isEmpty())
        {
            return List.of(OneBlockDropRegistry.DEFAULT_ITEM_ID);
        }
        return ids;
    }

    public static Map<String, Map<String, Integer>> getDefaultWeights()
    {
        return DEFAULT_WEIGHTS;
    }

    public static boolean isDefaultDrop(String expeditionId, String dropId)
    {
        if (dropId == null || dropId.isEmpty())
        {
            return false;
        }

        List<String> ids = DEFAULT_IDS.get(OneBlockExpeditionResolver.normalizeExpedition(expeditionId));
        return ids != null && ids.contains(dropId);
    }

    public static void ensureDefaults(String expeditionId, OneBlockPlayerExpeditionDropsState state)
    {
        if (state == null)
        {
            return;
        }

        state.unlockedDrops.removeIf(item -> item == null || item.isEmpty());

        List<String> defaults = getDefaultDropIds(expeditionId);
        if (!defaults.isEmpty())
        {
            state.unlockedDrops.addAll(defaults);
        }

        if (state.unlockedDrops.isEmpty())
        {
            state.unlockedDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
        }
    }

    private static DropDefinition drop(String dropId, int weight)
    {
        if (weight < 1)
        {
            weight = 1;
        }
        return new DropDefinition(dropId, weight);
    }

    private static Map<String, List<String>> buildDefaultIds(Map<String, List<DropDefinition>> defaults)
    {
        Map<String, List<String>> out = new HashMap<>();
        for (Map.Entry<String, List<DropDefinition>> entry : defaults.entrySet())
        {
            List<String> ids = new ArrayList<>();
            for (DropDefinition def : entry.getValue())
            {
                if (def != null && def.dropId != null && !def.dropId.isEmpty())
                {
                    ids.add(def.dropId);
                }
            }
            out.put(entry.getKey(), Collections.unmodifiableList(ids));
        }
        return Collections.unmodifiableMap(out);
    }

    private static Map<String, Map<String, Integer>> buildDefaultWeights(Map<String, List<DropDefinition>> defaults)
    {
        Map<String, Map<String, Integer>> out = new HashMap<>();
        for (Map.Entry<String, List<DropDefinition>> entry : defaults.entrySet())
        {
            Map<String, Integer> weights = new HashMap<>();
            for (DropDefinition def : entry.getValue())
            {
                if (def == null || def.dropId == null || def.dropId.isEmpty())
                {
                    continue;
                }
                int weight = def.weight < 1 ? 1 : def.weight;
                weights.put(def.dropId, weight);
            }
            out.put(entry.getKey(), Collections.unmodifiableMap(weights));
        }
        return Collections.unmodifiableMap(out);
    }
}
