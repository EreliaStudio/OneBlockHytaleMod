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
                drop("Ingredient_Stick", 20),
                drop("Rubble_Stone", 20),
                drop("Soil_Dirt", 10),
                drop("Plant_Grass_Lush", 10)
        ), List.of(
                crystalReward("Plain_Edge", 1),
                crystalReward("Forest_Edge", 1),
                crystalReward("Cave_Entry", 1)
        ));

        register(expeditions, "Plain_Edge", 25, List.of(
                drop("Plant_Grass_Lush", 20),
                drop("Ingredient_Fibre", 12),
                drop("Plant_Flower_Common_White", 8),
                drop("Plant_Hay_Bundle", 6),
                drop(OneBlockDropId.entityDropId("Rabbit"), 4),
                drop(OneBlockDropId.entityDropId("Chicken"), 3)
        ), List.of(
                crystalReward("Plains", 1),
                crystalReward("Flower_Prairie", 1)
        ));

        register(expeditions, "Plains", 35, List.of(
                drop("Plant_Grass_Lush", 20),
                drop("Plant_Hay_Bundle", 12),
                drop("Plant_Flower_Common_White", 8),
                drop("Plant_Sunflower_Block", 5),
                drop(OneBlockDropId.entityDropId("Fox"), 3),
                drop(OneBlockDropId.entityDropId("Boar"), 3)
        ), List.of(
                crystalReward("Horse_Field", 1),
                crystalReward("Cow_Pasture", 1),
                crystalReward("Fox_Hollow", 1),
                crystalReward("Windy_Steppe", 1),
                crystalReward("Gobelin_Camp", 1)
        ));

        register(expeditions, "Flower_Prairie", 35, List.of(
                drop("Plant_Flower_Common_White", 18),
                drop("Plant_Lavender_Block", 12),
                drop("Plant_Sunflower_Block", 10),
                drop("Plant_Petals_White", 8),
                drop("Plant_Grass_Lush", 8),
                drop(OneBlockDropId.entityDropId("Bluebird"), 4)
        ), List.of(
                reward("ExpeditionPoint", 1)
        ), List.of(
                bundle(List.of(reward("Ingredient_Motes_Light", 1)), 3),
                bundle(List.of(reward("Ingredient_Life_Essence", 1)), 1)
        ));

        register(expeditions, "Horse_Field", 40, List.of(
                drop("Plant_Grass_Lush", 20),
                drop("Plant_Hay_Bundle", 16),
                drop("Plant_Flower_Common_White", 6),
                drop("Ingredient_Hide_Light", 3),
                drop(OneBlockDropId.entityDropId("Antelope"), 3),
                drop(OneBlockDropId.entityDropId("Bison"), 2)
        ), List.of(
                reward("ExpeditionPoint", 1)
        ));

        register(expeditions, "Cow_Pasture", 40, List.of(
                drop("Plant_Grass_Lush", 20),
                drop("Plant_Hay_Bundle", 16),
                drop("Ingredient_Hide_Soft", 5),
                drop("Ingredient_Leather_Soft", 2),
                drop(OneBlockDropId.entityDropId("Sheep"), 3),
                drop(OneBlockDropId.entityDropId("Bison_Calf"), 2)
        ), List.of(
                reward("ExpeditionPoint", 1)
        ));

        register(expeditions, "Fox_Hollow", 40, List.of(
                drop("Plant_Fruit_Berries_Red", 16),
                drop("Plant_Bush", 10),
                drop("Plant_Crop_Mushroom_Common_Brown", 6),
                drop("Ingredient_Hide_Soft", 3),
                drop(OneBlockDropId.entityDropId("Fox"), 5),
                drop(OneBlockDropId.entityDropId("Rabbit"), 3)
        ), List.of(
                reward("ExpeditionPoint", 1)
        ));

        register(expeditions, "Windy_Steppe", 45, List.of(
                drop("Plant_Grass_Lush", 14),
                drop("Rock_Stone", 10),
                drop("Ingredient_Feathers_Light", 5),
                drop("Plant_Fruit_Windwillow", 4),
                drop("Wood_Windwillow_Trunk", 4),
                drop(OneBlockDropId.entityDropId("Crow"), 3)
        ), List.of(
                crystalReward("Sandstone_Cavern", 1)
        ));

        register(expeditions, "Forest_Edge", 25, List.of(
                drop("Ingredient_Stick", 20),
                drop("Wood_Oak_Trunk", 18),
                drop("Plant_Leaves_Oak", 12),
                drop("Plant_Bush", 8),
                drop("Plant_Fruit_Berries_Red", 5)
        ), List.of(
                crystalReward("Young_Forest", 1)
        ));

        register(expeditions, "Young_Forest", 35, List.of(
                drop("Wood_Oak_Trunk", 18),
                drop("Wood_Beech_Trunk", 10),
                drop("Ingredient_Stick", 12),
                drop("Plant_Leaves_Oak", 10),
                drop("Plant_Fruit_Apple", 4),
                drop(OneBlockDropId.entityDropId("Boar"), 3)
        ), List.of(
                crystalReward("Deep_Forest", 1),
                crystalReward("Mushroom_Grove", 1),
                crystalReward("Fairy_Pond", 1)
        ));

        register(expeditions, "Deep_Forest", 45, List.of(
                drop("Wood_Beech_Trunk", 14),
                drop("Wood_Birch_Trunk", 10),
                drop("Wood_Maple_Trunk", 8),
                drop("Plant_Vine", 6),
                drop("Plant_Crop_Mushroom_Common_Brown", 5),
                drop(OneBlockDropId.entityDropId("Wolf"), 3)
        ), List.of(
                crystalReward("Darkwood_Thicket", 1),
                crystalReward("Time_Locked_Glade", 1)
        ));

        register(expeditions, "Fairy_Pond", 40, List.of(
                drop("Plant_Lavender_Block", 12),
                drop("Plant_Petals_White", 10),
                drop("Ingredient_Crystal_Cyan", 4),
                drop("Ingredient_Motes_Light", 4),
                drop("Ingredient_Water_Essence", 2),
                drop(OneBlockDropId.entityDropId("Frog"), 4)
        ), List.of(
                crystalReward("Time_Locked_Glade", 1)
        ));

        register(expeditions, "Mushroom_Grove", 40, List.of(
                drop("Plant_Crop_Mushroom_Common_Brown", 14),
                drop("Plant_Crop_Mushroom_Block_Brown", 8),
                drop("Plant_Crop_Mushroom_Cap_Red", 6),
                drop("Plant_Crop_Mushroom_Flatcap_Green", 5),
                drop("Plant_Crop_Mushroom_Shelve_Brown", 5),
                drop(OneBlockDropId.entityDropId("Spider"), 3)
        ), List.of(
                reward("ExpeditionPoint", 1)
        ));

        register(expeditions, "Darkwood_Thicket", 50, List.of(
                drop("Wood_Ash_Trunk", 12),
                drop("Wood_Poisoned_Trunk", 8),
                drop("Plant_Vine", 8),
                drop("Plant_Leaves_Poisoned_Floor", 6),
                drop("Ingredient_Sac_Venom", 3),
                drop(OneBlockDropId.entityDropId("Spider"), 4)
        ), List.of(
                crystalReward("Swamp_Trail", 1)
        ));

        register(expeditions, "Swamp_Trail", 45, List.of(
                drop("Plant_Reeds_Marsh", 14),
                drop("Rock_Stone_Mossy", 12),
                drop("Plant_Vine_Rug", 8),
                drop("Plant_Roots_Leafy", 6),
                drop("Plant_Crop_Mushroom_Cap_Poison", 4),
                drop(OneBlockDropId.entityDropId("Frog"), 4)
        ), List.of(
                crystalReward("Sunken_Swamp", 1),
                crystalReward("Swamp_Ruins", 1)
        ));

        register(expeditions, "Sunken_Swamp", 55, List.of(
                drop("Wood_Sallow_Trunk", 12),
                drop("Plant_Reeds_Marsh", 12),
                drop("Plant_Roots_Leafy", 8),
                drop("Plant_Crop_Mushroom_Cap_Poison", 5),
                drop("Ingredient_Sac_Venom", 3),
                drop(OneBlockDropId.entityDropId("Crocodile"), 2)
        ), List.of(
                crystalReward("Forgotten_Marsh", 1)
        ));

        register(expeditions, "Forgotten_Marsh", 60, List.of(
                drop("Wood_Poisoned_Trunk", 12),
                drop("Plant_Crop_Mushroom_Cap_Poison", 8),
                drop("Plant_Reeds_Marsh", 8),
                drop("Ingredient_Bone_Fragment", 5),
                drop("Ingredient_Void_Essence", 2),
                drop(OneBlockDropId.entityDropId("Fen_Stalker"), 3)
        ), List.of(
                crystalReward("Desert_Fringe", 1)
        ));

        register(expeditions, "Time_Locked_Glade", 55, List.of(
                drop("Wood_Crystal_Trunk", 8),
                drop("Wood_Spiral_Trunk", 6),
                drop("Plant_Fruit_Spiral", 6),
                drop("Ingredient_Crystal_Green", 4),
                drop("Ingredient_Motes_Light", 3),
                drop(OneBlockDropId.entityDropId("Archaeopteryx"), 2)
        ), List.of(
                crystalReward("Primeval_Forest", 1)
        ));

        register(expeditions, "Primeval_Forest", 65, List.of(
                drop("Block_Primal_Stone", 12),
                drop("Plant_Primal_Fern", 10),
                drop("Plant_Giant_Jungle_Leaf", 8),
                drop("Rock_Dinosaur_Fossil", 4),
                drop("Ingredient_Primal_Meat", 3),
                drop(OneBlockDropId.entityDropId("Cave_Raptor"), 3)
        ), List.of(
                crystalReward("Dinosaur_Nest", 1)
        ));

        register(expeditions, "Dinosaur_Nest", 70, List.of(
                drop("Ingredient_Dinosaur_Bone", 10),
                drop("Rock_Dinosaur_Fossil", 8),
                drop("Ingredient_Claw_CaveRaptor", 5),
                drop("Ingredient_Tooth_CaveRex", 3),
                drop("Ingredient_Primal_Core", 1),
                drop(OneBlockDropId.entityDropId("Cave_Raptor"), 3),
                drop(OneBlockDropId.entityDropId("Cave_Rex"), 1)
        ), List.of(
                reward("ExpeditionPoint", 2)
        ));

        register(expeditions, "Cave_Entry", 25, List.of(
                drop("Rubble_Stone", 20),
                drop("Rock_Stone", 12),
                drop("Rock_Shale", 5),
                drop("Rock_Slate", 5),
                drop(OneBlockDropId.entityDropId("Bat"), 4),
                drop(OneBlockDropId.entityDropId("Rat"), 3)
        ), List.of(
                crystalReward("Shallow_Cave", 1),
                crystalReward("Stone_Gallery", 1)
        ));

        register(expeditions, "Shallow_Cave", 35, List.of(
                drop("Rock_Stone", 18),
                drop("Rubble_Stone", 12),
                drop("Rock_Shale", 8),
                drop("Rock_Slate", 6),
                drop("Plant_Roots_Cave", 4),
                drop(OneBlockDropId.entityDropId("Bat"), 3)
        ), List.of(
                crystalReward("Copper_Cave", 1),
                crystalReward("Mossy_Cave", 1)
        ));

        register(expeditions, "Stone_Gallery", 35, List.of(
                drop("Rubble_Stone", 16),
                drop("Rock_Stone", 14),
                drop("Rock_Shale", 10),
                drop("Rock_Slate", 10),
                drop("Rock_Stone_Mossy", 5)
        ), List.of(
                reward("ExpeditionPoint", 1)
        ));

        register(expeditions, "Mossy_Cave", 40, List.of(
                drop("Rock_Stone_Mossy", 12),
                drop("Plant_Roots_Cave", 10),
                drop("Plant_Roots_Cave_Small", 8),
                drop("Plant_Crop_Mushroom_Glowing_Green", 5),
                drop("Plant_Crop_Mushroom_Common_Brown", 5),
                drop(OneBlockDropId.entityDropId("Cave_Spider"), 3)
        ), List.of(
                reward("ExpeditionPoint", 1)
        ));

        register(expeditions, "Copper_Cave", 45, List.of(
                drop("Rock_Stone", 18),
                drop("Ore_Copper", 14),
                drop("Rubble_Stone", 8),
                drop("Rock_Shale", 6),
                drop(OneBlockDropId.entityDropId("Bat"), 3),
                drop(OneBlockDropId.entityDropId("Goblin_Miner"), 2)
        ), List.of(
                crystalReward("Iron_Hollow", 1),
                crystalReward("Gobelin_Camp", 1)
        ));

        register(expeditions, "Iron_Hollow", 50, List.of(
                drop("Rock_Stone", 16),
                drop("Ore_Copper", 12),
                drop("Ore_Iron", 5),
                drop("Rock_Slate", 6),
                drop("Ingredient_Stud_Iron", 2),
                drop(OneBlockDropId.entityDropId("Cave_Spider"), 3)
        ), List.of(
                crystalReward("Abandoned_Mine", 1),
                crystalReward("Frozen_Cavern", 1),
                crystalReward("Sandstone_Cavern", 1)
        ));

        register(expeditions, "Basalt_Gate", 55, List.of(
                drop("Rock_Basalt", 18),
                drop("Ore_Iron", 10),
                drop("Rock_Volcanic", 6),
                drop("Ingredient_Charcoal", 5),
                drop("Ore_Thorium", 2),
                drop(OneBlockDropId.entityDropId("Earth_Elemental"), 2)
        ), List.of(
                crystalReward("Basalt_Depths", 1)
        ));

        register(expeditions, "Basalt_Depths", 60, List.of(
                drop("Rock_Basalt", 16),
                drop("Rock_Volcanic", 8),
                drop("Ore_Iron", 8),
                drop("Ore_Thorium", 6),
                drop("Ingredient_Fire_Essence", 2),
                drop(OneBlockDropId.entityDropId("Earthen_Golem"), 2)
        ), List.of(
                crystalReward("Thorium_Fissure", 1),
                crystalReward("Crystal_Grotto", 1)
        ));

        register(expeditions, "Thorium_Fissure", 65, List.of(
                drop("Rock_Basalt", 14),
                drop("Ore_Thorium", 10),
                drop("Rock_Volcanic", 8),
                drop("Ore_Cobalt", 2),
                drop("Ingredient_Fire_Essence", 3),
                drop(OneBlockDropId.entityDropId("Ember_Golem"), 2)
        ), List.of(
                crystalReward("Cobalt_Chasm", 1)
        ));

        register(expeditions, "Cobalt_Chasm", 70, List.of(
                drop("Rock_Basalt", 12),
                drop("Ore_Thorium", 8),
                drop("Ore_Cobalt", 6),
                drop("Rock_Gem_Sapphire", 3),
                drop("Ingredient_Lightning_Essence", 2),
                drop(OneBlockDropId.entityDropId("Thunder_Golem"), 1)
        ), List.of(
                crystalReward("Mithril_Vein", 1)
        ));

        register(expeditions, "Mithril_Vein", 75, List.of(
                drop("Ore_Mithril", 7),
                drop("Ore_Cobalt", 6),
                drop("Rock_Gem_Emerald", 3),
                drop("Rock_Gem_Sapphire", 3),
                drop("Ingredient_Crystal_Cyan", 3),
                drop(OneBlockDropId.entityDropId("Earth_Elemental"), 2)
        ), List.of(
                crystalReward("Onyxium_Pocket", 1)
        ));

        register(expeditions, "Onyxium_Pocket", 80, List.of(
                drop("Ore_Onyxium", 6),
                drop("Ore_Mithril", 6),
                drop("Rock_Gem_Diamond", 2),
                drop("Ingredient_Void_Essence", 3),
                drop("Ingredient_Voidheart", 1),
                drop(OneBlockDropId.entityDropId("Void_Crawler"), 2)
        ), List.of(
                crystalReward("Adamantite_Core", 1)
        ));

        register(expeditions, "Adamantite_Core", 90, List.of(
                drop("Ore_Adamantite", 6),
                drop("Ore_Onyxium", 5),
                drop("Rock_Gem_Diamond", 3),
                drop("Rock_Gem_Zephyr", 2),
                drop("Ingredient_Voidheart", 1),
                drop(OneBlockDropId.entityDropId("Void_Spectre"), 1)
        ), List.of(
                crystalReward("Prisma_Crystal_Mine", 1)
        ));

        register(expeditions, "Prisma_Crystal_Mine", 100, List.of(
                drop("Ore_Prisma", 5),
                drop("Ore_Adamantite", 5),
                drop("Rock_Gem_Zephyr", 3),
                drop("Ingredient_Crystal_White", 4),
                drop("Ingredient_Bar_Prisma", 1),
                drop(OneBlockDropId.entityDropId("Void_Eye"), 1)
        ), List.of(
                reward("ExpeditionPoint", 4)
        ));

        register(expeditions, "Crystal_Grotto", 60, List.of(
                drop("Ingredient_Crystal_Blue", 8),
                drop("Ingredient_Crystal_Cyan", 8),
                drop("Rock_Gem_Sapphire", 4),
                drop("Rock_Gem_Emerald", 4),
                drop("Rock_Gem_Topaz", 3),
                drop(OneBlockDropId.entityDropId("Earth_Elemental"), 2)
        ), List.of(
                crystalReward("Frozen_Ruins", 1)
        ));

        register(expeditions, "Frozen_Cavern", 55, List.of(
                drop("Rock_Ice", 14),
                drop("Rock_Ice_Permafrost", 8),
                drop("Block_Ice_Blue", 6),
                drop("Block_Snow", 6),
                drop("Ore_Iron", 4),
                drop(OneBlockDropId.entityDropId("Frost_Zombie"), 2)
        ), List.of(
                crystalReward("Snowy_Pass", 1)
        ));

        register(expeditions, "Snowy_Pass", 60, List.of(
                drop("Block_Snow", 12),
                drop("Block_Snow_Packed", 8),
                drop("Block_Ice_Clear", 6),
                drop("Plant_Snow_Shrub", 5),
                drop("Ingredient_Ice_Essence", 2),
                drop(OneBlockDropId.entityDropId("Wolf"), 3)
        ), List.of(
                crystalReward("Snowy_Plains", 1)
        ));

        register(expeditions, "Snowy_Plains", 65, List.of(
                drop("Block_Snow_Packed", 12),
                drop("Plant_Frozen_Grass", 8),
                drop("Plant_Frost_Fern", 6),
                drop("Ingredient_Hide_Heavy", 4),
                drop("Ingredient_Ice_Essence", 2),
                drop(OneBlockDropId.entityDropId("Bison"), 2)
        ), List.of(
                crystalReward("Frostpine_Forest", 1),
                crystalReward("Ice_Lake", 1),
                crystalReward("Whitefang_Den", 1)
        ));

        register(expeditions, "Frostpine_Forest", 65, List.of(
                drop("Wood_Fir_Trunk", 12),
                drop("Wood_Ice_Trunk", 8),
                drop("Plant_Frost_Fern", 8),
                drop("Ingredient_Tree_Bark", 5),
                drop("Ingredient_Ice_Essence", 2),
                drop(OneBlockDropId.entityDropId("Wolf"), 3)
        ), List.of(
                crystalReward("Frozen_Ruins", 1)
        ));

        register(expeditions, "Ice_Lake", 60, List.of(
                drop("Block_Ice_Clear", 12),
                drop("Block_Frozen_Water", 8),
                drop("Block_Ice_Blue", 6),
                drop("Ingredient_Water_Essence", 2),
                drop("Ingredient_Ice_Essence", 2),
                drop(OneBlockDropId.entityDropId("Bluegill"), 3)
        ), List.of(
                reward("ExpeditionPoint", 1)
        ));

        register(expeditions, "Whitefang_Den", 65, List.of(
                drop("Ingredient_Hide_Heavy", 8),
                drop("Ingredient_Leather_Heavy", 4),
                drop("Ingredient_Bone_Fragment", 6),
                drop("Block_Permafrost", 6),
                drop("Ingredient_Ice_Essence", 2),
                drop(OneBlockDropId.entityDropId("Wolf"), 5)
        ), List.of(
                reward("ExpeditionPoint", 1)
        ));

        register(expeditions, "Polar_Necropolis", 80, List.of(
                drop("Ore_Cobalt_Ice", 8),
                drop("Rock_Glacial_Crystal", 6),
                drop("Rock_Frozen_Stone", 6),
                drop("Ingredient_Ice_Essence", 4),
                drop("Ingredient_Scale_IceDragon", 1),
                drop(OneBlockDropId.entityDropId("Ice_Dragon"), 1)
        ), List.of(
                reward("ExpeditionPoint", 3)
        ));

        register(expeditions, "Sandstone_Cavern", 55, List.of(
                drop("Rock_Sandstone", 14),
                drop("Block_Sandstone_Rough", 8),
                drop("Block_Sand", 6),
                drop("Ore_Copper", 5),
                drop("Ore_Iron", 2),
                drop(OneBlockDropId.entityDropId("Crawler"), 2)
        ), List.of(
                crystalReward("Desert_Fringe", 1)
        ));

        register(expeditions, "Desert_Fringe", 60, List.of(
                drop("Block_Sand", 12),
                drop("Block_Desert_Hardened_Earth", 8),
                drop("Plant_Desert_Dry_Shrub", 8),
                drop("Plant_Cactus_1", 5),
                drop("Ingredient_Hide_Camel", 2),
                drop(OneBlockDropId.entityDropId("Camel"), 2)
        ), List.of(
                crystalReward("Oasis", 1),
                crystalReward("Scorpion_Dunes", 1),
                crystalReward("Cactus_Flats", 1)
        ));

        register(expeditions, "Oasis", 55, List.of(
                drop("Wood_Palm_Trunk", 10),
                drop("Plant_Fruit_Coconut", 8),
                drop("Plant_Reeds_Marsh", 8),
                drop("Plant_Desert_Blue_Aloe", 6),
                drop("Ingredient_Water_Essence", 2),
                drop(OneBlockDropId.entityDropId("Duck"), 3)
        ), List.of(
                reward("ExpeditionPoint", 1)
        ));

        register(expeditions, "Scorpion_Dunes", 65, List.of(
                drop("Block_Sand", 12),
                drop("Block_Sand_Red", 8),
                drop("Plant_Desert_Saltbush", 6),
                drop("Ingredient_Shell_Scorpion", 5),
                drop("Ingredient_Claw_Scorpion", 3),
                drop(OneBlockDropId.entityDropId("Cactee"), 3)
        ), List.of(
                crystalReward("Desert_Temple", 1)
        ));

        register(expeditions, "Cactus_Flats", 60, List.of(
                drop("Plant_Cactus_1", 10),
                drop("Plant_Cactus_2", 8),
                drop("Plant_Cactus_3", 6),
                drop("Plant_Cactus_Ball_1", 6),
                drop("Plant_Cactus_Flower", 4),
                drop(OneBlockDropId.entityDropId("Armadillo"), 3)
        ), List.of(
                reward("ExpeditionPoint", 1)
        ));

        register(expeditions, "Sunfire_Tomb", 85, List.of(
                drop("Ore_Thorium_Desert", 8),
                drop("Rock_Gem_Ruby", 4),
                drop("Ingredient_Fire_Essence", 4),
                drop("Block_Sandstone_Red", 6),
                drop("Rock_Desert_Fossil", 3),
                drop(OneBlockDropId.entityDropId("Burnt_Skeleton_Wizard"), 2)
        ), List.of(
                reward("ExpeditionPoint", 3)
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
