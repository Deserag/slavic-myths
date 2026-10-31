#!/usr/bin/env python3
"""Offline contract audit for the owner-approved #373 economy matrix.
Only verifies static data: NOT a substitute for NeoForge client/server trading tests.
"""
import json
from collections import Counter, defaultdict
from fractions import Fraction
from pathlib import Path

root = Path(__file__).resolve().parents[2]
data = json.loads((root / "src/main/resources/data/slavicmyths/posad/trades.json").read_text(encoding="utf-8"))
coin = "slavicmyths:ancient_coin"
assert data["schema"] == 1 and data["currency"] == coin
assert data["thresholds"] == [0, 10, 70, 150, 250]
offers = data["offers"]
roles = set("POSADNIK GUARD SOTNIK BLACKSMITH WEAPONSMITH CRAFTER FARMER MUSHROOM_GROWER HERBALIST COOK STOREKEEPER CIVILIAN".split())
fixed = {"POSADNIK", "CIVILIAN"}
profession = roles - fixed
assert len(offers) == 142
assert {x["role"] for x in offers} == roles
assert Counter(x["type"] for x in offers) == {"sale": 108, "buy": 22, "barter": 12}
assert len({x["id"] for x in offers}) == len(offers)
expected_sale_prices = {
    "GUARD": [[10, 8], [24, 18], [34, 42], [40, 50], [64, 64]],
    "SOTNIK": [[38, 18], [38, 25], [54, 40], [60, 32], [64, 64]],
    "BLACKSMITH": [[20, 12], [24, 20], [36, 42], [38, 40], [60, 62]],
    "WEAPONSMITH": [[26, 20], [46, 36], [54, 52], [62, 64], [64, 64]],
    "CRAFTER": [[14, 10], [22, 30], [32, 36], [44, 52], [64, 60]],
    "FARMER": [[8, 9], [12, 12], [13, 13], [28, 36], [48, 56]],
    "MUSHROOM_GROWER": [[12, 16], [20, 24], [34, 18], [24, 24], [50, 56]],
    "HERBALIST": [[12, 14], [16, 18], [30, 32], [48, 52], [64, 62]],
    "COOK": [[12, 14], [18, 20], [28, 26], [42, 38], [56, 48]],
    "STOREKEEPER": [[14, 10], [20, 16], [28, 22], [44, 40], [62, 60]]
}
for role in profession:
    for tier, prices in enumerate(expected_sale_prices[role], 1):
        matched = [o for o in offers if o["role"] == role and o["tier"] == tier and o["type"] == "sale"]
        assert len(matched) == 2, (role, tier)
        assert [o["a"]["count"] for o in matched] == prices, (role, tier)
assert [o["a"]["count"] for o in offers if o["role"] == "POSADNIK" and o["type"] == "sale"] == [16, 32, 45, 55]
assert [o["a"]["count"] for o in offers if o["role"] == "CIVILIAN" and o["type"] == "sale"] == [8, 12, 6, 10]

for role in roles:
    assert any(o["role"] == role and o["type"] == "buy" for o in offers), role
    assert any(o["role"] == role and o["type"] == "barter" for o in offers), role

future = {"slavicmyths:garlic_clove", "slavicmyths:heat_berry_cutting", "slavicmyths:ash_mushroom",
          "slavicmyths:infernal_shchi", "slavicmyths:druzhina_roast", "slavicmyths:garlic_flatbread"}
found = set()
for o in offers:
    assert o["tier"] in range(1, 6)
    assert o["role"] in roles
    if o["role"] in fixed:
        assert o["tier"] == 1
    for key in ("a", "b", "out"):
        if key not in o:
            continue
        stack = o[key]
        assert 1 <= stack["count"] <= 64 and ":" in stack["id"], o
        if stack["id"] in future:
            assert o.get("deferred") is True, o
            found.add(stack["id"])
    if o["type"] == "sale":
        assert o["a"]["id"] == coin and o["out"]["id"] != coin
    elif o["type"] == "buy":
        assert o["out"]["id"] == coin and o["a"]["id"] != coin and "b" not in o
    else:
        assert o["a"]["id"] != coin and o["out"]["id"] != coin
        assert o.get("b", {}).get("id") != coin
    if "fallback" in o:
        assert o["deferred"] is True
        assert all(x["id"] not in future for x in o["fallback"].values())
assert found == future
assert sum(bool(x.get("pristine")) for x in offers) == 2
assert sum(bool(x.get("enchant")) for x in offers) == 1
assert sum("b" in x and x["a"]["id"] == coin for x in offers) == 7

# Direct coin -> good -> coin cycles must never create coins, across cities or roles.
sales = defaultdict(list)
for o in offers:
    if o["type"] == "sale" and "b" not in o and not o.get("deferred"):
        sales[o["out"]["id"]].append(Fraction(o["a"]["count"], o["out"]["count"]))
for o in offers:
    if o["type"] == "buy" and not o.get("deferred"):
        for buy_price in sales[o["a"]["id"]]:
            assert buy_price * o["a"]["count"] >= o["out"]["count"], o["id"]

stew = next(x for x in offers if x["id"] == "cook_barter_1")
assert (stew["a"]["count"], stew["b"]["count"], stew["out"]["count"]) == (4, 2, 1)
assert next(x for x in offers if x["id"] == "guard_sale_5_2")["enchant"] == "minecraft:piercing"
print("Posad economy #373 static audit: PASS, 142 offers, 12 roles, 100 tiered sales, 24 reverse/barter role families")
