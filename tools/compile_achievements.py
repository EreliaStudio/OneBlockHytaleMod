#!/usr/bin/env python3
"""Validate the human achievement file and compile it into the mod's runtime schema."""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path
from typing import Any

ID = re.compile(r"^[a-z0-9][a-z0-9_-]*$")


def fail(message: str) -> None:
    raise ValueError(message)


def string_list(value: Any, field: str, achievement_id: str) -> list[str]:
    if value is None:
        return []
    if not isinstance(value, list) or any(not isinstance(item, str) or not item.strip() for item in value):
        fail(f"{achievement_id}.{field} must be an array of non-empty strings")
    if len(value) != len(set(value)):
        fail(f"{achievement_id}.{field} contains duplicates")
    return value


def compile_file(source: Path) -> dict[str, Any]:
    raw = json.loads(source.read_text(encoding="utf-8"))
    rows = raw.get("achievements") if isinstance(raw, dict) else None
    if not isinstance(rows, list):
        fail("The root must contain an achievements array")

    compiled: list[dict[str, Any]] = []
    ids: set[str] = set()
    for index, row in enumerate(rows):
        if not isinstance(row, dict):
            fail(f"achievements[{index}] must be an object")
        achievement_id = row.get("id")
        if not isinstance(achievement_id, str) or not ID.fullmatch(achievement_id):
            fail(f"achievements[{index}].id must be lowercase and use only a-z, 0-9, _ or -")
        if achievement_id in ids:
            fail(f"Duplicate achievement id: {achievement_id}")
        ids.add(achievement_id)
        name = row.get("name")
        title = row.get("title")
        if not isinstance(name, str) or not name.strip():
            fail(f"{achievement_id}.name must be a non-empty string")
        if not isinstance(title, str) or not title.strip():
            fail(f"{achievement_id}.title must be a non-empty string")
        if len(title) > 64 or "\n" in title or "\r" in title:
            fail(f"{achievement_id}.title must be one line and at most 64 characters")
        money = row.get("money", 0)
        if not isinstance(money, int) or isinstance(money, bool) or money < 0:
            fail(f"{achievement_id}.money must be a non-negative integer")
        items = row.get("items", {})
        if not isinstance(items, dict):
            fail(f"{achievement_id}.items must be an object mapping Hytale item IDs to quantities")
        item_costs = []
        for item_id, quantity in items.items():
            if not isinstance(item_id, str) or not item_id.strip() or not isinstance(quantity, int) \
                    or isinstance(quantity, bool) or quantity <= 0:
                fail(f"{achievement_id}.items has an invalid item ID or quantity")
            item_costs.append({"id": item_id, "quantity": quantity})
        compiled.append({
            "id": achievement_id,
            "name": name,
            "title": title,
            "prerequisites": string_list(row.get("requires"), "requires", achievement_id),
            "cost": {
                "currency": money,
                "items": item_costs,
                "expeditions": string_list(row.get("expeditions"), "expeditions", achievement_id),
            },
        })

    lookup = {row["id"]: row for row in compiled}
    for row in compiled:
        for requirement in row["prerequisites"]:
            if requirement not in lookup:
                fail(f"{row['id']} requires unknown achievement {requirement}")

    visiting: set[str] = set()
    done: set[str] = set()

    def visit(achievement_id: str) -> None:
        if achievement_id in done:
            return
        if achievement_id in visiting:
            fail(f"Achievement prerequisite cycle contains {achievement_id}")
        visiting.add(achievement_id)
        for requirement in lookup[achievement_id]["prerequisites"]:
            visit(requirement)
        visiting.remove(achievement_id)
        done.add(achievement_id)

    for achievement_id in lookup:
        visit(achievement_id)
    return {"schemaVersion": 1, "achievements": compiled}


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", type=Path, help="Human-readable achievement JSON")
    parser.add_argument("output", type=Path, help="Runtime achievements.json to create")
    args = parser.parse_args()
    try:
        result = compile_file(args.input)
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(json.dumps(result, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    except (OSError, json.JSONDecodeError, ValueError) as error:
        parser.error(str(error))
    print(f"Compiled {len(result['achievements'])} achievements to {args.output}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
