#!/usr/bin/env python3
"""Generate complete French and Slovak OneBlock language catalogues."""

import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
LANGUAGES = ROOT / "mods/oneblock/src/main/resources/Server/Languages"
SOURCE = LANGUAGES / "en-US/server.lang"


NAME_TABLE = """
Default|Par défaut|Predvolená
CaveEntry|Entrée de la grotte|Vstup do jaskyne
ForestEdge|Lisière de la forêt|Okraj lesa
Plain|Plaine|Planina
Cave|Grotte|Jaskyňa
RatCave|Grotte aux rats|Potkania jaskyňa
LowerCave|Grotte inférieure|Dolná jaskyňa
CopperCave|Mine de cuivre|Medená jaskyňa
GoblinGank|Embuscade gobeline|Gobliní prepad
IronCave|Mine de fer|Železná jaskyňa
SandCave|Grotte de sable|Piesočná jaskyňa
GoldCave|Mine d’or|Zlatá jaskyňa
ThoriumCave|Mine de thorium|Tóriová jaskyňa
GoblinInvasion|Invasion gobeline|Goblinia invázia
SilverCave|Mine d’argent|Strieborná jaskyňa
CobaltCave|Mine de cobalt|Kobaltová jaskyňa
AdamantiteCave|Mine d’adamantite|Adamantitová jaskyňa
GemCave|Grotte aux gemmes|Jaskyňa drahokamov
FireCave|Grotte ardente|Ohnivá jaskyňa
GemDeepCave|Grotte profonde aux gemmes|Hlboká jaskyňa drahokamov
MithrilCave|Mine de mithril|Mithrilová jaskyňa
OnyxiumCave|Mine d’onyxium|Onýxiová jaskyňa
PrismaCave|Mine de prisma|Prizmatická jaskyňa
SandCavern|Caverne de sable|Piesočná kaverna
Desert|Désert|Púšť
ForestEntry|Entrée de la forêt|Vstup do lesa
Pond|Étang|Rybník
River|Rivière|Rieka
Lake|Lac|Jazero
Sea|Mer|More
PirateShipwreck|Épave pirate|Pirátsky vrak
Coastline|Littoral|Pobrežie
SeaCavern|Caverne marine|Morská kaverna
SeaInfestedNest|Nid marin infesté|Zamorené morské hniezdo
SeaMonster|Monstre marin|Morská príšera
Forest|Forêt|Les
AridForest|Forêt aride|Vyprahnutý les
Swamp|Marais|Močiar
EnchantedForest|Forêt enchantée|Začarovaný les
FairyPond|Étang féerique|Vílí rybník
DeepForest|Forêt profonde|Hlboký les
DarkForest|Forêt sombre|Temný les
BurnedForest|Forêt brûlée|Spálený les
CursedForest|Forêt maudite|Prekliaty les
Graveyard|Cimetière|Cintorín
UndeadTemple|Temple des morts-vivants|Chrám nemŕtvych
VoidPortal|Portail du Vide|Portál Prázdnoty
VoidTemple|Temple du Vide|Chrám Prázdnoty
OutlanderForest|Forêt des Exilés|Les Vyhnancov
OutlanderPlain|Plaine des Exilés|Planina Vyhnancov
OutlanderGank|Embuscade des Exilés|Prepad Vyhnancov
OutlanderCity|Cité des Exilés|Mesto Vyhnancov
Tundra|Toundra|Tundra
FrozenForest|Forêt gelée|Zamrznutý les
IceLand|Terres de glace|Ľadová krajina
IcyCavern|Caverne glacée|Ľadová kaverna
YetiCavern|Caverne du yéti|Yetiho kaverna
IceTemple|Temple de glace|Ľadový chrám
IcyForest|Forêt glacée|Ľadový les
FireLand|Terres de feu|Ohnivá krajina
Volcano|Volcan|Sopka
FieryGraveyard|Cimetière enflammé|Ohnivý cintorín
InfernalGate|Porte infernale|Pekelná brána
InfernalPlain|Plaine infernale|Pekelná planina
InfernalSwamp|Marais infernal|Pekelný močiar
FireGemCave|Grotte aux gemmes de feu|Jaskyňa ohnivých drahokamov
DesertTempleEntrance|Entrée du temple du désert|Vstup do púštneho chrámu
DeeperDesertTemple|Profondeurs du temple du désert|Hlbiny púštneho chrámu
DesertTemple|Temple du désert|Púštny chrám
PharaohRoom|Salle du pharaon|Faraónova komnata
InnerDesert|Désert intérieur|Vnútorná púšť
MuddyDesert|Désert boueux|Bahnitá púšť
InsectInvasion|Invasion d’insectes|Hmyzia invázia
InfestedDesert|Désert infesté|Zamorená púšť
InsectNest|Nid d’insectes|Hmyzie hniezdo
InsideInsectNest|Intérieur du nid d’insectes|Vnútro hmyzieho hniezda
InsectCore|Cœur de la colonie|Jadro hniezda
Quarry|Carrière|Lom
Hallow|Lieu sacré|Posvätné miesto
CowHallow|Sanctuaire des vaches|Útočisko kráv
HorseHallow|Sanctuaire des chevaux|Útočisko koní
MysteriousCavern|Caverne mystérieuse|Tajomná kaverna
MysticCave|Grotte mystique|Mystická jaskyňa
LuxuriousCave|Grotte luxueuse|Prepychová jaskyňa
JurassicCave|Grotte jurassique|Jurská jaskyňa
DinoCrisis|Crise des dinosaures|Dinosauří kríza
DryTrorkCamp|Camp Trork aride|Suchý tábor Trorkov
TrorkHuntingGround|Territoire de chasse Trork|Lovisko Trorkov
TrorkWarband|Bande de guerre Trork|Bojová družina Trorkov
TrorkStrongholdApproach|Approche de la forteresse Trork|Prístup k pevnosti Trorkov
TrorkElderGrove|Bosquet des anciens Trorks|Háj starších Trorkov
TrorkChieftainCamp|Camp du chef Trork|Tábor náčelníka Trorkov
FrozenGraveyard|Cimetière gelé|Zamrznutý cintorín
IcyNecropolis|Nécropole glacée|Ľadová nekropola
FrostboneCrypt|Crypte des os gelés|Krypta mrazivých kostí
BurntBattlefield|Champ de bataille calciné|Spálené bojisko
AshenCatacombs|Catacombes cendrées|Popolavé katakomby
BurntSkeletonCitadel|Citadelle des squelettes calcinés|Citadela spálených kostlivcov
JungleEdge|Lisière de la jungle|Okraj džungle
DryJunglePass|Col aride de la jungle|Suchý priesmyk džungľou
OvergrownRuins|Ruines envahies|Zarastené ruiny
SunkenJungleRuins|Ruines englouties de la jungle|Potopené ruiny džungle
LostNecropolis|Nécropole perdue|Stratená nekropola
ShadowedJungleRoad|Route ombragée de la jungle|Tienistá cesta džungľou
ArmoredDeadGrove|Bosquet des morts en armure|Háj obrnených mŕtvych
JungleCrypt|Crypte de la jungle|Krypta džungle
AncientUndeadSanctum|Ancien sanctuaire des morts-vivants|Starobylá svätyňa nemŕtvych
ShadowKnightCitadel|Citadelle du chevalier de l’ombre|Citadela tieňového rytiera
SpiritThreshold|Seuil des esprits|Prah duchov
ElementalConfluence|Confluence élémentaire|Sútok živlov
SpiritRealmTrial|Épreuve du royaume des esprits|Skúška ríše duchov
CrystalCavern|Caverne de cristal|Kryštálová kaverna
""".strip()


