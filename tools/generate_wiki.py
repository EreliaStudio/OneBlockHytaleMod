#!/usr/bin/env python3
"""Generate the player-facing OneBlock wiki from expeditions.json."""

from __future__ import annotations

import argparse
import json
import re
import sys
from collections import OrderedDict, defaultdict
from pathlib import Path
from typing import Any, Iterable


CATEGORY_ORDER = ("Easy", "Advanced", "Difficult", "Hard", "Expert", "Dungeon")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate wiki/Home.md, wiki/LootList.md, and one page per expedition."
    )
    parser.add_argument(
        "input",
        nargs="?",
        type=Path,
        default=Path("expeditions.json"),
        help="expedition JSON file (default: expeditions.json)",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=Path("wiki"),
        help="wiki output directory (default: wiki)",
    )
    parser.add_argument(
        "--render-names",
        type=Path,
        default=Path("item_render_names.json"),
        help="optional ID-to-display-name JSON map",
    )
    parser.add_argument(
        "--clean",
        action="store_true",
        help="remove stale Markdown files from the expedition output directory",
    )
    parser.add_argument(
        "--check",
        action="store_true",
        help="do not write; exit with status 1 if generated content differs",
    )
    return parser.parse_args()


def load_json(path: Path) -> Any:
    try:
        return json.loads(path.read_text(encoding="utf-8-sig"))
    except FileNotFoundError as error:
        raise ValueError(f"file not found: {path}") from error
    except json.JSONDecodeError as error:
        raise ValueError(f"invalid JSON in {path}: {error}") from error


def safe_filename(expedition_id: str) -> str:
    name = re.sub(r"[^A-Za-z0-9_-]+", "_", expedition_id.strip()).strip("._")
    if not name:
        raise ValueError(f"expedition ID has no safe filename: {expedition_id!r}")
    return f"{name}.md"


def humanize(value: str) -> str:
    value = value.replace("_", " ")
    value = re.sub(r"(?<=[a-z0-9])(?=[A-Z])", " ", value)
    value = re.sub(r"(?<=[A-Z])(?=[A-Z][a-z])", " ", value)
    return re.sub(r"\s+", " ", value).strip()


def display_name(expedition_id: str, cfg: dict[str, Any]) -> str:
    if cfg.get("DisplayName"):
        return str(cfg["DisplayName"])
    if expedition_id == "Default":
        return "Meadow"
    return humanize(expedition_id)


def percentage(value: float) -> str:
    if abs(value - round(value)) < 0.0000001:
        return f"{round(value)}%"
    return f"{value:.2f}%"


def quantity(entry: dict[str, Any]) -> int:
    return int(entry.get("Quantity", 1))


def entry_id(entry: dict[str, Any]) -> str | None:
    value = entry.get("ID", entry.get("CustomID"))
    return str(value) if value is not None else None


def parse_rewards(cfg: dict[str, Any]) -> tuple[list[dict[str, Any]], list[dict[str, Any]]]:
    rewards = cfg.get("CompletionRewards", cfg.get("Rewards", [])) or []
    if isinstance(rewards, list):
        return rewards, []
    if not isinstance(rewards, dict):
        raise ValueError("CompletionRewards must be an object or array")
    return list(rewards.get("Mandatory", []) or []), list(rewards.get("Random", []) or [])


def reward_edges(cfg: dict[str, Any]) -> list[tuple[str, float, str]]:
    mandatory, random_bundles = parse_rewards(cfg)
    edges: list[tuple[str, float, str]] = []
    for reward in mandatory:
        if reward.get("Crystal"):
            edges.append((str(reward["Crystal"]), 100.0, "Guaranteed"))

    total_weight = sum(float(bundle.get("Weight", 1)) for bundle in random_bundles)
    if total_weight > 0:
        for bundle in random_bundles:
            chance = float(bundle.get("Weight", 1)) * 100.0 / total_weight
            for reward in bundle.get("Items", []) or []:
                if reward.get("Crystal"):
                    edges.append((str(reward["Crystal"]), chance, "Random unlock pool"))
    return edges


def combine_edges(edges: Iterable[tuple[str, float, str]]) -> list[tuple[str, float, str]]:
    combined: OrderedDict[tuple[str, str], float] = OrderedDict()
    for target, chance, kind in edges:
        key = (target, kind)
        combined[key] = combined.get(key, 0.0) + chance
    return [(target, chance, kind) for (target, kind), chance in combined.items()]


