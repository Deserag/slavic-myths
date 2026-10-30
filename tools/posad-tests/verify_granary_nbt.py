#!/usr/bin/env python3
"""Exact 2.2.8 NBT, model and five unchanged loot tables. Offline, not Minecraft QA."""
from __future__ import annotations
import gzip
import json
from pathlib import Path
from verify_zeleynaya_nbt import Reader

ROOT=Path(__file__).resolve().parents[2]
MODEL=ROOT/"docs/concepts/nether-settlement-2.2/gryadki-zhitnitsa-2.2.8/MODEL_R1.json"
NBT=ROOT/"src/main/resources/data/slavicmyths/structure/posad/granary_2_2_8.nbt"
DRAFT=MODEL.parent/"loot_drafts"
LOOT=ROOT/"src/main/resources/data/slavicmyths/loot_table/chests"

def main():
    model=json.loads(MODEL.read_text("utf-8"))
    raw=NBT.read_bytes()
    assert raw[:2]==b"\\x1f\\x8b", "Not gzip"
    root=Reader(gzip.decompress(raw)).root()
    assert root["DataVersion"]==3955 and root["size"]==[25,11,28] and root["entities"]==[]
    blocks=root["blocks"]; palette=root["palette"]
    assert len(model["blocks"])==len(blocks)==1349 and len(palette)==43
    source={(b["u"],b["y"],b["v"]):b for b in model["blocks"]}
    assert len(source)==1349
    chests={tuple(c["local"][k] for k in ("u","y","v")):c["loot_table"] for c in model["chests"].values()}
    assert len(chests)==5
    unique=set()
    for block in blocks:
        pos=tuple(block["pos"])
        assert pos in source and pos not in unique, f"Missing/duplicate {pos}"
        unique.add(pos)
        assert 0<=block["state"]<len(palette)
        expected=source[pos]
        actual=palette[block["state"]]
        assert actual["Name"]==expected["id"], f"Wrong block {pos}"
        props={k:str(v).lower() if isinstance(v,bool) else str(v) for k,v in expected.get("state",{}).items()}
        assert actual.get("Properties",{})==props, f"Wrong blockstate {pos}"
        if pos in chests:
            assert actual["Name"]=="minecraft:chest"
            assert block["nbt"]=={"id":"minecraft:chest","LootTable":chests[pos]},f"Wrong loot {pos}"
        else:
            assert "nbt" not in block, f"Unexpected block entity {pos}"
    for table in chests.values():
        assert table.startswith("slavicmyths:chests/posad_granary_")
        name=table.split("/",1)[1]+".json"
        assert json.loads((DRAFT/name).read_text("utf-8"))==json.loads((LOOT/name).read_text("utf-8")),f"Edited loot weights: {name}"
    print("PASS: 1349 exact authored granary blocks, 43 palette states, five lazy-loot chests, five original loot tables")
    print("OFFLINE ONLY: Minecraft/NeoForge QA required")

if __name__=="__main__":main()