def name_maps():
    french = {}
    slovak = {}
    for row in NAME_TABLE.splitlines():
        expedition_id, fr_name, sk_name = row.split("|", 2)
        french[expedition_id] = fr_name
        slovak[expedition_id] = sk_name
    return french, slovak


FR_NAMES, SK_NAMES = name_maps()


COMMON_FR = {
    "items.Bench_OneBlockEnchanter.name": "Enchanteur de cristaux",
    "items.Bench_OneBlockEnchanter.description": "Fusionne les ressources récupérées pour créer des cristaux d’expédition.",
    "items.Bench_OneBlockDungeonEnchanter.name": "Enchanteur de cristaux de donjon",
    "items.Bench_OneBlockDungeonEnchanter.description": "Fusionne les ressources récupérées pour créer des cristaux d’expédition de donjon.",
    "items.Locket_GobelinDungeon.name": "Médaillon du donjon gobelin",
    "items.ExpeditionPoint.name": "Point d’expédition",
    "items.SwampRuinsToken.name": "Jeton des ruines du marais",
    "items.AbandonedMineToken.name": "Jeton de la mine abandonnée",
    "items.FrozenRuinsToken.name": "Jeton des ruines gelées",
    "items.DesertTempleToken.name": "Jeton du temple du désert",
    "items.PharaohCatacombsToken.name": "Jeton des catacombes du pharaon",
    "items.GobelinCampToken.name": "Jeton du camp gobelin",
}


