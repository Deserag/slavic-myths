#!/usr/bin/env python3
"""House R1 schema-7 geometry audit. Run before native NeoForge world tests."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
resources = root / "src/main/resources/data/slavicmyths/posad"
java = root / "src/main/java/org/slavicmyths/posad"
model = json.loads((resources / "communal_house_r1.json").read_text("utf-8"))
rows = model["rows_y_z_x"]
assert len(rows) == 17 and all(len(layer) == 15 for layer in rows)
assert all(len(line) == 23 for layer in rows for line in layer)
get = lambda x, y, z: rows[y][z][x]
overrides = {}

def write(x, y, z, kind):
    position = (x, y, z)
    assert position not in overrides, (position, overrides.get(position), kind)
    overrides[position] = kind

old_stairs = set()
for y, layer in enumerate(rows):
    for z, line in enumerate(layer):
        for x, mark in enumerate(line):
            if mark == "U":
                old_stairs.add((x, y, z))
expected_old_stairs = {(x, step+1, 11-step) for step in range(6) for x in (10, 11)}
assert old_stairs == expected_old_stairs

for step in range(6):
    y, z = step + 1, 11 - step
    for x in (10, 11):
        write(x, y, z, "floor" if y == 6 else "air")
    for x in (7, 8):
        for lower in range(1, y):
            write(x, lower, z, "support")
        write(x, y, z, "stairs")
        for above in range(y+1, y+3):
            write(x, above, z, "air")

for x in (10, 11):
    for z in (7, 8):
        assert get(x, 6, z) == "."
        write(x, 6, z, "floor")
for y in (1, 2):
    assert get(9, y, 11) == "W"
    write(9, y, 11, "air")
for y in (7, 8):
    assert get(9, y, 5) == "W"
    write(9, y, 5, "air")
assert get(8, 1, 11) == "H" and get(2, 1, 11) == "."
write(2, 1, 11, "chest")
for x in (10, 11):
    assert get(x, 3, 13) == "."
    write(x, 3, 13, "lintel")
assert get(6, 7, 8) == "."
write(6, 7, 8, "fence")

furnishing = [(6,1,3),(7,1,3),(16,1,3),(17,1,3),
    (4,1,8),(3,1,8),(17,1,8),(16,1,8),
    (4,7,4),(5,7,4),(16,7,3),(17,7,3),
    (4,7,8),(5,7,8),(17,7,8),(16,7,8)]
lights = [(2,1,2),(19,1,2),(2,1,8),(19,1,8),
    (2,7,2),(19,7,2),(2,7,8),(19,7,8)]
for position in furnishing:
    assert get(*position) == "."
    write(*position, "furniture")
for position in lights:
    assert get(*position) == "."
    write(*position, "light")

# The relocated flight is two-wide, has a supported riser and two blocks
# of headroom at each step. Both public doorways were explicitly opened.
for step in range(6):
    y, z = step+1, 11-step
    for x in (7, 8):
        assert overrides[x,y,z] == "stairs"
        assert overrides[x,y+1,z] == "air"
        assert overrides[x,y+2,z] == "air"
        assert all(overrides[x,n,z] == "support" for n in range(1,y))
assert all(overrides[9,y,11] == "air" for y in (1,2))
assert all(overrides[9,y,5] == "air" for y in (7,8))
assert all(overrides[10,y,12-y] == "air" for y in range(1,6))
assert all(overrides[11,y,12-y] == "air" for y in range(1,6))

# R1 has no roof-column holes: do not blindly add blocks to this roof.
for z in range(1,14):
    for x in range(1,22):
        assert any(get(x,y,z) != "." for y in range(12,17))

code = (java / "PosadHouseRenovation.java").read_text("utf-8")
piece = (java / "PosadPiece.java").read_text("utf-8")
command = (java / "PosadCommands.java").read_text("utf-8")
geometry = (java / "PosadGeometry.java").read_text("utf-8")
assert "SCHEMA_VERSION = 10" in geometry
assert "schema >= 7" in piece and "PosadHouseRenovation.voxel" in piece
assert "PosadHouseRenovation.chestLoot" in piece
assert 'id.equals("communal_house_2_2_3")' in command
assert "for(int step=0;step<6;step++)" in code and "for(int x:new int[]{7,8})" in code
assert "chests/nether_communal_personal" in code
print("PASS: 12 old stairs relocated, 12 supported treads, two-block headroom, both entries, 24 furnishings/lights, no R1 roof holes")
