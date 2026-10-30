#!/usr/bin/env python3
"""Schema-16 granary roof repair and Zeleynaya roof integrity/static support QA.

Never infer GameTest, light levels, survival placement or visual approval.
"""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
data = root / "src/main/resources/data/slavicmyths/posad"
java = root / "src/main/java/org/slavicmyths/posad"
granary = json.loads((data / "granary_2_2_8.json").read_text(encoding="utf-8"))
zeleynaya = json.loads((data / "zeleynaya_2_2_7.json").read_text(encoding="utf-8"))
assert granary["origin"] == [76,12] and granary["size"] == [25,11,28]
assert zeleynaya["origin"] == [76,44] and zeleynaya["size"] == [25,13,26]
assert len(granary["blocks"]) == 1349 and len(granary["chests"]) == 5
assert len(zeleynaya["blocks"]) == 1599 and len(zeleynaya["chests"]) == 5

def voxels(model):
    result = {}
    for x,y,z,palette_id in model["blocks"]:
        assert (x,y,z) not in result, (x,y,z)
        result[x,y,z] = model["palette"][palette_id]
    return result

g, z = voxels(granary), voxels(zeleynaya)
assert len(g) == 1349 and len(z) == 1599
edits = set()

# Exact 13x2 R1 granary slab platform on full-block masonry (no new
# coordinates, roof holes or unsupported cantilevers).
for v in range(1,14):
    for u, facing, flank in ((7,"west",6),(8,"east",9)):
        assert g[u,10,v] == "minecraft:nether_brick_slab[type=bottom,waterlogged=false]"
        assert g[u,9,v] == "minecraft:nether_bricks"
        assert g[flank,8,v].startswith("minecraft:nether_brick_stairs[facing=" + facing)
        assert (76+u,10,12+v) not in edits
        edits.add((76+u,10,12+v))
    # All adjacent original pitches are consistent and intentionally untouched.
    for y in range(6,9):
        offset = 2 * (y - 6)
        for u, facing in ((1+offset,"west"),(2+offset,"west"),(13-offset,"east"),(14-offset,"east")):
            assert g[u,y,v].startswith("minecraft:nether_brick_stairs[facing=" + facing)

# Main R1 Zeleynaya roof is already a continuous double-thick pitched
# surface with one central ridge, so DO NOT cosmetically overwrite it.
# The 6x5 slab drying canopy is purpose-built, not a roof hole.
hanging = 0
for v in range(1,18):
    assert z[9,10,v] == "minecraft:nether_bricks"
    for y in range(6,10):
        offset = 2 * (y - 6)
        for u, facing in ((1+offset,"west"),(2+offset,"west"),(16-offset,"east"),(17-offset,"east")):
            assert z[u,y,v].startswith("minecraft:nether_brick_stairs[facing=" + facing)
for v in range(12,17):
    for u in range(18,24):
        assert z[u,5,v] == "minecraft:crimson_slab"
for (u,y,v), state in z.items():
    if state.startswith("minecraft:lantern[hanging=true"):
        assert z[u,y+1,v] == "minecraft:crimson_planks"
        hanging += 1
assert hanging == 6

# Separate granary tool shed has a supported, original 2-row slab ridge
# over stair slopes; it is not subject to the main-roof repair.
for v in range(19,27):
    for u in (7,8):
        assert g[u,8,v] == "minecraft:crimson_slab[type=bottom,waterlogged=false]"
        assert g[u,7,v].startswith("minecraft:crimson_stairs[")
assert len(edits) == 26

for turn in range(4):
    rotated = set()
    for x,y,zcity in edits:
        wx,wz = ((x,zcity),(112-zcity,x),(112-x,112-zcity),(zcity,112-x))[turn]
        assert (wx,y,wz) not in rotated and 0 <= wx <= 112 and 0 <= wz <= 112
        rotated.add((wx,y,wz))
    assert len(rotated) == 26

roof = (java / "PosadEasternRoofRepair.java").read_text(encoding="utf-8")
piece = (java / "PosadPiece.java").read_text(encoding="utf-8")
commands = (java / "PosadCommands.java").read_text(encoding="utf-8")
geometry = (java / "PosadGeometry.java").read_text(encoding="utf-8")
assert "SCHEMA_VERSION = 17" in geometry
assert "schema >= 16" in piece and "PosadEasternRoofRepair.voxel(x,y,z)" in piece
assert piece.index("schema >= 16") < piece.index("schema >= 15")
assert 'id.equals("granary_2_2_8")' in commands and "PosadEasternRoofRepair.voxel(76+u,10,12+v)" in commands
assert "present.hasBlockEntity()" in commands and "world.setBlock(target,finish,2)" in commands
assert "source.getValue(SlabBlock.TYPE) != SlabType.BOTTOM" in roof
assert "outward.getValue(StairBlock.FACING) != facing" in roof
print("PASS: 1349/1599 immutable R1 blocks; granary 26 supported roof ridge stairs; Zeleynaya 272 source slope stairs, 17 ridge blocks, 30 canopy slabs, 6 supported lanterns; all 4 rotations and native/standalone wiring")
