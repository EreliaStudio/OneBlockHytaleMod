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
                drop("Ingredient_Fibre", 30),
                drop("Ingredient_Stick", 24),
                drop("Plant_Grass_Lush", 18),
                drop("Plant_Bush", 14),
                drop("Plant_Flower_Common_White", 10),
                drop("Plant_Hay_Bundle", 8)
        ));

        register(expeditions, "Rocky_Meadow", List.of(
                drop("Rock_Stone", 30),
                drop("Rock_Shale", 24),
                drop("Ingredient_Stick", 18),
                drop("Plant_Roots_Leafy", 14),
                drop("Plant_Petals_White", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Flower_Field", List.of(
                drop("Plant_Lavender_Block", 30),
                drop("Plant_Lavender_Stage_0", 24),
                drop("Plant_Sunflower_Block", 18),
                drop("Plant_Sunflower_Stage_0", 14),
                drop("Plant_Petals_White", 10),
                drop("Plant_Flower_Common_White", 8)
        ));

        register(expeditions, "Bramble_Thicket", List.of(
                drop("Plant_Bramble_Moss_Twisted", 30),
                drop("Plant_Vine", 24),
                drop("Plant_Vine_Rug", 18),
                drop("Plant_Roots_Leafy", 14),
                drop("Ingredient_Tree_Bark", 10),
                drop("Ingredient_Tree_Sap", 8)
        ));

        register(expeditions, "Marsh_Reeds", List.of(
                drop("Plant_Reeds_Marsh", 30),
                drop("Plant_Barnacles", 24),
                drop("Plant_Seaweed_Grass", 18),
                drop("Plant_Seaweed_Grass_Stack", 14),
                drop("Ingredient_Water_Essence", 10),
                drop("Plant_Roots_Cave_Small", 8)
        ));

        register(expeditions, "Cave", List.of(
                drop("Rock_Stone", 30),
                drop("Rock_Shale", 24),
                drop("Plant_Roots_Cave", 18),
                drop("Plant_Roots_Cave_Small", 14),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Deep_Cave", List.of(
                drop("Rock_Basalt", 30),
                drop("Rock_Slate", 24),
                drop("Rock_Stone_Mossy", 18),
                drop("Ingredient_Bone_Fragment", 10),
                drop("Ingredient_Void_Essence", 8)
        ));

        register(expeditions, "Copper_Cave", List.of(
                drop("Ore_Copper", 30),
                drop("Rock_Stone", 24),
                drop("Rock_Shale", 18),
                drop("Rock_Gem_Topaz", 10),
                drop("Ingredient_Bar_Copper", 8)
        ));

        register(expeditions, "Iron_Cave", List.of(
                drop("Ore_Iron", 30),
                drop("Rock_Slate", 24),
                drop("Rock_Basalt", 18),
                drop("Rock_Gem_Ruby", 10),
                drop("Ingredient_Bar_Iron", 8)
        ));

        register(expeditions, "Silver_Cave", List.of(
                drop("Ore_Silver", 30),
                drop("Rock_Basalt", 24),
                drop("Rock_Gem_Sapphire", 18),
                drop("Ingredient_Bar_Silver", 10),
                drop("Ingredient_Motes_Light", 8)
        ));

        register(expeditions, "Gold_Cave", List.of(
                drop("Ore_Gold", 30),
                drop("Rock_Slate", 24),
                drop("Rock_Gem_Emerald", 18),
                drop("Ingredient_Bar_Gold", 10),
                drop("Ingredient_Lightning_Essence", 8)
        ));

        register(expeditions, "Thorium_Cave", List.of(
                drop("Ore_Thorium", 30),
                drop("Rock_Volcanic", 24),
                drop("Rock_Gem_Diamond", 18),
                drop("Ingredient_Bar_Thorium", 10),
                drop("Ingredient_Fire_Essence", 8)
        ));

        register(expeditions, "Mithril_Cave", List.of(
                drop("Ore_Mithril", 30),
                drop("Rock_Gem_Diamond", 24),
                drop("Rock_Gem_Zephyr", 18),
                drop("Ingredient_Bar_Mithril", 10),
                drop("Ingredient_Lightning_Essence", 8)
        ));

        register(expeditions, "Cobalt_Depths", List.of(
                drop("Ore_Cobalt", 30),
                drop("Rock_Basalt", 24),
                drop("Rock_Ice", 18),
                drop("Ingredient_Bar_Cobalt", 10),
                drop("Ingredient_Ice_Essence", 8)
        ));

        register(expeditions, "Adamantite_Depths", List.of(
                drop("Ore_Adamantite", 30),
                drop("Rock_Volcanic", 24),
                drop("Rock_Gem_Diamond", 18),
                drop("Ingredient_Bar_Adamantite", 10),
                drop("Ingredient_Fire_Essence", 8)
        ));

        register(expeditions, "Onyxium_Abyss", List.of(
                drop("Ore_Onyxium", 30),
                drop("Rock_Volcanic", 24),
                drop("Rock_Stone_Mossy", 18),
                drop("Ingredient_Voidheart", 14),
                drop("Ingredient_Bar_Onyxium", 10),
                drop("Ingredient_Void_Essence", 8)
        ));

        register(expeditions, "Prisma_Rift", List.of(
                drop("Ore_Prisma", 30),
                drop("Rock_Gem_Zephyr", 24),
                drop("Ingredient_Bar_Prisma", 14),
                drop("Ingredient_Bolt_Prismaloom", 10),
                drop("Ingredient_Fabric_Scrap_Prismaloom", 8)
        ));

        register(expeditions, "Gem_Cave", List.of(
                drop("Rock_Gem_Topaz", 12),
                drop("Rock_Gem_Sapphire", 10),
                drop("Rock_Gem_Ruby", 8),
                drop("Rock_Gem_Emerald", 8),
                drop("Rock_Gem_Diamond", 4),
                drop("Rock_Gem_Zephyr", 3)
        ));

        register(expeditions, "Ancient_Forge", List.of(
                drop("Ingredient_Bar_Bronze", 30),
                drop("Ingredient_Bar_Copper", 24),
                drop("Ingredient_Bar_Iron", 18),
                drop("Ingredient_Stud_Iron", 14),
                drop("Ingredient_Charcoal", 10),
                drop("Ingredient_Powder_Boom", 8)
        ));

        register(expeditions, "Oak_Forest", List.of(
                drop("Wood_Oak_Trunk", 30),
                drop("Plant_Leaves_Oak", 24),
                drop("Plant_Fruit_Apple", 18),
                drop("Ingredient_Tree_Bark", 14),
                drop("Ingredient_Tree_Sap", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Beech_Forest", List.of(
                drop("Wood_Beech_Trunk", 30),
                drop("Plant_Leaves_Autumn_Floor", 24),
                drop("Plant_Fruit_Berries_Red", 18),
                drop("Ingredient_Tree_Bark", 14),
                drop("Ingredient_Tree_Sap", 10),
                drop("Ingredient_Fibre", 8)
        ));

        register(expeditions, "Birch_Forest", List.of(
                drop("Wood_Birch_Trunk", 30),
                drop("Plant_Leaves_Autumn_Floor", 24),
                drop("Plant_Fruit_Pinkberry", 18),
                drop("Ingredient_Tree_Bark", 14),
                drop("Ingredient_Tree_Sap", 10),
                drop("Plant_Vine", 8)
        ));

        register(expeditions, "Maple_Forest", List.of(
                drop("Wood_Maple_Trunk", 30),
                drop("Plant_Leaves_Autumn_Floor", 24),
                drop("Plant_Fruit_Berries_Red", 18),
                drop("Ingredient_Tree_Sap", 14),
                drop("Ingredient_Tree_Bark", 10),
                drop("Plant_Lavender_Block", 8)
        ));

        register(expeditions, "Ash_Grove", List.of(
                drop("Wood_Ash_Trunk", 30),
                drop("Plant_Roots_Leafy", 24),
                drop("Plant_Bramble_Moss_Twisted", 18),
                drop("Ingredient_Tree_Bark", 14),
                drop("Ingredient_Charcoal", 10),
                drop("Ingredient_Fire_Essence", 8)
        ));

        register(expeditions, "Aspen_Grove", List.of(
                drop("Wood_Aspen_Trunk", 30),
                drop("Plant_Leaves_Autumn_Floor", 24),
                drop("Plant_Flower_Common_White", 18),
                drop("Ingredient_Tree_Bark", 14),
                drop("Ingredient_Tree_Sap", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Cedar_Woods", List.of(
                drop("Wood_Cedar_Trunk", 30),
                drop("Plant_Roots_Leafy", 24),
                drop("Ingredient_Tree_Bark", 18),
                drop("Ingredient_Tree_Sap", 14),
                drop("Ingredient_Stick", 10),
                drop("Ingredient_Fibre", 8)
        ));

        register(expeditions, "Fir_Woods", List.of(
                drop("Wood_Fir_Trunk", 30),
                drop("Plant_Roots_Leafy", 24),
                drop("Plant_Leaves_Autumn_Floor", 18),
                drop("Ingredient_Tree_Bark", 14),
                drop("Ingredient_Tree_Sap", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Redwood_Forest", List.of(
                drop("Wood_Redwood_Trunk", 30),
                drop("Plant_Vine", 24),
                drop("Plant_Roots_Leafy", 18),
                drop("Ingredient_Tree_Bark", 14),
                drop("Ingredient_Tree_Sap", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Bamboo_Jungle", List.of(
                drop("Wood_Bamboo_Trunk", 30),
                drop("Wood_Jungle_Trunk", 24),
                drop("Plant_Leaves_Jungle_Floor", 18),
                drop("Plant_Vine", 14),
                drop("Plant_Reeds_Marsh", 10),
                drop("Plant_Fruit_Mango", 8)
        ));

        register(expeditions, "Jungle", List.of(
                drop("Wood_Jungle_Trunk", 30),
                drop("Wood_Banyan_Trunk", 24),
                drop("Plant_Leaves_Jungle_Floor", 18),
                drop("Plant_Vine", 14),
                drop("Plant_Fruit_Mango", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Banyan_Jungle", List.of(
                drop("Wood_Banyan_Trunk", 30),
                drop("Wood_Gumboab_Trunk", 24),
                drop("Plant_Leaves_Jungle_Floor", 18),
                drop("Plant_Roots_Leafy", 14),
                drop("Plant_Fruit_Mango", 10),
                drop("Ingredient_Tree_Sap", 8)
        ));

        register(expeditions, "Palm_Coast", List.of(
                drop("Wood_Palm_Trunk", 30),
                drop("Plant_Fruit_Coconut", 24),
                drop("Plant_Seaweed_Grass", 18),
                drop("Plant_Seaweed_Grass_Stack", 14),
                drop("Ingredient_Water_Essence", 10),
                drop("Plant_Barnacles", 8)
        ));

        register(expeditions, "Palo_Drywood", List.of(
                drop("Wood_Palo_Trunk", 30),
                drop("Wood_Dry_Trunk", 24),
                drop("Plant_Desert_Dry_Shrub", 18),
                drop("Plant_Desert_Saltbush", 14),
                drop("Ingredient_Tree_Bark", 10),
                drop("Ingredient_Tree_Sap", 8)
        ));

        register(expeditions, "BottleTree_Savanna", List.of(
                drop("Wood_Bottletree_Trunk", 30),
                drop("Plant_Fruit_Coconut", 24),
                drop("Plant_Hay_Bundle", 18),
                drop("Ingredient_Tree_Bark", 14),
                drop("Ingredient_Tree_Sap", 10),
                drop("Ingredient_Fibre", 8)
        ));

        register(expeditions, "Camphor_Grove", List.of(
                drop("Wood_Camphor_Trunk", 30),
                drop("Plant_Flower_Common_White", 24),
                drop("Plant_Petals_White", 18),
                drop("Ingredient_Tree_Sap", 14),
                drop("Ingredient_Tree_Bark", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Blue_Fig_Grove", List.of(
                drop("Wood_Fig_Blue_Trunk", 30),
                drop("Plant_Fruit_Azure", 24),
                drop("Plant_Fruit_Pinkberry", 18),
                drop("Plant_Leaves_Jungle_Floor", 14),
                drop("Ingredient_Tree_Sap", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Spiral_Woods", List.of(
                drop("Wood_Spiral_Trunk", 30),
                drop("Plant_Fruit_Spiral", 24),
                drop("Plant_Vine_Rug", 18),
                drop("Plant_Roots_Leafy", 14),
                drop("Ingredient_Tree_Sap", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Windwillow_Grove", List.of(
                drop("Wood_Windwillow_Trunk", 30),
                drop("Plant_Fruit_Windwillow", 24),
                drop("Plant_Vine", 18),
                drop("Plant_Petals_White", 14),
                drop("Ingredient_Lightning_Essence", 10),
                drop("Ingredient_Tree_Sap", 8)
        ));

        register(expeditions, "Stormbark_Woods", List.of(
                drop("Wood_Stormbark_Trunk", 30),
                drop("Plant_Fruit_Windwillow", 24),
                drop("Ingredient_Lightning_Essence", 18),
                drop("Ingredient_Bolt_Stormsilk", 14),
                drop("Ingredient_Fabric_Scrap_Stormsilk", 10),
                drop("Ingredient_Hide_Storm", 8)
        ));

        register(expeditions, "Wisteria_Wildwood", List.of(
                drop("Wood_Wisteria_Wild_Trunk", 30),
                drop("Plant_Petals_White", 24),
                drop("Plant_Vine_Rug", 18),
                drop("Plant_Fruit_Pinkberry", 14),
                drop("Ingredient_Tree_Sap", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Azure_Forest", List.of(
                drop("Wood_Azure_Trunk", 30),
                drop("Plant_Fruit_Azure", 24),
                drop("Plant_Crop_Mushroom_Block_Blue", 18),
                drop("Plant_Crop_Mushroom_Common_Blue", 14),
                drop("Ingredient_Ice_Essence", 10)
        ));

        register(expeditions, "Crystal_Grove", List.of(
                drop("Wood_Crystal_Trunk", 30),
                drop("Ingredient_Bolt_Prismaloom", 14),
                drop("Ingredient_Fabric_Scrap_Prismaloom", 10),
                drop("Ingredient_Motes_Light", 8)
        ));

        register(expeditions, "Poisoned_Woods", List.of(
                drop("Wood_Poisoned_Trunk", 30),
                drop("Plant_Leaves_Poisoned_Floor", 24),
                drop("Plant_Fruit_Poison", 18),
                drop("Plant_Crop_Mushroom_Cap_Poison", 14),
                drop("Ingredient_Sac_Venom", 10),
                drop("Ingredient_Void_Essence", 8)
        ));

        register(expeditions, "Petrified_Forest", List.of(
                drop("Wood_Petrified_Trunk", 30),
                drop("Rock_Stone_Mossy", 24),
                drop("Rock_Slate", 18),
                drop("Ingredient_Bone_Fragment", 14),
                drop("Ingredient_Void_Essence", 10),
                drop("Plant_Roots_Cave", 8)
        ));

        register(expeditions, "Sallow_Wetland", List.of(
                drop("Wood_Sallow_Trunk", 30),
                drop("Plant_Barnacles", 24),
                drop("Plant_Reeds_Marsh", 18),
                drop("Plant_Seaweed_Grass", 14),
                drop("Ingredient_Water_Essence", 10),
                drop("Plant_Roots_Leafy", 8)
        ));

        register(expeditions, "Fire_Woodland", List.of(
                drop("Wood_Fire_Trunk", 30),
                drop("Wood_Burnt_Trunk", 24),
                drop("Ingredient_Charcoal", 18),
                drop("Ingredient_Fire_Essence", 14),
                drop("Ingredient_Bolt_Cindercloth", 10),
                drop("Ingredient_Fabric_Scrap_Cindercloth", 8)
        ));

        register(expeditions, "Ice_Woodland", List.of(
                drop("Wood_Ice_Trunk", 30),
                drop("Block_Snow", 24),
                drop("Block_Ice_Blue", 18),
                drop("Plant_Snow_Shrub", 14),
                drop("Ingredient_Ice_Essence", 10),
                drop("Rock_Ice", 8)
        ));

        register(expeditions, "Desert", List.of(
                drop("Block_Sand", 30),
                drop("Block_Sandstone_Rough", 24),
                drop("Rock_Sandstone", 18),
                drop("Plant_Desert_Dry_Shrub", 14),
                drop("Plant_Cactus_1", 10),
                drop("Ingredient_Fire_Essence", 8)
        ));

        register(expeditions, "White_Dunes", List.of(
                drop("Block_Sand_White", 30),
                drop("Block_Clay", 24),
                drop("Plant_Desert_Yellow_Arid_Flower", 18),
                drop("Plant_Desert_Saltbush", 14),
                drop("Plant_Cactus_Flat_1", 10),
                drop("Rock_Desert_Cracked", 8)
        ));

        register(expeditions, "Red_Dunes", List.of(
                drop("Block_Sand_Red", 30),
                drop("Block_Clay_Red", 24),
                drop("Plant_Desert_Red_Featherleaf", 18),
                drop("Plant_Cactus_Flower", 14),
                drop("Plant_Cactus_2", 10),
                drop("Rock_Desert_Fossil", 8)
        ));

        register(expeditions, "Saltbush_Dunes", List.of(
                drop("Block_Desert_Hardened_Earth", 30),
                drop("Block_Desert_Dried_Mud", 24),
                drop("Plant_Desert_Saltbush", 18),
                drop("Plant_Desert_Blue_Aloe", 14),
                drop("Plant_Cactus_Ball_1", 10),
                drop("Ingredient_Water_Essence", 8)
        ));

        register(expeditions, "Sandstone_Ruins", List.of(
                drop("Block_Sandstone_Smooth", 30),
                drop("Block_Sandstone_Red", 24),
                drop("Block_Sandstone_Rough", 18),
                drop("Rock_Sandstone", 14),
                drop("Rock_Desert_Fossil", 10),
                drop("Ingredient_Motes_Light", 8)
        ));

        register(expeditions, "Cactus_Garden", List.of(
                drop("Plant_Cactus_1", 30),
                drop("Plant_Cactus_2", 24),
                drop("Plant_Cactus_3", 18),
                drop("Plant_Cactus_Ball_1", 14),
                drop("Plant_Cactus_Flat_1", 10),
                drop("Plant_Cactus_Flat_2", 8),
                drop("Plant_Cactus_Flat_3", 5),
                drop("Plant_Cactus_Flower", 3)
        ));

        register(expeditions, "Thorium_Desert_Mine", List.of(
                drop("Ore_Thorium_Desert", 30),
                drop("Rock_Desert_Cracked", 24),
                drop("Rock_Desert_Fossil", 18),
                drop("Block_Desert_Hardened_Earth", 14),
                drop("Ingredient_Fire_Essence", 10),
                drop("Rock_Gem_Topaz", 8)
        ));

        register(expeditions, "Scorched_Desert_Cache", List.of(
                drop("Ingredient_Shell_Scorpion", 30),
                drop("Ingredient_Claw_Scorpion", 24),
                drop("Ingredient_Fang_SaberTooth", 18),
                drop("Ingredient_Feather_Vulture", 14),
                drop("Ingredient_Hide_Camel", 10),
                drop("Ingredient_Hide_Scarak", 8)
        ));

        register(expeditions, "Snowfield", List.of(
                drop("Block_Snow", 30),
                drop("Block_Snow_Packed", 24),
                drop("Plant_Snow_Shrub", 18),
                drop("Plant_Frozen_Grass", 14),
                drop("Ingredient_Ice_Essence", 10),
                drop("Rock_Ice", 8)
        ));

        register(expeditions, "Frozen_Lake", List.of(
                drop("Block_Ice_Clear", 30),
                drop("Block_Ice_Blue", 24),
                drop("Block_Frozen_Water", 18),
                drop("Plant_Ice_Flower_Blue", 14),
                drop("Plant_Ice_Flower_White", 10),
                drop("Ingredient_Water_Essence", 8)
        ));

        register(expeditions, "Permafrost_Cave", List.of(
                drop("Block_Permafrost", 30),
                drop("Block_Frozen_Soil", 24),
                drop("Block_Ice_Frosted", 18),
                drop("Rock_Ice_Permafrost", 14),
                drop("Rock_Frozen_Stone", 10),
                drop("Ingredient_Ice_Essence", 8)
        ));

        register(expeditions, "Frost_Fern_Forest", List.of(
                drop("Plant_Frost_Fern", 30),
                drop("Plant_Ice_Flower_Blue", 24),
                drop("Plant_Ice_Flower_White", 18),
                drop("Plant_Frozen_Grass", 14),
                drop("Plant_Snow_Shrub", 10),
                drop("Wood_Ice_Trunk", 8)
        ));

        register(expeditions, "Cobalt_Ice_Mine", List.of(
                drop("Ore_Cobalt_Ice", 30),
                drop("Rock_Glacial_Crystal", 24),
                drop("Rock_Frozen_Stone", 18),
                drop("Block_Ice_Blue", 14),
                drop("Ingredient_Ice_Essence", 10),
                drop("Rock_Gem_Sapphire", 8)
        ));

        register(expeditions, "Frozen_Trophy_Cache", List.of(
                drop("Ingredient_Fur_PolarBear", 30),
                drop("Ingredient_Fur_Yeti", 24),
                drop("Ingredient_Horn_Ram", 18),
                drop("Ingredient_Tusk_Moose", 14),
                drop("Ingredient_Scale_IceDragon", 10),
                drop("Ingredient_Ice_Essence", 8)
        ));

        register(expeditions, "Volcanic_Badlands", List.of(
                drop("Block_Volcanic_Ash", 30),
                drop("Block_Lava_Rock_Cracked", 24),
                drop("Rock_Volcanic", 18),
                drop("Ingredient_Charcoal", 14),
                drop("Ingredient_Fire_Essence", 10)
        ));

        register(expeditions, "Deep_Inferno", List.of(
                drop("Rock_Volcanic", 30),
                drop("Block_Lava_Rock_Cracked", 24),
                drop("Ingredient_Fire_Essence", 18),
                drop("Ingredient_Void_Essence", 14),
                drop("Ingredient_Bolt_Cindercloth", 10),
                drop("Ingredient_Fabric_Scrap_Cindercloth", 8)
        ));

        register(expeditions, "Primal_Jungle", List.of(
                drop("Block_Jungle_Fern_Giant", 30),
                drop("Block_Glowing_Jungle_Plant", 24),
                drop("Plant_Primal_Fern", 18),
                drop("Plant_Glowcap_Jungle", 14),
                drop("Plant_Toxic_Jungle_Vine", 10),
                drop("Plant_Giant_Jungle_Leaf", 8)
        ));

        register(expeditions, "Dino_Caverns", List.of(
                drop("Block_Primal_Stone", 30),
                drop("Block_Fossil_Stone", 24),
                drop("Block_Amber", 18),
                drop("Rock_Amber_Encased", 14),
                drop("Rock_Dinosaur_Fossil", 10),
                drop("Ingredient_Dinosaur_Bone", 8)
        ));

        register(expeditions, "Primal_Iron_Mine", List.of(
                drop("Ore_Primal_Iron", 30),
                drop("Rock_Amber_Encased", 24),
                drop("Rock_Dinosaur_Fossil", 18),
                drop("Ingredient_Primal_Core", 14),
                drop("Ingredient_Dinosaur_Bone", 10),
                drop("Ingredient_Fire_Essence", 8)
        ));

        register(expeditions, "Primal_Trophy_Cache", List.of(
                drop("Ingredient_Tooth_CaveRex", 30),
                drop("Ingredient_Claw_CaveRaptor", 24),
                drop("Ingredient_Scale_Dimetrodon", 18),
                drop("Ingredient_Hide_Trillodon", 14),
                drop("Ingredient_Feather_Pterodactyl", 10),
                drop("Ingredient_Primal_Meat", 8)
        ));

        register(expeditions, "Common_Mushroom_Cave", List.of(
                drop("Plant_Crop_Mushroom_Block", 30),
                drop("Plant_Crop_Mushroom_Block_Brown", 24),
                drop("Plant_Crop_Mushroom_Common_Brown", 18),
                drop("Plant_Crop_Mushroom_Cap_Brown", 14),
                drop("Plant_Crop_Mushroom_Shelve_Brown", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Colored_Mushroom_Cave", List.of(
                drop("Plant_Crop_Mushroom_Block_Green", 30),
                drop("Plant_Crop_Mushroom_Block_Red", 24),
                drop("Plant_Crop_Mushroom_Block_White", 18),
                drop("Plant_Crop_Mushroom_Block_Yellow", 14),
                drop("Plant_Crop_Mushroom_Common_Lime", 10),
                drop("Plant_Crop_Mushroom_Cap_Green", 8)
        ));

        register(expeditions, "Blue_Mushroom_Grotto", List.of(
                drop("Plant_Crop_Mushroom_Block_Blue", 30),
                drop("Plant_Crop_Mushroom_Common_Blue", 24),
                drop("Plant_Crop_Mushroom_Flatcap_Blue", 18),
                drop("Plant_Crop_Mushroom_Glowing_Blue", 14),
                drop("Ingredient_Ice_Essence", 10)
        ));

        register(expeditions, "Poison_Mushroom_Grotto", List.of(
                drop("Plant_Crop_Mushroom_Block_Purple", 30),
                drop("Plant_Crop_Mushroom_Cap_Poison", 24),
                drop("Plant_Crop_Mushroom_Glowing_Purple", 18),
                drop("Plant_Crop_Mushroom_Glowing_Violet", 14),
                drop("Ingredient_Sac_Venom", 10),
                drop("Ingredient_Void_Essence", 8)
        ));

        register(expeditions, "Glowing_Mushroom_Cavern", List.of(
                drop("Plant_Crop_Mushroom_Glowing_Green", 30),
                drop("Plant_Crop_Mushroom_Glowing_Orange", 24),
                drop("Plant_Crop_Mushroom_Glowing_Red", 18),
                drop("Plant_Crop_Mushroom_Glowing_Purple", 14),
                drop("Plant_Crop_Mushroom_Glowing_Violet", 10),
                drop("Ingredient_Motes_Light", 8)
        ));

        register(expeditions, "Redcap_Mushroom_Cave", List.of(
                drop("Plant_Crop_Mushroom_Cap_Red", 30),
                drop("Plant_Crop_Mushroom_Cap_White", 24),
                drop("Plant_Crop_Mushroom_Flatcap_Green", 18),
                drop("Plant_Crop_Mushroom_Glowing_Green", 14),
                drop("Plant_Crop_Mushroom_Glowing_Orange", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Leatherworker_Camp", List.of(
                drop("Ingredient_Hide_Light", 30),
                drop("Ingredient_Hide_Medium", 24),
                drop("Ingredient_Hide_Heavy", 18),
                drop("Ingredient_Hide_Soft", 14),
                drop("Ingredient_Strap_Leather", 10),
                drop("Ingredient_Leather_Light", 8)
        ));

        register(expeditions, "Dark_Leather_Camp", List.of(
                drop("Ingredient_Hide_Dark", 30),
                drop("Ingredient_Leather_Dark", 24),
                drop("Ingredient_Hide_Scaled", 18),
                drop("Ingredient_Leather_Scaled", 14),
                drop("Ingredient_Hide_Prismic", 10),
                drop("Ingredient_Leather_Prismic", 8)
        ));

        register(expeditions, "Storm_Leather_Camp", List.of(
                drop("Ingredient_Hide_Storm", 30),
                drop("Ingredient_Leather_Storm", 24),
                drop("Ingredient_Leather_Medium", 18),
                drop("Ingredient_Leather_Heavy", 14),
                drop("Ingredient_Leather_Soft", 10),
                drop("Ingredient_Chitin_Sturdy", 8)
        ));

        register(expeditions, "Feather_Rookery", List.of(
                drop("Ingredient_Feathers_Light", 30),
                drop("Ingredient_Feathers_Blue", 24),
                drop("Ingredient_Feathers_Red", 18),
                drop("Ingredient_Feathers_Dark", 14),
                drop("Ingredient_Bone_Fragment", 10),
                drop("Ingredient_Sinue_Cindersinue", 8)
        ));

        register(expeditions, "Textile_Cottage", List.of(
                drop("Ingredient_Fabric_Scrap_Cotton", 30),
                drop("Ingredient_Fabric_Scrap_Linen", 24),
                drop("Ingredient_Fabric_Scrap_Wool", 18),
                drop("Ingredient_Bolt_Cotton", 14),
                drop("Ingredient_Bolt_Linen", 10),
                drop("Ingredient_Bolt_Wool", 8)
        ));

        register(expeditions, "Silk_Warren", List.of(
                drop("Ingredient_Fabric_Scrap_Silk", 30),
                drop("Ingredient_Bolt_Silk", 24),
                drop("Ingredient_Chitin_Sturdy", 18),
                drop("Ingredient_Fibre", 14),
                drop("Ingredient_Sinue_Cindersinue", 10),
                drop("Ingredient_Life_Essence", 8)
        ));

        register(expeditions, "Shadow_Loom", List.of(
                drop("Ingredient_Fabric_Scrap_Shadoweave", 30),
                drop("Ingredient_Bolt_Shadoweave", 24),
                drop("Ingredient_Fabric_Scrap_Prismaloom", 18),
                drop("Ingredient_Bolt_Prismaloom", 14),
                drop("Ingredient_Voidheart", 10),
                drop("Ingredient_Motes_Light", 8)
        ));

        register(expeditions, "Cinder_Loom", List.of(
                drop("Ingredient_Fabric_Scrap_Cindercloth", 30),
                drop("Ingredient_Bolt_Cindercloth", 24),
                drop("Ingredient_Charcoal", 18),
                drop("Ingredient_Fire_Essence", 14),
                drop("Wood_Burnt_Trunk", 10),
                drop("Rock_Volcanic", 8)
        ));

        register(expeditions, "Storm_Loom", List.of(
                drop("Ingredient_Fabric_Scrap_Stormsilk", 30),
                drop("Ingredient_Bolt_Stormsilk", 24),
                drop("Ingredient_Lightning_Essence", 18),
                drop("Wood_Stormbark_Trunk", 14),
                drop("Plant_Fruit_Windwillow", 10),
                drop("Ingredient_Motes_Light", 8)
        ));

        register(expeditions, "Essence_Font", List.of(
                drop("Ingredient_Fire_Essence", 30),
                drop("Ingredient_Ice_Essence", 24),
                drop("Ingredient_Life_Essence", 18),
                drop("Ingredient_Lightning_Essence", 14),
                drop("Ingredient_Void_Essence", 10),
                drop("Ingredient_Water_Essence", 8),
                drop("Ingredient_Motes_Light", 5),
                drop("Ingredient_Voidheart", 3)
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
