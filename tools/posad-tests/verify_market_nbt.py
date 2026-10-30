#!/usr/bin/env python3
"""Check exact authored market NBT, all chest bindings and eight original loot drafts."""
import gzip
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0,str(ROOT/"tools/posad-tests"))
from verify_zeleynaya_nbt import Reader

MODEL = ROOT/"docs/concepts/nether-settlement-2.2/menovye-ryady-2.2.6/2.2_6_MARKET_VOXELS_DRAFT_A.json"
NBT = ROOT/"src/main/resources/data/slavicmyths/structure/posad/menovye_ryady_2_2_6.nbt"
DRAFT = ROOT/"docs/concepts/nether-settlement-2.2/menovye-ryady-2.2.6/loot_drafts"
LOOT = ROOT/"src/main/resources/data/slavicmyths/loot_table/chests"

def verify():
    model=json.loads(MODEL.read_text(encoding="utf-8"))
    root=Reader(gzip.decompress(NBT.read_bytes())).root()
    assert root["DataVersion"]==3955 and root["size"]==[27,12,25] and root["entities"]==[]
    assert len(model["blocks"])==len(root["blocks"])==2203
    palette=root["palette"]
    original={(b["u"],b["y"],b["v"]):b["id"] for b in model["blocks"]}
    assert len(original)==2203
    chests={(x["u"],x["y"],x["v"]):x["loot_table"] for x in model["chests"]}
    assert len(chests)==8
    placed={}
    for block in root["blocks"]:
        pos=tuple(block["pos"])
        assert pos in original and pos not in placed
        assert 0 <= block["state"] < len(palette)
        p=palette[block["state"]]
        name=p["Name"]
        props=p.get("Properties",{})
        state=name+("["+",".join(f"{k}={v}" for k,v in sorted(props.items()))+"]" if props else "")
        def canonical(id):
            base,sep,rest=id.partition("[")
            return base+("["+",".join(sorted(rest[:-1].split(",")))+"]" if sep else "")
        assert canonical(state)==canonical(original[pos]),f"Wrong state {pos}"
        if pos in chests:
            assert name=="minecraft:chest"
            assert block["nbt"]=={"id":"minecraft:chest","LootTable":chests[pos]}
        else:
            assert "nbt" not in block
        placed[pos]=block
    assert len(placed)==2203
    for table in chests.values():
        name=table.split("/",1)[1]
        assert table.startswith("slavicmyths:chests/nether_market_")
        runtime=json.loads((LOOT/(name+".json")).read_text(encoding="utf-8"))
        approved=json.loads((DRAFT/(name+".json")).read_text(encoding="utf-8"))
        assert runtime==approved,f"Altered approved loot table {name}"
    print("PASS: 2203 exact market voxels, 25 palette states, 8 loot chests, eight unchanged approved loot drafts")

if __name__=="__main__":
    verify()
