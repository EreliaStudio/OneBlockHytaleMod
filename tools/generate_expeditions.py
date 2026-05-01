#!/usr/bin/env python3
"""
generate_expeditions.py — OneBlock Expedition Asset Generator
"""

import argparse
import json
import re
import shutil
import sys
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")


# ── Translation prefixes ──────────────────────────────────────────────────────
PREFIX_ITEMS_JSON = "server.items"
PREFIX_BENCH_JSON = "server.benchCategories"

PREFIX_ITEMS_LANG = "items"
PREFIX_BENCH_LANG = "benchCategories"


# ── Repo-relative paths (all inside the single oneblock mod) ──────────────────
ONEBLOCK = Path("mods/oneblock/src/main/resources")
ITEMS     = ONEBLOCK / "Server/Item/Items"
LANG_FILE = ONEBLOCK / "Server/Languages/en-US/server.lang"
ENCHANTER = ITEMS / "OneBlockEnchanter/Bench_OneBlockEnchanter.json"
DUNGEON_ENCHANTER = ITEMS / "OneBlockDungeonEnchanter/Bench_OneBlockDungeonEnchanter.json"
CRYSTAL_DIR = ITEMS / "Crystal/Expedition"
BLOCK_DIR   = ITEMS / "OneBlock"
CUSTOM_ITEM_DIR = ITEMS / "CustomItems"

BLOCK_TEXTURE_DIR = ONEBLOCK / "Common/BlockTextures"
BLOCK_ICON_DIR    = ONEBLOCK / "Common/Icons/ItemsGenerated"
ONEBLOCK_ICON_DIR = BLOCK_ICON_DIR / "OneBlock"
CRYSTAL_ICON_DIR  = BLOCK_ICON_DIR / "Crystals"
CUSTOM_ITEM_TEXTURE_DIR = ONEBLOCK / "Common/Items/CustomItems"
CUSTOM_ITEM_MODEL = ONEBLOCK / "Common/Blocks/CustomItems/OneBlock_CustomItem.blockymodel"
CUSTOM_ITEM_MODEL_RESOURCE = "Blocks/CustomItems/OneBlock_CustomItem.blockymodel"
DEFAULT_RENDER_NAMES_FILE = Path("item_render_names.json")

# ── Default asset templates (next to this script) ────────────────────────────
SCRIPT_DIR           = Path(__file__).parent
DEFAULT_BLOCK_TEXTURE = SCRIPT_DIR / "OneBlock_DefaultTexture.png"
DEFAULT_BLOCK_ICON    = SCRIPT_DIR / "OneBlock_DefaultIcon.png"
DEFAULT_CRYSTAL_ICON  = SCRIPT_DIR / "OneBlock_ExpeditionCrystal_DefaultIcon.png"
DEFAULT_CUSTOM_ITEM_ICON = SCRIPT_DIR / "OneBlock_CustomItem.png"

CUSTOM_ID_KEY = "CustomID"

# PNGs in Icons/ItemsGenerated that are NOT expedition-generated and must be preserved
_STATIC_ICON_NAMES = {
    "OneBlock_ExpeditionCrystal_DefaultIcon.png",
    "OneBlock_CrystalEnchantingTable_Icon.png",
    "OneBlock_DungeonEnchantingTable_Icon.png",
}

ENCHANTER_CATEGORY_ORDER = [
    "Surface",
    "Forest",
    "Underground",
    "Cold",
    "Inferno",
    "Dark",
]


def _safe_eid(expedition_id: str) -> str:
    return expedition_id.replace(" ", "_")


def _quality(item_level: int) -> str:
    if item_level <= 4:
        return "Uncommon"
    if item_level <= 7:
        return "Rare"
    return "Epic"


def _category_sort_key(category: dict) -> tuple[int, str]:
    category_id = category.get("Id", "")
    prefix = "OneBlock_Enchanter_"
    if category_id.startswith(prefix):
        group = category_id[len(prefix):]
        if group in ENCHANTER_CATEGORY_ORDER:
            return (ENCHANTER_CATEGORY_ORDER.index(group), category_id)
    return (len(ENCHANTER_CATEGORY_ORDER), category_id)


def build_crystal(expedition_id: str, category: str, item_level: int, inputs: list, ticks: int, bench_id: str = "OneBlockEnchanter", knowledge_required: bool = False) -> dict:
    eid = _safe_eid(expedition_id)
    gid = _safe_eid(category)
    item_id = f"OneBlock_Crystal_{eid}"
    data = {
        "TranslationProperties": {
            "Name": f"{PREFIX_ITEMS_JSON}.{item_id}.name",
            "Description": f"{PREFIX_ITEMS_JSON}.{item_id}.description",
        },
        "Id": item_id,
        "ItemLevel": item_level,
        "Icon": f"Icons/ItemsGenerated/Crystals/OneBlock_Crystal_{eid}.png",
        "Categories": ["Items.OneBlockExpeditionCrystal"],
        "PlayerAnimationsId": "Item",
        "Interactions": {
            "Primary": {
                "Interactions": [{"Type": "oneblock_crystal_use"}]
            },
            "Secondary": {
                "Interactions": [{"Type": "oneblock_crystal_use"}]
            },
        },
        "Consumable": True,
        "Tags": {
            "Type": ["OneBlock_ExpeditionCrystal"],
            "OneBlockCrystalExpedition": [expedition_id],
            "OneBlockCrystalTicks": [str(ticks)],
        },
        "MaxStack": 4,
        "Quality": _quality(item_level),
    }
    if inputs:
        recipe = {
            "Input": inputs,
            "OutputQuantity": 1,
            "BenchRequirement": [
                {
                    "Type": "Crafting",
                    "Categories": [f"OneBlock_{bench_id[len('OneBlock'):]}_{gid}"],
                    "Id": bench_id,
                }
            ],
        }
        if knowledge_required:
            recipe["KnowledgeRequired"] = True
        data["Recipe"] = recipe
    return data


