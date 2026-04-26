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

    private static final Map<String, List<DropDefinition>> DEFAULTS;
    private static final Map<String, List<String>> DEFAULT_IDS;
    private static final Map<String, Map<String, Integer>> DEFAULT_WEIGHTS;

    static
    {
        Map<String, List<DropDefinition>> defaults = new HashMap<>();

        defaults.put("Cave", List.of(
                drop("Rock_Stone", 30),
                drop("Rubble_Stone", 20),
                drop("Ingredient_Life_Essence", 2)
        ));

        defaults.put("Deep Cave", List.of(
                drop("Rock_Basalt", 30),
                drop("Rubble_Basalt", 20),
                drop("Ingredient_Void_Essence", 2)
        ));

        defaults.put("The Abyss", List.of(
                drop("Rock_Volcanic", 25),
                drop("Rock_Slate", 15),
                drop("Ingredient_Void_Essence", 3)
        ));

        defaults.put("Gem Cave", List.of(
                drop("Rock_Stone", 20),
                drop("Rock_Basalt", 15),
                drop("Rock_Shale", 10)
        ));

        defaults.put("Desert", List.of(
                drop("Block_Sand", 25),
                drop("Rock_Sandstone", 20),
                drop("Block_Sandstone_Rough", 15),
                drop("Ingredient_Fire_Essence", 2)
        ));

        defaults.put("Egyptian Temple", List.of(
                drop("Block_Sandstone_Smooth", 25),
                drop("Block_Sandstone_Red", 15),
                drop("Rock_Sandstone", 15),
                drop("Ingredient_Fire_Essence", 2)
        ));

        defaults.put("Forest", List.of(
                drop("Wood_Oak_Trunk", 20),
                drop("Plant_Leaves_Oak", 15),
                drop("Ingredient_Fibre", 15),
                drop("Ingredient_Life_Essence", 3)
        ));

        defaults.put("Deep Forest", List.of(
                drop("Wood_Ash_Trunk", 20),
                drop("Wood_Birch_Trunk", 15),
                drop("Ingredient_Fibre", 10),
                drop("Ingredient_Life_Essence", 2)
        ));

        defaults.put("Jungle", List.of(
                drop("Wood_Jungle_Trunk", 20),
                drop("Wood_Bamboo_Trunk", 15),
                drop("Plant_Reeds_Marsh", 10),
                drop("Ingredient_Life_Essence", 2)
        ));

        defaults.put("Azure Forest", List.of(
                drop("Wood_Azure_Trunk", 20),
                drop("Plant_Crop_Mushroom_Block_Blue", 15),
                drop("Plant_Fruit_Azure", 8),
                drop("Ingredient_Ice_Essence", 2)
        ));

        defaults.put("Fiery Forest", List.of(
                drop("Wood_Burnt_Trunk", 20),
                drop("Rock_Volcanic", 15),
                drop("Ingredient_Charcoal", 12),
                drop("Ingredient_Fire_Essence", 3)
        ));

        defaults.put("Snow", List.of(
                drop("Block_Snow", 30),
                drop("Block_Snow_Packed", 20),
                drop("Ingredient_Ice_Essence", 2)
        ));

        defaults.put("Frozen Forest", List.of(
                drop("Wood_Ice_Trunk", 20),
                drop("Block_Snow", 15),
                drop("Block_Permafrost", 15),
                drop("Ingredient_Ice_Essence", 2)
        ));

        defaults.put("Frozen Cave", List.of(
                drop("Block_Ice_Blue", 25),
                drop("Rock_Ice", 20),
                drop("Rock_Ice_Permafrost", 15),
                drop("Ingredient_Ice_Essence", 3)
        ));

        defaults.put("Plains", List.of(
                drop("Soil_Grass", 25),
                drop("Plant_Grass_Lush", 20),
                drop("Ingredient_Fibre", 15),
                drop("Ingredient_Life_Essence", 3)
        ));

        defaults.put("Inferno", List.of(
                drop("Rock_Volcanic", 25),
                drop("Block_Volcanic_Ash", 15),
                drop("Ingredient_Charcoal", 12),
                drop("Ingredient_Fire_Essence", 3)
        ));

        defaults.put("Deep Inferno", List.of(
                drop("Block_Lava_Rock_Cracked", 20),
                drop("Rock_Volcanic", 20),
                drop("Ingredient_Fire_Essence", 3),
                drop("Ingredient_Void_Essence", 2)
        ));

        defaults.put("Graveyard", List.of(
                drop("Soil_Dirt", 25),
                drop("Rock_Stone_Mossy", 20),
                drop("Ingredient_Void_Essence", 2)
        ));

        defaults.put("Ancient Graveyard", List.of(
                drop("Rock_Stone_Mossy", 20),
                drop("Rock_Slate", 15),
                drop("Ingredient_Bone_Fragment", 10),
                drop("Ingredient_Void_Essence", 3)
        ));

        defaults.put("Cursed Crypt", List.of(
                drop("Rock_Basalt", 20),
                drop("Rock_Volcanic", 15),
                drop("Ingredient_Bone_Fragment", 10),
                drop("Ingredient_Void_Essence", 3)
        ));

        defaults.put("Primal Jungle", List.of(
                drop("Wood_Jungle_Trunk", 20),
                drop("Block_Fossil_Stone", 15),
                drop("Plant_Primal_Fern", 10),
                drop("Ingredient_Life_Essence", 2)
        ));

        defaults.put("Dino Caverns", List.of(
                drop("Block_Primal_Stone", 20),
                drop("Block_Lava_Rock_Cracked", 15),
                drop("Rock_Volcanic", 15),
                drop("Ingredient_Life_Essence", 2)
        ));

        defaults.put("Volcanic Badlands", List.of(
                drop("Block_Volcanic_Ash", 20),
                drop("Rock_Volcanic", 20),
                drop("Block_Lava_Rock_Cracked", 15),
                drop("Ingredient_Fire_Essence", 2)
        ));

        DEFAULTS = Collections.unmodifiableMap(defaults);
        DEFAULT_IDS = buildDefaultIds(DEFAULTS);
        DEFAULT_WEIGHTS = buildDefaultWeights(DEFAULTS);
    }

    private OneBlockExpeditionDefaults() {}

    public static List<DropDefinition> getDefaults(String expeditionId)
    {
        List<DropDefinition> d = DEFAULTS.get(expeditionId);
        return d == null ? List.of() : d;
    }

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

    /** Returns all expedition IDs that have registered drop pools. */
    public static Set<String> getExpeditionIds()
    {
        return DEFAULTS.keySet();
    }

    private static DropDefinition drop(String dropId, int weight)
    {
        return new DropDefinition(dropId, Math.max(1, weight));
    }

    private static Map<String, List<String>> buildDefaultIds(Map<String, List<DropDefinition>> defaults)
    {
        Map<String, List<String>> out = new HashMap<>();
        for (Map.Entry<String, List<DropDefinition>> entry : defaults.entrySet())
        {
            List<String> ids = new ArrayList<>();
            for (DropDefinition def : entry.getValue())
            {
                if (def != null && def.dropId != null && !def.dropId.isEmpty()) ids.add(def.dropId);
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
                if (def == null || def.dropId == null || def.dropId.isEmpty()) continue;
                weights.put(def.dropId, Math.max(1, def.weight));
            }
            out.put(entry.getKey(), Collections.unmodifiableMap(weights));
        }
        return Collections.unmodifiableMap(out);
    }
}
