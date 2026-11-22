#!/usr/bin/env python3
"""Validate catalog-bound weapon tooltip styling and bilingual skill translations."""
from __future__ import annotations
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CATALOG = ROOT / "docs/balance/weapon-audit-20261122/catalog.json"
STYLE = ROOT / "src/main/java/org/slavicmyths/client/WeaponTooltips.java"
LANG = ROOT / "src/main/resources/assets/slavicmyths/lang"
NINE = ROOT / "src/main/java/org/slavicmyths/ninemor/NineMorNamedItems.java"
SISTERS = ROOT / "src/main/java/org/slavicmyths/sisters/BossRewardWeapons.java"
MYTAR = ROOT / "src/main/java/org/slavicmyths/posad/boss/ShackledSickleItem.java"
REWARD = ROOT / "src/main/java/org/slavicmyths/rewards/RewardContent.java"
STORM = ROOT / "src/main/java/org/slavicmyths/item/StormStaffItem.java"
GOLD = {"serpent_crossbow", "three_breath_crossbow", "morovoy_staff", "storm_anchor", "storm_harpoon"}


def unique_pairs(pairs):
    result = {}
    for k, v in pairs:
        if k in result:
            raise AssertionError("duplicate locale key: " + k)
        result[k] = v
    return result


def load_locale(code):
    return json.loads((LANG / (code + ".json")).read_text(encoding="utf-8"), object_pairs_hook=unique_pairs)


def main():
    catalog = json.loads(CATALOG.read_text(encoding="utf-8"))
    assert isinstance(catalog, (list, dict))
    # The historical inventory is the source of truth for the 102 known IDs.
    text = (ROOT / "docs/balance/weapon-audit-20261122/CATALOG.md").read_text(encoding="utf-8")
    ids = re.findall(r"^\| ([a-z0-9_]+) \|", text, flags=re.M)
    assert len(ids) == len(set(ids)) == 102, "invalid catalog count"
    src = STYLE.read_text(encoding="utf-8")
    match = re.search(r"WEAPONS = Set.of\((.*?)\);", src, flags=re.S)
    assert match, "missing weapon title palette"
    styled = re.findall(r'"([a-z0-9_]+)"', match.group(1))
    assert len(styled) == len(set(styled)) == 102 and set(styled) == set(ids), "catalog style coverage mismatch"
    elder = re.search(r"ELDER_SERPENT_WEAPONS =\s*Set.of\((.*?)\);", src, flags=re.S)
    assert elder and set(re.findall(r'"([a-z0-9_]+)"', elder.group(1))) == GOLD
    assert "stack.has(DataComponents.CUSTOM_NAME)" in src, "custom name protection missing"
    for color in ["0xE4E4E7", "0x55FFFF", "0xAA55FF", "0xFFAA00", "0xD4AF37", "0xFF5555", "0x55FF55", "0xAAAAAA", "0xFFFF55"]:
        assert color in src, "missing color " + color
    ru, en = load_locale("ru_ru"), load_locale("en_us")
    for item_id in ids:
        key = "item.slavicmyths." + item_id
        assert key in ru and key in en, "missing bilingual weapon name: " + key
    refs = set()
    for source in [NINE, SISTERS, MYTAR, REWARD, STORM]:
        refs.update(re.findall(r'Component.translatable\("(tooltip\.slavicmyths\.[^"]+)"', source.read_text(encoding="utf-8")))
    refs.add("tooltip.slavicmyths.weapon.elder_serpent")
    for key in refs:
        assert key in ru and key in en, "missing bilingual key " + key
    assert "22 с" in ru["tooltip.slavicmyths.weapon.likho_staff"]
    assert "11" in ru["tooltip.slavicmyths.weapon.storm_staff"]
    assert "1,6" in ru["tooltip.slavicmyths.weapon.storm_staff"]
    print(f"PASS: {len(ids)} weapon colors, {len(GOLD)} provenance-verified elder weapons, {len(refs)} localized skill keys; locale entries {len(ru)}/{len(en)}")


if __name__ == "__main__":
    main()
