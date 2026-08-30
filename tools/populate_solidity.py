#!/usr/bin/env python3
"""Populate explicit baseline Solidity values in an expedition data file."""

import argparse
import json
from pathlib import Path


TICKS_BY_ITEM_LEVEL = {
    1: 3,
    2: 4,
    3: 5,
    4: 6,
    5: 8,
}

PICKAXE_KEYWORDS = (
    "Cave",
    "Cavern",
    "Quarry",
    "Temple",
    "Crypt",
    "Catacomb",
    "Necropolis",
    "Citadel",
    "Ruins",
    "Volcano",
    "Portal",
    "Gate",
    "Core",
    "Nest",
    "PharaohRoom",
    "Graveyard",
    "Crystal",
    "Sanctum",
)

AXE_KEYWORDS = (
    "Forest",
    "Grove",
    "Jungle",
    "Shipwreck",
    "TrorkCamp",
    "TrorkHuntingGround",
    "TrorkWarband",
    "TrorkStronghold",
    "TrorkChieftainCamp",
)


def required_tool(expedition_id: str) -> str:
    if any(keyword in expedition_id for keyword in PICKAXE_KEYWORDS):
        return "Pickaxe"
    if any(keyword in expedition_id for keyword in AXE_KEYWORDS):
        return "Axe"
    return "Hand"


def solidity_for(expedition_id: str, config: dict) -> dict:
    item_level = int(config.get("ItemLevel", 1))
    ticks = TICKS_BY_ITEM_LEVEL.get(item_level, max(3, item_level + 2))
    return {
        "Ticks": ticks,
        "Tool": required_tool(expedition_id),
    }


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Add explicit baseline Solidity to OneBlock expeditions."
    )
    parser.add_argument("input", type=Path, help="Expedition JSON to update")
    parser.add_argument(
        "--overwrite",
        action="store_true",
        help="Replace existing Solidity values instead of preserving them",
    )
    args = parser.parse_args()

    raw = args.input.read_text(encoding="utf-8-sig")
    data = json.loads(raw)
    updated = 0

    for expedition_id, config in data.items():
        if expedition_id.startswith("_"):
            continue
        if "Solidity" in config and not args.overwrite:
            continue
        config["Solidity"] = solidity_for(expedition_id, config)
        updated += 1

    if updated:
        args.input.write_text(
            "\ufeff" + json.dumps(data, ensure_ascii=False, indent="\t") + "\n",
            encoding="utf-8",
        )
    print(f"Updated Solidity for {updated} expedition(s) in {args.input}.")


if __name__ == "__main__":
    main()
