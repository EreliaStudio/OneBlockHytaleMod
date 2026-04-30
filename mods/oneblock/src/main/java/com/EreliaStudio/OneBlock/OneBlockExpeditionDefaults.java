package com.EreliaStudio.OneBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
        /** Non-null when this reward is a crystal: the expedition ID whose knowledge is unlocked. */
        public final String unlockExpeditionId;

        public CompletionRewardDefinition(String dropId, int quantity)
        {
            this(dropId, quantity, null);
        }

        public CompletionRewardDefinition(String dropId, int quantity, String unlockExpeditionId)
        {
            this.dropId = dropId;
            this.quantity = Math.max(1, quantity);
            this.unlockExpeditionId = unlockExpeditionId;
        }

        public boolean isCrystalReward() { return unlockExpeditionId != null; }
    }

    public static final class RandomRewardBundle
    {
        public final List<CompletionRewardDefinition> items;
        public final int weight;

        public RandomRewardBundle(List<CompletionRewardDefinition> items, int weight)
        {
            this.items = items == null || items.isEmpty()
                    ? List.of()
                    : Collections.unmodifiableList(new ArrayList<>(items));
            this.weight = Math.max(1, weight);
        }
    }

    public static final class ExpeditionDefinition
    {
        public final String expeditionId;
        public final String blockId;
        public final int ticks;
        public final List<DropDefinition> drops;
        public final List<CompletionRewardDefinition> mandatoryRewards;
        public final List<RandomRewardBundle> randomBundles;

        public ExpeditionDefinition(String expeditionId, String blockId, int ticks, List<DropDefinition> drops)
        {
            this(expeditionId, blockId, ticks, drops, List.of(), List.of());
        }

        public ExpeditionDefinition(String expeditionId,
                                    String blockId,
                                    int ticks,
                                    List<DropDefinition> drops,
                                    List<CompletionRewardDefinition> mandatoryRewards)
        {
            this(expeditionId, blockId, ticks, drops, mandatoryRewards, List.of());
        }

        public ExpeditionDefinition(String expeditionId,
                                    String blockId,
                                    int ticks,
                                    List<DropDefinition> drops,
                                    List<CompletionRewardDefinition> mandatoryRewards,
                                    List<RandomRewardBundle> randomBundles)
        {
            this.expeditionId = expeditionId;
            this.blockId = blockId;
            this.ticks = ticks;
            this.drops = drops == null || drops.isEmpty()
                    ? List.of()
                    : Collections.unmodifiableList(new ArrayList<>(drops));
            this.mandatoryRewards = mandatoryRewards == null || mandatoryRewards.isEmpty()
                    ? List.of()
                    : Collections.unmodifiableList(new ArrayList<>(mandatoryRewards));
            this.randomBundles = randomBundles == null || randomBundles.isEmpty()
                    ? List.of()
                    : Collections.unmodifiableList(new ArrayList<>(randomBundles));
        }
    }

    private static final Map<String, ExpeditionDefinition> EXPEDITIONS;
    private static final Map<String, List<String>> DEFAULT_IDS;
    private static final Map<String, Map<String, Integer>> DEFAULT_WEIGHTS;
    private static final Set<String> COMPLETION_REWARD_DROP_IDS;
    private static final Random RANDOM = new Random();

    static
    {
        Map<String, ExpeditionDefinition> expeditions = new HashMap<>();

        register(expeditions, "Default", 25, List.of(
                drop("Ingredient_Fibre", 20),
                drop("Rubble_Stone", 20),
                drop("Soil_Dirt", 10)
        ));

        register(expeditions, "Cave_Entry", 25, List.of(
                drop("Rubble_Stone", 20),
                drop("Rock_Stone", 20)
        ), List.of(
                crystalReward("Cave", 1)
        ));

        register(expeditions, "Forest_Edge", 25, List.of(
                drop("Ingredient_Stick", 20),
                drop("Wood_Oak_Trunk", 20)
        ), List.of(
                crystalReward("Forest", 1)
        ));

        register(expeditions, "Cave", 25, List.of(
                drop("Rock_Stone", 20),
                drop("Ore_Copper", 20)
        ));

        register(expeditions, "Forest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Boar"), 10),
                drop("Wood_Oak_Trunk", 20)
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

    public static List<CompletionRewardDefinition> getMandatoryRewards(String expeditionId)
    {
        ExpeditionDefinition def = EXPEDITIONS.get(expeditionId);
        return def == null ? List.of() : def.mandatoryRewards;
    }

    public static List<RandomRewardBundle> getRandomBundles(String expeditionId)
    {
        ExpeditionDefinition def = EXPEDITIONS.get(expeditionId);
        return def == null ? List.of() : def.randomBundles;
    }

    public static RandomRewardBundle pickRandomBundle(String expeditionId)
    {
        List<RandomRewardBundle> bundles = getRandomBundles(expeditionId);
        if (bundles.isEmpty()) return null;

        int totalWeight = 0;
        for (RandomRewardBundle bundle : bundles) totalWeight += bundle.weight;
        int roll = RANDOM.nextInt(totalWeight);
        int cursor = 0;
        for (RandomRewardBundle bundle : bundles)
        {
            cursor += bundle.weight;
            if (roll < cursor) return bundle;
        }
        return bundles.get(bundles.size() - 1);
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

    private static CompletionRewardDefinition crystalReward(String expeditionId, int quantity)
    {
        return new CompletionRewardDefinition("OneBlock_Crystal_" + expeditionId, quantity, expeditionId);
    }

    private static RandomRewardBundle bundle(List<CompletionRewardDefinition> items, int weight)
    {
        return new RandomRewardBundle(items, weight);
    }

    private static void register(Map<String, ExpeditionDefinition> map, String expeditionId, int ticks, List<DropDefinition> drops)
    {
        register(map, expeditionId, ticks, drops, List.of(), List.of());
    }

    private static void register(Map<String, ExpeditionDefinition> map,
                                 String expeditionId,
                                 int ticks,
                                 List<DropDefinition> drops,
                                 List<CompletionRewardDefinition> mandatoryRewards)
    {
        register(map, expeditionId, ticks, drops, mandatoryRewards, List.of());
    }

    private static void register(Map<String, ExpeditionDefinition> map,
                                 String expeditionId,
                                 int ticks,
                                 List<DropDefinition> drops,
                                 List<CompletionRewardDefinition> mandatoryRewards,
                                 List<RandomRewardBundle> randomBundles)
    {
        String blockId = "OneBlock_Block_" + expeditionId;
        map.put(expeditionId, new ExpeditionDefinition(expeditionId, blockId, ticks, drops, mandatoryRewards, randomBundles));
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
            for (CompletionRewardDefinition reward : expedition.mandatoryRewards)
            {
                if (reward == null || reward.dropId == null || reward.dropId.isEmpty()) continue;
                out.add(reward.dropId);
            }
            for (RandomRewardBundle bundle : expedition.randomBundles)
            {
                if (bundle == null) continue;
                for (CompletionRewardDefinition reward : bundle.items)
                {
                    if (reward == null || reward.dropId == null || reward.dropId.isEmpty()) continue;
                    out.add(reward.dropId);
                }
            }
        }
        return Collections.unmodifiableSet(out);
    }

}
