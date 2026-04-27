#!/usr/bin/env python3
"""
generate_expeditions.py — OneBlock Expedition Asset Generator
"""

import argparse
import json
import re
import sys
from pathlib import Path

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")


# ── Translation prefixes ──────────────────────────────────────────────────────
# JSON needs "server."
PREFIX_ITEMS_JSON = "server.items"
PREFIX_BENCH_JSON = "server.benchCategories"

# server.lang does NOT need "server."
PREFIX_ITEMS_LANG = "items"
PREFIX_BENCH_LANG = "benchCategories"


# ── Repo-relative paths ───────────────────────────────────────────────────────
PROGRESSION = Path("mods/oneblock-progression/src/main/resources")
ITEMS = PROGRESSION / "Server/Item/Items"
LANG_FILE = PROGRESSION / "Server/Languages/en-US/server.lang"
ENCHANTER = ITEMS / "OneBlockEnchanter/Bench_OneBlockEnchanter.json"
WORKBENCH = ITEMS / "OneBlockWorkbench/Bench_OneBlockWorkbench.json"
CRYSTAL_DIR = ITEMS / "Crystal/Expedition"
BENCH_DIR = ITEMS / "ExpeditionBench"
RECIPE_DIR = ITEMS / "BenchRecipe"
UNLOCK_DIR = ITEMS / "ExpeditionUnlock"

CORE = Path("mods/oneblock-core/src/main/resources")
BLOCK_DIR = CORE / "Server/Item/Items/OneBlock"

ENCHANTER_CATEGORY_ORDER = [
    "Surface",
    "Forest",
    "Underground",
    "Cold",
    "Inferno",
    "Dark",
]

WORKBENCH_CATEGORY_ORDER = [
    "Surface",
    "Forest",
    "Underground",
    "Cold",
    "Inferno",
    "Dark",
]


def _safe_eid(expedition_id: str) -> str:
    return expedition_id.replace(" ", "_")


def _safe_drop_id(drop_id: str) -> str:
    return drop_id.replace(":", "_").replace(" ", "_")


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


def _workbench_category_sort_key(category: dict) -> tuple[int, str]:
    category_id = category.get("Id", "")

    prefix = "OneBlock_Workbench_"
    if category_id.startswith(prefix):
        group = category_id[len(prefix):]

        if group in WORKBENCH_CATEGORY_ORDER:
            return (WORKBENCH_CATEGORY_ORDER.index(group), category_id)

    return (len(WORKBENCH_CATEGORY_ORDER), category_id)


def build_crystal(expedition_id: str, category: str, size: str, item_level: int, inputs: list) -> dict:
    eid = _safe_eid(expedition_id)
    cid = _safe_eid(category)
    item_id = f"OneBlock_Crystal_{eid}_{size}"

    return {
        "TranslationProperties": {
            "Name": f"{PREFIX_ITEMS_JSON}.{item_id}.name",
            "Description": f"{PREFIX_ITEMS_JSON}.{item_id}.description",
        },
        "Id": item_id,
        "ItemLevel": item_level,
        "Icon": "Icons/ItemsGenerated/ExpeditionKey.png",
        "Categories": ["Items.OneBlockExpeditionCrystal"],
        "PlayerAnimationsId": "Item",
        "Interactions": {
            "Use": {
                "Interactions": [
                    {
                        "Type": "oneblock_crystal_use",
                    }
                ]
            }
        },
        "Recipe": {
            "Input": inputs,
            "OutputQuantity": 1,
            "BenchRequirement": [
                {
                    "Type": "Crafting",
                    "Categories": [f"OneBlock_Enchanter_{cid}"],
                    "Id": "OneBlockEnchanter",
                }
            ],
        },
        "Consumable": True,
        "Tags": {
            "Type": ["OneBlock_ExpeditionCrystal"],
            "OneBlockCrystalExpedition": [expedition_id],
            "OneBlockCrystalSize": [size],
        },
        "MaxStack": 4,
        "Quality": _quality(item_level),
    }