def build_oneblock_block(expedition_id: str, item_level: int) -> dict:
    eid = _safe_eid(expedition_id)
    item_id = f"OneBlock_Block_{eid}"

    return {
        "TranslationProperties": {
            "Name": f"{PREFIX_ITEMS_JSON}.{item_id}.name",
        },
        "Id": item_id,
        "ItemLevel": item_level,
        "Icon": f"Icons/ItemsGenerated/OneBlock/OneBlock_{eid}.png",
        "Categories": ["Blocks.OneBlock"],
        "PlayerAnimationsId": "Block",
        "Set": "OneBlock",
        "BlockType": {
            "Material": "Solid",
            "DrawType": "Cube",
            "Group": "OneBlock",
            "Flags": {},
            # Keep all variants hand-breakable; expedition length is controlled by Ticks.
            "Gathering": {
                "Breaking": {
                    "GatherType": "Soils",
                    "ID": "Soil_Dirt",
                    "Quantity": 0,
                }
            },
            "Textures": [
                {
                    "Down": f"BlockTextures/OneBlock_Block_{eid}.png",
                    "Sides": f"BlockTextures/OneBlock_Block_{eid}.png",
                    "Up": f"BlockTextures/OneBlock_Block_{eid}.png",
                    "Weight": 1,
                }
            ],
            "TransitionToGroups": [],
            "ParticleColor": "#ffffff",
        },
        "Tags": {
            "Type": ["OneBlock_Block"],
        },
        "IconProperties": {
            "Scale": 0.3,
            "Rotation": [339.295, 229.107, 337.792],
            "Translation": [0, -6],
        },
        "ItemSoundSetId": "ISS_Blocks_Stone",
    }


def build_custom_item(custom_id: str) -> dict:
    icon = f"Icons/ItemsGenerated/{custom_id}_Icon.png"
    texture = f"Items/CustomItems/{custom_id}_Texture.png"
    return {
        "TranslationProperties": {
            "Name": f"{PREFIX_ITEMS_JSON}.{custom_id}.name",
        },
        "Id": custom_id,
        "Icon": icon,
        "Categories": ["Items"],
        "Model": CUSTOM_ITEM_MODEL_RESOURCE,
        "Texture": texture,
        "PlayerAnimationsId": "Item",
        "IconProperties": {
            "Scale": 1,
            "Translation": [0, -3],
            "Rotation": [22.5, 45, 22.5],
        },
        "Tags": {
            "Type": ["Ingredient", "OneBlock_CustomItem"],
        },
        "ItemEntity": {
            "ParticleSystemId": None,
        },
        "DropOnDeath": True,
    }


def build_custom_item_model() -> dict:
    quad_shape = {
        "type": "quad",
        "offset": {"x": 0, "y": 0, "z": 0},
        "stretch": {"x": 1, "y": 1, "z": 1},
        "settings": {
            "size": {"x": 22, "y": 32},
            "normal": "+Z",
        },
        "visible": True,
        "doubleSided": True,
        "shadingMode": "fullbright",
        "unwrapMode": "custom",
        "textureLayout": {
            "front": {
                "offset": {"x": 4, "y": 0},
                "mirror": {"x": False, "y": False},
                "angle": 0,
            },
        },
    }

    return {
        "lod": "auto",
        "nodes": [
            {
                "id": "2",
                "name": "R-Attachment",
                "children": [
                    {
                        "id": "1",
                        "name": "CustomItem",
                        "children": [
                            {
                                "id": "0",
                                "name": "Front",
                                "children": [],
                                "position": {"x": 0, "y": 0, "z": 0},
                                "orientation": {"x": 0, "y": -0.707107, "z": 0, "w": 0.707107},
                                "shape": quad_shape,
                            },
                        ],
                        "position": {"x": 0, "y": 19, "z": 0},
                        "orientation": {"x": 0, "y": 0.707107, "z": 0, "w": 0.707107},
                        "shape": quad_shape,
                    },
                ],
                "position": {"x": 0, "y": 0, "z": 0},
                "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
                "shape": {
                    "type": "none",
                    "offset": {"x": 0, "y": 0, "z": 0},
                    "stretch": {"x": 1, "y": 1, "z": 1},
                    "settings": {"isPiece": True},
                    "visible": True,
                    "doubleSided": False,
                    "shadingMode": "standard",
                    "unwrapMode": "custom",
                    "textureLayout": {},
                },
            },
        ],
    }


def build_lang_block(expedition_id: str,
                     drop_pool: list,
                     mandatory_rewards: list,
                     random_bundles: list,
                     ticks: int,
                     render_names: dict[str, str]) -> str:
    eid = _safe_eid(expedition_id)
    display = expedition_id.replace("_", " ")
    sep = "─" * max(0, 55 - len(display))

    loot_lines = "\\nLoot :"
    for entry in drop_pool:
        loot_lines += f"\\n- {_display_drop_name(entry, render_names)}"

    reward_lines = ""
    if mandatory_rewards:
        reward_lines = "\\nCompletion rewards :"
        for entry in mandatory_rewards:
            crystal_id = entry.get("Crystal")
            if crystal_id is not None:
                crystal_display = str(crystal_id).replace("_", " ")
                reward_lines += f"\\n- {_entry_quantity(entry)}x {crystal_display} Crystal (unlocks {crystal_display})"
            else:
                reward_lines += f"\\n- {_entry_quantity(entry)}x {_display_drop_name(entry, render_names)}"

    if random_bundles:
        reward_lines += "\\nRandom reward (one bundle) :"
        for i, bundle in enumerate(random_bundles, 1):
            items_str = ", ".join(
                f"{_entry_quantity(item)}x {_display_drop_name(item, render_names)}"
                for item in bundle.get("Items", [])
            )
            weight = max(1, int(bundle.get("Weight", 1)))
            reward_lines += f"\\n  [{weight}] {items_str}"

    lines = [
		f"\n# GENERATED ─── {display} {sep}",

		# Existing generated item/block keys
		f"{PREFIX_ITEMS_LANG}.OneBlock_Block_{eid}.name=OneBlock {display}",
		f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}.name={display} Crystal",
		f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}.description=Consume to begin a {display} expedition.\\n{ticks} ticks.{loot_lines}{reward_lines}",

		# New gameplay/UI keys
		f"expeditions.{eid}.name={display}",
		f"announcements.expedition_started.{eid}={display} expedition started.",
		f"announcements.expedition_completed.{eid}={display} expedition complete. The OneBlock has returned to default.",
		f"announcements.expedition_unlocked.{eid}=New expedition unlocked: {display}",
		f"progress.expedition.{eid}={display}: {{0}}/{{1}} ticks remaining",
	]

    return "\n".join(lines)


