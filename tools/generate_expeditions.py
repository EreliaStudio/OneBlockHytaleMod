#!/usr/bin/env python3
import argparse
import json
import re
from pathlib import Path
from typing import Any, Dict, List, Tuple


REPO_ROOT = Path(__file__).resolve().parents[1]

BENCH_PATH = REPO_ROOT / "src/main/resources/Server/Item/Items/OneBlockUpgrader/Bench_OneBlockUpgrader.json"
UNLOCK_BASE = REPO_ROOT / "src/main/resources/Server/Item/Items/UnlockRecipe"
EXPEDITION_ITEM_BASE = REPO_ROOT / "src/main/resources/Server/Item/Items/Expedition"
LANG_PATH = REPO_ROOT / "src/main/resources/Server/Languages/en-US/server.lang"
DEFAULTS_PATH = REPO_ROOT / "src/main/java/com/EreliaStudio/OneBlock/OneBlockExpeditionDefaults.java"


ICON_RECIPE = "Icons/ItemsGenerated/BlockUpgrade.png"
ICON_KEY = "Icons/ItemsGenerated/ExpeditionKey.png"


def load_json(path: Path) -> Any:
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def write_json(path: Path, payload: Any) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(payload, indent=2, ensure_ascii=True)
    path.write_text(text + "\n", encoding="utf-8")


def is_entity_id(raw_id: str) -> Tuple[bool, str]:
    if raw_id is None:
        return False, ""
    trimmed = raw_id.strip()
    if not trimmed:
        return False, ""
    lower = trimmed.lower()
    for prefix in ("entity:", "npc:", "mob:"):
        if lower.startswith(prefix):
            return True, trimmed[len(prefix):].strip()
    return False, trimmed


def unlock_item_id(drop_id: str) -> str:
    if drop_id is None:
        return ""
    trimmed = drop_id.strip()
    if trimmed.startswith("OneBlock_Unlock_"):
        return trimmed
    is_entity, entity_id = is_entity_id(trimmed)
    if is_entity:
        safe_entity = entity_id.replace(":", "_")
        return f"OneBlock_Unlock_Entity_{safe_entity}"
    return f"OneBlock_Unlock_{trimmed}"


def normalize_weight(value: Any) -> int:
    try:
        w = int(value)
    except Exception:
        return 1
    return max(1, w)