def build_bench_recipe_item(expedition_id: str, item_level: int) -> dict:
    eid = _safe_eid(expedition_id)
    item_id = f"OneBlock_Bench_Recipe_{eid}"

    return {
        "TranslationProperties": {
            "Name": f"{PREFIX_ITEMS_JSON}.{item_id}.name",
            "Description": f"{PREFIX_ITEMS_JSON}.{item_id}.description",
        },
        "Id": item_id,
        "ItemLevel": item_level,
        "Icon": "Icons/ItemsGenerated/BlockUpgrade.png",
        "Categories": ["Items.RecipeDrop"],
        "PlayerAnimationsId": "Item",
        "Consumable": True,
        "Tags": {
            "Type": ["RecipeDrop"],
            "RecipeDropTarget": [f"Bench_OneBlock_{eid}"],
        },
        "MaxStack": 1,
        "Quality": _quality(item_level),
    }


def build_unlock_item(expedition_id: str, unlock: dict) -> dict:
    eid = _safe_eid(expedition_id)
    drop_id = unlock["DropId"]
    weight = unlock["Weight"]
    tier = unlock["Tier"]
    quality = unlock.get("Rank", "Common")
    cost = unlock["Cost"]

    bench_id = f"Bench_OneBlock_{eid}"
    item_id = f"OneBlock_Unlock_{eid}_{_safe_drop_id(drop_id)}"
    cat_id = f"{bench_id}_Tier{tier}"

    return {
        "TranslationProperties": {
            "Name": f"{PREFIX_ITEMS_JSON}.{item_id}.name",
            "Description": f"{PREFIX_ITEMS_JSON}.{item_id}.description",
        },
        "Id": item_id,
        "ItemLevel": tier,
        "Icon": "Icons/ItemsGenerated/ExpeditionKey.png",
        "Categories": ["Items.RecipeDrop"],
        "PlayerAnimationsId": "Item",
        "Recipe": {
            "Input": cost,
            "OutputQuantity": 1,
            "BenchRequirement": [
                {
                    "Type": "Crafting",
                    "Categories": [cat_id],
                    "Id": bench_id,
                }
            ],
        },
        "Consumable": True,
        "Tags": {
            "Type": ["OneBlock_Unlock_Consumable"],
            "OneBlockUnlockExpedition": [expedition_id],
            "OneBlockUnlockDropId": [drop_id],
            "OneBlockUnlockWeight": [str(weight)],
        },
        "MaxStack": 1,
        "Quality": quality,
    }