JAVA_ENTITY_PREFIX = "entity:"


def _entry_drop_id(entry: dict) -> str:
    crystal_id = entry.get("Crystal")
    if crystal_id is not None:
        return f"OneBlock_Crystal_{_safe_eid(str(crystal_id).strip())}"
    if CUSTOM_ID_KEY in entry:
        return str(entry[CUSTOM_ID_KEY]).strip()
    if "ID" in entry:
        return str(entry["ID"]).strip()
    raise ValueError(f"Drop entry must contain Crystal, ID, or {CUSTOM_ID_KEY}: {entry}")


def _entry_weight(entry: dict) -> int:
    try:
        return max(1, int(entry.get("Weight", 1)))
    except (TypeError, ValueError):
        return 1


def _display_drop_name(entry: dict, render_names: dict[str, str]) -> str:
    item_id = _entry_drop_id(entry)
    if "RenderName" in entry:
        return entry["RenderName"]
    if item_id in render_names:
        return render_names[item_id]
    if item_id.startswith(JAVA_ENTITY_PREFIX):
        item_id = item_id[len(JAVA_ENTITY_PREFIX):]
        if item_id in render_names:
            return render_names[item_id]
    return item_id.replace("_", " ")


def _entry_quantity(entry: dict) -> int:
    try:
        return max(1, int(entry.get("Quantity", 1)))
    except (TypeError, ValueError):
        return 1


def _java_drop_id_expr(drop_id: str) -> str:
    if drop_id.startswith(JAVA_ENTITY_PREFIX):
        entity = drop_id[len(JAVA_ENTITY_PREFIX):]
        return f'OneBlockDropId.entityDropId("{entity}")'
    return f'"{drop_id}"'


def _java_drop_expr(drop_id: str, weight: int) -> str:
    return f"drop({_java_drop_id_expr(drop_id)}, {weight})"


def _java_reward_expr(entry: dict) -> str:
    crystal_id = entry.get("Crystal")
    if crystal_id is not None:
        eid = _safe_eid(str(crystal_id).strip())
        return f'crystalReward("{eid}", {_entry_quantity(entry)})'
    return f"reward({_java_drop_id_expr(_entry_drop_id(entry))}, {_entry_quantity(entry)})"


def _java_list_expr(entries: list[str], closing_indent: str) -> str:
    if not entries:
        return "List.of()"
    return "List.of(\n" + ",\n".join(entries) + "\n" + closing_indent + ")"


def _parse_completion_rewards(raw) -> tuple[list, list]:
    """Returns (mandatory_entries, random_bundles). Supports both old list format and new {Mandatory, Random} format."""
    if isinstance(raw, list):
        return raw, []
    if isinstance(raw, dict):
        return raw.get("Mandatory") or [], raw.get("Random") or []
    return [], []


def _java_bundle_expr(bundle: dict, base_indent: str) -> str:
    item_exprs = ", ".join(
        _java_reward_expr(item)
        for item in bundle.get("Items", [])
    )
    weight = max(1, int(bundle.get("Weight", 1)))
    return f"bundle(List.of({item_exprs}), {weight})"


def build_java_defaults_block(all_expeditions: list[tuple[str, int, list, list, list]]) -> str:
    lines = [
        "    static",
        "    {",
        "        Map<String, ExpeditionDefinition> expeditions = new HashMap<>();",
    ]

    for expedition_id, ticks, drop_pool, mandatory_rewards, random_bundles in all_expeditions:
        lines.append("")

        entries = [
            f"                {_java_drop_expr(_entry_drop_id(entry), _entry_weight(entry))}"
            for entry in drop_pool
        ]
        drop_list = _java_list_expr(entries, "        ")

        mandatory_entries = [
            f"                {_java_reward_expr(entry)}"
            for entry in mandatory_rewards
        ]
        bundle_entries = [
            f"                {_java_bundle_expr(b, '                ')}"
            for b in random_bundles
        ]

        if mandatory_entries or bundle_entries:
            mandatory_list = _java_list_expr(mandatory_entries, "        ")
            if bundle_entries:
                random_list = _java_list_expr(bundle_entries, "        ")
                lines.append(f'        register(expeditions, "{expedition_id}", {ticks}, {drop_list}, {mandatory_list}, {random_list});')
            else:
                lines.append(f'        register(expeditions, "{expedition_id}", {ticks}, {drop_list}, {mandatory_list});')
        else:
            lines.append(f'        register(expeditions, "{expedition_id}", {ticks}, {drop_list});')

    lines += [
        "",
        "        EXPEDITIONS = Collections.unmodifiableMap(expeditions);",
        "        DEFAULT_IDS = buildDefaultIds(EXPEDITIONS);",
        "        DEFAULT_WEIGHTS = buildDefaultWeights(EXPEDITIONS);",
        "        COMPLETION_REWARD_DROP_IDS = buildCompletionRewardDropIds(EXPEDITIONS);",
        "    }",
    ]

    return "\n".join(lines)