def normalized_drops(cfg: dict[str, Any]) -> list[tuple[str, int, float]]:
    combined: OrderedDict[str, tuple[int, float]] = OrderedDict()
    for entry in cfg.get("BaseDropPool", []) or []:
        drop_id = entry_id(entry)
        if not drop_id:
            continue
        count, weight = combined.get(drop_id, (quantity(entry), 0.0))
        combined[drop_id] = (count, weight + float(entry.get("Weight", 1)))
    total = sum(weight for _, weight in combined.values())
    if total <= 0 and combined:
        raise ValueError("BaseDropPool weights must have a positive sum")
    return [
        (drop_id, count, weight * 100.0 / total)
        for drop_id, (count, weight) in combined.items()
    ]


def cost_text(cfg: dict[str, Any]) -> str:
    inputs = cfg.get("Crystal", {}).get("Input", []) or []
    if not inputs:
        return "No crafting cost"
    return " + ".join(f"{quantity(item)} × `{item['ItemId']}`" for item in inputs)


def render_drop(drop_id: str, count: int, render_names: dict[str, str]) -> str:
    prefix = f"{count} × " if count != 1 else ""
    if drop_id.startswith("entity:"):
        entity_id = drop_id.removeprefix("entity:")
        name = render_names.get(entity_id, humanize(entity_id))
        return f"{prefix}{name} *(mob)* (`{drop_id}`)"
    return f"{prefix}`{drop_id}`"


def render_reward(entry: dict[str, Any], render_names: dict[str, str]) -> str:
    reward_id = entry_id(entry)
    if not reward_id:
        return "unknown reward"
    name = render_names.get(reward_id, humanize(reward_id))
    return f"{quantity(entry)} × {name}"


def render_waves(waves: list[list[str]], render_names: dict[str, str]) -> list[str]:
    lines = ["| Wave | Enemies |", "|---:|---|"]
    for index, wave in enumerate(waves, 1):
        counts: OrderedDict[str, int] = OrderedDict()
        for entity_id in wave:
            counts[entity_id] = counts.get(entity_id, 0) + 1
        enemies = ", ".join(
            f"{render_names.get(entity_id, humanize(entity_id))} ×{count}"
            for entity_id, count in counts.items()
        )
        lines.append(f"| {index} | {enemies or 'None'} |")
    return lines


def link(
    expedition_id: str,
    expeditions: OrderedDict[str, dict[str, Any]],
    prefix: str = "",
) -> str:
    cfg = expeditions.get(expedition_id, {})
    return f"[{display_name(expedition_id, cfg)}]({prefix}{safe_filename(expedition_id)})"


