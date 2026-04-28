package com.EreliaStudio.OneBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class OneBlockDungeonDefaults
{
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

    public static final class DungeonDefinition
    {
        public final String dungeonId;
        public final String blockId;
        public final List<List<String>> waves;
        public final List<CompletionRewardDefinition> completionRewards;

        public DungeonDefinition(String dungeonId,
                                 String blockId,
                                 List<List<String>> waves,
                                 List<CompletionRewardDefinition> completionRewards)
        {
            this.dungeonId = dungeonId;
            this.blockId = blockId;
            this.waves = waves == null || waves.isEmpty()
                    ? List.of()
                    : Collections.unmodifiableList(waves.stream()
                            .map(w -> w == null ? List.<String>of() : Collections.unmodifiableList(new ArrayList<>(w)))
                            .toList());
            this.completionRewards = completionRewards == null || completionRewards.isEmpty()
                    ? List.of()
                    : Collections.unmodifiableList(new ArrayList<>(completionRewards));
        }
    }

    private static final Map<String, DungeonDefinition> DUNGEONS;
    private static final Set<String> ALL_ENTITY_IDS;
    private static final Set<String> COMPLETION_REWARD_DROP_IDS;

    static
    {
        Map<String, DungeonDefinition> dungeons = new HashMap<>();

        register(dungeons, "Gobelin_Dungeon", List.of(
                        List.of("entity:Goblin_Miner", "entity:Goblin_Miner", "entity:Goblin_Scrapper"),
                        List.of("entity:Goblin_Miner", "entity:Goblin_Thief", "entity:Goblin_Scrapper")
                ), List.of(
                reward("Ingredient_Life_Essence", 10),
                reward("Ore_Iron", 12),
                reward("Ingredient_Charcoal", 20)
        ));

        DUNGEONS = Collections.unmodifiableMap(dungeons);
        ALL_ENTITY_IDS = buildAllEntityIds(DUNGEONS);
        COMPLETION_REWARD_DROP_IDS = buildCompletionRewardDropIds(DUNGEONS);
    }

    private OneBlockDungeonDefaults() {}

    public static boolean isDungeon(String expeditionId)
    {
        return expeditionId != null && DUNGEONS.containsKey(expeditionId);
    }

    public static Set<String> getDungeonIds()
    {
        return DUNGEONS.keySet();
    }

    public static List<List<String>> getWaves(String dungeonId)
    {
        DungeonDefinition def = DUNGEONS.get(dungeonId);
        return def == null ? List.of() : def.waves;
    }

    public static List<String> getWave(String dungeonId, int waveIndex)
    {
        List<List<String>> waves = getWaves(dungeonId);
        if (waveIndex < 0 || waveIndex >= waves.size()) return List.of();
        return waves.get(waveIndex);
    }

    public static int getWaveCount(String dungeonId)
    {
        return getWaves(dungeonId).size();
    }

    public static List<CompletionRewardDefinition> getCompletionRewards(String dungeonId)
    {
        DungeonDefinition def = DUNGEONS.get(dungeonId);
        return def == null ? List.of() : def.completionRewards;
    }

    public static Set<String> getAllEntityIds()
    {
        return ALL_ENTITY_IDS;
    }

    public static Set<String> getCompletionRewardDropIds()
    {
        return COMPLETION_REWARD_DROP_IDS;
    }

    public static String getBlockId(String dungeonId)
    {
        DungeonDefinition def = DUNGEONS.get(dungeonId);
        return def == null ? null : def.blockId;
    }

    private static CompletionRewardDefinition reward(String dropId, int quantity)
    {
        return new CompletionRewardDefinition(dropId, quantity);
    }

    private static void register(Map<String, DungeonDefinition> map,
                                 String dungeonId,
                                 List<List<String>> waves,
                                 List<CompletionRewardDefinition> completionRewards)
    {
        String blockId = "OneBlock_Block_" + dungeonId;
        map.put(dungeonId, new DungeonDefinition(dungeonId, blockId, waves, completionRewards));
    }

    private static Set<String> buildAllEntityIds(Map<String, DungeonDefinition> dungeons)
    {
        Set<String> out = new HashSet<>();
        for (DungeonDefinition def : dungeons.values())
            for (List<String> wave : def.waves)
                out.addAll(wave);
        return Collections.unmodifiableSet(out);
    }

    private static Set<String> buildCompletionRewardDropIds(Map<String, DungeonDefinition> dungeons)
    {
        Set<String> out = new HashSet<>();
        for (DungeonDefinition def : dungeons.values())
            for (CompletionRewardDefinition reward : def.completionRewards)
                if (reward != null && reward.dropId != null && !reward.dropId.isEmpty())
                    out.add(reward.dropId);
        return Collections.unmodifiableSet(out);
    }
}