def build_lang_dungeon_block(expedition_id: str,
                             waves: list,
                             completion_rewards: list,
                             render_names: dict[str, str]) -> str:
    eid = _safe_eid(expedition_id)
    display = expedition_id.replace("_", " ")
    sep = "─" * max(0, 55 - len(display))

    wave_lines = f"\\n{len(waves)} waves :"
    for i, wave in enumerate(waves, 1):
        names = ", ".join(w.replace("_", " ") for w in wave)
        wave_lines += f"\\n  Wave {i}: {names}"

    reward_lines = ""
    if completion_rewards:
        reward_lines = "\\nCompletion rewards :"
        for entry in completion_rewards:
            reward_lines += f"\\n- {_entry_quantity(entry)}x {_display_drop_name(entry, render_names)}"

    lines = [
		f"\n# GENERATED ─── {display} {sep}",

		f"{PREFIX_ITEMS_LANG}.OneBlock_Block_{eid}.name=OneBlock {display}",
		f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}.name={display} Crystal",
		f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}.description=Consume to begin the {display} dungeon.{wave_lines}{reward_lines}",

		f"expeditions.{eid}.name={display}",
		f"announcements.dungeon_started.{eid}={display} dungeon started.",
		f"announcements.dungeon_completed.{eid}={display} dungeon complete. The OneBlock has returned to default.",
	]

    return "\n".join(lines)


def build_java_dungeon_defaults_block(all_dungeons: list[tuple[str, list, list]]) -> str:
    lines = [
        "    static",
        "    {",
        "        Map<String, DungeonDefinition> dungeons = new HashMap<>();",
    ]

    for dungeon_id, waves, completion_rewards in all_dungeons:
        lines.append("")

        wave_entries = []
        for wave in waves:
            entity_ids = ", ".join(f'"entity:{e}"' for e in wave)
            wave_entries.append(f"                        List.of({entity_ids})")
        if wave_entries:
            waves_expr = "List.of(\n" + ",\n".join(wave_entries) + "\n                )"
        else:
            waves_expr = "List.of()"

        reward_entries = [
            f"                {_java_reward_expr(entry)}"
            for entry in completion_rewards
        ]
        if reward_entries:
            reward_list = _java_list_expr(reward_entries, "        ")
            lines.append(f'        register(dungeons, "{dungeon_id}", {waves_expr}, {reward_list});')
        else:
            lines.append(f'        register(dungeons, "{dungeon_id}", {waves_expr}, List.of());')

    lines += [
        "",
        "        DUNGEONS = Collections.unmodifiableMap(dungeons);",
        "        ALL_ENTITY_IDS = buildAllEntityIds(DUNGEONS);",
        "        COMPLETION_REWARD_DROP_IDS = buildCompletionRewardDropIds(DUNGEONS);",
        "    }",
    ]

    return "\n".join(lines)


def _load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def _load_render_names(path: Path) -> dict[str, str]:
    if path is None or not path.exists():
        return {}

    data = json.loads(path.read_text(encoding="utf-8-sig"))
    if not isinstance(data, dict):
        raise ValueError(f"Render names file must contain a JSON object: {path}")

    out: dict[str, str] = {}
    for key, value in data.items():
        if key.startswith("_") or value is None:
            continue
        out[str(key)] = str(value)
    return out


def _save_json(path: Path, data: dict, dry_run: bool):
    if dry_run:
        print(f"  [dry-run] Would overwrite {path}")
        return
    path.write_bytes(
        (json.dumps(data, indent=2, ensure_ascii=False) + "\n").encode("utf-8")
    )
    print(f"  [patch]  {path}")


def ensure_block_assets(repo_root: Path, expedition_id: str, dry_run: bool, stale: "set[Path] | None" = None):
    """Copy default texture/icon for a new expedition block if specific files don't exist."""
    eid = _safe_eid(expedition_id)

    texture_dst = repo_root / BLOCK_TEXTURE_DIR / f"OneBlock_Block_{eid}.png"
    if stale is not None:
        stale.discard(texture_dst)
    if not texture_dst.exists():
        if DEFAULT_BLOCK_TEXTURE.exists():
            if not dry_run:
                texture_dst.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(DEFAULT_BLOCK_TEXTURE, texture_dst)
                print(f"  [copy]   {texture_dst.name} ← default block texture")
            else:
                print(f"  [dry-run] Would copy default block texture → {texture_dst.name}")
        else:
            print(f"  [warn]   No default block texture found at {DEFAULT_BLOCK_TEXTURE}")

    icon_dst = repo_root / ONEBLOCK_ICON_DIR / f"OneBlock_{eid}.png"
    if stale is not None:
        stale.discard(icon_dst)
    if not icon_dst.exists():
        if DEFAULT_BLOCK_ICON.exists():
            if not dry_run:
                icon_dst.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(DEFAULT_BLOCK_ICON, icon_dst)
                print(f"  [copy]   {icon_dst.name} ← default block icon")
            else:
                print(f"  [dry-run] Would copy default block icon → {icon_dst.name}")
        else:
            print(f"  [warn]   No default block icon found at {DEFAULT_BLOCK_ICON}")




def ensure_crystal_asset(repo_root: Path, expedition_id: str, dry_run: bool, stale: "set[Path] | None" = None):
    """Copy the default expedition crystal icon for a crystal if its specific icon does not exist."""
    eid = _safe_eid(expedition_id)
    icon_dst = repo_root / CRYSTAL_ICON_DIR / f"OneBlock_Crystal_{eid}.png"

    if stale is not None:
        stale.discard(icon_dst)

    if icon_dst.exists():
        print(f"  [skip]   {icon_dst.name} already exists")
        return

    default_src = repo_root / BLOCK_ICON_DIR / "OneBlock_ExpeditionCrystal_DefaultIcon.png"
    src = default_src if default_src.exists() else DEFAULT_CRYSTAL_ICON

    if not src.exists():
        print(f"  [warn]   No default crystal icon found at {default_src} or {DEFAULT_CRYSTAL_ICON}")
        return

    if dry_run:
        print(f"  [dry-run] Would copy default crystal icon -> {icon_dst.name}")
        return

    icon_dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, icon_dst)
    print(f"  [copy]   {icon_dst.name} <- default crystal icon")