def generate_expedition_page(
    expedition_id: str,
    cfg: dict[str, Any],
    expeditions: OrderedDict[str, dict[str, Any]],
    incoming: dict[str, list[tuple[str, float, str]]],
    render_names: dict[str, str],
) -> str:
    name = display_name(expedition_id, cfg)
    next_edges = combine_edges(reward_edges(cfg))
    from_edges = incoming.get(expedition_id, [])
    category = str(cfg.get("Category", cfg.get("Group", "Uncategorized")))
    tier = int(cfg.get("ItemLevel", 1))
    tool = str((cfg.get("Solidity") or {}).get("Tool", "Hand"))
    cost = cost_text(cfg)
    is_dungeon = category == "Dungeon"

    lines = [f"# {name}", "", f"`{expedition_id}`", ""]
    if from_edges:
        lines.append("**From:** " + " · ".join(link(source, expeditions) for source, _, _ in from_edges) + ("  " if next_edges else ""))
    if next_edges:
        lines.append("**Next:** " + " · ".join(link(target, expeditions) for target, _, _ in next_edges))
    if from_edges or next_edges:
        lines.append("")
    lines.extend(["---", ""])

    facts = [f"**Difficulty:** {category} (Tier {tier})", f"**Crystal cost:** {cost}"]
    if not is_dungeon:
        facts.append(f"**Duration:** {int(cfg.get('Ticks', 0))} ticks")
    facts.append(f"**Tool:** {tool}")
    lines.extend([" · ".join(facts), "", "### How to access", ""])

    if expedition_id == "Default":
        lines.append(f"- **Starting expedition.** Its crystal input costs {cost}.")
    else:
        if from_edges:
            for source, chance, kind in from_edges:
                if kind == "Guaranteed":
                    detail = "guaranteed completion crystal"
                else:
                    detail = "chance within that expedition's random unlock pool"
                lines.append(f"- {link(source, expeditions)} — **{percentage(chance)}** {detail}.")
        else:
            lines.append("- No expedition in this data set awards its crystal; it must be obtained separately.")
        lines.append(f"- Using this expedition requires its crystal recipe input: **{cost.replace('`', '`')}**.")

    lines.append("")
    if is_dungeon:
        lines.extend([
            "### Dungeon encounter",
            "",
            "This expedition has no normal weighted `BaseDropPool`. It is a combat dungeon.",
            "",
            *render_waves(cfg.get("Waves", []) or [], render_names),
        ])
    else:
        lines.extend(["### Loot pool", "", "| Drop | Chance per pool roll |", "|---|---:|"])
        drops = normalized_drops(cfg)
        if drops:
            for drop_id, count, chance in drops:
                lines.append(f"| {render_drop(drop_id, count, render_names)} | **{percentage(chance)}** |")
        else:
            lines.append("| _No drops configured_ | — |")

    mandatory, _ = parse_rewards(cfg)
    lines.extend(["", "### Completion rewards", ""])
    for reward in mandatory:
        if reward.get("Crystal"):
            target = str(reward["Crystal"])
            lines.append(f"- **100%:** crystal for {link(target, expeditions)} is guaranteed.")
        else:
            lines.append(f"- **{render_reward(reward, render_names)}** guaranteed.")
    for target, chance, kind in next_edges:
        if kind == "Random unlock pool":
            lines.append(f"- **{percentage(chance)}:** crystal for {link(target, expeditions)} from the random unlock pool.")
    if not mandatory and not next_edges:
        lines.append("- No completion rewards are configured.")
    elif not next_edges:
        lines.append("- No further expedition crystal is awarded.")

    lines.extend(["", "### Where it goes", ""])
    if next_edges:
        lines.extend(["| Next expedition | Chance | Unlock type |", "|---|---:|---|"])
        for target, chance, kind in next_edges:
            lines.append(f"| {link(target, expeditions)} (`{target}`) | **{percentage(chance)}** | {kind} |")
    else:
        lines.append("_Progression endpoint: this expedition does not award another expedition crystal._")
    lines.extend(["", "[Back to expedition index](../Home.md)", ""])
    return "\n".join(lines)


def generate_home(expeditions: OrderedDict[str, dict[str, Any]]) -> str:
    categories = list(CATEGORY_ORDER)
    categories.extend(
        category for category in OrderedDict.fromkeys(
            str(cfg.get("Category", cfg.get("Group", "Uncategorized"))) for cfg in expeditions.values()
        ) if category not in categories
    )
    lines = [
        "# OneBlock Expedition Wiki", "",
        "Player-facing reference for the OneBlock expedition system.", "",
        "Each expedition has its own page with:", "",
        "- how to access it;", "- crystal cost and difficulty;",
        "- normalized loot percentages;", "- dungeon waves when applicable;",
        "- completion rewards;", "- links to the expeditions it can unlock next.", "",
        "## Expedition index", "",
    ]
    for category in categories:
        matches = [(eid, cfg) for eid, cfg in expeditions.items()
                   if str(cfg.get("Category", cfg.get("Group", "Uncategorized"))) == category]
        if not matches:
            continue
        lines.extend([f"### {category}", ""])
        for expedition_id, cfg in matches:
            page_link = link(expedition_id, expeditions, "expeditions/")
            if expedition_id == "Default":
                page_link = page_link.replace("](", " (`Default`)](")
            lines.append(f"- {page_link} — Tier {int(cfg.get('ItemLevel', 1))}")
        lines.append("")
    lines.extend([
        "## Percentage rules", "",
        "- Loot percentages are normalized from `BaseDropPool` weights.",
        "- Duplicate IDs are combined before normalization.",
        "- Random next-expedition rewards are normalized inside that expedition's random completion-reward pool.",
        "- Mandatory expedition crystal rewards are shown as **100%**.", "",
        "## Item loot list", "", "- [Loot list](LootList.md)", "",
    ])
    return "\n".join(lines)