COMMON_SK = {
    "items.Bench_OneBlockEnchanter.name": "Očarovač kryštálov",
    "items.Bench_OneBlockEnchanter.description": "Spája zachránené suroviny a vytvára z nich expedičné kryštály.",
    "items.Bench_OneBlockDungeonEnchanter.name": "Očarovač kryštálov žalárov",
    "items.Bench_OneBlockDungeonEnchanter.description": "Spája zachránené suroviny a vytvára z nich expedičné kryštály pre žaláre.",
    "items.Locket_GobelinDungeon.name": "Medailón goblinieho žalára",
    "items.ExpeditionPoint.name": "Expedičný bod",
    "items.SwampRuinsToken.name": "Žetón ruín močiara",
    "items.AbandonedMineToken.name": "Žetón opustenej bane",
    "items.FrozenRuinsToken.name": "Žetón zamrznutých ruín",
    "items.DesertTempleToken.name": "Žetón púštneho chrámu",
    "items.PharaohCatacombsToken.name": "Žetón faraónových katakomb",
    "items.GobelinCampToken.name": "Žetón goblinieho tábora",
}


CONFIGS = {
    "fr-FR": {
        "names": FR_NAMES,
        "common": COMMON_FR,
        "block": "OneBlock : {name}",
        "crystal": "Cristal : {name}",
        "exp_start": "L’expédition « {name} » a commencé.",
        "exp_done": "L’expédition « {name} » est terminée. Le OneBlock est revenu à son état par défaut.",
        "exp_unlock": "Nouvelle expédition débloquée : « {name} »",
        "dungeon_start": "Le donjon « {name} » a commencé.",
        "dungeon_done": "Le donjon « {name} » est terminé. Le OneBlock est revenu à son état par défaut.",
        "progress": "{name} : {{0}}/{{1}} blocs restants",
        "exp_desc": "Consommez ce cristal pour commencer l’expédition « {name} ».",
        "dungeon_desc": "Consommez ce cristal pour commencer le donjon « {name} ».",
        "ticks": "{count} blocs à briser.",
        "waves": "{count} vagues.",
        "headers": {
            "Possible drops:": "Butins possibles :",
            "Items:": "Objets :",
            "Entities:": "Entités :",
            "Completion rewards:": "Récompenses de fin :",
            "Leads to:": "Débloque :",
        },
        "possible": " (possible)",
        "point": "Point d’expédition",
        "benches": {
            "OneBlockEnchanter_Easy": "Cristaux faciles",
            "OneBlockDungeonEnchanter_Dungeon": "Cristaux de donjon",
            "OneBlockEnchanter_Advanced": "Cristaux avancés",
            "OneBlockEnchanter_Difficult": "Cristaux difficiles",
            "OneBlockEnchanter_Hard": "Cristaux redoutables",
            "OneBlockEnchanter_Expert": "Cristaux experts",
        },
    },
    "sk-SK": {
        "names": SK_NAMES,
        "common": COMMON_SK,
        "block": "OneBlock: {name}",
        "crystal": "Kryštál: {name}",
        "exp_start": "Expedícia „{name}“ sa začala.",
        "exp_done": "Expedícia „{name}“ je dokončená. OneBlock sa vrátil do predvoleného stavu.",
        "exp_unlock": "Odomknutá nová expedícia: „{name}“",
        "dungeon_start": "Žalár „{name}“ sa začal.",
        "dungeon_done": "Žalár „{name}“ je dokončený. OneBlock sa vrátil do predvoleného stavu.",
        "progress": "{name}: zostáva {{0}}/{{1}} blokov",
        "exp_desc": "Spotrebujte tento kryštál a začnite expedíciu „{name}“.",
        "dungeon_desc": "Spotrebujte tento kryštál a vstúpte do žalára „{name}“.",
        "ticks": "Počet blokov na rozbitie: {count}.",
        "waves": "Počet vĺn: {count}.",
        "headers": {
            "Possible drops:": "Možná korisť:",
            "Items:": "Predmety:",
            "Entities:": "Entity:",
            "Completion rewards:": "Odmeny za dokončenie:",
            "Leads to:": "Odomyká:",
        },
        "possible": " (možné)",
        "point": "Expedičný bod",
        "benches": {
            "OneBlockEnchanter_Easy": "Jednoduché kryštály",
            "OneBlockDungeonEnchanter_Dungeon": "Kryštály žalárov",
            "OneBlockEnchanter_Advanced": "Pokročilé kryštály",
            "OneBlockEnchanter_Difficult": "Náročné kryštály",
            "OneBlockEnchanter_Hard": "Ťažké kryštály",
            "OneBlockEnchanter_Expert": "Expertné kryštály",
        },
    },
}