def build_expedition_bench(
    expedition_id: str,
    item_level: int,
    craft_input: list,
    unlocks_by_tier: dict,
    group: str,
    knowledge_required: bool,
) -> dict:
    eid = _safe_eid(expedition_id)
    gid = _safe_eid(group)
    bench_id = f"Bench_OneBlock_{eid}"
    item_bench_id = f"Bench_OneBlock_{eid}"

    return {
        "TranslationProperties": {
            "Name": f"{PREFIX_ITEMS_JSON}.{item_bench_id}.name",
            "Description": f"{PREFIX_ITEMS_JSON}.{item_bench_id}.description",
        },
        "Recipe": {
            "Input": craft_input,
            "BenchRequirement": [
                {
                    "Type": "Crafting",
                    "Categories": [f"OneBlock_Workbench_{gid}"],
                    "Id": "OneBlockWorkbench",
                }
            ],
            "KnowledgeRequired": knowledge_required,
        },
        "Icon": "Icons/ItemsGenerated/OneBlockUpgrader.png",
        "Categories": ["Furniture.Benches"],
        "BlockType": {
            "Material": "Solid",
            "DrawType": "Model",
            "Opacity": "Transparent",
            "CustomModel": "Blocks/Benches/Workbench.blockymodel",
            "CustomModelTexture": [
                {
                    "Texture": "Blocks/OneBlockUpgrader_Texture.png",
                    "Weight": 1,
                }
            ],
            "HitboxType": "Bench_Workbench",
            "VariantRotation": "NESW",
            "Bench": {
                "Id": bench_id,
                "Type": "Crafting",
                "Categories": [
                    {
                        "Id": f"{bench_id}_Tier{tier}",
                        "Icon": "Icons/CraftingCategories/ExpeditionKey.png",
                        "Name": f"{PREFIX_BENCH_JSON}.{bench_id}_Tier{tier}",
                        "Recipes": ids,
                    }
                    for tier, ids in sorted(unlocks_by_tier.items())
                ],
                "LocalOpenSoundEventId": "SFX_Workbench_Open",
                "LocalCloseSoundEventId": "SFX_Workbench_Close",
            },
            "State": {
                "Id": "crafting",
                "Definitions": {
                    "CraftCompleted": {
                        "CustomModelAnimation": "Blocks/Benches/Workbench_Crafting.blockyanim",
                        "Looping": True,
                    },
                    "CraftCompletedInstant": {
                        "CustomModelAnimation": "Blocks/Benches/Workbench_Crafting.blockyanim",
                    },
                },
            },
            "BlockEntity": {
                "Components": {
                    "BenchBlock": {},
                }
            },
            "Gathering": {
                "Breaking": {
                    "GatherType": "Benches",
                }
            },
            "BlockParticleSetId": "Wood",
            "ParticleColor": "#6e4a2f",
            "Support": {
                "Down": [
                    {
                        "FaceType": "Full",
                    }
                ]
            },
            "BlockSoundSetId": "Wood",
        },
        "PlayerAnimationsId": "Block",
        "IconProperties": {
            "Scale": 0.42,
            "Rotation": [22.5, 45.0, 22.5],
            "Translation": [12.9, -11.7],
        },
        "Tags": {
            "Type": ["Bench"],
        },
        "MaxStack": 1,
        "ItemLevel": item_level,
        "ItemSoundSetId": "ISS_Blocks_Wood",
    }


def build_oneblock_block(expedition_id: str, item_level: int) -> dict:
    eid = _safe_eid(expedition_id)
    item_id = f"OneBlock_Block_{eid}"

    return {
        "TranslationProperties": {
            "Name": f"{PREFIX_ITEMS_JSON}.{item_id}.name",
        },
        "Id": item_id,
        "ItemLevel": item_level,
        "Icon": f"Icons/ItemsGenerated/OneBlock_{eid}.png",
        "Categories": ["Blocks.OneBlock"],
        "PlayerAnimationsId": "Block",
        "Set": "OneBlock",
        "BlockType": {
            "Material": "Solid",
            "DrawType": "Cube",
            "Group": "OneBlock",
            "Flags": {},
            "Gathering": {
                "Breaking": {
                    "GatherType": "Soils",
                    "ItemId": "Soil_Dirt",
                    "Quantity": 0,
                }
            },
            "Textures": [
                {
                    "Down": f"BlockTextures/{item_id}.png",
                    "Sides": f"BlockTextures/{item_id}.png",
                    "Up": f"BlockTextures/{item_id}.png",
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


def build_lang_block(expedition_id: str) -> str:
    eid = _safe_eid(expedition_id)
    display = expedition_id
    sep = "─" * max(0, 55 - len(display))

    lines = [
        f"\n# GENERATED ─── {display} {sep}",
        f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}_Small.name={display} Crystal (Small)",
        f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}_Small.description=Consume to begin a 100-tick {display} expedition.",
        f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}_Large.name={display} Crystal (Large)",
		f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}_Large.description=Consume to begin a 300-tick {display} expedition.",
        f"{PREFIX_ITEMS_LANG}.Bench_OneBlock_{eid}.name={display} Bench",
        f"{PREFIX_ITEMS_LANG}.Bench_OneBlock_{eid}.description=An expedition bench for {display}. Upgrade it to unlock more powerful recipes.",
        f"{PREFIX_ITEMS_LANG}.OneBlock_Bench_Recipe_{eid}.name=Recipe: {display} Bench",
        f"{PREFIX_ITEMS_LANG}.OneBlock_Bench_Recipe_{eid}.description=Consume to unlock the {display} Bench crafting recipe at the OneBlock Workbench.",
        f"{PREFIX_BENCH_LANG}.Bench_OneBlock_{eid}_Tier1={display} Recipes",
    ]

    return "\n".join(lines)