def generate_loot_index(expeditions: OrderedDict[str, dict[str, Any]]) -> str:
    loot: dict[str, list[tuple[str, float]]] = defaultdict(list)
    for expedition_id, cfg in expeditions.items():
        if str(cfg.get("Category", cfg.get("Group", ""))) == "Dungeon":
            continue
        for drop_id, _, chance in normalized_drops(cfg):
            if not drop_id.startswith("entity:"):
                loot[drop_id].append((expedition_id, chance))

    sorted_ids = sorted(loot, key=str.casefold)
    def initial(item: str) -> str:
        match = re.search(r"[A-Za-z0-9]", item)
        return match.group(0).upper() if match else "#"

    initials = list(OrderedDict.fromkeys(initial(item) for item in sorted_ids))
    lines = [
        "# Loot Index", "",
        "This page lists every **item/resource** that can appear in an expedition loot pool, followed by every expedition where it can be obtained.", "",
        "Mobs (`entity:*` entries) are intentionally excluded from this page.", "",
        "The percentage shown next to each expedition is the normalized chance for that item in that expedition's full `BaseDropPool`.", "",
        "## Contents", "",
        *(f"- [{letter}](#{letter.lower()})" for letter in initials), "", "---", "",
    ]
    current_initial = None
    for drop_id in sorted_ids:
        item_initial = initial(drop_id)
        if item_initial != current_initial:
            lines.extend([f'<a id="{item_initial.lower()}"></a>', f"## {item_initial}", ""])
            current_initial = item_initial
        lines.extend([f"### `{drop_id}`", ""])
        sources = sorted(
            loot[drop_id],
            key=lambda source: source[0].casefold(),
        )
        for expedition_id, chance in sources:
            lines.append(f"- {link(expedition_id, expeditions, 'expeditions/')} — **{percentage(chance)}**")
        lines.append("")
    return "\n".join(lines)


def generate_readme() -> str:
    return """# Wiki structure

- `Home.md`: expedition index
- `LootList.md`: reverse index of item loot and its sources
- `expeditions/<ExpeditionId>.md`: one page per expedition

All expedition-to-expedition links are relative Markdown links, so the documentation works directly in a normal GitHub repository.

The pages are generated from `expeditions.json`. From the repository root, run:

```bash
python tools/generate_wiki.py expeditions.json --output wiki --clean
```

Use `--check` in automation to verify that the committed wiki is up to date without modifying it.
"""


def write_or_check(path: Path, content: str, check: bool) -> bool:
    expected = content.replace("\r\n", "\n")
    current = path.read_text(encoding="utf-8").replace("\r\n", "\n") if path.exists() else None
    changed = current != expected
    if changed and not check:
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content, encoding="utf-8", newline="\n")
    return changed


def main() -> int:
    args = parse_args()
    try:
        raw = load_json(args.input)
        if not isinstance(raw, dict):
            raise ValueError("the top-level expedition JSON value must be an object")
        expeditions: OrderedDict[str, dict[str, Any]] = OrderedDict(
            (str(key), value) for key, value in raw.items() if not str(key).startswith("_")
        )
        if not expeditions:
            raise ValueError("no expeditions found")
        for expedition_id, cfg in expeditions.items():
            if not isinstance(cfg, dict):
                raise ValueError(f"{expedition_id}: expedition definition must be an object")

        render_names: dict[str, str] = {}
        if args.render_names.exists():
            loaded_names = load_json(args.render_names)
            if isinstance(loaded_names, dict):
                render_names = {str(key): str(value) for key, value in loaded_names.items() if not str(key).startswith("_")}

        incoming: dict[str, list[tuple[str, float, str]]] = defaultdict(list)
        for source, cfg in expeditions.items():
            for target, chance, kind in combine_edges(reward_edges(cfg)):
                incoming[target].append((source, chance, kind))

        output = args.output
        pages: dict[Path, str] = {
            output / "README.md": generate_readme(),
            output / "Home.md": generate_home(expeditions),
            output / "LootList.md": generate_loot_index(expeditions),
        }
        for expedition_id, cfg in expeditions.items():
            pages[output / "expeditions" / safe_filename(expedition_id)] = generate_expedition_page(
                expedition_id, cfg, expeditions, incoming, render_names
            )

        changed = [path for path, content in pages.items() if write_or_check(path, content, args.check)]
        stale: list[Path] = []
        expedition_dir = output / "expeditions"
        if args.clean and expedition_dir.exists():
            expected = {path.resolve() for path in pages if path.parent == expedition_dir}
            stale = [path for path in expedition_dir.glob("*.md") if path.resolve() not in expected]
            if not args.check:
                for path in stale:
                    path.unlink()

        action = "would change" if args.check else "generated"
        print(f"{action}: {len(changed)} file(s); unchanged: {len(pages) - len(changed)}; stale: {len(stale)}")
        if args.check and (changed or stale):
            for path in [*changed, *stale]:
                print(path)
            return 1
        return 0
    except (OSError, TypeError, ValueError) as error:
        print(f"error: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
