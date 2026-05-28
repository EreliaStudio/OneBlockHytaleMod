"""Render expedition crystal icons with a shared crystal shell and destination cores."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw


SIZE = 64
SCALE = 1
CANVAS = SIZE * SCALE
REPO_ROOT = Path(__file__).resolve().parent.parent
EXPEDITIONS_JSON = REPO_ROOT / "expeditions.json"
DEFAULT_OUTPUT = (
    REPO_ROOT
    / "mods"
    / "oneblock"
    / "src"
    / "main"
    / "resources"
    / "Common"
    / "Icons"
    / "ItemsGenerated"
    / "Crystals"
)


PALETTES = {
    "plain": ("#75d8e8", "#419cbb", "#72bc66", "#275945"),
    "forest": ("#6ce5be", "#25885d", "#63b34c", "#173e2e"),
    "water": ("#6fe9ed", "#2588ca", "#31a9d6", "#123c78"),
    "desert": ("#ffe29b", "#d38942", "#ecbb5b", "#714027"),
    "cave": ("#bdcbe0", "#516482", "#4d6475", "#171e36"),
    "fire": ("#ffd15c", "#e2472f", "#db572b", "#3b1522"),
    "ice": ("#edffff", "#68cce8", "#80d6f0", "#264e82"),
    "undead": ("#be9cda", "#593e78", "#69556d", "#231c36"),
    "void": ("#ee73ff", "#4e2c92", "#4a2485", "#101635"),
    "spirit": ("#f8fbce", "#44c4c2", "#4cbfa8", "#173b55"),
    "insect": ("#e3de65", "#61883a", "#7a983d", "#2c2c25"),
    "tribal": ("#e7a85f", "#85533f", "#9a5835", "#332734"),
}


def sc(value: int | float) -> int:
    return round(value * SCALE)


def points(values: list[tuple[int | float, int | float]]) -> list[tuple[int, int]]:
    return [(sc(x), sc(y)) for x, y in values]


def rectangle(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], fill: str) -> None:
    draw.rectangle(tuple(sc(v) for v in xy), fill=fill)


def polygon(draw: ImageDraw.ImageDraw, xy: list[tuple[int, int]], fill: str) -> None:
    draw.polygon(points(xy), fill=fill)


def ellipse(draw: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], fill: str, width: int = 0) -> None:
    coords = tuple(sc(v) for v in xy)
    if width:
        draw.ellipse(coords, outline=fill, width=sc(width))
    else:
        draw.ellipse(coords, fill=fill)


def line(draw: ImageDraw.ImageDraw, xy: list[tuple[int, int]], fill: str, width: int = 1) -> None:
    draw.line(points(xy), fill=fill, width=sc(width), joint="curve")


def family_for(expedition_id: str) -> str:
    name = expedition_id.lower()
    matches = [
        ("spirit", ("spirit", "elemental")),
        ("void", ("void",)),
        ("fire", ("fire", "fiery", "infernal", "burn", "volcano", "ashen")),
        ("ice", ("ice", "icy", "frozen", "frost", "tundra", "yeti")),
        ("water", ("sea", "coast", "river", "lake", "pond", "pirate")),
        ("desert", ("desert", "sand", "pharaoh")),
        ("insect", ("insect", "infested")),
        ("undead", ("grave", "crypt", "necropolis", "undead", "dead", "shadow", "catacomb", "citadel")),
        ("forest", ("forest", "jungle", "swamp", "grove", "ruin")),
        ("tribal", ("trork", "outlander", "battlefield", "gank", "invasion")),
        ("cave", ("cave", "cavern", "quarry", "gem", "mithril", "iron", "gold", "silver", "cobalt", "adamantite", "onyxium", "prisma", "thorium")),
    ]
    for family, fragments in matches:
        if any(fragment in name for fragment in fragments):
            return family
    return "plain"


def tint_for(expedition_id: str, base: tuple[str, str, str, str]) -> tuple[str, str, str, str]:
    """Give close relatives a quiet identity without changing their family read."""
    digest = hashlib.sha1(expedition_id.encode("ascii")).digest()
    colors = list(base)
    for index in (0, 2):
        value = colors[index].lstrip("#")
        rgb = [
            max(0, min(255, int(value[channel * 2 : channel * 2 + 2], 16) + (digest[index * 3 + channel] % 17) - 8))
            for channel in range(3)
        ]
        colors[index] = "#" + "".join(f"{channel:02x}" for channel in rgb)
    return tuple(colors)  # type: ignore[return-value]


def inner_gradient(image: Image.Image, top: str, bottom: str) -> None:
    top_rgb = tuple(int(top[i : i + 2], 16) for i in (1, 3, 5))
    bottom_rgb = tuple(int(bottom[i : i + 2], 16) for i in (1, 3, 5))
    draw = ImageDraw.Draw(image)
    for y in range(sc(8), sc(56), sc(4)):
        ratio = (y - sc(8)) / sc(48)
        rgb = tuple(round(a + (b - a) * ratio) for a, b in zip(top_rgb, bottom_rgb))
        draw.rectangle((0, y, CANVAS, min(CANVAS, y + sc(4))), fill=rgb + (255,))


def draw_tree(draw: ImageDraw.ImageDraw, x: int, y: int, color: str, trunk: str = "#4b332a") -> None:
    rectangle(draw, (x - 1, y + 5, x + 1, y + 14), trunk)
    polygon(draw, [(x, y), (x - 6, y + 9), (x + 6, y + 9)], color)
    polygon(draw, [(x, y + 4), (x - 7, y + 13), (x + 7, y + 13)], color)


def draw_flame(draw: ImageDraw.ImageDraw, x: int, y: int) -> None:
    polygon(draw, [(x, y - 10), (x - 7, y + 4), (x - 5, y + 11), (x, y + 14), (x + 7, y + 8), (x + 5, y - 1)], "#ff7b31")
    polygon(draw, [(x + 1, y - 4), (x - 3, y + 6), (x, y + 11), (x + 4, y + 5)], "#ffe266")


def draw_snowflake(draw: ImageDraw.ImageDraw, x: int, y: int) -> None:
    color = "#efffff"
    for segment in (
        [(x, y - 10), (x, y + 10)],
        [(x - 9, y - 5), (x + 9, y + 5)],
        [(x - 9, y + 5), (x + 9, y - 5)],
    ):
        line(draw, segment, color, 2)
    ellipse(draw, (x - 2, y - 2, x + 2, y + 2), "#a4e9f5")


def draw_skull(draw: ImageDraw.ImageDraw, x: int, y: int) -> None:
    ellipse(draw, (x - 8, y - 9, x + 8, y + 7), "#d9d0c3")
    rectangle(draw, (x - 5, y + 4, x + 5, y + 11), "#c2b6a6")
    ellipse(draw, (x - 5, y - 3, x - 1, y + 2), "#33273e")
    ellipse(draw, (x + 1, y - 3, x + 5, y + 2), "#33273e")
    line(draw, [(x - 3, y + 7), (x + 3, y + 7)], "#706379", 1)


def draw_pick(draw: ImageDraw.ImageDraw, x: int, y: int, ore: str) -> None:
    line(draw, [(x - 6, y + 10), (x + 5, y - 7)], "#bd7845", 3)
    line(draw, [(x - 7, y - 6), (x + 8, y - 5)], "#c6d4dc", 2)
    polygon(draw, [(x + 4, y + 3), (x + 10, y + 1), (x + 8, y + 7)], ore)


def draw_wave(draw: ImageDraw.ImageDraw, y: int, color: str) -> None:
    line(draw, [(18, y), (23, y - 3), (28, y), (33, y + 2), (39, y - 2), (45, y)], color, 2)


def draw_bug(draw: ImageDraw.ImageDraw, x: int, y: int) -> None:
    ellipse(draw, (x - 5, y - 6, x + 5, y + 9), "#38273a")
    ellipse(draw, (x - 3, y - 9, x + 3, y - 4), "#433044")
    line(draw, [(x - 4, y - 1), (x - 10, y - 5)], "#d1c752", 1)
    line(draw, [(x + 4, y - 1), (x + 10, y - 5)], "#d1c752", 1)
    line(draw, [(x - 4, y + 4), (x - 10, y + 8)], "#d1c752", 1)
    line(draw, [(x + 4, y + 4), (x + 10, y + 8)], "#d1c752", 1)
    line(draw, [(x, y - 5), (x, y + 7)], "#ddb938", 1)


def draw_portal(draw: ImageDraw.ImageDraw, x: int, y: int) -> None:
    ellipse(draw, (x - 10, y - 13, x + 10, y + 13), "#bc5cf1", 2)
    ellipse(draw, (x - 7, y - 10, x + 7, y + 10), "#311b66", 2)
    line(draw, [(x, y - 7), (x, y + 8)], "#f08aff", 1)


def draw_gem(draw: ImageDraw.ImageDraw, color: str, x: int = 32, y: int = 34) -> None:
    polygon(draw, [(x, y - 12), (x + 9, y - 5), (x + 6, y + 8), (x, y + 14), (x - 7, y + 8), (x - 9, y - 5)], color)
    line(draw, [(x, y - 10), (x, y + 11)], "#f2ffff", 1)
    line(draw, [(x - 7, y - 4), (x, y), (x + 7, y - 4)], "#ffffff", 1)


def draw_arch(draw: ImageDraw.ImageDraw, color: str, x: int = 32, y: int = 33) -> None:
    line(draw, [(x - 10, y + 12), (x - 10, y - 4), (x - 6, y - 10), (x + 6, y - 10), (x + 10, y - 4), (x + 10, y + 12)], color, 3)
    rectangle(draw, (x - 4, y, x + 4, y + 13), "#20243b")


def draw_sword(draw: ImageDraw.ImageDraw, blade: str = "#eaf5ff") -> None:
    polygon(draw, [(33, 20), (36, 24), (31, 42), (28, 45), (29, 38)], blade)
    line(draw, [(27, 39), (36, 42)], "#e9af45", 2)
    line(draw, [(29, 43), (26, 47)], "#9b602b", 2)


def draw_leaf(draw: ImageDraw.ImageDraw, color: str, mirrored: bool = False) -> None:
    if mirrored:
        polygon(draw, [(39, 23), (24, 29), (27, 45), (39, 39), (43, 29)], color)
        line(draw, [(27, 42), (39, 27)], "#e2f59c", 1)
    else:
        polygon(draw, [(25, 23), (40, 29), (37, 45), (25, 39), (21, 29)], color)
        line(draw, [(37, 42), (25, 27)], "#e2f59c", 1)


def draw_bone(draw: ImageDraw.ImageDraw, color: str = "#ebe0c9") -> None:
    line(draw, [(26, 41), (38, 26)], color, 3)
    ellipse(draw, (23, 39, 28, 44), color)
    ellipse(draw, (36, 23, 41, 28), color)


def draw_totem(draw: ImageDraw.ImageDraw, color: str = "#e0a655") -> None:
    polygon(draw, [(32, 20), (39, 27), (36, 45), (28, 45), (25, 27)], color)
    ellipse(draw, (28, 28, 31, 31), "#32243c")
    ellipse(draw, (33, 28, 36, 31), "#32243c")
    line(draw, [(28, 36), (36, 36)], "#32243c", 1)


def draw_landmark(draw: ImageDraw.ImageDraw, expedition_id: str) -> None:
    """Overlay a readable, destination-specific focal mark inside the shared shell."""
    name = expedition_id
    if name == "Default":
        ellipse(draw, (26, 27, 38, 39), "#fff19c")
        line(draw, [(32, 22), (32, 44)], "#fff7c6", 1)
    elif name == "CaveEntry":
        draw_arch(draw, "#9b8790")
    elif name == "ForestEdge":
        draw_leaf(draw, "#83dc55")
    elif name == "Plain":
        ellipse(draw, (25, 24, 39, 38), "#ffe883")
        line(draw, [(24, 42), (40, 42)], "#61a947", 2)
    elif name == "Cave":
        polygon(draw, [(22, 22), (27, 35), (32, 24), (37, 37), (43, 22)], "#c8c5c0")
    elif name == "RatCave":
        ellipse(draw, (23, 29, 39, 42), "#a88a90")
        ellipse(draw, (22, 26, 28, 32), "#dfafb2")
        line(draw, [(37, 39), (44, 35)], "#d9b2ac", 1)
    elif name == "LowerCave":
        polygon(draw, [(22, 29), (42, 29), (42, 33), (28, 33), (28, 38), (39, 38), (39, 42), (23, 42)], "#9ca6b4")
    elif name in {"CopperCave", "IronCave", "GoldCave", "ThoriumCave", "SilverCave", "CobaltCave", "AdamantiteCave", "MithrilCave", "OnyxiumCave"}:
        colors = {
            "CopperCave": "#dd7645", "IronCave": "#b5bec8", "GoldCave": "#ffcf49",
            "ThoriumCave": "#69dc72", "SilverCave": "#f0f7ff", "CobaltCave": "#398ee8",
            "AdamantiteCave": "#4be5ac", "MithrilCave": "#92e7ff", "OnyxiumCave": "#44275e",
        }
        draw_gem(draw, colors[name])
    elif name in {"GemCave", "GemDeepCave", "PrismaCave", "FireGemCave"}:
        colors = {"GemCave": "#4de092", "GemDeepCave": "#33bfe2", "PrismaCave": "#f277e8", "FireGemCave": "#ff6539"}
        draw_gem(draw, colors[name])
        if name == "GemDeepCave":
            polygon(draw, [(24, 32), (20, 37), (24, 43), (28, 40)], "#61f3c4")
        if name == "PrismaCave":
            line(draw, [(24, 27), (40, 40)], "#ffd263", 2)
    elif name in {"GoblinGank", "GoblinInvasion"}:
        polygon(draw, [(24, 42), (25, 27), (31, 23), (37, 27), (39, 42)], "#62933b")
        ellipse(draw, (27, 30, 30, 33), "#ffe34f")
        ellipse(draw, (34, 30, 37, 33), "#ffe34f")
        if name == "GoblinInvasion":
            line(draw, [(22, 23), (42, 44)], "#d95739", 2)
    elif name in {"SandCave", "SandCavern"}:
        draw_arch(draw, "#deae60")
        if name == "SandCavern":
            ellipse(draw, (27, 28, 37, 37), "#ffc552")
    elif name == "Desert":
        line(draw, [(32, 24), (32, 44)], "#31734f", 2)
        line(draw, [(32, 32), (39, 29), (39, 26)], "#31734f", 2)
    elif name in {"DesertTempleEntrance", "DeeperDesertTemple", "DesertTemple"}:
        polygon(draw, [(32, 21), (20, 44), (44, 44)], "#cf843b")
        if name == "DesertTempleEntrance":
            draw_arch(draw, "#f3c665", 32, 37)
        elif name == "DeeperDesertTemple":
            line(draw, [(25, 41), (39, 41), (27, 37), (37, 37), (30, 33), (35, 33)], "#ffe091", 1)
        else:
            ellipse(draw, (28, 31, 36, 38), "#17243f")
    elif name == "PharaohRoom":
        polygon(draw, [(25, 25), (39, 25), (37, 44), (32, 48), (27, 44)], "#e7b94e")
        line(draw, [(26, 30), (38, 30)], "#3d9fc5", 2)
    elif name == "InnerDesert":
        ellipse(draw, (24, 25, 40, 41), "#ffda62")
        ellipse(draw, (28, 29, 36, 37), "#ec8d32")
    elif name == "MuddyDesert":
        ellipse(draw, (21, 36, 43, 46), "#795238")
        line(draw, [(25, 40), (38, 43)], "#b47b48", 2)
    elif name in {"Pond", "FairyPond"}:
        ellipse(draw, (20, 35, 44, 46), "#63e6ec")
        polygon(draw, [(30, 37), (39, 37), (35, 43), (27, 42)], "#62bc55")
        if name == "FairyPond":
            ellipse(draw, (31, 24, 35, 28), "#ffefad")
            line(draw, [(33, 27), (33, 34)], "#fff8c2", 1)
    elif name == "River":
        polygon(draw, [(28, 22), (38, 28), (27, 35), (38, 44), (32, 50), (21, 39), (32, 33), (24, 27)], "#d4ffff")
    elif name == "Lake":
        ellipse(draw, (20, 30, 44, 46), "#b8f8ff")
        ellipse(draw, (26, 34, 38, 40), "#289dc8")
    elif name == "Sea":
        draw_wave(draw, 29, "#e7ffff")
        draw_wave(draw, 38, "#86f1ff")
        draw_wave(draw, 46, "#34bedb")
    elif name == "Coastline":
        polygon(draw, [(22, 44), (22, 35), (39, 25), (43, 27), (29, 44)], "#efcc75")
        draw_wave(draw, 42, "#d9ffff")
    elif name == "PirateShipwreck":
        polygon(draw, [(22, 37), (43, 37), (37, 45), (26, 44)], "#633426")
        line(draw, [(32, 22), (32, 39)], "#efdbb4", 2)
        polygon(draw, [(33, 23), (42, 30), (33, 30)], "#d54d37")
    elif name == "SeaCavern":
        draw_arch(draw, "#294a7c")
        draw_wave(draw, 42, "#a5f7ff")
    elif name == "SeaInfestedNest":
        draw_bug(draw, 32, 33)
        draw_wave(draw, 45, "#6ae5e7")
    elif name == "SeaMonster":
        line(draw, [(24, 44), (22, 31), (28, 27)], "#1b5169", 3)
        line(draw, [(40, 44), (43, 30), (37, 25)], "#1b5169", 3)
        ellipse(draw, (29, 34, 35, 40), "#f4cf4b")
    elif name == "ForestEntry":
        line(draw, [(32, 43), (32, 29)], "#72452e", 2)
        draw_leaf(draw, "#7ed957")
    elif name == "Forest":
        rectangle(draw, (30, 35, 34, 47), "#603d29")
        ellipse(draw, (21, 22, 43, 38), "#2d883e")
        ellipse(draw, (25, 18, 39, 31), "#45aa4b")
    elif name == "DeepForest":
        rectangle(draw, (21, 39, 43, 47), "#112b24")
        draw_tree(draw, 26, 22, "#143e2d", "#261d25")
        draw_tree(draw, 38, 19, "#0d3129", "#261d25")
    elif name == "AridForest":
        draw_tree(draw, 32, 24, "#95733e", "#78412b")
        line(draw, [(25, 44), (39, 44)], "#e8be66", 2)
    elif name == "Swamp":
        draw_tree(draw, 29, 23, "#295838")
        draw_wave(draw, 44, "#77c5a0")
    elif name == "EnchantedForest":
        draw_tree(draw, 32, 24, "#46ba65")
        ellipse(draw, (25, 27, 28, 30), "#ffe37d")
        ellipse(draw, (38, 22, 41, 25), "#ffe37d")
    elif name in {"DarkForest", "CursedForest"}:
        draw_tree(draw, 32, 23, "#192b33", "#321d39")
        if name == "CursedForest":
            line(draw, [(24, 25), (40, 42)], "#c768db", 2)
    elif name == "BurnedForest":
        draw_tree(draw, 32, 23, "#34262a", "#191b24")
        draw_flame(draw, 39, 39)
    elif name == "Graveyard":
        rectangle(draw, (27, 29, 37, 45), "#b7adb1")
        line(draw, [(32, 25), (32, 36)], "#d9ccd0", 2)
        line(draw, [(27, 30), (37, 30)], "#d9ccd0", 2)
    elif name == "LostNecropolis":
        polygon(draw, [(22, 45), (22, 29), (27, 24), (32, 29), (37, 22), (42, 29), (42, 45)], "#665278")
        rectangle(draw, (28, 35, 36, 45), "#241d36")
    elif name == "AncientUndeadSanctum":
        polygon(draw, [(21, 43), (24, 28), (32, 19), (40, 28), (43, 43)], "#8661a1")
        ellipse(draw, (28, 30, 36, 38), "#d9b7ec")
        ellipse(draw, (30, 32, 34, 36), "#352142")
    elif name in {"UndeadTemple", "ShadowKnightCitadel", "BurntSkeletonCitadel"}:
        rectangle(draw, (23, 29, 41, 45), "#3a304c")
        polygon(draw, [(21, 29), (32, 20), (43, 29)], "#27243b")
        if name == "ShadowKnightCitadel":
            draw_sword(draw, "#ae80ff")
        elif name == "BurntSkeletonCitadel":
            draw_flame(draw, 32, 36)
    elif name in {"VoidPortal", "VoidTemple"}:
        draw_portal(draw, 32, 34)
        if name == "VoidTemple":
            polygon(draw, [(22, 29), (32, 20), (42, 29)], "#e48aff")
    elif name.startswith("Outlander"):
        draw_totem(draw, "#da9c55")
        if name == "OutlanderCity":
            rectangle(draw, (21, 37, 25, 45), "#ddc275")
            rectangle(draw, (39, 34, 43, 45), "#ddc275")
        elif name == "OutlanderGank":
            draw_sword(draw, "#dc5950")
        elif name == "OutlanderForest":
            draw_leaf(draw, "#4d9854")
    elif name == "Tundra":
        polygon(draw, [(19, 44), (30, 25), (36, 34), (41, 28), (45, 44)], "#f7ffff")
        line(draw, [(22, 46), (42, 46)], "#7bcce4", 2)
    elif name == "IceLand":
        draw_snowflake(draw, 32, 35)
    elif name in {"FrozenForest", "IcyForest"}:
        draw_tree(draw, 32, 24, "#ccf5fa", "#69afc6")
        if name == "IcyForest":
            draw_snowflake(draw, 25, 39)
    elif name == "IcyCavern":
        polygon(draw, [(23, 23), (28, 39), (32, 24), (37, 42), (42, 22)], "#efffff")
    elif name == "YetiCavern":
        ellipse(draw, (23, 27, 41, 45), "#f2ffff")
        ellipse(draw, (27, 33, 31, 37), "#28537d")
        ellipse(draw, (34, 33, 38, 37), "#28537d")
    elif name in {"IceTemple", "IcyNecropolis"}:
        rectangle(draw, (23, 30, 41, 45), "#cdf5ff")
        polygon(draw, [(21, 30), (32, 20), (43, 30)], "#ffffff")
        if name == "IcyNecropolis":
            draw_skull(draw, 32, 37)
    elif name == "FrozenGraveyard":
        rectangle(draw, (27, 28, 37, 44), "#e7fcff")
        draw_snowflake(draw, 32, 34)
    elif name == "FrostboneCrypt":
        draw_bone(draw)
        draw_snowflake(draw, 37, 29)
    elif name == "FireLand":
        draw_flame(draw, 32, 34)
    elif name == "FireCave":
        draw_arch(draw, "#49263a")
        draw_flame(draw, 32, 36)
    elif name == "InfernalPlain":
        line(draw, [(20, 43), (27, 37), (32, 42), (40, 34), (44, 40)], "#ffb339", 3)
        ellipse(draw, (27, 24, 37, 34), "#e64e2c")
    elif name == "Volcano":
        polygon(draw, [(20, 45), (32, 23), (44, 45)], "#51293b")
        line(draw, [(30, 27), (34, 35), (38, 40)], "#ff8334", 2)
    elif name == "FieryGraveyard":
        draw_skull(draw, 32, 34)
        draw_flame(draw, 39, 40)
    elif name == "InfernalGate":
        draw_arch(draw, "#ffa83b")
    elif name == "InfernalSwamp":
        draw_flame(draw, 32, 30)
        draw_wave(draw, 45, "#be7633")
    elif name == "InsectInvasion":
        draw_bug(draw, 32, 34)
        line(draw, [(21, 22), (43, 45)], "#df4a35", 3)
    elif name == "InfestedDesert":
        polygon(draw, [(19, 45), (32, 26), (45, 45)], "#dbb254")
        draw_bug(draw, 32, 37)
    elif name == "InsectNest":
        ellipse(draw, (21, 23, 43, 46), "#d6be59")
        ellipse(draw, (26, 28, 30, 35), "#7d6332")
        ellipse(draw, (34, 34, 38, 41), "#7d6332")
    elif name == "InsideInsectNest":
        line(draw, [(22, 25), (42, 44)], "#e9d570", 2)
        line(draw, [(42, 25), (22, 44)], "#e9d570", 2)
        line(draw, [(32, 22), (32, 46)], "#e9d570", 2)
        ellipse(draw, (28, 30, 36, 39), "#43302c")
    elif name == "InsectCore":
        polygon(draw, [(32, 21), (42, 29), (39, 42), (32, 48), (25, 42), (22, 29)], "#ffbe38")
        ellipse(draw, (28, 29, 36, 38), "#79353a")
    elif name == "Quarry":
        draw_pick(draw, 32, 34, "#aeb5bc")
        rectangle(draw, (21, 44, 43, 47), "#b8a16b")
    elif name in {"Hallow", "CowHallow", "HorseHallow"}:
        if name == "Hallow":
            ellipse(draw, (24, 25, 40, 41), "#f5d66d")
            line(draw, [(32, 22), (32, 44)], "#fff4ba", 2)
        elif name == "CowHallow":
            ellipse(draw, (23, 29, 41, 43), "#f3ede2")
            ellipse(draw, (26, 32, 31, 37), "#403847")
            ellipse(draw, (34, 36, 39, 41), "#403847")
        else:
            polygon(draw, [(26, 43), (27, 29), (34, 25), (40, 30), (36, 37), (34, 44)], "#eee2cc")
    elif name in {"MysteriousCavern", "MysticCave"}:
        ellipse(draw, (22, 25, 42, 43), "#5e408e", 2)
        if name == "MysteriousCavern":
            ellipse(draw, (27, 30, 37, 38), "#ef8cff")
            ellipse(draw, (31, 32, 34, 36), "#301840")
        else:
            line(draw, [(32, 26), (32, 43), (25, 34), (39, 34)], "#ffd8ff", 2)
    elif name == "LuxuriousCave":
        polygon(draw, [(23, 32), (27, 26), (32, 32), (37, 26), (41, 32), (38, 43), (26, 43)], "#ffd050")
    elif name in {"JurassicCave", "DinoCrisis"}:
        polygon(draw, [(22, 38), (29, 28), (40, 28), (44, 33), (35, 34), (30, 42)], "#274938")
        if name == "DinoCrisis":
            line(draw, [(23, 24), (41, 45)], "#ff593d", 2)
    elif name == "DryTrorkCamp":
        polygon(draw, [(20, 44), (32, 22), (44, 44)], "#bd6d46")
        rectangle(draw, (29, 35, 35, 45), "#322233")
    elif name == "TrorkHuntingGround":
        line(draw, [(24, 24), (24, 44), (35, 39), (40, 30), (36, 24)], "#f1d6a0", 2)
        line(draw, [(22, 34), (42, 34)], "#cb8048", 2)
    elif name == "TrorkWarband":
        draw_sword(draw, "#cfd6d4")
        line(draw, [(39, 25), (26, 44)], "#e34f3b", 3)
    elif name == "TrorkStrongholdApproach":
        draw_arch(draw, "#955b43")
        polygon(draw, [(22, 28), (32, 20), (42, 28)], "#d45b3f")
    elif name == "TrorkElderGrove":
        rectangle(draw, (30, 34, 34, 46), "#71432d")
        ellipse(draw, (20, 20, 44, 36), "#66894a")
    elif name == "TrorkChieftainCamp":
        draw_totem(draw, "#d08a4d")
        polygon(draw, [(22, 27), (26, 20), (32, 25), (38, 20), (42, 27)], "#e0503a")
    elif name in {"BurntBattlefield", "AshenCatacombs"}:
        if name == "BurntBattlefield":
            draw_sword(draw, "#d5d6d2")
            draw_flame(draw, 39, 40)
        else:
            draw_bone(draw)
            line(draw, [(22, 45), (42, 45)], "#da6945", 2)
    elif name in {"JungleEdge", "DryJunglePass"}:
        draw_leaf(draw, "#36a658")
        if name == "DryJunglePass":
            line(draw, [(23, 44), (41, 44)], "#e4b255", 2)
    elif name in {"OvergrownRuins", "SunkenJungleRuins"}:
        draw_arch(draw, "#a39a6a")
        draw_leaf(draw, "#54a958")
        if name == "SunkenJungleRuins":
            draw_wave(draw, 45, "#78d5c0")
    elif name in {"ShadowedJungleRoad", "ArmoredDeadGrove", "JungleCrypt"}:
        if name == "ShadowedJungleRoad":
            draw_sword(draw, "#7c61a8")
        elif name == "ArmoredDeadGrove":
            draw_skull(draw, 32, 34)
            polygon(draw, [(24, 28), (32, 22), (40, 28)], "#8e98ae")
        else:
            draw_arch(draw, "#6d5670")
            draw_leaf(draw, "#43834d")
    elif name in {"SpiritThreshold", "ElementalConfluence", "SpiritRealmTrial"}:
        ellipse(draw, (23, 24, 41, 43), "#fff4af", 2)
        if name == "SpiritThreshold":
            draw_arch(draw, "#ffffc7")
        elif name == "ElementalConfluence":
            line(draw, [(32, 24), (32, 43)], "#ff8b42", 2)
            line(draw, [(23, 34), (41, 34)], "#78edff", 2)
        else:
            draw_sword(draw, "#fff8bc")


def add_foreground_detail(image: Image.Image, expedition_id: str) -> None:
    """Scale the unique focal landmark down so it belongs to a micro landscape."""
    draw = ImageDraw.Draw(image)
    ore_colors = {
        "CopperCave": "#dd7645", "IronCave": "#b5bec8", "GoldCave": "#ffcf49",
        "ThoriumCave": "#69dc72", "SilverCave": "#f0f7ff", "CobaltCave": "#398ee8",
        "AdamantiteCave": "#4be5ac", "MithrilCave": "#92e7ff", "OnyxiumCave": "#a653cf",
        "GemCave": "#4de092", "GemDeepCave": "#33bfe2", "PrismaCave": "#f277e8",
        "FireGemCave": "#ff6539",
    }
    if expedition_id in ore_colors:
        ore = ore_colors[expedition_id]
        glows = {
            "CopperCave": "#52352d", "IronCave": "#384454", "GoldCave": "#584328",
            "ThoriumCave": "#274336", "SilverCave": "#394956", "CobaltCave": "#243a58",
            "AdamantiteCave": "#20483f", "MithrilCave": "#294553", "OnyxiumCave": "#382745",
            "GemCave": "#20443c", "GemDeepCave": "#1d3c50", "PrismaCave": "#492a48",
            "FireGemCave": "#552d27",
        }
        rectangle(draw, (35, 33, 45, 44), glows[expedition_id])
        line(draw, [(22, 47), (42, 47)], "#82604b", 1)
        line(draw, [(25, 44), (25, 49)], "#82604b", 1)
        line(draw, [(34, 44), (34, 49)], "#82604b", 1)
        rectangle(draw, (21, 29, 23, 32), ore)
        rectangle(draw, (23, 32, 25, 34), ore)
        rectangle(draw, (40, 37, 42, 40), ore)
        if expedition_id in {"GemCave", "GemDeepCave", "PrismaCave", "FireGemCave"}:
            rectangle(draw, (28, 42, 31, 46), ore)
            rectangle(draw, (32, 39, 34, 45), ore)
        return
    if expedition_id in {"Default", "Plain"}:
        rectangle(draw, (22, 44, 42, 46), "#65a84d")
        rectangle(draw, (26, 41, 27, 44), "#d4c050")
        rectangle(draw, (37, 40, 38, 44), "#eee08b")
        return
    if expedition_id == "FireLand":
        line(draw, [(20, 43), (26, 40), (30, 45), (37, 41), (44, 44)], "#ffc051", 1)
        return
    if expedition_id in {"IceLand", "Tundra"}:
        rectangle(draw, (20, 42, 44, 45), "#f4ffff")
        line(draw, [(25, 40), (29, 36), (33, 41)], "#94d8ef", 1)
        return
    if expedition_id == "Hallow":
        rectangle(draw, (21, 43, 43, 46), "#8cc85b")
        rectangle(draw, (31, 33, 32, 43), "#efda87")
        ellipse(draw, (28, 29, 35, 35), "#ffeb9e")
        return
    if expedition_id in {"SpiritThreshold", "ElementalConfluence", "SpiritRealmTrial"}:
        if expedition_id == "SpiritThreshold":
            rectangle(draw, (26, 35, 28, 45), "#efffc4")
            rectangle(draw, (37, 35, 39, 45), "#efffc4")
            line(draw, [(26, 35), (32, 30), (39, 35)], "#efffc4", 1)
        elif expedition_id == "ElementalConfluence":
            rectangle(draw, (24, 42, 28, 45), "#ff7543")
            rectangle(draw, (30, 42, 34, 45), "#70e7ff")
            rectangle(draw, (36, 42, 40, 45), "#8fda72")
        else:
            rectangle(draw, (24, 42, 40, 45), "#d4e999")
            rectangle(draw, (27, 38, 37, 41), "#e7f4af")
            rectangle(draw, (30, 34, 34, 37), "#fff5bd")
        return
    detail = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    draw_landmark(ImageDraw.Draw(detail), expedition_id)
    detail = detail.resize((48, 48), Image.Resampling.NEAREST)
    image.alpha_composite(detail, (8, 10))


def draw_destination_core(expedition_id: str, family: str, palette: tuple[str, str, str, str]) -> Image.Image:
    top, accent, ground, deep = palette
    image = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    inner_gradient(image, top, deep)
    draw = ImageDraw.Draw(image)
    name = expedition_id.lower()

    if family == "forest":
        polygon(draw, [(16, 45), (25, 33), (32, 41), (40, 31), (48, 45)], deep)
        rectangle(draw, (14, 43, 50, 55), ground)
        draw_tree(draw, 27, 24, "#1e6846")
        draw_tree(draw, 37, 27, "#287c4c")
        if "swamp" in name or "sunken" in name:
            draw_wave(draw, 46, "#52cdb1")
        if "ruin" in name or "crypt" in name:
            rectangle(draw, (38, 36, 43, 47), "#a79b70")
    elif family == "water":
        rectangle(draw, (14, 34, 50, 55), ground)
        if "river" in name:
            polygon(draw, [(26, 34), (35, 34), (29, 42), (39, 55), (29, 55), (23, 43)], "#d0fbf4")
        elif "pond" in name or "fairy" in name:
            ellipse(draw, (22, 37, 43, 49), "#d0fbf4")
            polygon(draw, [(33, 40), (40, 40), (37, 45), (31, 44)], "#4eae63")
        elif "coast" in name:
            polygon(draw, [(14, 42), (27, 37), (36, 39), (50, 35), (50, 55), (14, 55)], "#d9ba68")
            draw_wave(draw, 39, "#d0fbf4")
        else:
            draw_wave(draw, 39, "#d0fbf4")
            draw_wave(draw, 46, "#70e2ed")
        if "pirate" in name:
            polygon(draw, [(25, 37), (42, 37), (38, 43), (28, 43)], "#653b2e")
            line(draw, [(33, 24), (33, 38)], "#e5d5ae", 1)
            polygon(draw, [(34, 25), (41, 30), (34, 30)], "#e2c278")
        elif "monster" in name or "infested" in name:
            line(draw, [(30, 45), (27, 36), (30, 31)], "#224764", 2)
            line(draw, [(37, 45), (40, 35), (38, 31)], "#224764", 2)
        elif not any(fragment in name for fragment in ("river", "pond", "fairy", "coast")):
            ellipse(draw, (28, 24, 36, 32), "#f6f7e1")
    elif family == "desert":
        polygon(draw, [(14, 47), (26, 40), (38, 43), (50, 38), (50, 55), (14, 55)], ground)
        if "temple" in name or "pharaoh" in name:
            polygon(draw, [(32, 21), (20, 44), (44, 44)], "#cb8441")
            line(draw, [(32, 25), (32, 44)], "#f0c269", 1)
        else:
            line(draw, [(37, 28), (37, 46)], "#34714b", 2)
            line(draw, [(37, 34), (42, 32), (42, 29)], "#34714b", 2)
            line(draw, [(37, 38), (33, 36), (33, 33)], "#34714b", 2)
    elif family == "cave":
        polygon(draw, [(15, 29), (20, 20), (27, 26), (33, 18), (40, 25), (47, 20), (49, 46), (15, 46)], "#202a44")
        polygon(draw, [(15, 44), (22, 38), (31, 43), (40, 37), (49, 44), (49, 55), (15, 55)], ground)
        ore = "#eaeef3"
        for fragment, color in (("copper", "#dd7847"), ("gold", "#ffd05a"), ("silver", "#dcebf4"), ("cobalt", "#4ba7fa"), ("adamantite", "#5be9be"), ("mithril", "#91dfff"), ("onyx", "#d26dff"), ("gem", "#60e9d0"), ("prisma", "#ef93ff"), ("firegem", "#ff6940")):
            if fragment in name:
                ore = color
                break
        polygon(draw, [(17, 20), (20, 28), (23, 21), (25, 31), (28, 23)], "#111a2e")
        polygon(draw, [(37, 23), (40, 31), (43, 21), (46, 29), (48, 20)], "#111a2e")
        rectangle(draw, (39, 39, 41, 41), ore)
        rectangle(draw, (42, 37, 44, 39), ore)
        rectangle(draw, (20, 42, 23, 44), "#70788a")
    elif family == "fire":
        polygon(draw, [(14, 47), (21, 36), (27, 40), (33, 27), (40, 40), (47, 32), (50, 47), (50, 55), (14, 55)], "#351a27")
        rectangle(draw, (14, 47, 50, 55), "#a62e28")
        line(draw, [(16, 49), (24, 48), (29, 51), (37, 48), (48, 50)], "#ff8b32", 2)
        rectangle(draw, (22, 28, 24, 30), "#ff9735")
        rectangle(draw, (42, 24, 43, 26), "#ffb94e")
    elif family == "ice":
        polygon(draw, [(14, 45), (23, 34), (27, 38), (34, 24), (40, 35), (44, 28), (50, 44), (50, 55), (14, 55)], "#4b8fc0")
        polygon(draw, [(29, 33), (34, 24), (38, 34)], "#f7ffff")
        polygon(draw, [(20, 39), (23, 34), (26, 39)], "#e7ffff")
        rectangle(draw, (14, 44, 50, 55), "#b9eaf4")
        line(draw, [(17, 48), (28, 47), (35, 49), (47, 46)], "#efffff", 2)
    elif family == "undead":
        ellipse(draw, (36, 18, 45, 27), "#d7cde2")
        polygon(draw, [(14, 46), (22, 39), (28, 43), (35, 36), (42, 42), (50, 39), (50, 55), (14, 55)], "#30243d")
        rectangle(draw, (14, 45, 50, 55), ground)
        rectangle(draw, (20, 36, 23, 45), "#8b8192")
        line(draw, [(19, 36), (24, 36)], "#afa3ae", 1)
        rectangle(draw, (42, 39, 44, 46), "#887d91")
    elif family == "void":
        polygon(draw, [(14, 51), (27, 45), (38, 47), (50, 40), (50, 55), (14, 55)], deep)
        draw_portal(draw, 32, 32)
        ellipse(draw, (23, 20, 25, 22), "#f39eff")
        ellipse(draw, (41, 25, 43, 27), "#68e6ff")
    elif family == "spirit":
        polygon(draw, [(14, 49), (23, 39), (32, 43), (41, 36), (50, 48), (50, 55), (14, 55)], ground)
        polygon(draw, [(19, 34), (25, 31), (30, 34), (27, 37), (21, 37)], "#8ee4bc")
        polygon(draw, [(35, 28), (41, 25), (46, 29), (42, 32), (36, 32)], "#8ee4bc")
        ellipse(draw, (28, 22, 35, 29), "#f3ffc4")
        rectangle(draw, (31, 29, 32, 37), "#e8ffbb")
    elif family == "insect":
        polygon(draw, [(15, 48), (19, 29), (24, 25), (29, 34), (35, 22), (41, 31), (46, 25), (49, 48), (49, 55), (15, 55)], "#3f3828")
        rectangle(draw, (15, 45, 49, 55), ground)
        ellipse(draw, (20, 31, 26, 38), "#d6c257")
        ellipse(draw, (41, 36, 46, 43), "#bca640")
        line(draw, [(25, 28), (37, 42)], "#aea24b", 1)
        line(draw, [(39, 25), (29, 43)], "#aea24b", 1)
    elif family == "tribal":
        polygon(draw, [(14, 47), (25, 38), (34, 42), (44, 34), (50, 47), (50, 55), (14, 55)], ground)
        rectangle(draw, (14, 44, 50, 55), ground)
        polygon(draw, [(18, 44), (24, 34), (30, 44)], "#be6c42")
        polygon(draw, [(37, 44), (42, 30), (48, 44)], "#d28345")
        line(draw, [(33, 25), (33, 45)], "#482b2c", 1)
        polygon(draw, [(34, 26), (42, 29), (34, 32)], "#d95139")
    else:
        polygon(draw, [(14, 46), (24, 38), (32, 42), (42, 34), (50, 45), (50, 55), (14, 55)], ground)
        ellipse(draw, (28, 28, 36, 36), "#fff0ac")

    # A few high-signal destination overrides remain legible at inventory size.
    if "dino" in name or "jurassic" in name:
        polygon(draw, [(24, 39), (30, 31), (40, 31), (43, 35), (35, 36), (31, 42)], "#294635")
        line(draw, [(29, 39), (26, 45)], "#294635", 2)
        line(draw, [(34, 38), (38, 44)], "#294635", 2)
    if "cow" in name:
        ellipse(draw, (25, 30, 40, 42), "#f5ede0")
        ellipse(draw, (28, 33, 32, 37), "#403847")
        ellipse(draw, (35, 36, 39, 40), "#403847")
    if "horse" in name:
        polygon(draw, [(27, 42), (28, 30), (34, 27), (40, 31), (37, 36), (34, 34), (34, 43)], "#ece4d3")
    add_foreground_detail(image, expedition_id)
    return image


def draw_shell(image: Image.Image) -> None:
    draw = ImageDraw.Draw(image)
    line(draw, [(32, 3), (55, 16), (51, 48), (32, 62), (13, 48), (9, 16), (32, 3)], "#baf7ff", 2)
    line(draw, [(10, 17), (15, 20), (18, 46), (14, 48)], "#48cadd", 3)
    line(draw, [(54, 17), (49, 20), (46, 46), (50, 48)], "#1681ad", 3)
    line(draw, [(32, 8), (49, 20), (46, 45), (32, 55), (18, 45), (15, 20), (32, 8)], "#9aeaf2", 1)
    polygon(draw, [(15, 20), (32, 8), (28, 15), (20, 22)], "#d5fcff")
    polygon(draw, [(49, 20), (32, 8), (36, 15), (44, 22)], "#55cce4")
    line(draw, [(18, 22), (18, 40)], "#e4ffff", 1)
    line(draw, [(32, 9), (27, 20)], "#ffffff", 1)
    line(draw, [(47, 22), (45, 38)], "#c6f8ff", 1)
    polygon(draw, [(25, 51), (32, 56), (39, 51), (37, 58), (27, 58)], "#ca8b35")
    polygon(draw, [(28, 54), (32, 57), (36, 54), (34, 60), (30, 60)], "#f4c65c")
    ellipse(draw, (30, 54, 34, 58), "#78eaf1")


def render_icon(expedition_id: str) -> Image.Image:
    family = family_for(expedition_id)
    palette = tint_for(expedition_id, PALETTES[family])
    image = Image.new("RGBA", (CANVAS, CANVAS), (0, 0, 0, 0))
    interior = draw_destination_core(expedition_id, family, palette)
    polygon(ImageDraw.Draw(image), [(32, 3), (55, 16), (51, 48), (32, 62), (13, 48), (9, 16)], "#092541")
    mask = Image.new("L", (CANVAS, CANVAS), 0)
    ImageDraw.Draw(mask).polygon(points([(32, 8), (49, 20), (46, 45), (32, 55), (18, 45), (15, 20)]), fill=255)
    image.paste(interior, (0, 0), mask)
    draw_shell(image)
    return image


def create_contact_sheet(rendered: list[tuple[str, Image.Image]], destination: Path) -> None:
    columns = 12
    cell = 80
    rows = (len(rendered) + columns - 1) // columns
    sheet = Image.new("RGBA", (columns * cell, rows * cell), "#121a27")
    for index, (_, icon) in enumerate(rendered):
        x = (index % columns) * cell + 8
        y = (index // columns) * cell + 5
        sheet.alpha_composite(icon, (x, y))
    destination.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(destination)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-dir", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--contact-sheet", type=Path)
    args = parser.parse_args()

    definitions = json.loads(EXPEDITIONS_JSON.read_text(encoding="utf-8-sig"))
    expedition_ids = [name for name in definitions if not name.startswith("_")]
    args.output_dir.mkdir(parents=True, exist_ok=True)
    rendered: list[tuple[str, Image.Image]] = []
    for expedition_id in expedition_ids:
        icon = render_icon(expedition_id)
        icon.save(args.output_dir / f"OneBlock_Crystal_{expedition_id}.png")
        rendered.append((expedition_id, icon))

    if args.contact_sheet:
        create_contact_sheet(rendered, args.contact_sheet)
    print(f"Rendered {len(rendered)} expedition crystal icons to {args.output_dir}")


if __name__ == "__main__":
    main()