def build_recipe_inputs(craft_list: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
    inputs: List[Dict[str, Any]] = []
    for entry in craft_list or []:
        if not isinstance(entry, dict):
            continue
        qty = entry.get("Quantity", 1)
        try:
            qty = int(qty)
        except Exception:
            qty = 1
        if qty < 1:
            qty = 1
        if "ResourceTypeId" in entry:
            rid = entry.get("ResourceTypeId")
            if rid:
                inputs.append({"ResourceTypeId": rid, "Quantity": qty})
            continue
        item_id = entry.get("ID") or entry.get("ItemId")
        if item_id:
            inputs.append({"ItemId": item_id, "Quantity": qty})
    return inputs


def build_unlock_recipe(expedition: str,
                        category_id: str,
                        unlock: Dict[str, Any]) -> Tuple[str, Dict[str, Any], Dict[str, str]]:
    drop_id = unlock.get("ID", "")
    unlock_id = unlock_item_id(drop_id)
    name = unlock.get("Name") or unlock_id
    description = unlock.get("Description") or ""
    weight = normalize_weight(unlock.get("Weight", 1))

    is_entity, entity_id = is_entity_id(drop_id)

    tags: Dict[str, List[str]] = {
        "Type": ["OneBlock_Unlock_Consumable"],
        "OneBlockUnlockExpedition": [expedition],
        "OneBlockUnlockWeight": [str(weight)]
    }
    if is_entity:
        tags["OneBlockUnlockEntityId"] = [entity_id]
    else:
        tags["OneBlockUnlockDropId"] = [drop_id]

    payload = {
        "TranslationProperties": {
            "Name": f"server.items.{unlock_id}.name",
            "Description": f"server.items.{unlock_id}.description"
        },
        "Id": unlock_id,
        "Categories": [
            "Items.Recipes"
        ],
        "PlayerAnimationsId": "Item",
        "Model": "Items/Consumables/Recipes/Recipe.blockymodel",
        "Texture": "Items/Consumables/Recipes/Recipe_Texture.png",
        "IconProperties": {
            "Scale": 0.76,
            "Rotation": [135, 135, 0],
            "Translation": [-1, 5]
        },
        "Interactions": {
            "Primary": {"Interactions": [{"Type": "oneblock_unlock_pool_insert"}]},
            "Secondary": {"Interactions": [{"Type": "oneblock_unlock_pool_insert"}]}
        },
        "Recipe": {
            "Input": build_recipe_inputs(unlock.get("Craft", [])),
            "OutputQuantity": 1,
            "BenchRequirement": [
                {
                    "Type": "Crafting",
                    "Categories": [category_id],
                    "Id": "OneBlockUpgrader"
                }
            ]
        },
        "Icon": ICON_RECIPE,
        "Consumable": True,
        "Tags": tags,
        "ItemLevel": 1,
        "MaxStack": 1
    }

    lang_entries = {
        f"items.{unlock_id}.name": name,
        f"items.{unlock_id}.description": description
    }

    return unlock_id, payload, lang_entries


def build_key_item(expedition: str,
                   category_id: str,
                   key_def: Dict[str, Any]) -> Tuple[str, Dict[str, Any], Dict[str, str]]:
    key_id = key_def.get("ID", f"OneBlock_Expedition_{expedition}_Key")
    name = key_def.get("Name") or key_id
    description = key_def.get("Description") or ""

    payload = {
        "TranslationProperties": {
            "Name": f"server.items.{key_id}.name",
            "Description": f"server.items.{key_id}.description"
        },
        "Id": key_id,
        "ItemLevel": 1,
        "Icon": ICON_KEY,
        "Categories": [
            "Items.OneBlockExpedition"
        ],
        "PlayerAnimationsId": "Item",
        "BlockType": {
            "DrawType": "Model",
            "Material": "Solid",
            "Opacity": "Transparent",
            "CustomModel": "Blocks/Miscellaneous/Portal_Shard.blockymodel",
            "CustomModelTexture": [
                {
                    "Texture": "Blocks/Miscellaneous/Portal_Shard_Texture.png",
                    "Weight": 1
                }
            ]
        },
        "Interactions": {
            "Use": {
                "Interactions": [
                    {"Type": "oneblock_expedition_change"}
                ]
            }
        },
        "Recipe": {
            "Input": build_recipe_inputs(key_def.get("Craft", [])),
            "OutputQuantity": 1,
            "BenchRequirement": [
                {
                    "Type": "Crafting",
                    "Categories": [category_id],
                    "Id": "OneBlockUpgrader"
                }
            ]
        },
        "Consumable": True,
        "Tags": {
            "Type": ["OneBlock_Expedition"],
            "OneBlockExpeditionTargetBlockId": [f"OneBlock_Block_{expedition}"]
        },
        "MaxStack": 1
    }

    lang_entries = {
        f"items.{key_id}.name": name,
        f"items.{key_id}.description": description
    }

    return key_id, payload, lang_entries


def update_lang(path: Path, entries: Dict[str, str]) -> None:
    if not entries:
        return
    if path.exists():
        lines = path.read_text(encoding="utf-8").splitlines()
    else:
        lines = []

    key_to_index: Dict[str, int] = {}
    for idx, line in enumerate(lines):
        if "=" not in line or line.strip().startswith("#"):
            continue
        key = line.split("=", 1)[0].strip()
        if key:
            key_to_index[key] = idx

    for key, value in entries.items():
        line = f"{key} = {value}"
        if key in key_to_index:
            lines[key_to_index[key]] = line
        else:
            lines.append(line)

    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def generate_defaults_java(expeditions: Dict[str, Any]) -> None:
    def render_drop(drop_id: str, weight: int) -> str:
        is_entity, entity_id = is_entity_id(drop_id)
        if is_entity:
            return f"drop(OneBlockDropId.entityDropId(\"{entity_id}\"), {weight})"
        return f"drop(\"{drop_id}\", {weight})"

    lines: List[str] = []
    lines.append("package com.EreliaStudio.OneBlock;")
    lines.append("")
    lines.append("import java.util.ArrayList;")
    lines.append("import java.util.Collections;")
    lines.append("import java.util.HashMap;")
    lines.append("import java.util.List;")
    lines.append("import java.util.Map;")
    lines.append("")
    lines.append("public final class OneBlockExpeditionDefaults")
    lines.append("{")
    lines.append("    public static final class DropDefinition")
    lines.append("    {")
    lines.append("        public final String dropId;")
    lines.append("        public final int weight;")
    lines.append("")
    lines.append("        public DropDefinition(String dropId, int weight)")
    lines.append("        {")
    lines.append("            this.dropId = dropId;")
    lines.append("            this.weight = weight;")
    lines.append("        }")
    lines.append("    }")
    lines.append("")
    lines.append("    private static final Map<String, List<DropDefinition>> DEFAULTS;")
    lines.append("    private static final Map<String, List<String>> DEFAULT_IDS;")
    lines.append("    private static final Map<String, Map<String, Integer>> DEFAULT_WEIGHTS;")
    lines.append("")
    lines.append("    static")
    lines.append("    {")
    lines.append("        Map<String, List<DropDefinition>> defaults = new HashMap<>();")
    lines.append("")

    for expedition, data in expeditions.items():
        pool = data.get("BaseDropPool", [])
        drops: List[str] = []
        for entry in pool or []:
            if not isinstance(entry, dict):
                continue
            drop_id = entry.get("ID")
            if not drop_id:
                continue
            weight = normalize_weight(entry.get("Weight", 1))
            drops.append(render_drop(drop_id, weight))
        if not drops:
            continue
        lines.append(f"        defaults.put(\"{expedition}\", List.of(")
        for i, drop_line in enumerate(drops):
            suffix = "," if i < len(drops) - 1 else ""
            lines.append(f"                {drop_line}{suffix}")
        lines.append("        ));")
        lines.append("")

    lines.append("        DEFAULTS = Collections.unmodifiableMap(defaults);")
    lines.append("        DEFAULT_IDS = buildDefaultIds(DEFAULTS);")
    lines.append("        DEFAULT_WEIGHTS = buildDefaultWeights(DEFAULTS);")
    lines.append("    }")
    lines.append("")
    lines.append("    private OneBlockExpeditionDefaults()")
    lines.append("    {")
    lines.append("    }")
    lines.append("")
    lines.append("    public static List<DropDefinition> getDefaults(String expeditionId)")
    lines.append("    {")
    lines.append("        String key = OneBlockExpeditionResolver.normalizeExpedition(expeditionId);")
    lines.append("        List<DropDefinition> defaults = DEFAULTS.get(key);")
    lines.append("        if (defaults == null)")
    lines.append("        {")
    lines.append("            return List.of();")
    lines.append("        }")
    lines.append("        return defaults;")
    lines.append("    }")
    lines.append("")
    lines.append("    public static List<String> getDefaultDropIds(String expeditionId)")
    lines.append("    {")
    lines.append("        String key = OneBlockExpeditionResolver.normalizeExpedition(expeditionId);")
    lines.append("        List<String> ids = DEFAULT_IDS.get(key);")
    lines.append("        if (ids == null || ids.isEmpty())")
    lines.append("        {")
    lines.append("            return List.of(OneBlockDropRegistry.DEFAULT_ITEM_ID);")
    lines.append("        }")
    lines.append("        return ids;")
    lines.append("    }")
    lines.append("")
    lines.append("    public static Map<String, Map<String, Integer>> getDefaultWeights()")
    lines.append("    {")
    lines.append("        return DEFAULT_WEIGHTS;")
    lines.append("    }")
    lines.append("")
    lines.append("    public static boolean isDefaultDrop(String expeditionId, String dropId)")
    lines.append("    {")
    lines.append("        if (dropId == null || dropId.isEmpty())")
    lines.append("        {")
    lines.append("            return false;")
    lines.append("        }")
    lines.append("")
    lines.append("        List<String> ids = DEFAULT_IDS.get(OneBlockExpeditionResolver.normalizeExpedition(expeditionId));")
    lines.append("        return ids != null && ids.contains(dropId);")
    lines.append("    }")
    lines.append("")
    lines.append("    public static void ensureDefaults(String expeditionId, OneBlockPlayerExpeditionDropsState state)")
    lines.append("    {")
    lines.append("        if (state == null)")
    lines.append("        {")
    lines.append("            return;")
    lines.append("        }")
    lines.append("")
    lines.append("        state.unlockedDrops.removeIf(item -> item == null || item.isEmpty());")
    lines.append("")
    lines.append("        List<String> defaults = getDefaultDropIds(expeditionId);")
    lines.append("        if (!defaults.isEmpty())")
    lines.append("        {")
    lines.append("            state.unlockedDrops.addAll(defaults);")
    lines.append("        }")
    lines.append("")
    lines.append("        if (state.unlockedDrops.isEmpty())")
    lines.append("        {")
    lines.append("            state.unlockedDrops.add(OneBlockDropRegistry.DEFAULT_ITEM_ID);")
    lines.append("        }")
    lines.append("    }")
    lines.append("")
    lines.append("    private static DropDefinition drop(String dropId, int weight)")
    lines.append("    {")
    lines.append("        if (weight < 1)")
    lines.append("        {")
    lines.append("            weight = 1;")
    lines.append("        }")
    lines.append("        return new DropDefinition(dropId, weight);")
    lines.append("    }")
    lines.append("")
    lines.append("    private static Map<String, List<String>> buildDefaultIds(Map<String, List<DropDefinition>> defaults)")
    lines.append("    {")
    lines.append("        Map<String, List<String>> out = new HashMap<>();")
    lines.append("        for (Map.Entry<String, List<DropDefinition>> entry : defaults.entrySet())")
    lines.append("        {")
    lines.append("            List<String> ids = new ArrayList<>();")
    lines.append("            for (DropDefinition def : entry.getValue())")
    lines.append("            {")
    lines.append("                if (def != null && def.dropId != null && !def.dropId.isEmpty())")
    lines.append("                {")
    lines.append("                    ids.add(def.dropId);")
    lines.append("                }")
    lines.append("            }")
    lines.append("            out.put(entry.getKey(), Collections.unmodifiableList(ids));")
    lines.append("        }")
    lines.append("        return Collections.unmodifiableMap(out);")
    lines.append("    }")
    lines.append("")
    lines.append("    private static Map<String, Map<String, Integer>> buildDefaultWeights(Map<String, List<DropDefinition>> defaults)")
    lines.append("    {")
    lines.append("        Map<String, Map<String, Integer>> out = new HashMap<>();")
    lines.append("        for (Map.Entry<String, List<DropDefinition>> entry : defaults.entrySet())")
    lines.append("        {")
    lines.append("            Map<String, Integer> weights = new HashMap<>();")
    lines.append("            for (DropDefinition def : entry.getValue())")
    lines.append("            {")
    lines.append("                if (def == null || def.dropId == null || def.dropId.isEmpty())")
    lines.append("                {")
    lines.append("                    continue;")
    lines.append("                }")
    lines.append("                int weight = def.weight < 1 ? 1 : def.weight;")
    lines.append("                weights.put(def.dropId, weight);")
    lines.append("            }")
    lines.append("            out.put(entry.getKey(), Collections.unmodifiableMap(weights));")
    lines.append("        }")
    lines.append("        return Collections.unmodifiableMap(out);")
    lines.append("    }")
    lines.append("}")

    DEFAULTS_PATH.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser(description="Generate OneBlock expedition assets and bench categories.")
    parser.add_argument("config", type=Path, help="Path to the expedition JSON config.")
    args = parser.parse_args()

    config = load_json(args.config)
    if not isinstance(config, dict):
        raise SystemExit("Config root must be a JSON object keyed by expedition name.")

    bench = load_json(BENCH_PATH)
    bench_categories: List[Dict[str, Any]] = []

    lang_updates: Dict[str, str] = {}

    for expedition, data in config.items():
        if not isinstance(data, dict):
            continue

        category_id = f"OneBlock_Upgrader_Expedition_{expedition}"
        category_name_key = f"server.benchCategories.OneBlockUpgrader_Expedition_{expedition}"

        recipes: List[str] = []

        key_def = data.get("KeyCraft")
        if isinstance(key_def, dict):
            key_id, key_payload, key_lang = build_key_item(expedition, category_id, key_def)
            write_json(EXPEDITION_ITEM_BASE / f"{key_id}.json", key_payload)
            lang_updates.update(key_lang)
            recipes.append(key_id)

        unlocks = data.get("Unlockable", [])
        if isinstance(unlocks, list):
            for unlock in unlocks:
                if not isinstance(unlock, dict):
                    continue
                unlock_id, payload, unlock_lang = build_unlock_recipe(expedition, category_id, unlock)
                write_json(UNLOCK_BASE / f"Expedition_{expedition}" / f"{unlock_id}.json", payload)
                lang_updates.update(unlock_lang)
                recipes.append(unlock_id)

        bench_categories.append({
            "Id": category_id,
            "Icon": "Icons/CraftingCategories/ExpeditionKey.png",
            "Name": category_name_key,
            "Recipes": recipes
        })

        lang_updates[f"benchCategories.OneBlockUpgrader_Expedition_{expedition}"] = f"Expedition - {expedition}"

    # update bench categories
    bench_block = bench.get("BlockType", {}).get("Bench", {})
    if "BlockType" not in bench:
        bench["BlockType"] = {}
    if "Bench" not in bench["BlockType"]:
        bench["BlockType"]["Bench"] = {}
    bench["BlockType"]["Bench"]["Categories"] = bench_categories
    write_json(BENCH_PATH, bench)

    update_lang(LANG_PATH, lang_updates)
    generate_defaults_java(config)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