JAVA_ENTITY_PREFIX = "entity:"


def _java_drop_expr(drop_id: str, weight: int) -> str:
    if drop_id.startswith(JAVA_ENTITY_PREFIX):
        entity = drop_id[len(JAVA_ENTITY_PREFIX):]
        return f'drop(OneBlockDropId.entityDropId("{entity}"), {weight})'

    return f'drop("{drop_id}", {weight})'


def build_java_defaults_block(all_expeditions: list[tuple[str, list]]) -> str:
    lines = [
        "    static",
        "    {",
        "        Map<String, List<DropDefinition>> defaults = new HashMap<>();",
    ]

    for expedition_id, drop_pool in all_expeditions:
        lines.append("")

        entries = [
            f"                {_java_drop_expr(entry['ID'], entry['Weight'])}"
            for entry in drop_pool
        ]

        lines.append(f'        defaults.put("{expedition_id}", List.of(')
        lines.append(",\n".join(entries))
        lines.append("        ));")

    lines += [
        "",
        "        DEFAULTS = Collections.unmodifiableMap(defaults);",
        "        DEFAULT_IDS = buildDefaultIds(DEFAULTS);",
        "        DEFAULT_WEIGHTS = buildDefaultWeights(DEFAULTS);",
        "    }",
    ]

    return "\n".join(lines)


def _load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def _save_json(path: Path, data: dict, dry_run: bool):
    if dry_run:
        print(f"  [dry-run] Would overwrite {path}")
        return

    path.write_bytes(
        (json.dumps(data, indent=2, ensure_ascii=False) + "\n").encode("utf-8")
    )
    print(f"  [patch]  {path}")


def patch_enchanter(path: Path, expedition_id: str, group: str, dry_run: bool):
    eid = _safe_eid(expedition_id)
    gid = _safe_eid(group)

    cat_id = f"OneBlock_Enchanter_{gid}"
    lang_key = f"OneBlockEnchanter_{gid}"

    data = _load_json(path)
    categories = data["BlockType"]["Bench"]["Categories"]

    cat = next((candidate for candidate in categories if candidate["Id"] == cat_id), None)

    if cat is None:
        cat = {
            "Id": cat_id,
            "Icon": "Icons/CraftingCategories/ExpeditionKey.png",
            "Name": f"{PREFIX_BENCH_JSON}.{lang_key}",
            "Recipes": [],
        }
        categories.append(cat)
    else:
        cat["Name"] = f"{PREFIX_BENCH_JSON}.{lang_key}"

    for recipe_id in [
        f"OneBlock_Crystal_{eid}_Small",
        f"OneBlock_Crystal_{eid}_Large",
    ]:
        if recipe_id not in cat["Recipes"]:
            cat["Recipes"].append(recipe_id)

    categories.sort(key=_category_sort_key)

    _save_json(path, data, dry_run)
    print(f"  [patch]  Enchanter ← {eid} → group {gid}")


def patch_workbench(path: Path, expedition_id: str, group: str, dry_run: bool):
    eid = _safe_eid(expedition_id)
    gid = _safe_eid(group)

    data = _load_json(path)
    categories = data["BlockType"]["Bench"]["Categories"]

    cat_id = f"OneBlock_Workbench_{gid}"
    lang_key = f"OneBlockWorkbench_{gid}"
    bench_item_id = f"Bench_OneBlock_{eid}"

    cat = next((candidate for candidate in categories if candidate["Id"] == cat_id), None)

    if cat is None:
        cat = {
            "Id": cat_id,
            "Icon": "Icons/CraftingCategories/ExpeditionKey.png",
            "Name": f"{PREFIX_BENCH_JSON}.{lang_key}",
            "Recipes": [],
        }
        categories.append(cat)
    else:
        cat["Name"] = f"{PREFIX_BENCH_JSON}.{lang_key}"

    if bench_item_id not in cat["Recipes"]:
        cat["Recipes"].append(bench_item_id)

    categories.sort(key=_workbench_category_sort_key)

    _save_json(path, data, dry_run)
    print(f"  [patch]  Workbench ← {eid} → group {gid}")


