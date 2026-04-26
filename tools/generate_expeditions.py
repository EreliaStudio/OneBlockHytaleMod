#!/usr/bin/env python3
"""
generate_expeditions.py  —  OneBlock Expedition Asset Generator

Usage:
    python tools/generate_expeditions.py <input.json> [--repo-root <path>] [--dry-run]

For each expedition in the input JSON the script creates / overwrites:
  • Crystal/Expedition/OneBlock_Crystal_<Id>_Small.json
  • Crystal/Expedition/OneBlock_Crystal_<Id>_Large.json
  • ExpeditionBench/Bench_OneBlock_<Id>.json          (categories auto-built from Unlocks tiers)
  • BenchRecipe/OneBlock_Bench_Recipe_<Id>.json
  • ExpeditionUnlock/OneBlock_Unlock_<Id>_<Drop>.json  (one per Unlocks entry)

Patches shared files:
  • Bench_OneBlockEnchanter.json  (adds category + two recipe IDs)
  • Bench_OneBlockWorkbench.json  (appends bench ID to recipe list)
  • server.lang                   (appends translation keys)

Prints a Java snippet for OneBlockExpeditionDefaults.java to stdout.

Input JSON schema  (see expeditionsTemplate.json for a full example):

{
  "<ExpeditionId>": {
    "ItemLevel": <int>,
    "Crystal": {
      "Small": { "Input": [ { "ItemId": "...", "Quantity": <int> } ] },
      "Large": { "Input": [ ... ] }
    },
    "Bench": {
      "CraftInput": [ { "ItemId": "...", "Quantity": <int> } ],
      "Upgrades": [
        { "Input": [ ... ], "TimeSeconds": <int> },
        { "Input": [ ... ], "TimeSeconds": <int> }
      ],
      "Unlocks": [
        {
          "DropId":  "<item-id or entity:X>",
          "Weight":  <int>,
          "Tier":    <1|2|3>,
          "Rank":    "Common|Uncommon|Rare|Epic",
          "Cost":    [ { "ItemId": "...", "Quantity": <int> } ]
        }
      ]
    },
    "BaseDropPool": [
      { "ID": "<item-or-entity-id>", "Weight": <int> }
    ]
  }
}

Expedition IDs may contain spaces (e.g. "Deep Cave", "The Abyss").
Keys starting with "_" are treated as comments and skipped.
"""

import argparse
import json
import sys
from pathlib import Path

# Ensure stdout handles Unicode on Windows
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

# ── Repo-relative paths ───────────────────────────────────────────────────────
PROGRESSION  = Path("mods/oneblock-progression/src/main/resources")
ITEMS        = PROGRESSION / "Server/Item/Items"
LANG_FILE    = PROGRESSION / "Server/Languages/en-US/server.lang"
ENCHANTER    = ITEMS / "OneBlockEnchanter/Bench_OneBlockEnchanter.json"
WORKBENCH    = ITEMS / "OneBlockWorkbench/Bench_OneBlockWorkbench.json"
CRYSTAL_DIR   = ITEMS / "Crystal/Expedition"
BENCH_DIR     = ITEMS / "ExpeditionBench"
RECIPE_DIR    = ITEMS / "BenchRecipe"
UNLOCK_DIR    = ITEMS / "ExpeditionUnlock"


# ── Quality helper ────────────────────────────────────────────────────────────
def _quality(item_level: int) -> str:
    if item_level <= 4: return "Uncommon"
    if item_level <= 7: return "Rare"
    return "Epic"


# ── JSON builders ─────────────────────────────────────────────────────────────

def build_crystal(expedition_id: str, size: str, item_level: int, inputs: list) -> dict:
    item_id = f"OneBlock_Crystal_{expedition_id}_{size}"
    return {
        "TranslationProperties": {
            "Name":        f"server.items.{item_id}.name",
            "Description": f"server.items.{item_id}.description"
        },
        "Id":       item_id,
        "ItemLevel": item_level,
        "Icon":     "Icons/ItemsGenerated/Crystal_Expedition.png",
        "Categories": ["Items.OneBlockExpeditionCrystal"],
        "PlayerAnimationsId": "Item",
        "BlockType": {
            "DrawType": "Model",
            "Material": "Solid",
            "Opacity":  "Transparent",
            "CustomModel": "Blocks/Miscellaneous/Portal_Shard.blockymodel",
            "CustomModelTexture": [
                {"Texture": "Blocks/Miscellaneous/Portal_Shard_Texture.png", "Weight": 1}
            ]
        },
        "Interactions": {
            "Use": {"Interactions": [{"Type": "oneblock_crystal_use"}]}
        },
        "Recipe": {
            "Input": inputs,
            "OutputQuantity": 1,
            "BenchRequirement": [
                {
                    "Type": "Crafting",
                    "Categories": [f"OneBlock_Enchanter_{expedition_id}"],
                    "Id": "OneBlockEnchanter"
                }
            ]
        },
        "Consumable": True,
        "Tags": {
            "Type": ["OneBlock_ExpeditionCrystal"],
            "OneBlockCrystalExpedition": [expedition_id],
            "OneBlockCrystalSize": [size]
        },
        "MaxStack": 4,
        "Quality": _quality(item_level)
    }


