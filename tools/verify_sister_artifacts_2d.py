#!/usr/bin/env python3
"""Check all generated 2D blessing variants and the five creative registrations."""
from pathlib import Path
import json
from PIL import Image
from generate_sister_artifacts_2d import NAMES,SISTERS,masks,produce
root=Path(__file__).resolve().parents[1]
assets=root/"src/main/resources/assets/slavicmyths"
models=assets/"models/item"
textures=assets/"textures/item/sisters_artifacts"
produce(root,check=True)
catalog=(assets/"creative_catalog.tsv").read_text(encoding="utf-8").splitlines()
assert len(masks())==42
assert [m for m in masks() if m>0 and m.bit_count()==3].__len__()==20
for name in NAMES:
    assert sum(row.startswith(name+"\t") for row in catalog)==1,(name,"creative duplicates")
    rootmodel=json.loads((models/f"{name}.json").read_text(encoding="utf8"))
    assert rootmodel["parent"]=="minecraft:item/generated" and "elements" not in rootmodel,name
    assert len(rootmodel["overrides"])==41
    for mask in masks():
        suffix=f"{mask:02x}"
        path=textures/f"{name}_{suffix}.png"
        with Image.open(path) as img:
            assert img.size==(64,64) and img.mode=="RGBA",(name,suffix,img.size,img.mode)
            assert img.getbbox() is not None,(name,suffix,"blank")
        if mask:
            model=json.loads((models/"sisters_artifacts"/f"{name}_{suffix}.json").read_text(encoding="utf8"))
            assert model["parent"]=="minecraft:item/generated" and "elements" not in model
    # All single-sister jewels must be visually distinct, not just the same file or a static base.
    singles=[(textures/f"{name}_{1<<i:02x}.png").read_bytes() for i in range(6)]
    assert len(set(singles))==6,(name,"all sisters must show different gemstones")
print("PASS: 5 2D items, 210 PNGs (64x64 RGBA), 210 flat item models, all 6 gem styles, 0 catalog duplicates")