def append_lang_recipe(
    path: Path,
    item_id: str,
    name: str,
    description: str,
    dry_run: bool,
):
    existing = path.read_text(encoding="utf-8")
    marker = f"{PREFIX_ITEMS_LANG}.{item_id}.name"

    if marker in existing:
        print(f"  [skip]   Lang already has entry for {item_id}")
        return

    lines = [
        f"{PREFIX_ITEMS_LANG}.{item_id}.name={name}",
        f"{PREFIX_ITEMS_LANG}.{item_id}.description={description}",
    ]

    if dry_run:
        print(f"  [dry-run] Would append lang entry for {item_id}")
        return

    path.write_text(
        existing.rstrip() + "\n" + "\n".join(lines) + "\n",
        encoding="utf-8",
    )
    print(f"  [patch]  lang ← {item_id}")


def patch_lang(path: Path, expedition_id: str, dry_run: bool):
    eid = _safe_eid(expedition_id)

    existing = path.read_text(encoding="utf-8")
    marker = f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}_Small.name"

    if marker in existing:
        print(f"  [skip]   Lang already has entries for {expedition_id}")
        return

    block = build_lang_block(expedition_id)

    if dry_run:
        print(f"  [dry-run] Would append lang entries for {expedition_id}")
        return

    path.write_text(existing.rstrip() + "\n" + block + "\n", encoding="utf-8")
    print(f"  [patch]  lang ← {expedition_id}")


def patch_group_lang(path: Path, group: str, dry_run: bool):
    gid = _safe_eid(group)

    entries = {
        f"{PREFIX_BENCH_LANG}.OneBlockEnchanter_{gid}": f"{group} Crystals",
        f"{PREFIX_BENCH_LANG}.OneBlockWorkbench_{gid}": f"{group} Expedition Benches",
    }

    existing = path.read_text(encoding="utf-8")
    missing_lines = []

    for key, value in entries.items():
        if key not in existing:
            missing_lines.append(f"{key}={value}")

    if not missing_lines:
        return

    if dry_run:
        for line in missing_lines:
            print(f"  [dry-run] Would append lang entry for {line.split('=', 1)[0]}")
        return

    path.write_text(
        existing.rstrip() + "\n" + "\n".join(missing_lines) + "\n",
        encoding="utf-8",
    )

    for line in missing_lines:
        print(f"  [patch]  lang ← {line.split('=', 1)[0]}")


def write_json(path: Path, data: dict, dry_run: bool):
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
        (
            key.startswith(f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_")
            and ("_Small." in key or "_Large." in key)
        )
        or key.startswith(f"{PREFIX_ITEMS_LANG}.Bench_OneBlock_")
        or key.startswith(f"{PREFIX_ITEMS_LANG}.OneBlock_Bench_Recipe_")
        or key.startswith(f"{PREFIX_ITEMS_LANG}.OneBlock_Unlock_")
        or key.startswith(f"{PREFIX_BENCH_LANG}.OneBlockEnchanter_")
        or key.startswith(f"{PREFIX_BENCH_LANG}.OneBlockWorkbench_")
        or key.startswith(f"{PREFIX_BENCH_LANG}.Bench_OneBlock_")
        or (
            key.startswith(f"{PREFIX_ITEMS_JSON}.OneBlock_Crystal_")
            and ("_Small." in key or "_Large." in key)
        )
        or key.startswith(f"{PREFIX_ITEMS_JSON}.Bench_OneBlock_")
        or key.startswith(f"{PREFIX_ITEMS_JSON}.OneBlock_Bench_Recipe_")
        or key.startswith(f"{PREFIX_ITEMS_JSON}.OneBlock_Unlock_")
        or key.startswith(f"{PREFIX_BENCH_JSON}.OneBlockEnchanter_")
        or key.startswith(f"{PREFIX_BENCH_JSON}.OneBlockWorkbench_")
        or key.startswith(f"{PREFIX_BENCH_JSON}.Bench_OneBlock_")
    )


