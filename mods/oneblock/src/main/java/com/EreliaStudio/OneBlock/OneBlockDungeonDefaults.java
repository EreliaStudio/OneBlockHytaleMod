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

        register(dungeons, "RatCave", List.of(
                        List.of("entity:Rat", "entity:Rat", "entity:Rat"),
                        List.of("entity:Rat", "entity:Rat", "entity:Spider"),
                        List.of("entity:Spider", "entity:Spider", "entity:Cave_Spider")
                ), List.of(
                reward("ExpeditionPoint", 8)
        ));

        register(dungeons, "GobelinGank", List.of(
                        List.of("entity:Goblin_Scrapper", "entity:Goblin_Scrapper"),
                        List.of("entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Miner"),
                        List.of("entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Miner", "entity:Goblin_Miner")
                ), List.of(
                reward("ExpeditionPoint", 12),
                crystalReward("IronCave", 1),
                crystalReward("SandCave", 1)
        ));

        register(dungeons, "GobelinInvasion", List.of(
                        List.of("entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Miner"),
                        List.of("entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Miner", "entity:Goblin_Miner"),
                        List.of("entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Miner", "entity:Goblin_Miner", "entity:Goblin_Miner"),
                        List.of("entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Miner", "entity:Goblin_Miner", "entity:Goblin_Miner")
                ), List.of(
                reward("ExpeditionPoint", 24),
                crystalReward("CobaltCave", 1)
        ));

        register(dungeons, "PirateShipwreck", List.of(
                        List.of("entity:Skeleton_Pirate_Striker", "entity:Skeleton_Pirate_Striker", "entity:Skeleton_Pirate_Striker", "entity:Skeleton_Pirate_Striker"),
                        List.of("entity:Skeleton_Pirate_Striker", "entity:Skeleton_Pirate_Striker", "entity:Skeleton_Pirate_Striker", "entity:Skeleton_Pirate_Gunner", "entity:Skeleton_Pirate_Gunner"),
                        List.of("entity:Skeleton_Pirate_Gunner", "entity:Skeleton_Pirate_Gunner", "entity:Skeleton_Pirate_Gunner", "entity:Skeleton_Pirate_Gunner", "entity:Skeleton_Pirate_Striker", "entity:Skeleton_Pirate_Striker", "entity:Skeleton_Pirate_Striker"),
                        List.of("entity:Skeleton_Pirate_Captain", "entity:Skeleton_Pirate_Gunner", "entity:Skeleton_Pirate_Gunner", "entity:Skeleton_Pirate_Gunner", "entity:Skeleton_Pirate_Striker", "entity:Skeleton_Pirate_Striker", "entity:Skeleton_Pirate_Striker", "entity:Skeleton_Pirate_Striker")
                ), List.of(
                reward("ExpeditionPoint", 30)
        ));

        register(dungeons, "SeaMonster", List.of(
                        List.of("entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Spider", "entity:Spider", "entity:Spider"),
                        List.of("entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Cave_Spider", "entity:Cave_Spider", "entity:Cave_Spider"),
                        List.of("entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Cave_Spider", "entity:Cave_Spider", "entity:Cave_Spider", "entity:Cave_Spider"),
                        List.of("entity:Scarak_Broodmother", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Scarak_Seeker")
                ), List.of(
                reward("ExpeditionPoint", 40)
        ));

        register(dungeons, "UndeadTemple", List.of(
                        List.of("entity:Skeleton_Scout", "entity:Skeleton_Scout", "entity:Skeleton_Scout", "entity:Zombie", "entity:Zombie"),
                        List.of("entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Archer", "entity:Skeleton_Archer"),
                        List.of("entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger"),
                        List.of("entity:Skeleton_Mage", "entity:Skeleton_Mage", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Zombie", "entity:Zombie", "entity:Zombie", "entity:Zombie")
                ), List.of(
                reward("ExpeditionPoint", 45),
                crystalReward("VoidPortal", 1)
        ));

        register(dungeons, "VoidTemple", List.of(
                        List.of("entity:Void_Larva", "entity:Void_Larva", "entity:Void_Larva", "entity:Void_Larva", "entity:Void_Larva", "entity:Void_Larva", "entity:Void_Crawler", "entity:Void_Crawler"),
                        List.of("entity:Void_Crawler", "entity:Void_Crawler", "entity:Void_Crawler", "entity:Void_Crawler", "entity:Void_Crawler", "entity:Void_Eye", "entity:Void_Eye"),
                        List.of("entity:Void_Spawn", "entity:Void_Spawn", "entity:Void_Spawn", "entity:Void_Spawn", "entity:Void_Spectre", "entity:Void_Spectre"),
                        List.of("entity:Void_Spawn", "entity:Void_Spawn", "entity:Void_Spawn", "entity:Void_Spawn", "entity:Void_Spawn", "entity:Void_Spawn", "entity:Void_Spectre", "entity:Void_Spectre", "entity:Void_Spectre", "entity:Void_Spectre", "entity:Void_Eye", "entity:Void_Eye", "entity:Void_Eye")
                ), List.of(
                reward("ExpeditionPoint", 75)
        ));

        register(dungeons, "OutlanderGank", List.of(
                        List.of("entity:Outlander_Initiate", "entity:Outlander_Initiate", "entity:Outlander_Initiate", "entity:Outlander_Initiate", "entity:Outlander_Hunter"),
                        List.of("entity:Outlander_Initiate", "entity:Outlander_Initiate", "entity:Outlander_Initiate", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Marauder"),
                        List.of("entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder"),
                        List.of("entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Marauder", "entity:Outlander_Marauder")
                ), List.of(
                reward("ExpeditionPoint", 42)
        ));

        register(dungeons, "OutlanderCity", List.of(
                        List.of("entity:Outlander_Initiate", "entity:Outlander_Initiate", "entity:Outlander_Initiate", "entity:Outlander_Initiate", "entity:Outlander_Initiate", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Hunter"),
                        List.of("entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Berserker"),
                        List.of("entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Priest"),
                        List.of("entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Brute", "entity:Outlander_Priest"),
                        List.of("entity:Outlander_Brute", "entity:Outlander_Brute", "entity:Outlander_Priest", "entity:Outlander_Priest", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder")
                ), List.of(
                reward("ExpeditionPoint", 60)
        ));

        register(dungeons, "IceTemple", List.of(
                        List.of("entity:Frost_Zombie", "entity:Frost_Zombie", "entity:Frost_Zombie", "entity:Frost_Zombie", "entity:Frost_Skeleton_Scout", "entity:Frost_Skeleton_Scout"),
                        List.of("entity:Frost_Skeleton_Fighter", "entity:Frost_Skeleton_Fighter", "entity:Frost_Skeleton_Fighter", "entity:Frost_Skeleton_Fighter", "entity:Frost_Skeleton_Ranger", "entity:Frost_Skeleton_Ranger"),
                        List.of("entity:Frost_Golem", "entity:Frost_Golem", "entity:Frost_Skeleton_Mage", "entity:Frost_Skeleton_Mage", "entity:Frost_Skeleton_Mage"),
                        List.of("entity:Ice_Dragon", "entity:Frost_Skeleton_Knight", "entity:Frost_Skeleton_Knight", "entity:Frost_Skeleton_Knight", "entity:Frost_Skeleton_Knight", "entity:Frost_Zombie", "entity:Frost_Zombie", "entity:Frost_Zombie", "entity:Frost_Zombie", "entity:Frost_Zombie", "entity:Frost_Zombie")
                ), List.of(
                reward("ExpeditionPoint", 75)
        ));

        register(dungeons, "Volcano", List.of(
                        List.of("entity:Zombie_Burnt", "entity:Zombie_Burnt", "entity:Zombie_Burnt", "entity:Zombie_Burnt", "entity:Zombie_Burnt", "entity:Emberwulf", "entity:Emberwulf"),
                        List.of("entity:Burnt_Skeleton_Soldier", "entity:Burnt_Skeleton_Soldier", "entity:Burnt_Skeleton_Soldier", "entity:Burnt_Skeleton_Soldier", "entity:Burnt_Skeleton_Archer", "entity:Burnt_Skeleton_Archer", "entity:Burnt_Skeleton_Archer"),
                        List.of("entity:Burnt_Skeleton_Knight", "entity:Burnt_Skeleton_Knight", "entity:Burnt_Skeleton_Knight", "entity:Burnt_Skeleton_Wizard", "entity:Burnt_Skeleton_Wizard"),
                        List.of("entity:Ember_Golem", "entity:Ember_Golem", "entity:Burnt_Skeleton_Praetorian", "entity:Burnt_Skeleton_Praetorian", "entity:Burnt_Skeleton_Praetorian", "entity:Emberwulf", "entity:Emberwulf", "entity:Emberwulf", "entity:Emberwulf")
                ), List.of(
                reward("ExpeditionPoint", 55)
        ));

        register(dungeons, "DesertTemple", List.of(
                        List.of("entity:Sandswept_Zombie", "entity:Sandswept_Zombie", "entity:Sandswept_Zombie", "entity:Sandswept_Zombie", "entity:Skeleton_Scout", "entity:Skeleton_Scout"),
                        List.of("entity:Sandswept_Zombie", "entity:Sandswept_Zombie", "entity:Sandswept_Zombie", "entity:Sandswept_Zombie", "entity:Sandswept_Zombie", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer"),
                        List.of("entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Mage", "entity:Skeleton_Mage", "entity:Sandswept_Zombie", "entity:Sandswept_Zombie", "entity:Sandswept_Zombie", "entity:Sandswept_Zombie"),
                        List.of("entity:Skeleton_Sand_Archmage", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer")
                ), List.of(
                reward("ExpeditionPoint", 35),
                crystalReward("PharaonRoom", 1)
        ));

        register(dungeons, "InsectInvasion", List.of(
                        List.of("entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Spider", "entity:Spider", "entity:Spider"),
                        List.of("entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse"),
                        List.of("entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Cave_Spider", "entity:Cave_Spider", "entity:Cave_Spider", "entity:Cave_Spider", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter")
                ), List.of(
                reward("ExpeditionPoint", 26),
                crystalReward("MuddyDesert", 1)
        ));

        register(dungeons, "InsectNest", List.of(
                        List.of("entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Spider", "entity:Spider", "entity:Spider", "entity:Spider"),
                        List.of("entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Cave_Spider", "entity:Cave_Spider", "entity:Cave_Spider", "entity:Cave_Spider"),
                        List.of("entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Seeker", "entity:Scarak_Seeker"),
                        List.of("entity:Sand_Empress", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter")
                ), List.of(
                reward("ExpeditionPoint", 36),
                crystalReward("InsideInsectNest", 1)
        ));

        register(dungeons, "InsectCore", List.of(
                        List.of("entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter"),
                        List.of("entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Scarak_Seeker"),
                        List.of("entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Cave_Spider", "entity:Cave_Spider", "entity:Cave_Spider", "entity:Cave_Spider"),
                        List.of("entity:Scarak_Broodmother", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Scarak_Seeker")
                ), List.of(
                reward("ExpeditionPoint", 50)
        ));

        register(dungeons, "DinoCrisis", List.of(
                        List.of("entity:Archaeopteryx", "entity:Archaeopteryx", "entity:Archaeopteryx", "entity:Archaeopteryx", "entity:Archaeopteryx", "entity:Crocodile", "entity:Crocodile"),
                        List.of("entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Archaeopteryx", "entity:Archaeopteryx", "entity:Archaeopteryx"),
                        List.of("entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Crawler", "entity:Crawler"),
                        List.of("entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Rex"),
                        List.of("entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Raptor", "entity:Cave_Rex", "entity:Cave_Rex"),
                        List.of("entity:Cave_Rex", "entity:Cave_Rex", "entity:Cave_Rex", "entity:Earth_Elemental")
                ), List.of(
                reward("ExpeditionPoint", 65)
        ));

        register(dungeons, "TrorkWarband", List.of(
                        List.of("entity:Hunting_Wolf", "entity:Hunting_Wolf", "entity:Hunting_Wolf", "entity:Trork_Sentry", "entity:Trork_Sentry"),
                        List.of("entity:Trork_Hunter", "entity:Trork_Hunter", "entity:Trork_Hunter", "entity:Trork_Brawler", "entity:Trork_Brawler"),
                        List.of("entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Mauler"),
                        List.of("entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Hunting_Wolf", "entity:Hunting_Wolf")
                ), List.of(
                reward("ExpeditionPoint", 32),
                crystalReward("TrorkStrongholdApproach", 1)
        ));

        register(dungeons, "TrorkChieftainCamp", List.of(
                        List.of("entity:Trork_Sentry", "entity:Trork_Sentry", "entity:Trork_Sentry", "entity:Trork_Sentry", "entity:Hunting_Wolf", "entity:Hunting_Wolf", "entity:Hunting_Wolf"),
                        List.of("entity:Trork_Brawler", "entity:Trork_Brawler", "entity:Trork_Brawler", "entity:Trork_Hunter", "entity:Trork_Hunter", "entity:Trork_Hunter"),
                        List.of("entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Mauler", "entity:Trork_Mauler"),
                        List.of("entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Elder", "entity:Trork_Elder"),
                        List.of("entity:Trork_Chieftain", "entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior")
                ), List.of(
                reward("ExpeditionPoint", 55)
        ));

        register(dungeons, "FrostboneCrypt", List.of(
                        List.of("entity:Frost_Skeleton_Scout", "entity:Frost_Skeleton_Scout", "entity:Frost_Skeleton_Scout", "entity:Frost_Skeleton_Scout", "entity:Frost_Skeleton_Ranger", "entity:Frost_Skeleton_Ranger"),
                        List.of("entity:Frost_Skeleton_Fighter", "entity:Frost_Skeleton_Fighter", "entity:Frost_Skeleton_Fighter", "entity:Frost_Skeleton_Fighter", "entity:Frost_Skeleton_Archer", "entity:Frost_Skeleton_Archer", "entity:Frost_Skeleton_Archer"),
                        List.of("entity:Frost_Skeleton_Soldier", "entity:Frost_Skeleton_Soldier", "entity:Frost_Skeleton_Soldier", "entity:Frost_Skeleton_Soldier", "entity:Frost_Skeleton_Mage", "entity:Frost_Skeleton_Mage", "entity:Frost_Skeleton_Mage"),
                        List.of("entity:Frost_Skeleton_Knight", "entity:Frost_Skeleton_Knight", "entity:Frost_Skeleton_Knight", "entity:Frost_Skeleton_Archmage", "entity:Frost_Skeleton_Archmage"),
                        List.of("entity:Frost_Skeleton_Knight", "entity:Frost_Skeleton_Knight", "entity:Frost_Skeleton_Knight", "entity:Frost_Skeleton_Knight", "entity:Frost_Skeleton_Archmage", "entity:Frost_Skeleton_Archmage", "entity:Frost_Skeleton_Archmage", "entity:Frost_Golem")
                ), List.of(
                reward("ExpeditionPoint", 80),
                crystalReward("IceTemple", 1)
        ));

        register(dungeons, "BurntSkeletonCitadel", List.of(
                        List.of("entity:Burnt_Skeleton_Soldier", "entity:Burnt_Skeleton_Soldier", "entity:Burnt_Skeleton_Soldier", "entity:Burnt_Skeleton_Soldier", "entity:Burnt_Skeleton_Archer", "entity:Burnt_Skeleton_Archer", "entity:Burnt_Skeleton_Archer"),
                        List.of("entity:Burnt_Skeleton_Lancer", "entity:Burnt_Skeleton_Lancer", "entity:Burnt_Skeleton_Lancer", "entity:Burnt_Skeleton_Lancer", "entity:Burnt_Skeleton_Gunner", "entity:Burnt_Skeleton_Gunner", "entity:Burnt_Skeleton_Gunner"),
                        List.of("entity:Burnt_Skeleton_Knight", "entity:Burnt_Skeleton_Knight", "entity:Burnt_Skeleton_Knight", "entity:Burnt_Skeleton_Knight", "entity:Burnt_Skeleton_Wizard", "entity:Burnt_Skeleton_Wizard", "entity:Burnt_Skeleton_Wizard"),
                        List.of("entity:Burnt_Skeleton_Praetorian", "entity:Burnt_Skeleton_Praetorian", "entity:Burnt_Skeleton_Knight", "entity:Burnt_Skeleton_Knight", "entity:Burnt_Skeleton_Knight", "entity:Burnt_Skeleton_Knight", "entity:Burnt_Skeleton_Gunner", "entity:Burnt_Skeleton_Gunner", "entity:Burnt_Skeleton_Gunner"),
                        List.of("entity:Burnt_Skeleton_Praetorian", "entity:Burnt_Skeleton_Praetorian", "entity:Burnt_Skeleton_Praetorian", "entity:Burnt_Skeleton_Wizard", "entity:Burnt_Skeleton_Wizard", "entity:Burnt_Skeleton_Wizard", "entity:Burnt_Skeleton_Lancer", "entity:Burnt_Skeleton_Lancer", "entity:Burnt_Skeleton_Lancer", "entity:Burnt_Skeleton_Lancer")
                ), List.of(
                reward("ExpeditionPoint", 70),
                crystalReward("HellGate", 1)
        ));

        register(dungeons, "JungleCrypt", List.of(
                        List.of("entity:Zombie", "entity:Zombie", "entity:Zombie", "entity:Zombie", "entity:Sandswept_Zombie", "entity:Sandswept_Zombie", "entity:Sandswept_Zombie"),
                        List.of("entity:Skeleton_Scout", "entity:Skeleton_Scout", "entity:Skeleton_Scout", "entity:Skeleton_Scout", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer"),
                        List.of("entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Fighter", "entity:Skeleton_Fighter", "entity:Skeleton_Fighter"),
                        List.of("entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Skeleton_Mage", "entity:Skeleton_Mage")
                ), List.of(
                reward("ExpeditionPoint", 36),
                crystalReward("LostNecropolis", 1)
        ));

        register(dungeons, "AncientUndeadSanctum", List.of(
                        List.of("entity:Zombie", "entity:Zombie", "entity:Zombie", "entity:Zombie", "entity:Aberrant_Zombie", "entity:Aberrant_Zombie", "entity:Aberrant_Zombie", "entity:Small_Aberrant_Zombie", "entity:Small_Aberrant_Zombie", "entity:Small_Aberrant_Zombie", "entity:Small_Aberrant_Zombie"),
                        List.of("entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Fighter", "entity:Skeleton_Fighter", "entity:Skeleton_Fighter"),
                        List.of("entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Mage", "entity:Skeleton_Mage", "entity:Skeleton_Mage", "entity:Large_Aberrant_Zombie", "entity:Large_Aberrant_Zombie"),
                        List.of("entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Ghoul", "entity:Ghoul", "entity:Ghoul", "entity:Ghoul"),
                        List.of("entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Large_Aberrant_Zombie", "entity:Large_Aberrant_Zombie", "entity:Large_Aberrant_Zombie", "entity:Wraith", "entity:Wraith")
                ), List.of(
                reward("ExpeditionPoint", 58)
        ));

        register(dungeons, "ShadowKnightCitadel", List.of(
                        List.of("entity:Skeleton_Horse", "entity:Skeleton_Horse", "entity:Skeleton_Horse", "entity:Undead_Pig", "entity:Undead_Pig", "entity:Undead_Pig", "entity:Undead_Pig", "entity:Undead_Chicken", "entity:Undead_Chicken", "entity:Undead_Chicken", "entity:Undead_Chicken", "entity:Undead_Chicken"),
                        List.of("entity:Armored_Skeleton_Horse", "entity:Armored_Skeleton_Horse", "entity:Armored_Skeleton_Horse", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Knight"),
                        List.of("entity:Wraith", "entity:Wraith", "entity:Wraith", "entity:Wraith", "entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Skeleton_Archmage"),
                        List.of("entity:Shadow_Knight", "entity:Shadow_Knight", "entity:Armored_Skeleton_Horse", "entity:Armored_Skeleton_Horse", "entity:Armored_Skeleton_Horse", "entity:Wraith", "entity:Wraith", "entity:Wraith"),
                        List.of("entity:Shadow_Knight", "entity:Shadow_Knight", "entity:Shadow_Knight", "entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Armored_Skeleton_Horse", "entity:Armored_Skeleton_Horse", "entity:Armored_Skeleton_Horse", "entity:Armored_Skeleton_Horse")
                ), List.of(
                reward("ExpeditionPoint", 72)
        ));

        register(dungeons, "SpiritRealmTrial", List.of(
                        List.of("entity:Root_Spirit", "entity:Root_Spirit", "entity:Root_Spirit", "entity:Root_Spirit", "entity:Ember_Spirit", "entity:Ember_Spirit", "entity:Ember_Spirit", "entity:Ember_Spirit"),
                        List.of("entity:Frost_Spirit", "entity:Frost_Spirit", "entity:Frost_Spirit", "entity:Frost_Spirit", "entity:Thunder_Spirit", "entity:Thunder_Spirit", "entity:Thunder_Spirit", "entity:Thunder_Spirit"),
                        List.of("entity:Earthen_Golem", "entity:Earthen_Golem", "entity:Sandswept_Golem", "entity:Sandswept_Golem", "entity:Root_Spirit", "entity:Root_Spirit", "entity:Root_Spirit"),
                        List.of("entity:Ember_Golem", "entity:Ember_Golem", "entity:Firesteel_Golem", "entity:Firesteel_Golem", "entity:Ember_Spirit", "entity:Ember_Spirit", "entity:Ember_Spirit"),
                        List.of("entity:Frost_Golem", "entity:Frost_Golem", "entity:Frost_Golem", "entity:Frost_Spirit", "entity:Frost_Spirit", "entity:Frost_Spirit", "entity:Frost_Spirit"),
                        List.of("entity:Thunder_Golem", "entity:Thunder_Golem", "entity:Thunder_Golem", "entity:Thunder_Spirit", "entity:Thunder_Spirit", "entity:Thunder_Spirit", "entity:Thunder_Spirit"),
                        List.of("entity:Firesteel_Golem", "entity:Firesteel_Golem", "entity:Frost_Golem", "entity:Frost_Golem", "entity:Thunder_Golem", "entity:Thunder_Golem", "entity:Ember_Golem", "entity:Ember_Golem"),
                        List.of("entity:Earthen_Golem", "entity:Earthen_Golem", "entity:Sandswept_Golem", "entity:Sandswept_Golem", "entity:Firesteel_Golem", "entity:Firesteel_Golem", "entity:Thunder_Golem", "entity:Thunder_Golem")
                ), List.of(
                reward("ExpeditionPoint", 95)
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

    private static CompletionRewardDefinition crystalReward(String dungeonId, int quantity)
    {
        return new CompletionRewardDefinition("Crystal_" + dungeonId, quantity);
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