def build_bench_recipe_item(expedition_id: str, item_level: int) -> dict:
    item_id = f"OneBlock_Bench_Recipe_{expedition_id}"
    return {
        "TranslationProperties": {
            "Name":        f"server.items.{item_id}.name",
            "Description": f"server.items.{item_id}.description"
        },
        "Id":       item_id,
        "ItemLevel": item_level,
        "Icon":     "Icons/ItemsGenerated/RecipeDrop.png",
        "Categories": ["Items.RecipeDrop"],
        "PlayerAnimationsId": "Item",
        "Consumable": True,
        "Tags": {
            "Type": ["RecipeDrop"],
            "RecipeDropTarget": [f"Bench_OneBlock_{expedition_id}"]
        },
        "MaxStack": 1,
        "Quality": _quality(item_level)
    }


def _safe_drop_id(drop_id: str) -> str:
    """Turn a drop ID (including entity:X) into a filesystem/item-ID-safe string."""
    return drop_id.replace(":", "_").replace(" ", "_")


def build_unlock_item(expedition_id: str, unlock: dict) -> dict:
    """Build the consumable unlock item that adds a drop to the expedition loot table."""
    drop_id   = unlock["DropId"]
    weight    = unlock["Weight"]
    tier      = unlock["Tier"]
    quality   = unlock.get("Rank", "Common")
    cost      = unlock["Cost"]
    bench_id  = f"OneBlock_Bench_{expedition_id}"
    item_id   = f"OneBlock_Unlock_{expedition_id}_{_safe_drop_id(drop_id)}"
    cat_id    = f"{bench_id}_Tier{tier}"

    # Human-readable drop name for display (strip entity: prefix)
    drop_name = drop_id.split(":")[-1].replace("_", " ")

    return {
        "TranslationProperties": {
            "Name":        f"server.items.{item_id}.name",
            "Description": f"server.items.{item_id}.description"
        },
        "Id":       item_id,
        "ItemLevel": tier,
        "Icon":     "Icons/ItemsGenerated/RecipeDrop.png",
        "Categories": ["Items.RecipeDrop"],
        "PlayerAnimationsId": "Item",
        "Recipe": {
            "Input":          cost,
            "OutputQuantity": 1,
            "BenchRequirement": [
                {
                    "Type":       "Crafting",
                    "Categories": [cat_id],
                    "Id":         bench_id
                }
            ]
        },
        "Consumable": True,
        "Tags": {
            "Type":                       ["OneBlock_Unlock_Consumable"],
            "OneBlockUnlockExpedition":   [expedition_id],
            "OneBlockUnlockDropId":       [drop_id],
            "OneBlockUnlockWeight":       [str(weight)]
        },
        "MaxStack": 1,
        "Quality":  quality
    }


