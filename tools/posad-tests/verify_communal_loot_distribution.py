#!/usr/bin/env python3
"""10,000-seed offline loot configuration simulation for the approved 2.2.3 house.

This checks JSON rolls, item weights, count ranges and the 20% elder bonus.
It does NOT claim to execute Minecraft loot tables or chest save/reload.
"""
import json
import random
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
LOOT = ROOT / "src/main/resources/data/slavicmyths/loot_table/chests"

EXPECTED = {
    "pantry": (2, 4, [
        ("minecraft:bread", 8, 1, 3),
        ("slavicmyths:rye_bread", 6, 1, 2),
        ("slavicmyths:dried_mushrooms", 7, 1, 3),
        ("minecraft:cooked_porkchop", 4, 1, 2),
        ("minecraft:bowl", 3, 1, 2),
    ]),
    "utility": (2, 4, [
        ("minecraft:stick", 8, 2, 6),
        ("minecraft:string", 8, 1, 4),
        ("minecraft:leather", 5, 1, 2),
        ("minecraft:iron_nugget", 4, 1, 3),
        ("minecraft:coal", 5, 1, 3),
        ("minecraft:flint", 6, 1, 3),
    ]),
    "personal": (1, 3, [
        ("minecraft:paper", 8, 1, 3),
        ("minecraft:book", 3, 1, 1),
        ("slavicmyths:linen_thread", 6, 1, 3),
        ("minecraft:bread", 4, 1, 2),
    ]),
    "elder": (1, 3, [
        ("minecraft:gold_nugget", 5, 1, 2),
        ("minecraft:paper", 7, 1, 3),
        ("minecraft:book", 3, 1, 1),
        ("minecraft:iron_nugget", 5, 1, 3),
    ]),
}


def limits(value):
    if isinstance(value, int):
        return value, value
    assert value["type"] == "minecraft:uniform"
    return value["min"], value["max"]


def entry_spec(entry):
    assert entry["type"] == "minecraft:item"
    assert len(entry["functions"]) == 1
    fn = entry["functions"][0]
    assert fn["function"] == "minecraft:set_count"
    lo, hi = limits(fn["count"])
    return entry["name"], entry.get("weight", 1), lo, hi


def load(name):
    data = json.loads((LOOT / ("nether_communal_" + name + ".json")).read_text())
    assert data["type"] == "minecraft:chest"
    pools = data["pools"]
    assert len(pools) == (2 if name == "elder" else 1), name
    low, high, entries = EXPECTED[name]
    assert limits(pools[0]["rolls"]) == (low, high), name
    assert [entry_spec(entry) for entry in pools[0]["entries"]] == entries, name
    assert not pools[0].get("conditions"), name
    if name == "elder":
        bonus = pools[1]
        assert limits(bonus["rolls"]) == (1, 1)
        assert bonus["conditions"] == [{"condition": "minecraft:random_chance", "chance": 0.2}]
        assert [entry_spec(entry) for entry in bonus["entries"]] == [("slavicmyths:ancient_coin", 1, 1, 2)]
    return low, high, entries


tables = {name: load(name) for name in EXPECTED}
total = Counter()
bonus_houses = 0
for seed in range(10_000):
    rng = random.Random(seed)
    house = Counter()
    for name, (min_rolls, max_rolls, entries) in tables.items():
        rolls = rng.randint(min_rolls, max_rolls)
        weight = sum(e[1] for e in entries)
        assert weight > 0
        for _ in range(rolls):
            pick = rng.randrange(weight)
            for item, w, lo, hi in entries:
                if pick < w:
                    house[item] += rng.randint(lo, hi)
                    break
                pick -= w
            else:
                raise AssertionError("Invalid weighted selection")
        if name == "elder" and rng.random() < 0.2:
            house["slavicmyths:ancient_coin"] += rng.randint(1, 2)
    coins = house["slavicmyths:ancient_coin"]
    assert 0 <= coins <= 2, ("Unexpected elder chest coins", seed)
    bonus_houses += coins > 0
    total.update(house)
assert 1800 <= bonus_houses <= 2200, bonus_houses
assert 2600 <= total["slavicmyths:ancient_coin"] <= 3400, total["slavicmyths:ancient_coin"]
print(f"PASS offline 10000 seeded houses: coin chest probability={bonus_houses / 10000:.2%}, "
      f"expected coin quantity={total['slavicmyths:ancient_coin'] / 10000:.4f}/house")
print("PASS all four approved loot JSON pools: exact rolls, weights, item counts, bonus chance")
