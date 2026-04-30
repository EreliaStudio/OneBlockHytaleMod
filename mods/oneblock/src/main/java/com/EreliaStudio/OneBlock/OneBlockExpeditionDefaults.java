package com.EreliaStudio.OneBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    public static final class CompletionRewardDefinition
    {
        public final String dropId;
        public final int quantity;

        public CompletionRewardDefinition(String dropId, int quantity)
        {
            this.dropId = dropId;
            this.quantity = Math.max(1, quantity);
        }
    }

    public static final class ExpeditionDefinition
    {
        public final String expeditionId;
        public final String blockId;
        public final int ticks;
        public final List<DropDefinition> drops;
        public final List<CompletionRewardDefinition> completionRewards;

        public ExpeditionDefinition(String expeditionId, String blockId, int ticks, List<DropDefinition> drops)
        {
            this(expeditionId, blockId, ticks, drops, List.of());
        }

        public ExpeditionDefinition(String expeditionId,
                                    String blockId,
                                    int ticks,
                                    List<DropDefinition> drops,
                                    List<CompletionRewardDefinition> completionRewards)
        {
            this.expeditionId = expeditionId;
            this.blockId = blockId;
            this.ticks = ticks;
            this.drops = drops == null || drops.isEmpty()
                    ? List.of()
                    : Collections.unmodifiableList(new ArrayList<>(drops));
            this.completionRewards = completionRewards == null || completionRewards.isEmpty()
                    ? List.of()
                    : Collections.unmodifiableList(new ArrayList<>(completionRewards));
        }
    }

    private static final Map<String, ExpeditionDefinition> EXPEDITIONS;
    private static final Map<String, List<String>> DEFAULT_IDS;
    private static final Map<String, Map<String, Integer>> DEFAULT_WEIGHTS;
    private static final Set<String> COMPLETION_REWARD_DROP_IDS;

    static
    {
        Map<String, ExpeditionDefinition> expeditions = new HashMap<>();

        register(expeditions, "Meadow", 25, List.of(
                drop("Ingredient_Fibre", 20),
                drop("Rubble_Stone", 20),
                drop("Soil_Dirt", 10)
        ));

        register(expeditions, "Meadow_Cave", 25, List.of(
                drop("Rubble_Stone", 20),
                drop("Rock_Stone", 40),
                drop(OneBlockDropId.entityDropId("Rat"), 1)
        ), List.of(
                reward("ExpeditionPoint", 4)
        ));

        register(expeditions, "Meadow_Forest", 25, List.of(
                drop("Ingredient_Stick", 20),
                drop("Wood_Oak_Trunk", 30),
                drop("Plant_Sapling_Oak", 1),
                drop(OneBlockDropId.entityDropId("Rabbit"), 1),
                drop(OneBlockDropId.entityDropId("Chicken"), 1)
        ), List.of(
                reward("ExpeditionPoint", 4)
        ));

        register(expeditions, "Cave_Entry", 25, List.of(
                drop("Rubble_Stone", 20),
                drop("Rock_Stone", 10),
                drop("Ore_Copper", 6),
                drop(OneBlockDropId.entityDropId("Rat"), 3),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 2)
        ), List.of(
                reward("ExpeditionPoint", 8)
        ));

        register(expeditions, "Quarry", 50, List.of(
                drop("Rock_Stone", 10),
                drop("Soil_Dirt", 6),
                drop("Rock_Stone_Mossy", 1),
                drop(OneBlockDropId.entityDropId("Rat"), 3)
        ));

        register(expeditions, "Cave", 25, List.of(
                drop("Rock_Stone", 15),
                drop("Ore_Copper", 10),
                drop("Ore_Iron", 6),
                drop(OneBlockDropId.entityDropId("Rat"), 3),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 5)
        ), List.of(
                reward("ExpeditionPoint", 8)
        ));

        register(expeditions, "Forest", 25, List.of(
                drop("Wood_Oak_Trunk", 10),
                drop("Plant_Sapling_Oak", 2),
                drop("Wood_Beech_Trunk", 10),
                drop("Plant_Sapling_Beech", 2),
                drop("Wood_Birch_Trunk", 10),
                drop("Plant_Sapling_Birch", 2),
                drop("Plant_Crop_Mushroom_Common_Brown", 5),
                drop("Plant_Crop_Mushroom_Cap_Brown", 5),
                drop("Ingredient_Life_Essence", 5),
                drop(OneBlockDropId.entityDropId("Chicken"), 3),
                drop(OneBlockDropId.entityDropId("Boar"), 3)
        ), List.of(
                reward("ExpeditionPoint", 8)
        ));

        register(expeditions, "Fairy_pond", 50, List.of(
                drop("Plant_Crop_Mushroom_Glowing_Blue", 10),
                drop("Plant_Crop_Mushroom_Glowing_Purple", 10),
                drop("Plant_Fruit_Azure", 3),
                drop("Ingredient_Life_Essence", 3),
                drop("Wood_Azure_Trunk", 15),
                drop("Plant_Sapling_Azure", 4),
                drop("*Container_Bucket_State_Filled_Water", 1)
        ), List.of(
                reward("*Container_Bucket_State_Filled_Water", 1),
                reward("ExpeditionPoint", 4)
        ));

        EXPEDITIONS = Collections.unmodifiableMap(expeditions);
        DEFAULT_IDS = buildDefaultIds(EXPEDITIONS);
        DEFAULT_WEIGHTS = buildDefaultWeights(EXPEDITIONS);
        COMPLETION_REWARD_DROP_IDS = buildCompletionRewardDropIds(EXPEDITIONS);
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

    public static List<CompletionRewardDefinition> getCompletionRewards(String expeditionId)
    {
        ExpeditionDefinition def = EXPEDITIONS.get(expeditionId);
        return def == null ? List.of() : def.completionRewards;
    }

    public static Set<String> getCompletionRewardDropIds()
    {
        return COMPLETION_REWARD_DROP_IDS;
    }

    public static String getBlockId(String expeditionId)
    {
        ExpeditionDefinition def = EXPEDITIONS.get(expeditionId);
        return def == null ? null : def.blockId;
    }

    public static int getTicks(String expeditionId)
    {
        ExpeditionDefinition def = EXPEDITIONS.get(expeditionId);
        return def == null ? OneBlockExpeditionResolver.DEFAULT_TICKS : def.ticks;
    }

    private static DropDefinition drop(String dropId, int weight)
    {
        return new DropDefinition(dropId, Math.max(1, weight));
    }

    private static CompletionRewardDefinition reward(String dropId, int quantity)
    {
        return new CompletionRewardDefinition(dropId, quantity);
    }

    private static void register(Map<String, ExpeditionDefinition> map, String expeditionId, int ticks, List<DropDefinition> drops)
    {
        register(map, expeditionId, ticks, drops, List.of());
    }

    private static void register(Map<String, ExpeditionDefinition> map,
                                 String expeditionId,
                                 int ticks,
                                 List<DropDefinition> drops,
                                 List<CompletionRewardDefinition> completionRewards)
    {
        String blockId = "OneBlock_Block_" + expeditionId;
        map.put(expeditionId, new ExpeditionDefinition(expeditionId, blockId, ticks, drops, completionRewards));
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

    private static Set<String> buildCompletionRewardDropIds(Map<String, ExpeditionDefinition> expeditions)
    {
        Set<String> out = new HashSet<>();
        for (ExpeditionDefinition expedition : expeditions.values())
        {
            for (CompletionRewardDefinition reward : expedition.completionRewards)
            {
                if (reward == null || reward.dropId == null || reward.dropId.isEmpty()) continue;
                out.add(reward.dropId);
            }
        }
        return Collections.unmodifiableSet(out);
    }
}
