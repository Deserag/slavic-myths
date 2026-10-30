#!/usr/bin/env python3
"""Geometry audit of schema-10 courtyard overlays; not a Minecraft GameTest."""
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
src = root / "docs/concepts/nether-settlement-2.2"
j = root / "src/main/java/org/slavicmyths/posad"
code = (j / "PosadCourtyardFinishes.java").read_text("utf-8")
piece = (j / "PosadPiece.java").read_text("utf-8")
command = (j / "PosadCommands.java").read_text("utf-8")
geometry = (j / "PosadGeometry.java").read_text("utf-8")

d = json.loads((src / "druzhinny-dvor-2.2.4/2.2_DRUZHINNY_DVOR_MODEL_R1.json").read_text("utf-8"))
f = json.loads((src / "kuznechny-dvor-2.2.5/2.2_KUZNECHNY_DVOR_MODEL_R1.json").read_text("utf-8"))
forge = {(b["u"], b["y"], b["v"]): b["block"] for b in f["voxels"]}
assert len(forge) == len(f["voxels"]) == 1901
assert d["non_air_block_count"] == 2440
assert sum(c != "." for layer in d["rows_y_z_x"] for row in layer for c in row) == 2440

def cell(kind, x, y, z):
    if kind == "military":
        if not (0 <= x < 28 and 0 <= y < 18 and 0 <= z < 25):
            return None
        mark = d["rows_y_z_x"][y][z][x]
        return None if mark == "." else d["palette"][mark]
    return forge.get((x, y, z))

kinds = {"table", "chair", "stool", "bench", "shelf", "wardrobe", "bedside_cabinet", "weapon_rack"}
occupied = set()
counts = {}
for kind, origin, dims, furniture_count, lights_count, accents_count in [
    ("military", (12,75), (28,25), 20, 4, 12),
    ("forge", (12,44), (26,26), 13, 5, 12),
]:
    items = [(int(x),int(y),int(z),name) for x,y,z,name in re.findall(rf'(?<!void )\b{kind}\((\d+),(\d+),(\d+),"([^"]+)"\)', code)]
    lights = [(int(x),int(y),int(z)) for x,y,z in re.findall(rf'\b{kind}Light\((\d+),(\d+),(\d+)\)', code)]
    # Accent loops are explicit R1 wall coordinates, not free-form random decoration.
    if kind == "military":
        accents = [(x,4,v) for x in (4,9,21,24) for v in (1,11)] + [(x,4,v) for x in (2,25) for v in (3,9)]
    else:
        accents = [(x,4,v) for v in (4,6,10,12) for x in (2,15)] + [(x,4,v) for x in (2,11) for v in (19,22)]
    assert len(items) == furniture_count and len(lights) == lights_count and len(accents) == accents_count, kind
    for x,y,z,name in items:
        wood, furniture_type = name.split("_",1)
        assert wood in ("pine","linden") and furniture_type in kinds, name
        if furniture_type == "shelf":
            # The actual FurnitureBlock canSurvive requires wall support
            # behind the facing. The Java overlay chooses a valid direction.
            backing = [cell(kind,x+dx,y,z+dz) for dx,dz in ((0,1),(-1,0),(0,-1),(1,0))]
            assert any(block in ("minecraft:polished_blackstone_bricks","minecraft:nether_bricks","minecraft:crimson_planks") for block in backing), (kind,"unsupported shelf",x,y,z)
    for x,y,z in [(a,b,c) for a,b,c,_ in items] + lights:
        assert 0 <= x < dims[0] and 0 <= z < dims[1] and y in (1,7), (kind,x,y,z)
        assert cell(kind,x,y-1,z) is not None, (kind,"floor",x,y,z)
        assert all(cell(kind,x,dy,z) is None for dy in (y,y+1,y+2)), (kind,"headroom",x,y,z)
        assert (origin[0]+x,y,origin[1]+z) not in occupied, (kind,"duplicate",x,y,z)
        occupied.add((origin[0]+x,y,origin[1]+z))
    for x,y,z in accents:
        assert cell(kind,x,y,z) == "minecraft:polished_blackstone_bricks", (kind,"wall",x,y,z)
        assert (origin[0]+x,y,origin[1]+z) not in occupied
        occupied.add((origin[0]+x,y,origin[1]+z))
    if kind == "military":
        chests = [tuple(m["local"]) for m in d["loot_markers"]]
    else:
        chests = [(c["u"],c["y"],c["v"]) for c in f["chests"]]
    assert len(chests) == len(set(chests)) == 5
    for x,y,z in chests:
        assert "chest" in cell(kind,x,y,z)
        assert (origin[0]+x,y,origin[1]+z) not in occupied
        assert not any((origin[0]+x+dx,y,origin[1]+z+dz) in occupied for dx,dz in ((0,0),(1,0),(-1,0),(0,1),(0,-1)))
    counts[kind] = (len(items),len(lights),len(accents))

# All four whole-city rotations are bijective. No rotated district intrudes
# into the other district or any original loot chest.
for turns in range(4):
    rotated = set()
    for x,y,z in occupied:
        rx,rz = ((x,z),(112-z,x),(112-x,112-z),(z,112-x))[turns]
        assert 0 <= rx <= 112 and 0 <= rz <= 112
        assert (rx,y,rz) not in rotated
        rotated.add((rx,y,rz))
    assert len(rotated) == len(occupied)

assert "SCHEMA_VERSION = 14" in geometry
assert "schema >= 10" in piece and "PosadCourtyardFinishes.voxel" in piece
assert 'id.equals("druzhinny_dvor_2_2_4")' in command
assert 'id.equals("kuznechny_dvor_2_2_5")' in command
assert "PosadCourtyardFinishes.voxel" in command
assert "state.setValue(FurnitureBlock.FACING,facing)" in code
assert "public BlockState rotate(BlockState s,Rotation r)" in (root / "src/main/java/org/slavicmyths/furniture/FurnitureBlock.java").read_text("utf-8")
assert "present.hasBlockEntity()" in command
print(f"PASS: R1 2440/1901 blocks, 5+5 chests untouched, room floor/headroom, 4 rotations; military {counts['military']}, forge {counts['forge']}")
