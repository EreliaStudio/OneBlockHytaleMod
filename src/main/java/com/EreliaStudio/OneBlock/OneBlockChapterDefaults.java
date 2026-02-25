package com.EreliaStudio.OneBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OneBlockChapterDefaults
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

        defaults.put("A1", List.of(
                drop("Ingredient_Fibre", 10),
                drop("Rubble_Stone", 10)
        ));

        defaults.put("A2", List.of(
                drop("Wood_Ash_Trunk", 10),
                drop("Rock_Stone_Cobble", 10),
                drop("Ingredient_Fibre", 3),
                drop("Rubble_Stone", 3),
                drop("Soil_Grass", 5),
                drop("Ingredient_Stick", 3)
        ));

        defaults.put("A3", List.of(
                drop("Wood_Ash_Trunk", 15),
                drop("Rock_Stone_Cobble", 15),
                drop("Ingredient_Fibre", 3),
                drop("Rubble_Stone", 3),
                drop("Soil_Grass", 7),
                drop("Ingredient_Stick", 3)
        ));

        defaults.put("B1", List.of(
                drop("Wood_Ash_Trunk", 15),
                drop("Rock_Stone_Cobble", 15)
        ));

        defaults.put("B2", List.of(
                drop("Wood_Ash_Trunk", 15),
                drop("Rock_Stone_Cobble", 15)
        ));

        defaults.put("B3", List.of(
                drop(OneBlockDropId.entityDropId("Zombie"), 10),
                drop(OneBlockDropId.entityDropId("Skeleton"), 10),
                drop(OneBlockDropId.entityDropId("Crawler_Void"), 2)
        ));

        DEFAULTS = Collections.unmodifiableMap(defaults);
        DEFAULT_IDS = buildDefaultIds(DEFAULTS);
        DEFAULT_WEIGHTS = buildDefaultWeights(DEFAULTS);
    }

    private OneBlockChapterDefaults()
    {
    }

    public static List<DropDefinition> getDefaults(String chapterId)
    {
        String key = normalizeChapter(chapterId);
        List<DropDefinition> defaults = DEFAULTS.get(key);
        if (defaults == null)
        {
            return List.of();
        }
        return defaults;
    }

    public static List<String> getDefaultDropIds(String chapterId)
    {
        String key = normalizeChapter(chapterId);
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

    public static boolean isDefaultDrop(String chapterId, String dropId)
    {
        if (dropId == null || dropId.isEmpty())
        {
            return false;
        }

        List<String> ids = DEFAULT_IDS.get(normalizeChapter(chapterId));
        return ids != null && ids.contains(dropId);
    }

    public static void ensureDefaults(String chapterId, OneBlockPlayerChapterDropsState state)
    {
        if (state == null)
        {
            return;
        }

        state.unlockedDrops.removeIf(item -> item == null || item.isEmpty());
        state.enabledDrops.removeIf(item -> item == null || item.isEmpty());

        List<String> defaults = getDefaultDropIds(chapterId);
        if (!defaults.isEmpty())
        {
            state.unlockedDrops.addAll(defaults);
            if (state.enabledDrops.isEmpty())
            {
                state.enabledDrops.addAll(defaults);
            }
        }

        if (state.unlockedDrops.isEmpty())
        {
            state.unlockedDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
        }
        if (state.enabledDrops.isEmpty())
        {
            state.enabledDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);
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

    private static String normalizeChapter(String chapterId)
    {
        if (chapterId == null || chapterId.isEmpty())
        {
            return OneBlockChapterResolver.DEFAULT_CHAPTER;
        }
        return chapterId;
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
