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
PREFIX_ITEMS_JSON = "server.items"
PREFIX_BENCH_JSON = "server.benchCategories"

PREFIX_ITEMS_LANG = "items"
PREFIX_BENCH_LANG = "benchCategories"


# ── Repo-relative paths ───────────────────────────────────────────────────────
PROGRESSION = Path("mods/oneblock-progression/src/main/resources")
ITEMS = PROGRESSION / "Server/Item/Items"
LANG_FILE = PROGRESSION / "Server/Languages/en-US/server.lang"
ENCHANTER = ITEMS / "OneBlockEnchanter/Bench_OneBlockEnchanter.json"
CRYSTAL_DIR = ITEMS / "Crystal/Expedition"

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


def build_crystal(expedition_id: str, category: str, item_level: int, inputs: list) -> dict:
    eid = _safe_eid(expedition_id)
    gid = _safe_eid(category)
    item_id = f"OneBlock_Crystal_{eid}"
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
            "Primary": {
                "Interactions": [{"Type": "oneblock_crystal_use"}]
            },
            "Secondary": {
                "Interactions": [{"Type": "oneblock_crystal_use"}]
            },
        },
        "Recipe": {
            "Input": inputs,
            "OutputQuantity": 1,
            "BenchRequirement": [
                {
                    "Type": "Crafting",
                    "Categories": [f"OneBlock_Enchanter_{gid}"],
                    "Id": "OneBlockEnchanter",
                }
            ],
        },
        "Consumable": True,
        "Tags": {
            "Type": ["OneBlock_ExpeditionCrystal"],
            "OneBlockCrystalExpedition": [expedition_id],
        },
        "MaxStack": 4,
        "Quality": _quality(item_level),
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
        f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}.name={display} Crystal",
        f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}.description=Consume to begin a {display} expedition.",
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
        "        Map<String, ExpeditionDefinition> expeditions = new HashMap<>();",
    ]

    for expedition_id, drop_pool in all_expeditions:
        lines.append("")

        entries = [
            f"                {_java_drop_expr(entry['ID'], entry['Weight'])}"
            for entry in drop_pool
        ]

        lines.append(f'        register(expeditions, "{expedition_id}", List.of(')
        lines.append(",\n".join(entries))
        lines.append("        ));")

    lines += [
        "",
        "        EXPEDITIONS = Collections.unmodifiableMap(expeditions);",
        "        DEFAULT_IDS = buildDefaultIds(EXPEDITIONS);",
        "        DEFAULT_WEIGHTS = buildDefaultWeights(EXPEDITIONS);",
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

    cat = next((c for c in categories if c["Id"] == cat_id), None)
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

    recipe_id = f"OneBlock_Crystal_{eid}"
    if recipe_id not in cat["Recipes"]:
        cat["Recipes"].append(recipe_id)

    categories.sort(key=_category_sort_key)
    _save_json(path, data, dry_run)
    print(f"  [patch]  Enchanter ← {eid} → group {gid}")


def patch_lang(path: Path, expedition_id: str, dry_run: bool):
    eid = _safe_eid(expedition_id)
    existing = path.read_text(encoding="utf-8")
    marker = f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_{eid}.name"

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
    key = f"{PREFIX_BENCH_LANG}.OneBlockEnchanter_{gid}"
    value = f"{group} Crystals"

    existing = path.read_text(encoding="utf-8")
    if key in existing:
        return

    line = f"{key}={value}"

    if dry_run:
        print(f"  [dry-run] Would append lang entry for {key}")
        return

    path.write_text(existing.rstrip() + "\n" + line + "\n", encoding="utf-8")
    print(f"  [patch]  lang ← {key}")


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
        key.startswith(f"{PREFIX_ITEMS_LANG}.OneBlock_Crystal_")
        or key.startswith(f"{PREFIX_BENCH_LANG}.OneBlockEnchanter_")
        or key.startswith(f"{PREFIX_ITEMS_JSON}.OneBlock_Crystal_")
        or key.startswith(f"{PREFIX_BENCH_JSON}.OneBlockEnchanter_")
    )


def cleanup(repo_root: Path, dry_run: bool):
    print("\n=== Cleanup ===")

    for gen_dir in [CRYSTAL_DIR, BLOCK_DIR]:
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
            c for c in categories
            if not c.get("Id", "").startswith("OneBlock_Enchanter_")
        ]
        removed = before - len(data["BlockType"]["Bench"]["Categories"])
        if removed:
            _save_json(enchanter_path, data, dry_run)
            if not dry_run:
                print(f"  [clean]  Removed {removed} category/categories from Enchanter")

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
    parser.add_argument("--dry-run", action="store_true", help="Show what would be done without writing files")
    args = parser.parse_args()

    repo_root = Path(args.repo_root).resolve()
    input_path = Path(args.input)
    if not input_path.is_absolute():
        input_path = repo_root / input_path

    raw = json.loads(input_path.read_text(encoding="utf-8-sig"))
    expeditions = {k: v for k, v in raw.items() if not k.startswith("_")}

    enchanter_path = repo_root / ENCHANTER
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
        drop_pool = cfg["BaseDropPool"]
        group = cfg.get("Category", cfg.get("Group", expedition_id))
        eid = _safe_eid(expedition_id)

        write_json(
            repo_root / BLOCK_DIR / f"OneBlock_Block_{eid}.json",
            build_oneblock_block(expedition_id, item_level),
            args.dry_run,
        )

        write_json(
            repo_root / CRYSTAL_DIR / f"OneBlock_Crystal_{eid}.json",
            build_crystal(expedition_id, group, item_level, crystal_cfg["Input"]),
            args.dry_run,
        )

        if enchanter_path.exists():
            patch_enchanter(enchanter_path, expedition_id, group, args.dry_run)
        else:
            print(f"  [warn]   Enchanter JSON not found: {enchanter_path}")

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
                print("\n[skip]   OneBlockExpeditionDefaults.java static block already up to date")
            elif args.dry_run:
                print("\n[dry-run] Would overwrite static block in OneBlockExpeditionDefaults.java")
            else:
                java_defaults_path.write_text(new_source, encoding="utf-8")
                print(
                    f"\n[write]  OneBlockExpeditionDefaults.java static block "
                    f"updated ({len(all_expedition_drops)} expeditions)"
                )
    else:
        print(f"\n[warn]  {java_defaults_path} not found — printing static block to stdout:")
        print(static_block)

    print(f"\nDone. {len(expeditions)} expedition(s) processed.")


if __name__ == "__main__":
    main()