def expedition_id(key, prefix, suffix):
    return key[len(prefix): -len(suffix)]


def translate_description(value, expedition, config):
    source_lines = value.split(r"\n")
    translated = []
    is_dungeon = " dungeon." in source_lines[0]
    translated.append(
        config["dungeon_desc" if is_dungeon else "exp_desc"].format(
            name=config["names"][expedition]
        )
    )

    section = None
    for line in source_lines[1:]:
        tick_match = re.fullmatch(r"(\d+) ticks\.", line)
        wave_match = re.fullmatch(r"(\d+) waves\.", line)
        if tick_match:
            translated.append(config["ticks"].format(count=tick_match.group(1)))
            continue
        if wave_match:
            translated.append(config["waves"].format(count=wave_match.group(1)))
            continue
        if line in config["headers"]:
            section = line
            translated.append(config["headers"][line])
            continue

        if line.startswith("- ") and section == "Leads to:":
            target = line[2:]
            possible = target.endswith(" (possible)")
            if possible:
                target = target[:-11]
            target_name = config["names"].get(target, target)
            translated.append("- " + target_name + (config["possible"] if possible else ""))
            continue

        if line.startswith("- ") and section == "Completion rewards:":
            translated.append(line.replace("ExpeditionPoint", config["point"]))
            continue

        # Hytale item and entity names are canonical asset names. Keeping them
        # unchanged makes the hard-coded drop list match the base game's names.
        translated.append(line)

    return r"\n".join(translated)


def translate_entry(key, value, config):
    if key in config["common"]:
        return config["common"][key]

    prefix = "items.OneBlock_Block_"
    if key.startswith(prefix) and key.endswith(".name"):
        expedition = expedition_id(key, prefix, ".name")
        return config["block"].format(name=config["names"][expedition])

    prefix = "items.OneBlock_Crystal_"
    if key.startswith(prefix) and key.endswith(".name"):
        expedition = expedition_id(key, prefix, ".name")
        return config["crystal"].format(name=config["names"][expedition])
    if key.startswith(prefix) and key.endswith(".description"):
        expedition = expedition_id(key, prefix, ".description")
        return translate_description(value, expedition, config)

    prefix = "expeditions."
    if key.startswith(prefix) and key.endswith(".name"):
        expedition = expedition_id(key, prefix, ".name")
        return config["names"][expedition]

    announcement_types = {
        "announcements.expedition_started.": "exp_start",
        "announcements.expedition_completed.": "exp_done",
        "announcements.expedition_unlocked.": "exp_unlock",
        "announcements.dungeon_started.": "dungeon_start",
        "announcements.dungeon_completed.": "dungeon_done",
    }
    for announcement_prefix, template in announcement_types.items():
        if key.startswith(announcement_prefix):
            expedition = key[len(announcement_prefix):]
            return config[template].format(name=config["names"][expedition])

    prefix = "progress.expedition."
    if key.startswith(prefix):
        expedition = key[len(prefix):]
        return config["progress"].format(name=config["names"][expedition])

    prefix = "benchCategories."
    if key.startswith(prefix):
        return config["benches"][key[len(prefix):]]

    raise KeyError(f"No translation rule for {key}")


def generate(locale, config, source_text):
    output = []
    key_count = 0
    for line in source_text.splitlines():
        if not line or line.lstrip().startswith("#"):
            output.append(line)
            continue
        key, value = line.split("=", 1)
        output.append(f"{key}={translate_entry(key, value, config)}")
        key_count += 1

    target = LANGUAGES / locale / "server.lang"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text("\n".join(output) + "\n", encoding="utf-8")
    return target, key_count


def main():
    source_text = SOURCE.read_text(encoding="utf-8")
    source_ids = set()
    for line in source_text.splitlines():
        key = line.split("=", 1)[0]
        if key.startswith("expeditions.") and key.endswith(".name"):
            source_ids.add(key[len("expeditions."): -len(".name")])
    for locale, config in CONFIGS.items():
        missing = source_ids - config["names"].keys()
        extra = config["names"].keys() - source_ids
        if missing or extra:
            raise RuntimeError(f"{locale} name table mismatch; missing={missing}, extra={extra}")
        target, key_count = generate(locale, config, source_text)
        print(f"Generated {target.relative_to(ROOT)} ({key_count} keys)")


if __name__ == "__main__":
    main()
