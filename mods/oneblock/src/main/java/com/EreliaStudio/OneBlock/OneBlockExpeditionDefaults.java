package com.EreliaStudio.OneBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public static final class ExpeditionDefinition
    {
        public final String expeditionId;
        public final String blockId;
        public final List<DropDefinition> drops;

        public ExpeditionDefinition(String expeditionId, String blockId, List<DropDefinition> drops)
        {
            this.expeditionId = expeditionId;
            this.blockId = blockId;
            this.drops = Collections.unmodifiableList(new ArrayList<>(drops));
        }
    }

    private static final Map<String, ExpeditionDefinition> EXPEDITIONS;
    private static final Map<String, List<String>> DEFAULT_IDS;
    private static final Map<String, Map<String, Integer>> DEFAULT_WEIGHTS;

    static
    {
        Map<String, ExpeditionDefinition> expeditions = new HashMap<>();

        register(expeditions, "Meadow", List.of(
                drop("Ingredient_Fibre", 20),
                drop("Rubble_Stone", 20),
                drop("Soil_Dirt", 10)
        ));

        register(expeditions, "Meadow_Cave", List.of(
                drop("Rubble_Stone", 20),
                drop("Rock_Stone", 20)
        ));

        register(expeditions, "Meadow_Forest", List.of(
                drop("Ingredient_Stick", 20),
                drop("Wood_Oak_Trunk", 10)
        ));

        register(expeditions, "Cave_Entry", List.of(
                drop("Rubble_Stone", 20),
                drop("Rock_Stone", 10),
                drop("Ore_Copper", 6)
        ));

        register(expeditions, "Quarry", List.of(
                drop("Rock_Stone", 10),
                drop("Soil_Dirt", 6),
                drop("Rock_Stone_Mossy", 1)
        ));

        register(expeditions, "Cave", List.of(
                drop("Rock_Stone", 15),
                drop("Ore_Copper", 10),
                drop("Ore_Iron", 3)
        ));

        register(expeditions, "Forest", List.of(
                drop("Wood_Oak_Trunk", 10),
                drop("Wood_Beech_Trunk", 10),
                drop("Wood_Birch_Trunk", 10),
                drop("Plant_Crop_Mushroom_Common_Brown", 5),
                drop("Plant_Crop_Mushroom_Cap_Brown", 5),
                drop("Ingredient_Life_Essence", 1)
        ));

        register(expeditions, "Fairy_pond", List.of(
                drop("Plant_Crop_Mushroom_Glowing_Blue", 10),
                drop("Plant_Crop_Mushroom_Glowing_Purple", 10),
                drop("Plant_Fruit_Azure", 3),
                drop("Ingredient_Life_Essence", 3),
                drop("Wood_Azure_Trunk", 15)
        ));

        EXPEDITIONS = Collections.unmodifiableMap(expeditions);
        DEFAULT_IDS = buildDefaultIds(EXPEDITIONS);
        DEFAULT_WEIGHTS = buildDefaultWeights(EXPEDITIONS);
    }

    private OneBlockExpeditionDefaults() {}

    public static List<String> getDefaultDropIds(String expeditionId)
    {
        List<String> ids = DEFAULT_IDS.get(expeditionId);
        if (ids == null || ids.isEmpty()) return List.of(OneBlockDropRegistry.DEFAULT_ITEM_ID);
        return ids;
    }

    public static Map<String, Map<String, Integer>> getDefaultWeights()
    {
        return DEFAULT_WEIGHTS;
    }

    public static Map<String, Set<String>> getDefaultDropIdsByExpedition()
    {
        Map<String, Set<String>> out = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : DEFAULT_IDS.entrySet())
        {
            out.put(entry.getKey(), Set.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(out);
    }

    public static Set<String> getExpeditionIds()
    {
        return EXPEDITIONS.keySet();
    }

    public static String getBlockId(String expeditionId)
    {
        ExpeditionDefinition def = EXPEDITIONS.get(expeditionId);
        return def == null ? null : def.blockId;
    }

    private static DropDefinition drop(String dropId, int weight)
    {
        return new DropDefinition(dropId, Math.max(1, weight));
    }

    private static void register(Map<String, ExpeditionDefinition> map, String expeditionId, List<DropDefinition> drops)
    {
        String blockId = "OneBlock_Block_" + expeditionId;
        map.put(expeditionId, new ExpeditionDefinition(expeditionId, blockId, drops));
    }

    private static Map<String, List<String>> buildDefaultIds(Map<String, ExpeditionDefinition> expeditions)
    {
        Map<String, List<String>> out = new HashMap<>();
        for (Map.Entry<String, ExpeditionDefinition> entry : expeditions.entrySet())
        {
            List<String> ids = new ArrayList<>();
            for (DropDefinition def : entry.getValue().drops)
            {
                if (def != null && def.dropId != null && !def.dropId.isEmpty()) ids.add(def.dropId);
            }
            out.put(entry.getKey(), Collections.unmodifiableList(ids));
        }
        return Collections.unmodifiableMap(out);
    }

    private static Map<String, Map<String, Integer>> buildDefaultWeights(Map<String, ExpeditionDefinition> expeditions)
    {
        Map<String, Map<String, Integer>> out = new HashMap<>();
        for (Map.Entry<String, ExpeditionDefinition> entry : expeditions.entrySet())
        {
            Map<String, Integer> weights = new HashMap<>();
            for (DropDefinition def : entry.getValue().drops)
            {
                if (def == null || def.dropId == null || def.dropId.isEmpty()) continue;
                weights.put(def.dropId, Math.max(1, def.weight));
            }
            out.put(entry.getKey(), Collections.unmodifiableMap(weights));
        }
        return Collections.unmodifiableMap(out);
    }
}
