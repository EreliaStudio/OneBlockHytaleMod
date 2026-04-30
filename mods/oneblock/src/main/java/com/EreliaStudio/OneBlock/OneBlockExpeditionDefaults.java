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
                drop(OneBlockDropId.entityDropId("Rabbit"), 3),
                drop(OneBlockDropId.entityDropId("Chicken"), 3),
                drop("Ingredient_Fibre", 35),
                drop("Ingredient_Stick", 30),
                drop("Rubble_Stone", 25),
                drop("Soil_Dirt", 25)
        ), List.of(
                reward("ExpeditionPoint", 1),
                crystalReward("CaveEntry", 1),
                crystalReward("ForestEdge", 1),
                crystalReward("Plain", 1)
        ));

        register(expeditions, "CaveEntry", 25, List.of(
                drop(OneBlockDropId.entityDropId("Bat"), 2),
                drop(OneBlockDropId.entityDropId("Rat"), 2),
                drop("Rubble_Stone", 35),
                drop("Rock_Stone", 30),
                drop("Ore_Copper", 4)
        ), List.of(
                reward("ExpeditionPoint", 1),
                crystalReward("Cave", 1)
        ));

        register(expeditions, "ForestEdge", 25, List.of(
                drop(OneBlockDropId.entityDropId("Rabbit"), 4),
                drop(OneBlockDropId.entityDropId("Boar"), 2),
                drop(OneBlockDropId.entityDropId("Boar_Piglet"), 3),
                drop(OneBlockDropId.entityDropId("Fox"), 1),
                drop("Ingredient_Stick", 35),
                drop("Wood_Oak_Trunk", 28),
                drop("Plant_Sapling_Oak", 8),
                drop("Plant_Fruit_Apple", 3)
        ), List.of(
                reward("ExpeditionPoint", 1),
                crystalReward("ForestEntry", 1)
        ));

        register(expeditions, "Plain", 25, List.of(
                drop(OneBlockDropId.entityDropId("Rabbit"), 4),
                drop(OneBlockDropId.entityDropId("Sheep"), 3),
                drop(OneBlockDropId.entityDropId("Chicken"), 4),
                drop(OneBlockDropId.entityDropId("Antelope"), 1),
                drop("Plant_Crop_Carrot_Item", 16),
                drop("Plant_Crop_Corn_Item", 16),
                drop("Plant_Crop_Wheat_Item", 24),
                drop("Ingredient_Fibre", 30)
        ), List.of(
                reward("ExpeditionPoint", 1),
                crystalReward("Quarry", 1),
                crystalReward("Hallow", 1)
        ));

        register(expeditions, "Cave", 25, List.of(
                drop(OneBlockDropId.entityDropId("Rat"), 3),
                drop(OneBlockDropId.entityDropId("Bat"), 3),
                drop(OneBlockDropId.entityDropId("Spider"), 1),
                drop("Rock_Stone", 35),
                drop("Rubble_Stone", 30),
                drop("Ore_Copper", 5)
        ), List.of(
                reward("ExpeditionPoint", 1),
                crystalReward("RatCave", 1),
                crystalReward("LowerCave", 1)
        ));

        register(expeditions, "LowerCave", 28, List.of(
                drop(OneBlockDropId.entityDropId("Rat"), 3),
                drop(OneBlockDropId.entityDropId("Bat"), 3),
                drop(OneBlockDropId.entityDropId("Spider"), 2),
                drop(OneBlockDropId.entityDropId("Cave_Spider"), 1),
                drop("Rock_Stone", 35),
                drop("Rubble_Stone", 35),
                drop("Ore_Copper", 7)
        ), List.of(
                reward("ExpeditionPoint", 2),
                crystalReward("CopperCave", 1)
        ));

        register(expeditions, "CopperCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Bat"), 2),
                drop(OneBlockDropId.entityDropId("Rat"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 1),
                drop("Rock_Stone", 30),
                drop("Rubble_Stone", 25),
                drop("Ore_Copper", 20),
                drop("Ore_Iron", 3)
        ), List.of(
                reward("ExpeditionPoint", 2),
                crystalReward("GobelinGank", 1)
        ));

        register(expeditions, "IronCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Goblin_Scrapper"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 3),
                drop(OneBlockDropId.entityDropId("Goblin_Scavenger"), 1),
                drop("Rock_Basalt", 24),
                drop("Rubble_Basalt", 24),
                drop("Ore_Iron", 22),
                drop("Ore_Thorium", 3)
        ), List.of(
                reward("ExpeditionPoint", 3),
                crystalReward("GoldCave", 1),
                crystalReward("ThoriumCave", 1)
        ));

        register(expeditions, "SandCave", 28, List.of(
                drop(OneBlockDropId.entityDropId("Bat"), 2),
                drop(OneBlockDropId.entityDropId("Spider"), 2),
                drop(OneBlockDropId.entityDropId("Cactee"), 1),
                drop("Block_Sand", 25),
                drop("Rock_Sandstone", 28),
                drop("Rubble_Sandstone", 24),
                drop("Ore_Gold", 3)
        ), List.of(
                reward("ExpeditionPoint", 2),
                crystalReward("SandCavern", 1)
        ));

        register(expeditions, "GoldCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Goblin_Scrapper"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Thief"), 1),
                drop("Rock_Basalt", 24),
                drop("Rubble_Basalt", 24),
                drop("Ore_Gold", 16),
                drop("Rock_Gem_Topaz", 3)
        ), List.of(
                reward("ExpeditionPoint", 3)
        ));

        register(expeditions, "ThoriumCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Goblin_Scrapper"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Lobber"), 1),
                drop("Rock_Basalt", 24),
                drop("Rubble_Basalt", 24),
                drop("Ore_Thorium", 18),
                drop("Ore_Cobalt", 3)
        ), List.of(
                reward("ExpeditionPoint", 4),
                crystalReward("GobelinInvasion", 1),
                crystalReward("SilverCave", 1)
        ));

        register(expeditions, "SilverCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 2),
                drop(OneBlockDropId.entityDropId("Cave_Spider"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Thief"), 1),
                drop("Rock_Shale", 26),
                drop("Rubble_Shale", 24),
                drop("Ore_Silver", 18),
                drop("Rock_Gem_Sapphire", 3)
        ), List.of(
                reward("ExpeditionPoint", 4)
        ));

        register(expeditions, "CobaltCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Goblin_Scrapper"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Lobber"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Ogre"), 1),
                drop("Rock_Shale", 24),
                drop("Rubble_Shale", 22),
                drop("Ore_Cobalt", 18),
                drop("Ore_Adamantite", 3)
        ), List.of(
                reward("ExpeditionPoint", 5),
                crystalReward("AdamantiteCave", 1),
                crystalReward("GemCave", 1),
                crystalReward("FireCave", 1)
        ));

        register(expeditions, "AdamantiteCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Goblin_Scrapper"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Lobber"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Ogre"), 1),
                drop("Rubble_Shale", 22),
                drop("Rock_Shale", 22),
                drop("Ore_Adamantite", 18),
                drop("Ore_Mithril", 3)
        ), List.of(
                reward("ExpeditionPoint", 6),
                crystalReward("GemDeepCave", 1),
                crystalReward("MithrilCave", 1)
        ));

        register(expeditions, "GemCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Cave_Spider"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Thief"), 1),
                drop(OneBlockDropId.entityDropId("Earth_Elemental"), 1),
                drop("Rubble_Shale", 22),
                drop("Rock_Shale", 22),
                drop("Rock_Gem_Emerald", 12),
                drop("Rock_Gem_Topaz", 10),
                drop("Rock_Gem_Sapphire", 4)
        ), List.of(
                reward("ExpeditionPoint", 5)
        ));

        register(expeditions, "FireCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Zombie_Burnt"), 2),
                drop(OneBlockDropId.entityDropId("Emberwulf"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Soldier"), 1),
                drop("Rock_Volcanic", 26),
                drop("Rubble_Volcanic", 24),
                drop("Rubble_Volcanic_Medium", 14),
                drop("Ingredient_Fire_Essence", 8),
                drop("Ore_Thorium", 10),
                drop("Ore_Cobalt", 3)
        ), List.of(
                reward("ExpeditionPoint", 6),
                crystalReward("HellGate", 1)
        ));

        register(expeditions, "GemDeepCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Cave_Spider"), 2),
                drop(OneBlockDropId.entityDropId("Earth_Elemental"), 2),
                drop(OneBlockDropId.entityDropId("Earthen_Golem"), 1),
                drop("Rock_Shale", 20),
                drop("Rubble_Shale", 20),
                drop("Rock_Gem_Emerald", 12),
                drop("Rock_Gem_Topaz", 10),
                drop("Rock_Gem_Sapphire", 8),
                drop("Rock_Gem_Diamond", 3)
        ), List.of(
                reward("ExpeditionPoint", 7)
        ));

        register(expeditions, "MithrilCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Goblin_Scrapper"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Ogre"), 1),
                drop("Rock_Ledge_Brick", 28),
                drop("Ore_Mithril", 18),
                drop("Ore_Onyxium", 3)
        ), List.of(
                reward("ExpeditionPoint", 7),
                crystalReward("OnyxiumCave", 1)
        ));

        register(expeditions, "OnyxiumCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Goblin_Scrapper"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 2),
                drop(OneBlockDropId.entityDropId("Void_Crawler"), 1),
                drop("Rock_Ledge_Brick", 26),
                drop("Ore_Onyxium", 18),
                drop("Ore_Prisma", 3)
        ), List.of(
                reward("ExpeditionPoint", 8),
                crystalReward("PrismaCave", 1)
        ));

        register(expeditions, "PrismaCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Goblin_Scrapper"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 2),
                drop(OneBlockDropId.entityDropId("Void_Crawler"), 2),
                drop(OneBlockDropId.entityDropId("Void_Eye"), 1),
                drop("Rock_Volcanic", 20),
                drop("Rubble_Volcanic", 20),
                drop("Ore_Prisma", 16),
                drop("Rock_Gem_Zephyr", 4)
        ), List.of(
                reward("ExpeditionPoint", 10)
        ));

        register(expeditions, "SandCavern", 30, List.of(
                drop(OneBlockDropId.entityDropId("Bat"), 2),
                drop(OneBlockDropId.entityDropId("Cactee"), 2),
                drop(OneBlockDropId.entityDropId("Sandswept_Zombie"), 1),
                drop("Rubble_Stone", 22),
                drop("Rock_Stone", 22),
                drop("Rock_Sandstone", 26),
                drop("Rubble_Sandstone", 24),
                drop("Ore_Gold", 3)
        ), List.of(
                reward("ExpeditionPoint", 3),
                crystalReward("Desert", 1)
        ));

        register(expeditions, "Desert", 25, List.of(
                drop(OneBlockDropId.entityDropId("Cactee"), 2),
                drop(OneBlockDropId.entityDropId("Camel"), 3),
                drop(OneBlockDropId.entityDropId("Camel_Calf"), 2),
                drop(OneBlockDropId.entityDropId("Sandswept_Zombie"), 1),
                drop("Block_Sand", 32),
                drop("Rock_Sandstone", 24),
                drop("Rubble_Sandstone", 22),
                drop("Plant_Cactus_1", 16),
                drop("Plant_Cactus_Ball_1", 10),
                drop("Plant_Cactus_Flat_1", 10),
                drop("Ore_Gold", 3)
        ), List.of(
                reward("ExpeditionPoint", 3),
                crystalReward("DryJunglePass", 1),
                crystalReward("DesertTempleEntrace", 1),
                crystalReward("InnerDesert", 1),
                crystalReward("DryTrorkCamp", 1)
        ));

        register(expeditions, "ForestEntry", 25, List.of(
                drop(OneBlockDropId.entityDropId("Boar"), 3),
                drop(OneBlockDropId.entityDropId("Boar_Piglet"), 4),
                drop(OneBlockDropId.entityDropId("Pigeon"), 4),
                drop(OneBlockDropId.entityDropId("Fox"), 2),
                drop(OneBlockDropId.entityDropId("Wolf"), 1),
                drop("Ingredient_Stick", 28),
                drop("Wood_Oak_Trunk", 24),
                drop("Wood_Beech_Trunk", 16),
                drop("Wood_Ash_Trunk", 10),
                drop("Plant_Sapling_Oak", 8),
                drop("Plant_Sapling_Beech", 6),
                drop("Plant_Sapling_Ash", 5),
                drop("Plant_Crop_Mushroom_Cap_Brown", 8),
                drop("Plant_Crop_Mushroom_Common_Brown", 8),
                drop("Plant_Crop_Mushroom_Shelve_Brown", 5)
        ), List.of(
                reward("ExpeditionPoint", 1),
                crystalReward("Pond", 1),
                crystalReward("Forest", 1)
        ));

        register(expeditions, "Pond", 25, List.of(
                drop(OneBlockDropId.entityDropId("Frog"), 4),
                drop(OneBlockDropId.entityDropId("Duck"), 4),
                drop(OneBlockDropId.entityDropId("Bluegill"), 4),
                drop("Fish_Bluegill_Item", 20),
                drop("Plant_Moss_Block_Green", 18),
                drop("Plant_Moss_Rug_Green", 18),
                drop("Soil_Clay_Blue", 14),
                drop("Soil_Dirt", 18)
        ), List.of(
                reward("ExpeditionPoint", 2),
                crystalReward("River", 1)
        ));

        register(expeditions, "River", 25, List.of(
                drop(OneBlockDropId.entityDropId("Frog"), 3),
                drop(OneBlockDropId.entityDropId("Duck"), 3),
                drop(OneBlockDropId.entityDropId("Bluegill"), 4),
                drop(OneBlockDropId.entityDropId("Crab"), 1),
                drop("Fish_Bluegill_Item", 20),
                drop("Rubble_Stone", 18),
                drop("Rock_Stone", 18),
                drop("Soil_Clay_Blue", 14),
                drop("Plant_Moss_Green", 16)
        ), List.of(
                reward("ExpeditionPoint", 2),
                crystalReward("Lake", 1),
                crystalReward("Sea", 1),
                crystalReward("Costline", 1)
        ));

        register(expeditions, "Lake", 28, List.of(
                drop(OneBlockDropId.entityDropId("Frog"), 3),
                drop(OneBlockDropId.entityDropId("Duck"), 3),
                drop(OneBlockDropId.entityDropId("Bluegill"), 4),
                drop(OneBlockDropId.entityDropId("Crocodile"), 1),
                drop("Fish_Bluegill_Item", 22),
                drop("Plant_Moss_Block_Green", 18),
                drop("Plant_Moss_Rug_Green", 18),
                drop("Soil_Clay_Blue", 16),
                drop("Rock_Gem_Sapphire", 3)
        ), List.of(
                reward("ExpeditionPoint", 2)
        ));

        register(expeditions, "Sea", 30, List.of(
                drop(OneBlockDropId.entityDropId("Crab"), 4),
                drop(OneBlockDropId.entityDropId("Bluegill"), 3),
                drop(OneBlockDropId.entityDropId("Skeleton_Pirate_Striker"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Pirate_Gunner"), 1),
                drop("Fish_Whale_Humpback_Item", 2),
                drop("Rock_Aqua_Brick", 20),
                drop("Rock_Aqua_Brick_Smooth", 18),
                drop("Plant_Coral_Block_Yellow", 14),
                drop("Plant_Coral_Bush_Yellow", 14),
                drop("Plant_Coral_Model_Yellow", 10),
                drop("Rock_Gem_Sapphire", 3)
        ), List.of(
                reward("ExpeditionPoint", 3),
                crystalReward("PirateShipwreck", 1)
        ));

        register(expeditions, "Costline", 25, List.of(
                drop(OneBlockDropId.entityDropId("Crab"), 4),
                drop(OneBlockDropId.entityDropId("Duck"), 3),
                drop(OneBlockDropId.entityDropId("Cactee"), 1),
                drop("Block_Sand", 26),
                drop("Rock_Sandstone", 24),
                drop("Rubble_Sandstone", 22),
                drop("Plant_Coral_Bush_Yellow", 12),
                drop("Plant_Coral_Model_Yellow", 10),
                drop("Rock_Gem_Sapphire", 3)
        ), List.of(
                reward("ExpeditionPoint", 3),
                crystalReward("SeaCavern", 1)
        ));

        register(expeditions, "SeaCavern", 28, List.of(
                drop(OneBlockDropId.entityDropId("Crab"), 3),
                drop(OneBlockDropId.entityDropId("Cave_Spider"), 2),
                drop(OneBlockDropId.entityDropId("Scarak_Louse"), 1),
                drop("Rock_Aqua_Brick", 22),
                drop("Rock_Aqua_Brick_Smooth", 20),
                drop("Rock_Aqua_Brick_Ornate", 10),
                drop("Rock_Gem_Sapphire", 8),
                drop("Rock_Gem_Diamond", 3)
        ), List.of(
                reward("ExpeditionPoint", 4),
                crystalReward("SeaInfestedNest", 1)
        ));

        register(expeditions, "SeaInfestedNest", 28, List.of(
                drop(OneBlockDropId.entityDropId("Scarak_Louse"), 3),
                drop(OneBlockDropId.entityDropId("Scarak_Fighter"), 2),
                drop(OneBlockDropId.entityDropId("Scarak_Seeker"), 1),
                drop("Soil_Hive", 24),
                drop("Soil_Hive_Brick", 22),
                drop("Soil_Hive_Brick_Smooth", 16),
                drop("Ingredient_Hide_Scaled", 10),
                drop("Ingredient_Leather_Scaled", 6),
                drop("Rock_Gem_Sapphire", 6),
                drop("Rock_Gem_Diamond", 2)
        ), List.of(
                reward("ExpeditionPoint", 6),
                crystalReward("SeaMonster", 1)
        ));

        register(expeditions, "Forest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Boar"), 3),
                drop(OneBlockDropId.entityDropId("Boar_Piglet"), 4),
                drop(OneBlockDropId.entityDropId("Fox"), 2),
                drop(OneBlockDropId.entityDropId("Pigeon"), 4),
                drop(OneBlockDropId.entityDropId("Deer"), 2),
                drop(OneBlockDropId.entityDropId("Wolf"), 1),
                drop("Ingredient_Stick", 26),
                drop("Wood_Oak_Trunk", 22),
                drop("Wood_Beech_Trunk", 18),
                drop("Wood_Ash_Trunk", 12),
                drop("Plant_Sapling_Oak", 8),
                drop("Plant_Sapling_Beech", 6),
                drop("Plant_Sapling_Ash", 5),
                drop("Plant_Fruit_Apple", 5)
        ), List.of(
                reward("ExpeditionPoint", 2),
                crystalReward("AridForest", 1),
                crystalReward("Swamp", 1),
                crystalReward("DeepForest", 1),
                crystalReward("JungleEdge", 1)
        ));

        register(expeditions, "AridForest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Fox"), 2),
                drop(OneBlockDropId.entityDropId("Armadillo"), 3),
                drop(OneBlockDropId.entityDropId("Cactee"), 1),
                drop("Wood_Ash_Trunk", 24),
                drop("Wood_Bottletree_Trunk", 14),
                drop("Wood_Bottletree_Roots", 10),
                drop("Plant_Leaves_Bottle", 12),
                drop("Plant_Seeds_Bottletree", 8),
                drop("Rock_Sandstone", 22),
                drop("Rubble_Sandstone", 20)
        ), List.of(
                reward("ExpeditionPoint", 2),
                crystalReward("Desert", 1)
        ));

        register(expeditions, "Swamp", 25, List.of(
                drop(OneBlockDropId.entityDropId("Frog"), 4),
                drop(OneBlockDropId.entityDropId("Crocodile"), 2),
                drop(OneBlockDropId.entityDropId("Fen_Stalker"), 1),
                drop("Soil_Dirt", 22),
                drop("Soil_Clay_Blue", 20),
                drop("Plant_Moss_Block_Green", 18),
                drop("Plant_Moss_Rug_Green", 18),
                drop("Plant_Crop_Mushroom_Common_Brown", 12),
                drop("Plant_Crop_Mushroom_Shelve_Brown", 8),
                drop("Plant_Crop_Mushroom_Block_Yellow", 4)
        ), List.of(
                reward("ExpeditionPoint", 2),
                crystalReward("EnchantedForest", 1)
        ));

        register(expeditions, "EnchantedForest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Deer"), 3),
                drop(OneBlockDropId.entityDropId("Bluebird"), 4),
                drop(OneBlockDropId.entityDropId("Earth_Elemental"), 1),
                drop("Wood_Amber_Trunk", 18),
                drop("Wood_Amber_Roots", 12),
                drop("Plant_Leaves_Amber", 14),
                drop("Plant_Leaves_Goldentree", 12),
                drop("Plant_Flower_Common_Yellow", 14),
                drop("Plant_Flower_Orchid_Yellow", 8),
                drop("Rock_Gem_Emerald", 8),
                drop("Rock_Gem_Topaz", 3)
        ), List.of(
                reward("ExpeditionPoint", 4),
                crystalReward("FairyPond", 1)
        ));

        register(expeditions, "FairyPond", 25, List.of(
                drop(OneBlockDropId.entityDropId("Frog"), 3),
                drop(OneBlockDropId.entityDropId("Duck"), 3),
                drop(OneBlockDropId.entityDropId("Bluebird"), 4),
                drop(OneBlockDropId.entityDropId("Earth_Elemental"), 1),
                drop("Plant_Flower_Common_Pink2", 14),
                drop("Plant_Flower_Common_Yellow", 14),
                drop("Plant_Flower_Orchid_Yellow", 8),
                drop("Plant_Petals_Yellow", 12),
                drop("Plant_Moss_Yellow", 16),
                drop("Plant_Moss_Block_Yellow", 14),
                drop("Rock_Gem_Topaz", 6)
        ), List.of(
                reward("ExpeditionPoint", 5)
        ));

        register(expeditions, "DeepForest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Boar"), 3),
                drop(OneBlockDropId.entityDropId("Fox"), 2),
                drop(OneBlockDropId.entityDropId("Wolf"), 2),
                drop(OneBlockDropId.entityDropId("Crawler"), 1),
                drop("Wood_Beech_Trunk", 22),
                drop("Wood_Ash_Trunk", 18),
                drop("Wood_Maple_Trunk", 14),
                drop("Plant_Sapling_Beech", 8),
                drop("Plant_Sapling_Ash", 7),
                drop("Plant_Crop_Mushroom_Block_Yellow", 8),
                drop("Plant_Crop_Mushroom_Block_Yellow_Trunk", 5),
                drop("Rock_Gem_Emerald", 3)
        ), List.of(
                reward("ExpeditionPoint", 3),
                crystalReward("DarkForest", 1),
                crystalReward("JungleEdge", 1),
                crystalReward("TrorkHuntingGround", 1)
        ));

        register(expeditions, "DarkForest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Wolf"), 2),
                drop(OneBlockDropId.entityDropId("Crawler"), 2),
                drop(OneBlockDropId.entityDropId("Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Aberrant_Zombie"), 1),
                drop("Plant_Leaves_Bramble", 20),
                drop("Plant_Crop_Mushroom_Block_Yellow", 14),
                drop("Plant_Crop_Mushroom_Block_Yellow_Mycelium", 10),
                drop("Plant_Crop_Mushroom_Shelve_Yellow", 8),
                drop("Ingredient_Bone_Fragment", 10),
                drop("Rock_Gem_Ruby", 3)
        ), List.of(
                reward("ExpeditionPoint", 5),
                crystalReward("BurnedForest", 1),
                crystalReward("CursedForest", 1)
        ));

        register(expeditions, "BurnedForest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Zombie_Burnt"), 2),
                drop(OneBlockDropId.entityDropId("Emberwulf"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Archer"), 1),
                drop("Wood_Ash_Trunk", 20),
                drop("Ingredient_Fire_Essence", 8),
                drop("Rock_Volcanic", 22),
                drop("Rubble_Volcanic", 20),
                drop("Ingredient_Powder_Boom", 6),
                drop("Ore_Thorium", 3)
        ), List.of(
                reward("ExpeditionPoint", 5),
                crystalReward("FireLand", 1)
        ));

        register(expeditions, "CursedForest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Zombie"), 3),
                drop(OneBlockDropId.entityDropId("Aberrant_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Scout"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Archer"), 1),
                drop("Ingredient_Bone_Fragment", 20),
                drop("Deco_Bone_Full", 10),
                drop("Deco_Bone_Spike", 10),
                drop("Deco_Bone_Spine", 6),
                drop("Plant_Leaves_Bramble", 16),
                drop("Rock_Runic_Brick", 10),
                drop("Rock_Gem_Zephyr", 3)
        ), List.of(
                reward("ExpeditionPoint", 6),
                crystalReward("OutlanderForest", 1),
                crystalReward("Graveyard", 1)
        ));

        register(expeditions, "Graveyard", 25, List.of(
                drop(OneBlockDropId.entityDropId("Zombie"), 3),
                drop(OneBlockDropId.entityDropId("Skeleton_Scout"), 3),
                drop(OneBlockDropId.entityDropId("Skeleton_Archer"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Soldier"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Mage"), 1),
                drop("Ingredient_Bone_Fragment", 24),
                drop("Deco_Bone_Full", 12),
                drop("Deco_Bone_Spike", 10),
                drop("Furniture_Village_Tombstone", 8),
                drop("Furniture_Ancient_Coffin", 4),
                drop("Weapon_Club_Zombie_Arm", 2),
                drop("Weapon_Club_Zombie_Leg", 2)
        ), List.of(
                reward("ExpeditionPoint", 7),
                crystalReward("UndeadTemple", 1)
        ));

        register(expeditions, "VoidPortal", 25, List.of(
                drop(OneBlockDropId.entityDropId("Void_Larva"), 3),
                drop(OneBlockDropId.entityDropId("Void_Crawler"), 2),
                drop(OneBlockDropId.entityDropId("Void_Eye"), 1),
                drop("Portal_Device", 5),
                drop("Instance_Gateway", 4),
                drop("Rock_Runic_Brick", 20),
                drop("Rock_Runic_Brick_Ornate", 12),
                drop("Rock_Gem_Zephyr", 8),
                drop("Ore_Onyxium", 3)
        ), List.of(
                reward("ExpeditionPoint", 8),
                crystalReward("VoidTemple", 1),
                crystalReward("SpiritThreshold", 1)
        ));

        register(expeditions, "OutlanderForest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Outlander_Initiate"), 3),
                drop(OneBlockDropId.entityDropId("Outlander_Hunter"), 2),
                drop(OneBlockDropId.entityDropId("Outlander_Marauder"), 1),
                drop("Wood_Amber_Trunk", 18),
                drop("Wood_Amber_Roots", 12),
                drop("Plant_Leaves_Amber", 14),
                drop("Plant_Leaves_Bramble", 12),
                drop("Ingredient_Hide_Prismic", 4),
                drop("Rock_Gem_Zephyr", 3)
        ), List.of(
                reward("ExpeditionPoint", 7),
                crystalReward("OutlanderGank", 1),
                crystalReward("OutlanderPlain", 1)
        ));

        register(expeditions, "OutlanderPlain", 25, List.of(
                drop(OneBlockDropId.entityDropId("Outlander_Initiate"), 3),
                drop(OneBlockDropId.entityDropId("Outlander_Hunter"), 3),
                drop(OneBlockDropId.entityDropId("Outlander_Berserker"), 2),
                drop(OneBlockDropId.entityDropId("Outlander_Marauder"), 2),
                drop(OneBlockDropId.entityDropId("Outlander_Priest"), 1),
                drop(OneBlockDropId.entityDropId("Outlander_Brute"), 1),
                drop("Plant_Crop_Wheat_Item", 20),
                drop("Plant_Crop_Corn_Item", 18),
                drop("Plant_Crop_Carrot_Item", 16),
                drop("Ingredient_Hide_Prismic", 8),
                drop("Ingredient_Leather_Prismic", 6),
                drop("Ore_Adamantite", 3)
        ), List.of(
                reward("ExpeditionPoint", 7),
                crystalReward("OutlanderCity", 1),
                crystalReward("Toundra", 1)
        ));

        register(expeditions, "Toundra", 25, List.of(
                drop(OneBlockDropId.entityDropId("Wolf"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Scout"), 1),
                drop("Rock_Ice", 24),
                drop("Rubble_Ice", 24),
                drop("Rubble_Ice_Medium", 14),
                drop("Wood_Ice_Trunk", 14),
                drop("Plant_Seeds_Ice", 8),
                drop("Ingredient_Ice_Essence", 3)
        ), List.of(
                reward("ExpeditionPoint", 8),
                crystalReward("FrozenForest", 1),
                crystalReward("IceLand", 1)
        ));

        register(expeditions, "FrozenForest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Wolf"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Ranger"), 1),
                drop("Wood_Birch_Trunk", 18),
                drop("Plant_Sapling_Birch", 8),
                drop("Plant_Leaves_Birch", 14),
                drop("Wood_Ice_Trunk", 16),
                drop("Rock_Ice", 20),
                drop("Rubble_Ice", 18),
                drop("Ingredient_Ice_Essence", 3)
        ), List.of(
                reward("ExpeditionPoint", 8),
                crystalReward("IcyForest", 1)
        ));

        register(expeditions, "IceLand", 25, List.of(
                drop(OneBlockDropId.entityDropId("Frost_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Scout"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Golem"), 1),
                drop("Rock_Ice", 26),
                drop("Rubble_Ice", 24),
                drop("Rubble_Ice_Medium", 16),
                drop("Rock_Ice_Icicles", 10),
                drop("Wood_Ice_Trunk", 10),
                drop("Ingredient_Ice_Essence", 5)
        ), List.of(
                reward("ExpeditionPoint", 8),
                crystalReward("IcyCavern", 1)
        ));

        register(expeditions, "IcyCavern", 28, List.of(
                drop(OneBlockDropId.entityDropId("Frost_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Fighter"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Mage"), 1),
                drop("Rock_Ice", 22),
                drop("Rubble_Ice", 22),
                drop("Rock_Ice_Icicles", 12),
                drop("Ore_Silver", 12),
                drop("Rock_Gem_Sapphire", 6),
                drop("Ingredient_Ice_Essence", 4)
        ), List.of(
                reward("ExpeditionPoint", 9),
                crystalReward("YetiCavern", 1),
                crystalReward("FrozenGraveyard", 1)
        ));

        register(expeditions, "YetiCavern", 28, List.of(
                drop(OneBlockDropId.entityDropId("Frost_Golem"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Knight"), 2),
                drop(OneBlockDropId.entityDropId("Ice_Dragon"), 1),
                drop("Rock_Ice", 22),
                drop("Rubble_Ice_Medium", 18),
                drop("Rock_Ice_Icicles", 12),
                drop("Ingredient_Ice_Essence", 8),
                drop("Rock_Gem_Diamond", 3),
                drop("Weapon_Staff_Crystal_Ice", 1)
        ), List.of(
                reward("ExpeditionPoint", 10),
                crystalReward("IceTemple", 1)
        ));

        register(expeditions, "IcyForest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Wolf"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Ranger"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Golem"), 1),
                drop("Wood_Ice_Trunk", 18),
                drop("Plant_Seeds_Ice", 8),
                drop("Plant_Leaves_Birch", 12),
                drop("Wood_Birch_Trunk", 16),
                drop("Rock_Ice", 20),
                drop("Ingredient_Ice_Essence", 6)
        ), List.of(
                reward("ExpeditionPoint", 9),
                crystalReward("SpiritThreshold", 1)
        ));

        register(expeditions, "FireLand", 25, List.of(
                drop(OneBlockDropId.entityDropId("Zombie_Burnt"), 2),
                drop(OneBlockDropId.entityDropId("Emberwulf"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Soldier"), 1),
                drop("Rock_Volcanic", 26),
                drop("Rubble_Volcanic", 24),
                drop("Rubble_Volcanic_Medium", 16),
                drop("Ingredient_Fire_Essence", 8),
                drop("Ore_Thorium", 8),
                drop("Ore_Cobalt", 3)
        ), List.of(
                reward("ExpeditionPoint", 7),
                crystalReward("Volcano", 1),
                crystalReward("FieryGraveward", 1)
        ));

        register(expeditions, "FieryGraveward", 25, List.of(
                drop(OneBlockDropId.entityDropId("Zombie_Burnt"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Soldier"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Archer"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Knight"), 1),
                drop("Ingredient_Bone_Fragment", 18),
                drop("Deco_Bone_Full", 10),
                drop("Deco_Bone_Spike", 10),
                drop("Rock_Volcanic", 22),
                drop("Ingredient_Fire_Essence", 8),
                drop("Rock_Gem_Ruby", 3)
        ), List.of(
                reward("ExpeditionPoint", 8),
                crystalReward("FireCave", 1),
                crystalReward("BurntBattlefield", 1)
        ));

        register(expeditions, "HellGate", 25, List.of(
                drop(OneBlockDropId.entityDropId("Incandescent_Skeleton_Fighter"), 2),
                drop(OneBlockDropId.entityDropId("Incandescent_Skeleton_Mage"), 1),
                drop(OneBlockDropId.entityDropId("Ember_Golem"), 1),
                drop("Portal_Device", 5),
                drop("Rock_Volcanic_Cracked_Incandescent", 18),
                drop("Rock_Volcanic_Cracked_Lava_Incandescent", 16),
                drop("Ingredient_Fire_Essence", 8),
                drop("Ore_Mithril", 3),
                drop("Ore_Onyxium", 2)
        ), List.of(
                reward("ExpeditionPoint", 9),
                crystalReward("HellPlain", 1),
                crystalReward("HellSwamp", 1)
        ));

        register(expeditions, "HellPlain", 25, List.of(
                drop(OneBlockDropId.entityDropId("Incandescent_Skeleton_Fighter"), 2),
                drop(OneBlockDropId.entityDropId("Incandescent_Skeleton_Footman"), 2),
                drop(OneBlockDropId.entityDropId("Emberwulf"), 2),
                drop(OneBlockDropId.entityDropId("Firesteel_Golem"), 1),
                drop("Rock_Volcanic", 22),
                drop("Rubble_Volcanic", 22),
                drop("Ingredient_Fire_Essence", 8),
                drop("Ingredient_Bolt_Cindercloth", 6),
                drop("Ore_Mithril", 8),
                drop("Ore_Onyxium", 3)
        ), List.of(
                reward("ExpeditionPoint", 10),
                crystalReward("FireGemCave", 1)
        ));

        register(expeditions, "HellSwamp", 25, List.of(
                drop(OneBlockDropId.entityDropId("Fen_Stalker"), 2),
                drop(OneBlockDropId.entityDropId("Zombie_Burnt"), 2),
                drop(OneBlockDropId.entityDropId("Incandescent_Skeleton_Mage"), 1),
                drop(OneBlockDropId.entityDropId("Ember_Golem"), 1),
                drop("Soil_Clay_Black", 18),
                drop("Plant_Moss_Yellow", 16),
                drop("Plant_Moss_Block_Yellow", 14),
                drop("Plant_Crop_Mushroom_Block_Yellow", 10),
                drop("Ingredient_Fire_Essence", 8),
                drop("Rock_Gem_Ruby", 6),
                drop("Ore_Onyxium", 3)
        ), List.of(
                reward("ExpeditionPoint", 10)
        ));

        register(expeditions, "FireGemCave", 28, List.of(
                drop(OneBlockDropId.entityDropId("Incandescent_Skeleton_Fighter"), 2),
                drop(OneBlockDropId.entityDropId("Incandescent_Skeleton_Mage"), 1),
                drop(OneBlockDropId.entityDropId("Firesteel_Golem"), 1),
                drop(OneBlockDropId.entityDropId("Thunder_Golem"), 1),
                drop("Rock_Volcanic", 20),
                drop("Rock_Volcanic_Cracked_Incandescent", 14),
                drop("Rock_Gem_Ruby", 10),
                drop("Rock_Gem_Diamond", 5),
                drop("Ore_Onyxium", 8),
                drop("Ore_Prisma", 3)
        ), List.of(
                reward("ExpeditionPoint", 11),
                crystalReward("ElementalConfluence", 1)
        ));

        register(expeditions, "DesertTempleEntrace", 25, List.of(
                drop(OneBlockDropId.entityDropId("Sandswept_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Scout"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Archer"), 1),
                drop("Rock_Sandstone", 24),
                drop("Rubble_Sandstone", 22),
                drop("Furniture_Ancient_Pot", 10),
                drop("Furniture_Ancient_Crate", 8),
                drop("Deco_Pot_Clay_Broken", 10),
                drop("Ore_Gold", 3)
        ), List.of(
                reward("ExpeditionPoint", 4),
                crystalReward("DeeperDesertTemple", 1)
        ));

        register(expeditions, "DeeperDesertTemple", 25, List.of(
                drop(OneBlockDropId.entityDropId("Sandswept_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Soldier"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Archer"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Mage"), 1),
                drop("Rock_Sandstone", 22),
                drop("Rock_Sandstone_Brick", 18),
                drop("Furniture_Ancient_Pot", 8),
                drop("Furniture_Ancient_Statue", 6),
                drop("Furniture_Ancient_Candle", 8),
                drop("Rock_Gem_Topaz", 5),
                drop("Ore_Gold", 8)
        ), List.of(
                reward("ExpeditionPoint", 5),
                crystalReward("DesertTemple", 1)
        ));

        register(expeditions, "PharaonRoom", 25, List.of(
                drop(OneBlockDropId.entityDropId("Sandswept_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Knight"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Mage"), 1),
                drop(OneBlockDropId.entityDropId("Skeleton_Sand_Archmage"), 1),
                drop("Deco_Treasure", 6),
                drop("Furniture_Ancient_Coffin", 8),
                drop("Furniture_Ancient_Statue", 8),
                drop("Furniture_Ancient_Chest_Large_Treasure", 4),
                drop("Ore_Gold", 12),
                drop("Rock_Gem_Diamond", 3)
        ), List.of(
                reward("ExpeditionPoint", 7)
        ));

        register(expeditions, "InnerDesert", 25, List.of(
                drop(OneBlockDropId.entityDropId("Cactee"), 2),
                drop(OneBlockDropId.entityDropId("Camel"), 3),
                drop(OneBlockDropId.entityDropId("Sandswept_Zombie"), 1),
                drop(OneBlockDropId.entityDropId("Scarak_Louse"), 1),
                drop("Block_Sand", 30),
                drop("Rock_Sandstone", 24),
                drop("Rubble_Sandstone", 20),
                drop("Plant_Cactus_1", 14),
                drop("Plant_Cactus_Ball_1", 10),
                drop("Plant_Cactus_Flat_1", 10),
                drop("Ore_Gold", 3)
        ), List.of(
                reward("ExpeditionPoint", 4),
                crystalReward("DryJunglePass", 1),
                crystalReward("InsectInvasion", 1)
        ));

        register(expeditions, "MuddyDesert", 25, List.of(
                drop(OneBlockDropId.entityDropId("Cactee"), 2),
                drop(OneBlockDropId.entityDropId("Crocodile"), 2),
                drop(OneBlockDropId.entityDropId("Scarak_Louse"), 1),
                drop("Block_Sand", 22),
                drop("Soil_Dirt", 22),
                drop("Soil_Clay_Black", 18),
                drop("Soil_Gravel_Sand_Half", 16),
                drop("Plant_Cactus_1", 12),
                drop("Plant_Crop_Tomato_Item", 8)
        ), List.of(
                reward("ExpeditionPoint", 4),
                crystalReward("InfestedDesert", 1)
        ));

        register(expeditions, "InfestedDesert", 25, List.of(
                drop(OneBlockDropId.entityDropId("Scarak_Louse"), 3),
                drop(OneBlockDropId.entityDropId("Scarak_Fighter"), 2),
                drop(OneBlockDropId.entityDropId("Scarak_Seeker"), 1),
                drop("Soil_Hive", 24),
                drop("Soil_Hive_Brick", 20),
                drop("Soil_Hive_Brick_Smooth", 16),
                drop("Soil_Hive_Brick_Stairs", 10),
                drop("Plant_Cactus_Ball_1", 8),
                drop("Ingredient_Hide_Scaled", 6)
        ), List.of(
                reward("ExpeditionPoint", 5),
                crystalReward("InsectNest", 1)
        ));

        register(expeditions, "InsideInsectNest", 25, List.of(
                drop(OneBlockDropId.entityDropId("Scarak_Louse"), 3),
                drop(OneBlockDropId.entityDropId("Scarak_Fighter"), 2),
                drop(OneBlockDropId.entityDropId("Scarak_Defender"), 1),
                drop(OneBlockDropId.entityDropId("Scarak_Seeker"), 1),
                drop("Soil_Hive", 22),
                drop("Soil_Hive_Brick", 20),
                drop("Soil_Hive_Brick_Smooth", 16),
                drop("Soil_Hive_Brick_Beam", 10),
                drop("Soil_Hive_Brick_Stairs", 10),
                drop("Rock_Gem_Ruby", 5)
        ), List.of(
                reward("ExpeditionPoint", 6),
                crystalReward("InsectCore", 1)
        ));

        register(expeditions, "Quarry", 50, List.of(
                drop(OneBlockDropId.entityDropId("Bat"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 1),
                drop("Rubble_Stone", 45),
                drop("Rock_Stone", 45),
                drop("Rock_Slate", 22),
                drop("Rock_Shale", 20),
                drop("Rock_Basalt", 14),
                drop("Ore_Copper", 8),
                drop("Ore_Iron", 2)
        ), List.of(
                reward("ExpeditionPoint", 2),
                crystalReward("MysteriousCavern", 1)
        ));

        register(expeditions, "Hallow", 30, List.of(
                drop(OneBlockDropId.entityDropId("Rabbit"), 4),
                drop(OneBlockDropId.entityDropId("Sheep"), 4),
                drop(OneBlockDropId.entityDropId("Chicken"), 4),
                drop(OneBlockDropId.entityDropId("Deer"), 1),
                drop("Plant_Crop_Wheat_Item", 30),
                drop("Plant_Crop_Corn_Item", 20),
                drop("Plant_Crop_Carrot_Item", 20),
                drop("Food_Bread", 8),
                drop("Plant_Fruit_Apple", 8)
        ), List.of(
                reward("ExpeditionPoint", 2),
                crystalReward("CowHallow", 1),
                crystalReward("HorseHallow", 1),
                crystalReward("River", 1)
        ));

        register(expeditions, "CowHallow", 35, List.of(
                drop(OneBlockDropId.entityDropId("Cow"), 5),
                drop(OneBlockDropId.entityDropId("Cow_Calf"), 4),
                drop(OneBlockDropId.entityDropId("Wolf"), 1),
                drop("Plant_Crop_Wheat_Item", 30),
                drop("Plant_Crop_Corn_Item", 24),
                drop("Ingredient_Bolt_Wool", 8),
                drop("Food_Bread", 8),
                drop("Plant_Fruit_Apple", 8)
        ), List.of(
                reward("ExpeditionPoint", 2)
        ));

        register(expeditions, "HorseHallow", 35, List.of(
                drop(OneBlockDropId.entityDropId("Horse"), 5),
                drop(OneBlockDropId.entityDropId("Wolf"), 1),
                drop("Plant_Crop_Carrot_Item", 30),
                drop("Plant_Crop_Wheat_Item", 24),
                drop("Ingredient_Bolt_Wool", 8),
                drop("Food_Bread", 8),
                drop("Plant_Fruit_Apple", 8)
        ), List.of(
                reward("ExpeditionPoint", 2)
        ));

        register(expeditions, "MysteriousCavern", 30, List.of(
                drop(OneBlockDropId.entityDropId("Bat"), 3),
                drop(OneBlockDropId.entityDropId("Cave_Spider"), 2),
                drop(OneBlockDropId.entityDropId("Earth_Elemental"), 1),
                drop("Rubble_Stone", 24),
                drop("Rock_Stone", 24),
                drop("Rock_Slate", 18),
                drop("Rock_Shale", 18),
                drop("Rock_Gem_Sapphire", 3),
                drop("Ore_Iron", 8)
        ), List.of(
                reward("ExpeditionPoint", 3),
                crystalReward("MysticCave", 1)
        ));

        register(expeditions, "MysticCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Earth_Elemental"), 2),
                drop(OneBlockDropId.entityDropId("Earthen_Golem"), 1),
                drop("Rock_Runic_Brick", 22),
                drop("Rock_Runic_Brick_Ornate", 12),
                drop("Rock_Gem_Sapphire", 8),
                drop("Rock_Gem_Emerald", 6),
                drop("Ingredient_Crystal_Yellow", 3)
        ), List.of(
                reward("ExpeditionPoint", 4),
                crystalReward("LuxirousCave", 1)
        ));

        register(expeditions, "LuxirousCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Earth_Elemental"), 2),
                drop(OneBlockDropId.entityDropId("Earthen_Golem"), 2),
                drop(OneBlockDropId.entityDropId("Goblin_Thief"), 1),
                drop("Rock_Gem_Emerald", 10),
                drop("Rock_Gem_Topaz", 8),
                drop("Rock_Gem_Diamond", 3),
                drop("Ore_Gold", 12),
                drop("Ore_Adamantite", 3),
                drop("Deco_Treasure", 4)
        ), List.of(
                reward("ExpeditionPoint", 6),
                crystalReward("JurassicCave", 1)
        ));

        register(expeditions, "JurassicCave", 30, List.of(
                drop(OneBlockDropId.entityDropId("Archaeopteryx"), 3),
                drop(OneBlockDropId.entityDropId("Crocodile"), 2),
                drop(OneBlockDropId.entityDropId("Cave_Raptor"), 2),
                drop(OneBlockDropId.entityDropId("Cave_Rex"), 1),
                drop("Ingredient_Bone_Fragment", 20),
                drop("Deco_Bone_Full", 10),
                drop("Deco_Bone_Spike_Large", 8),
                drop("Ingredient_Hide_Scaled", 10),
                drop("Ingredient_Leather_Scaled", 6),
                drop("Rock_Gem_Diamond", 4),
                drop("Ore_Adamantite", 8)
        ), List.of(
                reward("ExpeditionPoint", 8),
                crystalReward("DinoCrisis", 1)
        ));

        register(expeditions, "DryTrorkCamp", 25, List.of(
                drop(OneBlockDropId.entityDropId("Trork_Sentry"), 3),
                drop(OneBlockDropId.entityDropId("Trork_Hunter"), 2),
                drop(OneBlockDropId.entityDropId("Hunting_Wolf"), 2),
                drop(OneBlockDropId.entityDropId("Trork_Brawler"), 1),
                drop("Wood_Ash_Trunk", 18),
                drop("Rock_Sandstone", 22),
                drop("Rubble_Sandstone", 20),
                drop("Ingredient_Bone_Fragment", 8),
                drop("Ingredient_Hide_Scaled", 6),
                drop("Ore_Gold", 3)
        ), List.of(
                reward("ExpeditionPoint", 5),
                crystalReward("TrorkWarband", 1)
        ));

        register(expeditions, "TrorkHuntingGround", 25, List.of(
                drop(OneBlockDropId.entityDropId("Hunting_Wolf"), 4),
                drop(OneBlockDropId.entityDropId("Trork_Hunter"), 3),
                drop(OneBlockDropId.entityDropId("Trork_Sentry"), 2),
                drop(OneBlockDropId.entityDropId("Trork_Brawler"), 1),
                drop("Wood_Beech_Trunk", 20),
                drop("Wood_Ash_Trunk", 18),
                drop("Plant_Leaves_Bramble", 14),
                drop("Ingredient_Bone_Fragment", 8),
                drop("Ingredient_Hide_Scaled", 6),
                drop("Rock_Gem_Emerald", 3)
        ), List.of(
                reward("ExpeditionPoint", 5),
                crystalReward("TrorkWarband", 1)
        ));

        register(expeditions, "TrorkStrongholdApproach", 25, List.of(
                drop(OneBlockDropId.entityDropId("Trork_Sentry"), 3),
                drop(OneBlockDropId.entityDropId("Trork_Warrior"), 3),
                drop(OneBlockDropId.entityDropId("Trork_Guard"), 2),
                drop(OneBlockDropId.entityDropId("Trork_Mauler"), 1),
                drop("Rock_Sandstone", 18),
                drop("Rock_Runic_Brick", 14),
                drop("Ingredient_Bone_Fragment", 10),
                drop("Ingredient_Hide_Scaled", 8),
                drop("Ingredient_Leather_Scaled", 5),
                drop("Ore_Thorium", 4)
        ), List.of(
                reward("ExpeditionPoint", 6),
                crystalReward("TrorkElderGrove", 1)
        ));

        register(expeditions, "TrorkElderGrove", 25, List.of(
                drop(OneBlockDropId.entityDropId("Trork_Elder"), 2),
                drop(OneBlockDropId.entityDropId("Trork_Hunter"), 2),
                drop(OneBlockDropId.entityDropId("Trork_Guard"), 2),
                drop(OneBlockDropId.entityDropId("Hunting_Wolf"), 2),
                drop("Wood_Amber_Trunk", 14),
                drop("Wood_Amber_Roots", 10),
                drop("Plant_Leaves_Amber", 12),
                drop("Plant_Leaves_Bramble", 12),
                drop("Rock_Gem_Emerald", 5),
                drop("Rock_Gem_Zephyr", 2)
        ), List.of(
                reward("ExpeditionPoint", 7),
                crystalReward("TrorkChieftainCamp", 1)
        ));

        register(expeditions, "FrozenGraveyard", 25, List.of(
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Scout"), 4),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Ranger"), 3),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Fighter"), 3),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Archer"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Soldier"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Mage"), 1),
                drop("Ingredient_Bone_Fragment", 24),
                drop("Deco_Bone_Full", 12),
                drop("Deco_Bone_Spike", 10),
                drop("Deco_Bone_Spine", 8),
                drop("Rock_Ice", 18),
                drop("Rubble_Ice", 16),
                drop("Rock_Ice_Icicles", 8),
                drop("Ingredient_Ice_Essence", 4),
                drop("Rock_Gem_Sapphire", 3)
        ), List.of(
                reward("ExpeditionPoint", 9),
                crystalReward("IcyNecropolis", 1)
        ));

        register(expeditions, "IcyNecropolis", 25, List.of(
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Ranger"), 3),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Fighter"), 3),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Archer"), 3),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Mage"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Soldier"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Knight"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Skeleton_Archmage"), 1),
                drop("Ingredient_Bone_Fragment", 20),
                drop("Deco_Bone_Full", 12),
                drop("Deco_Bone_Spike", 10),
                drop("Deco_Bone_Spine", 10),
                drop("Rock_Ice", 16),
                drop("Rubble_Ice", 14),
                drop("Rock_Ice_Icicles", 10),
                drop("Ingredient_Ice_Essence", 6),
                drop("Rock_Gem_Sapphire", 5),
                drop("Rock_Gem_Diamond", 2)
        ), List.of(
                reward("ExpeditionPoint", 10),
                crystalReward("FrostboneCrypt", 1)
        ));

        register(expeditions, "BurntBattlefield", 25, List.of(
                drop(OneBlockDropId.entityDropId("Skeleton_Scout"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Soldier"), 3),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Archer"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Lancer"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Knight"), 1),
                drop("Ingredient_Bone_Fragment", 22),
                drop("Deco_Bone_Full", 10),
                drop("Deco_Bone_Spike", 10),
                drop("Rock_Volcanic", 20),
                drop("Rubble_Volcanic", 18),
                drop("Ingredient_Fire_Essence", 6),
                drop("Ore_Thorium", 4),
                drop("Rock_Gem_Ruby", 3)
        ), List.of(
                reward("ExpeditionPoint", 8),
                crystalReward("AshenCatacombs", 1)
        ));

        register(expeditions, "AshenCatacombs", 25, List.of(
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Soldier"), 3),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Archer"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Lancer"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Gunner"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Wizard"), 2),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Knight"), 1),
                drop("Ingredient_Bone_Fragment", 20),
                drop("Deco_Bone_Full", 12),
                drop("Deco_Bone_Spike", 10),
                drop("Deco_Bone_Spine", 8),
                drop("Rock_Volcanic", 18),
                drop("Rubble_Volcanic", 16),
                drop("Rock_Volcanic_Cracked_Incandescent", 8),
                drop("Ingredient_Fire_Essence", 8),
                drop("Rock_Gem_Ruby", 5),
                drop("Ore_Mithril", 3)
        ), List.of(
                reward("ExpeditionPoint", 9),
                crystalReward("BurntSkeletonCitadel", 1)
        ));

        register(expeditions, "JungleEdge", 25, List.of(
                drop(OneBlockDropId.entityDropId("Parrot"), 4),
                drop(OneBlockDropId.entityDropId("Gecko"), 4),
                drop(OneBlockDropId.entityDropId("Frog"), 3),
                drop(OneBlockDropId.entityDropId("Marsh_Snake"), 2),
                drop(OneBlockDropId.entityDropId("Zombie"), 1),
                drop("Wood_Beech_Trunk", 18),
                drop("Wood_Maple_Trunk", 16),
                drop("Plant_Leaves_Bramble", 14),
                drop("Plant_Moss_Block_Green", 14),
                drop("Plant_Moss_Rug_Green", 12),
                drop("Plant_Crop_Mushroom_Common_Brown", 8),
                drop("Rock_Gem_Emerald", 3)
        ), List.of(
                reward("ExpeditionPoint", 4),
                crystalReward("OvergrownRuins", 1)
        ));

        register(expeditions, "DryJunglePass", 25, List.of(
                drop(OneBlockDropId.entityDropId("Cactee"), 2),
                drop(OneBlockDropId.entityDropId("Gecko"), 4),
                drop(OneBlockDropId.entityDropId("Marsh_Snake"), 2),
                drop(OneBlockDropId.entityDropId("Sandswept_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Scout"), 1),
                drop("Block_Sand", 20),
                drop("Rock_Sandstone", 20),
                drop("Rubble_Sandstone", 18),
                drop("Plant_Cactus_1", 10),
                drop("Plant_Moss_Block_Green", 10),
                drop("Plant_Leaves_Bramble", 10),
                drop("Rock_Gem_Topaz", 3)
        ), List.of(
                reward("ExpeditionPoint", 4),
                crystalReward("SunkenJungleRuins", 1)
        ));

        register(expeditions, "OvergrownRuins", 25, List.of(
                drop(OneBlockDropId.entityDropId("Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Scout"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Archer"), 1),
                drop(OneBlockDropId.entityDropId("Crocodile"), 1),
                drop(OneBlockDropId.entityDropId("Rhino_Toad"), 1),
                drop("Wood_Maple_Trunk", 14),
                drop("Plant_Leaves_Bramble", 16),
                drop("Plant_Moss_Block_Green", 14),
                drop("Plant_Moss_Rug_Green", 12),
                drop("Rock_Runic_Brick", 14),
                drop("Rock_Runic_Brick_Ornate", 8),
                drop("Ingredient_Bone_Fragment", 8),
                drop("Rock_Gem_Emerald", 4)
        ), List.of(
                reward("ExpeditionPoint", 5),
                crystalReward("JungleCrypt", 1)
        ));

        register(expeditions, "SunkenJungleRuins", 25, List.of(
                drop(OneBlockDropId.entityDropId("Sandswept_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Soldier"), 1),
                drop(OneBlockDropId.entityDropId("Crocodile"), 2),
                drop(OneBlockDropId.entityDropId("Marsh_Snake"), 2),
                drop("Soil_Clay_Blue", 16),
                drop("Soil_Dirt", 14),
                drop("Plant_Moss_Block_Green", 16),
                drop("Plant_Moss_Rug_Green", 14),
                drop("Rock_Runic_Brick", 12),
                drop("Rock_Sandstone_Brick", 10),
                drop("Ingredient_Bone_Fragment", 8),
                drop("Rock_Gem_Sapphire", 4)
        ), List.of(
                reward("ExpeditionPoint", 5),
                crystalReward("JungleCrypt", 1)
        ));

        register(expeditions, "LostNecropolis", 25, List.of(
                drop(OneBlockDropId.entityDropId("Skeleton_Soldier"), 3),
                drop(OneBlockDropId.entityDropId("Skeleton_Fighter"), 3),
                drop(OneBlockDropId.entityDropId("Skeleton_Archer"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Ranger"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Mage"), 2),
                drop(OneBlockDropId.entityDropId("Aberrant_Zombie"), 2),
                drop(OneBlockDropId.entityDropId("Large_Aberrant_Zombie"), 1),
                drop("Ingredient_Bone_Fragment", 20),
                drop("Deco_Bone_Full", 12),
                drop("Deco_Bone_Spike", 10),
                drop("Rock_Runic_Brick", 16),
                drop("Rock_Runic_Brick_Ornate", 10),
                drop("Rock_Gem_Emerald", 5),
                drop("Rock_Gem_Ruby", 3)
        ), List.of(
                reward("ExpeditionPoint", 7),
                crystalReward("AncientUndeadSanctum", 1),
                crystalReward("ShadowedJungleRoad", 1)
        ));

        register(expeditions, "ShadowedJungleRoad", 25, List.of(
                drop(OneBlockDropId.entityDropId("Wraith"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Horse"), 2),
                drop(OneBlockDropId.entityDropId("Armored_Skeleton_Horse"), 1),
                drop(OneBlockDropId.entityDropId("Skeleton_Knight"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Archmage"), 1),
                drop(OneBlockDropId.entityDropId("Undead_Pig"), 2),
                drop(OneBlockDropId.entityDropId("Undead_Chicken"), 2),
                drop("Plant_Leaves_Bramble", 16),
                drop("Rock_Runic_Brick", 16),
                drop("Rock_Runic_Brick_Ornate", 10),
                drop("Ingredient_Bone_Fragment", 16),
                drop("Deco_Bone_Spine", 8),
                drop("Rock_Gem_Zephyr", 3)
        ), List.of(
                reward("ExpeditionPoint", 8),
                crystalReward("ArmoredDeadGrove", 1)
        ));

        register(expeditions, "ArmoredDeadGrove", 25, List.of(
                drop(OneBlockDropId.entityDropId("Armored_Skeleton_Horse"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Horse"), 2),
                drop(OneBlockDropId.entityDropId("Shadow_Knight"), 1),
                drop(OneBlockDropId.entityDropId("Wraith"), 2),
                drop(OneBlockDropId.entityDropId("Skeleton_Knight"), 3),
                drop(OneBlockDropId.entityDropId("Skeleton_Archmage"), 2),
                drop(OneBlockDropId.entityDropId("Ghoul"), 2),
                drop("Wood_Amber_Trunk", 12),
                drop("Plant_Leaves_Bramble", 16),
                drop("Rock_Runic_Brick", 16),
                drop("Ingredient_Bone_Fragment", 14),
                drop("Deco_Bone_Full", 8),
                drop("Deco_Bone_Spine", 8),
                drop("Rock_Gem_Zephyr", 4)
        ), List.of(
                reward("ExpeditionPoint", 9),
                crystalReward("ShadowKnightCitadel", 1)
        ));

        register(expeditions, "SpiritThreshold", 25, List.of(
                drop(OneBlockDropId.entityDropId("Root_Spirit"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Spirit"), 2),
                drop(OneBlockDropId.entityDropId("Thunder_Spirit"), 2),
                drop(OneBlockDropId.entityDropId("Ember_Spirit"), 2),
                drop(OneBlockDropId.entityDropId("Earthen_Golem"), 1),
                drop("Ingredient_Life_Essence", 16),
                drop("Ingredient_Ice_Essence", 14),
                drop("Ingredient_Lightning_Essence", 14),
                drop("Ingredient_Fire_Essence", 14),
                drop("Ingredient_Water_Essence", 10),
                drop("Ingredient_Motes_Light", 8),
                drop("Ingredient_Void_Essence", 4),
                drop("Ingredient_Voidheart", 1)
        ), List.of(
                reward("ExpeditionPoint", 9),
                crystalReward("ElementalConfluence", 1)
        ));

        register(expeditions, "ElementalConfluence", 25, List.of(
                drop(OneBlockDropId.entityDropId("Earthen_Golem"), 2),
                drop(OneBlockDropId.entityDropId("Firesteel_Golem"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Golem"), 2),
                drop(OneBlockDropId.entityDropId("Ember_Golem"), 2),
                drop(OneBlockDropId.entityDropId("Sandswept_Golem"), 2),
                drop(OneBlockDropId.entityDropId("Thunder_Golem"), 2),
                drop(OneBlockDropId.entityDropId("Frost_Spirit"), 2),
                drop(OneBlockDropId.entityDropId("Thunder_Spirit"), 2),
                drop(OneBlockDropId.entityDropId("Root_Spirit"), 2),
                drop(OneBlockDropId.entityDropId("Ember_Spirit"), 2),
                drop("Ingredient_Fire_Essence", 16),
                drop("Ingredient_Ice_Essence", 16),
                drop("Ingredient_Life_Essence", 16),
                drop("Ingredient_Lightning_Essence", 16),
                drop("Ingredient_Water_Essence", 12),
                drop("Ingredient_Motes_Light", 10),
                drop("Ingredient_Void_Essence", 6),
                drop("Ingredient_Voidheart", 2)
        ), List.of(
                reward("ExpeditionPoint", 11),
                crystalReward("SpiritRealmTrial", 1)
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
