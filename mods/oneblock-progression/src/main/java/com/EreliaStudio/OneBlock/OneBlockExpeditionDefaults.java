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
        public final List<String> unlockKnowledgeIds;
        public final List<DropDefinition> drops;

        public ExpeditionDefinition(String expeditionId, String blockId,
                                    List<String> unlockKnowledgeIds, List<DropDefinition> drops)
        {
            this.expeditionId = expeditionId;
            this.blockId = blockId;
            this.unlockKnowledgeIds = Collections.unmodifiableList(new ArrayList<>(unlockKnowledgeIds));
            this.drops = Collections.unmodifiableList(new ArrayList<>(drops));
        }
    }

    private static final Map<String, ExpeditionDefinition> EXPEDITIONS;
    private static final Map<String, List<String>> DEFAULT_IDS;
    private static final Map<String, Map<String, Integer>> DEFAULT_WEIGHTS;

    static
    {
        Map<String, ExpeditionDefinition> expeditions = new HashMap<>();

        register(expeditions, "Cave", List.of(
                drop("Rock_Stone", 30),
                drop("Rubble_Stone", 20),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Deep_Cave", List.of(
                drop("Rock_Basalt", 30),
                drop("Rubble_Basalt", 20),
                drop("Ingredient_Void_Essence", 2)
        ));

        register(expeditions, "The_Abyss", List.of(
                drop("Rock_Volcanic", 25),
                drop("Rock_Slate", 15),
                drop("Ingredient_Void_Essence", 3)
        ));

        register(expeditions, "Gem_Cave", List.of(
                drop("Rock_Stone", 20),
                drop("Rock_Basalt", 15),
                drop("Rock_Shale", 10)
        ));

        register(expeditions, "Desert", List.of(
                drop("Block_Sand", 25),
                drop("Rock_Sandstone", 20),
                drop("Block_Sandstone_Rough", 15),
                drop("Ingredient_Fire_Essence", 2)
        ));

        register(expeditions, "Egyptian_Temple", List.of(
                drop("Block_Sandstone_Smooth", 25),
                drop("Block_Sandstone_Red", 15),
                drop("Rock_Sandstone", 15),
                drop("Ingredient_Fire_Essence", 2)
        ));

        register(expeditions, "Forest", List.of(
                drop("Wood_Oak_Trunk", 20),
                drop("Plant_Leaves_Oak", 15),
                drop("Ingredient_Fibre", 15),
                drop("Ingredient_Life_Essence", 3)
        ));

        register(expeditions, "Deep_Forest", List.of(
                drop("Wood_Ash_Trunk", 20),
                drop("Wood_Birch_Trunk", 15),
                drop("Ingredient_Fibre", 10),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Jungle", List.of(
                drop("Wood_Jungle_Trunk", 20),
                drop("Wood_Bamboo_Trunk", 15),
                drop("Plant_Reeds_Marsh", 10),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Azure_Forest", List.of(
                drop("Wood_Azure_Trunk", 20),
                drop("Plant_Crop_Mushroom_Block_Blue", 15),
                drop("Plant_Fruit_Azure", 8),
                drop("Ingredient_Ice_Essence", 2)
        ));

        register(expeditions, "Fiery_Forest", List.of(
                drop("Wood_Burnt_Trunk", 20),
                drop("Rock_Volcanic", 15),
                drop("Ingredient_Charcoal", 12),
                drop("Ingredient_Fire_Essence", 3)
        ));

        register(expeditions, "Snow", List.of(
                drop("Block_Snow", 30),
                drop("Block_Snow_Packed", 20),
                drop("Ingredient_Ice_Essence", 2)
        ));

        register(expeditions, "Frozen_Forest", List.of(
                drop("Wood_Ice_Trunk", 20),
                drop("Block_Snow", 15),
                drop("Block_Permafrost", 15),
                drop("Ingredient_Ice_Essence", 2)
        ));

        register(expeditions, "Frozen_Cave", List.of(
                drop("Block_Ice_Blue", 25),
                drop("Rock_Ice", 20),
                drop("Rock_Ice_Permafrost", 15),
                drop("Ingredient_Ice_Essence", 3)
        ));

        register(expeditions, "Meadow", List.of(
                drop("Ingredient_Fibre", 10),
                drop("Rubble_Stone", 10),
                drop("Ingredient_Stick", 10),
                drop("Soil_Dirt", 10)
        ));

        register(expeditions, "Plains", List.of(
                drop("Soil_Grass", 25),
                drop("Plant_Grass_Lush", 20),
                drop("Ingredient_Fibre", 15),
                drop("Ingredient_Life_Essence", 3)
        ));

        register(expeditions, "Inferno", List.of(
                drop("Rock_Volcanic", 25),
                drop("Block_Volcanic_Ash", 15),
                drop("Ingredient_Charcoal", 12),
                drop("Ingredient_Fire_Essence", 3)
        ));

        register(expeditions, "Deep_Inferno", List.of(
                drop("Block_Lava_Rock_Cracked", 20),
                drop("Rock_Volcanic", 20),
                drop("Ingredient_Fire_Essence", 3),
                drop("Ingredient_Void_Essence", 2)
        ));

        register(expeditions, "Graveyard", List.of(
                drop("Soil_Dirt", 25),
                drop("Rock_Stone_Mossy", 20),
                drop("Ingredient_Void_Essence", 2)
        ));

        register(expeditions, "Ancient_Graveyard", List.of(
                drop("Rock_Stone_Mossy", 20),
                drop("Rock_Slate", 15),
                drop("Ingredient_Bone_Fragment", 10),
                drop("Ingredient_Void_Essence", 3)
        ));

        register(expeditions, "Cursed_Crypt", List.of(
                drop("Rock_Basalt", 20),
                drop("Rock_Volcanic", 15),
                drop("Ingredient_Bone_Fragment", 10),
                drop("Ingredient_Void_Essence", 3)
        ));

        register(expeditions, "Primal_Jungle", List.of(
                drop("Wood_Jungle_Trunk", 20),
                drop("Block_Fossil_Stone", 15),
                drop("Plant_Primal_Fern", 10),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Dino_Caverns", List.of(
                drop("Block_Primal_Stone", 20),
                drop("Block_Lava_Rock_Cracked", 15),
                drop("Rock_Volcanic", 15),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Volcanic_Badlands", List.of(
                drop("Block_Volcanic_Ash", 20),
                drop("Rock_Volcanic", 20),
                drop("Block_Lava_Rock_Cracked", 15),
                drop("Ingredient_Fire_Essence", 2)
        ));

        EXPEDITIONS = Collections.unmodifiableMap(expeditions);
        DEFAULT_IDS = buildDefaultIds(EXPEDITIONS);
        DEFAULT_WEIGHTS = buildDefaultWeights(EXPEDITIONS);
    }

    private OneBlockExpeditionDefaults() {}

    // --- Existing public API ---

    public static List<DropDefinition> getDefaults(String expeditionId)
    {
        ExpeditionDefinition def = EXPEDITIONS.get(expeditionId);
        return def == null ? List.of() : def.drops;
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

    public static Set<String> getExpeditionIds()
    {
        return EXPEDITIONS.keySet();
    }

    // --- New public API ---

    public static ExpeditionDefinition getExpedition(String expeditionId)
    {
        return EXPEDITIONS.get(expeditionId);
    }

    public static List<String> getUnlockKnowledgeIds(String expeditionId)
    {
        ExpeditionDefinition def = EXPEDITIONS.get(expeditionId);
        return def == null ? List.of() : def.unlockKnowledgeIds;
    }

    public static String getBlockId(String expeditionId)
    {
        ExpeditionDefinition def = EXPEDITIONS.get(expeditionId);
        return def == null ? null : def.blockId;
    }

    // --- Private helpers ---

    private static DropDefinition drop(String dropId, int weight)
    {
        return new DropDefinition(dropId, Math.max(1, weight));
    }

    private static void register(Map<String, ExpeditionDefinition> map, String expeditionId,
                                 List<DropDefinition> drops)
    {
        String blockId = "OneBlock_Block_" + expeditionId;
        List<String> knowledgeIds = List.of("Bench_OneBlock_" + expeditionId);
        map.put(expeditionId, new ExpeditionDefinition(expeditionId, blockId, knowledgeIds, drops));
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