def build_expedition_bench(expedition_id: str, item_level: int,
                           craft_input: list, upgrades: list,
                           unlocks_by_tier: dict) -> dict:
    bench_id      = f"OneBlock_Bench_{expedition_id}"
    item_bench_id = f"Bench_OneBlock_{expedition_id}"

    tier_levels = []
    for upg in upgrades:
        tier_levels.append({
            "UpgradeRequirement": {
                "Input":       upg["Input"],
                "TimeSeconds": upg["TimeSeconds"]
            }
        })
    tier_levels.append({"UpgradeRequirement": {"Input": [], "TimeSeconds": 0}})

    return {
        "TranslationProperties": {
            "Name":        f"server.items.{item_bench_id}.name",
            "Description": f"server.items.{item_bench_id}.description"
        },
        "Id":       item_bench_id,
        "ItemLevel": item_level,
        "Icon":     "Icons/ItemsGenerated/OneBlockUpgrader.png",
        "Categories": ["Furniture.Benches"],
        "Recipe": {
            "Input": craft_input,
            "BenchRequirement": [
                {
                    "Type": "Crafting",
                    "Categories": ["OneBlock_Workbench_ExpeditionBenches"],
                    "Id": "OneBlockWorkbench"
                }
            ],
            "KnowledgeRequired": True
        },
        "BlockType": {
            "Material": "Solid",
            "DrawType":  "Model",
            "Opacity":   "Transparent",
            "CustomModel": "Blocks/Benches/Workbench.blockymodel",
            "CustomModelTexture": [
                {"Texture": "Blocks/OneBlockUpgrader_Texture.png", "Weight": 1}
            ],
            "HitboxType":      "Bench_Workbench",
            "VariantRotation": "NESW",
            "Bench": {
                "Id":         bench_id,
                "Type":       "Crafting",
                "TierLevels": tier_levels,
                "Categories": [
                    {
                        "Id":      f"{bench_id}_Tier{t}",
                        "Icon":    "Icons/CraftingCategories/ExpeditionKey.png",
                        "Name":    f"server.benchCategories.{bench_id}_Tier{t}",
                        "Recipes": ids
                    }
                    for t, ids in sorted(unlocks_by_tier.items())
                ],
                "LocalOpenSoundEventId":  "SFX_Workbench_Open",
                "LocalCloseSoundEventId": "SFX_Workbench_Close"
            },
            "State": {
                "Id": "crafting",
                "Definitions": {
                    "CraftCompleted": {
                        "CustomModelAnimation": "Blocks/Benches/Workbench_Crafting.blockyanim",
                        "Looping": True
                    },
                    "CraftCompletedInstant": {
                        "CustomModelAnimation": "Blocks/Benches/Workbench_Crafting.blockyanim"
                    }
                }
            },
            "Gathering": {"Breaking": {"GatherType": "Benches"}},
            "BlockParticleSetId": "Wood",
            "Support": {"Down": [{"FaceType": "Full"}]},
            "BlockSoundSetId": "Wood"
        },
        "PlayerAnimationsId": "Block",
        "Tags": {"Type": ["Bench"]},
        "MaxStack": 1,
        "ItemSoundSetId": "ISS_Blocks_Wood"
    }


# ── Lang builder ──────────────────────────────────────────────────────────────

def build_lang_block(expedition_id: str) -> str:
    eid   = expedition_id
    sep   = "─" * max(0, 55 - len(eid))
    lines = [
        f"\n# GENERATED ─── {eid} {sep}",
        f"server.items.OneBlock_Crystal_{eid}_Small.name={eid} Crystal (Small)",
        f"server.items.OneBlock_Crystal_{eid}_Small.description=Use on the OneBlock to begin a 100-tick {eid} expedition.",
        f"server.items.OneBlock_Crystal_{eid}_Large.name={eid} Crystal (Large)",
        f"server.items.OneBlock_Crystal_{eid}_Large.description=Use on the OneBlock to begin a 300-tick {eid} expedition.",
        f"server.items.Bench_OneBlock_{eid}.name={eid} Bench",
        f"server.items.Bench_OneBlock_{eid}.description=An expedition bench for {eid}. Upgrade it to unlock more powerful recipes.",
        f"server.items.OneBlock_Bench_Recipe_{eid}.name=Recipe: {eid} Bench",
        f"server.items.OneBlock_Bench_Recipe_{eid}.description=Consume to unlock the {eid} Bench crafting recipe at the OneBlock Workbench.",
        f"server.benchCategories.OneBlockEnchanter_{eid}={eid} Crystals",
        f"server.benchCategories.OneBlock_Bench_{eid}_Tier1={eid} Recipes",
    ]
    return "\n".join(lines)


# ── Java snippet builder ──────────────────────────────────────────────────────

JAVA_ENTITY_PREFIX = "entity:"

def _java_drop_expr(drop_id: str, weight: int) -> str:
    """Return the Java drop(...) call for a single drop entry."""
    if drop_id.startswith(JAVA_ENTITY_PREFIX):
        entity = drop_id[len(JAVA_ENTITY_PREFIX):]
        return f'drop(OneBlockDropId.entityDropId("{entity}"), {weight})'
    return f'drop("{drop_id}", {weight})'