def cleanup(repo_root: Path, dry_run: bool):
    print("\n=== Cleanup ===")

    for gen_dir in [CRYSTAL_DIR, BENCH_DIR, RECIPE_DIR, UNLOCK_DIR, BLOCK_DIR]:
        dir_path = repo_root / gen_dir

        if not dir_path.exists():
            continue

        files = list(dir_path.glob("*.json"))

        if not files:
            continue

        for file_path in files:
            if dry_run:
                print(f"  [dry-run] Would delete {gen_dir.name}/{file_path.name}")
            else:
                file_path.unlink()

        if not dry_run:
            print(f"  [clean]  Deleted {len(files)} file(s) from {gen_dir.name}/")

    enchanter_path = repo_root / ENCHANTER

    if enchanter_path.exists():
        data = _load_json(enchanter_path)
        categories = data["BlockType"]["Bench"]["Categories"]

        before = len(categories)

        data["BlockType"]["Bench"]["Categories"] = [
            category
            for category in categories
            if not category.get("Id", "").startswith("OneBlock_Enchanter_")
        ]

        removed = before - len(data["BlockType"]["Bench"]["Categories"])

        if removed:
            _save_json(enchanter_path, data, dry_run)

            if not dry_run:
                print(f"  [clean]  Removed {removed} category/categories from Enchanter")

    workbench_path = repo_root / WORKBENCH

    if workbench_path.exists():
        data = _load_json(workbench_path)
        categories = data["BlockType"]["Bench"]["Categories"]

        before = len(categories)

        data["BlockType"]["Bench"]["Categories"] = [
            category
            for category in categories
            if not category.get("Id", "").startswith("OneBlock_Workbench_")
        ]

        removed = before - len(data["BlockType"]["Bench"]["Categories"])

        if removed:
            _save_json(workbench_path, data, dry_run)

            if not dry_run:
                print(f"  [clean]  Removed {removed} category/categories from Workbench")

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

    parser.add_argument(
        "input",
        help="Input JSON, for example expeditionsTemplate.json",
    )

    parser.add_argument(
        "--repo-root",
        default=".",
        help="Repo root directory. Default: .",
    )

    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Show what would be done without writing files",
    )

    args = parser.parse_args()

    repo_root = Path(args.repo_root).resolve()
    input_path = Path(args.input)

    if not input_path.is_absolute():
        input_path = repo_root / input_path

    raw = json.loads(input_path.read_text(encoding="utf-8"))

    expeditions = {
        key: value
        for key, value in raw.items()
        if not key.startswith("_")
    }

    enchanter_path = repo_root / ENCHANTER
    workbench_path = repo_root / WORKBENCH
    lang_path = repo_root / LANG_FILE

    cleanup(repo_root, args.dry_run)

    java_defaults_path = repo_root / Path(
        "mods/oneblock-progression/src/main/java"
        "/com/EreliaStudio/OneBlock/OneBlockExpeditionDefaults.java"
    )

    all_expedition_drops: list[tuple[str, list]] = []

    for expedition_id, cfg in expeditions.items():
        print(f"\n=== {expedition_id} ===")

        item_level = cfg["ItemLevel"]
        crystal_cfg = cfg["Crystal"]
        bench_cfg = cfg["Bench"]
        drop_pool = cfg["BaseDropPool"]
        group = cfg.get("Category", cfg.get("Group", expedition_id))

        eid = _safe_eid(expedition_id)

        write_json(
            repo_root / BLOCK_DIR / f"OneBlock_Block_{eid}.json",
            build_oneblock_block(expedition_id, item_level),
            args.dry_run,
        )

        write_json(
            repo_root / CRYSTAL_DIR / f"OneBlock_Crystal_{eid}_Small.json",
            build_crystal(
                expedition_id,
                group,
                "Small",
                item_level,
                crystal_cfg["Small"]["Input"],
            ),
            args.dry_run,
        )

        write_json(
            repo_root / CRYSTAL_DIR / f"OneBlock_Crystal_{eid}_Large.json",
            build_crystal(
                expedition_id,
                group,
                "Large",
                item_level,
                crystal_cfg["Large"]["Input"],
            ),
            args.dry_run,
        )

        unlocks_by_tier: dict[int, list[str]] = {}

        for unlock in bench_cfg.get("Unlocks", []):
            drop_id = unlock["DropId"]
            tier = unlock["Tier"]
            item_id = f"OneBlock_Unlock_{eid}_{_safe_drop_id(drop_id)}"
            drop_name = drop_id.split(":")[-1].replace("_", " ")

            write_json(
                repo_root / UNLOCK_DIR / f"{item_id}.json",
                build_unlock_item(expedition_id, unlock),
                args.dry_run,
            )

            if lang_path.exists():
                append_lang_recipe(
                    lang_path,
                    item_id,
                    f"Unlock: {drop_name}",
                    f"Consume to add {drop_name} to the {expedition_id} loot pool "
                    f"(weight {unlock['Weight']}).",
                    args.dry_run,
                )

            unlocks_by_tier.setdefault(tier, []).append(item_id)

        write_json(
            repo_root / BENCH_DIR / f"Bench_OneBlock_{eid}.json",
            build_expedition_bench(
                expedition_id,
                item_level,
                bench_cfg["CraftInput"],
                unlocks_by_tier,
                group,
                bench_cfg.get("KnowledgeRequired", True),
            ),
            args.dry_run,
        )

        write_json(
            repo_root / RECIPE_DIR / f"OneBlock_Bench_Recipe_{eid}.json",
            build_bench_recipe_item(expedition_id, item_level),
            args.dry_run,
        )

        if enchanter_path.exists():
            patch_enchanter(
                enchanter_path,
                expedition_id,
                group,
                args.dry_run,
            )
        else:
            print(f"  [warn]   Enchanter JSON not found: {enchanter_path}")

        if workbench_path.exists():
            patch_workbench(
                workbench_path,
                expedition_id,
                group,
                args.dry_run,
            )
        else:
            print(f"  [warn]   Workbench JSON not found: {workbench_path}")

        if lang_path.exists():
            patch_lang(lang_path, expedition_id, args.dry_run)
            patch_group_lang(lang_path, group, args.dry_run)
        else:
            print(f"  [warn]   Lang file not found: {lang_path}")

        all_expedition_drops.append((expedition_id, drop_pool))

    static_block = build_java_defaults_block(all_expedition_drops)

    if java_defaults_path.exists():
        source = java_defaults_path.read_text(encoding="utf-8")

        if not re.search(r"    static\s*\{.*?    \}", source, re.DOTALL):
            print(
                "\n[warn]  Could not locate static block in "
                "OneBlockExpeditionDefaults.java — printing to stdout instead."
            )
            print(static_block)
        else:
            new_source = re.sub(
                r"    static\s*\{.*?    \}",
                static_block,
                source,
                count=1,
                flags=re.DOTALL,
            )

            if new_source == source:
                print(
                    "\n[skip]   OneBlockExpeditionDefaults.java static block "
                    "already up to date"
                )
            elif args.dry_run:
                print(
                    "\n[dry-run] Would overwrite static block in "
                    "OneBlockExpeditionDefaults.java"
                )
            else:
                java_defaults_path.write_text(new_source, encoding="utf-8")
                print(
                    "\n[write]  OneBlockExpeditionDefaults.java static block "
                    f"updated ({len(all_expedition_drops)} expeditions)"
                )
    else:
        print(
            f"\n[warn]  {java_defaults_path} not found — "
            "printing static block to stdout:"
        )
        print(static_block)

    print(f"\nDone. {len(expeditions)} expedition(s) processed.")


if __name__ == "__main__":
    main()