def _custom_item_ids_from_entries(entries: list) -> set[str]:
    out: set[str] = set()
    for entry in entries or []:
        if not isinstance(entry, dict):
            continue
        custom_id = entry.get(CUSTOM_ID_KEY)
        if custom_id is not None:
            custom_id = str(custom_id).strip()
            if custom_id:
                out.add(custom_id)
        # Handle random bundle items list
        for item in entry.get("Items", []):
            if not isinstance(item, dict):
                continue
            cid = item.get(CUSTOM_ID_KEY)
            if cid is not None:
                cid = str(cid).strip()
                if cid:
                    out.add(cid)
    return out


def _display_custom_item_name(custom_id: str) -> str:
    return custom_id.replace("_", " ")


def ensure_custom_item_model(repo_root: Path, dry_run: bool):
    model_dst = repo_root / CUSTOM_ITEM_MODEL
    if model_dst.exists():
        return

    if dry_run:
        print(f"  [dry-run] Would write {model_dst.name}")
        return

    model_dst.parent.mkdir(parents=True, exist_ok=True)
    model_dst.write_bytes(
        (json.dumps(build_custom_item_model(), indent=2, ensure_ascii=False) + "\n").encode("utf-8")
    )
    print(f"  [write]  {model_dst.name}")


def _patch_legacy_custom_item_asset_paths(item_dst: Path, custom_id: str, dry_run: bool):
    data = _load_json(item_dst)
    icon = f"Icons/ItemsGenerated/{custom_id}_Icon.png"
    texture = f"Items/CustomItems/{custom_id}_Texture.png"

    changed = False
    if data.get("Icon") == f"Icons/ItemsGenerated/{custom_id}.png":
        data["Icon"] = icon
        changed = True

    if data.get("Texture") in {
        f"Icons/ItemsGenerated/{custom_id}.png",
        f"Items/CustomItems/{custom_id}.png",
    }:
        data["Texture"] = texture
        changed = True

    if not changed:
        return

    if dry_run:
        print(f"  [dry-run] Would patch legacy custom item asset paths in {item_dst.name}")
        return

    item_dst.write_bytes(
        (json.dumps(data, indent=2, ensure_ascii=False) + "\n").encode("utf-8")
    )
    print(f"  [patch]  {item_dst.name} custom item asset paths")


def _copy_custom_item_asset(default_src: Path,
                            legacy_src: Path,
                            dst: Path,
                            label: str,
                            dry_run: bool):
    if dst.exists():
        print(f"  [skip]   {dst.name} already exists")
        return

    src = legacy_src if legacy_src.exists() else default_src
    if not src.exists():
        print(f"  [warn]   No default custom item {label} found at {default_src}")
        return

    if dry_run:
        print(f"  [dry-run] Would copy custom item {label} -> {dst.name}")
        return

    dst.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(src, dst)
    print(f"  [copy]   {dst.name} <- custom item {label}")


def _remove_legacy_custom_item_asset(path: Path, replacement: Path, label: str, dry_run: bool):
    if not path.exists() or not replacement.exists():
        return

    if dry_run:
        print(f"  [dry-run] Would remove legacy custom item {label} {path.name}")
        return

    path.unlink()
    print(f"  [clean]  removed legacy custom item {label} {path.name}")


def ensure_custom_item_assets(repo_root: Path, custom_id: str, dry_run: bool, stale: "set[Path] | None" = None):
    ensure_custom_item_model(repo_root, dry_run)

    item_dst = repo_root / CUSTOM_ITEM_DIR / f"{custom_id}.json"
    if stale is not None:
        stale.discard(item_dst)
    if not item_dst.exists():
        if dry_run:
            print(f"  [dry-run] Would write {item_dst.name}")
        else:
            item_dst.parent.mkdir(parents=True, exist_ok=True)
            item_dst.write_bytes(
                (json.dumps(build_custom_item(custom_id), indent=2, ensure_ascii=False) + "\n").encode("utf-8")
            )
            print(f"  [write]  {item_dst.name}")
    else:
        _patch_legacy_custom_item_asset_paths(item_dst, custom_id, dry_run)
        print(f"  [skip]   {item_dst.name} already exists")

    legacy_icon = repo_root / BLOCK_ICON_DIR / f"{custom_id}.png"
    icon_dst = repo_root / BLOCK_ICON_DIR / f"{custom_id}_Icon.png"
    if stale is not None:
        stale.discard(icon_dst)
    _copy_custom_item_asset(DEFAULT_CUSTOM_ITEM_ICON, legacy_icon, icon_dst, "icon", dry_run)
    _remove_legacy_custom_item_asset(legacy_icon, icon_dst, "icon", dry_run)

    legacy_texture = repo_root / CUSTOM_ITEM_TEXTURE_DIR / f"{custom_id}.png"
    texture_dst = repo_root / CUSTOM_ITEM_TEXTURE_DIR / f"{custom_id}_Texture.png"
    if stale is not None:
        stale.discard(texture_dst)
    _copy_custom_item_asset(DEFAULT_CUSTOM_ITEM_ICON, legacy_texture, texture_dst, "texture", dry_run)
    _remove_legacy_custom_item_asset(legacy_texture, texture_dst, "texture", dry_run)


