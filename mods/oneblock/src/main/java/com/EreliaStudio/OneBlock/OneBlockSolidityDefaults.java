package com.EreliaStudio.OneBlock;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generated per-block durability and tool requirements.
 *
 * <p>The static block is maintained by {@code tools/generate_expeditions.py}
 * from each expedition's {@code Solidity} object.</p>
 */
public final class OneBlockSolidityDefaults
{
    public enum RequiredTool
    {
        HAND,
        PICKAXE,
        AXE
    }

    public record SolidityDefinition(int ticks, RequiredTool requiredTool)
    {
        public SolidityDefinition
        {
            ticks = Math.max(1, ticks);
            requiredTool = requiredTool == null ? RequiredTool.HAND : requiredTool;
        }
    }

    private static final SolidityDefinition DEFAULT_DEFINITION =
            new SolidityDefinition(1, RequiredTool.HAND);
    private static final Map<String, SolidityDefinition> BY_BLOCK_ID;

    static
    {
        Map<String, SolidityDefinition> definitions = new HashMap<>();

        register(definitions, "Default", 3, RequiredTool.HAND);

        register(definitions, "CaveEntry", 3, RequiredTool.PICKAXE);

        register(definitions, "ForestEdge", 3, RequiredTool.AXE);

        register(definitions, "Plain", 3, RequiredTool.HAND);

        register(definitions, "Cave", 3, RequiredTool.PICKAXE);

        register(definitions, "RatCave", 3, RequiredTool.PICKAXE);

        register(definitions, "LowerCave", 3, RequiredTool.PICKAXE);

        register(definitions, "CopperCave", 3, RequiredTool.PICKAXE);

        register(definitions, "GoblinGank", 3, RequiredTool.HAND);

        register(definitions, "IronCave", 4, RequiredTool.PICKAXE);

        register(definitions, "SandCave", 3, RequiredTool.PICKAXE);

        register(definitions, "GoldCave", 4, RequiredTool.PICKAXE);

        register(definitions, "ThoriumCave", 4, RequiredTool.PICKAXE);

        register(definitions, "GoblinInvasion", 4, RequiredTool.HAND);

        register(definitions, "SilverCave", 4, RequiredTool.PICKAXE);

        register(definitions, "CobaltCave", 4, RequiredTool.PICKAXE);

        register(definitions, "AdamantiteCave", 5, RequiredTool.PICKAXE);

        register(definitions, "GemCave", 5, RequiredTool.PICKAXE);

        register(definitions, "FireCave", 5, RequiredTool.PICKAXE);

        register(definitions, "GemDeepCave", 5, RequiredTool.PICKAXE);

        register(definitions, "MithrilCave", 6, RequiredTool.PICKAXE);

        register(definitions, "OnyxiumCave", 6, RequiredTool.PICKAXE);

        register(definitions, "PrismaCave", 8, RequiredTool.PICKAXE);

        register(definitions, "SandCavern", 4, RequiredTool.PICKAXE);

        register(definitions, "Desert", 4, RequiredTool.HAND);

        register(definitions, "ForestEntry", 3, RequiredTool.AXE);

        register(definitions, "Pond", 3, RequiredTool.HAND);

        register(definitions, "River", 3, RequiredTool.HAND);

        register(definitions, "Lake", 3, RequiredTool.HAND);

        register(definitions, "Sea", 4, RequiredTool.HAND);

        register(definitions, "PirateShipwreck", 4, RequiredTool.AXE);

        register(definitions, "Coastline", 4, RequiredTool.HAND);

        register(definitions, "SeaCavern", 4, RequiredTool.PICKAXE);

        register(definitions, "SeaInfestedNest", 5, RequiredTool.PICKAXE);

        register(definitions, "SeaMonster", 5, RequiredTool.HAND);

        register(definitions, "Forest", 3, RequiredTool.AXE);

        register(definitions, "AridForest", 3, RequiredTool.AXE);

        register(definitions, "Swamp", 3, RequiredTool.HAND);

        register(definitions, "EnchantedForest", 4, RequiredTool.AXE);

        register(definitions, "FairyPond", 4, RequiredTool.HAND);

        register(definitions, "DeepForest", 4, RequiredTool.AXE);

        register(definitions, "DarkForest", 4, RequiredTool.AXE);

        register(definitions, "BurnedForest", 5, RequiredTool.AXE);

        register(definitions, "CursedForest", 5, RequiredTool.AXE);

        register(definitions, "Graveyard", 5, RequiredTool.PICKAXE);

        register(definitions, "UndeadTemple", 6, RequiredTool.PICKAXE);

        register(definitions, "VoidPortal", 6, RequiredTool.PICKAXE);

        register(definitions, "VoidTemple", 8, RequiredTool.PICKAXE);

        register(definitions, "OutlanderForest", 5, RequiredTool.AXE);

        register(definitions, "OutlanderPlain", 6, RequiredTool.HAND);

        register(definitions, "OutlanderGank", 6, RequiredTool.HAND);

        register(definitions, "OutlanderCity", 6, RequiredTool.HAND);

        register(definitions, "Tundra", 6, RequiredTool.HAND);

        register(definitions, "FrozenForest", 6, RequiredTool.AXE);

        register(definitions, "IceLand", 6, RequiredTool.HAND);

        register(definitions, "IcyCavern", 6, RequiredTool.PICKAXE);

        register(definitions, "YetiCavern", 8, RequiredTool.PICKAXE);

        register(definitions, "IceTemple", 8, RequiredTool.PICKAXE);

        register(definitions, "IcyForest", 6, RequiredTool.AXE);

        register(definitions, "FireLand", 6, RequiredTool.HAND);

        register(definitions, "Volcano", 6, RequiredTool.PICKAXE);

        register(definitions, "FieryGraveyard", 6, RequiredTool.PICKAXE);

        register(definitions, "InfernalGate", 6, RequiredTool.PICKAXE);

        register(definitions, "InfernalPlain", 8, RequiredTool.HAND);

        register(definitions, "InfernalSwamp", 8, RequiredTool.HAND);

        register(definitions, "FireGemCave", 8, RequiredTool.PICKAXE);

        register(definitions, "DesertTempleEntrance", 4, RequiredTool.PICKAXE);

        register(definitions, "DeeperDesertTemple", 5, RequiredTool.PICKAXE);

        register(definitions, "DesertTemple", 5, RequiredTool.PICKAXE);

        register(definitions, "PharaohRoom", 5, RequiredTool.PICKAXE);

        register(definitions, "InnerDesert", 4, RequiredTool.HAND);

        register(definitions, "MuddyDesert", 4, RequiredTool.HAND);

        register(definitions, "InsectInvasion", 4, RequiredTool.HAND);

        register(definitions, "InfestedDesert", 5, RequiredTool.HAND);

        register(definitions, "InsectNest", 5, RequiredTool.PICKAXE);

        register(definitions, "InsideInsectNest", 5, RequiredTool.PICKAXE);

        register(definitions, "InsectCore", 6, RequiredTool.PICKAXE);

        register(definitions, "Quarry", 3, RequiredTool.PICKAXE);

        register(definitions, "Hallow", 3, RequiredTool.HAND);

        register(definitions, "CowHallow", 3, RequiredTool.HAND);

        register(definitions, "HorseHallow", 3, RequiredTool.HAND);

        register(definitions, "MysteriousCavern", 4, RequiredTool.PICKAXE);

        register(definitions, "MysticCave", 4, RequiredTool.PICKAXE);

        register(definitions, "LuxuriousCave", 5, RequiredTool.PICKAXE);

        register(definitions, "JurassicCave", 6, RequiredTool.PICKAXE);

        register(definitions, "DinoCrisis", 8, RequiredTool.HAND);

        register(definitions, "DryTrorkCamp", 4, RequiredTool.AXE);

        register(definitions, "TrorkHuntingGround", 4, RequiredTool.AXE);

        register(definitions, "TrorkWarband", 5, RequiredTool.AXE);

        register(definitions, "TrorkStrongholdApproach", 5, RequiredTool.AXE);

        register(definitions, "TrorkElderGrove", 5, RequiredTool.AXE);

        register(definitions, "TrorkChieftainCamp", 6, RequiredTool.AXE);

        register(definitions, "FrozenGraveyard", 8, RequiredTool.PICKAXE);

        register(definitions, "IcyNecropolis", 8, RequiredTool.PICKAXE);

        register(definitions, "FrostboneCrypt", 8, RequiredTool.PICKAXE);

        register(definitions, "BurntBattlefield", 6, RequiredTool.HAND);

        register(definitions, "AshenCatacombs", 6, RequiredTool.PICKAXE);

        register(definitions, "BurntSkeletonCitadel", 8, RequiredTool.PICKAXE);

        register(definitions, "JungleEdge", 4, RequiredTool.AXE);

        register(definitions, "DryJunglePass", 4, RequiredTool.AXE);

        register(definitions, "OvergrownRuins", 5, RequiredTool.PICKAXE);

        register(definitions, "SunkenJungleRuins", 5, RequiredTool.PICKAXE);

        register(definitions, "LostNecropolis", 5, RequiredTool.PICKAXE);

        register(definitions, "ShadowedJungleRoad", 6, RequiredTool.AXE);

        register(definitions, "ArmoredDeadGrove", 6, RequiredTool.AXE);

        register(definitions, "JungleCrypt", 5, RequiredTool.PICKAXE);

        register(definitions, "AncientUndeadSanctum", 6, RequiredTool.PICKAXE);

        register(definitions, "ShadowKnightCitadel", 8, RequiredTool.PICKAXE);

        register(definitions, "SpiritThreshold", 8, RequiredTool.HAND);

        register(definitions, "ElementalConfluence", 8, RequiredTool.HAND);

        register(definitions, "SpiritRealmTrial", 8, RequiredTool.HAND);

        register(definitions, "CrystalCavern", 4, RequiredTool.PICKAXE);

        BY_BLOCK_ID = Collections.unmodifiableMap(definitions);
    }

    private OneBlockSolidityDefaults() {}

    public static SolidityDefinition get(String blockId)
    {
        if (blockId == null || blockId.isBlank()) return DEFAULT_DEFINITION;
        return BY_BLOCK_ID.getOrDefault(blockId, DEFAULT_DEFINITION);
    }

    private static void register(Map<String, SolidityDefinition> definitions,
                                 String expeditionId,
                                 int ticks,
                                 RequiredTool requiredTool)
    {
        String safeId = expeditionId.replace(' ', '_');
        definitions.put(
                "OneBlock_Block_" + safeId,
                new SolidityDefinition(ticks, requiredTool)
        );
    }
}