def build_java_defaults_block(all_expeditions: list[tuple[str, list]]) -> str:
    """Build the entire static { } block for OneBlockExpeditionDefaults."""
    lines = [
        "    static",
        "    {",
        "        Map<String, List<DropDefinition>> defaults = new HashMap<>();",
    ]
    for expedition_id, drop_pool in all_expeditions:
        lines.append("")
        entries = [f"                {_java_drop_expr(e['ID'], e['Weight'])}" for e in drop_pool]
        inner = ",\n".join(entries)
        lines.append(f'        defaults.put("{expedition_id}", List.of(')
        lines.append(inner)
        lines.append("        ));")
    lines += [
        "",
        "        DEFAULTS = Collections.unmodifiableMap(defaults);",
        "        DEFAULT_IDS = buildDefaultIds(DEFAULTS);",
        "        DEFAULT_WEIGHTS = buildDefaultWeights(DEFAULTS);",
        "    }",
    ]
    return "\n".join(lines)


# ── Patch helpers ─────────────────────────────────────────────────────────────

def _load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))

def _save_json(path: Path, data: dict, dry_run: bool):
    if dry_run:
        print(f"  [dry-run] Would overwrite {path}")
        return
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"  [patch]  {path}")


def patch_enchanter(path: Path, expedition_id: str, dry_run: bool):
    data = _load_json(path)
    categories = data["BlockType"]["Bench"]["Categories"]
    cat_id = f"OneBlock_Enchanter_{expedition_id}"
    if any(c["Id"] == cat_id for c in categories):
        print(f"  [skip]   Enchanter already has category {cat_id}")
        return
    categories.append({
        "Id":   cat_id,
        "Icon": "Icons/CraftingCategories/ExpeditionKey.png",
        "Name": f"server.benchCategories.{cat_id}",
        "Recipes": [
            f"OneBlock_Crystal_{expedition_id}_Small",
            f"OneBlock_Crystal_{expedition_id}_Large"
        ]
    })
    _save_json(path, data, dry_run)


def patch_workbench(path: Path, expedition_id: str, dry_run: bool):
    data    = _load_json(path)
    recipes = data["BlockType"]["Bench"]["Categories"][0]["Recipes"]
    bench_item_id = f"Bench_OneBlock_{expedition_id}"
    if bench_item_id in recipes:
        print(f"  [skip]   Workbench already lists {bench_item_id}")
        return
    recipes.append(bench_item_id)
    _save_json(path, data, dry_run)


def append_lang_recipe(path: Path, item_id: str, name: str, description: str, dry_run: bool):
    existing = path.read_text(encoding="utf-8")
    marker = f"server.items.{item_id}.name"
    if marker in existing:
        print(f"  [skip]   Lang already has entry for {item_id}")
        return
    lines = [
        f"server.items.{item_id}.name={name}",
        f"server.items.{item_id}.description={description}"
    ]
    block = "\n" + "\n".join(lines)
    if dry_run:
        print(f"  [dry-run] Would append lang entry for {item_id}")
        return
    path.write_text(existing.rstrip() + block + "\n", encoding="utf-8")
    print(f"  [patch]  lang ← {item_id}")


def patch_lang(path: Path, expedition_id: str, dry_run: bool):
    existing = path.read_text(encoding="utf-8")
    marker = f"server.items.OneBlock_Crystal_{expedition_id}_Small.name"
    if marker in existing:
        print(f"  [skip]   Lang already has entries for {expedition_id}")
        return
    block = build_lang_block(expedition_id)
    if dry_run:
        print(f"  [dry-run] Would append lang entries for {expedition_id}")
        return
    path.write_text(existing.rstrip() + "\n" + block + "\n", encoding="utf-8")
    print(f"  [patch]  lang ← {expedition_id}")


def write_json(path: Path, data: dict, dry_run: bool):
    if dry_run:
        print(f"  [dry-run] Would write {path.name}")
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"  [write]  {path.name}")


# ── Cleanup ───────────────────────────────────────────────────────────────────