def patch_enchanter(path: Path, expedition_id: str, group: str, dry_run: bool, bench_id: str = "OneBlockEnchanter"):
    eid = _safe_eid(expedition_id)
    gid = _safe_eid(group)
    bench_suffix = bench_id[len("OneBlock"):]
    cat_id = f"OneBlock_{bench_suffix}_{gid}"
    lang_key = f"{bench_id}_{gid}"

    data = _load_json(path)
    categories = data["BlockType"]["Bench"]["Categories"]

    cat = next((c for c in categories if c["Id"] == cat_id), None)
    if cat is None:
        cat = {
            "Id": cat_id,
            "Icon": "Icons/CraftingCategories/OneBlock_ExpeditionCrystal_DefaultIcon.png",
            "Name": f"{PREFIX_BENCH_JSON}.{lang_key}",
            "Recipes": [],
        }
        categories.append(cat)
    else:
        cat["Name"] = f"{PREFIX_BENCH_JSON}.{lang_key}"

    recipe_id = f"OneBlock_Crystal_{eid}"
    if recipe_id not in cat["Recipes"]:
        cat["Recipes"].append(recipe_id)

    categories.sort(key=_category_sort_key)
    _save_json(path, data, dry_run)
    print(f"  [patch]  {bench_id} ← {eid} → group {gid}")


def patch_lang(path: Path,
               expedition_id: str,
               drop_pool: list,
               mandatory_rewards: list,
               random_bundles: list,
               ticks: int,
               render_names: dict[str, str],
               dry_run: bool):
    eid = _safe_eid(expedition_id)
    existing = path.read_text(encoding="utf-8")
    marker = f"{PREFIX_ITEMS_LANG}.OneBlock_Block_{eid}.name"

    if marker in existing:
        print(f"  [skip]   Lang already has entries for {expedition_id}")
        return

    block = build_lang_block(expedition_id, drop_pool, mandatory_rewards, random_bundles, ticks, render_names)

    if dry_run:
        print(f"  [dry-run] Would append lang entries for {expedition_id}")
        return

    path.write_text(existing.rstrip() + "\n" + block + "\n", encoding="utf-8")
    print(f"  [patch]  lang ← {expedition_id}")


def patch_group_lang(path: Path, group: str, dry_run: bool, bench_id: str = "OneBlockEnchanter"):
    gid = _safe_eid(group)
    key = f"{PREFIX_BENCH_LANG}.{bench_id}_{gid}"
    value = f"{group.replace('_', ' ')} Crystals"

    existing = path.read_text(encoding="utf-8")
    if key in existing:
        return

    line = f"{key}={value}"

    if dry_run:
        print(f"  [dry-run] Would append lang entry for {key}")
        return

    path.write_text(existing.rstrip() + "\n" + line + "\n", encoding="utf-8")
    print(f"  [patch]  lang ← {key}")


def patch_custom_item_lang(path: Path, custom_id: str, dry_run: bool):
    key = f"{PREFIX_ITEMS_LANG}.{custom_id}.name"
    existing = path.read_text(encoding="utf-8")
    if key in existing:
        return

    line = f"{key}={_display_custom_item_name(custom_id)}"

    if dry_run:
        print(f"  [dry-run] Would append lang entry for {key}")
        return

    path.write_text(existing.rstrip() + "\n" + line + "\n", encoding="utf-8")
    print(f"  [patch]  lang <- {key}")


def _patch_dungeon_lang(path: Path,
                        expedition_id: str,
                        waves: list,
                        completion_rewards: list,
                        render_names: dict[str, str],
                        dry_run: bool):
    eid = _safe_eid(expedition_id)
    existing = path.read_text(encoding="utf-8")
    marker = f"{PREFIX_ITEMS_LANG}.OneBlock_Block_{eid}.name"

    if marker in existing:
        print(f"  [skip]   Lang already has entries for {expedition_id}")
        return

    block = build_lang_dungeon_block(expedition_id, waves, completion_rewards, render_names)

    if dry_run:
        print(f"  [dry-run] Would append lang entries for {expedition_id}")
        return

    path.write_text(existing.rstrip() + "\n" + block + "\n", encoding="utf-8")
    print(f"  [patch]  lang ← {expedition_id}")


def _patch_java_static(java_path: Path, static_block: str, label: str, count: int, dry_run: bool):
    if not java_path.exists():
        print(f"\n[warn]  {java_path} not found — printing static block to stdout:")
        print(static_block)
        return

    source = java_path.read_text(encoding="utf-8")
    if not re.search(r"    static\s*\{.*?    \}", source, re.DOTALL):
        print(f"\n[warn]  Could not locate static block in {label} — printing to stdout instead.")
        print(static_block)
        return

    new_source = re.sub(
        r"    static\s*\{.*?    \}",
        static_block,
        source,
        count=1,
        flags=re.DOTALL,
    )
    if new_source == source:
        print(f"\n[skip]   {label} static block already up to date")
    elif dry_run:
        print(f"\n[dry-run] Would overwrite static block in {label}")
    else:
        java_path.write_text(new_source, encoding="utf-8")
        print(f"\n[write]  {label} static block updated ({count} entries)")


def write_json(path: Path, data: dict, dry_run: bool, stale: "set[Path] | None" = None):
    if stale is not None:
        stale.discard(path)
    if dry_run:
        print(f"  [dry-run] Would write {path.name}")
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(
        (json.dumps(data, indent=2, ensure_ascii=False) + "\n").encode("utf-8")
    )
    print(f"  [write]  {path.name}")


