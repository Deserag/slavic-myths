#!/usr/bin/env python3
"""Verify exact #355/#356 city-local sources against runtime compact models."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCES = (
    ("zeleynaya_2_2_7", "zeleynaya-izba-2.2.7", 1599, (76, 44), (25, 13, 26)),
    ("granary_2_2_8", "gryadki-zhitnitsa-2.2.8", 1349, (76, 12), (25, 11, 28)),
)
for ident, folder, expected, origin, size in SOURCES:
    raw = json.loads((ROOT / "docs/concepts/nether-settlement-2.2" / folder / "MODEL_R1.json").read_text(encoding="utf-8"))
    compact = json.loads((ROOT / "src/main/resources/data/slavicmyths/posad" / (ident + ".json")).read_text(encoding="utf-8"))
    assert compact["origin"] == list(origin) and compact["size"] == list(size), ident
    assert len(raw["blocks"]) == len(compact["blocks"]) == expected, ident
    seen = set()
    for original, packed in zip(raw["blocks"], compact["blocks"]):
        props = ",".join(f"{k}={v}" for k, v in sorted(original.get("state", {}).items()))
        state = original["id"] + (f"[{props}]" if props else "")
        assert packed[:3] == [original["u"], original["y"], original["v"]], ident
        assert compact["palette"][packed[3]] == state, ident
        assert tuple(packed[:3]) not in seen, ident
        seen.add(tuple(packed[:3]))
    chests = {tuple(row[:3]): row[3] for row in compact["chests"]}
    original_chests = set()
    for chest in raw["chests"].values():
        p = chest["local"]
        xyz = tuple(p if isinstance(p, list) else [p["u"], p["y"], p["v"]])
        original_chests.add(xyz)
        assert chests[xyz] == chest["loot_table"].removeprefix("slavicmyths:"), ident
    assert set(chests) == original_chests and len(chests) == 5, ident
    print(f"PASS {ident}: {expected} exact voxels, 5 source chests")