def _is_generated_lang_line(line: str) -> bool:
    """Return True for any line that was written by a previous generator run."""
    stripped = line.strip()
    if stripped.startswith("# GENERATED"):
        return True
    if not stripped or stripped.startswith("#"):
        return False
    if "=" not in stripped:
        return False
    key = stripped.split("=", 1)[0].strip()
    return (
        (key.startswith("server.items.OneBlock_Crystal_") and ("_Small." in key or "_Large." in key)) or
        key.startswith("server.items.Bench_OneBlock_") or
        key.startswith("server.items.OneBlock_Bench_Recipe_") or
        key.startswith("server.items.OneBlock_Unlock_") or
        key.startswith("server.benchCategories.OneBlockEnchanter_") or
        key.startswith("server.benchCategories.OneBlock_Bench_")
    )


def cleanup(repo_root: Path, dry_run: bool):
    print("\n=== Cleanup ===")

    # Delete all generated JSON files from output directories
    for gen_dir in [CRYSTAL_DIR, BENCH_DIR, RECIPE_DIR, UNLOCK_DIR]:
        dir_path = repo_root / gen_dir
        if not dir_path.exists():
            continue
        files = list(dir_path.glob("*.json"))
        if not files:
            continue
        for f in files:
            if dry_run:
                print(f"  [dry-run] Would delete {gen_dir.name}/{f.name}")
            else:
                f.unlink()
        if not dry_run:
            print(f"  [clean]  Deleted {len(files)} file(s) from {gen_dir.name}/")

    # Strip generated categories from the Enchanter
    enchanter_path = repo_root / ENCHANTER
    if enchanter_path.exists():
        data = _load_json(enchanter_path)
        cats = data["BlockType"]["Bench"]["Categories"]
        before = len(cats)
        data["BlockType"]["Bench"]["Categories"] = [
            c for c in cats if not c.get("Id", "").startswith("OneBlock_Enchanter_")
        ]
        removed = before - len(data["BlockType"]["Bench"]["Categories"])
        if removed:
            _save_json(enchanter_path, data, dry_run)
            if not dry_run:
                print(f"  [clean]  Removed {removed} category/categories from Enchanter")

    # Strip generated recipe IDs from the Workbench
    workbench_path = repo_root / WORKBENCH
    if workbench_path.exists():
        data = _load_json(workbench_path)
        recipes = data["BlockType"]["Bench"]["Categories"][0]["Recipes"]
        before = len(recipes)
        data["BlockType"]["Bench"]["Categories"][0]["Recipes"] = [
            r for r in recipes if not r.startswith("Bench_OneBlock_")
        ]
        removed = before - len(data["BlockType"]["Bench"]["Categories"][0]["Recipes"])
        if removed:
            _save_json(workbench_path, data, dry_run)
            if not dry_run:
                print(f"  [clean]  Removed {removed} recipe ID(s) from Workbench")

    # Strip generated lines from the lang file
    lang_path = repo_root / LANG_FILE
    if lang_path.exists():
        lines = lang_path.read_text(encoding="utf-8").splitlines()
        cleaned = [l for l in lines if not _is_generated_lang_line(l)]
        # Collapse runs of more than one blank line
        result, prev_blank = [], False
        for l in cleaned:
            is_blank = not l.strip()
            if is_blank and prev_blank:
                continue
            result.append(l)
            prev_blank = is_blank
        removed = len(lines) - len(result)
        if removed:
            if dry_run:
                print(f"  [dry-run] Would remove ~{removed} generated line(s) from lang")
            else:
                lang_path.write_text("\n".join(result).rstrip() + "\n", encoding="utf-8")
                print(f"  [clean]  Removed {removed} generated line(s) from lang")

    print("  Cleanup done.\n")


# ── Entry point ───────────────────────────────────────────────────────────────

