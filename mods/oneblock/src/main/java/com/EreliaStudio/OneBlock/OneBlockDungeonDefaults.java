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
                        List.of("entity:Spider", "entity:Spider", "entity:Spider_Cave")
                ), List.of(
                reward("ExpeditionPoint", 8)
        ));

        register(dungeons, "GoblinGank", List.of(
                        List.of("entity:Goblin_Scrapper", "entity:Goblin_Scrapper"),
                        List.of("entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Miner"),
                        List.of("entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Scrapper", "entity:Goblin_Miner", "entity:Goblin_Miner")
                ), List.of(
                reward("ExpeditionPoint", 12)
        ));

        register(dungeons, "GoblinInvasion", List.of(
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
                        List.of("entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Spider_Cave", "entity:Spider_Cave", "entity:Spider_Cave"),
                        List.of("entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Spider_Cave", "entity:Spider_Cave", "entity:Spider_Cave", "entity:Spider_Cave"),
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
                        List.of("entity:Larva_Void", "entity:Larva_Void", "entity:Larva_Void", "entity:Larva_Void", "entity:Larva_Void", "entity:Larva_Void", "entity:Crawler_Void", "entity:Crawler_Void"),
                        List.of("entity:Crawler_Void", "entity:Crawler_Void", "entity:Crawler_Void", "entity:Crawler_Void", "entity:Crawler_Void", "entity:Eye_Void", "entity:Eye_Void"),
                        List.of("entity:Spawn_Void", "entity:Spawn_Void", "entity:Spawn_Void", "entity:Spawn_Void", "entity:Wraith", "entity:Wraith"),
                        List.of("entity:Spawn_Void", "entity:Spawn_Void", "entity:Spawn_Void", "entity:Spawn_Void", "entity:Spawn_Void", "entity:Spawn_Void", "entity:Wraith", "entity:Wraith", "entity:Wraith", "entity:Wraith", "entity:Eye_Void", "entity:Eye_Void", "entity:Eye_Void")
                ), List.of(
                reward("ExpeditionPoint", 75)
        ));

        register(dungeons, "OutlanderGank", List.of(
                        List.of("entity:Outlander_Cultist", "entity:Outlander_Cultist", "entity:Outlander_Cultist", "entity:Outlander_Cultist", "entity:Outlander_Hunter"),
                        List.of("entity:Outlander_Cultist", "entity:Outlander_Cultist", "entity:Outlander_Cultist", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Marauder"),
                        List.of("entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder"),
                        List.of("entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Marauder", "entity:Outlander_Marauder")
                ), List.of(
                reward("ExpeditionPoint", 42)
        ));

        register(dungeons, "OutlanderCity", List.of(
                        List.of("entity:Outlander_Cultist", "entity:Outlander_Cultist", "entity:Outlander_Cultist", "entity:Outlander_Cultist", "entity:Outlander_Cultist", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Hunter"),
                        List.of("entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Berserker"),
                        List.of("entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Hunter", "entity:Outlander_Priest"),
                        List.of("entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Berserker", "entity:Outlander_Brute", "entity:Outlander_Priest"),
                        List.of("entity:Outlander_Brute", "entity:Outlander_Brute", "entity:Outlander_Priest", "entity:Outlander_Priest", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder", "entity:Outlander_Marauder")
                ), List.of(
                reward("ExpeditionPoint", 60)
        ));

        register(dungeons, "IceTemple", List.of(
                        List.of("entity:Zombie_Frost", "entity:Zombie_Frost", "entity:Zombie_Frost", "entity:Zombie_Frost", "entity:Skeleton_Frost_Scout", "entity:Skeleton_Frost_Scout"),
                        List.of("entity:Skeleton_Frost_Fighter", "entity:Skeleton_Frost_Fighter", "entity:Skeleton_Frost_Fighter", "entity:Skeleton_Frost_Fighter", "entity:Skeleton_Frost_Ranger", "entity:Skeleton_Frost_Ranger"),
                        List.of("entity:Golem_Crystal_Frost", "entity:Golem_Crystal_Frost", "entity:Skeleton_Frost_Mage", "entity:Skeleton_Frost_Mage", "entity:Skeleton_Frost_Mage"),
                        List.of("entity:Dragon_Frost", "entity:Skeleton_Frost_Knight", "entity:Skeleton_Frost_Knight", "entity:Skeleton_Frost_Knight", "entity:Skeleton_Frost_Knight", "entity:Zombie_Frost", "entity:Zombie_Frost", "entity:Zombie_Frost", "entity:Zombie_Frost", "entity:Zombie_Frost", "entity:Zombie_Frost")
                ), List.of(
                reward("ExpeditionPoint", 75)
        ));

        register(dungeons, "Volcano", List.of(
                        List.of("entity:Zombie_Burnt", "entity:Zombie_Burnt", "entity:Zombie_Burnt", "entity:Zombie_Burnt", "entity:Zombie_Burnt", "entity:Emberwulf", "entity:Emberwulf"),
                        List.of("entity:Skeleton_Burnt_Soldier", "entity:Skeleton_Burnt_Soldier", "entity:Skeleton_Burnt_Soldier", "entity:Skeleton_Burnt_Soldier", "entity:Skeleton_Burnt_Archer", "entity:Skeleton_Burnt_Archer", "entity:Skeleton_Burnt_Archer"),
                        List.of("entity:Skeleton_Burnt_Knight", "entity:Skeleton_Burnt_Knight", "entity:Skeleton_Burnt_Knight", "entity:Skeleton_Burnt_Wizard", "entity:Skeleton_Burnt_Wizard"),
                        List.of("entity:Golem_Crystal_Flame", "entity:Golem_Crystal_Flame", "entity:Skeleton_Burnt_Praetorian", "entity:Skeleton_Burnt_Praetorian", "entity:Skeleton_Burnt_Praetorian", "entity:Emberwulf", "entity:Emberwulf", "entity:Emberwulf", "entity:Emberwulf")
                ), List.of(
                reward("ExpeditionPoint", 55)
        ));

        register(dungeons, "DesertTemple", List.of(
                        List.of("entity:Zombie_Sand", "entity:Zombie_Sand", "entity:Zombie_Sand", "entity:Zombie_Sand", "entity:Skeleton_Scout", "entity:Skeleton_Scout"),
                        List.of("entity:Zombie_Sand", "entity:Zombie_Sand", "entity:Zombie_Sand", "entity:Zombie_Sand", "entity:Zombie_Sand", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer"),
                        List.of("entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Mage", "entity:Skeleton_Mage", "entity:Zombie_Sand", "entity:Zombie_Sand", "entity:Zombie_Sand", "entity:Zombie_Sand"),
                        List.of("entity:Skeleton_Sand_Archmage", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer")
                ), List.of(
                reward("ExpeditionPoint", 35),
                crystalReward("PharaohRoom", 1)
        ));

        register(dungeons, "InsectInvasion", List.of(
                        List.of("entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Spider", "entity:Spider", "entity:Spider"),
                        List.of("entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse"),
                        List.of("entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Spider_Cave", "entity:Spider_Cave", "entity:Spider_Cave", "entity:Spider_Cave", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter")
                ), List.of(
                reward("ExpeditionPoint", 26),
                crystalReward("MuddyDesert", 1)
        ));

        register(dungeons, "InsectNest", List.of(
                        List.of("entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Spider", "entity:Spider", "entity:Spider", "entity:Spider"),
                        List.of("entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Spider_Cave", "entity:Spider_Cave", "entity:Spider_Cave", "entity:Spider_Cave"),
                        List.of("entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Seeker", "entity:Scarak_Seeker"),
                        List.of("entity:Scarak_Broodmother", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter")
                ), List.of(
                reward("ExpeditionPoint", 36),
                crystalReward("InsideInsectNest", 1)
        ));

        register(dungeons, "InsectCore", List.of(
                        List.of("entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Louse", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter"),
                        List.of("entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Scarak_Seeker"),
                        List.of("entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Fighter", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Spider_Cave", "entity:Spider_Cave", "entity:Spider_Cave", "entity:Spider_Cave"),
                        List.of("entity:Scarak_Broodmother", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Defender", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Scarak_Seeker", "entity:Scarak_Seeker")
                ), List.of(
                reward("ExpeditionPoint", 50)
        ));

        register(dungeons, "DinoCrisis", List.of(
                        List.of("entity:Archaeopteryx", "entity:Archaeopteryx", "entity:Archaeopteryx", "entity:Archaeopteryx", "entity:Archaeopteryx", "entity:Crocodile", "entity:Crocodile"),
                        List.of("entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Archaeopteryx", "entity:Archaeopteryx", "entity:Archaeopteryx"),
                        List.of("entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Crawler", "entity:Crawler"),
                        List.of("entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Rex_Cave"),
                        List.of("entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Raptor_Cave", "entity:Rex_Cave", "entity:Rex_Cave"),
                        List.of("entity:Rex_Cave", "entity:Rex_Cave", "entity:Rex_Cave", "entity:Golem_Crystal_Earth")
                ), List.of(
                reward("ExpeditionPoint", 65)
        ));

        register(dungeons, "TrorkWarband", List.of(
                        List.of("entity:Wolf_Trork_Shaman", "entity:Wolf_Trork_Shaman", "entity:Wolf_Trork_Shaman", "entity:Trork_Sentry", "entity:Trork_Sentry"),
                        List.of("entity:Trork_Hunter", "entity:Trork_Hunter", "entity:Trork_Hunter", "entity:Trork_Brawler", "entity:Trork_Brawler"),
                        List.of("entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Mauler"),
                        List.of("entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Wolf_Trork_Shaman", "entity:Wolf_Trork_Shaman")
                ), List.of(
                reward("ExpeditionPoint", 32),
                crystalReward("TrorkStrongholdApproach", 1)
        ));

        register(dungeons, "TrorkChieftainCamp", List.of(
                        List.of("entity:Trork_Sentry", "entity:Trork_Sentry", "entity:Trork_Sentry", "entity:Trork_Sentry", "entity:Wolf_Trork_Shaman", "entity:Wolf_Trork_Shaman", "entity:Wolf_Trork_Shaman"),
                        List.of("entity:Trork_Brawler", "entity:Trork_Brawler", "entity:Trork_Brawler", "entity:Trork_Hunter", "entity:Trork_Hunter", "entity:Trork_Hunter"),
                        List.of("entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Mauler", "entity:Trork_Mauler"),
                        List.of("entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Doctor_Witch", "entity:Trork_Doctor_Witch"),
                        List.of("entity:Trork_Chieftain", "entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Guard", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior", "entity:Trork_Warrior")
                ), List.of(
                reward("ExpeditionPoint", 55)
        ));

        register(dungeons, "FrostboneCrypt", List.of(
                        List.of("entity:Skeleton_Frost_Scout", "entity:Skeleton_Frost_Scout", "entity:Skeleton_Frost_Scout", "entity:Skeleton_Frost_Scout", "entity:Skeleton_Frost_Ranger", "entity:Skeleton_Frost_Ranger"),
                        List.of("entity:Skeleton_Frost_Fighter", "entity:Skeleton_Frost_Fighter", "entity:Skeleton_Frost_Fighter", "entity:Skeleton_Frost_Fighter", "entity:Skeleton_Frost_Archer", "entity:Skeleton_Frost_Archer", "entity:Skeleton_Frost_Archer"),
                        List.of("entity:Skeleton_Frost_Soldier", "entity:Skeleton_Frost_Soldier", "entity:Skeleton_Frost_Soldier", "entity:Skeleton_Frost_Soldier", "entity:Skeleton_Frost_Mage", "entity:Skeleton_Frost_Mage", "entity:Skeleton_Frost_Mage"),
                        List.of("entity:Skeleton_Frost_Knight", "entity:Skeleton_Frost_Knight", "entity:Skeleton_Frost_Knight", "entity:Skeleton_Frost_Archmage", "entity:Skeleton_Frost_Archmage"),
                        List.of("entity:Skeleton_Frost_Knight", "entity:Skeleton_Frost_Knight", "entity:Skeleton_Frost_Knight", "entity:Skeleton_Frost_Knight", "entity:Skeleton_Frost_Archmage", "entity:Skeleton_Frost_Archmage", "entity:Skeleton_Frost_Archmage", "entity:Golem_Crystal_Frost")
                ), List.of(
                reward("ExpeditionPoint", 80),
                crystalReward("IceTemple", 1)
        ));

        register(dungeons, "BurntSkeletonCitadel", List.of(
                        List.of("entity:Skeleton_Burnt_Soldier", "entity:Skeleton_Burnt_Soldier", "entity:Skeleton_Burnt_Soldier", "entity:Skeleton_Burnt_Soldier", "entity:Skeleton_Burnt_Archer", "entity:Skeleton_Burnt_Archer", "entity:Skeleton_Burnt_Archer"),
                        List.of("entity:Skeleton_Burnt_Lancer", "entity:Skeleton_Burnt_Lancer", "entity:Skeleton_Burnt_Lancer", "entity:Skeleton_Burnt_Lancer", "entity:Skeleton_Burnt_Gunner", "entity:Skeleton_Burnt_Gunner", "entity:Skeleton_Burnt_Gunner"),
                        List.of("entity:Skeleton_Burnt_Knight", "entity:Skeleton_Burnt_Knight", "entity:Skeleton_Burnt_Knight", "entity:Skeleton_Burnt_Knight", "entity:Skeleton_Burnt_Wizard", "entity:Skeleton_Burnt_Wizard", "entity:Skeleton_Burnt_Wizard"),
                        List.of("entity:Skeleton_Burnt_Praetorian", "entity:Skeleton_Burnt_Praetorian", "entity:Skeleton_Burnt_Knight", "entity:Skeleton_Burnt_Knight", "entity:Skeleton_Burnt_Knight", "entity:Skeleton_Burnt_Knight", "entity:Skeleton_Burnt_Gunner", "entity:Skeleton_Burnt_Gunner", "entity:Skeleton_Burnt_Gunner"),
                        List.of("entity:Skeleton_Burnt_Praetorian", "entity:Skeleton_Burnt_Praetorian", "entity:Skeleton_Burnt_Praetorian", "entity:Skeleton_Burnt_Wizard", "entity:Skeleton_Burnt_Wizard", "entity:Skeleton_Burnt_Wizard", "entity:Skeleton_Burnt_Lancer", "entity:Skeleton_Burnt_Lancer", "entity:Skeleton_Burnt_Lancer", "entity:Skeleton_Burnt_Lancer")
                ), List.of(
                reward("ExpeditionPoint", 70),
                crystalReward("InfernalGate", 1)
        ));

        register(dungeons, "JungleCrypt", List.of(
                        List.of("entity:Zombie", "entity:Zombie", "entity:Zombie", "entity:Zombie", "entity:Zombie_Sand", "entity:Zombie_Sand", "entity:Zombie_Sand"),
                        List.of("entity:Skeleton_Scout", "entity:Skeleton_Scout", "entity:Skeleton_Scout", "entity:Skeleton_Scout", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer"),
                        List.of("entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Fighter", "entity:Skeleton_Fighter", "entity:Skeleton_Fighter"),
                        List.of("entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Skeleton_Mage", "entity:Skeleton_Mage")
                ), List.of(
                reward("ExpeditionPoint", 36),
                crystalReward("LostNecropolis", 1)
        ));

        register(dungeons, "AncientUndeadSanctum", List.of(
                        List.of("entity:Zombie", "entity:Zombie", "entity:Zombie", "entity:Zombie", "entity:Zombie_Aberrant", "entity:Zombie_Aberrant", "entity:Zombie_Aberrant", "entity:Zombie_Aberrant_Small", "entity:Zombie_Aberrant_Small", "entity:Zombie_Aberrant_Small", "entity:Zombie_Aberrant_Small"),
                        List.of("entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Soldier", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Archer", "entity:Skeleton_Fighter", "entity:Skeleton_Fighter", "entity:Skeleton_Fighter"),
                        List.of("entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Mage", "entity:Skeleton_Mage", "entity:Skeleton_Mage", "entity:Zombie_Aberrant_Big", "entity:Zombie_Aberrant_Big"),
                        List.of("entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Skeleton_Ranger", "entity:Ghoul", "entity:Ghoul", "entity:Ghoul", "entity:Ghoul"),
                        List.of("entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Zombie_Aberrant_Big", "entity:Zombie_Aberrant_Big", "entity:Zombie_Aberrant_Big", "entity:Wraith", "entity:Wraith")
                ), List.of(
                reward("ExpeditionPoint", 58)
        ));

        register(dungeons, "ShadowKnightCitadel", List.of(
                        List.of("entity:Horse_Skeleton", "entity:Horse_Skeleton", "entity:Horse_Skeleton", "entity:Pig_Undead", "entity:Pig_Undead", "entity:Pig_Undead", "entity:Pig_Undead", "entity:Chicken_Undead", "entity:Chicken_Undead", "entity:Chicken_Undead", "entity:Chicken_Undead", "entity:Chicken_Undead"),
                        List.of("entity:Horse_Skeleton_Armored", "entity:Horse_Skeleton_Armored", "entity:Horse_Skeleton_Armored", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Knight", "entity:Skeleton_Knight"),
                        List.of("entity:Wraith", "entity:Wraith", "entity:Wraith", "entity:Wraith", "entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Skeleton_Archmage"),
                        List.of("entity:Shadow_Knight", "entity:Shadow_Knight", "entity:Horse_Skeleton_Armored", "entity:Horse_Skeleton_Armored", "entity:Horse_Skeleton_Armored", "entity:Wraith", "entity:Wraith", "entity:Wraith"),
                        List.of("entity:Shadow_Knight", "entity:Shadow_Knight", "entity:Shadow_Knight", "entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Skeleton_Archmage", "entity:Horse_Skeleton_Armored", "entity:Horse_Skeleton_Armored", "entity:Horse_Skeleton_Armored", "entity:Horse_Skeleton_Armored")
                ), List.of(
                reward("ExpeditionPoint", 72)
        ));

        register(dungeons, "SpiritRealmTrial", List.of(
                        List.of("entity:Spirit_Root", "entity:Spirit_Root", "entity:Spirit_Root", "entity:Spirit_Root", "entity:Spirit_Ember", "entity:Spirit_Ember", "entity:Spirit_Ember", "entity:Spirit_Ember"),
                        List.of("entity:Spirit_Frost", "entity:Spirit_Frost", "entity:Spirit_Frost", "entity:Spirit_Frost", "entity:Spirit_Thunder", "entity:Spirit_Thunder", "entity:Spirit_Thunder", "entity:Spirit_Thunder"),
                        List.of("entity:Golem_Crystal_Earth", "entity:Golem_Crystal_Earth", "entity:Golem_Crystal_Sand", "entity:Golem_Crystal_Sand", "entity:Spirit_Root", "entity:Spirit_Root", "entity:Spirit_Root"),
                        List.of("entity:Golem_Crystal_Flame", "entity:Golem_Crystal_Flame", "entity:Golem_Firesteel", "entity:Golem_Firesteel", "entity:Spirit_Ember", "entity:Spirit_Ember", "entity:Spirit_Ember"),
                        List.of("entity:Golem_Crystal_Frost", "entity:Golem_Crystal_Frost", "entity:Golem_Crystal_Frost", "entity:Spirit_Frost", "entity:Spirit_Frost", "entity:Spirit_Frost", "entity:Spirit_Frost"),
                        List.of("entity:Golem_Crystal_Thunder", "entity:Golem_Crystal_Thunder", "entity:Golem_Crystal_Thunder", "entity:Spirit_Thunder", "entity:Spirit_Thunder", "entity:Spirit_Thunder", "entity:Spirit_Thunder"),
                        List.of("entity:Golem_Firesteel", "entity:Golem_Firesteel", "entity:Golem_Crystal_Frost", "entity:Golem_Crystal_Frost", "entity:Golem_Crystal_Thunder", "entity:Golem_Crystal_Thunder", "entity:Golem_Crystal_Flame", "entity:Golem_Crystal_Flame"),
                        List.of("entity:Golem_Crystal_Earth", "entity:Golem_Crystal_Earth", "entity:Golem_Crystal_Sand", "entity:Golem_Crystal_Sand", "entity:Golem_Firesteel", "entity:Golem_Firesteel", "entity:Golem_Crystal_Thunder", "entity:Golem_Crystal_Thunder")
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
