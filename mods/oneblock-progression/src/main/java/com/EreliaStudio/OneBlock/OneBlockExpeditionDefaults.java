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
                drop("Ingredient_Fibre", 25),
                drop("Ingredient_Stick", 20),
                drop("Rubble_Stone", 20),
                drop("Soil_Dirt", 20),
                drop("Plant_Grass_Lush", 15),
                drop("Plant_Hay_Bundle", 10),
                drop("Plant_Bush", 8)
        ));

        register(expeditions, "Plains", List.of(
                drop("Soil_Grass", 25),
                drop("Plant_Grass_Lush", 20),
                drop("Ingredient_Fibre", 15),
                drop("Rubble_Stone", 15),
                drop("Plant_Hay_Bundle", 10),
                drop("Plant_Sapling_Oak", 8),
                drop("Ingredient_Life_Essence", 3)
        ));

        register(expeditions, "Flower_Field", List.of(
                drop("Plant_Flower_Common_White", 20),
                drop("Plant_Lavender_Block", 15),
                drop("Plant_Sunflower_Block", 15),
                drop("Plant_Petals_White", 15),
                drop("Plant_Grass_Lush", 15),
                drop("Ingredient_Fibre", 12),
                drop("Plant_Cactus_Flower", 5)
        ));

        register(expeditions, "Farmland", List.of(
                drop("Plant_Crop_Wheat_Item", 20),
                drop("Plant_Crop_Carrot_Item", 18),
                drop("Plant_Crop_Corn_Item", 18),
                drop("Plant_Crop_Potato_Item", 18),
                drop("Plant_Crop_Rice_Item", 15),
                drop("Plant_Crop_Onion_Item", 12),
                drop("Soil_Dirt", 8)
        ));

        register(expeditions, "Forest", List.of(
                drop("Wood_Oak_Trunk", 25),
                drop("Plant_Leaves_Oak", 20),
                drop("Ingredient_Fibre", 15),
                drop("Ingredient_Stick", 15),
                drop("Ingredient_Tree_Bark", 10),
                drop("Plant_Sapling_Oak", 8),
                drop("Ingredient_Life_Essence", 3)
        ));

        register(expeditions, "Garden", List.of(
                drop("Plant_Crop_Tomato_Item", 15),
                drop("Plant_Crop_Lettuce_Item", 15),
                drop("Plant_Crop_Aubergine_Item", 14),
                drop("Plant_Crop_Cauliflower_Item", 14),
                drop("Plant_Crop_Turnip_Item", 12),
                drop("Plant_Crop_Pumpkin_Item", 12),
                drop("Plant_Crop_Chilli_Item", 10),
                drop("Plant_Crop_Cotton_Item", 10)
        ));

        register(expeditions, "Cactus_Field", List.of(
                drop("Plant_Cactus_1", 22),
                drop("Plant_Cactus_2", 18),
                drop("Plant_Cactus_3", 16),
                drop("Plant_Cactus_Ball_1", 15),
                drop("Plant_Cactus_Flat_1", 12),
                drop("Plant_Cactus_Flat_2", 10),
                drop("Plant_Cactus_Flat_3", 10),
                drop("Plant_Cactus_Flower", 6)
        ));

        register(expeditions, "Desert", List.of(
                drop("Block_Sand", 30),
                drop("Rock_Sandstone", 22),
                drop("Block_Sandstone_Rough", 18),
                drop("Block_Clay", 14),
                drop("Block_Desert_Hardened_Earth", 12),
                drop("Plant_Desert_Dry_Shrub", 8),
                drop("Ingredient_Fire_Essence", 2)
        ));

        register(expeditions, "Desert_Oasis", List.of(
                drop("Block_Sand_White", 20),
                drop("Plant_Desert_Blue_Aloe", 18),
                drop("Plant_Desert_Yellow_Arid_Flower", 18),
                drop("Plant_Desert_Red_Featherleaf", 15),
                drop("Plant_Desert_Saltbush", 14),
                drop("Block_Clay_Red", 12),
                drop("Ingredient_Water_Essence", 3)
        ));

        register(expeditions, "Autumn_Forest", List.of(
                drop("Wood_Beech_Trunk", 22),
                drop("Wood_Maple_Trunk", 20),
                drop("Wood_Aspen_Trunk", 18),
                drop("Plant_Leaves_Autumn_Floor", 16),
                drop("Plant_Fruit_Berries_Red", 12),
                drop("Plant_Fruit_Apple", 10),
                drop("Plant_Sapling_Apple", 6)
        ));

        register(expeditions, "Snow", List.of(
                drop("Block_Snow", 30),
                drop("Block_Snow_Packed", 22),
                drop("Block_Frozen_Soil", 18),
                drop("Plant_Snow_Shrub", 14),
                drop("Plant_Frost_Fern", 12),
                drop("Plant_Frozen_Grass", 10),
                drop("Ingredient_Ice_Essence", 2)
        ));

        register(expeditions, "Deep_Forest", List.of(
                drop("Wood_Ash_Trunk", 22),
                drop("Wood_Birch_Trunk", 20),
                drop("Plant_Vine", 16),
                drop("Ingredient_Tree_Bark", 14),
                drop("Ingredient_Tree_Sap", 12),
                drop("Ingredient_Fibre", 10),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Swamp", List.of(
                drop("Wood_Sallow_Trunk", 18),
                drop("Plant_Reeds_Marsh", 18),
                drop("Plant_Barnacles", 14),
                drop("Plant_Bramble_Moss_Twisted", 14),
                drop("Plant_Vine_Rug", 12),
                drop("Plant_Seaweed_Grass", 10),
                drop("Plant_Seaweed_Grass_Stack", 8),
                drop("Ingredient_Water_Essence", 2)
        ));

        register(expeditions, "Tropical_Forest", List.of(
                drop("Wood_Palm_Trunk", 22),
                drop("Wood_Banyan_Trunk", 18),
                drop("Wood_Bottletree_Trunk", 15),
                drop("Plant_Fruit_Coconut", 15),
                drop("Plant_Fruit_Mango", 15),
                drop("Plant_Fruit_Pinkberry", 12),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Mushroom_Grove", List.of(
                drop("Plant_Crop_Mushroom_Block", 18),
                drop("Plant_Crop_Mushroom_Block_Brown", 16),
                drop("Plant_Crop_Mushroom_Block_Red", 14),
                drop("Plant_Crop_Mushroom_Cap_Brown", 14),
                drop("Plant_Crop_Mushroom_Cap_Red", 12),
                drop("Plant_Crop_Mushroom_Cap_White", 12),
                drop("Plant_Crop_Mushroom_Shelve_Brown", 10),
                drop("Plant_Crop_Mushroom_Common_Brown", 8)
        ));

        register(expeditions, "Cave", List.of(
                drop("Rock_Stone", 28),
                drop("Rubble_Stone", 22),
                drop("Rock_Shale", 18),
                drop("Ore_Copper", 12),
                drop("Plant_Roots_Cave", 10),
                drop("Plant_Roots_Cave_Small", 8),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Graveyard", List.of(
                drop("Soil_Dirt", 28),
                drop("Rock_Stone_Mossy", 22),
                drop("Rock_Stone", 18),
                drop("Ingredient_Bone_Fragment", 10),
                drop("Ingredient_Void_Essence", 2)
        ));

        register(expeditions, "Ice_Fields", List.of(
                drop("Block_Ice_Clear", 25),
                drop("Block_Ice_Frosted", 22),
                drop("Block_Frozen_Water", 18),
                drop("Plant_Ice_Flower_Blue", 14),
                drop("Plant_Ice_Flower_White", 14),
                drop("Ingredient_Ice_Essence", 3)
        ));

        register(expeditions, "Primal_Jungle", List.of(
                drop("Wood_Jungle_Trunk", 20),
                drop("Block_Jungle_Fern_Giant", 16),
                drop("Block_Glowing_Jungle_Plant", 14),
                drop("Plant_Primal_Fern", 14),
                drop("Plant_Toxic_Jungle_Vine", 12),
                drop("Plant_Giant_Jungle_Leaf", 12),
                drop("Plant_Glowcap_Jungle", 8),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Seed_Vault", List.of(
                drop("Plant_Seeds_Wheat_Eternal", 14),
                drop("Plant_Seeds_Carrot_Eternal", 14),
                drop("Plant_Seeds_Corn_Eternal", 14),
                drop("Plant_Seeds_Potato_Eternal", 12),
                drop("Plant_Seeds_Rice_Eternal", 12),
                drop("Plant_Seeds_Onion_Eternal", 10),
                drop("Plant_Seeds_Lettuce_Eternal", 10),
                drop("Plant_Seeds_Tomato_Eternal", 10),
                drop("Plant_Seeds_Turnip_Eternal", 8),
                drop("Plant_Seeds_Pumpkin_Eternal", 8),
                drop("Plant_Seeds_Aubergine_Eternal", 8),
                drop("Plant_Seeds_Cauliflower_Eternal", 8),
                drop("Plant_Seeds_Chilli_Eternal", 6),
                drop("Plant_Seeds_Cotton_Eternal", 6)
        ));

        register(expeditions, "Textile_Workshop", List.of(
                drop("Ingredient_Bolt_Cotton", 20),
                drop("Ingredient_Bolt_Linen", 18),
                drop("Ingredient_Bolt_Wool", 16),
                drop("Ingredient_Fabric_Scrap_Cotton", 15),
                drop("Ingredient_Fabric_Scrap_Linen", 15),
                drop("Ingredient_Fabric_Scrap_Wool", 12)
        ));

        register(expeditions, "Jungle", List.of(
                drop("Wood_Jungle_Trunk", 22),
                drop("Wood_Bamboo_Trunk", 18),
                drop("Plant_Reeds_Marsh", 14),
                drop("Plant_Vine_Rug", 14),
                drop("Plant_Leaves_Jungle_Floor", 12),
                drop("Plant_Fruit_Mango", 10),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Gem_Cave", List.of(
                drop("Rock_Stone", 20),
                drop("Rock_Basalt", 16),
                drop("Rock_Shale", 14),
                drop("Rock_Gem_Emerald", 7),
                drop("Rock_Gem_Ruby", 6),
                drop("Rock_Gem_Sapphire", 6),
                drop("Rock_Gem_Topaz", 6)
        ));

        register(expeditions, "Precious_Vein", List.of(
                drop("Ore_Gold", 16),
                drop("Ore_Silver", 20),
                drop("Rock_Gem_Topaz", 10),
                drop("Rock_Gem_Emerald", 8),
                drop("Rock_Stone", 20),
                drop("Rock_Basalt", 15)
        ));

        register(expeditions, "Egyptian_Temple", List.of(
                drop("Block_Sandstone_Smooth", 25),
                drop("Block_Sandstone_Red", 18),
                drop("Rock_Sandstone", 18),
                drop("Block_Clay_Red", 12),
                drop("Ore_Gold", 6),
                drop("Ingredient_Fire_Essence", 3)
        ));

        register(expeditions, "Desert_Ruins", List.of(
                drop("Rock_Desert_Cracked", 22),
                drop("Rock_Desert_Fossil", 18),
                drop("Block_Desert_Dried_Mud", 18),
                drop("Block_Sand_Red", 15),
                drop("Ore_Thorium_Desert", 6),
                drop("Ingredient_Fire_Essence", 3)
        ));

        register(expeditions, "Fungi_Cavern", List.of(
                drop("Plant_Crop_Mushroom_Block_Green", 15),
                drop("Plant_Crop_Mushroom_Block_Purple", 15),
                drop("Plant_Crop_Mushroom_Block_White", 14),
                drop("Plant_Crop_Mushroom_Block_Yellow", 14),
                drop("Plant_Crop_Mushroom_Cap_Green", 10),
                drop("Plant_Crop_Mushroom_Cap_Poison", 8),
                drop("Plant_Crop_Mushroom_Common_Blue", 8),
                drop("Plant_Crop_Mushroom_Common_Lime", 8),
                drop("Plant_Crop_Mushroom_Flatcap_Blue", 6),
                drop("Plant_Crop_Mushroom_Flatcap_Green", 6)
        ));

        register(expeditions, "Poison_Grove", List.of(
                drop("Wood_Poisoned_Trunk", 22),
                drop("Plant_Fruit_Poison", 18),
                drop("Plant_Leaves_Poisoned_Floor", 18),
                drop("Plant_Toxic_Jungle_Vine", 16),
                drop("Plant_Vine", 12),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Redwood_Grove", List.of(
                drop("Wood_Redwood_Trunk", 22),
                drop("Wood_Cedar_Trunk", 18),
                drop("Wood_Fir_Trunk", 18),
                drop("Wood_Camphor_Trunk", 16),
                drop("Ingredient_Tree_Bark", 14),
                drop("Ingredient_Tree_Sap", 10),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Frozen_Forest", List.of(
                drop("Wood_Ice_Trunk", 25),
                drop("Block_Snow", 18),
                drop("Block_Permafrost", 16),
                drop("Plant_Frost_Fern", 14),
                drop("Plant_Ice_Flower_Blue", 12),
                drop("Plant_Ice_Flower_White", 10),
                drop("Ingredient_Ice_Essence", 2)
        ));

        register(expeditions, "Ancient_Graveyard", List.of(
                drop("Rock_Stone_Mossy", 22),
                drop("Rock_Slate", 18),
                drop("Rock_Basalt", 15),
                drop("Ingredient_Bone_Fragment", 12),
                drop("Plant_Roots_Leafy", 10),
                drop("Ingredient_Void_Essence", 3)
        ));

        register(expeditions, "Azure_Forest", List.of(
                drop("Wood_Azure_Trunk", 22),
                drop("Plant_Crop_Mushroom_Block_Blue", 16),
                drop("Plant_Crop_Mushroom_Block_Yellow", 14),
                drop("Plant_Fruit_Azure", 12),
                drop("Wood_Crystal_Trunk", 10),
                drop("Plant_Ice_Flower_Blue", 10),
                drop("Ingredient_Ice_Essence", 3)
        ));

        register(expeditions, "Mystical_Forest", List.of(
                drop("Wood_Crystal_Trunk", 18),
                drop("Wood_Spiral_Trunk", 16),
                drop("Wood_Wisteria_Wild_Trunk", 16),
                drop("Plant_Fruit_Spiral", 14),
                drop("Plant_Fruit_Windwillow", 12),
                drop("Plant_Roots_Leafy", 10),
                drop("Ingredient_Motes_Light", 3)
        ));

        register(expeditions, "Storm_Wilds", List.of(
                drop("Wood_Stormbark_Trunk", 22),
                drop("Wood_Windwillow_Trunk", 20),
                drop("Plant_Fruit_Windwillow", 15),
                drop("Plant_Roots_Leafy", 14),
                drop("Ingredient_Lightning_Essence", 3)
        ));

        register(expeditions, "Ancient_Grove", List.of(
                drop("Wood_Petrified_Trunk", 22),
                drop("Wood_Gumboab_Trunk", 18),
                drop("Wood_Palo_Trunk", 16),
                drop("Wood_Dry_Trunk", 14),
                drop("Plant_Bush", 12),
                drop("Rock_Desert_Fossil", 8),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Deep_Cave", List.of(
                drop("Rock_Basalt", 25),
                drop("Rubble_Basalt", 18),
                drop("Rock_Slate", 18),
                drop("Ore_Iron", 14),
                drop("Ore_Silver", 8),
                drop("Plant_Roots_Cave", 10),
                drop("Ingredient_Void_Essence", 3)
        ));

        register(expeditions, "Deep_Mine", List.of(
                drop("Ore_Mithril", 16),
                drop("Ore_Cobalt", 14),
                drop("Ore_Gold", 14),
                drop("Ore_Silver", 12),
                drop("Rock_Volcanic", 15),
                drop("Rock_Slate", 12)
        ));

        register(expeditions, "Frozen_Cave", List.of(
                drop("Block_Ice_Blue", 25),
                drop("Rock_Ice", 20),
                drop("Rock_Ice_Permafrost", 18),
                drop("Block_Ice_Frosted", 15),
                drop("Rock_Frozen_Stone", 12),
                drop("Ingredient_Ice_Essence", 3)
        ));

        register(expeditions, "Glowing_Cavern", List.of(
                drop("Plant_Crop_Mushroom_Glowing_Blue", 15),
                drop("Plant_Crop_Mushroom_Glowing_Green", 15),
                drop("Plant_Crop_Mushroom_Glowing_Purple", 14),
                drop("Plant_Crop_Mushroom_Glowing_Red", 12),
                drop("Plant_Crop_Mushroom_Glowing_Orange", 12),
                drop("Plant_Crop_Mushroom_Glowing_Violet", 12),
                drop("Plant_Roots_Leafy", 10),
                drop("Ingredient_Motes_Light", 3)
        ));

        register(expeditions, "Dino_Caverns", List.of(
                drop("Block_Primal_Stone", 22),
                drop("Block_Fossil_Stone", 18),
                drop("Block_Amber", 14),
                drop("Block_Lava_Rock_Cracked", 14),
                drop("Rock_Amber_Encased", 10),
                drop("Rock_Volcanic", 12),
                drop("Ingredient_Life_Essence", 2)
        ));

        register(expeditions, "Mystic_Seeds", List.of(
                drop("Plant_Seeds_Health1", 12),
                drop("Plant_Seeds_Health2", 10),
                drop("Plant_Seeds_Health3", 8),
                drop("Plant_Seeds_Mana1", 12),
                drop("Plant_Seeds_Mana2", 10),
                drop("Plant_Seeds_Mana3", 8),
                drop("Plant_Seeds_Stamina1", 12),
                drop("Plant_Seeds_Stamina2", 10),
                drop("Plant_Seeds_Stamina3", 8),
                drop("Plant_Seeds_Mushroom", 6),
                drop("Plant_Seeds_Wild", 4)
        ));

        register(expeditions, "Amber_Dig", List.of(
                drop("Rock_Amber_Encased", 22),
                drop("Rock_Dinosaur_Fossil", 20),
                drop("Ingredient_Dinosaur_Bone", 16),
                drop("Ore_Primal_Iron", 12),
                drop("Ingredient_Primal_Core", 8),
                drop("Block_Fossil_Stone", 12)
        ));

        register(expeditions, "Fiery_Forest", List.of(
                drop("Wood_Burnt_Trunk", 22),
                drop("Wood_Fire_Trunk", 18),
                drop("Rock_Volcanic", 16),
                drop("Ingredient_Charcoal", 14),
                drop("Plant_Fruit_Poison", 8),
                drop("Ingredient_Fire_Essence", 3)
        ));

        register(expeditions, "Inferno", List.of(
                drop("Rock_Volcanic", 28),
                drop("Block_Volcanic_Ash", 20),
                drop("Block_Lava_Rock_Cracked", 16),
                drop("Ingredient_Charcoal", 14),
                drop("Ingredient_Powder_Boom", 6),
                drop("Ingredient_Sinue_Cindersinue", 5),
                drop("Ingredient_Fire_Essence", 3)
        ));

        register(expeditions, "Volcanic_Badlands", List.of(
                drop("Block_Volcanic_Ash", 25),
                drop("Rock_Volcanic", 22),
                drop("Block_Lava_Rock_Cracked", 18),
                drop("Ore_Thorium", 8),
                drop("Ingredient_Charcoal", 12),
                drop("Ingredient_Fire_Essence", 3)
        ));

        register(expeditions, "Cursed_Crypt", List.of(
                drop("Rock_Basalt", 22),
                drop("Rock_Slate", 18),
                drop("Rock_Volcanic", 15),
                drop("Ingredient_Bone_Fragment", 12),
                drop("Ingredient_Voidheart", 5),
                drop("Ingredient_Void_Essence", 4)
        ));

        register(expeditions, "Glacial_Depths", List.of(
                drop("Rock_Glacial_Crystal", 22),
                drop("Rock_Frozen_Stone", 20),
                drop("Block_Ice_Blue", 18),
                drop("Ore_Cobalt_Ice", 12),
                drop("Rock_Ice_Permafrost", 14),
                drop("Ingredient_Ice_Essence", 4)
        ));

        register(expeditions, "Crystal_Depths", List.of(
                drop("Rock_Gem_Diamond", 8),
                drop("Rock_Gem_Zephyr", 6),
                drop("Ore_Prisma", 10),
                drop("Rock_Volcanic", 20),
                drop("Rock_Basalt", 18),
                drop("Ingredient_Void_Essence", 4),
                drop("Ingredient_Motes_Light", 4)
        ));

        register(expeditions, "Advanced_Textile", List.of(
                drop("Ingredient_Bolt_Silk", 16),
                drop("Ingredient_Bolt_Shadoweave", 14),
                drop("Ingredient_Bolt_Stormsilk", 14),
                drop("Ingredient_Bolt_Cindercloth", 12),
                drop("Ingredient_Bolt_Prismaloom", 8),
                drop("Ingredient_Fabric_Scrap_Silk", 12),
                drop("Ingredient_Fabric_Scrap_Shadoweave", 10),
                drop("Ingredient_Fabric_Scrap_Stormsilk", 10),
                drop("Ingredient_Fabric_Scrap_Cindercloth", 8),
                drop("Ingredient_Fabric_Scrap_Prismaloom", 6)
        ));

        register(expeditions, "The_Abyss", List.of(
                drop("Rock_Volcanic", 22),
                drop("Rock_Slate", 18),
                drop("Rock_Basalt", 15),
                drop("Ore_Onyxium", 8),
                drop("Ingredient_Void_Essence", 4),
                drop("Ingredient_Voidheart", 4)
        ));

        register(expeditions, "Deep_Inferno", List.of(
                drop("Block_Lava_Rock_Cracked", 25),
                drop("Rock_Volcanic", 22),
                drop("Ore_Adamantite", 8),
                drop("Ingredient_Sinue_Cindersinue", 8),
                drop("Ingredient_Fire_Essence", 4),
                drop("Ingredient_Void_Essence", 3)
        ));

        register(expeditions, "Abyssal_Mine", List.of(
                drop("Ore_Adamantite", 14),
                drop("Ore_Onyxium", 12),
                drop("Ore_Prisma", 10),
                drop("Ore_Thorium", 12),
                drop("Rock_Volcanic", 16),
                drop("Ingredient_Void_Essence", 4),
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