def main():
    parser = argparse.ArgumentParser(
        description="Generate OneBlock expedition assets from a JSON definition file."
    )
    parser.add_argument("input",       help="Input JSON (e.g. expeditionsTemplate.json)")
    parser.add_argument("--repo-root", default=".", help="Repo root directory (default: .)")
    parser.add_argument("--dry-run",   action="store_true",
                        help="Show what would be done without writing files")
    args = parser.parse_args()

    repo_root  = Path(args.repo_root).resolve()
    input_path = Path(args.input)
    if not input_path.is_absolute():
        input_path = repo_root / input_path

    raw = json.loads(input_path.read_text(encoding="utf-8"))
    expeditions = {k: v for k, v in raw.items() if not k.startswith("_")}

    enchanter_path = repo_root / ENCHANTER
    workbench_path = repo_root / WORKBENCH
    lang_path      = repo_root / LANG_FILE

    cleanup(repo_root, args.dry_run)

    java_defaults_path = repo_root / Path(
        "mods/oneblock-progression/src/main/java"
        "/com/EreliaStudio/OneBlock/OneBlockExpeditionDefaults.java"
    )

    all_expedition_drops: list[tuple[str, list]] = []

    for expedition_id, cfg in expeditions.items():
        print(f"\n=== {expedition_id} ===")

        item_level  = cfg["ItemLevel"]
        crystal_cfg = cfg["Crystal"]
        bench_cfg   = cfg["Bench"]
        drop_pool   = cfg["BaseDropPool"]

        # Crystal Small
        write_json(
            repo_root / CRYSTAL_DIR / f"OneBlock_Crystal_{expedition_id}_Small.json",
            build_crystal(expedition_id, "Small", item_level, crystal_cfg["Small"]["Input"]),
            args.dry_run
        )

        # Crystal Large
        write_json(
            repo_root / CRYSTAL_DIR / f"OneBlock_Crystal_{expedition_id}_Large.json",
            build_crystal(expedition_id, "Large", item_level, crystal_cfg["Large"]["Input"]),
            args.dry_run
        )

        # Bench unlock items — group by tier for bench categories
        unlocks_by_tier: dict = {}
        for unlock in bench_cfg.get("Unlocks", []):
            drop_id  = unlock["DropId"]
            tier     = unlock["Tier"]
            item_id  = f"OneBlock_Unlock_{expedition_id}_{_safe_drop_id(drop_id)}"
            drop_name = drop_id.split(":")[-1].replace("_", " ")

            write_json(
                repo_root / UNLOCK_DIR / f"{item_id}.json",
                build_unlock_item(expedition_id, unlock),
                args.dry_run
            )
            if lang_path.exists():
                append_lang_recipe(
                    lang_path,
                    item_id,
                    f"Unlock: {drop_name}",
                    f"Consume to add {drop_name} to the {expedition_id} loot pool (weight {unlock['Weight']}).",
                    args.dry_run
                )

            unlocks_by_tier.setdefault(tier, []).append(item_id)

        # Expedition bench
        write_json(
            repo_root / BENCH_DIR / f"Bench_OneBlock_{expedition_id}.json",
            build_expedition_bench(expedition_id, item_level,
                                   bench_cfg["CraftInput"], bench_cfg["Upgrades"],
                                   unlocks_by_tier),
            args.dry_run
        )

        # Bench recipe drop item
        write_json(
            repo_root / RECIPE_DIR / f"OneBlock_Bench_Recipe_{expedition_id}.json",
            build_bench_recipe_item(expedition_id, item_level),
            args.dry_run
        )

        # Patch shared files
        if enchanter_path.exists():
            patch_enchanter(enchanter_path, expedition_id, args.dry_run)
        else:
            print(f"  [warn]   Enchanter JSON not found: {enchanter_path}")

        if workbench_path.exists():
            patch_workbench(workbench_path, expedition_id, args.dry_run)
        else:
            print(f"  [warn]   Workbench JSON not found: {workbench_path}")

        if lang_path.exists():
            patch_lang(lang_path, expedition_id, args.dry_run)
        else:
            print(f"  [warn]   Lang file not found: {lang_path}")

        all_expedition_drops.append((expedition_id, drop_pool))

    # ── Rewrite OneBlockExpeditionDefaults.java static block ─────────────────
    static_block = build_java_defaults_block(all_expedition_drops)

    if java_defaults_path.exists():
        source = java_defaults_path.read_text(encoding="utf-8")
        # Replace everything between the first "    static" and its closing "    }"
        import re
        new_source = re.sub(
            r"    static\s*\{.*?    \}",
            static_block,
            source,
            count=1,
            flags=re.DOTALL,
        )
        if new_source == source:
            print("\n[warn]  Could not locate static block in OneBlockExpeditionDefaults.java — printing to stdout instead.")
            print(static_block)
        elif args.dry_run:
            print("\n[dry-run] Would overwrite static block in OneBlockExpeditionDefaults.java")
        else:
            java_defaults_path.write_text(new_source, encoding="utf-8")
            print(f"\n[write]  OneBlockExpeditionDefaults.java static block updated ({len(all_expedition_drops)} expeditions)")
    else:
        print(f"\n[warn]  {java_defaults_path} not found — printing static block to stdout:")
        print(static_block)

    print(f"\nDone. {len(expeditions)} expedition(s) processed.")


if __name__ == "__main__":
    main()
