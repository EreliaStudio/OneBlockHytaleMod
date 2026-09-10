"""Canonical, deterministic expedition calculations shared by all generators.

Duplicate IDs sum weights; conflicting quantities are rejected, never guessed.
Probabilities are percentages. A random reward bundle is one mutually exclusive roll.
"""
from collections import OrderedDict
from copy import deepcopy
import re


def quantity(entry):
    value = entry.get("Quantity", 1)
    if isinstance(value, bool) or not isinstance(value, int) or value < 1:
        raise ValueError("Quantity must be a positive integer")
    return value


def entry_id(entry):
    return entry.get("ID", entry.get("CustomID"))


def weight(entry):
    value = entry.get("Weight", 1)
    if isinstance(value, bool) or not isinstance(value, int) or not 0 < value <= 2147483647:
        raise ValueError("Weight must be a positive 32-bit integer")
    return value


def combined_drops(cfg):
    combined = OrderedDict()
    for entry in cfg.get("BaseDropPool", []) or []:
        identifier = entry_id(entry)
        if not isinstance(identifier, str) or not identifier:
            raise ValueError("Drop requires an ID or CustomID")
        count, value = quantity(entry), weight(entry)
        if identifier in combined:
            previous = combined[identifier]
            if quantity(previous) != count:
                raise ValueError(f"Conflicting quantities for duplicate drop {identifier}")
            previous["Weight"] += value
        else:
            combined[identifier] = {**entry, "Quantity": count, "Weight": value}
    if sum(e["Weight"] for e in combined.values()) > 2147483647:
        raise ValueError("Drop pool exceeds native integer weight limit")
    return list(combined.values())


def normalized_drops(cfg):
    entries = combined_drops(cfg)
    total = sum(weight(e) for e in entries)
    return [(entry_id(e), quantity(e), weight(e) * 100.0 / total) for e in entries]


def parse_rewards(cfg):
    rewards = cfg.get("CompletionRewards", cfg.get("Rewards", [])) or []
    if isinstance(rewards, list):
        return rewards, []
    if not isinstance(rewards, dict):
        raise ValueError("CompletionRewards must be an object or array")
    return list(rewards.get("Mandatory", []) or []), list(rewards.get("Random", []) or [])


def reward_edges(cfg):
    mandatory, bundles = parse_rewards(cfg)
    edges = [(target, 100.0, "Guaranteed") for target in
             dict.fromkeys(e["Crystal"] for e in mandatory if e.get("Crystal"))]
    total = sum(weight(b) for b in bundles)
    for bundle in bundles:
        # Multiple copies of the same crystal in one bundle are one discovery event.
        for target in dict.fromkeys(e["Crystal"] for e in bundle.get("Items", []) if e.get("Crystal")):
            edges.append((target, weight(bundle) * 100.0 / total, "Random unlock pool"))
    return edges


def combine_edges(edges):
    combined = OrderedDict()
    for target, chance, kind in edges:
        combined[(target, kind)] = combined.get((target, kind), 0.0) + chance
    return [(target, min(100.0, chance), kind) for (target, kind), chance in combined.items()]


def expected_yield(chance, count, duration):
    return chance / 100.0 * count * duration


def validate_expeditions(raw):
    expeditions = OrderedDict((k, deepcopy(v)) for k, v in raw.items() if not k.startswith("_"))
    for identifier, cfg in expeditions.items():
        if not re.fullmatch(r"[A-Za-z0-9_]+", identifier):
            raise ValueError(f"Unsafe expedition ID: {identifier}")
        cfg["BaseDropPool"] = combined_drops(cfg)
        if cfg.get("Category", cfg.get("Group")) != "Dungeon" and (not cfg["BaseDropPool"] or not isinstance(cfg.get("Ticks"), int) or cfg["Ticks"] < 1):
            raise ValueError(f"{identifier}: normal expeditions require drops and positive Ticks")
        for cost in cfg.get("Crystal", {}).get("Input", []):
            quantity(cost)
            if not cost.get("ItemId"):
                raise ValueError(f"{identifier}: crystal input requires ItemId")
        mandatory, bundles = parse_rewards(cfg)
        if sum(weight(b) for b in bundles) > 2147483647:
            raise ValueError("Reward pool exceeds native integer weight limit")
        for reward in mandatory + [e for b in bundles for e in b.get("Items", [])]:
            quantity(reward)
            if reward.get("Crystal") and reward["Crystal"] not in expeditions:
                raise ValueError(f"Unknown unlock destination: {reward['Crystal']}")
            if not reward.get("Crystal") and not entry_id(reward):
                raise ValueError("Reward requires an item, mob, or crystal ID")
    return expeditions


def build_catalog(raw, mob_assets=None):
    expeditions = validate_expeditions(raw)
    result = []
    for identifier, cfg in expeditions.items():
        dungeon = cfg.get("Category", cfg.get("Group")) == "Dungeon"
        drops = [dict(id=i, quantity=q, chance=p, expected=expected_yield(p, q, cfg.get("Ticks", 0)), kind="block")
                 for i, q, p in normalized_drops(cfg)] if not dungeon else []
        mandatory, bundles = parse_rewards(cfg)
        rewards = []
        total = sum(weight(b) for b in bundles)
        for entries, chance in [(mandatory, 100.0)] + [(b.get("Items", []), weight(b)*100.0/total) for b in bundles]:
            for e in entries:
                rid = "OneBlock_Crystal_" + e["Crystal"] if e.get("Crystal") else entry_id(e)
                rewards.append(dict(id=rid, quantity=quantity(e), chance=chance,
                                    expected=chance/100.0*quantity(e), kind="reward"))
        edges = OrderedDict()
        for target, chance, kind in combine_edges(reward_edges(cfg)):
            edges[target] = min(100.0, edges.get(target, 0.0) + chance)
        result.append(dict(id=identifier, name=cfg.get("DisplayName", identifier),
                           crystal="OneBlock_Crystal_"+identifier, category=cfg.get("Category", cfg.get("Group", "Easy")),
                           tier=cfg.get("ItemLevel", 1), duration=cfg.get("Ticks", 0),
                           tool=cfg.get("Solidity", {}).get("Tool", "Hand"), dungeon=dungeon,
                           drops=drops, rewards=rewards, waves=cfg.get("Waves", []),
                           children=[dict(target=t, chance=p) for t, p in edges.items()]))
    used_mobs = {d["id"][7:] for e in result for d in e["drops"] + e["rewards"] if d["id"].startswith("entity:")}
    used_mobs.update(m.removeprefix("entity:") for e in result for w in e["waves"] for m in w)
    return dict(version=1, expeditions=result,
                mobs={m: mob_assets[m] for m in sorted(used_mobs) if mob_assets and m in mob_assets})