def _is_generated_lang_line(line: str) -> bool:
    stripped = line.strip()

    if stripped.startswith("# GENERATED"):
        return True
    if not stripped or stripped.startswith("#"):
        return False
    if "=" not in stripped:
        return False

    key = stripped.split("=", 1)[0].strip()

    return (
        key.startswith(f"{PREFIX_ITEMS_LANG}.OneBlock_Block_")
        or key.startswith(f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_")
        or key.startswith(f"{PREFIX_BENCH_LANG}.OneBlockEnchanter_")
        or key.startswith(f"{PREFIX_BENCH_LANG}.OneBlockDungeonEnchanter_")
        or key.startswith(f"{PREFIX_ITEMS_JSON}.OneBlock_Block_")
        or key.startswith(f"{PREFIX_ITEMS_JSON}.OneBlock_Crystal_")
        or key.startswith(f"{PREFIX_BENCH_JSON}.OneBlockEnchanter_")
        or key.startswith(f"{PREFIX_BENCH_JSON}.OneBlockDungeonEnchanter_")
        or key.startswith("expeditions.")
        or key.startswith("announcements.expedition_")
        or key.startswith("announcements.dungeon_")
        or key.startswith("progress.expedition.")
    )


def collect_generated_files(repo_root: Path) -> "set[Path]":
    """Return absolute paths of all currently generated files that the script manages."""
    stale: set[Path] = set()
    globs = [
        (repo_root / CRYSTAL_DIR,            "*.json"),
        (repo_root / BLOCK_DIR,              "OneBlock_Block_*.json"),
        (repo_root / BLOCK_TEXTURE_DIR,      "OneBlock_Block_*.png"),
        # Legacy root-level OneBlock icons are included so --clean removes old generated paths.
        (repo_root / BLOCK_ICON_DIR,         "OneBlock_*.png"),
        (repo_root / ONEBLOCK_ICON_DIR,      "OneBlock_*.png"),
        (repo_root / BLOCK_ICON_DIR,         "*_Icon.png"),
        (repo_root / CRYSTAL_ICON_DIR,       "OneBlock_Crystal_*.png"),
        (repo_root / CUSTOM_ITEM_DIR,        "*.json"),
        (repo_root / CUSTOM_ITEM_TEXTURE_DIR, "*_Texture.png"),
    ]
    for directory, pattern in globs:
        if directory.exists():
            stale.update(directory.glob(pattern))
    for name in _STATIC_ICON_NAMES:
        stale.discard(repo_root / BLOCK_ICON_DIR / name)
    return stale


def remove_stale_files(stale: "set[Path]", dry_run: bool):
    if not stale:
        return
    print("\n=== Removing stale files ===")
    for path in sorted(stale):
        if dry_run:
            print(f"  [dry-run] Would delete {path.name}")
        else:
            path.unlink(missing_ok=True)
            print(f"  [clean]  {path.name}")


def cleanup(repo_root: Path, dry_run: bool):
    print("\n=== Cleanup (lang + enchanter) ===")

    for enc_path, prefix, label in [
        (repo_root / ENCHANTER, "OneBlock_Enchanter_", "Enchanter"),
        (repo_root / DUNGEON_ENCHANTER, "OneBlock_DungeonEnchanter_", "DungeonEnchanter"),
    ]:
        if enc_path.exists():
            data = _load_json(enc_path)
            categories = data["BlockType"]["Bench"]["Categories"]
            before = len(categories)
            data["BlockType"]["Bench"]["Categories"] = [
                c for c in categories
                if not c.get("Id", "").startswith(prefix)
            ]
            removed = before - len(data["BlockType"]["Bench"]["Categories"])
            if removed:
                _save_json(enc_path, data, dry_run)
                if not dry_run:
                    print(f"  [clean]  Removed {removed} category/categories from {label}")

    lang_path = repo_root / LANG_FILE
    if lang_path.exists():
        lines = lang_path.read_text(encoding="utf-8").splitlines()
        cleaned = [line for line in lines if not _is_generated_lang_line(line)]

        result = []
        previous_blank = False
        for line in cleaned:
            is_blank = not line.strip()
            if is_blank and previous_blank:
                continue
            result.append(line)
            previous_blank = is_blank

        removed = len(lines) - len(result)
        if removed:
            if dry_run:
                print(f"  [dry-run] Would remove ~{removed} generated line(s) from lang")
            else:
                lang_path.write_text(
                    "\n".join(result).rstrip() + "\n",
                    encoding="utf-8",
                )
                print(f"  [clean]  Removed {removed} generated line(s) from lang")

    print("  Cleanup done.\n")


def main():
    parser = argparse.ArgumentParser(
        description="Generate OneBlock expedition assets from a JSON definition file."
    )
    parser.add_argument("input", help="Input JSON, for example expeditionsTemplate.json")
    parser.add_argument("--repo-root", default=".", help="Repo root directory. Default: .")
    parser.add_argument(
        "--render-names",
        default=str(DEFAULT_RENDER_NAMES_FILE),
        help="JSON file mapping drop item/entity IDs to display names. Default: item_render_names.json",
    )
    parser.add_argument("--dry-run", action="store_true", help="Show what would be done without writing files")
    parser.add_argument("--clean", action="store_true", help="Remove all previously generated files before regenerating (eliminates stale crystals, blocks, and lang entries)")
    args = parser.parse_args()

    repo_root = Path(args.repo_root).resolve()
    input_path = Path(args.input)
    if not input_path.is_absolute():
        input_path = repo_root / input_path

    raw = json.loads(input_path.read_text(encoding="utf-8-sig"))
    expeditions = {k: v for k, v in raw.items() if not k.startswith("_")}

    knowledge_gated_ids: set[str] = set()
    for cfg in expeditions.values():
        raw_rewards = cfg.get("CompletionRewards", cfg.get("Rewards")) or []
        mandatory, rand_bundles = _parse_completion_rewards(raw_rewards)
        for entry in mandatory:
            crystal_id = entry.get("Crystal")
            if crystal_id:
                knowledge_gated_ids.add(_safe_eid(str(crystal_id).strip()))
        for bundle in rand_bundles:
            for entry in bundle.get("Items", []):
                crystal_id = entry.get("Crystal")
                if crystal_id:
                    knowledge_gated_ids.add(_safe_eid(str(crystal_id).strip()))

    render_names_path = Path(args.render_names)
    if not render_names_path.is_absolute():
        render_names_path = repo_root / render_names_path
    render_names = _load_render_names(render_names_path)

    enchanter_path = repo_root / ENCHANTER
    lang_path = repo_root / LANG_FILE

    stale: "set[Path] | None" = None
    if args.clean:
        stale = collect_generated_files(repo_root)
        cleanup(repo_root, args.dry_run)

    java_defaults_path = repo_root / Path(
        "mods/oneblock/src/main/java"
        "/com/EreliaStudio/OneBlock/OneBlockExpeditionDefaults.java"
    )
    java_dungeon_defaults_path = repo_root / Path(
        "mods/oneblock/src/main/java"
        "/com/EreliaStudio/OneBlock/OneBlockDungeonDefaults.java"
    )
    dungeon_enchanter_path = repo_root / DUNGEON_ENCHANTER

    all_expedition_drops: list[tuple[str, int, list, list]] = []
    all_dungeon_waves: list[tuple[str, list, list]] = []
    seen_custom_item_ids: set[str] = set()

    for expedition_id, cfg in expeditions.items():
        print(f"\n=== {expedition_id} ===")

        item_level = cfg["ItemLevel"]
        crystal_cfg = cfg["Crystal"]
        group = cfg.get("Category", cfg.get("Group", expedition_id))
        completion_rewards = cfg.get("CompletionRewards", cfg.get("Rewards", [])) or []
        eid = _safe_eid(expedition_id)
        is_dungeon = group == "Dungeon"

        mandatory_rewards, random_bundles = _parse_completion_rewards(completion_rewards)

        custom_item_ids = _custom_item_ids_from_entries(mandatory_rewards)
        custom_item_ids.update(_custom_item_ids_from_entries(random_bundles))
        if not is_dungeon:
            custom_item_ids.update(_custom_item_ids_from_entries(cfg.get("BaseDropPool", [])))
        for custom_id in sorted(custom_item_ids - seen_custom_item_ids):
            ensure_custom_item_assets(repo_root, custom_id, args.dry_run, stale)
            if lang_path.exists():
                patch_custom_item_lang(lang_path, custom_id, args.dry_run)
        seen_custom_item_ids.update(custom_item_ids)

        ensure_block_assets(repo_root, expedition_id, args.dry_run, stale)
        ensure_crystal_asset(repo_root, expedition_id, args.dry_run, stale)

        write_json(
            repo_root / BLOCK_DIR / f"OneBlock_Block_{eid}.json",
            build_oneblock_block(expedition_id, item_level),
            args.dry_run,
            stale,
        )

        if is_dungeon:
            waves = cfg.get("Waves", [])
            ticks = len(waves)

            write_json(
                repo_root / CRYSTAL_DIR / f"OneBlock_Crystal_{eid}.json",
                build_crystal(expedition_id, group, item_level, crystal_cfg["Input"], ticks, "OneBlockDungeonEnchanter", knowledge_required=(eid in knowledge_gated_ids)),
                args.dry_run,
                stale,
            )

            if crystal_cfg["Input"] and dungeon_enchanter_path.exists():
                patch_enchanter(dungeon_enchanter_path, expedition_id, group, args.dry_run, "OneBlockDungeonEnchanter")
            elif not crystal_cfg["Input"]:
                print(f"  [skip]   {eid} has no recipe input; not adding it to DungeonEnchanter")
            else:
                print(f"  [warn]   DungeonEnchanter JSON not found: {dungeon_enchanter_path}")

            if lang_path.exists():
                _patch_dungeon_lang(lang_path, expedition_id, waves, mandatory_rewards, render_names, args.dry_run)
                patch_group_lang(lang_path, group, args.dry_run, "OneBlockDungeonEnchanter")
            else:
                print(f"  [warn]   Lang file not found: {lang_path}")

            all_dungeon_waves.append((expedition_id, waves, mandatory_rewards))
        else:
            ticks = cfg.get("Ticks", 100)
            drop_pool = cfg["BaseDropPool"]

            write_json(
                repo_root / CRYSTAL_DIR / f"OneBlock_Crystal_{eid}.json",
                build_crystal(expedition_id, group, item_level, crystal_cfg["Input"], ticks, knowledge_required=(eid in knowledge_gated_ids)),
                args.dry_run,
                stale,
            )

            if crystal_cfg["Input"] and enchanter_path.exists():
                patch_enchanter(enchanter_path, expedition_id, group, args.dry_run)
            elif not crystal_cfg["Input"]:
                print(f"  [skip]   {eid} has no recipe input; not adding it to Enchanter")
            else:
                print(f"  [warn]   Enchanter JSON not found: {enchanter_path}")

            if lang_path.exists():
                patch_lang(lang_path, expedition_id, drop_pool, mandatory_rewards, random_bundles, ticks, render_names, args.dry_run)
                patch_group_lang(lang_path, group, args.dry_run)
            else:
                print(f"  [warn]   Lang file not found: {lang_path}")

            all_expedition_drops.append((expedition_id, ticks, drop_pool, mandatory_rewards, random_bundles))

    _patch_java_static(java_defaults_path, build_java_defaults_block(all_expedition_drops),
                       "OneBlockExpeditionDefaults.java", len(all_expedition_drops), args.dry_run)
    _patch_java_static(java_dungeon_defaults_path, build_java_dungeon_defaults_block(all_dungeon_waves),
                       "OneBlockDungeonDefaults.java", len(all_dungeon_waves), args.dry_run)

    if stale is not None:
        remove_stale_files(stale, args.dry_run)

    total = len(expeditions)
    print(f"\nDone. {total} expedition(s) processed ({len(all_dungeon_waves)} dungeon(s)).")


if __name__ == "__main__":
    main()
