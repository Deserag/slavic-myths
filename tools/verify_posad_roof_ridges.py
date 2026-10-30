#!/usr/bin/env python3
"""Schema-14 communal/military real roof crest repair; static R1 geometry QA.

This does not replace Java 21 build, GameTest, roof lighting or visual Minecraft QA.
"""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
java = root / "src/main/java/org/slavicmyths/posad"
resources = root / "src/main/resources/data/slavicmyths/posad"
models = [
    ("communal", resources / "communal_house_r1.json", 13, 15, 16, 0, 22, 6, 7, 8, 1992),
    ("military", root / "docs/concepts/nether-settlement-2.2/druzhinny-dvor-2.2.4/2.2_DRUZHINNY_DVOR_MODEL_R1.json", 12, 75, 17, 1, 26, 5, 6, 7, 2440),
]
patched = set()

for name, path, ox, oz, y, xmin, xmax, north, center, south, expected_blocks in models:
    source = json.loads(path.read_text(encoding="utf-8"))
    rows, palette = source["rows_y_z_x"], source["palette"]
    assert source["non_air_block_count"] == expected_blocks
    assert sum(c != "." for layer in rows for row in layer for c in row) == expected_blocks
    assert len(rows) == (17 if name == "communal" else 18)
    for x in range(xmin, xmax + 1):
        assert palette[rows[y][center][x]] == "minecraft:nether_bricks"
        for z, facing, neighbor in ((north, "north", north - 1), (south, "south", south + 1)):
            assert palette[rows[y][z][x]] == "minecraft:nether_bricks", (name,x,y,z)
            assert palette[rows[y-1][neighbor][x]].startswith("minecraft:nether_brick_stairs[facing=" + facing), (name,x,neighbor)
            city_x, city_z = ox + x, oz + z
            assert (city_x,y,city_z) not in patched
            patched.add((city_x,y,city_z))
    # The replacement creates a single-block-wide full masonry crest,
    # bounded symmetrically by vanilla stairs with the correct inward rise.
    roof_zmax = 14 if name == "communal" else 12
    for x in range(xmin, xmax + 1):
        for z in range(roof_zmax + 1):
            assert any(rows[k][z][x] != "." for k in range(12, y + 1)), (name,x,z)
        for z in range((roof_zmax + 1) // 2):
            def state(zlocal):
                mark = rows[y][zlocal][x]
                if zlocal == north: return "minecraft:nether_brick_stairs[facing=north]"
                if zlocal == south: return "minecraft:nether_brick_stairs[facing=south]"
                return palette.get(mark, "minecraft:air")
            left, right = state(z), state(roof_zmax - z)
            if "stairs" in left or "stairs" in right:
                assert "facing=north" in left and "facing=south" in right, (name,x,z,left,right)
            else:
                assert left == right, (name,x,z,left,right)

assert len(patched) == 98
for turn in range(4):
    rotated = set()
    for x,y,z in patched:
        wx,wz = ((x,z),(112-z,x),(112-x,112-z),(z,112-x))[turn]
        assert 0 <= wx <= 112 and 0 <= wz <= 112
        assert (wx,y,wz) not in rotated
        rotated.add((wx,y,wz))
    assert len(rotated) == 98

repair = (java / "PosadDistrictRoofRepair.java").read_text(encoding="utf-8")
piece = (java / "PosadPiece.java").read_text(encoding="utf-8")
commands = (java / "PosadCommands.java").read_text(encoding="utf-8")
geometry = (java / "PosadGeometry.java").read_text(encoding="utf-8")
assert "SCHEMA_VERSION = 16" in geometry
assert "schema >= 14" in piece and "PosadDistrictRoofRepair.voxel(x,y,z)" in piece
assert piece.index("schema >= 14") < piece.index("schema >= 13")
assert "PosadDistrictRoofRepair.voxel(x,y,z)" in commands
assert 'id.equals("communal_house_2_2_3") || id.equals("druzhinny_dvor_2_2_4")' in commands
assert "present.hasBlockEntity()" in commands and "world.setBlock(target,roof,2)" in commands
assert "source.is(Blocks.NETHER_BRICKS)" in repair and "lowerPitch.getValue(StairBlock.FACING) != facing" in repair
assert "PosadBuildings.voxel(x,16,22)" in repair and "PosadNextBuildings.voxel(x,17,81)" in repair
print("PASS: immutable 1992/2440 R1 blocks, 98 supported roof-crest stair repairs, full roof coverage, 4 rotations, city and standalone parity wiring")
